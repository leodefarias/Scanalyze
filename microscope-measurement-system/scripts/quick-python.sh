#!/bin/bash

# Sistema de Micromedição - Launcher Python Rápido
# Executa apenas o módulo de visão computacional

echo "🐍 Iniciando Visão Computacional Python..."
echo "💡 Interface para captura e processamento em tempo real"
echo

# Usa ambiente virtual se disponível
if [ -f "python-vision/venv/bin/python" ]; then
    echo "🔧 Usando ambiente virtual Python..."
    python-vision/venv/bin/python python-vision/microscope_gui.py
else
    python3 python-vision/microscope_gui.py
fi