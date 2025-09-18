package br.com.micromedicao.service;

import br.com.micromedicao.model.*;
import java.util.*;
import java.time.LocalDateTime;

/**
 * Classe de serviço principal do sistema de micromedição.
 * Esta classe gerencia todas as operações de negócio relacionadas
 * ao cadastro, consulta e manipulação das entidades do sistema.
 * 
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class MicromedicaoService {
    
    /**
     * Lista de amostras cadastradas no sistema
     */
    private List<Sample> samples;
    
    /**
     * Lista de medições realizadas no sistema
     */
    private List<Measurement> measurements;
    
    /**
     * Lista de operadores cadastrados no sistema
     */
    private List<Operator> operators;
    
    /**
     * Lista de microscópios disponíveis no sistema
     */
    private List<DigitalMicroscope> microscopes;
    
    /**
     * Lista de imagens microscópicas do sistema
     */
    private List<MicroscopyImage> images;

    /**
     * Construtor padrão que inicializa as listas do serviço.
     */
    public MicromedicaoService() {
        this.samples = new ArrayList<>();
        this.measurements = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.microscopes = new ArrayList<>();
        this.images = new ArrayList<>();
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
                this.samples.add(sample);
                System.out.println("Amostra cadastrada com sucesso: " + sample.getNome());
                return true;
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
        return new ArrayList<>(this.samples);
    }

    /**
     * Busca uma amostra pelo ID.
     * 
     * @param id ID da amostra a ser buscada
     * @return Sample amostra encontrada ou null se não encontrada
     */
    public Sample buscarSamplePorId(String id) {
        if (id != null) {
            for (Sample sample : this.samples) {
                if (id.equals(sample.getId())) {
                    return sample;
                }
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
                this.measurements.add(measurement);
                System.out.println("Medição cadastrada com sucesso: " + measurement.getId());
                return true;
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
        return new ArrayList<>(this.measurements);
    }

    /**
     * Busca uma medição pelo ID.
     * 
     * @param id ID da medição a ser buscada
     * @return Measurement medição encontrada ou null se não encontrada
     */
    public Measurement buscarMeasurementPorId(String id) {
        if (id != null) {
            for (Measurement measurement : this.measurements) {
                if (id.equals(measurement.getId())) {
                    return measurement;
                }
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
        List<Measurement> resultado = new ArrayList<>();
        if (sampleId != null) {
            for (Measurement measurement : this.measurements) {
                if (measurement.getSample() != null && 
                    sampleId.equals(measurement.getSample().getId())) {
                    resultado.add(measurement);
                }
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
                this.operators.add(operator);
                System.out.println("Operador cadastrado com sucesso: " + operator.getNome());
                return true;
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
        return new ArrayList<>(this.operators);
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