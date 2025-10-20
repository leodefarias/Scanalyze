package br.com.micromedicao.api;

import br.com.micromedicao.model.*;
import br.com.micromedicao.service.MicromedicaoService;
import br.com.micromedicao.integration.DataIntegration;

import java.util.*;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador REST para API do Sistema de Micromedição
 * Fornece endpoints para integração com frontend e sistemas externos
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class RestApiController {

    private MicromedicaoService service;
    private DataIntegration integration;
    private Map<String, Object> responses;

    /**
     * Construtor do controlador REST
     */
    public RestApiController() {
        this.service = new MicromedicaoService();
        this.integration = new DataIntegration(service);
        this.responses = new HashMap<>();

        // Inicializa com dados de exemplo
        initializeDefaultData();
    }

    /**
     * Inicializa o sistema carregando dados existentes do banco ou criando dados padrão se necessário
     */
    private void initializeDefaultData() {
        System.out.println("Inicializando sistema com integração ao banco de dados Oracle...");

        // O serviço já carrega dados existentes automaticamente no construtor
        // Agora verificamos se existem dados, e se não, criamos dados padrão

        List<Operator> operators = service.listarOperators();
        if (operators.isEmpty()) {
            System.out.println("Criando operadores padrão...");
            service.cadastrarOperator(new Operator("OP001", "Dr. João Silva", "joao.silva@lab.com", "ADMIN"));
            service.cadastrarOperator(new Operator("OP002", "Maria Santos", "maria.santos@lab.com", "TECNICO"));
            service.cadastrarOperator(new Operator("OP003", "Carlos Oliveira", "carlos.oliveira@lab.com", "OPERADOR"));
        } else {
            System.out.println("Carregados " + operators.size() + " operadores existentes do banco");
        }

        List<DigitalMicroscope> microscopes = service.listarMicroscopes();
        if (microscopes.isEmpty()) {
            System.out.println("Criando microscópios padrão...");
            service.cadastrarMicroscope(new DigitalMicroscope("MIC001", "Olympus BX53", "1920x1080", 12.5));
            service.cadastrarMicroscope(new DigitalMicroscope("MIC002", "Nikon Eclipse E200", "1280x720", 8.0));
        } else {
            System.out.println("Carregados " + microscopes.size() + " microscópios existentes do banco");
        }

        List<Sample> samples = service.listarSamples();
        if (samples.isEmpty()) {
            System.out.println("Criando amostras padrão...");
            service.cadastrarSample(new Sample("SAMPLE_001", "Sangue Paciente A", "Sangue", "Dr. João Silva"));
            service.cadastrarSample(new Sample("SAMPLE_002", "Tecido Muscular", "Tecido", "Maria Santos"));
            service.cadastrarSample(new Sample("SAMPLE_003", "Célula Neural", "Neurônio", "Carlos Oliveira"));
        } else {
            System.out.println("Carregadas " + samples.size() + " amostras existentes do banco");
        }

        List<Measurement> measurements = service.listarMeasurements();
        System.out.println("Carregadas " + measurements.size() + " medições existentes do banco");

        System.out.println("Sistema inicializado com sucesso - dados persistidos no Oracle!");
    }

    // ===== ENDPOINTS PARA AMOSTRAS =====

    /**
     * GET /api/samples - Lista todas as amostras
     */
    public String getSamples() {
        List<Sample> samples = service.listarSamples();
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"samples\": [\n");

        for (int i = 0; i < samples.size(); i++) {
            Sample sample = samples.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(sample.getId()).append("\",\n");
            json.append("      \"nome\": \"").append(sample.getNome()).append("\",\n");
            json.append("      \"tipo\": \"").append(sample.getTipo()).append("\",\n");
            json.append("      \"dataColeta\": \"").append(sample.getDataColeta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\",\n");
            json.append("      \"operadorResponsavel\": \"").append(sample.getOperadorResponsavel()).append("\"\n");
            json.append("    }");
            if (i < samples.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ],\n");
        json.append("  \"total\": ").append(samples.size()).append(",\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\"\n");
        json.append("}");

        return json.toString();
    }

    /**
     * POST /api/samples - Cadastra nova amostra
     */
    public String createSample(String id, String nome, String tipo, String operadorResponsavel) {
        Sample novaSample = new Sample(id, nome, tipo, operadorResponsavel);
        boolean sucesso = service.cadastrarSample(novaSample);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"success\": ").append(sucesso).append(",\n");
        json.append("  \"message\": \"").append(sucesso ? "Amostra cadastrada com sucesso" : "Erro ao cadastrar amostra").append("\",\n");
        json.append("  \"sampleId\": \"").append(id).append("\",\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\"\n");
        json.append("}");

        // Exporta automaticamente os dados atualizados
        if (sucesso) {
            exportarDadosAtualizados();
        }

        return json.toString();
    }

    /**
     * GET /api/samples/{id} - Busca amostra por ID
     */
    public String getSampleById(String id) {
        Sample sample = service.buscarSamplePorId(id);

        if (sample != null) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"found\": true,\n");
            json.append("  \"sample\": {\n");
            json.append("    \"id\": \"").append(sample.getId()).append("\",\n");
            json.append("    \"nome\": \"").append(sample.getNome()).append("\",\n");
            json.append("    \"tipo\": \"").append(sample.getTipo()).append("\",\n");
            json.append("    \"dataColeta\": \"").append(sample.getDataColeta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\",\n");
            json.append("    \"operadorResponsavel\": \"").append(sample.getOperadorResponsavel()).append("\"\n");
            json.append("  }\n");
            json.append("}");
            return json.toString();
        } else {
            return "{\"found\": false, \"message\": \"Amostra não encontrada\"}";
        }
    }

    // ===== ENDPOINTS PARA MEDIÇÕES =====

    /**
     * GET /api/measurements - Lista todas as medições
     */
    public String getMeasurements() {
        List<Measurement> measurements = service.listarMeasurements();
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"measurements\": [\n");

        for (int i = 0; i < measurements.size(); i++) {
            Measurement measurement = measurements.get(i);
            double areaPixels = measurement.getAreaPixels();
            if (areaPixels <= 0 && measurement.getScalePixelsPerUm() > 0) {
                double scale = measurement.getScalePixelsPerUm();
                areaPixels = measurement.getArea() * Math.pow(scale, 2);
            }

            double scale = measurement.getScalePixelsPerUm() > 0 ? measurement.getScalePixelsPerUm() : 10.0;
            MicroscopyImage imagem = measurement.getImagem();
            String imagemId = imagem != null ? imagem.getId() : "";
            String imagemArquivo = imagem != null ? imagem.getArquivo() : null;

            json.append("    {\n");
            json.append("      \"id\": \"").append(measurement.getId()).append("\",\n");
            json.append("      \"sampleId\": \"").append(measurement.getSample().getId()).append("\",\n");
            json.append("      \"area_pixels\": ").append(String.format(Locale.US, "%.2f", areaPixels)).append(",\n");
            json.append("      \"area_um2\": ").append(String.format(Locale.US, "%.6f", measurement.getArea())).append(",\n");
            json.append("      \"dataHora\": \"").append(measurement.getDataHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\",\n");
            json.append("      \"imagemId\": \"").append(imagemId).append("\",\n");
            json.append("      \"nomeImagem\": \"").append(imagemArquivo != null ? imagemArquivo : "").append("\",\n");
            json.append("      \"operator\": \"").append(measurement.getSample().getOperadorResponsavel()).append("\",\n");
            json.append("      \"scale_pixels_per_um\": ").append(String.format(Locale.US, "%.4f", scale)).append('\n');
            json.append("    }");
            if (i < measurements.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ],\n");
        json.append("  \"total\": ").append(measurements.size()).append(",\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\"\n");
        json.append("}");

        return json.toString();
    }

    /**
     * POST /api/measurements - Registra nova medição
     */
    public String createMeasurement(String id, String sampleId, double area, String imagemId) {
        Sample sample = service.buscarSamplePorId(sampleId);

        if (sample == null) {
            return "{\"success\": false, \"message\": \"Amostra não encontrada\"}";
        }

        // Busca imagem se fornecida
        MicroscopyImage imagem = null;
        if (imagemId != null && !imagemId.isEmpty()) {
            for (MicroscopyImage img : service.listarImages()) {
                if (img.getId().equals(imagemId)) {
                    imagem = img;
                    break;
                }
            }
        }

        Measurement novaMedicao = new Measurement(id, sample, area, imagem);
        boolean sucesso = service.cadastrarMeasurement(novaMedicao);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"success\": ").append(sucesso).append(",\n");
        json.append("  \"message\": \"").append(sucesso ? "Medição registrada com sucesso" : "Erro ao registrar medição").append("\",\n");
        json.append("  \"measurementId\": \"").append(id).append("\",\n");
        json.append("  \"area\": ").append(area).append(",\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\"\n");
        json.append("}");

        // Exporta automaticamente os dados atualizados
        if (sucesso) {
            exportarDadosAtualizados();
        }

        return json.toString();
    }

    // ===== ENDPOINTS PARA OPERADORES =====

    /**
     * GET /api/operators - Lista todos os operadores
     */
    public String getOperators() {
        List<Operator> operators = service.listarOperators();
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"operators\": [\n");

        for (int i = 0; i < operators.size(); i++) {
            Operator operator = operators.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(operator.getId()).append("\",\n");
            json.append("      \"nome\": \"").append(operator.getNome()).append("\",\n");
            json.append("      \"email\": \"").append(operator.getEmail()).append("\",\n");
            json.append("      \"nivelAcesso\": \"").append(operator.getNivelAcesso()).append("\"\n");
            json.append("    }");
            if (i < operators.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ],\n");
        json.append("  \"total\": ").append(operators.size()).append("\n");
        json.append("}");

        return json.toString();
    }

    // ===== ENDPOINTS PARA MICROSCÓPIOS =====

    /**
     * GET /api/microscopes - Lista todos os microscópios
     */
    public String getMicroscopes() {
        List<DigitalMicroscope> microscopes = service.listarMicroscopes();
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"microscopes\": [\n");

        for (int i = 0; i < microscopes.size(); i++) {
            DigitalMicroscope microscope = microscopes.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(microscope.getId()).append("\",\n");
            json.append("      \"modelo\": \"").append(microscope.getModelo()).append("\",\n");
            json.append("      \"resolucao\": \"").append(microscope.getResolucao()).append("\",\n");
            json.append("      \"escala\": ").append(microscope.getEscala()).append("\n");
            json.append("    }");
            if (i < microscopes.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ],\n");
        json.append("  \"total\": ").append(microscopes.size()).append("\n");
        json.append("}");

        return json.toString();
    }

    // ===== ENDPOINTS PARA IMAGENS =====

    /**
     * GET /api/images/{imageId} - Recupera imagem do banco de dados como BLOB
     *
     * @param imageId ID da imagem ou nome do arquivo
     * @return byte[] com os dados da imagem ou null se não encontrado
     */
    public byte[] getImageBlob(String imageId) {
        try {
            // Primeiro tenta buscar por IMAGE_ID exato
            byte[] imageData = service.getImageBlobById(imageId);

            if (imageData != null) {
                System.out.println("✅ Imagem recuperada do banco: " + imageId + " (" + imageData.length + " bytes)");
                return imageData;
            }

            // Se não encontrou, tenta buscar por nome de arquivo
            // Remove extensão e prefixos comuns
            String cleanId = imageId.replaceAll("\\.(jpg|jpeg|png|tiff|bmp)$", "");
            imageData = service.getImageBlobById(cleanId);

            if (imageData != null) {
                System.out.println("✅ Imagem recuperada do banco (por nome): " + cleanId + " (" + imageData.length + " bytes)");
                return imageData;
            }

            System.out.println("⚠️ Imagem não encontrada no banco: " + imageId);
            return null;

        } catch (Exception e) {
            System.err.println("❌ Erro ao recuperar imagem do banco: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ===== ENDPOINTS PARA ESTATÍSTICAS =====

    /**
     * GET /api/stats - Retorna estatísticas gerais do sistema
     */
    public String getStats() {
        List<Measurement> measurements = service.listarMeasurements();
        List<Sample> samples = service.listarSamples();
        List<Operator> operators = service.listarOperators();

        double areaMedia = measurements.isEmpty() ? 0.0 : service.calcularAreaMedia();
        Measurement maiorMedicao = service.encontrarMaiorMedicao();

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"totalMeasurements\": ").append(measurements.size()).append(",\n");
        json.append("  \"totalSamples\": ").append(samples.size()).append(",\n");
        json.append("  \"totalOperators\": ").append(operators.size()).append(",\n");
        json.append("  \"averageArea\": ").append(String.format(Locale.US, "%.2f", areaMedia)).append(",\n");
        json.append("  \"maxArea\": ").append(maiorMedicao != null ? String.format(Locale.US, "%.2f", maiorMedicao.getArea()) : "0.00").append(",\n");
        json.append("  \"lastUpdate\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\"\n");
        json.append("}");

        return json.toString();
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Exporta dados atualizados para arquivos de integração
     */
    private void exportarDadosAtualizados() {
        try {
            String dirIntegracao = "../../data-integration";
            integration.gerarArquivoJSON(dirIntegracao + "/measurements.json");
            integration.exportarAmostrasJSON(dirIntegracao + "/samples.json");
            System.out.println("Dados exportados automaticamente para " + dirIntegracao);
        } catch (Exception e) {
            System.out.println("Erro ao exportar dados: " + e.getMessage());
        }
    }

    /**
     * Endpoint para forçar exportação manual
     */
    public String exportData() {
        try {
            exportarDadosAtualizados();
            return "{\"success\": true, \"message\": \"Dados exportados com sucesso\"}";
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"Erro ao exportar dados: " + e.getMessage() + "\"}";
        }
    }

    /**
     * Health check da API
     */
    public String healthCheck() {
        return "{\n" +
               "  \"status\": \"OK\",\n" +
               "  \"service\": \"Scanalyze Micromedicao API\",\n" +
               "  \"version\": \"1.0\",\n" +
               "  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\",\n" +
               "  \"database\": \"Connected\"\n" +
               "}";
    }
}
