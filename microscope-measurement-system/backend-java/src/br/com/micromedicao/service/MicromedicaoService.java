package br.com.micromedicao.service;

import br.com.micromedicao.model.*;
import br.com.micromedicao.dao.*;
import java.util.*;
import java.time.LocalDateTime;
import java.sql.SQLException;

/**
 * Classe de serviço principal do sistema de micromedição.
 * Esta classe gerencia todas as operações de negócio relacionadas
 * ao cadastro, consulta e manipulação das entidades do sistema.
 *
 * Versão atualizada para integração com banco de dados Oracle.
 *
 * @author Sistema de Micromedição
 * @version 2.0
 */
public class MicromedicaoService {

    // DAOs para acesso ao banco de dados
    private SampleDAO sampleDAO;
    private MeasurementDAO measurementDAO;
    private OperatorDAO operatorDAO;
    private MicroscopyImageDAO imageDAO;

    // Cache em memória para performance (opcional)
    private Map<String, Sample> samplesCache;
    private Map<String, Measurement> measurementsCache;
    private Map<String, Operator> operatorsCache;
    private Map<String, DigitalMicroscope> microscopesCache;
    private Map<String, MicroscopyImage> imagesCache;

    // Collections para compatibilidade com métodos existentes
    private List<Sample> samples;
    private List<Measurement> measurements;
    private List<Operator> operators;
    private List<DigitalMicroscope> microscopes;
    private List<MicroscopyImage> images;

    private boolean useDatabasePersistence = true;

    /**
     * Construtor que inicializa os DAOs e caches do serviço.
     */
    public MicromedicaoService() {
        // Inicializa DAOs
        this.sampleDAO = new SampleDAO();
        this.measurementDAO = new MeasurementDAO();
        this.operatorDAO = new OperatorDAO();
        this.imageDAO = new MicroscopyImageDAO();

        // Inicializa caches
        this.samplesCache = new HashMap<>();
        this.measurementsCache = new HashMap<>();
        this.operatorsCache = new HashMap<>();
        this.microscopesCache = new HashMap<>();
        this.imagesCache = new HashMap<>();

        // Inicializa listas
        this.samples = new ArrayList<>();
        this.measurements = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.microscopes = new ArrayList<>();
        this.images = new ArrayList<>();

        // Tenta carregar dados existentes do banco
        loadExistingDataFromDatabase();
    }

    /**
     * Carrega dados existentes do banco de dados no cache.
     */
    private void loadExistingDataFromDatabase() {
        if (!useDatabasePersistence) return;

        try {
            System.out.println("Verificando conexão com banco de dados Oracle...");

            // Primeiro testa a conexão
            if (!testDatabaseConnection()) {
                System.err.println("Não foi possível conectar ao banco de dados Oracle");
                System.out.println("Sistema funcionará em modo de memória apenas");
                useDatabasePersistence = false;
                return;
            }

            System.out.println("Conexão com Oracle estabelecida com sucesso!");
            System.out.println("Carregando dados existentes do banco de dados...");

            // Carrega amostras
            List<Sample> existingSamples = sampleDAO.findAll();
            for (Sample sample : existingSamples) {
                samplesCache.put(sample.getId(), sample);
                samples.add(sample);
            }
            System.out.println("Carregadas " + existingSamples.size() + " amostras do banco");

            // Carrega medições
            List<Measurement> existingMeasurements = measurementDAO.findAll();
            for (Measurement measurement : existingMeasurements) {
                measurementsCache.put(measurement.getId(), measurement);
                measurements.add(measurement);
            }
            System.out.println("Carregadas " + existingMeasurements.size() + " medições do banco");

            // Carrega operadores
            List<Operator> existingOperators = operatorDAO.findAll();
            for (Operator operator : existingOperators) {
                operatorsCache.put(operator.getId(), operator);
                operators.add(operator);
            }
            System.out.println("Carregados " + existingOperators.size() + " operadores do banco");

        } catch (SQLException e) {
            System.err.println("Erro ao carregar dados do banco: " + e.getMessage());
            System.out.println("Verificando se as tabelas existem...");

            // Se erro foi por tabelas não existirem, tenta criar
            if (e.getMessage().contains("table or view does not exist") ||
                e.getMessage().contains("ORA-00942")) {
                System.out.println("Tabelas não existem. Sistema funcionará em modo de memória.");
                System.out.println("Para habilitar persistência, execute o script oracle_schema.sql no banco Oracle");
            }

            System.out.println("Sistema funcionará em modo de memória apenas");
            useDatabasePersistence = false;
        }
    }

    /**
     * Testa a conexão com o banco de dados.
     *
     * @return true se conexão foi bem-sucedida
     */
    private boolean testDatabaseConnection() {
        try {
            br.com.micromedicao.connection.ConnectionFactory factory =
                br.com.micromedicao.connection.ConnectionFactory.getInstance();
            return factory.testConnection();
        } catch (Exception e) {
            System.err.println("Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }

    // ===== MÉTODOS PARA SAMPLE =====

    /**
     * Cadastra uma nova amostra no sistema.
     *
     * @param sample Amostra a ser cadastrada
     * @return boolean true se o cadastro foi bem-sucedido
     */
    public boolean cadastrarSample(Sample sample) {
        if (sample != null && sample.getId() != null) {
            // Verifica se já existe uma amostra com o mesmo ID
            if (buscarSamplePorId(sample.getId()) == null) {
                boolean success = false;

                // Tenta salvar no banco de dados primeiro
                if (useDatabasePersistence) {
                    try {
                        success = sampleDAO.insert(sample);
                        if (success) {
                            System.out.println("Amostra salva no banco: " + sample.getNome());
                        }
                    } catch (SQLException e) {
                        System.err.println("Erro ao salvar amostra no banco: " + e.getMessage());
                        // Continuará para salvar em cache
                    }
                }

                // Sempre adiciona ao cache para consistência
                samplesCache.put(sample.getId(), sample);
                success = true; // Garante que retorne true se chegou até aqui

                System.out.println("Amostra cadastrada com sucesso: " + sample.getNome());
                return success;
            } else {
                System.out.println("Erro: Já existe uma amostra com ID " + sample.getId());
                return false;
            }
        }
        System.out.println("Erro: Amostra inválida para cadastro");
        return false;
    }

    /**
     * Lista todas as amostras cadastradas.
     *
     * @return List<Sample> lista de todas as amostras
     */
    public List<Sample> listarSamples() {
        // Tenta buscar do banco primeiro para dados mais atualizados
        if (useDatabasePersistence) {
            try {
                List<Sample> samplesFromDB = sampleDAO.findAll();
                // Atualiza cache com dados do banco
                samplesCache.clear();
                for (Sample sample : samplesFromDB) {
                    samplesCache.put(sample.getId(), sample);
                }
                return new ArrayList<>(samplesFromDB);
            } catch (SQLException e) {
                System.err.println("Erro ao buscar amostras do banco: " + e.getMessage());
                System.out.println("Retornando dados do cache...");
            }
        }

        // Fallback para cache se banco não disponível
        return new ArrayList<>(samplesCache.values());
    }

    /**
     * Busca uma amostra pelo ID.
     *
     * @param id ID da amostra a ser buscada
     * @return Sample amostra encontrada ou null se não encontrada
     */
    public Sample buscarSamplePorId(String id) {
        if (id == null) return null;

        // Primeiro verifica no cache
        Sample sample = samplesCache.get(id);
        if (sample != null) {
            return sample;
        }

        // Se não encontrou no cache e banco está disponível, busca no banco
        if (useDatabasePersistence) {
            try {
                sample = sampleDAO.findBySampleId(id);
                if (sample != null) {
                    // Adiciona ao cache para próximas consultas
                    samplesCache.put(sample.getId(), sample);
                    return sample;
                }
            } catch (SQLException e) {
                System.err.println("Erro ao buscar amostra no banco: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Busca amostras por tipo.
     * 
     * @param tipo Tipo das amostras a serem buscadas
     * @return List<Sample> lista de amostras do tipo especificado
     */
    public List<Sample> buscarSamplesPorTipo(String tipo) {
        List<Sample> resultado = new ArrayList<>();
        if (tipo != null) {
            for (Sample sample : this.samples) {
                if (tipo.equalsIgnoreCase(sample.getTipo())) {
                    resultado.add(sample);
                }
            }
        }
        return resultado;
    }

    // ===== MÉTODOS PARA MEASUREMENT =====

    /**
     * Cadastra uma nova medição no sistema.
     *
     * @param measurement Medição a ser cadastrada
     * @return boolean true se o cadastro foi bem-sucedido
     */
    public boolean cadastrarMeasurement(Measurement measurement) {
        if (measurement != null && measurement.getId() != null && measurement.validarMedicao()) {
            // Verifica se já existe uma medição com o mesmo ID
            if (buscarMeasurementPorId(measurement.getId()) == null) {
                boolean success = false;

                // Tenta salvar no banco de dados primeiro
                if (useDatabasePersistence) {
                    try {
                        success = measurementDAO.insert(measurement);
                        if (success) {
                            System.out.println("Medição salva no banco: " + measurement.getId());
                        }
                    } catch (SQLException e) {
                        System.err.println("Erro ao salvar medição no banco: " + e.getMessage());
                        // Continuará para salvar em cache
                    }
                }

                // Sempre adiciona ao cache para consistência
                measurementsCache.put(measurement.getId(), measurement);
                success = true; // Garante que retorne true se chegou até aqui

                System.out.println("Medição cadastrada com sucesso: " + measurement.getId());
                return success;
            } else {
                System.out.println("Erro: Já existe uma medição com ID " + measurement.getId());
                return false;
            }
        }
        System.out.println("Erro: Medição inválida para cadastro");
        return false;
    }

    /**
     * Lista todas as medições cadastradas.
     *
     * @return List<Measurement> lista de todas as medições
     */
    public List<Measurement> listarMeasurements() {
        // Tenta buscar do banco primeiro para dados mais atualizados
        if (useDatabasePersistence) {
            try {
                List<Measurement> measurementsFromDB = measurementDAO.findAll();
                // Atualiza cache com dados do banco
                measurementsCache.clear();
                for (Measurement measurement : measurementsFromDB) {
                    measurementsCache.put(measurement.getId(), measurement);
                }
                return new ArrayList<>(measurementsFromDB);
            } catch (SQLException e) {
                System.err.println("Erro ao buscar medições do banco: " + e.getMessage());
                System.out.println("Retornando dados do cache...");
            }
        }

        // Fallback para cache se banco não disponível
        return new ArrayList<>(measurementsCache.values());
    }

    /**
     * Busca uma medição pelo ID.
     *
     * @param id ID da medição a ser buscada
     * @return Measurement medição encontrada ou null se não encontrada
     */
    public Measurement buscarMeasurementPorId(String id) {
        if (id == null) return null;

        // Primeiro verifica no cache
        Measurement measurement = measurementsCache.get(id);
        if (measurement != null) {
            return measurement;
        }

        // Se não encontrou no cache e banco está disponível, busca no banco
        if (useDatabasePersistence) {
            try {
                measurement = measurementDAO.findByMeasurementId(id);
                if (measurement != null) {
                    // Adiciona ao cache para próximas consultas
                    measurementsCache.put(measurement.getId(), measurement);
                    return measurement;
                }
            } catch (SQLException e) {
                System.err.println("Erro ao buscar medição no banco: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Busca medições por amostra.
     *
     * @param sampleId ID da amostra
     * @return List<Measurement> lista de medições da amostra
     */
    public List<Measurement> buscarMeasurementsPorSample(String sampleId) {
        if (sampleId == null) return new ArrayList<>();

        // Tenta buscar do banco primeiro
        if (useDatabasePersistence) {
            try {
                return measurementDAO.findBySampleId(sampleId);
            } catch (SQLException e) {
                System.err.println("Erro ao buscar medições por amostra no banco: " + e.getMessage());
            }
        }

        // Fallback para cache
        List<Measurement> resultado = new ArrayList<>();
        for (Measurement measurement : measurementsCache.values()) {
            if (measurement.getSample() != null &&
                sampleId.equals(measurement.getSample().getId())) {
                resultado.add(measurement);
            }
        }
        return resultado;
    }

    // ===== MÉTODOS PARA OPERATOR =====

    /**
     * Cadastra um novo operador no sistema.
     *
     * @param operator Operador a ser cadastrado
     * @return boolean true se o cadastro foi bem-sucedido
     */
    public boolean cadastrarOperator(Operator operator) {
        if (operator != null && operator.getId() != null) {
            // Verifica se já existe um operador com o mesmo ID ou email
            if (buscarOperatorPorId(operator.getId()) == null &&
                buscarOperatorPorEmail(operator.getEmail()) == null) {

                boolean success = false;

                // Tenta salvar no banco de dados primeiro
                if (useDatabasePersistence) {
                    try {
                        success = operatorDAO.insert(operator);
                        if (success) {
                            System.out.println("Operador salvo no banco: " + operator.getNome());
                        }
                    } catch (SQLException e) {
                        System.err.println("Erro ao salvar operador no banco: " + e.getMessage());
                    }
                }

                // Sempre adiciona ao cache para consistência
                operatorsCache.put(operator.getId(), operator);
                success = true;

                System.out.println("Operador cadastrado com sucesso: " + operator.getNome());
                return success;
            } else {
                System.out.println("Erro: Já existe um operador com ID ou email informado");
                return false;
            }
        }
        System.out.println("Erro: Operador inválido para cadastro");
        return false;
    }

    /**
     * Lista todos os operadores cadastrados.
     *
     * @return List<Operator> lista de todos os operadores
     */
    public List<Operator> listarOperators() {
        // Tenta buscar do banco primeiro para dados mais atualizados
        if (useDatabasePersistence) {
            try {
                List<Operator> operatorsFromDB = operatorDAO.findAll();
                // Atualiza cache com dados do banco
                operatorsCache.clear();
                for (Operator operator : operatorsFromDB) {
                    operatorsCache.put(operator.getId(), operator);
                }
                return new ArrayList<>(operatorsFromDB);
            } catch (SQLException e) {
                System.err.println("Erro ao buscar operadores do banco: " + e.getMessage());
                System.out.println("Retornando dados do cache...");
            }
        }

        // Fallback para cache se banco não disponível
        return new ArrayList<>(operatorsCache.values());
    }

    /**
     * Busca um operador pelo ID.
     * 
     * @param id ID do operador a ser buscado
     * @return Operator operador encontrado ou null se não encontrado
     */
    public Operator buscarOperatorPorId(String id) {
        if (id != null) {
            for (Operator operator : this.operators) {
                if (id.equals(operator.getId())) {
                    return operator;
                }
            }
        }
        return null;
    }

    /**
     * Busca um operador pelo email.
     * 
     * @param email Email do operador a ser buscado
     * @return Operator operador encontrado ou null se não encontrado
     */
    public Operator buscarOperatorPorEmail(String email) {
        if (email != null) {
            for (Operator operator : this.operators) {
                if (email.equalsIgnoreCase(operator.getEmail())) {
                    return operator;
                }
            }
        }
        return null;
    }

    // ===== MÉTODOS PARA DIGITAL MICROSCOPE =====

    /**
     * Cadastra um novo microscópio no sistema.
     * 
     * @param microscope Microscópio a ser cadastrado
     * @return boolean true se o cadastro foi bem-sucedido
     */
    public boolean cadastrarMicroscope(DigitalMicroscope microscope) {
        if (microscope != null && microscope.getId() != null && microscope.verificarFuncionamento()) {
            // Verifica se já existe um microscópio com o mesmo ID
            if (buscarMicroscopePorId(microscope.getId()) == null) {
                this.microscopes.add(microscope);
                System.out.println("Microscópio cadastrado com sucesso: " + microscope.getModelo());
                return true;
            } else {
                System.out.println("Erro: Já existe um microscópio com ID " + microscope.getId());
                return false;
            }
        }
        System.out.println("Erro: Microscópio inválido para cadastro");
        return false;
    }

    /**
     * Lista todos os microscópios cadastrados.
     * 
     * @return List<DigitalMicroscope> lista de todos os microscópios
     */
    public List<DigitalMicroscope> listarMicroscopes() {
        return new ArrayList<>(this.microscopes);
    }

    /**
     * Busca um microscópio pelo ID.
     * 
     * @param id ID do microscópio a ser buscado
     * @return DigitalMicroscope microscópio encontrado ou null se não encontrado
     */
    public DigitalMicroscope buscarMicroscopePorId(String id) {
        if (id != null) {
            for (DigitalMicroscope microscope : this.microscopes) {
                if (id.equals(microscope.getId())) {
                    return microscope;
                }
            }
        }
        return null;
    }

    // ===== MÉTODOS PARA MICROSCOPY IMAGE =====

    /**
     * Cadastra uma nova imagem no sistema.
     * 
     * @param image Imagem a ser cadastrada
     * @return boolean true se o cadastro foi bem-sucedido
     */
    public boolean cadastrarImage(MicroscopyImage image) {
        if (image != null && image.getId() != null && 
            "Íntegra".equals(image.verificarIntegridade())) {
            // Verifica se já existe uma imagem com o mesmo ID
            if (buscarImagePorId(image.getId()) == null) {
                this.images.add(image);
                System.out.println("Imagem cadastrada com sucesso: " + image.getArquivo());
                return true;
            } else {
                System.out.println("Erro: Já existe uma imagem com ID " + image.getId());
                return false;
            }
        }
        System.out.println("Erro: Imagem inválida para cadastro");
        return false;
    }

    /**
     * Lista todas as imagens cadastradas.
     * 
     * @return List<MicroscopyImage> lista de todas as imagens
     */
    public List<MicroscopyImage> listarImages() {
        return new ArrayList<>(this.images);
    }

    /**
     * Busca uma imagem pelo ID.
     *
     * @param id ID da imagem a ser buscada
     * @return MicroscopyImage imagem encontrada ou null se não encontrada
     */
    public MicroscopyImage buscarImagePorId(String id) {
        if (id != null) {
            for (MicroscopyImage image : this.images) {
                if (id.equals(image.getId())) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * Recupera os dados binários (BLOB) de uma imagem do banco de dados.
     *
     * @param imageId ID da imagem
     * @return byte[] com os dados da imagem ou null se não encontrado
     */
    public byte[] getImageBlobById(String imageId) {
        if (imageId == null || !useDatabasePersistence) {
            return null;
        }

        try {
            return imageDAO.getImageBlobById(imageId);
        } catch (SQLException e) {
            System.err.println("Erro ao recuperar BLOB da imagem " + imageId + ": " + e.getMessage());
            return null;
        }
    }

    // ===== MÉTODOS DE RELATÓRIO E ESTATÍSTICAS =====

    /**
     * Gera um relatório geral do sistema.
     */
    public void gerarRelatorioGeral() {
        System.out.println("\n=== RELATÓRIO GERAL DO SISTEMA ===");
        System.out.println("Amostras cadastradas: " + this.samples.size());
        System.out.println("Medições realizadas: " + this.measurements.size());
        System.out.println("Operadores cadastrados: " + this.operators.size());
        System.out.println("Microscópios disponíveis: " + this.microscopes.size());
        System.out.println("Imagens capturadas: " + this.images.size());
        
        if (!this.measurements.isEmpty()) {
            double areaMedia = calcularAreaMedia();
            System.out.println("Área média das medições: " + String.format("%.2f", areaMedia) + " unidades²");
        }
        
        System.out.println("================================");
    }

    /**
     * Calcula a área média de todas as medições.
     * 
     * @return double área média
     */
    public double calcularAreaMedia() {
        if (this.measurements.isEmpty()) return 0.0;
        
        double somaAreas = 0.0;
        for (Measurement measurement : this.measurements) {
            somaAreas += measurement.getArea();
        }
        return somaAreas / this.measurements.size();
    }

    /**
     * Encontra a medição com maior área.
     * 
     * @return Measurement medição com maior área ou null se não houver medições
     */
    public Measurement encontrarMaiorMedicao() {
        if (this.measurements.isEmpty()) return null;
        
        Measurement maior = this.measurements.get(0);
        for (Measurement measurement : this.measurements) {
            if (measurement.getArea() > maior.getArea()) {
                maior = measurement;
            }
        }
        return maior;
    }

    /**
     * Conta o número de entidades cadastradas.
     * 
     * @return Map<String, Integer> mapa com contadores de cada tipo de entidade
     */
    public Map<String, Integer> obterEstatisticas() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("samples", this.samples.size());
        stats.put("measurements", this.measurements.size());
        stats.put("operators", this.operators.size());
        stats.put("microscopes", this.microscopes.size());
        stats.put("images", this.images.size());
        return stats;
    }

    /**
     * Valida a integridade geral do sistema.
     * 
     * @return boolean true se o sistema está íntegro
     */
    public boolean validarIntegridadeSistema() {
        boolean integro = true;
        
        // Valida se todas as medições têm amostras válidas
        for (Measurement measurement : this.measurements) {
            if (measurement.getSample() == null || 
                buscarSamplePorId(measurement.getSample().getId()) == null) {
                System.out.println("Erro: Medição " + measurement.getId() + " com amostra inválida");
                integro = false;
            }
        }
        
        // Valida se todas as imagens têm microscópios válidos
        for (MicroscopyImage image : this.images) {
            if (image.getMicroscopio() == null || 
                buscarMicroscopePorId(image.getMicroscopio().getId()) == null) {
                System.out.println("Erro: Imagem " + image.getId() + " com microscópio inválido");
                integro = false;
            }
        }
        
        if (integro) {
            System.out.println("Sistema íntegro - todas as referências são válidas");
        }
        
        return integro;
    }
}