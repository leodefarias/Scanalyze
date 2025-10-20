package br.com.micromedicao.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe que representa uma amostra para análise microscópica.
 * Esta classe encapsula todas as informações relacionadas a uma amostra,
 * incluindo dados de identificação, tipo e responsável pela coleta.
 * 
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class Sample {
    
    /**
     * Identificador único da amostra
     */
    private String id;
    
    /**
     * Nome descritivo da amostra
     */
    private String nome;
    
    /**
     * Tipo ou categoria da amostra (ex: sangue, tecido, etc.)
     */
    private String tipo;
    
    /**
     * Data e hora da coleta da amostra
     */
    private LocalDateTime dataColeta;
    
    /**
     * Operador responsável pela coleta da amostra
     */
    private String operadorResponsavel;

    /**
     * Construtor completo para criar uma nova amostra.
     * 
     * @param id Identificador único da amostra
     * @param nome Nome descritivo da amostra
     * @param tipo Tipo ou categoria da amostra
     * @param dataColeta Data e hora da coleta
     * @param operadorResponsavel Nome do operador responsável
     */
    public Sample(String id, String nome, String tipo, LocalDateTime dataColeta, String operadorResponsavel) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.dataColeta = dataColeta;
        this.operadorResponsavel = operadorResponsavel;
    }

    /**
     * Construtor alternativo com data atual.
     * 
     * @param id Identificador único da amostra
     * @param nome Nome descritivo da amostra
     * @param tipo Tipo ou categoria da amostra
     * @param operadorResponsavel Nome do operador responsável
     */
    public Sample(String id, String nome, String tipo, String operadorResponsavel) {
        this(id, nome, tipo, LocalDateTime.now(), operadorResponsavel);
    }

    // Getters e Setters

    /**
     * Obtém o identificador da amostra.
     * 
     * @return String com o ID da amostra
     */
    public String getId() {
        return id;
    }

    /**
     * Define o identificador da amostra.
     * 
     * @param id Novo identificador da amostra
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtém o nome da amostra.
     * 
     * @return String com o nome da amostra
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome da amostra.
     * 
     * @param nome Novo nome da amostra
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Obtém o tipo da amostra.
     * 
     * @return String com o tipo da amostra
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Define o tipo da amostra.
     * 
     * @param tipo Novo tipo da amostra
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Obtém a data de coleta da amostra.
     * 
     * @return LocalDateTime com a data de coleta
     */
    public LocalDateTime getDataColeta() {
        return dataColeta;
    }

    /**
     * Define a data de coleta da amostra.
     * 
     * @param dataColeta Nova data de coleta
     */
    public void setDataColeta(LocalDateTime dataColeta) {
        this.dataColeta = dataColeta;
    }

    /**
     * Obtém o operador responsável pela amostra.
     * 
     * @return String com o nome do operador responsável
     */
    public String getOperadorResponsavel() {
        return operadorResponsavel;
    }

    /**
     * Define o operador responsável pela amostra.
     * 
     * @param operadorResponsavel Nome do novo operador responsável
     */
    public void setOperadorResponsavel(String operadorResponsavel) {
        this.operadorResponsavel = operadorResponsavel;
    }

    // Métodos de funcionalidade

    /**
     * Exibe os detalhes completos da amostra no console.
     * Este método formata e apresenta todas as informações da amostra
     * de forma legível para o usuário.
     */
    public void exibirDetalhes() {
        System.out.println("=== DETALHES DA AMOSTRA ===");
        System.out.println("ID: " + this.id);
        System.out.println("Nome: " + this.nome);
        System.out.println("Tipo: " + this.tipo);
        System.out.println("Data de Coleta: " + this.dataColeta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("Operador Responsável: " + this.operadorResponsavel);
        System.out.println("========================");
    }

    /**
     * Atualiza o tipo da amostra e registra a alteração.
     * Este método permite modificar o tipo da amostra e 
     * fornece feedback sobre a operação.
     * 
     * @param novoTipo Novo tipo a ser atribuído à amostra
     * @return boolean true se a atualização foi bem-sucedida
     */
    public boolean atualizarTipo(String novoTipo) {
        if (novoTipo != null && !novoTipo.trim().isEmpty()) {
            String tipoAnterior = this.tipo;
            this.tipo = novoTipo.trim();
            System.out.println("Tipo da amostra " + this.id + " atualizado de '" + 
                             tipoAnterior + "' para '" + this.tipo + "'");
            return true;
        }
        return false;
    }

    /**
     * Verifica se a amostra foi coletada recentemente.
     * Considera como recente amostras coletadas nas últimas 24 horas.
     * 
     * @return boolean true se a amostra foi coletada nas últimas 24 horas
     */
    public boolean isColetaRecente() {
        return this.dataColeta.isAfter(LocalDateTime.now().minusDays(1));
    }

    /**
     * Calcula a idade da amostra em horas.
     * 
     * @return long número de horas desde a coleta
     */
    public long calcularIdadeEmHoras() {
        return java.time.Duration.between(this.dataColeta, LocalDateTime.now()).toHours();
    }

    /**
     * Retorna uma representação em string da amostra.
     * 
     * @return String formatada com as informações principais da amostra
     */
    @Override
    public String toString() {
        return String.format("Sample{id='%s', nome='%s', tipo='%s', dataColeta=%s, operador='%s'}", 
                           id, nome, tipo, 
                           dataColeta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 
                           operadorResponsavel);
    }
}