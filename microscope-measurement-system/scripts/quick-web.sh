#!/bin/bash

# Sistema de Micromedição - Launcher Web Rápido
# Abre apenas o dashboard web

echo "🌐 Abrindo Dashboard Web..."
echo "💡 Visualização de dados e análises"
echo

# Detectar sistema operacional e abrir navegador
if command -v xdg-open &> /dev/null; then
    xdg-open "frontend-dashboard/index.html"
elif command -v open &> /dev/null; then
    open "frontend-dashboard/index.html"
elif command -v start &> /dev/null; then
    start "frontend-dashboard/index.html"
else
    echo "💡 Abra manualmente o arquivo: frontend-dashboard/index.html"
fi