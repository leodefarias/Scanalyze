#!/usr/bin/env python3
"""
Teste para verificar se o frontend consegue acessar os dados
"""

import http.server
import socketserver
import threading
import time
import requests
import json

def start_server():
    """Inicia servidor HTTP para servir os arquivos."""
    PORT = 8003

    class CustomHandler(http.server.SimpleHTTPRequestHandler):
        def end_headers(self):
            # Adiciona headers CORS
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
            self.send_header('Access-Control-Allow-Headers', 'Content-Type')
            super().end_headers()

    with socketserver.TCPServer(("", PORT), CustomHandler) as httpd:
        print(f"🌐 Servidor iniciado em http://localhost:{PORT}")
        httpd.serve_forever()

def test_data_access():
    """Testa se conseguimos acessar os dados."""
    print("📋 Testando acesso aos dados...")

    # Aguarda servidor inicializar
    time.sleep(2)

    try:
        # Testa acesso ao JSON
        response = requests.get("http://localhost:8003/data-integration/measurements.json", timeout=5)

        if response.status_code == 200:
            data = response.json()
            measurements = data.get('measurements', [])
            print(f"✅ JSON acessível com {len(measurements)} medições")

            # Mostra últimas 3 medições
            if measurements:
                print("📊 Últimas medições:")
                for measurement in measurements[-3:]:
                    print(f"   - {measurement.get('id', 'ID?')}: {measurement.get('area_um2', 0)} μm²")
            else:
                print("⚠️ Nenhuma medição encontrada no JSON")

        else:
            print(f"❌ Erro HTTP {response.status_code} ao acessar JSON")

    except Exception as e:
        print(f"❌ Erro ao acessar dados: {e}")

    # Testa acesso ao dashboard
    try:
        response = requests.get("http://localhost:8003/frontend-dashboard/index.html", timeout=5)
        if response.status_code == 200:
            print("✅ Dashboard acessível")
        else:
            print(f"❌ Dashboard não acessível (HTTP {response.status_code})")
    except Exception as e:
        print(f"❌ Erro ao acessar dashboard: {e}")

def main():
    print("🧪 TESTE DE FRONTEND E DADOS")
    print("=" * 40)

    # Inicia servidor em thread
    server_thread = threading.Thread(target=start_server, daemon=True)
    server_thread.start()

    # Testa acesso aos dados
    test_data_access()

    print("\n💡 Para testar manualmente:")
    print("   1. Abra: http://localhost:8003/frontend-dashboard/")
    print("   2. Dados JSON: http://localhost:8003/data-integration/measurements.json")
    print("   3. Pressione Ctrl+C para sair")

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n👋 Finalizando...")

if __name__ == "__main__":
    main()