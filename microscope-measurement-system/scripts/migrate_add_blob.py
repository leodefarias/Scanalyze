#!/usr/bin/env python3
"""
Script de Migração: Adiciona campo BLOB para imagens
Executa a migração do banco de dados Oracle para adicionar o campo IMAGEM_BLOB
"""

import sys
import os

# Adiciona o diretório python-vision ao path para importar oracle_integration
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'python-vision'))

from oracle_integration import OracleIntegration

def run_migration():
    """Executa a migração do banco de dados."""
    print("=" * 70)
    print("MIGRAÇÃO: Adicionar campo BLOB para imagens")
    print("=" * 70)

    # Cria instância da integração Oracle
    oracle = OracleIntegration()

    # Conecta ao banco
    print("\n1. Conectando ao banco Oracle...")
    if not oracle.connect():
        print("❌ Falha ao conectar ao banco Oracle")
        print("Verifique as credenciais e conectividade de rede")
        return False

    print("✅ Conectado ao banco Oracle")

    try:
        cursor = oracle.connection.cursor()

        # Verifica se a coluna já existe
        print("\n2. Verificando se a coluna IMAGEM_BLOB já existe...")
        cursor.execute("""
            SELECT COUNT(*) FROM USER_TAB_COLUMNS
            WHERE TABLE_NAME = 'TB_MICROSCOPY_IMAGES'
            AND COLUMN_NAME = 'IMAGEM_BLOB'
        """)

        column_exists = cursor.fetchone()[0] > 0

        if column_exists:
            print("⚠️  A coluna IMAGEM_BLOB já existe na tabela TB_MICROSCOPY_IMAGES")
            print("Nenhuma alteração necessária")
            cursor.close()
            oracle.disconnect()
            return True

        print("✅ Coluna IMAGEM_BLOB não existe, prosseguindo com a migração...")

        # Adiciona a coluna BLOB
        print("\n3. Adicionando coluna IMAGEM_BLOB do tipo BLOB...")
        cursor.execute("""
            ALTER TABLE TB_MICROSCOPY_IMAGES ADD (
                IMAGEM_BLOB BLOB
            )
        """)

        # Adiciona comentário
        print("4. Adicionando comentário descritivo...")
        cursor.execute("""
            COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.IMAGEM_BLOB
            IS 'Conteúdo binário da imagem armazenado como BLOB'
        """)

        # Commit das alterações
        oracle.connection.commit()
        print("✅ Alterações comitadas com sucesso")

        # Verifica a estrutura atualizada
        print("\n5. Verificando estrutura atualizada da tabela...")
        cursor.execute("""
            SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE
            FROM USER_TAB_COLUMNS
            WHERE TABLE_NAME = 'TB_MICROSCOPY_IMAGES'
            ORDER BY COLUMN_ID
        """)

        print("\n" + "=" * 70)
        print("ESTRUTURA DA TABELA TB_MICROSCOPY_IMAGES")
        print("=" * 70)
        print(f"{'COLUMN_NAME':<30} {'DATA_TYPE':<15} {'LENGTH':<10} {'NULL?':<10}")
        print("-" * 70)

        for row in cursor:
            column_name, data_type, data_length, nullable = row
            length_str = str(data_length) if data_length else "N/A"
            print(f"{column_name:<30} {data_type:<15} {length_str:<10} {nullable:<10}")

        cursor.close()

        print("\n" + "=" * 70)
        print("✅ MIGRAÇÃO CONCLUÍDA COM SUCESSO!")
        print("=" * 70)
        print("\nA tabela TB_MICROSCOPY_IMAGES agora possui o campo IMAGEM_BLOB")
        print("O sistema está pronto para armazenar imagens diretamente no banco de dados")
        print("\n")

        return True

    except Exception as e:
        print(f"\n❌ ERRO durante a migração: {e}")
        if oracle.connection:
            try:
                oracle.connection.rollback()
                print("⚠️  Rollback executado")
            except Exception:
                pass
        return False

    finally:
        oracle.disconnect()

if __name__ == "__main__":
    success = run_migration()
    sys.exit(0 if success else 1)