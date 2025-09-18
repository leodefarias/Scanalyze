package br.com.micromedicao.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe que representa uma imagem microscópica no sistema.
 * Esta classe encapsula as informações de uma imagem capturada,
 * incluindo dados do arquivo, microscópio utilizado e metadados.
 * 
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class MicroscopyImage {
    
    /**
     * Identificador único da imagem
     */
    private String id;
    
    /**
     * Nome do arquivo da imagem
     */
    private String arquivo;
    
    /**
     * Microscópio usado para capturar a imagem
     */
    private DigitalMicroscope microscopio;
    
    /**
     * Data e hora da captura da imagem
     */
    private LocalDateTime dataCaptura;

    /**
     * Construtor completo para criar uma nova imagem microscópica.
     * 
     * @param id Identificador único da imagem
     * @param arquivo Nome do arquivo da imagem
     * @param microscopio Microscópio utilizado para captura
     * @param dataCaptura Data e hora da captura
     */
    public MicroscopyImage(String id, String arquivo, DigitalMicroscope microscopio, LocalDateTime dataCaptura) {
        this.id = id;
        this.arquivo = arquivo;
        this.microscopio = microscopio;
        this.dataCaptura = dataCaptura;
    }

    /**
     * Construtor alternativo com data atual.
     * 
     * @param id Identificador único da imagem
     * @param arquivo Nome do arquivo da imagem
     * @param microscopio Microscópio utilizado para captura
     */
    public MicroscopyImage(String id, String arquivo, DigitalMicroscope microscopio) {
        this(id, arquivo, microscopio, LocalDateTime.now());
    }

    // Getters e Setters

    /**
     * Obtém o identificador da imagem.
     * 
     * @return String com o ID da imagem
     */
    public String getId() {
        return id;
    }

    /**
     * Define o identificador da imagem.
     * 
     * @param id Novo identificador da imagem
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtém o nome do arquivo da imagem.
     * 
     * @return String com o nome do arquivo
     */
    public String getArquivo() {
        return arquivo;
    }

    /**
     * Define o nome do arquivo da imagem.
     * 
     * @param arquivo Novo nome do arquivo
     */
    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    /**
     * Obtém o microscópio associado à imagem.
     * 
     * @return DigitalMicroscope objeto do microscópio
     */
    public DigitalMicroscope getMicroscopio() {
        return microscopio;
    }

    /**
     * Define o microscópio associado à imagem.
     * 
     * @param microscopio Novo microscópio a ser associado
     */
    public void setMicroscopio(DigitalMicroscope microscopio) {
        this.microscopio = microscopio;
    }

    /**
     * Obtém a data de captura da imagem.
     * 
     * @return LocalDateTime da captura
     */
    public LocalDateTime getDataCaptura() {
        return dataCaptura;
    }

    /**
     * Define a data de captura da imagem.
     * 
     * @param dataCaptura Nova data de captura
     */
    public void setDataCaptura(LocalDateTime dataCaptura) {
        this.dataCaptura = dataCaptura;
    }

    // Métodos de funcionalidade

    /**
     * Exibe os detalhes completos da imagem no console.
     * Este método formata e apresenta todas as informações da imagem
     * de forma legível para o usuário.
     */
    public void exibirDetalhes() {
        System.out.println("=== DETALHES DA IMAGEM ===");
        System.out.println("ID: " + this.id);
        System.out.println("Arquivo: " + this.arquivo);
        System.out.println("Data de Captura: " + this.dataCaptura.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        if (this.microscopio != null) {
            System.out.println("Microscópio: " + this.microscopio.getModelo() + " (ID: " + this.microscopio.getId() + ")");
            System.out.println("Resolução: " + this.microscopio.getResolucao());
            System.out.println("Escala: " + String.format("%.2f", this.microscopio.getEscala()) + " pixels/μm");
        }
        System.out.println("Formato: " + obterFormatoArquivo());
        System.out.println("Status: " + verificarIntegridade());
        System.out.println("========================");
    }

    /**
     * Associa a imagem a um microscópio específico.
     * Este método permite associar ou reasociar uma imagem a um microscópio.
     * 
     * @param novoMicroscopio Microscópio a ser associado
     * @return boolean true se a associação foi bem-sucedida
     */
    public boolean associarMicroscopio(DigitalMicroscope novoMicroscopio) {
        if (novoMicroscopio != null && novoMicroscopio.getId() != null) {
            DigitalMicroscope microscopioAnterior = this.microscopio;
            this.microscopio = novoMicroscopio;
            
            System.out.println("Imagem " + this.id + " associada ao microscópio " + novoMicroscopio.getModelo());
            if (microscopioAnterior != null) {
                System.out.println("Microscópio anterior: " + microscopioAnterior.getModelo());
            }
            return true;
        }
        System.out.println("Erro: Microscópio inválido para associação");
        return false;
    }

    /**
     * Calcula a área em pixels de uma região específica.
     * Método básico que simula o cálculo de área em pixels.
     * 
     * @param larguraPixels Largura da região em pixels
     * @param alturaPixels Altura da região em pixels
     * @return int área calculada em pixels
     */
    public int calcularArea(int larguraPixels, int alturaPixels) {
        if (larguraPixels > 0 && alturaPixels > 0) {
            int area = larguraPixels * alturaPixels;
            System.out.println("Área calculada: " + area + " pixels²");
            return area;
        }
        return 0;
    }

    /**
     * Calcula a área convertida para micrômetros quadrados.
     * Sobrecarga do método calcularArea com conversão de unidade.
     * 
     * @param larguraPixels Largura da região em pixels
     * @param alturaPixels Altura da região em pixels
     * @return double área em micrômetros quadrados
     */
    public double calcularArea(int larguraPixels, int alturaPixels, boolean converterParaMicrometros) {
        int areaPixels = calcularArea(larguraPixels, alturaPixels);
        
        if (converterParaMicrometros && this.microscopio != null && this.microscopio.getEscala() > 0) {
            double escala = this.microscopio.getEscala();
            double areaMicrometros = areaPixels / (escala * escala);
            System.out.println("Área convertida: " + String.format("%.2f", areaMicrometros) + " μm²");
            return areaMicrometros;
        }
        
        return areaPixels;
    }

    /**
     * Verifica a integridade da imagem.
     * Valida se a imagem possui todas as informações necessárias.
     * 
     * @return String status da integridade
     */
    public String verificarIntegridade() {
        boolean arquivoValido = this.arquivo != null && !this.arquivo.trim().isEmpty();
        boolean microscopioValido = this.microscopio != null;
        boolean dataValida = this.dataCaptura != null;
        boolean formatoValido = validarFormatoArquivo();
        
        if (arquivoValido && microscopioValido && dataValida && formatoValido) {
            return "Íntegra";
        } else {
            StringBuilder problemas = new StringBuilder("Problemas: ");
            if (!arquivoValido) problemas.append("arquivo inválido, ");
            if (!microscopioValido) problemas.append("microscópio não associado, ");
            if (!dataValida) problemas.append("data inválida, ");
            if (!formatoValido) problemas.append("formato não suportado, ");
            return problemas.toString().replaceAll(", $", "");
        }
    }

    /**
     * Obtém o formato do arquivo da imagem.
     * 
     * @return String formato do arquivo
     */
    public String obterFormatoArquivo() {
        if (this.arquivo != null && this.arquivo.contains(".")) {
            return this.arquivo.substring(this.arquivo.lastIndexOf(".") + 1).toUpperCase();
        }
        return "Desconhecido";
    }

    /**
     * Valida se o formato do arquivo é suportado.
     * 
     * @return boolean true se o formato é suportado
     */
    private boolean validarFormatoArquivo() {
        String formato = obterFormatoArquivo().toLowerCase();
        return formato.equals("jpg") || formato.equals("jpeg") || 
               formato.equals("png") || formato.equals("bmp") || 
               formato.equals("tiff") || formato.equals("tif");
    }

    /**
     * Gera um novo nome de arquivo baseado em convenções.
     * 
     * @param prefixo Prefixo a ser usado no nome
     * @return String novo nome do arquivo
     */
    public String gerarNovoNomeArquivo(String prefixo) {
        String formato = obterFormatoArquivo().toLowerCase();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String novoNome = prefixo + "_" + timestamp + "." + formato;
        
        System.out.println("Novo nome gerado: " + novoNome);
        return novoNome;
    }

    /**
     * Verifica se a imagem foi capturada recentemente.
     * Considera como recente imagens capturadas na última hora.
     * 
     * @return boolean true se foi capturada na última hora
     */
    public boolean isCapturaRecente() {
        return this.dataCaptura.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Calcula a idade da imagem em horas.
     * 
     * @return long número de horas desde a captura
     */
    public long calcularIdadeEmHoras() {
        return java.time.Duration.between(this.dataCaptura, LocalDateTime.now()).toHours();
    }

    /**
     * Obtém informações técnicas da imagem baseadas no microscópio.
     * 
     * @return String informações técnicas formatadas
     */
    public String obterInformacoesTecnicas() {
        if (this.microscopio != null) {
            return String.format("Resolução: %s | Escala: %.2f pixels/μm | Qualidade: %s", 
                                this.microscopio.getResolucao(),
                                this.microscopio.getEscala(),
                                this.microscopio.toString().contains("Excelente") ? "Alta" : "Padrão");
        }
        return "Informações técnicas não disponíveis";
    }

    /**
     * Retorna uma representação em string da imagem.
     * 
     * @return String formatada com as informações principais da imagem
     */
    @Override
    public String toString() {
        return String.format("MicroscopyImage{id='%s', arquivo='%s', dataCaptura=%s, microscopio='%s'}", 
                           id, arquivo, 
                           dataCaptura.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                           microscopio != null ? microscopio.getModelo() : "N/A");
    }
}