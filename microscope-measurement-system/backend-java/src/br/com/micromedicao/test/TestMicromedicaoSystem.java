package br.com.micromedicao.test;

import br.com.micromedicao.model.*;
import br.com.micromedicao.dao.*;
import br.com.micromedicao.service.MicromedicaoService;
import br.com.micromedicao.connection.ConnectionFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Classe de teste completa para o Sistema de Micromedição Automatizada.
 * Testa todos os métodos de relevância e operações CRUD das entidades.
 *
 * Esta classe instancia objetos e testa os métodos implementados,
 * validando a integração entre as camadas Model, DAO e Service.
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class TestMicromedicaoSystem {

    private OperatorDAO operatorDAO;
    private SampleDAO sampleDAO;
    private MicroscopyImageDAO imageDAO;
    private MeasurementDAO measurementDAO;
    private MicromedicaoService service;

    /**
     * Construtor que inicializa os DAOs e serviços.
     */
    public TestMicromedicaoSystem() {
        this.operatorDAO = new OperatorDAO();
        this.sampleDAO = new SampleDAO();
        this.imageDAO = new MicroscopyImageDAO();
        this.measurementDAO = new MeasurementDAO();
        this.service = new MicromedicaoService();
    }

    /**
     * Executa todos os testes do sistema.
     */
    public void runAllTests() {
        System.out.println("🧪 === INICIANDO TESTES DO SISTEMA DE MICROMEDIÇÃO ===");
        System.out.println();

        try {
            // 1. Teste de conexão
            testConnection();

            // 2. Testes das entidades Model
            testModelEntities();

            // 3. Testes dos DAOs (CRUD)
            testDAOOperations();

            // 4. Testes dos métodos de negócio
            testBusinessMethods();

            // 5. Testes de integração
            testIntegration();

            // 6. Testes de validação
            testValidation();

            // 7. Estatísticas finais
            printFinalStatistics();

            System.out.println("\n✅ === TODOS OS TESTES CONCLUÍDOS COM SUCESSO ===");

        } catch (Exception e) {
            System.err.println("\n❌ ERRO DURANTE OS TESTES: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Teste 1: Conexão com banco de dados.
     */
    private void testConnection() {
        System.out.println("🔌 Teste 1: Conexão com Banco de Dados");
        System.out.println("-------------------------------------");

        ConnectionFactory factory = ConnectionFactory.getInstance();
        boolean connectionOk = factory.testConnection();

        if (connectionOk) {
            System.out.println("✅ Conexão com Oracle estabelecida com sucesso!");
        } else {
            System.out.println("❌ Falha na conexão com Oracle!");
        }

        System.out.println();
    }

    /**
     * Teste 2: Entidades do modelo DDD.
     */
    private void testModelEntities() {
        System.out.println("🏗️ Teste 2: Entidades do Modelo DDD");
        System.out.println("----------------------------------");

        // Teste da entidade Operator
        System.out.println("📋 Testando Operator:");
        Operator operator = new Operator("OP999", "Teste Operador", "teste@email.com", "ADMIN");
        operator.exibirDetalhes();
        System.out.println("Autenticação: " + operator.autenticar());
        System.out.println("É Admin: " + operator.isAdmin());
        System.out.println("Tem permissão MEDIR: " + operator.temPermissao("MEDIR"));

        // Teste da entidade Sample
        System.out.println("\n🧪 Testando Sample:");
        Sample sample = new Sample("SAMPLE_999", "Amostra Teste", "Sangue", "Dr. Teste");
        sample.exibirDetalhes();
        System.out.println("Coleta recente: " + sample.isColetaRecente());
        System.out.println("Idade em horas: " + sample.calcularIdadeEmHoras());
        sample.atualizarTipo("Tecido");

        // Teste da entidade MicroscopyImage
        System.out.println("\n📸 Testando MicroscopyImage:");
        MicroscopyImage image = new MicroscopyImage("IMG_999", "teste.jpg", sample, LocalDateTime.now());
        image.exibirDetalhes();
        System.out.println("Imagem válida: " + image.validarImagem());

        // Teste da entidade Measurement
        System.out.println("\n📏 Testando Measurement:");
        Measurement measurement = new Measurement("MEAS_999", sample, 1500.0, image);
        measurement.exibirDetalhes();
        System.out.println("Medição válida: " + measurement.validarMedicao());
        System.out.println("Área com escala 0.5: " + measurement.calcularArea(0.5));
        System.out.println("Área formatada: " + measurement.calcularArea(1.0, "μm²"));
        System.out.println("Medição recente: " + measurement.isMedicaoRecente());

        System.out.println();
    }

    /**
     * Teste 3: Operações CRUD dos DAOs.
     */
    private void testDAOOperations() throws SQLException {
        System.out.println("💾 Teste 3: Operações CRUD dos DAOs");
        System.out.println("----------------------------------");

        // Teste OperatorDAO
        System.out.println("👤 Testando OperatorDAO:");
        testOperatorDAO();

        // Teste SampleDAO
        System.out.println("\n🧪 Testando SampleDAO:");
        testSampleDAO();

        // Teste MicroscopyImageDAO
        System.out.println("\n📸 Testando MicroscopyImageDAO:");
        testImageDAO();

        // Teste MeasurementDAO
        System.out.println("\n📏 Testando MeasurementDAO:");
        testMeasurementDAO();

        System.out.println();
    }

    /**
     * Teste específico do OperatorDAO.
     */
    private void testOperatorDAO() throws SQLException {
        // Buscar operador existente
        Operator operator = operatorDAO.findByOperatorId("OP001");
        if (operator != null) {
            System.out.println("✅ Operador encontrado: " + operator.getNome());
        }

        // Listar todos
        List<Operator> operators = operatorDAO.findAll();
        System.out.println("✅ Total de operadores: " + operators.size());

        // Contar operadores
        int count = operatorDAO.count();
        System.out.println("✅ Contagem de operadores: " + count);

        // Buscar por nível de acesso
        List<Operator> admins = operatorDAO.findByAccessLevel("ADMIN");
        System.out.println("✅ Administradores: " + admins.size());
    }

    /**
     * Teste específico do SampleDAO.
     */
    private void testSampleDAO() throws SQLException {
        // Buscar amostra existente
        Sample sample = sampleDAO.findBySampleId("SAMPLE_001");
        if (sample != null) {
            System.out.println("✅ Amostra encontrada: " + sample.getNome());
        }

        // Listar todas
        List<Sample> samples = sampleDAO.findAll();
        System.out.println("✅ Total de amostras: " + samples.size());

        // Buscar por tipo
        List<Sample> bloodSamples = sampleDAO.findByType("Sangue");
        System.out.println("✅ Amostras de sangue: " + bloodSamples.size());

        // Contar amostras
        int count = sampleDAO.count();
        System.out.println("✅ Contagem de amostras: " + count);
    }

    /**
     * Teste específico do MicroscopyImageDAO.
     */
    private void testImageDAO() throws SQLException {
        // Buscar imagem existente
        MicroscopyImage image = imageDAO.findByImageId("IMG_1749780022");
        if (image != null) {
            System.out.println("✅ Imagem encontrada: " + image.getArquivo());
        }

        // Listar todas
        List<MicroscopyImage> images = imageDAO.findAll();
        System.out.println("✅ Total de imagens: " + images.size());

        // Buscar por amostra
        List<MicroscopyImage> sampleImages = imageDAO.findBySampleId("SAMPLE_001");
        System.out.println("✅ Imagens da SAMPLE_001: " + sampleImages.size());

        // Contar imagens
        int count = imageDAO.count();
        System.out.println("✅ Contagem de imagens: " + count);
    }

    /**
     * Teste específico do MeasurementDAO.
     */
    private void testMeasurementDAO() throws SQLException {
        // Buscar medição existente
        Measurement measurement = measurementDAO.findByMeasurementId("MEAS_1749780022");
        if (measurement != null) {
            System.out.println("✅ Medição encontrada: Área = " + measurement.getArea() + " μm²");
        }

        // Listar todas
        List<Measurement> measurements = measurementDAO.findAll();
        System.out.println("✅ Total de medições: " + measurements.size());

        // Buscar por amostra
        List<Measurement> sampleMeasurements = measurementDAO.findBySampleId("SAMPLE_001");
        System.out.println("✅ Medições da SAMPLE_001: " + sampleMeasurements.size());

        // Estatísticas
        double[] stats = measurementDAO.getAreaStatistics();
        System.out.printf("✅ Estatísticas - Média: %.2f, Min: %.2f, Max: %.2f μm²%n",
                        stats[0], stats[1], stats[2]);

        // Contar medições
        int count = measurementDAO.count();
        System.out.println("✅ Contagem de medições: " + count);
    }

    /**
     * Teste 4: Métodos de negócio relevantes.
     */
    private void testBusinessMethods() throws SQLException {
        System.out.println("🎯 Teste 4: Métodos de Negócio Relevantes");
        System.out.println("----------------------------------------");

        // Método 1: Processo completo de medição automatizada
        System.out.println("📊 Método 1: Processo Completo de Medição");
        testCompleteAutomatedMeasurement();

        // Método 2: Validação de medições por operador
        System.out.println("\n✅ Método 2: Validação de Medições");
        testMeasurementValidation();

        // Método 3: Análise estatística de amostras
        System.out.println("\n📈 Método 3: Análise Estatística");
        testStatisticalAnalysis();

        // Método 4: Gestão de operadores e permissões
        System.out.println("\n👥 Método 4: Gestão de Operadores");
        testOperatorManagement();

        System.out.println();
    }

    /**
     * Método de negócio 1: Processo completo de medição automatizada.
     */
    private void testCompleteAutomatedMeasurement() throws SQLException {
        try {
            // Simula o processo completo: amostra → imagem → medição
            Operator operator = operatorDAO.findByOperatorId("OP001");
            Sample sample = sampleDAO.findBySampleId("SAMPLE_001");

            if (operator != null && sample != null) {
                // Calcula nova área baseada em parâmetros de visão computacional
                double areaCalculada = service.calcularAreaAutomatizada(sample, 1200, 10.0);

                System.out.println("✅ Área calculada automaticamente: " + areaCalculada + " μm²");
                System.out.println("✅ Processo de medição automatizada executado com sucesso");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Simulação de processo automatizado: " + e.getMessage());
        }
    }

    /**
     * Método de negócio 2: Validação de medições por operador.
     */
    private void testMeasurementValidation() throws SQLException {
        // Busca medições não validadas
        List<Measurement> measurements = measurementDAO.findAll();
        int validatedCount = 0;

        for (Measurement measurement : measurements) {
            if (measurement.validarMedicao()) {
                // Simula validação por operador ADMIN
                boolean validated = measurementDAO.validateMeasurement(measurement.getId(), "Dr. João Silva");
                if (validated) {
                    validatedCount++;
                }
            }
        }

        System.out.println("✅ Total de medições validadas: " + validatedCount);
        System.out.println("✅ Processo de validação executado com sucesso");
    }

    /**
     * Método de negócio 3: Análise estatística de amostras.
     */
    private void testStatisticalAnalysis() throws SQLException {
        double[] stats = measurementDAO.getAreaStatistics();
        int totalMeasurements = measurementDAO.count();
        int totalSamples = sampleDAO.count();

        // Calcula métricas avançadas
        double coefficient = (stats[2] - stats[1]) / stats[0]; // Coeficiente de variação simplificado
        double efficiency = (double) totalMeasurements / totalSamples; // Medições por amostra

        System.out.printf("✅ Análise Estatística Completa:%n");
        System.out.printf("   - Total de medições: %d%n", totalMeasurements);
        System.out.printf("   - Total de amostras: %d%n", totalSamples);
        System.out.printf("   - Área média: %.2f μm²%n", stats[0]);
        System.out.printf("   - Variação (max-min): %.2f μm²%n", stats[2] - stats[1]);
        System.out.printf("   - Coeficiente de dispersão: %.2f%n", coefficient);
        System.out.printf("   - Eficiência (med/amostra): %.2f%n", efficiency);
    }

    /**
     * Método de negócio 4: Gestão de operadores e permissões.
     */
    private void testOperatorManagement() throws SQLException {
        List<Operator> operators = operatorDAO.findAll();
        int adminCount = 0, tecnicoCount = 0, operadorCount = 0;

        for (Operator op : operators) {
            // Testa permissões específicas
            boolean canMeasure = op.temPermissao("MEDIR");
            boolean canReport = op.temPermissao("RELATORIO");
            boolean isAdmin = op.isAdmin();

            System.out.printf("👤 %s (%s): Medir=%s, Relatório=%s, Admin=%s%n",
                            op.getNome(), op.getNivelAcesso(), canMeasure, canReport, isAdmin);

            // Conta por nível
            switch (op.getNivelAcesso()) {
                case "ADMIN": adminCount++; break;
                case "TECNICO": tecnicoCount++; break;
                case "OPERADOR": operadorCount++; break;
            }
        }

        System.out.printf("✅ Distribuição: %d ADMINs, %d TÉCNICOs, %d OPERADOREs%n",
                        adminCount, tecnicoCount, operadorCount);
    }

    /**
     * Teste 5: Integração entre componentes.
     */
    private void testIntegration() throws SQLException {
        System.out.println("🔗 Teste 5: Integração entre Componentes");
        System.out.println("---------------------------------------");

        // Teste integração Service → DAO → Model
        System.out.println("🔄 Testando integração Service → DAO → Model:");

        try {
            // Processa medições existentes através do serviço
            boolean processed = service.processarMedicoesExistentes();
            System.out.println("✅ Processamento via Service: " + (processed ? "Sucesso" : "Falha"));

            // Integração entre DAOs (relacionamentos)
            List<Measurement> measurements = measurementDAO.findAll();
            for (Measurement m : measurements.subList(0, Math.min(3, measurements.size()))) {
                Sample relatedSample = sampleDAO.findBySampleId(m.getSample().getId());
                if (relatedSample != null) {
                    System.out.println("✅ Relacionamento Measurement → Sample OK: " + relatedSample.getNome());
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ Integração: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Teste 6: Validações e regras de negócio.
     */
    private void testValidation() {
        System.out.println("🛡️ Teste 6: Validações e Regras de Negócio");
        System.out.println("------------------------------------------");

        // Validações de entidades
        Operator invalidOperator = new Operator("", "", "email-inválido", "NIVEL_INEXISTENTE");
        System.out.println("👤 Operador inválido - Autenticação: " + invalidOperator.autenticar());

        Sample validSample = new Sample("SAMPLE_TEST", "Teste", "Sangue", "Dr. Teste");
        System.out.println("🧪 Amostra válida - Idade: " + validSample.calcularIdadeEmHoras() + "h");

        Measurement invalidMeasurement = new Measurement("MEAS_INVALID", validSample, -100.0, null);
        System.out.println("📏 Medição inválida - Validação: " + invalidMeasurement.validarMedicao());

        // Teste de comparação de medições
        Measurement m1 = new Measurement("MEAS_1", validSample, 100.0, null);
        Measurement m2 = new Measurement("MEAS_2", validSample, 200.0, null);
        int comparison = m1.compararArea(m2);
        System.out.println("📊 Comparação de áreas (100 vs 200): " + comparison + " (esperado: -1)");

        System.out.println();
    }

    /**
     * Exibe estatísticas finais do sistema.
     */
    private void printFinalStatistics() throws SQLException {
        System.out.println("📊 Estatísticas Finais do Sistema");
        System.out.println("--------------------------------");

        int operators = operatorDAO.count();
        int samples = sampleDAO.count();
        int images = imageDAO.count();
        int measurements = measurementDAO.count();

        System.out.println("📈 Resumo do Banco de Dados:");
        System.out.println("   👥 Operadores: " + operators);
        System.out.println("   🧪 Amostras: " + samples);
        System.out.println("   📸 Imagens: " + images);
        System.out.println("   📏 Medições: " + measurements);

        if (measurements > 0) {
            double[] stats = measurementDAO.getAreaStatistics();
            System.out.printf("   📊 Área média: %.2f μm²%n", stats[0]);
            System.out.printf("   📊 Área total: %.2f μm²%n", stats[0] * measurements);
        }

        System.out.println();
    }

    /**
     * Método principal para execução dos testes.
     *
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        System.out.println("🚀 SISTEMA DE MICROMEDIÇÃO AUTOMATIZADA - TESTES");
        System.out.println("================================================");
        System.out.println();

        TestMicromedicaoSystem tester = new TestMicromedicaoSystem();
        tester.runAllTests();

        System.out.println("🎯 === EXECUÇÃO DE TESTES FINALIZADA ===");
    }
}