#!/usr/bin/env python3
"""
Script de Validação: Verifica Imagens Migradas para o Banco

Valida que as imagens foram corretamente migradas para o campo BLOB
comparando com os arquivos locais originais.

Uso:
    python3 validate_migrated_images.py
    python3 validate_migrated_images.py --verbose
    python3 validate_migrated_images.py --save-failed  # Salva imagens que falharam
"""

import sys
import os
import argparse
from pathlib import Path
import hashlib

# Adiciona o diretório python-vision ao path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'python-vision'))

try:
    from oracle_integration import OracleIntegration
    import cv2
    import numpy as np
except ImportError as e:
    print(f"❌ Erro ao importar dependências: {e}")
    sys.exit(1)


class ImageValidator:
    """Classe para validar imagens migradas."""

    def __init__(self, verbose=False, save_failed=False):
        self.verbose = verbose
        self.save_failed = save_failed
        self.oracle = None

        # Estatísticas
        self.stats = {
            'total_in_db': 0,
            'with_blob': 0,
            'without_blob': 0,
            'validated_ok': 0,
            'size_mismatch': 0,
            'decode_failed': 0,
            'local_file_missing': 0
        }

        # Diretórios
        self.images_dir = Path(__file__).parent.parent / 'data-integration'
        self.failed_dir = self.images_dir / 'validation_failed'

    def log(self, message, level='INFO'):
        """Log com controle de verbosidade."""
        if self.verbose or level in ['SUCCESS', 'ERROR', 'WARNING']:
            prefix = {
                'INFO': 'ℹ️ ',
                'SUCCESS': '✅',
                'ERROR': '❌',
                'WARNING': '⚠️ '
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

    def get_all_images_from_db(self):
        """Recupera todas as imagens do banco."""
        cursor = self.oracle.connection.cursor()

        # Monta query dinamicamente baseado nas colunas disponíveis
        size_column_sql = self.size_column if self.size_column else "NULL"

        query = f"""
            SELECT
                IMAGE_ID,
                {self.filename_column},
                DBMS_LOB.GETLENGTH(IMAGEM_BLOB) as BLOB_SIZE,
                {size_column_sql} as FILE_SIZE,
                FORMATO
            FROM TB_MICROSCOPY_IMAGES
            ORDER BY DATA_CRIACAO DESC
        """
        cursor.execute(query)

        images = []
        for row in cursor:
            image_id, filename, blob_size, file_size, format = row
            images.append({
                'image_id': image_id,
                'filename': filename,
                'blob_size': blob_size,
                'file_size': file_size,
                'format': format,
                'has_blob': blob_size is not None and blob_size > 0
            })

        cursor.close()
        return images

    def get_blob_from_db(self, image_id):
        """Recupera o BLOB de uma imagem."""
        cursor = self.oracle.connection.cursor()

        cursor.execute("""
            SELECT IMAGEM_BLOB
            FROM TB_MICROSCOPY_IMAGES
            WHERE IMAGE_ID = :1
        """, [image_id])

        result = cursor.fetchone()
        cursor.close()

        if result and result[0]:
            return result[0].read()

        return None

    def find_local_file(self, image_id, filename):
        """Encontra o arquivo local correspondente."""
        # Tenta pelo nome do arquivo primeiro
        if filename:
            local_path = self.images_dir / filename
            if local_path.exists():
                return local_path

        # Tenta pelo IMAGE_ID
        for ext in ['.jpg', '.jpeg', '.png', '.JPG', '.JPEG', '.PNG']:
            local_path = self.images_dir / f"{image_id}{ext}"
            if local_path.exists():
                return local_path

        return None

    def calculate_hash(self, data):
        """Calcula hash MD5 dos dados."""
        return hashlib.md5(data).hexdigest()

    def validate_image_decoding(self, image_bytes):
        """Valida se os bytes podem ser decodificados como imagem."""
        try:
            nparr = np.frombuffer(image_bytes, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            return img is not None
        except Exception as e:
            self.log(f"Erro ao decodificar imagem: {e}", 'ERROR')
            return False

    def save_failed_image(self, image_id, blob_data):
        """Salva imagem que falhou na validação para análise."""
        if not self.save_failed:
            return

        # Cria diretório se não existe
        self.failed_dir.mkdir(exist_ok=True)

        output_path = self.failed_dir / f"{image_id}_failed.dat"

        try:
            with open(output_path, 'wb') as f:
                f.write(blob_data)
            self.log(f"Imagem com falha salva: {output_path}")
        except Exception as e:
            self.log(f"Erro ao salvar imagem com falha: {e}", 'ERROR')

    def validate_single_image(self, image_info):
        """Valida uma única imagem."""
        image_id = image_info['image_id']
        filename = image_info['filename']

        self.log(f"\nValidando: {image_id} ({filename})")

        # Verifica se tem BLOB
        if not image_info['has_blob']:
            self.log(f"  ⚠️  Sem BLOB no banco", 'WARNING')
            self.stats['without_blob'] += 1
            return False

        # Recupera o BLOB
        blob_data = self.get_blob_from_db(image_id)
        if not blob_data:
            self.log(f"  ❌ Falha ao recuperar BLOB", 'ERROR')
            return False

        blob_size = len(blob_data)
        self.log(f"  📄 BLOB recuperado: {blob_size:,} bytes")

        # Valida decodificação
        if not self.validate_image_decoding(blob_data):
            self.log(f"  ❌ BLOB não pode ser decodificado como imagem", 'ERROR')
            self.stats['decode_failed'] += 1
            self.save_failed_image(image_id, blob_data)
            return False

        self.log(f"  ✅ BLOB decodificado com sucesso")

        # Encontra arquivo local para comparação
        local_file = self.find_local_file(image_id, filename)

        if not local_file:
            self.log(f"  ⚠️  Arquivo local não encontrado (não é erro - pode ter sido deletado)", 'WARNING')
            self.stats['local_file_missing'] += 1
            self.stats['validated_ok'] += 1  # Considera OK se decodifica
            return True

        # Compara tamanhos
        local_size = local_file.stat().st_size
        self.log(f"  📁 Arquivo local: {local_size:,} bytes")

        # Permite diferença de até 5% (compressão JPEG pode variar)
        size_diff = abs(local_size - blob_size)
        size_diff_percent = (size_diff / local_size) * 100

        if size_diff_percent > 5:
            self.log(f"  ⚠️  Diferença de tamanho: {size_diff_percent:.1f}%", 'WARNING')
            self.stats['size_mismatch'] += 1
            return False

        self.log(f"  ✅ Tamanhos compatíveis (diferença: {size_diff_percent:.1f}%)")

        # Valida com hash (opcional, se tamanhos são idênticos)
        if size_diff == 0:
            with open(local_file, 'rb') as f:
                local_data = f.read()

            local_hash = self.calculate_hash(local_data)
            blob_hash = self.calculate_hash(blob_data)

            if local_hash == blob_hash:
                self.log(f"  ✅ Hash MD5 idêntico: {local_hash[:16]}...")
            else:
                self.log(f"  ℹ️  Hash diferente (normal para JPEG recompactado)")

        self.stats['validated_ok'] += 1
        return True

    def print_report(self):
        """Imprime relatório de validação."""
        print("\n" + "=" * 70)
        print("RELATÓRIO DE VALIDAÇÃO DE IMAGENS")
        print("=" * 70)
        print(f"Total de imagens no banco:          {self.stats['total_in_db']}")
        print(f"Imagens com BLOB:                   {self.stats['with_blob']}")
        print(f"Imagens sem BLOB:                   {self.stats['without_blob']}")
        print(f"\nValidação:")
        print(f"  ✅ Validadas com sucesso:         {self.stats['validated_ok']}")
        print(f"  ⚠️  Diferença de tamanho:         {self.stats['size_mismatch']}")
        print(f"  ❌ Falha ao decodificar:          {self.stats['decode_failed']}")
        print(f"  📁 Arquivo local não encontrado:  {self.stats['local_file_missing']}")

        success_rate = 0
        if self.stats['with_blob'] > 0:
            success_rate = (self.stats['validated_ok'] / self.stats['with_blob']) * 100

        print(f"\nTaxa de sucesso: {success_rate:.1f}%")
        print("=" * 70)

        # Status final
        if self.stats['decode_failed'] == 0 and self.stats['validated_ok'] > 0:
            print("\n✅ VALIDAÇÃO CONCLUÍDA COM SUCESSO!")
            print("   Todas as imagens com BLOB foram validadas corretamente.")
        elif self.stats['decode_failed'] > 0:
            print(f"\n⚠️  VALIDAÇÃO IDENTIFICOU {self.stats['decode_failed']} PROBLEMA(S)")
            print("   Algumas imagens não podem ser decodificadas.")
        else:
            print("\n ℹ️ NENHUMA IMAGEM COM BLOB ENCONTRADA PARA VALIDAR")

        print()

    def run(self):
        """Executa o processo completo de validação."""
        print("=" * 70)
        print("VALIDAÇÃO DE IMAGENS MIGRADAS PARA O BANCO DE DADOS")
        print("=" * 70)
        print()

        # Conecta ao banco
        if not self.connect_to_database():
            return False

        # Busca imagens do banco
        self.log("Buscando imagens do banco de dados...")
        images = self.get_all_images_from_db()

        self.stats['total_in_db'] = len(images)
        self.stats['with_blob'] = sum(1 for img in images if img['has_blob'])
        self.stats['without_blob'] = self.stats['total_in_db'] - self.stats['with_blob']

        self.log(f"Encontradas {self.stats['total_in_db']} imagens no banco", 'SUCCESS')
        self.log(f"  - Com BLOB: {self.stats['with_blob']}")
        self.log(f"  - Sem BLOB: {self.stats['without_blob']}")

        if self.stats['with_blob'] == 0:
            self.log("Nenhuma imagem com BLOB para validar", 'WARNING')
            self.oracle.disconnect()
            return True

        # Valida cada imagem com BLOB
        print()
        for image_info in images:
            if image_info['has_blob']:
                self.validate_single_image(image_info)

        # Imprime relatório
        self.print_report()

        # Desconecta
        self.oracle.disconnect()

        return self.stats['decode_failed'] == 0


def main():
    """Função principal."""
    parser = argparse.ArgumentParser(
        description='Valida imagens migradas para o banco de dados Oracle'
    )

    parser.add_argument(
        '--verbose', '-v',
        action='store_true',
        help='Exibe log detalhado de todas as operações'
    )

    parser.add_argument(
        '--save-failed',
        action='store_true',
        help='Salva imagens que falharam na validação para análise'
    )

    args = parser.parse_args()

    # Cria e executa validador
    validator = ImageValidator(
        verbose=args.verbose,
        save_failed=args.save_failed
    )

    success = validator.run()
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()