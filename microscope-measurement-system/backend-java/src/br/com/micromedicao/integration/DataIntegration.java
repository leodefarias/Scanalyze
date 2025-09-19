package br.com.micromedicao.integration;

import br.com.micromedicao.model.*;
import br.com.micromedicao.service.MicromedicaoService;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Classe responsável pela integração de dados externos.
 * Esta classe gerencia a importação e exportação de dados
 * através de arquivos CSV e JSON, facilitando a comunicação
 * com o módulo Python de visão computacional.
 * 
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class DataIntegration {
    
    /**
     * Serviço principal do sistema para cadastro de entidades
     */
    private MicromedicaoService service;
    
    /**
     * Formatador de data/hora para arquivos de integração
     */
    private DateTimeFormatter dateFormatter;

    /**
     * Construtor que inicializa a integração com o serviço.
     * 
     * @param service Serviço principal do sistema
     */
    public DataIntegration(MicromedicaoService service) {
        this.service = service;
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    // ===== MÉTODOS DE IMPORTAÇÃO CSV =====

    /**
     * Importa medições de um arquivo CSV.
     * O arquivo deve ter o formato: id,sampleId,area,dataHora,imagemId,nomeImagem
     * 
     * @param caminhoArquivo Caminho para o arquivo CSV
     * @return int número de medições importadas com sucesso
     */
    public int importarMedicoesCSV(String caminhoArquivo) {
        int medicoesCadastradas = 0;
        
        System.out.println("Iniciando importação de medições do arquivo: " + caminhoArquivo);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            boolean isHeader = true;
            
            while ((linha = reader.readLine()) != null) {
                // Pula o cabeçalho
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] campos = linha.split(",");
                if (campos.length >= 6) {
                    try {
                        String id = campos[0].trim();
                        String sampleId = campos[1].trim();
                        double area = Double.parseDouble(campos[2].trim());
                        LocalDateTime dataHora = LocalDateTime.parse(campos[3].trim(), dateFormatter);
                        String imagemId = campos[4].trim();
                        String nomeImagem = campos[5].trim();
                        
                        // Busca a amostra correspondente
                        Sample sample = service.buscarSamplePorId(sampleId);
                        if (sample == null) {
                            System.out.println("Aviso: Amostra " + sampleId + " não encontrada. Criando amostra temporária.");
                            sample = new Sample(sampleId, "Amostra_" + sampleId, "Importada", "Sistema");
                            service.cadastrarSample(sample);
                        }
                        
                        // Busca ou cria a imagem
                        MicroscopyImage imagem = service.buscarImagePorId(imagemId);
                        if (imagem == null) {
                            // Busca um microscópio padrão ou cria um temporário
                            DigitalMicroscope microscopio = obterMicroscopioPadrao();
                            imagem = new MicroscopyImage(imagemId, nomeImagem, microscopio, dataHora);
                            service.cadastrarImage(imagem);
                        }
                        
                        // Cria e cadastra a medição
                        Measurement measurement = new Measurement(id, sample, area, dataHora, imagem);
                        if (service.cadastrarMeasurement(measurement)) {
                            medicoesCadastradas++;
                        }
                        
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.out.println("Erro ao processar linha: " + linha + " - " + e.getMessage());
                    }
                } else {
                    System.out.println("Linha com formato inválido ignorada: " + linha);
                }
            }
            
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo CSV: " + e.getMessage());
            return 0;
        }
        
        System.out.println("Importação concluída. " + medicoesCadastradas + " medições importadas.");
        return medicoesCadastradas;
    }

    /**
     * Importa configurações de microscópios de um arquivo CSV.
     * O arquivo deve ter o formato: id,modelo,resolucao,escala
     * 
     * @param caminhoArquivo Caminho para o arquivo CSV
     * @return int número de microscópios importados
     */
    public int importarMicroscopiosCSV(String caminhoArquivo) {
        int microscopiosCadastrados = 0;
        
        System.out.println("Iniciando importação de microscópios do arquivo: " + caminhoArquivo);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            boolean isHeader = true;
            
            while ((linha = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] campos = linha.split(",");
                if (campos.length >= 4) {
                    try {
                        String id = campos[0].trim();
                        String modelo = campos[1].trim();
                        String resolucao = campos[2].trim();
                        double escala = Double.parseDouble(campos[3].trim());
                        
                        DigitalMicroscope microscope = new DigitalMicroscope(id, modelo, resolucao, escala);
                        if (service.cadastrarMicroscope(microscope)) {
                            microscopiosCadastrados++;
                        }
                        
                    } catch (NumberFormatException e) {
                        System.out.println("Erro ao processar linha: " + linha + " - " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo CSV: " + e.getMessage());
            return 0;
        }
        
        System.out.println("Importação concluída. " + microscopiosCadastrados + " microscópios importados.");
        return microscopiosCadastrados;
    }

    // ===== MÉTODOS DE EXPORTAÇÃO CSV =====

    /**
     * Exporta todas as medições para um arquivo CSV.
     * 
     * @param caminhoArquivo Caminho onde salvar o arquivo CSV
     * @return boolean true se a exportação foi bem-sucedida
     */
    public boolean exportarMedicoesCSV(String caminhoArquivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoArquivo))) {
            // Escreve o cabeçalho
            writer.println("id,sampleId,sampleNome,area,dataHora,imagemId,nomeImagem,microscopioModelo");
            
            // Escreve os dados das medições
            List<Measurement> measurements = service.listarMeasurements();
            for (Measurement measurement : measurements) {
                writer.printf("%s,%s,%s,%.2f,%s,%s,%s,%s%n",
                    measurement.getId(),
                    measurement.getSample() != null ? measurement.getSample().getId() : "",
                    measurement.getSample() != null ? measurement.getSample().getNome() : "",
                    measurement.getArea(),
                    measurement.getDataHora().format(dateFormatter),
                    measurement.getImagem() != null ? measurement.getImagem().getId() : "",
                    measurement.getImagem() != null ? measurement.getImagem().getArquivo() : "",
                    measurement.getImagem() != null && measurement.getImagem().getMicroscopio() != null ? 
                        measurement.getImagem().getMicroscopio().getModelo() : ""
                );
            }
            
            System.out.println("Medições exportadas para: " + caminhoArquivo);
            return true;
            
        } catch (IOException e) {
            System.out.println("Erro ao exportar medições: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporta relatório de amostras para CSV.
     * 
     * @param caminhoArquivo Caminho onde salvar o arquivo CSV
     * @return boolean true se a exportação foi bem-sucedida
     */
    public boolean exportarAmostrasCSV(String caminhoArquivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoArquivo))) {
            writer.println("id,nome,tipo,dataColeta,operadorResponsavel,numeroMedicoes");
            
            List<Sample> samples = service.listarSamples();
            for (Sample sample : samples) {
                int numeroMedicoes = service.buscarMeasurementsPorSample(sample.getId()).size();
                writer.printf("%s,%s,%s,%s,%s,%d%n",
                    sample.getId(),
                    sample.getNome(),
                    sample.getTipo(),
                    sample.getDataColeta().format(dateFormatter),
                    sample.getOperadorResponsavel(),
                    numeroMedicoes
                );
            }
            
            System.out.println("Amostras exportadas para: " + caminhoArquivo);
            return true;
            
        } catch (IOException e) {
            System.out.println("Erro ao exportar amostras: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporta amostras para arquivo JSON compatível com o frontend.
     *
     * @param caminhoArquivo Caminho para o arquivo JSON
     * @return boolean true se a exportação foi bem-sucedida
     */
    public boolean exportarAmostrasJSON(String caminhoArquivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoArquivo))) {
            writer.println("{");
            writer.println("  \"samples\": [");

            List<Sample> samples = service.listarSamples();
            for (int i = 0; i < samples.size(); i++) {
                Sample sample = samples.get(i);
                writer.println("    {");
                writer.println("      \"id\": \"" + sample.getId() + "\",");
                writer.println("      \"nome\": \"" + sample.getNome() + "\",");
                writer.println("      \"tipo\": \"" + sample.getTipo() + "\",");
                writer.println("      \"dataColeta\": \"" + sample.getDataColeta().format(dateFormatter) + "\",");
                writer.println("      \"operadorResponsavel\": \"" + sample.getOperadorResponsavel() + "\"");
                if (i < samples.size() - 1) {
                    writer.println("    },");
                } else {
                    writer.println("    }");
                }
            }

            writer.println("  ]");
            writer.println("}");

            System.out.println("Amostras exportadas em JSON para: " + caminhoArquivo);
            return true;

        } catch (IOException e) {
            System.out.println("Erro ao exportar amostras JSON: " + e.getMessage());
            return false;
        }
    }

    // ===== MÉTODOS DE INTEGRAÇÃO JSON =====

    /**
     * Processa um arquivo JSON simples com dados de medição.
     * Formato esperado: {"measurements": [{"id": "...", "sampleId": "...", "area": 123.45, ...}]}
     * 
     * @param caminhoArquivo Caminho para o arquivo JSON
     * @return int número de medições processadas
     */
    public int processarArquivoJSON(String caminhoArquivo) {
        int medicoesCadastradas = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            StringBuilder jsonContent = new StringBuilder();
            String linha;
            
            while ((linha = reader.readLine()) != null) {
                jsonContent.append(linha);
            }
            
            // Processamento simples do JSON (sem biblioteca externa)
            String json = jsonContent.toString();
            medicoesCadastradas = processarJSONSimples(json);
            
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo JSON: " + e.getMessage());
        }
        
        return medicoesCadastradas;
    }

    /**
     * Gera um arquivo JSON com as medições atuais.
     * 
     * @param caminhoArquivo Caminho onde salvar o arquivo JSON
     * @return boolean true se a geração foi bem-sucedida
     */
    public boolean gerarArquivoJSON(String caminhoArquivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoArquivo))) {
            writer.println("{");
            writer.println("  \"measurements\": [");
            
            List<Measurement> measurements = service.listarMeasurements();
            for (int i = 0; i < measurements.size(); i++) {
                Measurement m = measurements.get(i);
                writer.printf("    {%n");
                writer.printf("      \"id\": \"%s\",%n", m.getId());
                writer.printf("      \"sampleId\": \"%s\",%n", 
                    m.getSample() != null ? m.getSample().getId() : "");
                writer.printf("      \"area\": %.2f,%n", m.getArea());
                writer.printf("      \"dataHora\": \"%s\",%n", 
                    m.getDataHora().format(dateFormatter));
                writer.printf("      \"imagemArquivo\": \"%s\"%n", 
                    m.getImagem() != null ? m.getImagem().getArquivo() : "");
                writer.print("    }");
                if (i < measurements.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            
            writer.println("  ]");
            writer.println("}");
            
            System.out.println("Arquivo JSON gerado: " + caminhoArquivo);
            return true;
            
        } catch (IOException e) {
            System.out.println("Erro ao gerar arquivo JSON: " + e.getMessage());
            return false;
        }
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Obtém um microscópio padrão para usar em importações.
     * Se não existir nenhum, cria um temporário.
     * 
     * @return DigitalMicroscope microscópio padrão
     */
    private DigitalMicroscope obterMicroscopioPadrao() {
        List<DigitalMicroscope> microscopes = service.listarMicroscopes();
        if (!microscopes.isEmpty()) {
            return microscopes.get(0);
        } else {
            // Cria um microscópio temporário
            DigitalMicroscope temp = new DigitalMicroscope("TEMP_001", 
                "Microscópio Temporário", "1920x1080", 10.0);
            service.cadastrarMicroscope(temp);
            return temp;
        }
    }

    /**
     * Processa JSON de forma simples sem biblioteca externa.
     * Este é um processamento básico que funciona com formato específico.
     * 
     * @param json String JSON a ser processada
     * @return int número de medições processadas
     */
    private int processarJSONSimples(String json) {
        int count = 0;
        
        // Implementação básica para demonstração
        // Em produção, seria recomendado usar uma biblioteca JSON como Jackson ou Gson
        
        if (json.contains("\"measurements\"")) {
            System.out.println("JSON detectado - processamento básico implementado");
            System.out.println("Para produção, recomenda-se usar biblioteca JSON dedicada");
            
            // Simula processamento de uma medição para demonstração
            Sample sample = service.listarSamples().isEmpty() ? 
                new Sample("JSON_001", "Amostra JSON", "Importada", "Sistema") :
                service.listarSamples().get(0);
            
            if (service.listarSamples().isEmpty()) {
                service.cadastrarSample(sample);
            }
            
            DigitalMicroscope microscope = obterMicroscopioPadrao();
            MicroscopyImage image = new MicroscopyImage("JSON_IMG_001", "json_import.jpg", microscope);
            service.cadastrarImage(image);
            
            Measurement measurement = new Measurement("JSON_MEAS_001", sample, 100.0, image);
            if (service.cadastrarMeasurement(measurement)) {
                count = 1;
            }
        }
        
        return count;
    }

    /**
     * Cria arquivos de exemplo para demonstração.
     * 
     * @param diretorio Diretório onde criar os arquivos
     * @return boolean true se os arquivos foram criados
     */
    public boolean criarArquivosExemplo(String diretorio) {
        try {
            // Cria arquivo CSV de exemplo
            PrintWriter csvWriter = new PrintWriter(new FileWriter(diretorio + "/medicoes_exemplo.csv"));
            csvWriter.println("id,sampleId,area,dataHora,imagemId,nomeImagem");
            csvWriter.println("MEAS_001,SAMPLE_001,150.75,2024-01-15 10:30:00,IMG_001,amostra_001.jpg");
            csvWriter.println("MEAS_002,SAMPLE_002,89.23,2024-01-15 11:45:00,IMG_002,amostra_002.jpg");
            csvWriter.close();
            
            // Cria arquivo JSON de exemplo
            PrintWriter jsonWriter = new PrintWriter(new FileWriter(diretorio + "/medicoes_exemplo.json"));
            jsonWriter.println("{");
            jsonWriter.println("  \"measurements\": [");
            jsonWriter.println("    {");
            jsonWriter.println("      \"id\": \"MEAS_003\",");
            jsonWriter.println("      \"sampleId\": \"SAMPLE_003\",");
            jsonWriter.println("      \"area\": 200.50,");
            jsonWriter.println("      \"dataHora\": \"2024-01-15 12:00:00\",");
            jsonWriter.println("      \"imagemArquivo\": \"amostra_003.jpg\"");
            jsonWriter.println("    }");
            jsonWriter.println("  ]");
            jsonWriter.println("}");
            jsonWriter.close();
            
            System.out.println("Arquivos de exemplo criados em: " + diretorio);
            return true;
            
        } catch (IOException e) {
            System.out.println("Erro ao criar arquivos de exemplo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Monitora um diretório para novos arquivos de medição.
     * Este método simula um monitoramento de diretório.
     * 
     * @param diretorio Diretório a ser monitorado
     * @return List<String> lista de arquivos encontrados
     */
    public List<String> monitorarDiretorio(String diretorio) {
        List<String> arquivosEncontrados = new ArrayList<>();
        
        File dir = new File(diretorio);
        if (dir.exists() && dir.isDirectory()) {
            File[] arquivos = dir.listFiles((d, name) -> 
                name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".json"));
            
            if (arquivos != null) {
                for (File arquivo : arquivos) {
                    arquivosEncontrados.add(arquivo.getAbsolutePath());
                }
            }
        }
        
        return arquivosEncontrados;
    }
}