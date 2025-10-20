#!/usr/bin/env python3
"""
Script de Migração: Imagens Locais → Banco de Dados Oracle (BLOB)

Migra imagens do diretório data-integration/ para o campo IMAGEM_BLOB
da tabela TB_MICROSCOPY_IMAGES no banco Oracle.

Uso:
    python3 migrate_images_to_blob.py                    # Migração completa
    python3 migrate_images_to_blob.py --dry-run          # Simula sem modificar banco
    python3 migrate_images_to_blob.py --skip-processed   # Ignora imagens _processed
    python3 migrate_images_to_blob.py --verbose          # Log detalhado
"""

import sys
import os
import argparse
from pathlib import Path
from datetime import datetime

# Adiciona o diretório python-vision ao path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'python-vision'))

try:
    from oracle_integration import OracleIntegration
    import oracledb
except ImportError as e:
    print(f"❌ Erro ao importar dependências: {e}")
    print("Certifique-se de que oracle_integration.py e oracledb estão disponíveis")
    sys.exit(1)


class ImageMigrator:
    """Classe para migrar imagens locais para o banco de dados."""

    def __init__(self, dry_run=False, skip_processed=False, verbose=False, batch_size=10):
        self.dry_run = dry_run
        self.skip_processed = skip_processed
        self.verbose = verbose
        self.batch_size = batch_size
        self.oracle = None

        # Estatísticas
        self.stats = {
            'found': 0,
            'migrated': 0,
            'already_has_blob': 0,
            'not_in_db': 0,
            'errors': 0,
            'total_size': 0,
            'skipped_processed': 0
        }

        # Diretório de imagens
        self.images_dir = Path(__file__).parent.parent / 'data-integration'

    def log(self, message, level='INFO'):
        """Log com controle de verbosidade."""
        if self.verbose or level in ['SUCCESS', 'ERROR', 'WARNING']:
            prefix = {
                'INFO': 'ℹ️ ',
                'SUCCESS': '✅',
                'ERROR': '❌',
                'WARNING': '⚠️ ',
                'PROGRESS': '▶️ '
            }.get(level, '')
            print(f"{prefix} {message}")

    def connect_to_database(self):
        """Conecta ao banco de dados Oracle."""
        self.log("Conectando ao banco de dados Oracle...")
        self.oracle = OracleIntegration()

        if not self.oracle.connect():
            self.log("Falha ao conectar ao banco Oracle", 'ERROR')
            return False

        self.log("Conectado ao banco Oracle", 'SUCCESS')

        # Detecta nomes das colunas
        self.detect_column_names()

        return True

    def detect_column_names(self):
        """Detecta os nomes das colunas da tabela."""
        cursor = self.oracle.connection.cursor()

        # Busca colunas da tabela
        cursor.execute("""
            SELECT COLUMN_NAME
            FROM USER_TAB_COLUMNS
            WHERE TABLE_NAME = 'TB_MICROSCOPY_IMAGES'
        """)

        columns = [row[0] for row in cursor]
        cursor.close()

        # Detecta nome da coluna de arquivo
        self.filename_column = 'NOME_ARQUIVO' if 'NOME_ARQUIVO' in columns else 'ARQUIVO'
        self.log(f"Coluna de arquivo detectada: {self.filename_column}")

        # Detecta nome da coluna de tamanho (pode não existir)
        self.size_column = None
        if 'TAMANHO_ARQUIVO' in columns:
            self.size_column = 'TAMANHO_ARQUIVO'
        elif 'TAMANHO_BYTES' in columns:
            self.size_column = 'TAMANHO_BYTES'
        elif 'FILE_SIZE' in columns:
            self.size_column = 'FILE_SIZE'

        if self.size_column:
            self.log(f"Coluna de tamanho detectada: {self.size_column}")
        else:
            self.log("Coluna de tamanho não encontrada (campo opcional)")

    def find_images(self):
        """Encontra todas as imagens no diretório."""
        self.log(f"Escaneando diretório: {self.images_dir}")

        patterns = ['*.jpg', '*.jpeg', '*.png', '*.JPG', '*.JPEG', '*.PNG']
        images = []

        for pattern in patterns:
            images.extend(self.images_dir.glob(pattern))

        # Filtra imagens processadas se necessário
        if self.skip_processed:
            original_count = len(images)
            images = [img for img in images if '_processed' not in img.name]
            self.stats['skipped_processed'] = original_count - len(images)
            self.log(f"Ignorando {self.stats['skipped_processed']} imagens processadas")

        self.stats['found'] = len(images)
        self.log(f"Encontradas {len(images)} imagens para migrar", 'SUCCESS')

        return sorted(images)

    def extract_image_id(self, filename):
        """Extrai o IMAGE_ID do nome do arquivo."""
        # Remove extensão e sufixos
        name = filename.stem

        # Remove _processed se presente
        name = name.replace('_processed', '')

        return name

    def check_if_image_exists_in_db(self, image_id, filename):
        """Verifica se a imagem existe no banco e se já tem BLOB."""
        cursor = self.oracle.connection.cursor()

        # Tenta por IMAGE_ID (usa nome de coluna detectado)
        query = f"""
            SELECT ID, IMAGE_ID, {self.filename_column},
                   DBMS_LOB.GETLENGTH(IMAGEM_BLOB) as BLOB_SIZE
            FROM TB_MICROSCOPY_IMAGES
            WHERE IMAGE_ID = :1 OR {self.filename_column} = :2
        """
        cursor.execute(query, [image_id, filename])

        result = cursor.fetchone()
        cursor.close()

        if result:
            db_id, db_image_id, db_filename, blob_size = result
            has_blob = blob_size is not None and blob_size > 0
            return {
                'exists': True,
                'db_id': db_id,
                'image_id': db_image_id,
                'filename': db_filename,
                'has_blob': has_blob,
                'blob_size': blob_size
            }

        return {'exists': False}

    def read_image_file(self, image_path):
        """Lê o arquivo de imagem como bytes."""
        try:
            with open(image_path, 'rb') as f:
                return f.read()
        except Exception as e:
            self.log(f"Erro ao ler arquivo {image_path.name}: {e}", 'ERROR')
            return None

    def update_image_blob(self, image_id, image_bytes, file_size):
        """Atualiza o BLOB da imagem no banco."""
        cursor = self.oracle.connection.cursor()

        try:
            # Configura o tipo BLOB
            cursor.setinputsizes(imagem_blob=oracledb.DB_TYPE_BLOB)

            # Monta query dinamicamente baseado nas colunas disponíveis
            if self.size_column:
                query = f"""
                    UPDATE TB_MICROSCOPY_IMAGES
                    SET IMAGEM_BLOB = :imagem_blob,
                        {self.size_column} = :file_size
                    WHERE IMAGE_ID = :image_id
                """
                params = {
                    'imagem_blob': image_bytes,
                    'file_size': file_size,
                    'image_id': image_id
                }
            else:
                # Se não tem coluna de tamanho, atualiza apenas o BLOB
                query = """
                    UPDATE TB_MICROSCOPY_IMAGES
                    SET IMAGEM_BLOB = :imagem_blob
                    WHERE IMAGE_ID = :image_id
                """
                params = {
                    'imagem_blob': image_bytes,
                    'image_id': image_id
                }

            cursor.execute(query, params)

            rows_updated = cursor.rowcount
            cursor.close()

            return rows_updated > 0

        except Exception as e:
            self.log(f"Erro ao atualizar BLOB para {image_id}: {e}", 'ERROR')
            cursor.close()
            return False

    def migrate_single_image(self, image_path):
        """Migra uma única imagem para o banco."""
        filename = image_path.name
        image_id = self.extract_image_id(image_path)

        self.log(f"Processando: {filename} (ID: {image_id})", 'PROGRESS')

        # Verifica se existe no banco
        db_info = self.check_if_image_exists_in_db(image_id, filename)

        if not db_info['exists']:
            self.log(f"  ⚠️  Não encontrado no banco: {filename}", 'WARNING')
            self.stats['not_in_db'] += 1
            return False

        if db_info['has_blob']:
            self.log(f"  ℹ️  Já possui BLOB ({db_info['blob_size']} bytes): {filename}")
            self.stats['already_has_blob'] += 1
            return True

        # Lê o arquivo
        image_bytes = self.read_image_file(image_path)
        if image_bytes is None:
            self.stats['errors'] += 1
            return False

        file_size = len(image_bytes)
        self.log(f"  📄 Arquivo lido: {file_size:,} bytes")

        # Modo dry-run: apenas simula
        if self.dry_run:
            self.log(f"  🔍 [DRY-RUN] Atualizaria BLOB para: {db_info['image_id']}")
            self.stats['migrated'] += 1
            self.stats['total_size'] += file_size
            return True

        # Atualiza o banco
        success = self.update_image_blob(db_info['image_id'], image_bytes, file_size)

        if success:
            self.log(f"  ✅ BLOB atualizado: {db_info['image_id']} ({file_size:,} bytes)", 'SUCCESS')
            self.stats['migrated'] += 1
            self.stats['total_size'] += file_size
            return True
        else:
            self.log(f"  ❌ Falha ao atualizar BLOB: {db_info['image_id']}", 'ERROR')
            self.stats['errors'] += 1
            return False

    def migrate_all_images(self, images):
        """Migra todas as imagens em lotes."""
        total = len(images)

        for i, image_path in enumerate(images, 1):
            self.log(f"\n[{i}/{total}] Migrando: {image_path.name}")

            self.migrate_single_image(image_path)

            # Commit em lotes
            if not self.dry_run and i % self.batch_size == 0:
                self.oracle.connection.commit()
                self.log(f"💾 Commit do lote (imagens {i-self.batch_size+1}-{i})")

        # Commit final
        if not self.dry_run:
            self.oracle.connection.commit()
            self.log("💾 Commit final realizado")

    def print_report(self):
        """Imprime relatório final da migração."""
        print("\n" + "=" * 70)
        print("RELATÓRIO DE MIGRAÇÃO DE IMAGENS")
        print("=" * 70)
        print(f"Total de imagens encontradas:      {self.stats['found']}")

        if self.skip_processed:
            print(f"Imagens processadas ignoradas:      {self.stats['skipped_processed']}")

        print(f"Imagens migradas com sucesso:       {self.stats['migrated']}")
        print(f"Imagens que já tinham BLOB:         {self.stats['already_has_blob']}")
        print(f"Imagens sem registro no banco:      {self.stats['not_in_db']}")
        print(f"Erros durante migração:             {self.stats['errors']}")
        print(f"Tamanho total migrado:              {self.stats['total_size'] / (1024*1024):.2f} MB")

        if self.dry_run:
            print("\n⚠️  MODO DRY-RUN: Nenhuma modificação foi feita no banco")

        print("=" * 70)

        # Status final
        if self.stats['errors'] == 0 and self.stats['migrated'] > 0:
            print("\n✅ MIGRAÇÃO CONCLUÍDA COM SUCESSO!")
        elif self.stats['errors'] > 0:
            print(f"\n⚠️  MIGRAÇÃO CONCLUÍDA COM {self.stats['errors']} ERRO(S)")
        else:
            print("\n ℹ️ NENHUMA IMAGEM FOI MIGRADA")

        print()

    def run(self):
        """Executa o processo completo de migração."""
        print("=" * 70)
        print("MIGRAÇÃO DE IMAGENS LOCAIS PARA BANCO DE DADOS ORACLE")
        print("=" * 70)

        if self.dry_run:
            print("⚠️  MODO DRY-RUN ATIVADO - Nenhuma modificação será feita no banco")

        print()

        # Conecta ao banco
        if not self.connect_to_database():
            return False

        # Encontra imagens
        images = self.find_images()

        if not images:
            self.log("Nenhuma imagem encontrada para migrar", 'WARNING')
            return True

        # Confirma migração se não for dry-run
        if not self.dry_run:
            print(f"\n⚠️  Você está prestes a migrar {len(images)} imagens para o banco Oracle.")
            print("   Esta operação irá atualizar os registros existentes.")
            response = input("   Deseja continuar? [s/N]: ")

            if response.lower() not in ['s', 'sim', 'y', 'yes']:
                print("❌ Migração cancelada pelo usuário")
                return False

        # Migra imagens
        print()
        self.migrate_all_images(images)

        # Imprime relatório
        self.print_report()

        # Desconecta
        self.oracle.disconnect()

        return self.stats['errors'] == 0


def main():
    """Função principal."""
    parser = argparse.ArgumentParser(
        description='Migra imagens locais para o banco de dados Oracle como BLOB'
    )

    parser.add_argument(
        '--dry-run',
        action='store_true',
        help='Simula a migração sem modificar o banco de dados'
    )

    parser.add_argument(
        '--skip-processed',
        action='store_true',
        help='Ignora imagens com sufixo _processed'
    )

    parser.add_argument(
        '--verbose', '-v',
        action='store_true',
        help='Exibe log detalhado de todas as operações'
    )

    parser.add_argument(
        '--batch-size',
        type=int,
        default=10,
        help='Número de imagens por lote de commit (padrão: 10)'
    )

    args = parser.parse_args()

    # Cria e executa migrador
    migrator = ImageMigrator(
        dry_run=args.dry_run,
        skip_processed=args.skip_processed,
        verbose=args.verbose,
        batch_size=args.batch_size
    )

    success = migrator.run()
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()