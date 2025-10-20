#!/usr/bin/env python3
"""Deleta os últimos 10 registros de medições do Oracle."""

import sys
import argparse
from datetime import datetime

try:
    import oracledb
except ImportError as exc:
    sys.stderr.write("oracledb não encontrado. Execute: pip install oracledb\n")
    sys.exit(1)

DB_CONFIG = {
    "user": "RM555211",
    "password": "281005",
    "dsn": "oracle.fiap.com.br:1521/orcl",
}

# Query para buscar os últimos 10 registros
QUERY_SELECT = """
    SELECT MEASUREMENT_ID, AREA_MICROMETERS, DATA_MEDICAO, SAMPLE_ID_FK
    FROM TB_MEASUREMENTS
    ORDER BY DATA_MEDICAO DESC
    FETCH FIRST 10 ROWS ONLY
"""

def main() -> int:
    """Conecta ao Oracle e deleta os últimos 10 registros de medições."""

    # Parse argumentos
    parser = argparse.ArgumentParser(description="Deleta os últimos 10 registros de medições")
    parser.add_argument("--confirm", action="store_true", help="Confirma a deleção sem prompt interativo")
    args = parser.parse_args()

    print("=" * 80)
    print("DELETAR ÚLTIMOS 10 REGISTROS DE MEDIÇÕES")
    print("=" * 80)
    print()

    # Conectar ao banco
    try:
        print("Conectando ao Oracle Database...")
        conn = oracledb.connect(**DB_CONFIG)
        print("✓ Conexão estabelecida com sucesso!\n")
    except Exception as exc:
        sys.stderr.write(f"✗ Falha ao conectar: {exc}\n")
        return 1

    try:
        # Buscar os últimos 10 registros
        print("Buscando os últimos 10 registros de medições...")
        with conn.cursor() as cursor:
            cursor.execute(QUERY_SELECT)
            rows = cursor.fetchall()

        if not rows:
            print("✗ Nenhuma medição encontrada.")
            return 0

        print(f"✓ Encontrados {len(rows)} registros\n")

        # Mostrar os registros que serão deletados
        print("Registros que serão DELETADOS:")
        print("-" * 80)
        print(f"{'MEASUREMENT_ID':<20} | {'AREA_UM2':<15} | {'DATA_MEDICAO':<20} | {'SAMPLE_ID_FK':<10}")
        print("-" * 80)

        measurement_ids = []
        for measurement_id, area_um2, timestamp, sample_id_fk in rows:
            measurement_ids.append(measurement_id)

            if isinstance(timestamp, datetime):
                formatted = timestamp.strftime("%Y-%m-%d %H:%M:%S")
            else:
                formatted = str(timestamp)

            print(f"{measurement_id:<20} | {area_um2:<15.6f} | {formatted:<20} | {sample_id_fk:<10}")

        print("-" * 80)
        print()

        # Confirmar deleção
        print(f"⚠️  ATENÇÃO: Você está prestes a deletar {len(measurement_ids)} registros!")
        print("⚠️  Esta ação NÃO PODE ser desfeita.")
        print()

        if not args.confirm:
            try:
                confirmacao = input("Digite 'CONFIRMAR' para prosseguir com a deleção: ")
                if confirmacao.strip().upper() != "CONFIRMAR":
                    print("\n✗ Operação cancelada pelo usuário.")
                    return 0
            except EOFError:
                print("\n✗ Modo não-interativo detectado. Use --confirm para confirmar a deleção.")
                return 1
        else:
            print("✓ Confirmação automática via --confirm")

        # Realizar a deleção
        print("\nIniciando deleção...")

        # Criar placeholders para a query IN
        placeholders = ", ".join([f":id{i}" for i in range(len(measurement_ids))])
        delete_query = f"DELETE FROM TB_MEASUREMENTS WHERE MEASUREMENT_ID IN ({placeholders})"

        with conn.cursor() as cursor:
            # Preparar os parâmetros
            params = {f"id{i}": mid for i, mid in enumerate(measurement_ids)}

            cursor.execute(delete_query, params)
            deleted_count = cursor.rowcount

            # Commit da transação
            conn.commit()

            print(f"✓ {deleted_count} registros deletados com sucesso!")

        # Salvar backup dos IDs deletados
        backup_file = "deleted_measurements_backup.txt"
        with open(backup_file, "w") as f:
            f.write("REGISTROS DE MEDIÇÕES DELETADOS\n")
            f.write(f"Data: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"Total: {len(measurement_ids)} registros\n")
            f.write("-" * 80 + "\n")
            for mid in measurement_ids:
                f.write(f"{mid}\n")

        print(f"✓ Backup dos IDs deletados salvo em: {backup_file}")
        print()
        print("=" * 80)
        print("OPERAÇÃO CONCLUÍDA COM SUCESSO!")
        print("=" * 80)

    except Exception as exc:
        conn.rollback()
        sys.stderr.write(f"\n✗ Erro durante a operação: {exc}\n")
        return 1

    finally:
        conn.close()
        print("\n✓ Conexão fechada.")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
