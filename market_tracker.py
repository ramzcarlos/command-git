import os
import json
import time
import datetime as dt
from typing import Any, Dict, List, Optional, Tuple
import redis


def _today_str(t: Optional[float] = None) -> str:
    return dt.datetime.fromtimestamp(t or time.time()).strftime("%Y%m%d")


class MarketTracker:
    """
    PoC plug-and-play para historial de clientes y métricas de mercado con Redis.
    - Eventos por cliente y stream global
    - Top items por día (ZSET)
    - Usuarios únicos por día (HLL)
    -es un PoC con redis
    
    """

    def __init__(
        self,
        redis_url: str = None,
        *,
        maxlen_per_customer_stream: int = 2000,
        global_stream_maxlen: int = 200_000,
        stats_ttl_days: int = 90
    ):
        self.r = redis.from_url(redis_url or os.getenv("REDIS_URL", "redis://localhost:6379/0"))
        self.maxlen_cust = maxlen_per_customer_stream
        self.maxlen_global = global_stream_maxlen
        self.stats_ttl_days = stats_ttl_days

    # ---------- Claves ----------
    def k_cust_stream(self, cust_id: str) -> str:
        return f"cust:{cust_id}:events"

    def k_global_stream(self) -> str:
        return "events"

    def k_items_stats(self, day: str) -> str:
        return f"stats:items:{day}"

    def k_uv_stats(self, day: str) -> str:
        return f"stats:uv:{day}"

    # ---------- API principal ----------
    def track_event(
        self,
        cust_id: str,
        event_type: str,
        *,
        item_id: Optional[str] = None,
        query: Optional[str] = None,
        metadata: Optional[Dict[str, Any]] = None,
        ts: Optional[float] = None
    ) -> str:
        """
        Registra un evento. Ejemplos event_type: "view_item", "search", "add_to_cart", "purchase".
        Solo IDs, evita PII. 'metadata' debe ser JSON-serializable.
        """
        ts_ms = int((ts or time.time()) * 1000)
        payload = {
            "cust_id": cust_id,
            "type": event_type,
            "ts": str(ts_ms),
        }
        if item_id:
            payload["item_id"] = item_id
        if query:
            payload["query"] = query
        if metadata:
            payload["meta"] = json.dumps(metadata, ensure_ascii=False)

        pipe = self.r.pipeline(transaction=False)

        # 1) stream por cliente (con retención)
        pipe.xadd(self.k_cust_stream(cust_id), payload, maxlen=self.maxlen_cust, approximate=True)

        # 2) stream global (para ETL batch/consumers)
        pipe.xadd(self.k_global_stream(), payload, maxlen=self.maxlen_global, approximate=True)

        # 3) Métricas
        day = _today_str(ts or time.time())
        # 3.1) usuarios únicos por día (HLL)
        pipe.pfadd(self.k_uv_stats(day), cust_id)
        pipe.expire(self.k_uv_stats(day), self.stats_ttl_days * 86400)

        # 3.2) popularidad por ítem (si aplica)
        if item_id and event_type in {"view_item", "add_to_cart", "purchase"}:
            increment = {"view_item": 1, "add_to_cart": 3, "purchase": 10}[event_type]
            zkey = self.k_items_stats(day)
            pipe.zincrby(zkey, increment, item_id)
            pipe.expire(zkey, self.stats_ttl_days * 86400)

        # Ejecuta
        res = pipe.execute()
        # Primer XADD devuelve el ID del entry
        return res[0]

    def get_customer_history(
        self,
        cust_id: str,
        *,
        start: str = "-",
        end: str = "+",
        limit: int = 100
    ) -> List[Dict[str, Any]]:
        """
        Devuelve eventos del cliente (orden cronológico).
        start/end son IDs de stream. Por defecto todo. 'limit' recorta el resultado.
        """
        entries = self.r.xrange(self.k_cust_stream(cust_id), min=start, max=end, count=limit)
        out = []
        for ev_id, fields in entries:
            # fields es dict bytes->bytes
            obj = {k.decode(): v.decode() for k, v in fields.items()}
            obj["_id"] = ev_id.decode()
            out.append(obj)
        return out

    def get_top_items(self, day: Optional[str] = None, top_n: int = 10) -> List[Tuple[str, float]]:
        """
        Ranking de ítems por día (por defecto hoy). Retorna [(item_id, score), ...]
        """
        day = day or _today_str()
        zkey = self.k_items_stats(day)
        res = self.r.zrevrange(zkey, 0, top_n - 1, withscores=True)
        # decode item ids
        return [(i.decode(), s) for i, s in res]

    def get_unique_users(self, day: Optional[str] = None) -> int:
        """
        Usuarios únicos (HLL) por día.
        """
        day = day or _today_str()
        return self.r.pfcount(self.k_uv_stats(day))

    # ---------- Utilidades ----------
    def read_global_since(self, last_id: str = "0-0", count: int = 100) -> List[Tuple[str, Dict[str, Any]]]:
        """
        Lee el stream global a partir de last_id (útil para consumidores).
        """
        res = self.r.xread({self.k_global_stream(): last_id}, count=count, block=0)
        if not res:
            return []
        _, entries = res[0]
        out = []
        for ev_id, fields in entries:
            obj = {k.decode(): v.decode() for k, v in fields.items()}
            out.append((ev_id.decode(), obj))
        return out
