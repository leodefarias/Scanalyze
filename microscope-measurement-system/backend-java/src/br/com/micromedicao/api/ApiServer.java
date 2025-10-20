package br.com.micromedicao.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * Servidor HTTP para API REST do Sistema de Micromedição
 * Servidor leve sem dependências externas para desenvolvimento e testes
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class ApiServer {

    private HttpServer server;
    private RestApiController controller;
    private static final int PORT = 8081;

    /**
     * Construtor do servidor API
     */
    public ApiServer() {
        this.controller = new RestApiController();
    }

    /**
     * Inicia o servidor HTTP
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Configura endpoints da API
        server.createContext("/api/samples", new SamplesHandler());
        server.createContext("/api/measurements", new MeasurementsHandler());
        server.createContext("/api/operators", new OperatorsHandler());
        server.createContext("/api/microscopes", new MicroscopesHandler());
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/export", new ExportHandler());
        server.createContext("/api/images/", new ImagesHandler());
        server.createContext("/api/health", new HealthHandler());

        // Configura CORS para permitir acesso do frontend
        server.createContext("/", new CorsHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("==============================================");
        System.out.println("🚀 SCANALYZE API SERVER INICIADO");
        System.out.println("📡 Porta: " + PORT);
        System.out.println("🌐 URL Base: http://localhost:" + PORT);
        System.out.println("📋 Endpoints disponíveis:");
        System.out.println("   GET  /api/samples        - Lista amostras");
        System.out.println("   POST /api/samples        - Cadastra amostra");
        System.out.println("   GET  /api/measurements   - Lista medições");
        System.out.println("   POST /api/measurements   - Registra medição");
        System.out.println("   GET  /api/operators      - Lista operadores");
        System.out.println("   GET  /api/microscopes    - Lista microscópios");
        System.out.println("   GET  /api/stats          - Estatísticas gerais");
        System.out.println("   POST /api/export         - Exporta dados");
        System.out.println("   GET  /api/images/{nome}  - Serve imagens");
        System.out.println("   GET  /api/health         - Health check");
        System.out.println("==============================================");
    }

    /**
     * Para o servidor HTTP
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Servidor API parado");
        }
    }

    // ===== HANDLERS DOS ENDPOINTS =====

    /**
     * Handler para endpoints de amostras
     */
    class SamplesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";

            try {
                if ("GET".equals(method)) {
                    if (path.equals("/api/samples")) {
                        response = controller.getSamples();
                    } else if (path.startsWith("/api/samples/")) {
                        String id = path.substring("/api/samples/".length());
                        response = controller.getSampleById(id);
                    }
                } else if ("POST".equals(method)) {
                    Map<String, String> params = parseFormData(exchange);
                    String id = params.get("id");
                    String nome = params.get("nome");
                    String tipo = params.get("tipo");
                    String operador = params.get("operadorResponsavel");

                    response = controller.createSample(id, nome, tipo, operador);
                }

                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, 500, errorResponse);
            }
        }
    }

    /**
     * Handler para endpoints de medições
     */
    class MeasurementsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            String method = exchange.getRequestMethod();
            String response = "";

            try {
                if ("GET".equals(method)) {
                    response = controller.getMeasurements();
                } else if ("POST".equals(method)) {
                    Map<String, String> params = parseFormData(exchange);
                    String id = params.get("id");
                    String sampleId = params.get("sampleId");
                    double area = Double.parseDouble(params.getOrDefault("area", "0.0"));
                    String imagemId = params.get("imagemId");

                    response = controller.createMeasurement(id, sampleId, area, imagemId);
                }

                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, 500, errorResponse);
            }
        }
    }

    /**
     * Handler para endpoints de operadores
     */
    class OperatorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                String response = controller.getOperators();
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, 500, errorResponse);
            }
        }
    }

    /**
     * Handler para endpoints de microscópios
     */
    class MicroscopesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                String response = controller.getMicroscopes();
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, 500, errorResponse);
            }
        }
    }

    /**
     * Handler para estatísticas
     */
    class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                String response = controller.getStats();
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, 500, errorResponse);
            }
        }
    }

    /**
     * Handler para exportação de dados
     */
    class ExportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                String response = controller.exportData();
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, 500, errorResponse);
            }
        }
    }

    /**
     * Handler para health check
     */
    class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                String response = controller.healthCheck();
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, 500, errorResponse);
            }
        }
    }

    /**
     * Handler para servir imagens (primeiro tenta banco, depois arquivo local)
     */
    class ImagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            String method = exchange.getRequestMethod();
            if (!"GET".equals(method)) {
                String errorResponse = "{\"error\": \"Método não permitido\"}";
                sendJsonResponse(exchange, 405, errorResponse);
                return;
            }

            try {
                String path = exchange.getRequestURI().getPath();
                String imageName = path.substring("/api/images/".length());

                if (imageName.isEmpty()) {
                    String errorResponse = "{\"error\": \"Nome da imagem não fornecido\"}";
                    sendJsonResponse(exchange, 400, errorResponse);
                    return;
                }

                byte[] imageData = null;
                String contentType = "image/jpeg";

                // ESTRATÉGIA 1: Tenta buscar do banco de dados primeiro
                System.out.println("🔍 Buscando imagem no banco: " + imageName);
                imageData = controller.getImageBlob(imageName);

                if (imageData != null) {
                    System.out.println("✅ Imagem recuperada do banco de dados (" + imageData.length + " bytes)");
                } else {
                    // ESTRATÉGIA 2: Fallback para arquivo local (retrocompatibilidade)
                    System.out.println("⚠️ Imagem não encontrada no banco, tentando arquivo local...");

                    String imagePath = System.getProperty("user.dir") + "/../data-integration/" + imageName;
                    File imageFile = new File(imagePath);

                    if (!imageFile.exists() || !imageFile.isFile()) {
                        System.err.println("❌ Imagem não encontrada: nem no banco nem no arquivo local");
                        String errorResponse = "{\"error\": \"Imagem não encontrada\"}";
                        sendJsonResponse(exchange, 404, errorResponse);
                        return;
                    }

                    // Lê o arquivo de imagem local
                    FileInputStream fis = new FileInputStream(imageFile);
                    imageData = fis.readAllBytes();
                    fis.close();
                    System.out.println("✅ Imagem recuperada do arquivo local (" + imageData.length + " bytes)");
                }

                // Determina o Content-Type baseado na extensão
                if (imageName.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (imageName.toLowerCase().endsWith(".jpg") || imageName.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (imageName.toLowerCase().endsWith(".tiff") || imageName.toLowerCase().endsWith(".tif")) {
                    contentType = "image/tiff";
                } else if (imageName.toLowerCase().endsWith(".bmp")) {
                    contentType = "image/bmp";
                }

                // Adiciona headers de cache para melhor performance
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600"); // Cache de 1 hora
                exchange.sendResponseHeaders(200, imageData.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(imageData);
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao servir imagem: " + e.getMessage());
                e.printStackTrace();
                String errorResponse = "{\"error\": \"Erro ao carregar imagem: " + e.getMessage() + "\"}";
                sendJsonResponse(exchange, 500, errorResponse);
            }
        }
    }

    /**
     * Handler para CORS
     */
    class CorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            // Se não é um endpoint da API, retorna 404
            String notFoundResponse = "{\"error\": \"Endpoint não encontrado\"}";
            sendJsonResponse(exchange, 404, notFoundResponse);
        }
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Adiciona headers CORS para permitir acesso do frontend
     */
    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "86400");
    }

    /**
     * Envia resposta JSON
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Faz parse dos dados do formulário
     */
    private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> result = new HashMap<>();

        if ("POST".equals(exchange.getRequestMethod())) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine();

            if (formData != null) {
                String[] pairs = formData.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        result.put(key, value);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Método principal para iniciar o servidor
     */
    public static void main(String[] args) {
        ApiServer server = new ApiServer();
        try {
            server.start();
            System.out.println("✅ Servidor iniciado com sucesso!");
            System.out.println("🔗 Teste: http://localhost:" + PORT + "/api/health");
            System.out.println("📊 Dashboard: Abra o frontend em outra aba");
            System.out.println("⏹️  Para parar: Ctrl+C");

            // Adiciona shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Parando servidor...");
                server.stop();
            }));

            // Mantém o servidor rodando
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("❌ Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}