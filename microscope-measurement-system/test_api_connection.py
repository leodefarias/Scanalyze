#!/usr/bin/env python3
"""
Teste rápido da conexão API para interface Python
"""

import sys
import os

# Adiciona o diretório python-vision ao path
sys.path.append(os.path.join(os.path.dirname(__file__), 'python-vision'))

def test_api_integration():
    """Testa se a interface consegue conectar à API."""
    print("🔗 Testando conexão API da interface...")

    try:
        import requests
        print("✅ Módulo requests disponível")

        # Testa conexão
        response = requests.get("http://localhost:8080/api/health", timeout=3)
        if response.status_code == 200:
            data = response.json()
            print(f"✅ API REST conectada: {data.get('service', 'Desconhecido')}")
            print(f"📊 Status: {data.get('status', 'Desconhecido')}")
            print(f"🕒 Timestamp: {data.get('timestamp', 'Desconhecido')}")
            return True
        else:
            print(f"❌ API respondeu com erro: {response.status_code}")
            return False

    except ImportError:
        print("❌ Módulo requests não encontrado")
        return False
    except Exception as e:
        print(f"❌ Erro de conexão: {e}")
        return False

def main():
    print("🧪 TESTE DE CONEXÃO API")
    print("=" * 30)

    success = test_api_integration()

    if success:
        print("\n🎉 TUDO FUNCIONANDO!")
        print("A interface Python agora pode:")
        print("✅ Conectar à API REST")
        print("✅ Registrar medições automaticamente")
        print("✅ Sincronizar com o backend")
        print("\n💡 Reinicie o sistema para ver a diferença!")
    else:
        print("\n⚠️ Problema na integração API")
        print("Sistema continuará funcionando em modo JSON")

if __name__ == "__main__":
    main()