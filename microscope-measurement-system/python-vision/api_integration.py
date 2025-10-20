#!/usr/bin/env python3
"""
Sistema de Captura Automática - Integração com API
Demonstra como o sistema de captura do Python pode salvar dados automaticamente
no banco de dados via API REST, sem necessidade de carregar arquivos JSON manualmente.

Autor: Sistema de Micromedição Scanalyze
Versão: 1.0
"""

import requests
import json
import time
from datetime import datetime
import random

# Configurações da API
API_BASE_URL = "https://scanalyze-api.agreeableplant-ba923b61.eastus2.azurecontainerapps.io/api"
API_TIMEOUT = 10

class ScanalyzeApiClient:
    """Cliente para integração com a API REST do Scanalyze"""

    def __init__(self, base_url=API_BASE_URL):
        self.base_url = base_url
        self.session = requests.Session()

    def health_check(self):
        """Verifica se a API está online"""
        try:
            response = self.session.get(f"{self.base_url}/health", timeout=API_TIMEOUT)
            return response.status_code == 200
        except:
            return False

    def create_sample(self, sample_id, nome, tipo, operador_responsavel):
        """Cadastra nova amostra via API"""
        try:
            data = {
                'id': sample_id,
                'nome': nome,
                'tipo': tipo,
                'operadorResponsavel': operador_responsavel
            }

            response = self.session.post(f"{self.base_url}/samples", data=data, timeout=API_TIMEOUT)
            result = response.json()

            if result.get('success'):
                print(f"✅ Amostra {sample_id} cadastrada com sucesso")
                return True
            else:
                print(f"❌ Erro ao cadastrar amostra: {result.get('message', 'Erro desconhecido')}")
                return False

        except Exception as e:
            print(f"❌ Erro de conexão ao cadastrar amostra: {e}")
            return False

    def create_measurement(self, measurement_id, sample_id, area, imagem_id=None):
        """Registra nova medição via API"""
        try:
            data = {
                'id': measurement_id,
                'sampleId': sample_id,
                'area': str(area)
            }

            if imagem_id:
                data['imagemId'] = imagem_id

            response = self.session.post(f"{self.base_url}/measurements", data=data, timeout=API_TIMEOUT)
            result = response.json()

            if result.get('success'):
                print(f"✅ Medição {measurement_id} registrada com sucesso (Área: {area:.2f} μm²)")
                return True
            else:
                print(f"❌ Erro ao registrar medição: {result.get('message', 'Erro desconhecido')}")
                return False

        except Exception as e:
            print(f"❌ Erro de conexão ao registrar medição: {e}")
            return False

    def get_samples(self):
        """Lista todas as amostras cadastradas"""
        try:
            response = self.session.get(f"{self.base_url}/samples", timeout=API_TIMEOUT)
            data = response.json()
            return data.get('samples', [])
        except Exception as e:
            print(f"❌ Erro ao buscar amostras: {e}")
            return []

    def get_measurements(self):
        """Lista todas as medições registradas"""
        try:
            response = self.session.get(f"{self.base_url}/measurements", timeout=API_TIMEOUT)
            data = response.json()
            return data.get('measurements', [])
        except Exception as e:
            print(f"❌ Erro ao buscar medições: {e}")
            return []

class MicroscopeVisionSystem:
    """Simulador do sistema de visão computacional do microscópio"""

    def __init__(self, api_client):
        self.api_client = api_client
        self.capture_counter = 0

    def simulate_sample_detection(self):
        """Simula detecção automática de nova amostra"""
        sample_types = ['Sangue', 'Tecido', 'Neurônio', 'Célula', 'Fibra']
        operators = ['Dr. João Silva', 'Maria Santos', 'Carlos Oliveira', 'Dr. Ana Costa']

        timestamp = int(time.time())
        sample_id = f"AUTO_SAMPLE_{timestamp}"
        nome = f"Amostra Automática {datetime.now().strftime('%H:%M:%S')}"
        tipo = random.choice(sample_types)
        operador = random.choice(operators)

        print(f"🔬 Nova amostra detectada: {nome}")
        return self.api_client.create_sample(sample_id, nome, tipo, operador)

    def simulate_measurement_capture(self, sample_id):
        """Simula captura e análise automática de medição"""
        # Simula análise de imagem com OpenCV/PIL
        self.capture_counter += 1

        # Simula área medida (em μm²)
        area_um2 = random.uniform(8.0, 25.0)

        timestamp = int(time.time())
        measurement_id = f"AUTO_MEAS_{timestamp}_{self.capture_counter}"
        imagem_id = f"AUTO_IMG_{timestamp}_{self.capture_counter}"

        print(f"📸 Capturando imagem e analisando área...")
        print(f"🔍 Área detectada: {area_um2:.2f} μm²")

        return self.api_client.create_measurement(measurement_id, sample_id, area_um2, imagem_id)

    def automated_capture_cycle(self, duration_minutes=5, interval_seconds=30):
        """Executa ciclo automatizado de captura"""
        print(f"🚀 Iniciando captura automatizada por {duration_minutes} minutos")
        print(f"📊 Intervalo entre capturas: {interval_seconds} segundos")
        print("=" * 60)

        start_time = time.time()
        end_time = start_time + (duration_minutes * 60)

        # Pega amostras existentes
        samples = self.api_client.get_samples()
        available_samples = [s['id'] for s in samples]

        if not available_samples:
            print("📋 Nenhuma amostra disponível, criando algumas automaticamente...")
            for i in range(3):
                self.simulate_sample_detection()
                time.sleep(2)

            samples = self.api_client.get_samples()
            available_samples = [s['id'] for s in samples]

        capture_count = 0

        while time.time() < end_time:
            try:
                # Aleatoriamente cria nova amostra ou usa existente
                if random.random() < 0.3:  # 30% chance de nova amostra
                    if self.simulate_sample_detection():
                        samples = self.api_client.get_samples()
                        available_samples = [s['id'] for s in samples]

                # Seleciona amostra aleatória para medição
                if available_samples:
                    sample_id = random.choice(available_samples)
                    if self.simulate_measurement_capture(sample_id):
                        capture_count += 1

                print(f"⏱️  Medições capturadas até agora: {capture_count}")
                print("-" * 40)

                time.sleep(interval_seconds)

            except KeyboardInterrupt:
                print("\n⏹️  Captura interrompida pelo usuário")
                break
            except Exception as e:
                print(f"❌ Erro durante captura: {e}")
                time.sleep(5)

        print(f"🏁 Captura automatizada finalizada!")
        print(f"📊 Total de medições capturadas: {capture_count}")
        return capture_count

def demonstrate_integration():
    """Demonstra integração completa do sistema"""
    print("=" * 60)
    print("🔬 SCANALYZE - SISTEMA DE CAPTURA AUTOMÁTICA")
    print("   Integração Python + API REST + Frontend")
    print("=" * 60)

    # Inicializa cliente da API
    api_client = ScanalyzeApiClient()

    # Verifica se API está online
    print("🔍 Verificando conexão com API...")
    if not api_client.health_check():
        print("❌ API REST não está disponível!")
        print("💡 Para iniciar a API, execute:")
        print("   cd backend-java/src")
        print("   javac br/com/micromedicao/api/*.java br/com/micromedicao/model/*.java br/com/micromedicao/service/*.java br/com/micromedicao/integration/*.java")
        print("   java br.com.micromedicao.api.ApiServer")
        return

    print("✅ API REST conectada com sucesso!")

    # Inicializa sistema de visão
    vision_system = MicroscopeVisionSystem(api_client)

    # Demonstra funcionalidades
    print("\n📋 1. Criando amostra de exemplo...")
    timestamp = int(time.time())
    sample_id = f"DEMO_SAMPLE_{timestamp}"
    vision_system.api_client.create_sample(
        sample_id,
        "Amostra de Demonstração",
        "Tecido",
        "Sistema Automático"
    )

    print("\n📊 2. Registrando medições automáticas...")
    for i in range(3):
        area = random.uniform(10.0, 20.0)
        measurement_id = f"DEMO_MEAS_{timestamp}_{i+1}"
        vision_system.api_client.create_measurement(measurement_id, sample_id, area)
        time.sleep(1)

    print("\n📈 3. Verificando dados no sistema...")
    samples = api_client.get_samples()
    measurements = api_client.get_measurements()

    print(f"   📋 Total de amostras: {len(samples)}")
    print(f"   📊 Total de medições: {len(measurements)}")

    print("\n🎯 4. Iniciando captura automática...")
    print("💡 O frontend será atualizado automaticamente via auto-refresh!")
    print("🌐 Abra http://localhost:3000 (ou onde estiver rodando o frontend)")
    print("🔄 As novas medições aparecerão automaticamente no dashboard")

    # Inicia captura automática por 2 minutos
    vision_system.automated_capture_cycle(duration_minutes=2, interval_seconds=15)

    print("\n✅ Demonstração concluída!")
    print("📊 Verifique o dashboard para ver os dados atualizados automaticamente")

if __name__ == "__main__":
    try:
        demonstrate_integration()
    except KeyboardInterrupt:
        print("\n👋 Sistema encerrado pelo usuário")
    except Exception as e:
        print(f"❌ Erro geral: {e}")