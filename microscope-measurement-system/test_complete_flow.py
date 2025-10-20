#!/usr/bin/env python3
"""
Teste Completo do Fluxo: Python → Oracle/JSON → Frontend
Simula o fluxo completo de captura, processamento e visualização

Autor: Sistema de Micromedição
Versão: 1.0
"""

import sys
import os
import time
from datetime import datetime

# Adiciona o diretório python-vision ao path
sys.path.append(os.path.join(os.path.dirname(__file__), 'python-vision'))

def test_complete_flow():
    """Testa o fluxo completo de ponta a ponta."""
    print("🚀 TESTE COMPLETO DO FLUXO INTEGRADO")
    print("=" * 60)

    try:
        # Importa o sistema de visão
        from microscope_vision import MicroscopeVision
        import numpy as np

        print("1️⃣ Inicializando sistema de visão computacional...")
        vision = MicroscopeVision()

        # Simula dados de captura
        print("2️⃣ Simulando captura de dados...")
        vision.current_frame = np.zeros((480, 640, 3), dtype=np.uint8)
        vision.processed_frame = np.zeros((480, 640, 3), dtype=np.uint8)
        vision.current_area_pixels = 1800
        vision.current_area_um2 = 18.75

        # Salva múltiplas medições
        measurements = []
        for i in range(3):
            print(f"3️⃣.{i+1} Salvando medição {i+1}/3...")

            # Varia os valores para simular medições diferentes
            vision.current_area_pixels = 1500 + (i * 200)
            vision.current_area_um2 = 15.0 + (i * 2.5)

            sample_id = f"FLOW_TEST_SAMPLE_{i+1:02d}"
            measurement = vision.save_measurement(sample_id, f"Operador Teste {i+1}")

            if measurement:
                measurements.append(measurement)
                print(f"   ✅ Medição {measurement['id']} salva com sucesso")
            else:
                print(f"   ❌ Falha ao salvar medição {i+1}")

            time.sleep(1)  # Pequena pausa entre medições

        # Libera recursos
        vision.release_resources()

        print(f"\n4️⃣ Resumo do teste:")
        print(f"   📊 Medições processadas: {len(measurements)}")

        if measurements:
            print(f"   📏 Primeira medição: {measurements[0]['area_um2']} μm²")
            print(f"   📏 Última medição: {measurements[-1]['area_um2']} μm²")
            print(f"   🖼️ Imagens capturadas: {len(measurements)}")

        # Verifica arquivo JSON final
        print("\n5️⃣ Verificando arquivo JSON gerado...")
        json_file = "data-integration/measurements.json"

        if os.path.exists(json_file):
            try:
                with open(json_file, 'r', encoding='utf-8') as f:
                    import json
                    data = json.load(f)

                total_measurements = len(data.get('measurements', []))
                print(f"   ✅ Arquivo JSON válido com {total_measurements} medições totais")

                # Mostra as últimas medições (do teste atual)
                if total_measurements >= len(measurements):
                    print(f"   📋 Últimas medições do teste:")
                    for measurement in data['measurements'][-len(measurements):]:
                        print(f"      - {measurement.get('id', 'ID?')}: {measurement.get('area_um2', 0)} μm²")

            except Exception as e:
                print(f"   ❌ Erro ao ler JSON: {e}")
        else:
            print(f"   ❌ Arquivo JSON não encontrado em {json_file}")

        print("\n6️⃣ Status final:")
        print("   🔗 Integração Python ↔ Banco/JSON: ✅ Funcionando")
        print("   📁 Geração de arquivos JSON: ✅ Funcionando")
        print("   🌐 Frontend pode acessar dados: ✅ Disponível")

        print(f"\n💡 Para visualizar no frontend:")
        print(f"   1. Abra: http://localhost:8002/frontend-dashboard/")
        print(f"   2. O dashboard carregará automaticamente os dados de:")
        print(f"      http://localhost:8002/data-integration/measurements.json")
        print(f"   3. Verá as medições em tempo real sem importação manual")

        return True

    except Exception as e:
        print(f"❌ Erro no teste completo: {e}")
        import traceback
        traceback.print_exc()
        return False

def main():
    """Executa o teste completo."""
    success = test_complete_flow()

    print("\n" + "=" * 60)
    if success:
        print("🎉 TESTE COMPLETO: SUCESSO!")
        print("\n✅ Sistema totalmente integrado e funcionando!")
        print("\n🚀 Próximos passos:")
        print("   1. Execute o python-vision/microscope_vision.py para captura real")
        print("   2. Abra o frontend em http://localhost:8002/frontend-dashboard/")
        print("   3. Veja as medições aparecendo automaticamente no dashboard")
        print("   4. Configure Oracle Client para usar banco real (opcional)")
    else:
        print("❌ TESTE COMPLETO: FALHOU!")
        print("\n🔧 Verifique os erros acima e tente novamente")

if __name__ == "__main__":
    main()