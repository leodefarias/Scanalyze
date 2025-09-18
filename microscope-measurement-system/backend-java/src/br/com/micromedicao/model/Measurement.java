package br.com.micromedicao.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe que representa uma medição realizada em uma amostra.
 * Esta classe encapsula as informações de uma medição específica,
 * incluindo a área calculada, a amostra medida e a imagem associada.
 * 
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class Measurement {
    
    /**
     * Identificador único da medição
     */
    private String id;
    
    /**
     * Amostra que foi medida
     */
    private Sample sample;
    
    /**
     * Área calculada da medição em unidades quadradas
     */
    private double area;
    
    /**
     * Data e hora quando a medição foi realizada
     */
    private LocalDateTime dataHora;
    
    /**
     * Imagem microscópica associada à medição
     */
    private MicroscopyImage imagem;

    /**
     * Construtor completo para criar uma nova medição.
     * 
     * @param id Identificador único da medição
     * @param sample Amostra que foi medida
     * @param area Área calculada em unidades quadradas
     * @param dataHora Data e hora da medição
     * @param imagem Imagem microscópica associada
     */
    public Measurement(String id, Sample sample, double area, LocalDateTime dataHora, MicroscopyImage imagem) {
        this.id = id;
        this.sample = sample;
        this.area = area;
        this.dataHora = dataHora;
        this.imagem = imagem;
    }

    /**
     * Construtor alternativo com data atual.
     * 
     * @param id Identificador único da medição
     * @param sample Amostra que foi medida
     * @param area Área calculada em unidades quadradas
     * @param imagem Imagem microscópica associada
     */
    public Measurement(String id, Sample sample, double area, MicroscopyImage imagem) {
        this(id, sample, area, LocalDateTime.now(), imagem);
    }

    // Getters e Setters

    /**
     * Obtém o identificador da medição.
     * 
     * @return String com o ID da medição
     */
    public String getId() {
        return id;
    }

    /**
     * Define o identificador da medição.
     * 
     * @param id Novo identificador da medição
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtém a amostra medida.
     * 
     * @return Sample objeto da amostra
     */
    public Sample getSample() {
        return sample;
    }

    /**
     * Define a amostra medida.
     * 
     * @param sample Nova amostra a ser associada
     */
    public void setSample(Sample sample) {
        this.sample = sample;
    }

    /**
     * Obtém a área calculada.
     * 
     * @return double valor da área em unidades quadradas
     */
    public double getArea() {
        return area;
    }

    /**
     * Define a área calculada.
     * 
     * @param area Novo valor da área
     */
    public void setArea(double area) {
        this.area = area;
    }

    /**
     * Obtém a data e hora da medição.
     * 
     * @return LocalDateTime da medição
     */
    public LocalDateTime getDataHora() {
        return dataHora;
    }

    /**
     * Define a data e hora da medição.
     * 
     * @param dataHora Nova data e hora
     */
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    /**
     * Obtém a imagem associada à medição.
     * 
     * @return MicroscopyImage objeto da imagem
     */
    public MicroscopyImage getImagem() {
        return imagem;
    }

    /**
     * Define a imagem associada à medição.
     * 
     * @param imagem Nova imagem a ser associada
     */
    public void setImagem(MicroscopyImage imagem) {
        this.imagem = imagem;
    }

    // Métodos de funcionalidade

    /**
     * Exibe os detalhes completos da medição no console.
     * Este método formata e apresenta todas as informações da medição
     * de forma legível para o usuário.
     */
    public void exibirDetalhes() {
        System.out.println("=== DETALHES DA MEDIÇÃO ===");
        System.out.println("ID: " + this.id);
        System.out.println("Data/Hora: " + this.dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("Área: " + String.format("%.2f", this.area) + " unidades²");
        if (this.sample != null) {
            System.out.println("Amostra: " + this.sample.getNome() + " (ID: " + this.sample.getId() + ")");
        }
        if (this.imagem != null) {
            System.out.println("Imagem: " + this.imagem.getArquivo());
        }
        System.out.println("========================");
    }

    /**
     * Calcula a área com diferentes escalas de magnificação.
     * Sobrecarga do método calcularArea para diferentes unidades.
     * 
     * @param escala Fator de escala para conversão
     * @return double área recalculada com a nova escala
     */
    public double calcularArea(double escala) {
        return this.area * escala;
    }

    /**
     * Calcula a área convertendo para diferentes unidades.
     * Sobrecarga do método calcularArea com tipo de unidade.
     * 
     * @param escala Fator de escala para conversão
     * @param unidade String representando a unidade desejada
     * @return String área formatada com a unidade especificada
     */
    public String calcularArea(double escala, String unidade) {
        double areaConvertida = this.area * escala;
        return String.format("%.2f %s", areaConvertida, unidade);
    }

    /**
     * Valida se a medição está dentro de parâmetros aceitáveis.
     * Verifica se a área é positiva e se existe uma amostra associada.
     * 
     * @return boolean true se a medição é válida
     */
    public boolean validarMedicao() {
        boolean areaValida = this.area > 0;
        boolean sampleValida = this.sample != null;
        boolean imagemValida = this.imagem != null;
        
        if (!areaValida) {
            System.out.println("Erro: Área deve ser maior que zero");
        }
        if (!sampleValida) {
            System.out.println("Erro: Medição deve estar associada a uma amostra");
        }
        if (!imagemValida) {
            System.out.println("Erro: Medição deve ter uma imagem associada");
        }
        
        return areaValida && sampleValida && imagemValida;
    }

    /**
     * Compara esta medição com outra baseando-se na área.
     * 
     * @param outraMedicao Medição a ser comparada
     * @return int -1 se menor, 0 se igual, 1 se maior
     */
    public int compararArea(Measurement outraMedicao) {
        if (outraMedicao == null) return 1;
        return Double.compare(this.area, outraMedicao.area);
    }

    /**
     * Verifica se a medição foi realizada recentemente.
     * Considera como recente medições realizadas na última hora.
     * 
     * @return boolean true se a medição foi realizada na última hora
     */
    public boolean isMedicaoRecente() {
        return this.dataHora.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Retorna uma representação em string da medição.
     * 
     * @return String formatada com as informações principais da medição
     */
    @Override
    public String toString() {
        return String.format("Measurement{id='%s', area=%.2f, dataHora=%s, sample='%s'}", 
                           id, area, 
                           dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                           sample != null ? sample.getNome() : "N/A");
    }
}