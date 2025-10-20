/**
 * Configuração do Frontend - Scanalyze Dashboard
 *
 * Para desenvolvimento local: Use http://localhost:8081/api
 * Para produção Azure: Atualize com a URL do Container App
 */

const CONFIG = {
    // URL base da API
    // Desenvolvimento: 'http://localhost:8081/api'
    // Produção: 'https://scanalyze-api.agreeableplant-ba923b61.eastus2.azurecontainerapps.io/api'
    API_BASE_URL: window.location.hostname === 'localhost'
        ? 'http://localhost:8081/api'
        : 'https://scanalyze-api.politeglacier-21141b44.eastus2.azurecontainerapps.io/api',

    // Caminho base para imagens
    get IMAGE_BASE_PATH() {
        return this.API_BASE_URL + '/images/';
    },

    // Configurações de auto-refresh (em milissegundos)
    AUTO_REFRESH_INTERVAL: 30000, // 30 segundos

    // Versão do dashboard
    VERSION: '2.0.0'
};

// Exporta para uso global
window.SCANALYZE_CONFIG = CONFIG;
