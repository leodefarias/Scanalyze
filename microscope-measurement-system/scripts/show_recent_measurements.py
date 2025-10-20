#!/usr/bin/env python3
"""Mostra as últimas medições registradas no Oracle."""

import sys
from datetime import datetime

try:
    import oracledb
except ImportError as exc:  # pragma: no cover
    sys.stderr.write("oracledb não encontrado. Execute dentro do venv.\n")
    sys.exit(1)

DB_CONFIG = {
    "user": "RM555211",
    "password": "281005",
    "dsn": "oracle.fiap.com.br:1521/orcl",
}

QUERY = """
    SELECT MEASUREMENT_ID, AREA_MICROMETERS, DATA_MEDICAO
    FROM TB_MEASUREMENTS
    ORDER BY DATA_MEDICAO DESC
    FETCH FIRST 5 ROWS ONLY
"""

def main() -> int:
    try:
        conn = oracledb.connect(**DB_CONFIG)
    except Exception as exc:
        sys.stderr.write(f"Falha ao conectar: {exc}\n")
        return 1

    try:
        with conn.cursor() as cursor:
            cursor.execute(QUERY)
            rows = cursor.fetchall()
    finally:
        conn.close()

    if not rows:
        print("Nenhuma medição encontrada.")
        return 0

    print("MEASUREMENT_ID | AREA_UM2 | DATA")
    for measurement_id, area_um2, timestamp in rows:
        if isinstance(timestamp, datetime):
            formatted = timestamp.strftime("%Y-%m-%d %H:%M:%S")
        else:
            formatted = str(timestamp)
        print(f"{measurement_id} | {area_um2:.6f} μm² | {formatted}")

    return 0

if __name__ == "__main__":
    raise SystemExit(main())
