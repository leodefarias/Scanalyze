#!/usr/bin/env python3
"""
Módulo de Integração Oracle para Sistema de Micromedição
Este módulo implementa a conexão direta entre Python e Oracle Database,
permitindo inserção automática de medições sem necessidade de arquivos intermediários.

Autor: Sistema de Micromedição
Versão: 1.0
"""

import cx_Oracle
import json
import os
from datetime import datetime
from typing import Dict, List, Optional, Any
import logging

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class OracleIntegration:
    """
    Classe para integração direta com Oracle Database.
    Gerencia conexões e operações CRUD para o sistema de micromedição.
    """

    def __init__(self):
        """Inicializa a integração Oracle."""
        # Configurações Oracle FIAP (mesmas do Java)
        self.oracle_config = {
            'user': 'RM555211',
            'password': '281005',
            'dsn': 'oracle.fiap.com.br:1521/orcl'
        }
        self.connection = None
        self._setup_cx_oracle()

    def _setup_cx_oracle(self):
        """Configura cx_Oracle se disponível."""
        try:
            # Tenta importar cx_Oracle
            global cx_Oracle
            import cx_Oracle
            logger.info("✅ cx_Oracle disponível para conexão Oracle")
        except ImportError:
            logger.warning("⚠️ cx_Oracle não instalado. Usando modo fallback JSON")
            cx_Oracle = None

    def connect(self) -> bool:
        """
        Estabelece conexão com Oracle Database.

        Returns:
            bool: True se conexão bem-sucedida, False caso contrário
        """
        if cx_Oracle is None:
            logger.info("📁 Usando modo JSON fallback (cx_Oracle não disponível)")
            return False

        try:
            self.connection = cx_Oracle.connect(
                user=self.oracle_config['user'],
                password=self.oracle_config['password'],
                dsn=self.oracle_config['dsn']
            )
            logger.info("✅ Conexão Oracle estabelecida com sucesso!")
            return True

        except cx_Oracle.Error as e:
            logger.error(f"❌ Erro na conexão Oracle: {e}")
            return False
        except Exception as e:
            logger.error(f"❌ Erro inesperado na conexão: {e}")
            return False

    def disconnect(self):
        """Fecha conexão com Oracle Database."""
        if self.connection:
            try:
                self.connection.close()
                logger.info("✅ Conexão Oracle fechada")
            except Exception as e:
                logger.error(f"❌ Erro ao fechar conexão: {e}")

    def test_connection(self) -> bool:
        """
        Testa a conexão com Oracle Database.

        Returns:
            bool: True se teste bem-sucedido, False caso contrário
        """
        if not self.connect():
            return False

        try:
            cursor = self.connection.cursor()
            cursor.execute("SELECT 'Oracle Connection Test' as STATUS FROM DUAL")
            result = cursor.fetchone()
            cursor.close()

            if result:
                logger.info(f"✅ Teste de conexão: {result[0]}")
                return True
            else:
                logger.error("❌ Teste de conexão falhou")
                return False

        except Exception as e:
            logger.error(f"❌ Erro no teste de conexão: {e}")
            return False
        finally:
            self.disconnect()

    def insert_measurement(self, measurement_data: Dict[str, Any]) -> bool:
        """
        Insere uma medição diretamente no Oracle.

        Args:
            measurement_data: Dicionário com dados da medição

        Returns:
            bool: True se inserção bem-sucedida, False caso contrário
        """
        if not self.connection:
            # Fallback para JSON se não há conexão Oracle
            return self._save_to_json_fallback(measurement_data)

        try:
            cursor = self.connection.cursor()

            # SQL para inserir medição (simplificado, assumindo dados existentes)
            sql = """
            INSERT INTO TB_MEASUREMENTS (
                ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK,
                MICROSCOPE_ID_FK, OPERATOR_ID_FK, AREA_PIXELS,
                AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO,
                METODO_PROCESSAMENTO, VALIDADA, DATA_CRIACAO
            ) VALUES (
                SQ_MEASUREMENTS.NEXTVAL, :measurement_id,
                (SELECT ID FROM TB_SAMPLES WHERE SAMPLE_ID = :sample_id),
                (SELECT ID FROM TB_MICROSCOPY_IMAGES WHERE IMAGE_ID = :image_id),
                (SELECT ID FROM TB_DIGITAL_MICROSCOPES WHERE MICROSCOPE_ID = 'MICRO001'),
                (SELECT ID FROM TB_OPERATORS WHERE OPERATOR_ID = 'OP001'),
                :area_pixels, :area_um2, :scale,
                TO_DATE(:data_hora, 'YYYY-MM-DD HH24:MI:SS'),
                'OpenCV Automatic Detection', 'N', SYSDATE
            )
            """

            # Prepara dados para inserção
            data = {
                'measurement_id': measurement_data.get('id'),
                'sample_id': measurement_data.get('sampleId'),
                'image_id': measurement_data.get('imagemId'),
                'area_pixels': measurement_data.get('area_pixels'),
                'area_um2': measurement_data.get('area_um2'),
                'scale': measurement_data.get('scale_pixels_per_um', 10.0),
                'data_hora': measurement_data.get('dataHora')
            }

            cursor.execute(sql, data)
            self.connection.commit()
            cursor.close()

            logger.info(f"✅ Medição {data['measurement_id']} inserida no Oracle")
            return True

        except Exception as e:
            logger.error(f"❌ Erro ao inserir medição no Oracle: {e}")
            if self.connection:
                self.connection.rollback()
            return False

    def _save_to_json_fallback(self, measurement_data: Dict[str, Any]) -> bool:
        """
        Salva medição em JSON como fallback.

        Args:
            measurement_data: Dados da medição

        Returns:
            bool: True se salvamento bem-sucedido
        """
        try:
            json_file = "../data-integration/measurements.json"

            # Carrega dados existentes
            if os.path.exists(json_file):
                with open(json_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
            else:
                data = {"measurements": []}

            # Adiciona nova medição
            data["measurements"].append(measurement_data)

            # Salva arquivo atualizado
            with open(json_file, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)

            logger.info(f"✅ Medição salva em JSON (fallback): {measurement_data.get('id')}")
            return True

        except Exception as e:
            logger.error(f"❌ Erro ao salvar JSON fallback: {e}")
            return False

    def get_measurements_count(self) -> int:
        """
        Obtém o número total de medições no banco.

        Returns:
            int: Número de medições ou -1 se erro
        """
        if not self.connection:
            return -1

        try:
            cursor = self.connection.cursor()
            cursor.execute("SELECT COUNT(*) FROM TB_MEASUREMENTS")
            count = cursor.fetchone()[0]
            cursor.close()
            return count

        except Exception as e:
            logger.error(f"❌ Erro ao contar medições: {e}")
            return -1

    def get_operators(self) -> List[Dict[str, Any]]:
        """
        Obtém lista de operadores do sistema.

        Returns:
            List: Lista de operadores ou lista vazia se erro
        """
        if not self.connection:
            return []

        try:
            cursor = self.connection.cursor()
            cursor.execute("""
                SELECT OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO
                FROM TB_OPERATORS
                WHERE ATIVO = 'S'
                ORDER BY NOME
            """)

            operators = []
            for row in cursor:
                operators.append({
                    'operator_id': row[0],
                    'nome': row[1],
                    'email': row[2],
                    'nivel_acesso': row[3]
                })

            cursor.close()
            return operators

        except Exception as e:
            logger.error(f"❌ Erro ao buscar operadores: {e}")
            return []

    def create_sample_if_not_exists(self, sample_id: str, sample_name: str = None) -> bool:
        """
        Cria uma amostra se ela não existir.

        Args:
            sample_id: ID da amostra
            sample_name: Nome da amostra (opcional)

        Returns:
            bool: True se criação/existência confirmada
        """
        if not self.connection:
            return False

        try:
            cursor = self.connection.cursor()

            # Verifica se amostra já existe
            cursor.execute("SELECT COUNT(*) FROM TB_SAMPLES WHERE SAMPLE_ID = :1", [sample_id])
            exists = cursor.fetchone()[0] > 0

            if exists:
                cursor.close()
                return True

            # Cria nova amostra
            cursor.execute("""
                INSERT INTO TB_SAMPLES (
                    ID, SAMPLE_ID, NOME, TIPO, DATA_COLETA,
                    OPERADOR_RESPONSAVEL, STATUS, DATA_CRIACAO
                ) VALUES (
                    SQ_SAMPLES.NEXTVAL, :1, :2, 'Microscópica', SYSDATE,
                    'Sistema Automático', 'ATIVA', SYSDATE
                )
            """, [sample_id, sample_name or f"Amostra {sample_id}"])

            self.connection.commit()
            cursor.close()

            logger.info(f"✅ Amostra {sample_id} criada no Oracle")
            return True

        except Exception as e:
            logger.error(f"❌ Erro ao criar amostra {sample_id}: {e}")
            if self.connection:
                self.connection.rollback()
            return False

def install_cx_oracle():
    """
    Instala cx_Oracle automaticamente se não estiver disponível.
    """
    try:
        import cx_Oracle
        logger.info("✅ cx_Oracle já está instalado")
        return True
    except ImportError:
        logger.info("📦 Instalando cx_Oracle...")
        try:
            import subprocess
            import sys
            subprocess.check_call([sys.executable, "-m", "pip", "install", "cx_Oracle"])
            logger.info("✅ cx_Oracle instalado com sucesso!")
            return True
        except Exception as e:
            logger.error(f"❌ Erro ao instalar cx_Oracle: {e}")
            logger.info("💡 Para instalar manualmente: pip install cx_Oracle")
            return False

# Exemplo de uso e teste
if __name__ == "__main__":
    print("🔬 Teste da Integração Oracle")
    print("="*50)

    # Tenta instalar cx_Oracle se necessário
    install_cx_oracle()

    # Testa integração
    oracle = OracleIntegration()

    if oracle.test_connection():
        print("🎉 Integração Oracle funcionando!")

        # Testa operações básicas
        count = oracle.get_measurements_count()
        if count >= 0:
            print(f"📊 Medições no banco: {count}")

        operators = oracle.get_operators()
        if operators:
            print(f"👥 Operadores cadastrados: {len(operators)}")
            for op in operators[:3]:  # Mostra apenas 3 primeiros
                print(f"   - {op['nome']} ({op['nivel_acesso']})")
    else:
        print("⚠️ Usando modo JSON fallback")

        # Testa salvamento JSON
        test_measurement = {
            "id": f"MEAS_TEST_{int(datetime.now().timestamp())}",
            "sampleId": "SAMPLE_TEST",
            "area_pixels": 1500,
            "area_um2": 15.0,
            "dataHora": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "imagemId": "IMG_TEST",
            "nomeImagem": "test_image.jpg",
            "operator": "Sistema Teste",
            "scale_pixels_per_um": 10.0
        }

        if oracle._save_to_json_fallback(test_measurement):
            print("✅ Teste JSON fallback funcionando!")

    oracle.disconnect()
    print("🏁 Teste concluído!")