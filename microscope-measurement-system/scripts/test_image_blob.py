#!/usr/bin/env python3
"""
Script de Teste: Armazenamento de Imagens como BLOB
Testa o salvamento e recuperação de imagens no banco Oracle
"""

import sys
import os
import cv2
import numpy as np

# Adiciona o diretório python-vision ao path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'python-vision'))

from oracle_integration import OracleIntegration

def create_test_image():
    """Cria uma imagem de teste simples."""
    # Cria uma imagem 640x480 com gradiente
    img = np.zeros((480, 640, 3), dtype=np.uint8)

    # Adiciona gradiente colorido
    for i in range(480):
        for j in range(640):
            img[i, j] = [i % 256, j % 256, (i + j) % 256]

    # Adiciona texto
    cv2.putText(img, "Teste BLOB", (200, 240), cv2.FONT_HERSHEY_SIMPLEX,
                2, (255, 255, 255), 3)

    return img

def test_image_blob_storage():
    """Testa o armazenamento de imagens como BLOB."""
    print("=" * 70)
    print("TESTE: Armazenamento de Imagens como BLOB no Oracle")
    print("=" * 70)

    # Conecta ao Oracle
    oracle = OracleIntegration()

    print("\n1. Conectando ao banco Oracle...")
    if not oracle.connect():
        print("❌ Falha ao conectar ao banco Oracle")
        return False

    print("✅ Conectado ao banco Oracle")

    try:
        # Cria imagem de teste
        print("\n2. Criando imagem de teste...")
        test_image = create_test_image()
        print(f"✅ Imagem criada: {test_image.shape[1]}x{test_image.shape[0]} pixels")

        # Converte para bytes
        print("\n3. Convertendo imagem para bytes (JPEG)...")
        success, buffer = cv2.imencode('.jpg', test_image)
        if not success:
            print("❌ Falha ao codificar imagem")
            return False

        image_bytes = buffer.tobytes()
        print(f"✅ Imagem codificada: {len(image_bytes)} bytes")

        # Prepara dados de teste
        from datetime import datetime
        timestamp = int(datetime.now().timestamp())
        image_id = f"IMG_TEST_{timestamp}"
        sample_id = "SAMPLE_TEST"

        # Garante que a amostra existe
        print(f"\n4. Garantindo que amostra '{sample_id}' existe...")
        oracle.create_sample_if_not_exists(sample_id, "Amostra de Teste")

        # Salva a imagem com BLOB
        print(f"\n5. Salvando imagem '{image_id}' no banco com BLOB...")

        measurement_data = {
            'nomeImagem': f'{image_id}.jpg',
            'sampleId': sample_id,
            'resolucao': '640x480',
            'formato': 'JPG'
        }

        success = oracle.create_image_if_not_exists(
            image_id=image_id,
            measurement_data=measurement_data,
            image_bytes=image_bytes
        )

        if not success:
            print("❌ Falha ao salvar imagem no banco")
            return False

        print("✅ Imagem salva com sucesso!")

        # Verifica se o BLOB foi salvo
        print(f"\n6. Verificando se o BLOB foi salvo corretamente...")
        cursor = oracle.connection.cursor()
        cursor.execute("""
            SELECT
                IMAGE_ID,
                ARQUIVO,
                DBMS_LOB.GETLENGTH(IMAGEM_BLOB) as BLOB_SIZE,
                FORMATO
            FROM TB_MICROSCOPY_IMAGES
            WHERE IMAGE_ID = :1
        """, [image_id])

        row = cursor.fetchone()
        cursor.close()

        if not row:
            print(f"❌ Imagem '{image_id}' não encontrada no banco")
            return False

        stored_id, arquivo, blob_size, formato = row

        print(f"\n✅ BLOB verificado no banco de dados:")
        print(f"   - IMAGE_ID: {stored_id}")
        print(f"   - ARQUIVO: {arquivo}")
        print(f"   - BLOB_SIZE: {blob_size} bytes")
        print(f"   - FORMATO: {formato}")

        if blob_size != len(image_bytes):
            print(f"\n⚠️  AVISO: Tamanho do BLOB difere do esperado")
            print(f"   Esperado: {len(image_bytes)} bytes")
            print(f"   Encontrado: {blob_size} bytes")

        # Tenta recuperar o BLOB
        print(f"\n7. Recuperando BLOB do banco...")
        cursor = oracle.connection.cursor()
        cursor.execute("""
            SELECT IMAGEM_BLOB
            FROM TB_MICROSCOPY_IMAGES
            WHERE IMAGE_ID = :1
        """, [image_id])

        row = cursor.fetchone()
        if row and row[0]:
            retrieved_blob = row[0].read()
            print(f"✅ BLOB recuperado: {len(retrieved_blob)} bytes")

            # Tenta decodificar a imagem
            nparr = np.frombuffer(retrieved_blob, np.uint8)
            recovered_image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            if recovered_image is not None:
                print(f"✅ Imagem decodificada com sucesso: {recovered_image.shape}")

                # Salva a imagem recuperada para comparação visual
                output_path = os.path.join(
                    os.path.dirname(__file__),
                    '..',
                    'data-integration',
                    f'{image_id}_recovered.jpg'
                )
                cv2.imwrite(output_path, recovered_image)
                print(f"✅ Imagem recuperada salva em: {output_path}")
            else:
                print("❌ Falha ao decodificar imagem recuperada")
                return False
        else:
            print("❌ BLOB não encontrado ou vazio")
            return False

        cursor.close()

        print("\n" + "=" * 70)
        print("✅ TESTE CONCLUÍDO COM SUCESSO!")
        print("=" * 70)
        print("\nO sistema está funcionando corretamente:")
        print("  ✓ Imagens são convertidas para bytes")
        print("  ✓ BLOBs são salvos no banco Oracle")
        print("  ✓ BLOBs podem ser recuperados e decodificados")
        print(f"\nImagem de teste criada: {image_id}")
        print("\n")

        return True

    except Exception as e:
        print(f"\n❌ ERRO durante o teste: {e}")
        import traceback
        traceback.print_exc()
        return False

    finally:
        oracle.disconnect()

if __name__ == "__main__":
    success = test_image_blob_storage()
    sys.exit(0 if success else 1)