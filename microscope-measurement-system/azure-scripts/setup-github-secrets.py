#!/usr/bin/env python3
"""
Script para configurar GitHub Secrets para deploy automático
"""

import base64
import json
import os
import sys
import urllib.request
import urllib.error

# Tenta importar nacl, se não estiver disponível, usa alternativa
try:
    from nacl import encoding, public
    HAS_NACL = True
except ImportError:
    HAS_NACL = False
    print("WARNING: PyNaCl not installed. Install with: pip install PyNaCl")
    print("Continuing without encryption...")

# Configurações - Obter de variáveis de ambiente
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")
REPO = os.environ.get("GITHUB_REPO", "leodefarias/Scanalyze")
API_URL = f"https://api.github.com/repos/{REPO}/actions/secrets"

# Secrets a serem configurados - Obter de variáveis de ambiente
SECRETS = {
    "AZURE_STATIC_WEB_APPS_API_TOKEN": os.environ.get("AZURE_STATIC_WEB_APPS_API_TOKEN", ""),
    "ORACLE_PASSWORD": os.environ.get("ORACLE_PASSWORD", ""),
    "API_BASE_URL": os.environ.get("API_BASE_URL", ""),
}

def get_public_key():
    """Obtém a chave pública do repositório"""
    req = urllib.request.Request(
        f"{API_URL}/public-key",
        headers={"Authorization": f"token {GITHUB_TOKEN}"}
    )
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read())
        return data['key_id'], data['key']

def encrypt_secret(public_key: str, secret_value: str) -> str:
    """Encripta um secret usando a chave pública"""
    if not HAS_NACL:
        # Sem encriptação - GitHub vai rejeitar, mas mostra o processo
        return base64.b64encode(secret_value.encode()).decode()

    public_key_bytes = base64.b64decode(public_key)
    sealed_box = public.SealedBox(public.PublicKey(public_key_bytes))
    encrypted = sealed_box.encrypt(secret_value.encode())
    return base64.b64encode(encrypted).decode()

def create_or_update_secret(name: str, value: str, key_id: str, public_key: str):
    """Cria ou atualiza um secret"""
    encrypted_value = encrypt_secret(public_key, value)

    data = json.dumps({
        "encrypted_value": encrypted_value,
        "key_id": key_id
    }).encode()

    req = urllib.request.Request(
        f"{API_URL}/{name}",
        data=data,
        method='PUT',
        headers={
            "Authorization": f"token {GITHUB_TOKEN}",
            "Content-Type": "application/json"
        }
    )

    try:
        with urllib.request.urlopen(req) as response:
            if response.status in [201, 204]:
                print(f"✅ {name} configurado")
            else:
                print(f"⚠️  {name} - Status {response.status}")
    except urllib.error.HTTPError as e:
        print(f"❌ {name} - Erro: {e.code} {e.reason}")
        if e.code == 422:
            print("   Instale PyNaCl: pip install PyNaCl")

def main():
    print("🔑 Configurando GitHub Secrets...")

    if not HAS_NACL:
        print("\n⚠️  PyNaCl não instalado!")
        print("   Execute: pip install PyNaCl")
        print("   Ou: python3 -m pip install PyNaCl\n")
        response = input("Continuar mesmo assim? (não funcionará) [y/N]: ")
        if response.lower() != 'y':
            sys.exit(1)

    try:
        key_id, public_key = get_public_key()
        print(f"📦 Obtido public key (ID: {key_id})\n")

        for name, value in SECRETS.items():
            create_or_update_secret(name, value, key_id, public_key)

        print("\n✅ Secrets configurados com sucesso!")
        print("\n📝 Próximos passos:")
        print("   1. git add .")
        print("   2. git commit -m 'feat: deploy Azure configurado'")
        print("   3. git push origin main")

    except Exception as e:
        print(f"❌ Erro: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
