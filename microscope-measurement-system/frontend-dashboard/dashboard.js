/**
 * Dashboard JavaScript para Sistema de Micromedição
 * Gerencia visualização de dados, gráficos e interações do usuário
 * 
 * Autor: Sistema de Micromedição
 * Versão: 1.0
 */

// Variáveis globais
let measurementsData = [];
let samplesData = new Map();
let charts = {};
let autoRefreshInterval = null;
let currentSort = { column: null, direction: 'asc' };

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard carregado');
    
    // Carrega dados iniciais
    loadSampleData();
    
    // Configura eventos
    setupEventListeners();
    
    // Tenta carregar dados automaticamente
    tryLoadDefaultData();
    
    // Atualiza timestamp
    updateLastUpdateTime();
});

/**
 * Configura os event listeners necessários
 */
function setupEventListeners() {
    // Auto-refresh
    const autoRefreshCheckbox = document.getElementById('auto-refresh');
    if (autoRefreshCheckbox) {
        autoRefreshCheckbox.addEventListener('change', toggleAutoRefresh);
    }
    
    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 'r') {
            e.preventDefault();
            refreshData();
        }
    });
}

/**
 * Tenta carregar dados padrão do arquivo measurements.json
 */
async function tryLoadDefaultData() {
    showLoading(true);
    
    try {
        // Tenta carregar do diretório de integração
        const response = await fetch('../data-integration/measurements.json');
        if (response.ok) {
            const data = await response.json();
            processMeasurementsData(data);
            console.log('Dados carregados automaticamente');
        } else {
            // Se não conseguir carregar, cria dados de exemplo
            createSampleData();
        }
    } catch (error) {
        console.log('Não foi possível carregar dados automáticos, usando dados de exemplo');
        createSampleData();
    } finally {
        showLoading(false);
    }
}

/**
 * Cria dados de exemplo para demonstração
 */
function createSampleData() {
    const sampleMeasurements = {
        measurements: [
            {
                id: "MEAS_001",
                sampleId: "SAMPLE_001",
                area_pixels: 1250,
                area_um2: 12.50,
                dataHora: "2024-01-15 10:30:00",
                imagemId: "IMG_001",
                nomeImagem: "amostra_001.jpg",
                operator: "Dr. João Silva",
                scale_pixels_per_um: 10.0
            },
            {
                id: "MEAS_002",
                sampleId: "SAMPLE_002",
                area_pixels: 890,
                area_um2: 8.90,
                dataHora: "2024-01-15 11:45:00",
                imagemId: "IMG_002",
                nomeImagem: "amostra_002.jpg",
                operator: "Maria Santos",
                scale_pixels_per_um: 10.0
            },
            {
                id: "MEAS_003",
                sampleId: "SAMPLE_001",
                area_pixels: 1520,
                area_um2: 15.20,
                dataHora: "2024-01-15 14:20:00",
                imagemId: "IMG_003",
                nomeImagem: "amostra_003.jpg",
                operator: "Dr. João Silva",
                scale_pixels_per_um: 10.0
            },
            {
                id: "MEAS_004",
                sampleId: "SAMPLE_003",
                area_pixels: 750,
                area_um2: 7.50,
                dataHora: "2024-01-15 16:10:00",
                imagemId: "IMG_004",
                nomeImagem: "amostra_004.jpg",
                operator: "Carlos Oliveira",
                scale_pixels_per_um: 10.0
            },
            {
                id: "MEAS_005",
                sampleId: "SAMPLE_002",
                area_pixels: 1100,
                area_um2: 11.00,
                dataHora: "2024-01-16 09:15:00",
                imagemId: "IMG_005",
                nomeImagem: "amostra_005.jpg",
                operator: "Maria Santos",
                scale_pixels_per_um: 10.0
            }
        ]
    };
    
    processMeasurementsData(sampleMeasurements);
}

/**
 * Carrega dados de amostras (simulado)
 */
function loadSampleData() {
    samplesData.set("SAMPLE_001", {
        id: "SAMPLE_001",
        nome: "Sangue Paciente A",
        tipo: "Sangue",
        dataColeta: "2024-01-15 08:00:00",
        operadorResponsavel: "Dr. João Silva"
    });
    
    samplesData.set("SAMPLE_002", {
        id: "SAMPLE_002",
        nome: "Tecido Muscular",
        tipo: "Tecido",
        dataColeta: "2024-01-15 09:30:00",
        operadorResponsavel: "Maria Santos"
    });
    
    samplesData.set("SAMPLE_003", {
        id: "SAMPLE_003",
        nome: "Célula Neural",
        tipo: "Neurônio",
        dataColeta: "2024-01-15 15:00:00",
        operadorResponsavel: "Carlos Oliveira"
    });
}

/**
 * Processa os dados de medições carregados
 */
function processMeasurementsData(data) {
    if (data && data.measurements && Array.isArray(data.measurements)) {
        measurementsData = data.measurements;
        
        // Atualiza todas as visualizações
        updateOverviewStats();
        updateRecentMeasurementsTable();
        updateAllMeasurementsTable();
        updateSamplesSection();
        updateCharts();
        updateFilters();
        
        console.log(`${measurementsData.length} medições carregadas`);
    } else {
        console.error('Formato de dados inválido');
        showError('Formato de dados inválido');
    }
}

/**
 * Atualiza as estatísticas da visão geral
 */
function updateOverviewStats() {
    if (!measurementsData.length) return;
    
    // Total de medições
    document.getElementById('total-measurements').textContent = measurementsData.length;
    
    // Total de amostras únicas
    const uniqueSamples = new Set(measurementsData.map(m => m.sampleId));
    document.getElementById('total-samples').textContent = uniqueSamples.size;
    
    // Área média
    const totalArea = measurementsData.reduce((sum, m) => sum + (m.area_um2 || 0), 0);
    const avgArea = totalArea / measurementsData.length;
    document.getElementById('average-area').textContent = `${avgArea.toFixed(2)} μm²`;
    
    // Última medição
    const sortedByDate = [...measurementsData].sort((a, b) => 
        new Date(b.dataHora) - new Date(a.dataHora)
    );
    if (sortedByDate.length > 0) {
        const lastMeasurement = new Date(sortedByDate[0].dataHora);
        document.getElementById('last-measurement').textContent = 
            lastMeasurement.toLocaleString('pt-BR');
    }
}

/**
 * Atualiza a tabela de medições recentes
 */
function updateRecentMeasurementsTable() {
    const tbody = document.getElementById('recent-measurements');
    if (!tbody) return;
    
    // Pega as 5 medições mais recentes
    const recentMeasurements = [...measurementsData]
        .sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora))
        .slice(0, 5);
    
    tbody.innerHTML = recentMeasurements.map(measurement => `
        <tr>
            <td>${measurement.id}</td>
            <td>${measurement.sampleId}</td>
            <td>${measurement.area_um2?.toFixed(2) || 'N/A'} μm²</td>
            <td>${new Date(measurement.dataHora).toLocaleString('pt-BR')}</td>
            <td>${measurement.operator || 'N/A'}</td>
        </tr>
    `).join('');
}

/**
 * Atualiza a tabela de todas as medições
 */
function updateAllMeasurementsTable() {
    const tbody = document.getElementById('all-measurements');
    if (!tbody) return;
    
    let filteredData = applyCurrentFilters();
    
    tbody.innerHTML = filteredData.map(measurement => `
        <tr>
            <td>${measurement.id}</td>
            <td>${measurement.sampleId}</td>
            <td>${measurement.area_um2?.toFixed(2) || 'N/A'} μm²</td>
            <td>${measurement.area_pixels || 'N/A'} px</td>
            <td>${new Date(measurement.dataHora).toLocaleString('pt-BR')}</td>
            <td>${measurement.operator || 'N/A'}</td>
            <td>${measurement.nomeImagem ? 
                `<a href="#" onclick="showImageModal('${measurement.nomeImagem}')">${measurement.nomeImagem}</a>` : 
                'N/A'}</td>
        </tr>
    `).join('');
}

/**
 * Atualiza a seção de amostras
 */
function updateSamplesSection() {
    const container = document.getElementById('samples-grid');
    if (!container) return;
    
    // Agrupa medições por amostra
    const sampleGroups = measurementsData.reduce((groups, measurement) => {
        const sampleId = measurement.sampleId;
        if (!groups[sampleId]) {
            groups[sampleId] = [];
        }
        groups[sampleId].push(measurement);
        return groups;
    }, {});
    
    container.innerHTML = Object.entries(sampleGroups).map(([sampleId, measurements]) => {
        const sampleInfo = samplesData.get(sampleId) || { 
            nome: sampleId, 
            tipo: 'Desconhecido',
            operadorResponsavel: 'N/A'
        };
        
        const totalArea = measurements.reduce((sum, m) => sum + (m.area_um2 || 0), 0);
        const avgArea = totalArea / measurements.length;
        
        return `
            <div class="sample-card">
                <h3>🧪 ${sampleInfo.nome}</h3>
                <div class="sample-info">
                    <span><strong>ID:</strong></span>
                    <span>${sampleId}</span>
                </div>
                <div class="sample-info">
                    <span><strong>Tipo:</strong></span>
                    <span>${sampleInfo.tipo}</span>
                </div>
                <div class="sample-info">
                    <span><strong>Medições:</strong></span>
                    <span>${measurements.length}</span>
                </div>
                <div class="sample-info">
                    <span><strong>Área Média:</strong></span>
                    <span>${avgArea.toFixed(2)} μm²</span>
                </div>
                <div class="sample-info">
                    <span><strong>Responsável:</strong></span>
                    <span>${sampleInfo.operadorResponsavel}</span>
                </div>
            </div>
        `;
    }).join('');
}

/**
 * Atualiza os gráficos
 */
function updateCharts() {
    if (!measurementsData.length) return;
    
    updateAreaDistributionChart();
    updateTimelineChart();
    updateOperatorChart();
    updateAreaTrendChart();
}

/**
 * Atualiza o gráfico de distribuição de áreas
 */
function updateAreaDistributionChart() {
    const ctx = document.getElementById('area-distribution-chart');
    if (!ctx) return;
    
    // Destroi gráfico anterior se existir
    if (charts.areaDistribution) {
        charts.areaDistribution.destroy();
    }
    
    // Cria intervalos de área
    const areas = measurementsData.map(m => m.area_um2 || 0);
    const minArea = Math.min(...areas);
    const maxArea = Math.max(...areas);
    const interval = (maxArea - minArea) / 5;
    
    const bins = [
        { label: `${minArea.toFixed(1)}-${(minArea + interval).toFixed(1)}`, count: 0 },
        { label: `${(minArea + interval).toFixed(1)}-${(minArea + 2*interval).toFixed(1)}`, count: 0 },
        { label: `${(minArea + 2*interval).toFixed(1)}-${(minArea + 3*interval).toFixed(1)}`, count: 0 },
        { label: `${(minArea + 3*interval).toFixed(1)}-${(minArea + 4*interval).toFixed(1)}`, count: 0 },
        { label: `${(minArea + 4*interval).toFixed(1)}-${maxArea.toFixed(1)}`, count: 0 }
    ];
    
    areas.forEach(area => {
        const binIndex = Math.min(Math.floor((area - minArea) / interval), 4);
        bins[binIndex].count++;
    });
    
    charts.areaDistribution = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: bins.map(b => b.label + ' μm²'),
            datasets: [{
                label: 'Número de Medições',
                data: bins.map(b => b.count),
                backgroundColor: 'rgba(52, 152, 219, 0.7)',
                borderColor: 'rgba(52, 152, 219, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
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
 * Atualiza o gráfico de timeline
 */
function updateTimelineChart() {
    const ctx = document.getElementById('measurements-timeline-chart');
    if (!ctx) return;
    
    if (charts.timeline) {
        charts.timeline.destroy();
    }
    
    // Agrupa medições por dia
    const dailyData = measurementsData.reduce((acc, measurement) => {
        const date = new Date(measurement.dataHora).toDateString();
        acc[date] = (acc[date] || 0) + 1;
        return acc;
    }, {});
    
    const sortedDates = Object.keys(dailyData).sort((a, b) => new Date(a) - new Date(b));
    
    charts.timeline = new Chart(ctx, {
        type: 'line',
        data: {
            labels: sortedDates.map(date => new Date(date).toLocaleDateString('pt-BR')),
            datasets: [{
                label: 'Medições por Dia',
                data: sortedDates.map(date => dailyData[date]),
                borderColor: 'rgba(231, 76, 60, 1)',
                backgroundColor: 'rgba(231, 76, 60, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
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
 * Atualiza o gráfico de medições por operador
 */
function updateOperatorChart() {
    const ctx = document.getElementById('operator-chart');
    if (!ctx) return;
    
    if (charts.operator) {
        charts.operator.destroy();
    }
    
    // Conta medições por operador
    const operatorData = measurementsData.reduce((acc, measurement) => {
        const operator = measurement.operator || 'Desconhecido';
        acc[operator] = (acc[operator] || 0) + 1;
        return acc;
    }, {});
    
    const colors = [
        'rgba(52, 152, 219, 0.7)',
        'rgba(46, 204, 113, 0.7)',
        'rgba(155, 89, 182, 0.7)',
        'rgba(241, 196, 15, 0.7)',
        'rgba(231, 76, 60, 0.7)'
    ];
    
    charts.operator = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(operatorData),
            datasets: [{
                data: Object.values(operatorData),
                backgroundColor: colors.slice(0, Object.keys(operatorData).length),
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

/**
 * Atualiza o gráfico de tendência de áreas
 */
function updateAreaTrendChart() {
    const ctx = document.getElementById('area-trend-chart');
    if (!ctx) return;
    
    if (charts.areaTrend) {
        charts.areaTrend.destroy();
    }
    
    // Ordena medições por data
    const sortedMeasurements = [...measurementsData].sort((a, b) => 
        new Date(a.dataHora) - new Date(b.dataHora)
    );
    
    charts.areaTrend = new Chart(ctx, {
        type: 'scatter',
        data: {
            datasets: [{
                label: 'Área das Medições',
                data: sortedMeasurements.map((m, index) => ({
                    x: index + 1,
                    y: m.area_um2 || 0
                })),
                backgroundColor: 'rgba(52, 152, 219, 0.6)',
                borderColor: 'rgba(52, 152, 219, 1)',
                showLine: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Sequência de Medições'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Área (μm²)'
                    },
                    beginAtZero: true
                }
            }
        }
    });
}

/**
 * Atualiza os filtros com dados disponíveis
 */
function updateFilters() {
    // Atualiza filtro de amostras
    const sampleFilter = document.getElementById('sample-filter');
    if (sampleFilter) {
        const uniqueSamples = [...new Set(measurementsData.map(m => m.sampleId))];
        sampleFilter.innerHTML = '<option value="">Todas as amostras</option>' +
            uniqueSamples.map(sample => `<option value="${sample}">${sample}</option>`).join('');
    }
    
    // Atualiza filtro de operadores
    const operatorFilter = document.getElementById('operator-filter');
    if (operatorFilter) {
        const uniqueOperators = [...new Set(measurementsData.map(m => m.operator || 'Desconhecido'))];
        operatorFilter.innerHTML = '<option value="">Todos os operadores</option>' +
            uniqueOperators.map(op => `<option value="${op}">${op}</option>`).join('');
    }
}

/**
 * Aplica os filtros atuais aos dados
 */
function applyCurrentFilters() {
    let filteredData = [...measurementsData];
    
    // Filtro de data
    const dateFilter = document.getElementById('date-filter');
    if (dateFilter && dateFilter.value) {
        const filterDate = new Date(dateFilter.value);
        filteredData = filteredData.filter(m => {
            const measurementDate = new Date(m.dataHora);
            return measurementDate.toDateString() === filterDate.toDateString();
        });
    }
    
    // Filtro de amostra
    const sampleFilter = document.getElementById('sample-filter');
    if (sampleFilter && sampleFilter.value) {
        filteredData = filteredData.filter(m => m.sampleId === sampleFilter.value);
    }
    
    // Filtro de operador
    const operatorFilter = document.getElementById('operator-filter');
    if (operatorFilter && operatorFilter.value) {
        filteredData = filteredData.filter(m => (m.operator || 'Desconhecido') === operatorFilter.value);
    }
    
    return filteredData;
}

/**
 * Aplica filtros e atualiza visualizações
 */
function applyFilters() {
    updateAllMeasurementsTable();
}

/**
 * Limpa todos os filtros
 */
function clearFilters() {
    document.getElementById('date-filter').value = '';
    document.getElementById('sample-filter').value = '';
    document.getElementById('operator-filter').value = '';
    applyFilters();
}

/**
 * Ordena a tabela por coluna
 */
function sortTable(column) {
    // Alterna direção se for a mesma coluna
    if (currentSort.column === column) {
        currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
        currentSort.column = column;
        currentSort.direction = 'asc';
    }
    
    measurementsData.sort((a, b) => {
        let valueA = a[column];
        let valueB = b[column];
        
        // Trata valores de data
        if (column === 'dataHora') {
            valueA = new Date(valueA);
            valueB = new Date(valueB);
        }
        
        // Trata valores numéricos
        if (typeof valueA === 'string' && !isNaN(parseFloat(valueA))) {
            valueA = parseFloat(valueA);
            valueB = parseFloat(valueB);
        }
        
        if (currentSort.direction === 'asc') {
            return valueA > valueB ? 1 : -1;
        } else {
            return valueA < valueB ? 1 : -1;
        }
    });
    
    updateAllMeasurementsTable();
}

/**
 * Mostra uma seção específica
 */
function showSection(sectionName) {
    // Remove classe active de todas as seções
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Remove classe active de todos os botões de navegação
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Ativa a seção selecionada
    const section = document.getElementById(sectionName);
    if (section) {
        section.classList.add('active');
    }
    
    // Ativa o botão correspondente
    event.target.classList.add('active');
    
    // Atualiza gráficos se necessário
    if (sectionName === 'charts') {
        setTimeout(updateCharts, 100);
    }
}

/**
 * Carrega arquivo de dados
 */
function loadDataFile() {
    const fileInput = document.getElementById('file-input');
    const file = fileInput.files[0];
    
    if (!file) return;
    
    showLoading(true);
    
    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const data = JSON.parse(e.target.result);
            processMeasurementsData(data);
            showSuccess('Dados carregados com sucesso!');
        } catch (error) {
            showError('Erro ao processar arquivo: ' + error.message);
        } finally {
            showLoading(false);
        }
    };
    
    reader.readAsText(file);
}

/**
 * Toggle do auto-refresh
 */
function toggleAutoRefresh() {
    const checkbox = document.getElementById('auto-refresh');
    const interval = document.getElementById('refresh-interval').value;
    
    if (checkbox.checked) {
        autoRefreshInterval = setInterval(() => {
            tryLoadDefaultData();
        }, parseInt(interval) * 1000);
        showSuccess('Auto-refresh ativado');
    } else {
        if (autoRefreshInterval) {
            clearInterval(autoRefreshInterval);
            autoRefreshInterval = null;
        }
        showSuccess('Auto-refresh desativado');
    }
}

/**
 * Exporta dados
 */
function exportData(format) {
    if (!measurementsData.length) {
        showError('Nenhum dado para exportar');
        return;
    }
    
    let content = '';
    let filename = '';
    let mimeType = '';
    
    if (format === 'csv') {
        content = convertToCSV(measurementsData);
        filename = 'medicoes_' + new Date().toISOString().split('T')[0] + '.csv';
        mimeType = 'text/csv';
    } else if (format === 'json') {
        content = JSON.stringify({ measurements: measurementsData }, null, 2);
        filename = 'medicoes_' + new Date().toISOString().split('T')[0] + '.json';
        mimeType = 'application/json';
    }
    
    downloadFile(content, filename, mimeType);
    showSuccess(`Dados exportados como ${format.toUpperCase()}`);
}

/**
 * Converte dados para CSV
 */
function convertToCSV(data) {
    const headers = ['id', 'sampleId', 'area_pixels', 'area_um2', 'dataHora', 'imagemId', 'nomeImagem', 'operator'];
    const csvRows = [headers.join(',')];
    
    data.forEach(row => {
        const values = headers.map(header => {
            const value = row[header] || '';
            return `"${value.toString().replace(/"/g, '""')}"`;
        });
        csvRows.push(values.join(','));
    });
    
    return csvRows.join('\n');
}

/**
 * Faz download de arquivo
 */
function downloadFile(content, filename, mimeType) {
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

/**
 * Limpa cache
 */
function clearCache() {
    if (confirm('Tem certeza que deseja limpar todos os dados armazenados?')) {
        measurementsData = [];
        samplesData.clear();
        
        // Destroi gráficos
        Object.values(charts).forEach(chart => {
            if (chart) chart.destroy();
        });
        charts = {};
        
        // Limpa visualizações
        document.getElementById('total-measurements').textContent = '0';
        document.getElementById('total-samples').textContent = '0';
        document.getElementById('average-area').textContent = '0.00 μm²';
        document.getElementById('last-measurement').textContent = 'Nunca';
        
        document.getElementById('recent-measurements').innerHTML = '';
        document.getElementById('all-measurements').innerHTML = '';
        document.getElementById('samples-grid').innerHTML = '';
        
        showSuccess('Cache limpo com sucesso');
    }
}

/**
 * Atualiza o timestamp da última atualização
 */
function updateLastUpdateTime() {
    document.getElementById('last-update').textContent = new Date().toLocaleString('pt-BR');
}

/**
 * Atualiza dados manualmente
 */
function refreshData() {
    showLoading(true);
    tryLoadDefaultData().then(() => {
        updateLastUpdateTime();
        showSuccess('Dados atualizados');
    });
}

/**
 * Mostra/esconde loading
 */
function showLoading(show) {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.classList.toggle('hidden', !show);
    }
}

/**
 * Mostra mensagem de sucesso
 */
function showSuccess(message) {
    // Implementação simples - pode ser substituída por um sistema de notificações mais sofisticado
    console.log('SUCCESS:', message);
    // Aqui você pode adicionar um toast/notification
}

/**
 * Mostra mensagem de erro
 */
function showError(message) {
    console.error('ERROR:', message);
    alert('Erro: ' + message);
}

/**
 * Mostra modal de imagem (placeholder)
 */
function showImageModal(imageName) {
    alert('Visualizar imagem: ' + imageName + '\n\n(Funcionalidade a ser implementada)');
}

// Exporta funções para uso global
window.showSection = showSection;
window.applyFilters = applyFilters;
window.clearFilters = clearFilters;
window.sortTable = sortTable;
window.loadDataFile = loadDataFile;
window.toggleAutoRefresh = toggleAutoRefresh;
window.exportData = exportData;
window.clearCache = clearCache;
window.showImageModal = showImageModal;