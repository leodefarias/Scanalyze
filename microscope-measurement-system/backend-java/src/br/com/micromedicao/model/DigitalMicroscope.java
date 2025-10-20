package br.com.micromedicao.model;

/**
 * Classe que representa um microscópio digital do sistema.
 * Esta classe encapsula as informações técnicas de um microscópio digital,
 * incluindo especificações técnicas e configurações de medição.
 * 
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class DigitalMicroscope {
    
    /**
     * Identificador único do microscópio
     */
    private String id;
    
    /**
     * Modelo do microscópio digital
     */
    private String modelo;
    
    /**
     * Resolução da câmera em pixels (formato: "1920x1080")
     */
    private String resolucao;
    
    /**
     * Escala de conversão pixel/micrômetro
     */
    private double escala;

    /**
     * Construtor completo para criar um novo microscópio digital.
     * 
     * @param id Identificador único do microscópio
     * @param modelo Modelo do microscópio
     * @param resolucao Resolução da câmera
     * @param escala Escala de conversão pixel/micrômetro
     */
    public DigitalMicroscope(String id, String modelo, String resolucao, double escala) {
        this.id = id;
        this.modelo = modelo;
        this.resolucao = resolucao;
        this.escala = escala;
    }

    // Getters e Setters

    /**
     * Obtém o identificador do microscópio.
     * 
     * @return String com o ID do microscópio
     */
    public String getId() {
        return id;
    }

    /**
     * Define o identificador do microscópio.
     * 
     * @param id Novo identificador do microscópio
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtém o modelo do microscópio.
     * 
     * @return String com o modelo
     */
    public String getModelo() {
        return modelo;
    }

    /**
     * Define o modelo do microscópio.
     * 
     * @param modelo Novo modelo do microscópio
     */
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    /**
     * Obtém a resolução do microscópio.
     * 
     * @return String com a resolução
     */
    public String getResolucao() {
        return resolucao;
    }

    /**
     * Define a resolução do microscópio.
     * 
     * @param resolucao Nova resolução
     */
    public void setResolucao(String resolucao) {
        this.resolucao = resolucao;
    }

    /**
     * Obtém a escala do microscópio.
     * 
     * @return double com a escala pixel/micrômetro
     */
    public double getEscala() {
        return escala;
    }

    /**
     * Define a escala do microscópio.
     * 
     * @param escala Nova escala pixel/micrômetro
     */
    public void setEscala(double escala) {
        this.escala = escala;
    }

    // Métodos de funcionalidade

    /**
     * Exibe os detalhes completos do microscópio no console.
     * Este método formata e apresenta todas as informações do microscópio
     * de forma legível para o usuário.
     */
    public void exibirDetalhes() {
        System.out.println("=== DETALHES DO MICROSCÓPIO ===");
        System.out.println("ID: " + this.id);
        System.out.println("Modelo: " + this.modelo);
        System.out.println("Resolução: " + this.resolucao);
        System.out.println("Escala: " + String.format("%.2f", this.escala) + " pixels/μm");
        System.out.println("Resolução Total: " + calcularPixelsTotal() + " pixels");
        System.out.println("Qualidade: " + avaliarQualidade());
        System.out.println("========================");
    }

    /**
     * Simula a captura de uma imagem pelo microscópio.
     * Este método representa a funcionalidade de captura de imagem
     * e retorna informações sobre a captura realizada.
     * 
     * @return String com informações sobre a imagem capturada
     */
    public String capturarImagem() {
        System.out.println("Iniciando captura de imagem no microscópio " + this.modelo);
        System.out.println("Configurando resolução: " + this.resolucao);
        System.out.println("Aplicando escala: " + this.escala + " pixels/μm");
        
        // Simula tempo de captura
        try {
            Thread.sleep(500); // Simula meio segundo de captura
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String nomeImagem = "IMG_" + System.currentTimeMillis() + "_" + this.id + ".jpg";
        System.out.println("Imagem capturada com sucesso: " + nomeImagem);
        
        return nomeImagem;
    }

    /**
     * Captura uma imagem com configurações específicas.
     * Sobrecarga do método capturarImagem com parâmetros personalizados.
     * 
     * @param magnificacao Nível de magnificação desejado
     * @return String com informações sobre a imagem capturada
     */
    public String capturarImagem(int magnificacao) {
        System.out.println("Capturando imagem com magnificação " + magnificacao + "x");
        String nomeImagem = capturarImagem();
        return nomeImagem.replace(".jpg", "_" + magnificacao + "x.jpg");
    }

    /**
     * Captura uma imagem com configurações específicas e nome personalizado.
     * Sobrecarga do método capturarImagem com mais parâmetros.
     * 
     * @param magnificacao Nível de magnificação desejado
     * @param nomePersonalizado Nome personalizado para a imagem
     * @return String com o nome final da imagem
     */
    public String capturarImagem(int magnificacao, String nomePersonalizado) {
        System.out.println("Capturando imagem personalizada: " + nomePersonalizado);
        System.out.println("Magnificação: " + magnificacao + "x");
        
        // Simula captura personalizada
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String nomeCompleto = nomePersonalizado + "_" + magnificacao + "x_" + this.id + ".jpg";
        System.out.println("Imagem capturada: " + nomeCompleto);
        return nomeCompleto;
    }

    /**
     * Calibra o microscópio ajustando a escala.
     * 
     * @param novaEscala Nova escala a ser configurada
     * @return boolean true se a calibração foi bem-sucedida
     */
    public boolean calibrar(double novaEscala) {
        if (novaEscala > 0) {
            double escalaAnterior = this.escala;
            this.escala = novaEscala;
            System.out.println("Microscópio " + this.id + " calibrado:");
            System.out.println("Escala anterior: " + String.format("%.2f", escalaAnterior) + " pixels/μm");
            System.out.println("Nova escala: " + String.format("%.2f", this.escala) + " pixels/μm");
            return true;
        }
        System.out.println("Erro: Escala deve ser maior que zero");
        return false;
    }

    /**
     * Converte pixels para micrômetros usando a escala do microscópio.
     * 
     * @param pixels Número de pixels a converter
     * @return double valor convertido em micrômetros
     */
    public double converterPixelsParaMicrometros(int pixels) {
        if (this.escala > 0) {
            return pixels / this.escala;
        }
        return 0.0;
    }

    /**
     * Converte micrômetros para pixels usando a escala do microscópio.
     * 
     * @param micrometros Valor em micrômetros a converter
     * @return int valor convertido em pixels
     */
    public int converterMicrometrosParaPixels(double micrometros) {
        return (int) Math.round(micrometros * this.escala);
    }

    /**
     * Calcula o número total de pixels baseado na resolução.
     * 
     * @return long número total de pixels
     */
    private long calcularPixelsTotal() {
        if (this.resolucao != null && this.resolucao.contains("x")) {
            String[] dimensoes = this.resolucao.split("x");
            try {
                int largura = Integer.parseInt(dimensoes[0].trim());
                int altura = Integer.parseInt(dimensoes[1].trim());
                return (long) largura * altura;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Avalia a qualidade do microscópio baseada na resolução e escala.
     * 
     * @return String descrição da qualidade
     */
    private String avaliarQualidade() {
        long totalPixels = calcularPixelsTotal();
        
        if (totalPixels >= 8000000 && this.escala >= 10.0) { // 8MP+ e escala alta
            return "Excelente";
        } else if (totalPixels >= 2000000 && this.escala >= 5.0) { // 2MP+ e escala média
            return "Boa";
        } else if (totalPixels >= 1000000 && this.escala >= 2.0) { // 1MP+ e escala básica
            return "Regular";
        } else {
            return "Básica";
        }
    }

    /**
     * Verifica se o microscópio está funcionando corretamente.
     * 
     * @return boolean true se todos os parâmetros estão válidos
     */
    public boolean verificarFuncionamento() {
        boolean idValido = this.id != null && !this.id.trim().isEmpty();
        boolean modeloValido = this.modelo != null && !this.modelo.trim().isEmpty();
        boolean resolucaoValida = this.resolucao != null && this.resolucao.contains("x");
        boolean escalaValida = this.escala > 0;
        
        if (idValido && modeloValido && resolucaoValida && escalaValida) {
            System.out.println("Microscópio " + this.modelo + " funcionando corretamente");
            return true;
        } else {
            System.out.println("Problemas detectados no microscópio " + this.modelo);
            return false;
        }
    }

    /**
     * Retorna uma representação em string do microscópio.
     * 
     * @return String formatada com as informações principais do microscópio
     */
    @Override
    public String toString() {
        return String.format("DigitalMicroscope{id='%s', modelo='%s', resolucao='%s', escala=%.2f}", 
                           id, modelo, resolucao, escala);
    }
}