#!/bin/bash
#
# Script para Testar Endpoint de Imagens
# Testa se as imagens estão sendo servidas corretamente do banco de dados
#

echo "======================================================"
echo "Teste: Endpoint de Imagens (/api/images)"
echo "======================================================"
echo

# Verifica se a API está rodando
echo "1. Verificando se a API está rodando..."
if curl -s http://localhost:8081/api/health > /dev/null 2>&1; then
    echo "✅ API está online"
else
    echo "❌ API não está rodando"
    echo "Inicie a API com: cd backend-java && ./compile_and_run.sh"
    exit 1
fi

echo

# Busca medições para pegar nome de imagem
echo "2. Buscando medições para encontrar imagens..."
RESPONSE=$(curl -s http://localhost:8081/api/measurements)

# Extrai o primeiro nome de imagem
IMAGE_NAME=$(echo "$RESPONSE" | grep -o '"nomeImagem": *"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -z "$IMAGE_NAME" ]; then
    echo "⚠️  Nenhuma imagem encontrada nas medições"
    echo "Execute uma medição primeiro usando o sistema python-vision"
    exit 1
fi

echo "✅ Imagem encontrada: $IMAGE_NAME"
echo

# Testa o endpoint de imagens
echo "3. Testando endpoint /api/images/$IMAGE_NAME..."
HTTP_CODE=$(curl -s -o /tmp/test_image.jpg -w "%{http_code}" http://localhost:8081/api/images/$IMAGE_NAME)

if [ "$HTTP_CODE" = "200" ]; then
    FILE_SIZE=$(stat -c%s /tmp/test_image.jpg 2>/dev/null || stat -f%z /tmp/test_image.jpg 2>/dev/null)
    echo "✅ Imagem recuperada com sucesso!"
    echo "   HTTP Status: $HTTP_CODE"
    echo "   Tamanho: $FILE_SIZE bytes"
    echo "   Arquivo salvo em: /tmp/test_image.jpg"

    # Verifica se o arquivo é uma imagem válida
    if file /tmp/test_image.jpg | grep -q "JPEG\|PNG\|image"; then
        echo "✅ Arquivo é uma imagem válida"
    else
        echo "⚠️  Arquivo pode não ser uma imagem válida"
    fi
else
    echo "❌ Erro ao recuperar imagem"
    echo "   HTTP Status: $HTTP_CODE"
    exit 1
fi

echo
echo "======================================================"
echo "✅ TESTE CONCLUÍDO COM SUCESSO!"
echo "======================================================"
echo
echo "O endpoint de imagens está funcionando corretamente."
echo "Verifique os logs da API para ver se a imagem veio:"
echo "  - Do banco de dados Oracle (BLOB)"
echo "  - Do arquivo local (fallback)"
echo