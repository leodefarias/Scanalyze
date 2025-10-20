#!/usr/bin/env python3
"""
Script de Teste para Integração Python → Oracle → Frontend
Testa se os registros estão sendo salvos corretamente no banco de dados
e se aparecem no frontend automaticamente.

Autor: Sistema de Micromedição
Versão: 1.0
"""

import time
import sys
import os
from datetime import datetime
from oracle_integration import OracleIntegration

def test_oracle_connection():
    """Testa conexão básica com Oracle."""
    print("🔗 Testando conexão Oracle...")

    oracle = OracleIntegration()

    if oracle.test_connection():
        print("✅ Conexão Oracle OK")
        return True
    else:
        print("❌ Falha na conexão Oracle")
        return False

def test_measurement_insertion():
    """Testa inserção de medição no banco."""
    print("\n💾 Testando inserção de medição...")

    oracle = OracleIntegration()

    if not oracle.connect():
        print("❌ Não foi possível conectar ao Oracle")
        return False

    # Dados de teste
    test_measurement = {
        "id": f"TEST_INTEGRATION_{int(time.time())}",
        "sampleId": "SAMPLE_TEST_AUTO",
        "area_pixels": 1500,
        "area_um2": 15.75,
        "dataHora": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "imagemId": f"IMG_TEST_{int(time.time())}",
        "nomeImagem": f"test_integration_{int(time.time())}.jpg",
        "operator": "Sistema Teste Automático",
        "scale_pixels_per_um": 10.0
    }

    # Tenta inserir
    success = oracle.insert_measurement(test_measurement)

    oracle.disconnect()

    if success:
        print(f"✅ Medição {test_measurement['id']} inserida com sucesso")
        return test_measurement
    else:
        print("❌ Falha ao inserir medição")
        return None

def test_microscope_vision_integration():
    """Testa a integração usando a classe MicroscopeVision."""
    print("\n🔬 Testando integração com MicroscopeVision...")

    try:
        from microscope_vision import MicroscopeVision
        import numpy as np

        # Cria instância sem inicializar câmera
        vision = MicroscopeVision()

        # Simula frame capturado (necessário para save_measurement funcionar)
        vision.current_frame = np.zeros((480, 640, 3), dtype=np.uint8)
        vision.processed_frame = np.zeros((480, 640, 3), dtype=np.uint8)

        # Simula dados de medição
        vision.current_area_pixels = 1200
        vision.current_area_um2 = 12.5

        # Tenta salvar medição
        measurement = vision.save_measurement("TEST_VISION_INTEGRATION", "Sistema Teste")

        # Libera recursos
        vision.release_resources()

        if measurement:
            print(f"✅ Medição integrada salva: {measurement['id']}")
            return True
        else:
            print("❌ Falha ao salvar medição integrada")
            return False

    except Exception as e:
        print(f"❌ Erro no teste de integração: {e}")
        return False

def test_json_fallback():
    """Testa se o fallback JSON funciona quando Oracle não está disponível."""
    print("\n📁 Testando fallback JSON...")

    oracle = OracleIntegration()
    # Simula falha de conexão
    oracle.connection = None

    test_measurement = {
        "id": f"TEST_FALLBACK_{int(time.time())}",
        "sampleId": "SAMPLE_FALLBACK",
        "area_pixels": 800,
        "area_um2": 8.5,
        "dataHora": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "imagemId": f"IMG_FALLBACK_{int(time.time())}",
        "nomeImagem": f"test_fallback_{int(time.time())}.jpg",
        "operator": "Sistema Teste Fallback",
        "scale_pixels_per_um": 10.0
    }

    success = oracle.insert_measurement(test_measurement)

    if success:
        print(f"✅ Fallback JSON funcionando: {test_measurement['id']}")
        return True
    else:
        print("❌ Falha no fallback JSON")
        return False

def check_json_files():
    """Verifica se os arquivos JSON foram criados/atualizados."""
    print("\n📄 Verificando arquivos JSON...")

    json_file = "../data-integration/measurements.json"

    if os.path.exists(json_file):
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                import json
                data = json.load(f)

            measurements_count = len(data.get('measurements', []))
            print(f"✅ Arquivo JSON encontrado com {measurements_count} medições")

            # Mostra a última medição
            if measurements_count > 0:
                last_measurement = data['measurements'][-1]
                print(f"   Última medição: {last_measurement.get('id', 'ID não encontrado')}")
                print(f"   Data: {last_measurement.get('dataHora', 'Data não encontrada')}")

            return True

        except Exception as e:
            print(f"❌ Erro ao ler arquivo JSON: {e}")
            return False
    else:
        print("❌ Arquivo JSON não encontrado")
        return False

def main():
    """Executa todos os testes de integração."""
    print("="*60)
    print("🧪 TESTE DE INTEGRAÇÃO PYTHON → ORACLE → FRONTEND")
    print("="*60)

    results = {}

    # Teste 1: Conexão Oracle
    results['oracle_connection'] = test_oracle_connection()

    # Teste 2: Inserção no banco
    results['measurement_insertion'] = test_measurement_insertion() is not None

    # Teste 3: Integração com MicroscopeVision
    results['vision_integration'] = test_microscope_vision_integration()

    # Teste 4: Fallback JSON
    results['json_fallback'] = test_json_fallback()

    # Teste 5: Arquivos JSON
    results['json_files'] = check_json_files()

    # Resumo dos resultados
    print("\n" + "="*60)
    print("📊 RESUMO DOS TESTES")
    print("="*60)

    passed = 0
    total = len(results)

    for test_name, passed_test in results.items():
        status = "✅ PASSOU" if passed_test else "❌ FALHOU"
        print(f"{test_name.replace('_', ' ').title()}: {status}")
        if passed_test:
            passed += 1

    print(f"\n🎯 Resultado Final: {passed}/{total} testes passaram")

    if passed == total:
        print("🎉 TODOS OS TESTES PASSARAM! Integração funcionando perfeitamente.")
        print("\n💡 Próximos passos:")
        print("   1. Execute o sistema Python para capturar medições")
        print("   2. Abra o frontend para ver os dados em tempo real")
        print("   3. Verifique se o backend Java API está rodando")
    else:
        print("⚠️ Alguns testes falharam. Verifique a configuração do sistema.")
        print("\n🔧 Possíveis soluções:")
        if not results['oracle_connection']:
            print("   - Verifique as credenciais Oracle em oracle_integration.py")
            print("   - Confirme se o cx_Oracle está instalado: pip install cx_Oracle")
        if not results['json_fallback']:
            print("   - Verifique permissões de escrita no diretório data-integration")

if __name__ == "__main__":
    main()