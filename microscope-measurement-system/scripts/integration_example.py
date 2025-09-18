#!/usr/bin/env python3
"""
Script de demonstração da integração completa do Sistema de Micromedição.
Este script simula o fluxo completo de captura, processamento e integração.

Autor: Sistema de Micromedição
Versão: 1.0
"""

import json
import csv
import os
import time
from datetime import datetime
import random

def create_sample_data():
    """Cria dados de exemplo para demonstração da integração."""
    
    # Cria diretório se não existir
    output_dir = "data-integration"
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    # Simula medições capturadas pelo módulo Python
    measurements = []
    
    samples = ["SAMPLE_001", "SAMPLE_002", "SAMPLE_003"]
    operators = ["Dr. João Silva", "Maria Santos", "Carlos Oliveira"]
    
    for i in range(10):
        timestamp = datetime.now().timestamp() + i * 3600  # Uma medição por hora
        measurement = {
            "id": f"MEAS_{int(timestamp)}",
            "sampleId": random.choice(samples),
            "area_pixels": random.randint(800, 2000),
            "area_um2": round(random.uniform(8.0, 20.0), 2),
            "dataHora": datetime.fromtimestamp(timestamp).strftime("%Y-%m-%d %H:%M:%S"),
            "imagemId": f"IMG_{int(timestamp)}",
            "nomeImagem": f"measurement_{int(timestamp)}.jpg",
            "operator": random.choice(operators),
            "scale_pixels_per_um": 10.0
        }
        measurements.append(measurement)
    
    # Salva em JSON (formato principal)
    json_path = os.path.join(output_dir, "measurements.json")
    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump({"measurements": measurements}, f, indent=2, ensure_ascii=False)
    
    # Salva em CSV (para compatibilidade com Java)
    csv_path = os.path.join(output_dir, "measurements.csv")
    with open(csv_path, 'w', newline='', encoding='utf-8') as f:
        fieldnames = ['id', 'sampleId', 'area_pixels', 'area_um2', 'dataHora', 
                     'imagemId', 'nomeImagem', 'operator', 'scale_pixels_per_um']
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(measurements)
    
    print(f"✅ Criados arquivos de integração:")
    print(f"   - {json_path}")
    print(f"   - {csv_path}")
    print(f"   - {len(measurements)} medições simuladas")
    
    return measurements

def create_sample_metadata():
    """Cria metadados de amostras."""
    
    output_dir = "data-integration"
    samples_data = [
        {
            "id": "SAMPLE_001",
            "nome": "Sangue Paciente A",
            "tipo": "Sangue",
            "dataColeta": "2024-01-15 08:00:00",
            "operadorResponsavel": "Dr. João Silva"
        },
        {
            "id": "SAMPLE_002", 
            "nome": "Tecido Muscular",
            "tipo": "Tecido",
            "dataColeta": "2024-01-15 09:30:00",
            "operadorResponsavel": "Maria Santos"
        },
        {
            "id": "SAMPLE_003",
            "nome": "Célula Neural",
            "tipo": "Neurônio", 
            "dataColeta": "2024-01-15 15:00:00",
            "operadorResponsavel": "Carlos Oliveira"
        }
    ]
    
    # Salva metadados das amostras
    samples_path = os.path.join(output_dir, "samples.json")
    with open(samples_path, 'w', encoding='utf-8') as f:
        json.dump({"samples": samples_data}, f, indent=2, ensure_ascii=False)
    
    print(f"✅ Criado arquivo de amostras: {samples_path}")

def simulate_integration_flow():
    """Simula o fluxo completo de integração."""
    
    print("🔬 === SIMULAÇÃO DE INTEGRAÇÃO COMPLETA ===")
    print()
    
    print("1️⃣ Simulando captura de medições pelo módulo Python...")
    measurements = create_sample_data()
    time.sleep(1)
    
    print("\n2️⃣ Criando metadados de amostras...")
    create_sample_metadata()
    time.sleep(1)
    
    print("\n3️⃣ Verificando arquivos criados...")
    data_dir = "data-integration"
    files_created = []
    
    for filename in ["measurements.json", "measurements.csv", "samples.json"]:
        filepath = os.path.join(data_dir, filename)
        if os.path.exists(filepath):
            size = os.path.getsize(filepath)
            files_created.append(f"   ✅ {filename} ({size} bytes)")
        else:
            files_created.append(f"   ❌ {filename} (não encontrado)")
    
    for file_info in files_created:
        print(file_info)
    
    print("\n4️⃣ Simulando importação pelo backend Java...")
    time.sleep(1)
    
    # Simula mensagens que o Java mostraria
    java_messages = [
        "Iniciando importação de medições...",
        f"✅ {len(measurements)} medições importadas com sucesso",
        "✅ Validação de integridade: OK",
        "✅ Dados disponíveis para o dashboard"
    ]
    
    for message in java_messages:
        print(f"   [JAVA] {message}")
        time.sleep(0.5)
    
    print("\n5️⃣ Dados prontos para visualização no dashboard!")
    print("   📊 Abra frontend-dashboard/index.html no navegador")
    print("   📁 Ou use 'Carregar Dados' para importar measurements.json")
    
    print("\n✨ === INTEGRAÇÃO CONCLUÍDA ===")
    print()
    print("Próximos passos:")
    print("1. Execute o backend Java: java br.com.micromedicao.app.App")
    print("2. Inicie a interface Python: python microscope_gui.py") 
    print("3. Abra o dashboard: frontend-dashboard/index.html")
    print("4. Use 'REGISTRAR MEDIÇÃO' para capturar dados reais")

def verify_system_files():
    """Verifica se todos os arquivos do sistema estão presentes."""
    
    print("🔍 Verificando arquivos do sistema...")
    
    required_files = {
        "Backend Java": [
            "backend-java/src/br/com/micromedicao/model/Sample.java",
            "backend-java/src/br/com/micromedicao/model/Measurement.java",
            "backend-java/src/br/com/micromedicao/model/Operator.java",
            "backend-java/src/br/com/micromedicao/model/DigitalMicroscope.java",
            "backend-java/src/br/com/micromedicao/model/MicroscopyImage.java",
            "backend-java/src/br/com/micromedicao/service/MicromedicaoService.java",
            "backend-java/src/br/com/micromedicao/integration/DataIntegration.java",
            "backend-java/src/br/com/micromedicao/app/App.java"
        ],
        "Python Vision": [
            "python-vision/microscope_vision.py",
            "python-vision/microscope_gui.py",
            "python-vision/requirements.txt"
        ],
        "Frontend Dashboard": [
            "frontend-dashboard/index.html",
            "frontend-dashboard/styles.css", 
            "frontend-dashboard/dashboard.js"
        ]
    }
    
    all_present = True
    
    for module, files in required_files.items():
        print(f"\n📂 {module}:")
        for file_path in files:
            if os.path.exists(file_path):
                print(f"   ✅ {file_path}")
            else:
                print(f"   ❌ {file_path}")
                all_present = False
    
    if all_present:
        print("\n✅ Todos os arquivos estão presentes!")
        print("🚀 Sistema pronto para uso!")
    else:
        print("\n❌ Alguns arquivos estão faltando.")
        print("⚠️  Verifique a estrutura do projeto.")
    
    return all_present

def main():
    """Função principal."""
    
    print("🔬 Sistema de Micromedição - Script de Integração")
    print("=" * 60)
    
    # Verifica arquivos do sistema
    if not verify_system_files():
        print("\n❌ Sistema incompleto. Verifique os arquivos.")
        return
    
    print("\n" + "="*60)
    
    # Executa simulação de integração
    simulate_integration_flow()

if __name__ == "__main__":
    main()