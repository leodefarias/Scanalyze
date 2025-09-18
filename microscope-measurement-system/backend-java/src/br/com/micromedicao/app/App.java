package br.com.micromedicao.app;

import br.com.micromedicao.model.*;
import br.com.micromedicao.service.MicromedicaoService;
import br.com.micromedicao.integration.DataIntegration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Classe principal executora do sistema de micromedição.
 * Esta classe demonstra o funcionamento completo do sistema,
 * testando todas as funcionalidades implementadas e simulando
 * a integração com o módulo Python de visão computacional.
 * 
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class App {
    
    /**
     * Serviço principal do sistema
     */
    private static MicromedicaoService service;
    
    /**
     * Módulo de integração de dados
     */
    private static DataIntegration integration;

    /**
     * Método principal que executa a demonstração do sistema.
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("    SISTEMA DE MICROMEDIÇÃO AUTOMATIZADA");
        System.out.println("         Backend Java - Demonstração");
        System.out.println("==============================================\n");
        
        // Inicializa os serviços
        inicializarSistema();
        
        // Executa as demonstrações
        demonstrarCadastroOperadores();
        demonstrarCadastroMicroscopios();
        demonstrarCadastroAmostras();
        demonstrarCapturaImagens();
        demonstrarRealizacaoMedicoes();
        demonstrarIntegracaoArquivos();
        demonstrarRelatorios();
        demonstrarValidacoes();
        
        // Finaliza com relatório geral
        finalizarDemonstracao();
    }

    /**
     * Inicializa o sistema criando as instâncias dos serviços.
     */
    private static void inicializarSistema() {
        System.out.println(">>> Inicializando Sistema...");
        service = new MicromedicaoService();
        integration = new DataIntegration(service);
        System.out.println("Sistema inicializado com sucesso!\n");
    }

    /**
     * Demonstra o cadastro de operadores no sistema.
     */
    private static void demonstrarCadastroOperadores() {
        System.out.println("=== DEMONSTRAÇÃO: Cadastro de Operadores ===");
        
        // Cria operadores com diferentes níveis de acesso
        Operator admin = new Operator("OP001", "Dr. João Silva", "joao.silva@lab.com", "ADMIN");
        Operator tecnico = new Operator("OP002", "Maria Santos", "maria.santos@lab.com", "TECNICO");
        Operator operador = new Operator("OP003", "Carlos Oliveira", "carlos.oliveira@lab.com", "OPERADOR");
        
        // Cadastra os operadores
        service.cadastrarOperator(admin);
        service.cadastrarOperator(tecnico);
        service.cadastrarOperator(operador);
        
        // Demonstra funcionalidades dos operadores
        System.out.println("\n--- Testando Funcionalidades dos Operadores ---");
        admin.exibirDetalhes();
        admin.autenticar();
        System.out.println("Admin tem permissão para RELATORIO? " + admin.temPermissao("RELATORIO"));
        
        tecnico.exibirDetalhes();
        tecnico.autenticar("maria.santos@lab.com", "senha123");
        
        operador.atualizarNivelAcesso("TECNICO");
        System.out.println();
    }

    /**
     * Demonstra o cadastro de microscópios digitais.
     */
    private static void demonstrarCadastroMicroscopios() {
        System.out.println("=== DEMONSTRAÇÃO: Cadastro de Microscópios ===");
        
        // Cria microscópios com diferentes especificações
        DigitalMicroscope micro1 = new DigitalMicroscope("MIC001", "Olympus BX53", "1920x1080", 12.5);
        DigitalMicroscope micro2 = new DigitalMicroscope("MIC002", "Nikon Eclipse E200", "1280x720", 8.0);
        DigitalMicroscope micro3 = new DigitalMicroscope("MIC003", "Zeiss Axioskop 2", "2048x1536", 15.0);
        
        // Cadastra os microscópios
        service.cadastrarMicroscope(micro1);
        service.cadastrarMicroscope(micro2);
        service.cadastrarMicroscope(micro3);
        
        // Demonstra funcionalidades dos microscópios
        System.out.println("\n--- Testando Funcionalidades dos Microscópios ---");
        micro1.exibirDetalhes();
        String imagem1 = micro1.capturarImagem();
        String imagem2 = micro1.capturarImagem(400);
        String imagem3 = micro1.capturarImagem(1000, "amostra_especial");
        
        micro2.calibrar(10.0);
        System.out.println("Conversão 100 pixels = " + micro2.converterPixelsParaMicrometros(100) + " μm");
        System.out.println();
    }

    /**
     * Demonstra o cadastro de amostras para análise.
     */
    private static void demonstrarCadastroAmostras() {
        System.out.println("=== DEMONSTRAÇÃO: Cadastro de Amostras ===");
        
        // Cria amostras de diferentes tipos
        Sample amostra1 = new Sample("SAMP001", "Sangue Paciente A", "Sangue", "Dr. João Silva");
        Sample amostra2 = new Sample("SAMP002", "Tecido Muscular", "Tecido", "Maria Santos");
        Sample amostra3 = new Sample("SAMP003", "Célula Neural", "Neurônio", 
                                   LocalDateTime.now().minusHours(2), "Carlos Oliveira");
        
        // Cadastra as amostras
        service.cadastrarSample(amostra1);
        service.cadastrarSample(amostra2);
        service.cadastrarSample(amostra3);
        
        // Demonstra funcionalidades das amostras
        System.out.println("\n--- Testando Funcionalidades das Amostras ---");
        amostra1.exibirDetalhes();
        amostra1.atualizarTipo("Sangue Humano");
        System.out.println("Amostra 1 é recente? " + amostra1.isColetaRecente());
        System.out.println("Idade da Amostra 3: " + amostra3.calcularIdadeEmHoras() + " horas");
        
        // Busca amostras por tipo
        List<Sample> amostrasSegue = service.buscarSamplesPorTipo("Sangue Humano");
        System.out.println("Amostras de sangue encontradas: " + amostrasSegue.size());
        System.out.println();
    }

    /**
     * Demonstra a captura e cadastro de imagens microscópicas.
     */
    private static void demonstrarCapturaImagens() {
        System.out.println("=== DEMONSTRAÇÃO: Captura de Imagens ===");
        
        // Busca microscópios cadastrados
        List<DigitalMicroscope> microscopios = service.listarMicroscopes();
        
        if (!microscopios.isEmpty()) {
            DigitalMicroscope microscopio = microscopios.get(0);
            
            // Simula captura de imagens
            String nomeArquivo1 = microscopio.capturarImagem(200, "amostra_001");
            String nomeArquivo2 = microscopio.capturarImagem(400, "amostra_002");
            String nomeArquivo3 = microscopio.capturarImagem(600, "amostra_003");
            
            // Cria objetos de imagem e cadastra
            MicroscopyImage img1 = new MicroscopyImage("IMG001", nomeArquivo1, microscopio);
            MicroscopyImage img2 = new MicroscopyImage("IMG002", nomeArquivo2, microscopio);
            MicroscopyImage img3 = new MicroscopyImage("IMG003", nomeArquivo3, microscopio);
            
            service.cadastrarImage(img1);
            service.cadastrarImage(img2);
            service.cadastrarImage(img3);
            
            // Demonstra funcionalidades das imagens
            System.out.println("\n--- Testando Funcionalidades das Imagens ---");
            img1.exibirDetalhes();
            int area = img1.calcularArea(150, 200);
            double areaMicrometros = img1.calcularArea(150, 200, true);
            
            img2.associarMicroscopio(microscopios.size() > 1 ? microscopios.get(1) : microscopio);
            System.out.println("Imagem 3 é recente? " + img3.isCapturaRecente());
        }
        System.out.println();
    }

    /**
     * Demonstra a realização de medições nas amostras.
     */
    private static void demonstrarRealizacaoMedicoes() {
        System.out.println("=== DEMONSTRAÇÃO: Realização de Medições ===");
        
        // Busca entidades necessárias
        List<Sample> amostras = service.listarSamples();
        List<MicroscopyImage> imagens = service.listarImages();
        
        if (!amostras.isEmpty() && !imagens.isEmpty()) {
            // Cria medições simulando análise automática
            Measurement med1 = new Measurement("MEAS001", amostras.get(0), 156.75, imagens.get(0));
            Measurement med2 = new Measurement("MEAS002", amostras.get(Math.min(1, amostras.size()-1)), 
                                             89.32, imagens.get(Math.min(1, imagens.size()-1)));
            Measurement med3 = new Measurement("MEAS003", amostras.get(Math.min(2, amostras.size()-1)), 
                                             234.89, LocalDateTime.now().minusMinutes(30), 
                                             imagens.get(Math.min(2, imagens.size()-1)));
            
            // Cadastra as medições
            service.cadastrarMeasurement(med1);
            service.cadastrarMeasurement(med2);
            service.cadastrarMeasurement(med3);
            
            // Demonstra funcionalidades das medições
            System.out.println("\n--- Testando Funcionalidades das Medições ---");
            med1.exibirDetalhes();
            System.out.println("Área com escala 0.5: " + med1.calcularArea(0.5));
            System.out.println("Área em μm²: " + med1.calcularArea(0.01, "μm²"));
            System.out.println("Medição 1 é válida? " + med1.validarMedicao());
            System.out.println("Comparação med1 vs med2: " + med1.compararArea(med2));
            System.out.println("Medição 3 é recente? " + med3.isMedicaoRecente());
            
            // Busca medições por amostra
            List<Measurement> medicoesAmostra1 = service.buscarMeasurementsPorSample(amostras.get(0).getId());
            System.out.println("Medições da primeira amostra: " + medicoesAmostra1.size());
        }
        System.out.println();
    }

    /**
     * Demonstra a integração com arquivos CSV/JSON.
     */
    private static void demonstrarIntegracaoArquivos() {
        System.out.println("=== DEMONSTRAÇÃO: Integração de Arquivos ===");
        
        // Cria diretório para integração
        String dirIntegracao = "../data-integration";
        
        // Cria arquivos de exemplo
        System.out.println("Criando arquivos de exemplo...");
        integration.criarArquivosExemplo(dirIntegracao);
        
        // Exporta dados atuais
        System.out.println("\nExportando dados atuais...");
        integration.exportarMedicoesCSV(dirIntegracao + "/medicoes_export.csv");
        integration.exportarAmostrasCSV(dirIntegracao + "/amostras_export.csv");
        integration.gerarArquivoJSON(dirIntegracao + "/medicoes_export.json");
        
        // Simula importação (os arquivos podem não existir ainda)
        System.out.println("\nSimulando importação de dados...");
        try {
            int importadas = integration.importarMedicoesCSV(dirIntegracao + "/medicoes_exemplo.csv");
            System.out.println("Tentativa de importação concluída.");
        } catch (Exception e) {
            System.out.println("Arquivo de exemplo não encontrado - criando dados simulados");
            criarDadosSimuladosIntegracao();
        }
        
        // Monitora diretório
        List<String> arquivos = integration.monitorarDiretorio(dirIntegracao);
        System.out.println("Arquivos encontrados no diretório: " + arquivos.size());
        
        System.out.println();
    }

    /**
     * Demonstra a geração de relatórios e estatísticas.
     */
    private static void demonstrarRelatorios() {
        System.out.println("=== DEMONSTRAÇÃO: Relatórios e Estatísticas ===");
        
        // Gera relatório geral
        service.gerarRelatorioGeral();
        
        // Mostra estatísticas detalhadas
        System.out.println("\n--- Estatísticas Detalhadas ---");
        System.out.println("Área média das medições: " + String.format("%.2f", service.calcularAreaMedia()));
        
        Measurement maiorMedicao = service.encontrarMaiorMedicao();
        if (maiorMedicao != null) {
            System.out.println("Maior medição: " + maiorMedicao.getId() + 
                             " (Área: " + String.format("%.2f", maiorMedicao.getArea()) + ")");
        }
        
        // Lista resumida de todas as entidades
        System.out.println("\n--- Listagem Resumida ---");
        System.out.println("Operadores cadastrados:");
        for (Operator op : service.listarOperators()) {
            System.out.println("  - " + op.getNome() + " (" + op.getNivelAcesso() + ")");
        }
        
        System.out.println("Microscópios disponíveis:");
        for (DigitalMicroscope mic : service.listarMicroscopes()) {
            System.out.println("  - " + mic.getModelo() + " (" + mic.getResolucao() + ")");
        }
        
        System.out.println("Amostras cadastradas:");
        for (Sample sample : service.listarSamples()) {
            System.out.println("  - " + sample.getNome() + " (" + sample.getTipo() + ")");
        }
        
        System.out.println();
    }

    /**
     * Demonstra as validações e verificações de integridade.
     */
    private static void demonstrarValidacoes() {
        System.out.println("=== DEMONSTRAÇÃO: Validações e Integridade ===");
        
        // Valida integridade do sistema
        boolean sistemaIntegro = service.validarIntegridadeSistema();
        System.out.println("Sistema íntegro: " + sistemaIntegro);
        
        // Testa validações individuais
        System.out.println("\n--- Testando Validações Individuais ---");
        
        // Testa operador inválido
        Operator operadorInvalido = new Operator("", "", "email_invalido", "NIVEL_INEXISTENTE");
        System.out.println("Cadastro de operador inválido: " + service.cadastrarOperator(operadorInvalido));
        
        // Testa microscópio inválido
        DigitalMicroscope microscopioInvalido = new DigitalMicroscope("", "", "", -1.0);
        System.out.println("Cadastro de microscópio inválido: " + service.cadastrarMicroscope(microscopioInvalido));
        
        // Verifica funcionamento dos microscópios
        System.out.println("\n--- Verificando Funcionamento dos Microscópios ---");
        for (DigitalMicroscope mic : service.listarMicroscopes()) {
            mic.verificarFuncionamento();
        }
        
        System.out.println();
    }

    /**
     * Cria dados simulados para demonstrar a integração.
     */
    private static void criarDadosSimuladosIntegracao() {
        System.out.println("Criando dados simulados de integração...");
        
        // Simula dados vindos do módulo Python
        Sample samplePython = new Sample("PY_SAMP001", "Amostra Python", "Automática", "Sistema Python");
        service.cadastrarSample(samplePython);
        
        DigitalMicroscope micPython = service.listarMicroscopes().isEmpty() ? 
            new DigitalMicroscope("PY_MIC001", "Webcam USB", "640x480", 5.0) :
            service.listarMicroscopes().get(0);
        
        if (service.listarMicroscopes().isEmpty()) {
            service.cadastrarMicroscope(micPython);
        }
        
        MicroscopyImage imgPython = new MicroscopyImage("PY_IMG001", "python_capture.jpg", micPython);
        service.cadastrarImage(imgPython);
        
        Measurement medPython = new Measurement("PY_MEAS001", samplePython, 175.50, imgPython);
        service.cadastrarMeasurement(medPython);
        
        System.out.println("Dados simulados criados para demonstrar integração com Python");
    }

    /**
     * Finaliza a demonstração com um relatório final.
     */
    private static void finalizarDemonstracao() {
        System.out.println("=== DEMONSTRAÇÃO FINALIZADA ===");
        
        // Relatório final
        service.gerarRelatorioGeral();
        
        System.out.println("\n--- Próximos Passos ---");
        System.out.println("1. Executar módulo Python de visão computacional");
        System.out.println("2. Capturar imagens e registrar medições via interface Python");
        System.out.println("3. Importar dados gerados pelo Python usando DataIntegration");
        System.out.println("4. Visualizar resultados no dashboard web");
        
        System.out.println("\n--- Arquivos Gerados ---");
        System.out.println("- CSV de medições: ../data-integration/medicoes_export.csv");
        System.out.println("- CSV de amostras: ../data-integration/amostras_export.csv");
        System.out.println("- JSON de medições: ../data-integration/medicoes_export.json");
        
        System.out.println("\n==============================================");
        System.out.println("    DEMONSTRAÇÃO BACKEND JAVA CONCLUÍDA");
        System.out.println("     Todas as funcionalidades testadas!");
        System.out.println("==============================================");
    }
}