#!/bin/bash

# Sistema de Micromedição - Instalador com --break-system-packages
# Para sistemas que permitem instalação global forçada

echo "🔬 Instalador FORÇADO (--break-system-packages)"
echo "⚠️  Use apenas se souber o que está fazendo!"
echo

read -p "Tem certeza que deseja forçar instalação global? (s/N): " confirm
if [[ $confirm != [sS] ]]; then
    echo "❌ Instalação cancelada. Use './install' para instalação segura."
    exit 1
fi

echo "🚀 Forçando instalação global..."
cd python-vision

if [ -f "requirements.txt" ]; then
    pip3 install -r requirements.txt --break-system-packages
    echo "✅ Dependências instaladas com --break-system-packages"
else
    echo "❌ Arquivo requirements.txt não encontrado!"
    exit 1
fi

cd ..
echo "🏁 Instalação forçada concluída!"
echo "💡 Para execução: ./start"