# Scanalyze Dashboard - Frontend Responsivo

Dashboard web responsivo para o Sistema de Micromedição Scanalyze com Tailwind CSS build process.

## 🚀 Mudanças Implementadas (Opção C - Abordagem Moderna)

### Arquitetura
- **Migrado de Tailwind CDN para build process local**
- CSS otimizado e minificado (apenas classes usadas)
- Estrutura modular com `@layer` do Tailwind
- Melhor performance e menor tamanho de arquivo

### Estrutura de Arquivos

```
frontend-dashboard/
├── index.html              # Arquivo HTML principal
├── dashboard.js            # JavaScript da aplicação
├── styles.css              # CSS compilado (NÃO EDITAR DIRETAMENTE)
├── package.json            # Dependências npm
├── tailwind.config.js      # Configuração do Tailwind
├── src/
│   └── input.css          # CSS fonte (EDITAR ESTE ARQUIVO)
└── node_modules/          # Dependências (gitignored)
```

## 📦 Dependências

- Node.js v24.1.0+
- npm 11.3.0+
- Tailwind CSS 3.4.1

## 🛠️ Scripts Disponíveis

### Build do CSS (Produção)
Compila e minifica o CSS:
```bash
npm run build:css
```

### Watch Mode (Desenvolvimento)
Recompila automaticamente quando src/input.css é modificado:
```bash
npm run watch:css
```

## 🎨 Customizando Estilos

### 1. Editar src/input.css
Este é o arquivo CSS fonte. Ele contém:
- **@layer base**: Estilos base customizados
- **@layer components**: Componentes reutilizáveis
- **@layer utilities**: Classes utilities customizadas

```css
/* Exemplo de customização */
@layer components {
  .meu-botao {
    @apply px-4 py-2 bg-blue-600 text-white rounded-lg;
  }
}
```

### 2. Recompilar
Após editar `src/input.css`, rode:
```bash
npm run build:css
```

**⚠️ IMPORTANTE:** Nunca edite `styles.css` diretamente! Ele é gerado automaticamente.

## 🎯 Features do CSS

### Responsividade
- **Mobile First**: Breakpoints otimizados
  - `sm:` 640px+
  - `md:` 768px+
  - `lg:` 1024px+
  - `xl:` 1280px+
  - `2xl:` 1536px+

### Menu Mobile
- Botão hamburger em telas < 768px
- Sidebar transforma-se em drawer lateral
- Overlay escuro com fechamento ao clicar fora
- Animações suaves com `transition-transform`

### Componentes Customizados
- `.section-content` / `.section-content.active`
- `.nav-link.active`
- `.list-item`
- Scrollbar customizado (webkit)

### Cores Customizadas
Paleta neutral e rose definida no `tailwind.config.js`:
- neutral: 50-900
- rose: 50-600

## 🐛 Troubleshooting

### CSS não atualiza
1. Verifique se editou `src/input.css` (não `styles.css`)
2. Rode `npm run build:css`
3. Force refresh no navegador (Ctrl+F5)

### Menu mobile não funciona
1. Verifique console do navegador para erros JavaScript
2. Confirme que `dashboard.js` está carregado
3. Verifique se `styles.css` foi recompilado

### Classes Tailwind não funcionam
1. Adicione a classe no HTML
2. Rode `npm run build:css` (Tailwind detecta automaticamente)
3. Refresh do navegador

## 📝 Desenvolvimento

### Workflow Recomendado
1. Abra terminal e rode: `npm run watch:css`
2. Edite `src/input.css` ou HTML conforme necessário
3. Tailwind recompila automaticamente
4. Refresh do navegador para ver mudanças

### Adicionando Nova Cor
1. Edite `tailwind.config.js`:
```js
theme: {
  extend: {
    colors: {
      'custom-blue': '#1234ab',
    }
  }
}
```
2. Use no HTML: `<div class="bg-custom-blue">`
3. Recompile: `npm run build:css`

## 🚢 Deploy / Produção

Antes de fazer deploy:
1. `npm run build:css` (gera CSS minificado)
2. Commit `styles.css` compilado
3. **NÃO** commite `node_modules/`

### .gitignore recomendado
```
node_modules/
.DS_Store
```

## ✅ Checklist de Problemas Resolvidos

- ✅ Conflitos CSS Tailwind CDN vs customizado
- ✅ Transition de transform quebrada
- ✅ Classes utilities faltantes (p-2, top-4, shadow-lg, etc.)
- ✅ Grid system funcionando corretamente
- ✅ Z-index e stacking context corretos
- ✅ Responsividade completa (mobile → desktop)
- ✅ Menu mobile com animações suaves
- ✅ Performance otimizada (CSS minificado)
- ✅ Build process automatizado

## 📚 Recursos

- [Documentação Tailwind CSS](https://tailwindcss.com/docs)
- [Tailwind Play (teste classes)](https://play.tailwindcss.com/)
- [Responsive Design](https://tailwindcss.com/docs/responsive-design)
- [Dark Mode](https://tailwindcss.com/docs/dark-mode) (para futuras implementações)

## 🔄 Atualizando Tailwind

```bash
npm update tailwindcss
npm run build:css
```

---

**Versão:** 2.0.0
**Última atualização:** 30/09/2025
**Desenvolvido por:** Scanalyze Team
