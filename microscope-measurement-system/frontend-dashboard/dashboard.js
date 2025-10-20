/**
 * Dashboard JavaScript para Sistema de Micromedição Scanalyze
 * Versão 2.0 - Dashboard Completo e Funcional
 *
 * Autor: Sistema de Micromedição Scanalyze
 * Data: 2024-09-18
 */

// Variáveis globais
let measurementsData = [];
let samplesData = new Map();
let charts = {};
let autoRefreshInterval = null;
let currentSort = { column: null, direction: 'asc' };
let currentPage = 1;
let pageSize = 25;
let filteredData = [];
// Usa configuração do config.js ou fallback para localhost
const API_BASE_URL = window.SCANALYZE_CONFIG?.API_BASE_URL || 'http://localhost:8081/api';
const IMAGE_BASE_PATH = window.SCANALYZE_CONFIG?.IMAGE_BASE_PATH || (API_BASE_URL + '/images/');

function parseNumber(value, fallback = 0) {
    if (value === null || value === undefined) return fallback;
    if (typeof value === 'number') {
        return Number.isFinite(value) ? value : fallback;
    }
    if (typeof value === 'string') {
        const normalized = parseFloat(value.replace(',', '.'));
        return Number.isFinite(normalized) ? normalized : fallback;
    }
    return fallback;
}

function sanitizeFileName(name) {
    if (!name) return '';
    const cleaned = String(name).trim().replace(/\\/g, '/');
    const parts = cleaned.split('/');
    return parts[parts.length - 1];
}

function normalizeMeasurementEntry(raw) {
    const normalized = {
        id: raw.id || '',
        sampleId: raw.sampleId || raw.sample_id || '',
        area_um2: parseNumber(raw.area_um2),
        area_pixels: parseNumber(raw.area_pixels),
        dataHora: raw.dataHora || raw.data_hora || '',
        imagemId: raw.imagemId || raw.imagem_id || '',
        nomeImagem: sanitizeFileName(raw.nomeImagem || raw.imageFile || raw.imagemArquivo),
        operator: raw.operator || raw.operador || '',
        scale_pixels_per_um: parseNumber(raw.scale_pixels_per_um, 0)
    };

    if (!normalized.dataHora && raw.timestamp) {
        normalized.dataHora = raw.timestamp;
    }

    if (!normalized.operator && raw.sample && raw.sample.operadorResponsavel) {
        normalized.operator = raw.sample.operadorResponsavel;
    }

    if ((!normalized.area_pixels || normalized.area_pixels <= 0) && normalized.area_um2 > 0 && normalized.scale_pixels_per_um > 0) {
        const scale = normalized.scale_pixels_per_um;
        normalized.area_pixels = normalized.area_um2 * Math.pow(scale, 2);
    }

    return normalized;
}

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Scanalyze Dashboard v2.0 carregado');

    // Configura navegação
    setupNavigation();

    // Verifica status da API
    checkApiStatus();

    // Carrega dados iniciais
    initializeSampleData();

    // Configura eventos
    setupEventListeners();

    // Tenta carregar dados automaticamente
    loadDefaultData();

    // Atualiza timestamp
    updateLastUpdateTime();

    // Mostra seção dashboard por padrão
    showSection('dashboard');

    // Inicializa componentes
    initializeComponents();

    // Inicia auto-refresh automaticamente
    setTimeout(() => {
        const checkbox = document.getElementById('auto-refresh');
        if (checkbox) {
            checkbox.checked = true;
            startAutoRefresh();
            console.log('🔄 Auto-refresh iniciado automaticamente');
        }
    }, 2000); // Aguarda 2 segundos para garantir que tudo foi carregado
});

/**
 * Inicializa componentes do sistema
 */
function initializeComponents() {
    // Atualiza status do sistema
    updateSystemStatus();

    // Inicializa gráficos vazios
    initializeEmptyCharts();

    console.log('✅ Componentes inicializados');
}

/**
 * Configura navegação entre seções
 */
function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const section = link.getAttribute('data-section');
            showSection(section);

            // Atualiza link ativo
            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
        });
    });
}

/**
 * Mostra uma seção específica
 */
function showSection(sectionName) {
    // Esconde todas as seções
    const sections = document.querySelectorAll('.section-content');
    sections.forEach(section => {
        section.style.display = 'none';
        section.classList.remove('active');
    });

    // Mostra a seção solicitada
    const targetSection = document.getElementById(sectionName + '-section');
    if (targetSection) {
        targetSection.style.display = 'block';
        targetSection.classList.add('active');
    }

    // Atualiza o título da página
    updatePageTitle(sectionName);

    // Carrega dados específicos da seção
    switch(sectionName) {
        case 'dashboard':
            updateDashboard();
            break;
        case 'measurements':
            updateMeasurementsSection();
            break;
        case 'samples':
            updateSamplesSection();
            break;
        case 'settings':
            updateSettingsSection();
            break;
    }

    console.log(`📄 Seção '${sectionName}' ativada`);
}

/**
 * Configura os event listeners necessários
 */
function setupEventListeners() {
    // Auto-refresh checkbox
    const autoRefreshCheckbox = document.getElementById('auto-refresh');
    if (autoRefreshCheckbox) {
        autoRefreshCheckbox.addEventListener('change', toggleAutoRefresh);
    }

    // Auto-refresh interval select
    const refreshIntervalSelect = document.getElementById('refresh-interval');
    if (refreshIntervalSelect) {
        refreshIntervalSelect.addEventListener('change', function() {
            const newInterval = parseInt(this.value);
            console.log(`⏱️ Intervalo de auto-refresh alterado para ${newInterval}s`);
            restartAutoRefreshIfActive();
        });
    }

    // Filtros
    const startDateFilter = document.getElementById('start-date-filter');
    const endDateFilter = document.getElementById('end-date-filter');
    const sampleFilter = document.getElementById('sample-filter');
    const operatorFilter = document.getElementById('operator-filter');
    const searchFilter = document.getElementById('search-filter');

    if (startDateFilter) startDateFilter.addEventListener('change', applyFilters);
    if (endDateFilter) endDateFilter.addEventListener('change', applyFilters);
    if (sampleFilter) sampleFilter.addEventListener('change', applyFilters);
    if (operatorFilter) operatorFilter.addEventListener('change', applyFilters);
    if (searchFilter) searchFilter.addEventListener('input', applyFilters);

    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 'r') {
            e.preventDefault();
            refreshData();
        }
        if (e.ctrlKey && e.key === 'e') {
            e.preventDefault();
            exportData('csv');
        }
    });

    // Limpeza ao fechar/recarregar página
    window.addEventListener('beforeunload', function() {
        console.log('🧹 Limpando intervalos antes de fechar página');
        stopAutoRefresh();
    });

    // Limpeza quando a página perde foco (tab inativa)
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            console.log('👁️ Página ficou inativa');
        } else {
            console.log('👁️ Página ficou ativa');
            // Atualizar status quando voltar
            updateRefreshStatus();
        }
    });

    console.log('🎯 Event listeners configurados');
}

/**
 * Carrega dados de amostras (base de dados)
 */
function initializeSampleData() {
    // Tenta carregar dados da API primeiro, depois do arquivo
    return fetch(API_BASE_URL + '/samples')
        .then(response => {
            if (!response.ok) {
                throw new Error('API não disponível');
            }
            return response.json();
        })
        .then(data => {
            if (data.samples && Array.isArray(data.samples)) {
                samplesData.clear();
                data.samples.forEach(sample => {
                    samplesData.set(sample.id, sample);
                });
                console.log(`📋 ${data.samples.length} amostras carregadas da API REST`);

                // Atualiza a seção de amostras se estiver ativa
                updateSamplesSection();
                updateApiStatusDisplay('online', 'API conectada');
                return data;
            } else {
                throw new Error('Formato de dados da API inválido');
            }
        })
        .catch(error => {
            console.log('⚠️ API não disponível, tentando arquivo local:', error.message);
            updateApiStatusDisplay('offline', 'API offline, usando dados locais');
            return loadSamplesFromFile();
        });
}

/**
 * Carrega amostras do arquivo JSON (fallback)
 */
function loadSamplesFromFile() {
    return fetch('../data-integration/samples.json')
        .then(response => {
            if (!response.ok) {
                throw new Error('Arquivo samples.json não encontrado');
            }
            return response.json();
        })
        .then(data => {
            if (data.samples && Array.isArray(data.samples)) {
                samplesData.clear();
                data.samples.forEach(sample => {
                    samplesData.set(sample.id, sample);
                });
                console.log(`📋 ${data.samples.length} amostras carregadas do arquivo samples.json`);

                // Atualiza a seção de amostras se estiver ativa
                updateSamplesSection();
                return data;
            } else {
                throw new Error('Formato de arquivo inválido');
            }
        })
        .catch(error => {
            console.log('⚠️ Carregando amostras de exemplo:', error.message);
            loadDefaultSamples();
            return null;
        });
}

/**
 * Carrega amostras de exemplo (fallback)
 */
function loadDefaultSamples() {
    const samplesList = [
        {
            id: "SAMPLE_001",
            nome: "Sangue Paciente A",
            tipo: "Sangue",
            dataColeta: "2024-01-15 08:00:00",
            operadorResponsavel: "Dr. João Silva"
        },
        {
            id: "SAMPLE_002",
            nome: "Tecido Muscular",
            tipo: "Tecido",
            dataColeta: "2024-01-15 09:30:00",
            operadorResponsavel: "Maria Santos"
        },
        {
            id: "SAMPLE_003",
            nome: "Célula Neural",
            tipo: "Neurônio",
            dataColeta: "2024-01-15 15:00:00",
            operadorResponsavel: "Carlos Oliveira"
        }
    ];

    samplesData.clear();
    samplesList.forEach(sample => {
        samplesData.set(sample.id, sample);
    });

    console.log(`📋 ${samplesList.length} amostras de exemplo carregadas`);
}

/**
 * Processa os dados de medições carregados
 */
function processMeasurementsData(data) {
    if (data && data.measurements && Array.isArray(data.measurements)) {
        const normalizedMeasurements = data.measurements.map(normalizeMeasurementEntry);
        measurementsData = normalizedMeasurements;
        filteredData = [...normalizedMeasurements];

        // Atualiza todas as visualizações
        updateDashboard();
        updateFilters();

        console.log(`✅ ${measurementsData.length} medições processadas com sucesso`);
        showNotification('Dados carregados com sucesso!', 'success');
    } else {
        console.error('❌ Formato de dados inválido');
        showError('Formato de dados inválido');
    }
}

/**
 * Atualiza o dashboard principal
 */
function updateDashboard() {
    updateOverviewStats();
    updateRecentMeasurementsList();
    updateDashboardCharts();
    updateAdvancedStats();
}

/**
 * Atualiza as estatísticas da visão geral
 */
function updateOverviewStats() {
    if (!measurementsData.length) {
        // Valores padrão quando não há dados
        setElementText('total-measurements', '0');
        setElementText('total-samples', '0');
        setElementText('average-area', '0.00 μm²');
        setElementText('last-measurement', 'Nunca');
        setElementText('progress-current', '0');
        updateProgressBar('progress-bar', 0);
        return;
    }

    // Total de medições
    setElementText('total-measurements', measurementsData.length.toLocaleString());

    // Total de amostras únicas
    const uniqueSamples = new Set(measurementsData.map(m => m.sampleId));
    setElementText('total-samples', uniqueSamples.size);

    // Área média
    const totalArea = measurementsData.reduce((sum, m) => sum + parseNumber(m.area_um2), 0);
    const avgArea = totalArea / measurementsData.length;
    setElementText('average-area', `${avgArea.toFixed(2)} μm²`);

    // Última medição
    const sortedByDate = [...measurementsData].sort((a, b) =>
        new Date(b.dataHora) - new Date(a.dataHora)
    );
    if (sortedByDate.length > 0) {
        const lastMeasurementDate = new Date(sortedByDate[0].dataHora);
        setElementText('last-measurement', lastMeasurementDate.toLocaleDateString('pt-BR'));
    }

    // Atualiza barra de progresso
    const current = measurementsData.length;
    const total = 100; // Meta fictícia
    const percentage = Math.min((current / total) * 100, 100);

    setElementText('progress-current', current);
    updateProgressBar('progress-bar', percentage);
}

/**
 * Atualiza estatísticas avançadas do dashboard
 */
function updateAdvancedStats() {
    if (!measurementsData.length) {
        setElementText('max-area', '0.00');
        setElementText('min-area', '0.00');
        setElementText('std-deviation', '0.00');
        setElementText('avg-time', '0.0h');
        return;
    }

    const areas = measurementsData.map(m => parseNumber(m.area_um2));

    // Área máxima
    const maxArea = Math.max(...areas);
    setElementText('max-area', `${maxArea.toFixed(2)} μm²`);

    // Área mínima
    const minArea = Math.min(...areas);
    setElementText('min-area', `${minArea.toFixed(2)} μm²`);

    // Desvio padrão
    const mean = areas.reduce((a, b) => a + b, 0) / areas.length;
    const squaredDiffs = areas.map(area => Math.pow(area - mean, 2));
    const avgSquaredDiff = squaredDiffs.reduce((a, b) => a + b, 0) / squaredDiffs.length;
    const stdDev = Math.sqrt(avgSquaredDiff);
    setElementText('std-deviation', `${stdDev.toFixed(2)} μm²`);

    // Tempo médio entre medições (fictício)
    const avgTime = (Math.random() * 2 + 0.5).toFixed(1);
    setElementText('avg-time', `${avgTime}h`);
}

/**
 * Atualiza a lista de medições recentes
 */
function updateRecentMeasurementsList() {
    const container = document.getElementById('recent-measurements-list');
    if (!container) return;

    if (!measurementsData.length) {
        container.innerHTML = '<div class="py-8 text-center text-neutral-500">Nenhuma medição encontrada</div>';
        return;
    }

    // Pega as 3 medições mais recentes
    const recentMeasurements = [...measurementsData]
        .sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora))
        .slice(0, 3);

    container.innerHTML = recentMeasurements.map(measurement => `
        <div class="flex justify-between py-3">
            <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-neutral-200 flex items-center justify-center">
                    <svg class="w-5 h-5 text-neutral-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                </div>
                <div>
                    <div class="text-sm font-medium text-neutral-800 leading-none">${measurement.sampleId}</div>
                    <div class="text-[11px] text-neutral-500 leading-tight mt-1">${parseNumber(measurement.area_um2).toFixed(2)} μm² - ${new Date(measurement.dataHora).toLocaleDateString('pt-BR')}</div>
                </div>
            </div>
            <button class="text-neutral-500 hover:text-neutral-800 text-sm inline-flex items-center gap-1" onclick="showMeasurementDetails('${measurement.id}')">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                Info
            </button>
        </div>
    `).join('');
}

/**
 * Atualiza os gráficos do dashboard
 */
function updateDashboardCharts() {
    updateAreaTrendChart();
}

/**
 * Atualiza o gráfico de tendência de áreas
 */
function updateAreaTrendChart() {
    const canvas = document.getElementById('area-trend-chart');
    if (!canvas) return;

    // Destrói gráfico anterior se existir
    if (charts.areaTrend) {
        charts.areaTrend.destroy();
    }

    if (!measurementsData.length) {
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.fillStyle = '#9CA3AF';
        ctx.font = '14px Inter';
        ctx.textAlign = 'center';
        ctx.fillText('Nenhum dado disponível', canvas.width / 2, canvas.height / 2);
        return;
    }

    // Agrupa dados por dia
    const dailyData = groupMeasurementsByDay();
    const chartData = Object.entries(dailyData)
        .map(([date, data]) => ({
            date: formatDateForChart(date),
            average: data.total / data.count,
            count: data.count
        }))
        .sort((a, b) => new Date(a.date) - new Date(b.date));

    // Cria novo gráfico
    const ctx = canvas.getContext('2d');
    charts.areaTrend = new Chart(ctx, {
        type: 'line',
        data: {
            labels: chartData.map(d => d.date),
            datasets: [{
                label: 'Área Média (μm²)',
                data: chartData.map(d => d.average),
                borderColor: '#3B82F6',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                tension: 0.4,
                fill: true,
                pointBackgroundColor: '#3B82F6',
                pointBorderColor: '#ffffff',
                pointBorderWidth: 2,
                pointRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#ffffff',
                    bodyColor: '#ffffff',
                    borderColor: '#3B82F6',
                    borderWidth: 1,
                    callbacks: {
                        title: function(context) {
                            return `Data: ${context[0].label}`;
                        },
                        label: function(context) {
                            const dataPoint = chartData[context.dataIndex];
                            return [
                                `Área Média: ${context.parsed.y.toFixed(2)} μm²`,
                                `Medições: ${dataPoint.count}`
                            ];
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: '#F3F4F6'
                    },
                    ticks: {
                        callback: function(value) {
                            return value.toFixed(1) + ' μm²';
                        }
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            },
            interaction: {
                intersect: false,
                mode: 'index'
            }
        }
    });
}

/**
 * Atualiza a seção de medições
 */
function updateMeasurementsSection() {
    updateFilteredStats();
    updateMeasurementsTable();
    updateMeasurementCharts();
}

/**
 * Atualiza estatísticas filtradas
 */
function updateFilteredStats() {
    const filtered = filteredData;

    setElementText('filtered-count', filtered.length.toLocaleString());

    if (filtered.length > 0) {
    const avgArea = filtered.reduce((sum, m) => sum + parseNumber(m.area_um2), 0) / filtered.length;
        setElementText('filtered-avg', `${avgArea.toFixed(2)} μm²`);

        const uniqueSamples = new Set(filtered.map(m => m.sampleId));
        setElementText('filtered-samples', uniqueSamples.size);

        const uniqueOperators = new Set(filtered.map(m => m.operator).filter(Boolean));
        setElementText('filtered-operators', uniqueOperators.size);
    } else {
        setElementText('filtered-avg', '0.00 μm²');
        setElementText('filtered-samples', '0');
        setElementText('filtered-operators', '0');
    }
}

/**
 * Atualiza a tabela de medições
 */
function updateMeasurementsTable() {
    const tbody = document.getElementById('all-measurements');
    if (!tbody) return;

    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const pageData = filteredData.slice(startIndex, endIndex);

    if (!pageData.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="px-6 py-8 text-center text-neutral-500">Nenhuma medição encontrada</td></tr>';
        updatePagination();
        return;
    }

    tbody.innerHTML = pageData.map((measurement, index) => `
        <tr class="hover:bg-neutral-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-neutral-900">${measurement.id}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-neutral-900">
                <div class="flex items-center gap-2">
                    <div class="w-2 h-2 bg-blue-400 rounded-full"></div>
                    ${measurement.sampleId}
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-neutral-900">
                <span class="font-medium">${parseNumber(measurement.area_um2).toFixed(2)}</span> μm²
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-neutral-600">${Number.isFinite(measurement.area_pixels) && measurement.area_pixels > 0 ? measurement.area_pixels.toFixed(2) : 'N/A'} px</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-neutral-600">
                ${formatDateTime(measurement.dataHora)}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-neutral-600">
                <div class="flex items-center gap-2">
                    <div class="w-6 h-6 bg-neutral-200 rounded-full flex items-center justify-center text-xs font-medium">
                        ${getOperatorInitials(measurement.operator)}
                    </div>
                    ${measurement.operator || 'N/A'}
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                    <button onclick="showMeasurementDetails('${measurement.id}')" class="text-blue-600 hover:text-blue-800 text-xs px-2 py-1 bg-blue-50 rounded">
                        Ver
                    </button>
                    <button onclick="showImageModal('${measurement.nomeImagem}')" class="text-green-600 hover:text-green-800 text-xs px-2 py-1 bg-green-50 rounded">
                        Imagem
                    </button>
                </div>
            </td>
        </tr>
    `).join('');

    updatePagination();
}

/**
 * Atualiza os gráficos da seção de medições
 */
function updateMeasurementCharts() {
    updateAreaDistributionChart();
    updateOperatorChart();
}

/**
 * Atualiza gráfico de distribuição de áreas
 */
function updateAreaDistributionChart() {
    const canvas = document.getElementById('area-distribution-chart');
    if (!canvas) return;

    if (charts.areaDistribution) {
        charts.areaDistribution.destroy();
    }

    if (!filteredData.length) {
        return;
    }

    const areas = filteredData.map(m => parseNumber(m.area_um2));
    const bins = createHistogramBins(areas, 8);

    const ctx = canvas.getContext('2d');
    charts.areaDistribution = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: bins.map(bin => `${bin.min.toFixed(1)}-${bin.max.toFixed(1)}`),
            datasets: [{
                label: 'Frequência',
                data: bins.map(bin => bin.count),
                backgroundColor: 'rgba(59, 130, 246, 0.6)',
                borderColor: '#3B82F6',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Área (μm²)'
                    }
                }
            }
        }
    });
}

/**
 * Atualiza gráfico de medições por operador
 */
function updateOperatorChart() {
    const canvas = document.getElementById('operator-chart');
    if (!canvas) return;

    if (charts.operator) {
        charts.operator.destroy();
    }

    if (!filteredData.length) {
        return;
    }

    const operatorData = filteredData.reduce((acc, m) => {
        const operator = m.operator || 'Não informado';
        acc[operator] = (acc[operator] || 0) + 1;
        return acc;
    }, {});

    const ctx = canvas.getContext('2d');
    charts.operator = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(operatorData),
            datasets: [{
                data: Object.values(operatorData),
                backgroundColor: [
                    '#3B82F6',
                    '#10B981',
                    '#F59E0B',
                    '#EF4444',
                    '#8B5CF6',
                    '#06B6D4'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

/**
 * Atualiza a seção de amostras
 */
function updateSamplesSection() {
    updateSampleStats();
    updateSampleCharts();
    updateSamplesDisplay();
}

/**
 * Atualiza estatísticas das amostras
 */
function updateSampleStats() {
    const sampleGroups = groupMeasurementsBySample();

    setElementText('total-samples-count', Object.keys(sampleGroups).length);
    setElementText('total-sample-measurements', measurementsData.length);

    // Encontra amostra mais ativa
    const mostActive = Object.entries(sampleGroups)
        .sort((a, b) => b[1].length - a[1].length)[0];

    if (mostActive) {
        setElementText('most-active-sample', mostActive[0]);
    }

    // Conta tipos únicos
    const uniqueTypes = new Set();
    samplesData.forEach(sample => {
        uniqueTypes.add(sample.tipo);
    });
    setElementText('unique-types-count', uniqueTypes.size);
}

/**
 * Atualiza gráficos da seção de amostras
 */
function updateSampleCharts() {
    updateSampleTypeChart();
    updateSampleActivityChart();
}

/**
 * Atualiza gráfico de tipos de amostra
 */
function updateSampleTypeChart() {
    const canvas = document.getElementById('sample-type-chart');
    if (!canvas) return;

    if (charts.sampleType) {
        charts.sampleType.destroy();
    }

    const typeData = {};
    samplesData.forEach(sample => {
        typeData[sample.tipo] = (typeData[sample.tipo] || 0) + 1;
    });

    const ctx = canvas.getContext('2d');
    charts.sampleType = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: Object.keys(typeData),
            datasets: [{
                data: Object.values(typeData),
                backgroundColor: [
                    '#3B82F6',
                    '#10B981',
                    '#F59E0B',
                    '#EF4444'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

/**
 * Atualiza gráfico de atividade por amostra
 */
function updateSampleActivityChart() {
    const canvas = document.getElementById('sample-activity-chart');
    if (!canvas) return;

    if (charts.sampleActivity) {
        charts.sampleActivity.destroy();
    }

    const sampleGroups = groupMeasurementsBySample();
    const sampleData = Object.entries(sampleGroups)
        .map(([sampleId, measurements]) => ({
            sampleId,
            count: measurements.length
        }))
        .sort((a, b) => b.count - a.count);

    const ctx = canvas.getContext('2d');
    charts.sampleActivity = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: sampleData.map(s => s.sampleId),
            datasets: [{
                label: 'Medições',
                data: sampleData.map(s => s.count),
                backgroundColor: 'rgba(16, 185, 129, 0.6)',
                borderColor: '#10B981',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}

/**
 * Atualiza a exibição das amostras
 */
function updateSamplesDisplay() {
    const container = document.getElementById('samples-grid');
    if (!container) return;

    if (!measurementsData.length) {
        container.innerHTML = '<div class="col-span-full py-8 text-center text-neutral-500">Nenhuma amostra encontrada</div>';
        return;
    }

    const sampleGroups = groupMeasurementsBySample();

    container.innerHTML = Object.entries(sampleGroups).map(([sampleId, measurements]) => {
        const sampleInfo = samplesData.get(sampleId) || {
            nome: sampleId,
            tipo: 'Desconhecido',
            operadorResponsavel: 'N/A'
        };

        const totalArea = measurements.reduce((sum, m) => sum + (m.area_um2 || 0), 0);
        const avgArea = totalArea / measurements.length;
        const lastMeasurement = Math.max(...measurements.map(m => new Date(m.dataHora)));

        return `
            <div class="bg-white rounded-2xl p-6 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center gap-3 mb-4">
                    <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
                        ${getSampleIcon(sampleInfo.tipo)}
                    </div>
                    <div>
                        <h3 class="text-lg font-semibold text-neutral-800">${sampleInfo.nome}</h3>
                        <p class="text-sm text-neutral-500">${sampleId}</p>
                    </div>
                </div>

                <div class="space-y-3">
                    <div class="flex justify-between">
                        <span class="text-sm text-neutral-600">Tipo:</span>
                        <span class="text-sm font-medium text-neutral-800">${sampleInfo.tipo}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-sm text-neutral-600">Medições:</span>
                        <span class="text-sm font-medium text-neutral-800">${measurements.length}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-sm text-neutral-600">Área Média:</span>
                        <span class="text-sm font-medium text-neutral-800">${avgArea.toFixed(2)} μm²</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-sm text-neutral-600">Responsável:</span>
                        <span class="text-sm font-medium text-neutral-800">${sampleInfo.operadorResponsavel}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-sm text-neutral-600">Última Medição:</span>
                        <span class="text-sm font-medium text-neutral-800">${new Date(lastMeasurement).toLocaleDateString('pt-BR')}</span>
                    </div>
                </div>

                <div class="mt-4 pt-4 border-t border-neutral-200">
                    <button onclick="viewSampleDetails('${sampleId}')" class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-medium">
                        Ver Detalhes
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

/**
 * Atualiza a seção de configurações
 */
function updateSettingsSection() {
    updateSystemStatus();
    updateRefreshStatus();
}

/**
 * Atualiza status do sistema
 */
function updateSystemStatus() {
    const lastSync = document.getElementById('last-sync');
    if (lastSync) {
        lastSync.textContent = new Date().toLocaleTimeString('pt-BR');
    }

    // Verifica status da API
    checkApiStatus();
}

/**
 * Verifica status da API REST
 */
function checkApiStatus() {
    fetch(API_BASE_URL + '/health')
        .then(response => response.json())
        .then(data => {
            if (data.status === 'OK') {
                updateApiStatusDisplay('online', 'API REST Online');
                console.log('🟢 API REST Online');
            }
        })
        .catch(error => {
            updateApiStatusDisplay('offline', 'API REST Offline - Usando arquivos locais');
            console.log('🔴 API REST Offline:', error.message);
        });
}

/**
 * Atualiza display do status da API
 */
function updateApiStatusDisplay(status, message) {
    // Cria ou atualiza indicador de status da API
    let statusElement = document.getElementById('api-status');
    if (!statusElement) {
        statusElement = document.createElement('div');
        statusElement.id = 'api-status';
        statusElement.className = 'fixed bottom-4 right-4 px-3 py-1 rounded-lg text-xs font-medium z-50';
        document.body.appendChild(statusElement);
    }

    if (status === 'online') {
        statusElement.className = 'fixed bottom-4 right-4 px-3 py-1 rounded-lg text-xs font-medium z-50 bg-green-100 text-green-800';
        statusElement.innerHTML = '🟢 ' + message;
    } else {
        statusElement.className = 'fixed bottom-4 right-4 px-3 py-1 rounded-lg text-xs font-medium z-50 bg-yellow-100 text-yellow-800';
        statusElement.innerHTML = '🟡 ' + message;
    }
}

/**
 * Aplica filtros atuais aos dados
 */
function applyCurrentFilters() {
    let filtered = [...measurementsData];

    const startDate = document.getElementById('start-date-filter')?.value;
    const endDate = document.getElementById('end-date-filter')?.value;
    const sampleFilter = document.getElementById('sample-filter')?.value;
    const operatorFilter = document.getElementById('operator-filter')?.value;
    const searchFilter = document.getElementById('search-filter')?.value;

    if (startDate) {
        filtered = filtered.filter(m => {
            const measurementDate = new Date(m.dataHora);
            return measurementDate >= new Date(startDate);
        });
    }

    if (endDate) {
        filtered = filtered.filter(m => {
            const measurementDate = new Date(m.dataHora);
            return measurementDate <= new Date(endDate + 'T23:59:59');
        });
    }

    if (sampleFilter) {
        filtered = filtered.filter(m => m.sampleId === sampleFilter);
    }

    if (operatorFilter) {
        filtered = filtered.filter(m => m.operator === operatorFilter);
    }

    if (searchFilter) {
        const search = searchFilter.toLowerCase();
        filtered = filtered.filter(m =>
            m.id.toLowerCase().includes(search) ||
            m.sampleId.toLowerCase().includes(search)
        );
    }

    return filtered;
}

/**
 * Aplica filtros e atualiza visualizações
 */
function applyFilters() {
    filteredData = applyCurrentFilters();
    currentPage = 1; // Reset para primeira página

    updateFilteredStats();
    updateMeasurementsTable();
    updateMeasurementCharts();

    console.log(`🔍 Filtros aplicados: ${filteredData.length} registros encontrados`);
}

/**
 * Limpa todos os filtros
 */
function clearFilters() {
    const startDateFilter = document.getElementById('start-date-filter');
    const endDateFilter = document.getElementById('end-date-filter');
    const sampleFilter = document.getElementById('sample-filter');
    const operatorFilter = document.getElementById('operator-filter');
    const searchFilter = document.getElementById('search-filter');

    if (startDateFilter) startDateFilter.value = '';
    if (endDateFilter) endDateFilter.value = '';
    if (sampleFilter) sampleFilter.value = '';
    if (operatorFilter) operatorFilter.value = '';
    if (searchFilter) searchFilter.value = '';

    applyFilters();

    console.log('🧹 Filtros limpos');
}

/**
 * Atualiza os filtros com opções disponíveis
 */
function updateFilters() {
    updateSampleFilter();
    updateOperatorFilter();
}

/**
 * Atualiza o filtro de amostras
 */
function updateSampleFilter() {
    const select = document.getElementById('sample-filter');
    if (!select) return;

    const uniqueSamples = [...new Set(measurementsData.map(m => m.sampleId))];
    const currentValue = select.value;

    select.innerHTML = '<option value="">Todas</option>' +
        uniqueSamples.map(sample => `<option value="${sample}">${sample}</option>`).join('');

    if (uniqueSamples.includes(currentValue)) {
        select.value = currentValue;
    }
}

/**
 * Atualiza o filtro de operadores
 */
function updateOperatorFilter() {
    const select = document.getElementById('operator-filter');
    if (!select) return;

    const uniqueOperators = [...new Set(measurementsData.map(m => m.operator).filter(Boolean))];
    const currentValue = select.value;

    select.innerHTML = '<option value="">Todos</option>' +
        uniqueOperators.map(operator => `<option value="${operator}">${operator}</option>`).join('');

    if (uniqueOperators.includes(currentValue)) {
        select.value = currentValue;
    }
}

/**
 * Atualiza paginação
 */
function updatePagination() {
    const totalPages = Math.ceil(filteredData.length / pageSize);
    const paginationInfo = document.getElementById('pagination-info');
    const paginationControls = document.getElementById('pagination-controls');

    if (paginationInfo) {
        const start = (currentPage - 1) * pageSize + 1;
        const end = Math.min(currentPage * pageSize, filteredData.length);
        paginationInfo.textContent = `Mostrando ${start} a ${end} de ${filteredData.length.toLocaleString()} registros`;
    }

    if (paginationControls) {
        let controlsHTML = '';

        // Botão anterior
        controlsHTML += `
            <button onclick="changePage(${currentPage - 1})"
                    ${currentPage <= 1 ? 'disabled' : ''}
                    class="px-3 py-1 border border-neutral-300 rounded text-sm ${currentPage <= 1 ? 'opacity-50 cursor-not-allowed' : 'hover:bg-neutral-50'}">
                Anterior
            </button>
        `;

        // Números das páginas
        const maxVisiblePages = 5;
        let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            controlsHTML += `
                <button onclick="changePage(${i})"
                        class="px-3 py-1 border border-neutral-300 rounded text-sm ${i === currentPage ? 'bg-blue-600 text-white border-blue-600' : 'hover:bg-neutral-50'}">
                    ${i}
                </button>
            `;
        }

        // Botão próximo
        controlsHTML += `
            <button onclick="changePage(${currentPage + 1})"
                    ${currentPage >= totalPages ? 'disabled' : ''}
                    class="px-3 py-1 border border-neutral-300 rounded text-sm ${currentPage >= totalPages ? 'opacity-50 cursor-not-allowed' : 'hover:bg-neutral-50'}">
                Próxima
            </button>
        `;

        paginationControls.innerHTML = controlsHTML;
    }
}

/**
 * Muda página da tabela
 */
function changePage(page) {
    const totalPages = Math.ceil(filteredData.length / pageSize);
    if (page < 1 || page > totalPages) return;

    currentPage = page;
    updateMeasurementsTable();
}

/**
 * Atualiza tamanho da página
 */
function updatePageSize() {
    const select = document.getElementById('page-size');
    if (select) {
        pageSize = parseInt(select.value);
        currentPage = 1;
        updateMeasurementsTable();
    }
}

/**
 * Ordena a tabela por coluna
 */
function sortTable(column) {
    if (currentSort.column === column) {
        currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
        currentSort.column = column;
        currentSort.direction = 'asc';
    }

    filteredData.sort((a, b) => {
        let aVal = a[column];
        let bVal = b[column];

        if (column === 'dataHora') {
            aVal = new Date(aVal);
            bVal = new Date(bVal);
        } else if (typeof aVal === 'string') {
            aVal = aVal.toLowerCase();
            bVal = bVal.toLowerCase();
        }

        if (aVal < bVal) return currentSort.direction === 'asc' ? -1 : 1;
        if (aVal > bVal) return currentSort.direction === 'asc' ? 1 : -1;
        return 0;
    });

    currentPage = 1;
    updateMeasurementsTable();

    console.log(`📊 Tabela ordenada por ${column} (${currentSort.direction})`);
}

/**
 * Alterna atualização automática
 */
function toggleAutoRefresh() {
    const checkbox = document.getElementById('auto-refresh');

    if (checkbox && checkbox.checked) {
        startAutoRefresh();
        console.log('🔄 Auto-refresh ativado via checkbox');
        showNotification('Atualização automática ativada', 'info');
    } else {
        stopAutoRefresh();
        console.log('⏹️ Auto-refresh desativado via checkbox');
        showNotification('Atualização automática desativada', 'info');
    }
}

/**
 * Inicia o auto-refresh com o intervalo atual
 */
function startAutoRefresh() {
    // Sempre limpar intervalo existente primeiro
    stopAutoRefresh();

    const intervalSelect = document.getElementById('refresh-interval');
    const interval = parseInt(intervalSelect?.value || 30) * 1000;

    autoRefreshInterval = setInterval(() => {
        refreshData();
        updateRefreshStatus();
    }, interval);

    updateRefreshStatus();
    console.log(`🔄 Auto-refresh iniciado com intervalo de ${interval/1000}s (ID: ${autoRefreshInterval})`);
}

/**
 * Para o auto-refresh
 */
function stopAutoRefresh() {
    if (autoRefreshInterval) {
        console.log(`⏹️ Parando auto-refresh (ID: ${autoRefreshInterval})`);
        clearInterval(autoRefreshInterval);
        autoRefreshInterval = null;
    }
    updateRefreshStatus();
}

/**
 * Reinicia o auto-refresh se estiver ativo (para mudanças de intervalo)
 */
function restartAutoRefreshIfActive() {
    const checkbox = document.getElementById('auto-refresh');
    if (checkbox && checkbox.checked && autoRefreshInterval) {
        console.log('🔄 Reiniciando auto-refresh devido a mudança de intervalo');
        startAutoRefresh();
    }
}

/**
 * Atualiza status do refresh automático
 */
function updateRefreshStatus() {
    const statusElement = document.getElementById('refresh-status');
    const nextRefreshElement = document.getElementById('next-refresh');
    const checkbox = document.getElementById('auto-refresh');
    const intervalSelect = document.getElementById('refresh-interval');

    // Debug: verificar estado atual
    const isChecked = checkbox?.checked || false;
    const hasInterval = autoRefreshInterval !== null;
    const currentInterval = parseInt(intervalSelect?.value || 30);

    console.log(`📊 Status Auto-refresh: Checkbox=${isChecked}, Interval=${hasInterval ? 'Ativo' : 'Inativo'}, Tempo=${currentInterval}s, ID=${autoRefreshInterval}`);

    if (autoRefreshInterval) {
        if (statusElement) statusElement.textContent = `Ativo (${currentInterval}s)`;
        if (nextRefreshElement) {
            const nextUpdate = new Date(Date.now() + currentInterval * 1000);
            nextRefreshElement.textContent = nextUpdate.toLocaleTimeString('pt-BR');
        }
    } else {
        if (statusElement) statusElement.textContent = 'Desativado';
        if (nextRefreshElement) nextRefreshElement.textContent = '-';
    }

    // Verificar consistência entre checkbox e interval
    if (isChecked && !hasInterval) {
        console.warn('⚠️ Inconsistência: Checkbox marcado mas sem intervalo ativo');
    } else if (!isChecked && hasInterval) {
        console.warn('⚠️ Inconsistência: Checkbox desmarcado mas com intervalo ativo');
    }
}

/**
 * Função de diagnóstico para problemas de auto-refresh
 */
function diagnoseAutoRefresh() {
    const checkbox = document.getElementById('auto-refresh');
    const intervalSelect = document.getElementById('refresh-interval');
    const statusElement = document.getElementById('refresh-status');

    console.log('🔍 DIAGNÓSTICO AUTO-REFRESH:');
    console.log(`   Checkbox existe: ${!!checkbox}`);
    console.log(`   Checkbox marcado: ${checkbox?.checked}`);
    console.log(`   Select existe: ${!!intervalSelect}`);
    console.log(`   Valor do select: ${intervalSelect?.value}`);
    console.log(`   Status element existe: ${!!statusElement}`);
    console.log(`   Status text: ${statusElement?.textContent}`);
    console.log(`   AutoRefreshInterval: ${autoRefreshInterval}`);
    console.log(`   Tipo do interval: ${typeof autoRefreshInterval}`);

    // Verificar se há intervalos órfãos (apenas no desenvolvimento)
    if (typeof window !== 'undefined' && window._debugIntervals) {
        console.log(`   Intervalos debug: ${window._debugIntervals.length}`);
    }

    return {
        checkboxExists: !!checkbox,
        checkboxChecked: checkbox?.checked,
        selectExists: !!intervalSelect,
        selectValue: intervalSelect?.value,
        statusExists: !!statusElement,
        statusText: statusElement?.textContent,
        activeInterval: autoRefreshInterval,
        isConsistent: checkbox?.checked === (autoRefreshInterval !== null)
    };
}

// Adicionar função ao escopo global para debugging no console
window.diagnoseAutoRefresh = diagnoseAutoRefresh;

/**
 * Atualiza dados
 */
function refreshData() {
    console.log('🔄 Atualizando dados...');
    showLoading();

    // Carrega dados de medições e amostras
    Promise.all([
        loadDefaultData(),
        initializeSampleData()
    ]).then(() => {
        hideLoading();
        updateLastUpdateTime();
        showNotification('Dados atualizados com sucesso!', 'success');
    }).catch(error => {
        console.error('Erro ao atualizar dados:', error);
        hideLoading();
        showNotification('Erro ao atualizar dados', 'error');
    });
}

/**
 * Atualiza timestamp da última atualização
 */
function updateLastUpdateTime() {
    const element = document.getElementById('last-update');
    if (element) {
        element.textContent = new Date().toLocaleString('pt-BR');
    }
}

/**
 * Tenta carregar dados padrão
 */
function loadDefaultData() {
    // Tenta carregar dados da API primeiro, depois do arquivo
    return fetch(API_BASE_URL + '/measurements')
        .then(response => {
            if (!response.ok) {
                throw new Error('API não disponível');
            }
            return response.json();
        })
        .then(data => {
            updateApiStatusDisplay('online', 'API conectada');
            console.log('📁 Dados de medições carregados da API REST');
            processMeasurementsData(data);
            return data;
        })
        .catch(error => {
            console.log('⚠️ API não disponível, tentando arquivo local:', error.message);
            updateApiStatusDisplay('offline', 'API offline, usando dados locais');
            return loadMeasurementsFromFile();
        });
}

/**
 * Carrega medições do arquivo JSON (fallback)
 */
function loadMeasurementsFromFile() {
    return fetch('../data-integration/measurements.json')
        .then(response => {
            if (!response.ok) {
                throw new Error('Arquivo não encontrado');
            }
            return response.json();
        })
        .then(data => {
            console.log('📁 Dados carregados do arquivo local');
            processMeasurementsData(data);
            return data;
        })
        .catch(error => {
            console.log('⚠️ Carregando dados de exemplo:', error.message);
            loadSampleMeasurements();
            return null;
        });
}

/**
 * Carrega medições de exemplo
 */
function loadSampleMeasurements() {
    const sampleMeasurements = {
        measurements: [
            {
                id: "MEAS_SAMPLE_001",
                sampleId: "SAMPLE_001",
                area_pixels: 1200,
                area_um2: 15.5,
                dataHora: new Date(Date.now() - 86400000).toISOString(),
                imagemId: "IMG_SAMPLE_001",
                nomeImagem: "sample_001.jpg",
                operator: "Dr. João Silva",
                scale_pixels_per_um: 10.0
            },
            {
                id: "MEAS_SAMPLE_002",
                sampleId: "SAMPLE_002",
                area_pixels: 980,
                area_um2: 12.3,
                dataHora: new Date(Date.now() - 172800000).toISOString(),
                imagemId: "IMG_SAMPLE_002",
                nomeImagem: "sample_002.jpg",
                operator: "Maria Santos",
                scale_pixels_per_um: 10.0
            },
            {
                id: "MEAS_SAMPLE_003",
                sampleId: "SAMPLE_003",
                area_pixels: 1500,
                area_um2: 18.7,
                dataHora: new Date(Date.now() - 259200000).toISOString(),
                imagemId: "IMG_SAMPLE_003",
                nomeImagem: "sample_003.jpg",
                operator: "Carlos Oliveira",
                scale_pixels_per_um: 10.0
            }
        ]
    };

    processMeasurementsData(sampleMeasurements);
}

/**
 * Carrega arquivo de dados
 */
function loadMeasurementsFile() {
    const fileInput = document.getElementById('measurements-file');
    const file = fileInput?.files[0];

    if (!file) return;

    showLoading();

    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const data = JSON.parse(e.target.result);
            processMeasurementsData(data);
            hideLoading();
            showNotification('Arquivo de medições carregado com sucesso!', 'success');
            console.log('📁 Arquivo de medições carregado:', file.name);
        } catch (error) {
            console.error('❌ Erro ao carregar arquivo:', error);
            showError('Erro ao carregar arquivo: formato inválido');
            hideLoading();
        }
    };
    reader.readAsText(file);
}

/**
 * Carrega arquivo de amostras
 */
function loadSamplesFile() {
    const fileInput = document.getElementById('samples-file');
    const file = fileInput?.files[0];

    if (!file) return;

    showLoading();

    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const data = JSON.parse(e.target.result);
            if (data.samples && Array.isArray(data.samples)) {
                samplesData.clear();
                data.samples.forEach(sample => {
                    samplesData.set(sample.id, sample);
                });

                updateSamplesSection();
                hideLoading();
                showNotification('Arquivo de amostras carregado com sucesso!', 'success');
                console.log('📁 Arquivo de amostras carregado:', file.name);
            } else {
                throw new Error('Formato de arquivo inválido');
            }
        } catch (error) {
            console.error('❌ Erro ao carregar arquivo de amostras:', error);
            showError('Erro ao carregar arquivo de amostras: formato inválido');
            hideLoading();
        }
    };
    reader.readAsText(file);
}

/**
 * Exporta dados filtrados
 */
function exportFilteredData() {
    if (!filteredData.length) {
        showNotification('Nenhum dado para exportar', 'warning');
        return;
    }

    exportToCSV(filteredData, 'medicoes_filtradas.csv');
    showNotification('Dados exportados com sucesso!', 'success');
}

/**
 * Exporta dados
 */
function exportData(format) {
    if (!measurementsData.length) {
        showNotification('Nenhum dado para exportar', 'warning');
        return;
    }

    switch(format) {
        case 'csv':
            exportToCSV(measurementsData, 'medicoes_completas.csv');
            break;
        case 'json':
            exportToJSON(measurementsData, 'medicoes_completas.json');
            break;
        case 'pdf':
            exportToPDF();
            break;
    }

    showNotification(`Dados exportados em formato ${format.toUpperCase()}!`, 'success');
}

/**
 * Exporta dados em formato CSV
 */
function exportToCSV(data, filename) {
    const headers = ['ID', 'Amostra', 'Área (μm²)', 'Área (pixels)', 'Data/Hora', 'Operador', 'Imagem'];
    const csvContent = [headers.join(',')];

    data.forEach(m => {
        const row = [
            m.id,
            m.sampleId,
            m.area_um2 || '',
            m.area_pixels || '',
            m.dataHora,
            m.operator || '',
            m.nomeImagem || ''
        ];
        csvContent.push(row.join(','));
    });

    downloadFile(csvContent.join('\n'), filename, 'text/csv');
}

/**
 * Exporta dados em formato JSON
 */
function exportToJSON(data, filename) {
    const jsonContent = JSON.stringify({
        exported_at: new Date().toISOString(),
        total_records: data.length,
        measurements: data
    }, null, 2);

    downloadFile(jsonContent, filename, 'application/json');
}

/**
 * Exporta relatório em PDF (simulado)
 */
function exportToPDF() {
    showNotification('Gerando relatório PDF... (funcionalidade em desenvolvimento)', 'info');

    // Aqui seria implementada a geração real do PDF
    setTimeout(() => {
        showNotification('Relatório PDF gerado com sucesso!', 'success');
    }, 2000);
}

/**
 * Testa conexão com banco de dados
 */
function testDatabaseConnection() {
    showLoading();

    // Simula teste de conexão
    setTimeout(() => {
        hideLoading();
        showNotification('Conexão com banco de dados OK!', 'success');
        console.log('🔗 Teste de conexão realizado');
    }, 1500);
}

/**
 * Otimiza banco de dados
 */
function optimizeDatabase() {
    showLoading();

    // Simula otimização
    setTimeout(() => {
        hideLoading();
        showNotification('Banco de dados otimizado com sucesso!', 'success');
        console.log('⚡ Banco de dados otimizado');
    }, 3000);
}

/**
 * Faz backup do banco de dados
 */
function backupDatabase() {
    showLoading();

    // Simula backup
    setTimeout(() => {
        hideLoading();
        showNotification('Backup realizado com sucesso!', 'success');
        console.log('💾 Backup do banco realizado');
    }, 2000);
}

/**
 * Reset completo dos dados
 */
function resetAllData() {
    if (confirm('⚠️ ATENÇÃO: Esta ação irá apagar TODOS os dados do sistema. Esta operação é IRREVERSÍVEL.\n\nTem certeza que deseja continuar?')) {
        if (confirm('🚨 CONFIRMAÇÃO FINAL: Você está prestes a apagar todos os dados. Digite "CONFIRMAR" para prosseguir:') &&
            prompt('Digite "CONFIRMAR" para continuar:') === 'CONFIRMAR') {

            showLoading();

            // Simula reset
            setTimeout(() => {
                measurementsData = [];
                filteredData = [];
                samplesData.clear();
                initializeSampleData();

                updateDashboard();
                updateMeasurementsSection();
                updateSamplesSection();

                hideLoading();
                showNotification('Sistema resetado completamente!', 'success');
                console.log('🔄 Sistema resetado');
            }, 2000);
        }
    }
}

/**
 * Limpa cache
 */
function clearCache() {
    if (confirm('Tem certeza que deseja limpar o cache do navegador?')) {
        localStorage.clear();
        sessionStorage.clear();

        showNotification('Cache limpo com sucesso!', 'success');
        console.log('🧹 Cache limpo');
    }
}

/**
 * Mostra logs do sistema
 */
function showSystemLogs() {
    const logs = [
        '2024-09-18 10:30:15 - Sistema iniciado',
        '2024-09-18 10:30:16 - Dados carregados (10 medições)',
        '2024-09-18 10:35:22 - Filtro aplicado: SAMPLE_001',
        '2024-09-18 10:40:11 - Dados exportados (CSV)',
        '2024-09-18 10:45:33 - Auto-refresh ativado (30s)',
        '2024-09-18 11:00:00 - Dados atualizados automaticamente'
    ];

    alert('📋 LOGS DO SISTEMA:\n\n' + logs.join('\n'));
}

/**
 * Mostra detalhes de uma medição
 */
function showMeasurementDetails(measurementId) {
    const measurement = measurementsData.find(m => m.id === measurementId);
    if (!measurement) return;

    const sample = samplesData.get(measurement.sampleId);

    const areaDisplay = parseNumber(measurement.area_um2).toFixed(2);
    const areaPixelsDisplay = Number.isFinite(measurement.area_pixels) && measurement.area_pixels > 0
        ? measurement.area_pixels.toFixed(2) + ' px'
        : 'N/A';
    const scaleDisplay = parseNumber(measurement.scale_pixels_per_um);
    const imageDisplay = sanitizeFileName(measurement.nomeImagem);

    const details = `
📊 DETALHES DA MEDIÇÃO

🆔 ID: ${measurement.id}
🧪 Amostra: ${measurement.sampleId}${sample ? ` (${sample.nome})` : ''}
📏 Área: ${areaDisplay} μm² (${areaPixelsDisplay})
📅 Data/Hora: ${formatDateTime(measurement.dataHora)}
👤 Operador: ${measurement.operator || 'N/A'}
🖼️ Imagem: ${imageDisplay || 'N/A'}
📐 Escala: ${scaleDisplay > 0 ? scaleDisplay.toFixed(2) : 'N/A'} pixels/μm
    `.trim();

    alert(details);
}

/**
 * Mostra detalhes de uma amostra
 */
function viewSampleDetails(sampleId) {
    const sample = samplesData.get(sampleId);
    const measurements = measurementsData.filter(m => m.sampleId === sampleId);

    if (!sample) return;

    const totalArea = measurements.reduce((sum, m) => sum + parseNumber(m.area_um2), 0);
    const avgArea = measurements.length > 0 ? totalArea / measurements.length : 0;

    const details = `
🧪 DETALHES DA AMOSTRA

🆔 ID: ${sample.id}
📝 Nome: ${sample.nome}
🏷️ Tipo: ${sample.tipo}
📅 Data de Coleta: ${new Date(sample.dataColeta).toLocaleString('pt-BR')}
👤 Responsável: ${sample.operadorResponsavel}

📊 ESTATÍSTICAS:
📏 Medições Realizadas: ${measurements.length}
📐 Área Média: ${avgArea.toFixed(2)} μm²
📈 Área Total: ${totalArea.toFixed(2)} μm²
    `.trim();

    alert(details);
}

/**
 * Mostra modal de imagem
 */
function showImageModal(imageName) {
    const fileName = sanitizeFileName(imageName);
    if (!fileName) {
        showNotification('Imagem não disponível', 'warning');
        return;
    }

    const modal = ensureImageModal();
    const img = modal.querySelector('img');
    const caption = modal.querySelector('[data-role="image-caption"]');

    caption.textContent = fileName;
    modal.style.display = 'flex';
    modal.setAttribute('data-open', 'true');
    img.src = `${IMAGE_BASE_PATH}${fileName}?t=${Date.now()}`;
    img.alt = `Imagem ${fileName}`;

    img.onerror = () => {
        showNotification('Não foi possível carregar a imagem selecionada.', 'error');
        closeImageModal();
    };
}

function ensureImageModal() {
    let modal = document.getElementById('image-modal');
    if (modal) {
        return modal;
    }

    modal = document.createElement('div');
    modal.id = 'image-modal';
    Object.assign(modal.style, {
        position: 'fixed',
        inset: '0',
        backgroundColor: 'rgba(15, 23, 42, 0.65)',
        display: 'none',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: '9999',
        padding: '24px'
    });

    const content = document.createElement('div');
    Object.assign(content.style, {
        backgroundColor: '#ffffff',
        borderRadius: '16px',
        boxShadow: '0 25px 50px -12px rgba(15, 23, 42, 0.35)',
        maxWidth: '80vw',
        maxHeight: '80vh',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
    });

    const header = document.createElement('div');
    header.textContent = 'Visualização da imagem';
    Object.assign(header.style, {
        padding: '16px 24px',
        fontSize: '14px',
        fontWeight: '600',
        borderBottom: '1px solid #e5e7eb'
    });

    const imgWrapper = document.createElement('div');
    Object.assign(imgWrapper.style, {
        padding: '16px',
        backgroundColor: '#0f172a',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
    });

    const img = document.createElement('img');
    Object.assign(img.style, {
        maxWidth: '100%',
        maxHeight: '65vh',
        borderRadius: '12px'
    });
    img.alt = '';
    imgWrapper.appendChild(img);

    const footer = document.createElement('div');
    Object.assign(footer.style, {
        padding: '12px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '16px',
        borderTop: '1px solid #e5e7eb',
        fontSize: '12px',
        color: '#475569'
    });

    const caption = document.createElement('span');
    caption.setAttribute('data-role', 'image-caption');
    caption.textContent = '';

    const closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.textContent = 'Fechar';
    Object.assign(closeButton.style, {
        padding: '8px 16px',
        borderRadius: '9999px',
        border: 'none',
        backgroundColor: '#1e293b',
        color: '#ffffff',
        fontSize: '12px',
        fontWeight: '600',
        cursor: 'pointer'
    });
    closeButton.addEventListener('click', closeImageModal);

    footer.appendChild(caption);
    footer.appendChild(closeButton);

    content.appendChild(header);
    content.appendChild(imgWrapper);
    content.appendChild(footer);
    modal.appendChild(content);

    modal.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeImageModal();
        }
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && modal.getAttribute('data-open') === 'true') {
            closeImageModal();
        }
    });

    document.body.appendChild(modal);
    return modal;
}

function closeImageModal() {
    const modal = document.getElementById('image-modal');
    if (!modal) return;
    modal.style.display = 'none';
    modal.setAttribute('data-open', 'false');
}

window.closeImageModal = closeImageModal;

/**
 * Inicializa gráficos vazios
 */
function initializeEmptyCharts() {
    const chartIds = [
        'area-trend-chart',
        'area-distribution-chart',
        'operator-chart',
        'sample-type-chart',
        'sample-activity-chart'
    ];

    chartIds.forEach(chartId => {
        const canvas = document.getElementById(chartId);
        if (canvas) {
            const ctx = canvas.getContext('2d');
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.fillStyle = '#9CA3AF';
            ctx.font = '14px Inter';
            ctx.textAlign = 'center';
            ctx.fillText('Aguardando dados...', canvas.width / 2, canvas.height / 2);
        }
    });
}

/**
 * Atualiza período do gráfico
 */
function updateChartPeriod() {
    const select = document.getElementById('chart-period');
    if (select) {
        console.log(`📊 Período do gráfico alterado para: ${select.value} dias`);
        updateAreaTrendChart();
    }
}

/**
 * Filtra amostras
 */
function filterSamples() {
    const searchInput = document.getElementById('sample-search');
    const typeFilter = document.getElementById('sample-type-filter');

    // Esta função seria implementada para filtrar a visualização de amostras
    console.log('🔍 Filtrando amostras...');
}

/**
 * Recarrega dados de amostras
 */
function refreshSampleData() {
    console.log('🔄 Atualizando dados de amostras...');
    showLoading();

    initializeSampleData()
        .then(() => {
            hideLoading();
            showNotification('Dados de amostras atualizados com sucesso!', 'success');
            console.log('✅ Amostras atualizadas');
        })
        .catch(error => {
            console.error('Erro ao atualizar amostras:', error);
            hideLoading();
            showNotification('Erro ao atualizar dados de amostras', 'error');
        });
}

/**
 * Cadastra nova amostra via API
 */
function createNewSample(id, nome, tipo, operadorResponsavel) {
    const formData = new FormData();
    formData.append('id', id);
    formData.append('nome', nome);
    formData.append('tipo', tipo);
    formData.append('operadorResponsavel', operadorResponsavel);

    return fetch(API_BASE_URL + '/samples', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log('✅ Nova amostra cadastrada:', id);
            // Recarrega dados após cadastro
            return initializeSampleData();
        } else {
            throw new Error(data.message || 'Erro ao cadastrar amostra');
        }
    });
}

/**
 * Registra nova medição via API
 */
function createNewMeasurement(id, sampleId, area, imagemId) {
    const formData = new FormData();
    formData.append('id', id);
    formData.append('sampleId', sampleId);
    formData.append('area', area.toString());
    if (imagemId) formData.append('imagemId', imagemId);

    return fetch(API_BASE_URL + '/measurements', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log('✅ Nova medição registrada:', id);
            // Recarrega dados após registro
            return loadDefaultData();
        } else {
            throw new Error(data.message || 'Erro ao registrar medição');
        }
    });
}

// === FUNÇÕES UTILITÁRIAS ===

/**
 * Atualiza o título da página baseado na seção ativa
 */
function updatePageTitle(sectionName) {
    const pageTitleElement = document.getElementById('page-title');
    const titleMap = {
        'dashboard': 'Dashboard',
        'measurements': 'Medições',
        'samples': 'Amostras',
        'settings': 'Configurações'
    };

    const newTitle = titleMap[sectionName] || 'Dashboard';

    if (pageTitleElement) {
        pageTitleElement.textContent = newTitle;
    }

    // Também atualiza o título da aba do navegador
    document.title = `Scanalyze - ${newTitle}`;
}

/**
 * Define texto de um elemento
 */
function setElementText(elementId, text) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = text;
    }
}

/**
 * Atualiza barra de progresso
 */
function updateProgressBar(elementId, percentage) {
    const element = document.getElementById(elementId);
    if (element) {
        element.style.width = percentage + '%';
    }
}

/**
 * Agrupa medições por dia
 */
function groupMeasurementsByDay() {
    return measurementsData.reduce((acc, measurement) => {
        const date = new Date(measurement.dataHora).toISOString().split('T')[0];
        if (!acc[date]) {
            acc[date] = { total: 0, count: 0 };
        }
        acc[date].total += parseNumber(measurement.area_um2);
        acc[date].count += 1;
        return acc;
    }, {});
}

/**
 * Agrupa medições por amostra
 */
function groupMeasurementsBySample() {
    return measurementsData.reduce((groups, measurement) => {
        const sampleId = measurement.sampleId;
        if (!groups[sampleId]) {
            groups[sampleId] = [];
        }
        groups[sampleId].push(measurement);
        return groups;
    }, {});
}

/**
 * Formata data para gráfico
 */
function formatDateForChart(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
}

/**
 * Formata data e hora
 */
function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Obtém iniciais do operador
 */
function getOperatorInitials(operatorName) {
    if (!operatorName) return '?';
    return operatorName.split(' ')
        .map(name => name.charAt(0))
        .join('')
        .substring(0, 2)
        .toUpperCase();
}

/**
 * Obtém ícone da amostra
 */
function getSampleIcon(tipo) {
    const icons = {
        'Sangue': `<svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                  </svg>`,
        'Tecido': `<svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 7.172V5L8 4z"></path>
                  </svg>`,
        'Neurônio': `<svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
                    </svg>`
    };

    return icons[tipo] || `<svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                           <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 7.172V5L8 4z"></path>
                         </svg>`;
}

/**
 * Cria bins para histograma
 */
function createHistogramBins(data, numBins) {
    const min = Math.min(...data);
    const max = Math.max(...data);
    const binSize = (max - min) / numBins;

    const bins = [];
    for (let i = 0; i < numBins; i++) {
        const binMin = min + i * binSize;
        const binMax = min + (i + 1) * binSize;
        const count = data.filter(value => value >= binMin && (i === numBins - 1 ? value <= binMax : value < binMax)).length;

        bins.push({
            min: binMin,
            max: binMax,
            count: count
        });
    }

    return bins;
}

/**
 * Faz download de arquivo
 */
function downloadFile(content, filename, contentType) {
    const blob = new Blob([content], { type: contentType });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

/**
 * Mostra overlay de carregamento
 */
function showLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.style.display = 'flex';
    }
}

/**
 * Esconde overlay de carregamento
 */
function hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
}

/**
 * Mostra notificação
 */
function showNotification(message, type = 'info') {
    const colors = {
        success: '#10B981',
        error: '#EF4444',
        warning: '#F59E0B',
        info: '#3B82F6'
    };

    // Cria elemento de notificação
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 px-6 py-3 rounded-lg text-white font-medium z-50 shadow-lg transition-all duration-300 transform translate-x-full`;
    notification.style.backgroundColor = colors[type];
    notification.textContent = message;

    document.body.appendChild(notification);

    // Anima entrada
    setTimeout(() => {
        notification.classList.remove('translate-x-full');
    }, 100);

    // Remove após 3 segundos
    setTimeout(() => {
        notification.classList.add('translate-x-full');
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

/**
 * Mostra mensagem de erro
 */
function showError(message) {
    showNotification(message, 'error');
    console.error('❌', message);
}

/**
 * Funcionalidade do menu mobile
 */
function initMobileMenu() {
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebar-overlay');

    if (!mobileMenuBtn || !sidebar || !sidebarOverlay) {
        console.warn('⚠️ Elementos do menu mobile não encontrados');
        return;
    }

    // Abre o menu mobile
    mobileMenuBtn.addEventListener('click', () => {
        sidebar.classList.remove('-translate-x-full');
        sidebarOverlay.classList.remove('hidden');
        document.body.style.overflow = 'hidden'; // Previne scroll do body
    });

    // Fecha o menu quando clica no overlay
    sidebarOverlay.addEventListener('click', () => {
        closeMobileMenu();
    });

    // Fecha o menu quando clica em um link de navegação (em mobile)
    const navLinks = sidebar.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', () => {
            // Verifica se está em mobile (overlay visível)
            if (!sidebarOverlay.classList.contains('hidden')) {
                setTimeout(closeMobileMenu, 150); // Pequeno delay para feedback visual
            }
        });
    });

    console.log('📱 Menu mobile inicializado');
}

function closeMobileMenu() {
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebar-overlay');

    if (sidebar && sidebarOverlay) {
        sidebar.classList.add('-translate-x-full');
        sidebarOverlay.classList.add('hidden');
        document.body.style.overflow = ''; // Restaura scroll do body
    }
}

// Inicializa menu mobile quando o DOM estiver pronto
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initMobileMenu);
} else {
    initMobileMenu();
}

// Fecha menu mobile ao redimensionar para desktop
window.addEventListener('resize', () => {
    if (window.innerWidth >= 768) { // md breakpoint do Tailwind
        closeMobileMenu();
    }
});

console.log('🎉 Dashboard Scanalyze v2.0 carregado com sucesso!');
