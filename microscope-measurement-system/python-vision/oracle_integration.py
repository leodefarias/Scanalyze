#!/usr/bin/env python3
"""
Módulo de Integração Oracle para Sistema de Micromedição
Este módulo implementa a conexão direta entre Python e Oracle Database,
permitindo inserção automática de medições sem necessidade de arquivos intermediários.

Autor: Sistema de Micromedição
Versão: 1.0
"""

import oracledb
import json
import os
from datetime import datetime
from typing import Dict, List, Optional, Any
from pathlib import Path
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
        self._setup_oracledb()

    def _setup_oracledb(self):
        """Configura oracledb se disponível."""
        try:
            # Tenta importar oracledb
            global oracledb
            import oracledb
            logger.info("✅ oracledb disponível para conexão Oracle")
        except ImportError:
            logger.warning("⚠️ oracledb não instalado. Usando modo fallback JSON")
            oracledb = None

    def connect(self) -> bool:
        """
        Estabelece conexão com Oracle Database.

        Returns:
            bool: True se conexão bem-sucedida, False caso contrário
        """
        if oracledb is None:
            logger.info("📁 Usando modo JSON fallback (oracledb não disponível)")
            return False

        try:
            self.connection = oracledb.connect(
                user=self.oracle_config['user'],
                password=self.oracle_config['password'],
                dsn=self.oracle_config['dsn']
            )
            logger.info("✅ Conexão Oracle estabelecida com sucesso!")
            return True

        except oracledb.Error as e:
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
        Testa a conexão com Oracle Database e verifica se as tabelas existem.

        Returns:
            bool: True se teste bem-sucedido, False caso contrário
        """
        if not self.connect():
            return False

        try:
            cursor = self.connection.cursor()

            # Teste básico de conectividade
            cursor.execute("SELECT 'Oracle Connection Test' as STATUS FROM DUAL")
            result = cursor.fetchone()

            if not result:
                logger.error("❌ Teste de conexão falhou")
                return False

            logger.info(f"✅ Teste de conexão: {result[0]}")

            # Verificar se as tabelas necessárias existem
            tables_to_check = [
                'TB_OPERATORS',
                'TB_DIGITAL_MICROSCOPES',
                'TB_SAMPLES',
                'TB_MICROSCOPY_IMAGES',
                'TB_MEASUREMENTS'
            ]

            missing_tables = []
            for table in tables_to_check:
                cursor.execute("""
                    SELECT COUNT(*) FROM USER_TABLES
                    WHERE TABLE_NAME = :1
                """, [table])

                exists = cursor.fetchone()[0] > 0
                if not exists:
                    missing_tables.append(table)

            cursor.close()

            if missing_tables:
                logger.error(f"❌ Tabelas não encontradas: {', '.join(missing_tables)}")
                logger.error("💡 Execute o script setup_oracle_database.sql para criar as tabelas")
                return False

            logger.info("✅ Todas as tabelas necessárias foram encontradas")
            return True

        except Exception as e:
            logger.error(f"❌ Erro no teste de conexão: {e}")
            return False
        finally:
            self.disconnect()

    def insert_measurement(self, measurement_data: Dict[str, Any]) -> bool:
        """
        Insere uma medição diretamente no Oracle com criação automática de dependências.

        Args:
            measurement_data: Dicionário com dados da medição

        Returns:
            bool: True se inserção bem-sucedida, False caso contrário
        """
        if not self.connection:
            # Fallback para JSON se não há conexão Oracle
            logger.info("Usando fallback JSON (sem conexão Oracle)")
            self._save_to_json_fallback(measurement_data)
            return False

        try:
            # Garante que amostra existe
            sample_id = measurement_data.get('sampleId', 'AUTO_SAMPLE')
            if not self.create_sample_if_not_exists(sample_id):
                logger.warning(f"Não foi possível criar/verificar amostra {sample_id}")

            # Cria entrada de imagem se não existir
            image_id = measurement_data.get('imagemId')
            image_bytes = measurement_data.get('imagemBytes')  # Novo campo para bytes da imagem
            if image_id and not self.create_image_if_not_exists(image_id, measurement_data, image_bytes):
                logger.warning(f"Não foi possível criar imagem {image_id}")

            cursor = self.connection.cursor()

            # SQL para inserir medição com tratamento seguro de dependências
            sql = """
            INSERT INTO TB_MEASUREMENTS (
                ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK,
                MICROSCOPE_ID_FK, OPERATOR_ID_FK, AREA_PIXELS,
                AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO,
                METODO_PROCESSAMENTO, VALIDADA, DATA_CRIACAO
            ) VALUES (
                SQ_MEASUREMENTS.NEXTVAL, :measurement_id,
                (SELECT ID FROM TB_SAMPLES WHERE SAMPLE_ID = :sample_id AND ROWNUM = 1),
                (SELECT ID FROM TB_MICROSCOPY_IMAGES WHERE IMAGE_ID = :image_id AND ROWNUM = 1),
                (SELECT MIN(ID) FROM TB_DIGITAL_MICROSCOPES WHERE ROWNUM = 1),
                (SELECT MIN(ID) FROM TB_OPERATORS WHERE ROWNUM = 1),
                :area_pixels, :area_um2, :scale,
                TO_DATE(:data_hora, 'YYYY-MM-DD HH24:MI:SS'),
                'OpenCV Automatic Detection', 'N', SYSDATE
            )
            """

            # Prepara dados para inserção
            data = {
                'measurement_id': measurement_data.get('id'),
                'sample_id': sample_id,
                'image_id': image_id,
                'area_pixels': measurement_data.get('area_pixels', 0),
                'area_um2': measurement_data.get('area_um2', 0.0),
                'scale': measurement_data.get('scale_pixels_per_um', 10.0),
                'data_hora': measurement_data.get('dataHora')
            }

            cursor.execute(sql, data)
            self.connection.commit()
            cursor.close()

            logger.info(f"✅ Medição {data['measurement_id']} inserida no Oracle com sucesso")

            # Também salva no JSON como backup
            self._save_to_json_fallback(measurement_data)

            return True

        except Exception as e:
            logger.error(f"❌ Erro ao inserir medição no Oracle: {e}")
            if self.connection:
                try:
                    self.connection.rollback()
                except Exception:
                    pass

            # Usa fallback JSON em caso de erro
            logger.info("Usando fallback JSON devido a erro Oracle")
            self._save_to_json_fallback(measurement_data)
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
            # Caminho absoluto para garantir que sempre funcione
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            json_file = os.path.join(base_dir, "data-integration", "measurements.json")

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

    def _get_table_columns(self, table_name: str) -> set:
        """Retorna as colunas existentes para a tabela informada."""
        if not self.connection:
            return set()

        cursor = self.connection.cursor()
        try:
            cursor.execute("""
                SELECT COLUMN_NAME
                FROM USER_TAB_COLUMNS
                WHERE TABLE_NAME = :table_name
            """, [table_name.upper()])
            columns = {row[0] for row in cursor}
            return columns
        finally:
            cursor.close()

    def _get_sample_numeric_id(self, sample_identifier: Optional[str]) -> Optional[int]:
        """Obtém o ID numérico da amostra correspondente ao SAMPLE_ID."""
        if not self.connection:
            return None

        cursor = self.connection.cursor()
        try:
            if sample_identifier:
                cursor.execute("SELECT ID FROM TB_SAMPLES WHERE SAMPLE_ID = :1 AND ROWNUM = 1", [sample_identifier])
                row = cursor.fetchone()
                if row and row[0] is not None:
                    return int(row[0])

            cursor.execute("SELECT MIN(ID) FROM TB_SAMPLES")
            row = cursor.fetchone()
            return int(row[0]) if row and row[0] is not None else None
        finally:
            cursor.close()

    def _get_default_microscope_id(self) -> Optional[int]:
        """Obtém um microscópio válido para uso como foreign key."""
        if not self.connection:
            return None

        cursor = self.connection.cursor()
        try:
            cursor.execute("SELECT MIN(ID) FROM TB_DIGITAL_MICROSCOPES")
            row = cursor.fetchone()
            return int(row[0]) if row and row[0] is not None else None
        finally:
            cursor.close()

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

    def create_image_if_not_exists(self, image_id: str, measurement_data: Optional[Dict[str, Any]] = None, image_bytes: Optional[bytes] = None) -> bool:
        """
        Cria uma entrada de imagem se ela não existir.

        Args:
            image_id: ID da imagem
            measurement_data: Dados da medição relacionados (opcional)
            image_bytes: Dados binários da imagem (opcional)

        Returns:
            bool: True se criação/existência confirmada
        """
        if not self.connection:
            return False

        try:
            cursor = self.connection.cursor()
            cursor.execute("SELECT COUNT(*) FROM TB_MICROSCOPY_IMAGES WHERE IMAGE_ID = :1", [image_id])
            exists = cursor.fetchone()[0] > 0
            cursor.close()

            if exists:
                return True

            columns = self._get_table_columns('TB_MICROSCOPY_IMAGES')
            if not columns:
                logger.warning('Não foi possível obter a estrutura da tabela TB_MICROSCOPY_IMAGES')
                return False

            params: Dict[str, Any] = {'image_id': image_id}
            file_name = None
            if measurement_data:
                file_name = measurement_data.get('nomeImagem')
            if not file_name:
                file_name = f"{image_id}.jpg"

            nome_column = 'NOME_ARQUIVO' if 'NOME_ARQUIVO' in columns else 'ARQUIVO'

            columns_to_insert = ['ID', 'IMAGE_ID', nome_column]
            values_parts = ['SQ_MICROSCOPY_IMAGES.NEXTVAL', ':image_id', ':nome_arquivo']
            params['nome_arquivo'] = file_name

            # Adiciona o BLOB se fornecido
            if 'IMAGEM_BLOB' in columns and image_bytes:
                columns_to_insert.append('IMAGEM_BLOB')
                values_parts.append(':imagem_blob')
                params['imagem_blob'] = image_bytes
                logger.info(f"📷 Preparando BLOB da imagem ({len(image_bytes)} bytes)")

            sample_fk_column = 'SAMPLE_ID_FK' if 'SAMPLE_ID_FK' in columns else None
            if sample_fk_column:
                sample_fk = self._get_sample_numeric_id((measurement_data or {}).get('sampleId'))
                if sample_fk is None:
                    logger.error('Não foi possível determinar SAMPLE_ID_FK para a imagem gerada')
                    return False
                columns_to_insert.append(sample_fk_column)
                values_parts.append(':sample_fk')
                params['sample_fk'] = sample_fk

            microscope_fk_column = 'MICROSCOPE_ID_FK' if 'MICROSCOPE_ID_FK' in columns else None
            if microscope_fk_column:
                microscope_fk = self._get_default_microscope_id()
                if microscope_fk is None:
                    logger.error('Não foi possível determinar MICROSCOPE_ID_FK para a imagem gerada')
                    return False
                columns_to_insert.append(microscope_fk_column)
                values_parts.append(':microscope_fk')
                params['microscope_fk'] = microscope_fk

            size_column = None
            if 'TAMANHO_ARQUIVO' in columns:
                size_column = 'TAMANHO_ARQUIVO'
            elif 'TAMANHO_BYTES' in columns:
                size_column = 'TAMANHO_BYTES'
            if size_column:
                columns_to_insert.append(size_column)
                values_parts.append(':file_size')
                params['file_size'] = len(image_bytes) if image_bytes else 0

            if 'RESOLUCAO' in columns:
                columns_to_insert.append('RESOLUCAO')
                values_parts.append(':resolution')
                params['resolution'] = (measurement_data or {}).get('resolucao', '1920x1080')

            if 'FORMATO' in columns:
                columns_to_insert.append('FORMATO')
                values_parts.append(':format')
                fmt_value = (measurement_data or {}).get('formato')
                if not fmt_value and file_name:
                    fmt_value = Path(file_name).suffix.replace('.', '').upper()
                if fmt_value:
                    fmt_value = fmt_value.upper()
                allowed_formats = {'JPEG', 'JPG', 'PNG', 'TIFF', 'BMP'}
                if fmt_value not in allowed_formats:
                    fmt_value = 'JPG'
                params['format'] = fmt_value

            if 'MAGNIFICACAO' in columns:
                columns_to_insert.append('MAGNIFICACAO')
                values_parts.append(':magnification')
                params['magnification'] = (measurement_data or {}).get('magnification', 0)

            if 'OPERADOR_CAPTURA' in columns:
                columns_to_insert.append('OPERADOR_CAPTURA')
                values_parts.append(':operator_capture')
                params['operator_capture'] = (measurement_data or {}).get('operator', 'Sistema Automático')

            if 'OBSERVACOES' in columns:
                columns_to_insert.append('OBSERVACOES')
                values_parts.append(':notes')
                params['notes'] = 'Imagem capturada automaticamente pelo módulo Python'

            if 'DATA_CAPTURA' in columns:
                columns_to_insert.append('DATA_CAPTURA')
                values_parts.append('SYSDATE')

            if 'DATA_CRIACAO' in columns:
                columns_to_insert.append('DATA_CRIACAO')
                values_parts.append('SYSDATE')

            insert_sql = f"INSERT INTO TB_MICROSCOPY_IMAGES ({', '.join(columns_to_insert)}) VALUES ({', '.join(values_parts)})"
            cursor = self.connection.cursor()

            # Se há um BLOB, configura o tipo corretamente
            if image_bytes and 'imagem_blob' in params:
                try:
                    cursor.setinputsizes(imagem_blob=oracledb.DB_TYPE_BLOB)
                except Exception as e:
                    logger.warning(f"⚠️ Não foi possível configurar tipo BLOB: {e}")

            cursor.execute(insert_sql, params)
            self.connection.commit()
            cursor.close()

            logger.info(f"✅ Entrada de imagem {image_id} criada no Oracle" +
                       (f" (BLOB: {len(image_bytes)} bytes)" if image_bytes else " (sem BLOB)"))
            return True

        except Exception as e:
            logger.error(f"❌ Erro ao criar entrada de imagem {image_id}: {e}")
            if self.connection:
                try:
                    self.connection.rollback()
                except Exception:
                    pass
            return False

    def diagnose_oracle_issues(self) -> Dict[str, Any]:
        """
        Diagnostica problemas comuns do Oracle e retorna relatório detalhado.

        Returns:
            Dict: Relatório de diagnóstico
        """
        diagnosis = {
            'connection_ok': False,
            'tables_exist': False,
            'data_initialized': False,
            'issues_found': [],
            'recommendations': []
        }

        # Teste de conexão
        try:
            if self.connect():
                diagnosis['connection_ok'] = True
                logger.info("✅ Conexão Oracle estabelecida")

                cursor = self.connection.cursor()

                # Verificar tabelas
                required_tables = [
                    'TB_OPERATORS', 'TB_DIGITAL_MICROSCOPES', 'TB_SAMPLES',
                    'TB_MICROSCOPY_IMAGES', 'TB_MEASUREMENTS'
                ]

                existing_tables = []
                missing_tables = []

                for table in required_tables:
                    try:
                        cursor.execute(f"SELECT COUNT(*) FROM {table}")
                        count = cursor.fetchone()[0]
                        existing_tables.append((table, count))
                        logger.info(f"✅ Tabela {table}: {count} registros")
                    except Exception as e:
                        missing_tables.append(table)
                        logger.error(f"❌ Tabela {table}: {str(e)}")

                if not missing_tables:
                    diagnosis['tables_exist'] = True

                # Verificar dados iniciais
                if existing_tables:
                    operators_count = next((count for table, count in existing_tables if table == 'TB_OPERATORS'), 0)
                    microscopes_count = next((count for table, count in existing_tables if table == 'TB_DIGITAL_MICROSCOPES'), 0)

                    if operators_count > 0 and microscopes_count > 0:
                        diagnosis['data_initialized'] = True
                    else:
                        diagnosis['issues_found'].append("Dados iniciais não encontrados")
                        diagnosis['recommendations'].append("Execute setup_oracle_database.sql para inserir dados iniciais")

                # Compilar problemas encontrados
                if missing_tables:
                    diagnosis['issues_found'].extend([f"Tabela não encontrada: {table}" for table in missing_tables])
                    diagnosis['recommendations'].append("Execute o script setup_oracle_database.sql para criar as tabelas")

                cursor.close()
                self.disconnect()

            else:
                diagnosis['issues_found'].append("Não foi possível conectar ao Oracle")
                diagnosis['recommendations'].extend([
                    "Verifique as credenciais Oracle",
                    "Confirme conectividade de rede com oracle.fiap.com.br",
                    "Instale o driver oracledb: pip install oracledb"
                ])

        except Exception as e:
            diagnosis['issues_found'].append(f"Erro durante diagnóstico: {str(e)}")
            diagnosis['recommendations'].append("Verifique logs para detalhes do erro")

        return diagnosis

def install_oracledb():
    """
    Instala oracledb automaticamente se não estiver disponível.
    """
    try:
        import oracledb
        logger.info("✅ oracledb já está instalado")
        return True
    except ImportError:
        logger.info("📦 Instalando oracledb...")
        try:
            import subprocess
            import sys
            subprocess.check_call([sys.executable, "-m", "pip", "install", "oracledb"])
            logger.info("✅ oracledb instalado com sucesso!")
            return True
        except Exception as e:
            logger.error(f"❌ Erro ao instalar oracledb: {e}")
            logger.info("💡 Para instalar manualmente: pip install oracledb")
            return False

# Exemplo de uso e teste
if __name__ == "__main__":
    print("🔬 Teste da Integração Oracle")
    print("="*50)

    # Tenta instalar oracledb se necessário
    install_oracledb()

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