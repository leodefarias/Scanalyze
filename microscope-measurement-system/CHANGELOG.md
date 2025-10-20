# Changelog - Sistema de Micromedição Automatizada

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

---

## [1.2.0] - 2025-09-30

### 🎉 Adicionado
- **Armazenamento BLOB**: Imagens agora são armazenadas diretamente no Oracle como BLOB
- Campo `IMAGEM_BLOB` na tabela `TB_MICROSCOPY_IMAGES`
- Método `getImageBlob()` no `RestApiController` para recuperar imagens do banco
- Método `getImageBlobById()` no `MicroscopyImageDAO`
- Scripts de migração de imagens antigas:
  - `scripts/migrate_images_to_blob.py` - Migra imagens locais para BLOB
  - `scripts/validate_migrated_images.py` - Valida integridade das imagens
  - `scripts/test_image_blob.py` - Testa funcionalidade BLOB
- Suporte a múltiplos formatos de imagem (JPEG, PNG, TIFF, BMP)
- Headers de cache HTTP para performance (`Cache-Control: public, max-age=3600`)
- Conversão automática de imagem para bytes no Python (`microscope_vision.py`)

### 🔧 Modificado
- **API Server**: `ImagesHandler` atualizado com estratégia dupla (banco → arquivo)
- `MicroscopyImage.java`: Adicionados métodos para gerenciar BLOB
- `oracle_integration.py`: Atualizado para salvar bytes da imagem no banco
- `microscope_vision.py`: Converte imagem capturada para bytes antes de salvar
- Prioridade de busca: banco de dados primeiro, fallback para arquivo local

### 📖 Documentação
- Removidos 10 arquivos de documentação duplicados
- Consolidado todo conteúdo relevante no `README.md`
- Criado `CHANGELOG.md` unificado
- Expandida seção de Solução de Problemas
- Adicionada seção de Configuração Oracle detalhada

---

## [1.1.0] - 2025-09-30

### 🪟 Compatibilidade Windows

#### Adicionado
- **Script `restart-api.bat`**: Reinicia API REST no Windows
  - Usa `taskkill` ao invés de `pkill`
  - Usa `start /b` ao invés de `nohup &`
  - Porta 8081 padronizada
- Seção completa de documentação Windows no `README.md`
- Instruções específicas para Windows em comandos úteis

#### Corrigido
- **Porta da API**: Padronizada porta 8081 em todos os scripts Windows
  - `scripts/start.bat` atualizado (4 instâncias corrigidas)
  - Consistência com versão Linux
- **Classpaths Java**: Corrigidos separadores em scripts `.bat`
  - Unix usa `:` → `ojdbc8.jar:classes`
  - Windows usa `;` → `ojdbc8.jar;classes`
  - 5 scripts batch corrigidos
- Compilação Java com classpath correto incluindo `ojdbc8.jar`

#### Documentação
- Nova seção "🪟 Compatibilidade Windows" (77 linhas)
- Diferenças técnicas documentadas (classpath, paths, comandos)
- Lista completa de scripts Windows disponíveis
- Comandos úteis separados por plataforma
- Porta da API padronizada (8081) documentada

---

## [1.0.0] - 2025-09-23

### 🎉 Release Inicial

#### Integração Oracle Database
- **Conexão Oracle 100% funcional**
  - Driver `ojdbc8.jar` com auto-download
  - Scripts de compilação para Linux/Mac/Windows
  - Teste de conectividade automático
- **Schema completo** com 5 tabelas:
  - `TB_OPERATORS` - Operadores do sistema
  - `TB_DIGITAL_MICROSCOPES` - Microscópios digitais
  - `TB_SAMPLES` - Amostras patológicas
  - `TB_MICROSCOPY_IMAGES` - Imagens microscópicas
  - `TB_MEASUREMENTS` - Medições realizadas
- **Integração Python→Oracle**:
  - Módulo `oracle_integration.py` dedicado
  - Biblioteca `cx_Oracle` para conexão direta
  - Fallback automático para JSON
  - Auto-instalação de dependências
- **Scripts de verificação**:
  - `verify_oracle_integration.py` - Verificação completa
  - `verify_oracle_setup.py` - Setup e diagnóstico
  - `test_oracle_connection.sh` - Teste específico Java

#### Ambiente Virtual Python
- **Detecção automática** de ambientes gerenciados externamente
- **Criação automática** de venv em sistemas modernos
- **Resolução do erro** `externally-managed-environment`
- **Compatibilidade** com Ubuntu 24.04+, Debian 12+, Fedora 38+
- **Fallback inteligente** para `--user` em sistemas antigos
- Scripts atualizados para usar venv automaticamente

#### Estrutura Profissional
- **Diretório raiz limpo** com apenas 8 itens principais
- **Scripts organizados** por categoria (scripts/)
- **Documentação centralizada** (docs/)
- **Launchers rápidos** para componentes individuais:
  - `quick-python.sh/.bat` - Visão computacional
  - `quick-java.sh/.bat` - Backend Java
  - `quick-web.sh/.bat` - Dashboard web
- **Instalação em 2 comandos**:
  - `./install` ou `install.bat`
  - `./start` ou `start.bat`

#### Funcionalidades Core
- **Visão Computacional**:
  - Captura em tempo real com OpenCV
  - Processamento automático de imagens
  - Cálculo preciso de áreas
  - Preview em tempo real com contornos
- **Backend Java**:
  - Arquitetura DDD (Domain Driven Design)
  - Persistência Oracle com DAOs
  - API REST na porta 8081
  - Integração de dados JSON/Oracle
- **Frontend Web**:
  - Dashboard responsivo
  - Visualização em tempo real
  - Gráficos com Chart.js
  - Navegação entre seções

### Resolvido
- ❌ **ORA-00942**: table or view does not exist
  - Scripts de setup automático criados
  - Verificação de tabelas implementada
  - Diagnóstico avançado disponível
  - Documentação de troubleshooting completa
- ❌ **externally-managed-environment**: Python package installation error
  - Ambiente virtual criado automaticamente
  - Instalação isolada sem conflitos
  - Execução transparente com venv

---

## [0.9.0] - Sprint 3

### Adicionado
- Sistema local completo
- Interface Python parcialmente integrada
- API parcialmente integrada
- Salvamento de imagens no banco iniciado

---

## Tipos de Mudanças
- **Adicionado**: para novas funcionalidades
- **Modificado**: para mudanças em funcionalidades existentes
- **Depreciado**: para funcionalidades que serão removidas
- **Removido**: para funcionalidades removidas
- **Corrigido**: para correções de bugs
- **Segurança**: para vulnerabilidades corrigidas

---

**Última atualização:** 30/09/2025
**Versão atual:** 1.2.0