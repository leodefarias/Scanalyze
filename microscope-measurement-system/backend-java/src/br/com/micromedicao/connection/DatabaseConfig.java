package br.com.micromedicao.connection;

/**
 * Classe de configuração do banco de dados.
 * Centraliza todas as configurações relacionadas ao banco Oracle.
 *
 * Esta classe contém constantes e configurações utilizadas pela
 * ConnectionFactory e classes DAO para interação com o banco de dados.
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public final class DatabaseConfig {

    /**
     * Configurações de conexão Oracle FIAP
     */
    public static final String ORACLE_URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl";
    public static final String ORACLE_USER = "RM555211";
    public static final String ORACLE_PASSWORD = "281005";
    public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    /**
     * Configurações de timeout e pool de conexões
     */
    public static final int CONNECTION_TIMEOUT = 30; // segundos
    public static final int QUERY_TIMEOUT = 60; // segundos
    public static final int MAX_CONNECTIONS = 10;

    /**
     * Nomes das tabelas do sistema
     */
    public static final String TABLE_OPERATORS = "TB_OPERATORS";
    public static final String TABLE_DIGITAL_MICROSCOPES = "TB_DIGITAL_MICROSCOPES";
    public static final String TABLE_SAMPLES = "TB_SAMPLES";
    public static final String TABLE_MICROSCOPY_IMAGES = "TB_MICROSCOPY_IMAGES";
    public static final String TABLE_MEASUREMENTS = "TB_MEASUREMENTS";

    /**
     * Nomes das sequences
     */
    public static final String SEQUENCE_OPERATORS = "SQ_OPERATORS";
    public static final String SEQUENCE_DIGITAL_MICROSCOPES = "SQ_DIGITAL_MICROSCOPES";
    public static final String SEQUENCE_SAMPLES = "SQ_SAMPLES";
    public static final String SEQUENCE_MICROSCOPY_IMAGES = "SQ_MICROSCOPY_IMAGES";
    public static final String SEQUENCE_MEASUREMENTS = "SQ_MEASUREMENTS";

    /**
     * Configurações do sistema
     */
    public static final String SYSTEM_NAME = "Sistema de Micromedição Automatizada";
    public static final String SYSTEM_VERSION = "1.0";
    public static final String DATABASE_SCHEMA_VERSION = "1.0";

    /**
     * Valores padrão do sistema
     */
    public static final String DEFAULT_OPERATOR_STATUS = "S"; // Ativo
    public static final String DEFAULT_MICROSCOPE_STATUS = "ATIVO";
    public static final String DEFAULT_SAMPLE_STATUS = "ATIVA";
    public static final String DEFAULT_IMAGE_FORMAT = "JPG";
    public static final double DEFAULT_SCALE_PIXELS_PER_UM = 10.0;
    public static final String DEFAULT_MEASUREMENT_VALIDATED = "N"; // Não validada

    /**
     * Níveis de acesso válidos
     */
    public static final String ACCESS_LEVEL_ADMIN = "ADMIN";
    public static final String ACCESS_LEVEL_TECNICO = "TECNICO";
    public static final String ACCESS_LEVEL_OPERADOR = "OPERADOR";

    /**
     * Status válidos para microscópios
     */
    public static final String MICROSCOPE_STATUS_ATIVO = "ATIVO";
    public static final String MICROSCOPE_STATUS_INATIVO = "INATIVO";
    public static final String MICROSCOPE_STATUS_MANUTENCAO = "MANUTENCAO";

    /**
     * Status válidos para amostras
     */
    public static final String SAMPLE_STATUS_ATIVA = "ATIVA";
    public static final String SAMPLE_STATUS_PROCESSADA = "PROCESSADA";
    public static final String SAMPLE_STATUS_ARQUIVADA = "ARQUIVADA";

    /**
     * Formatos de imagem suportados
     */
    public static final String[] SUPPORTED_IMAGE_FORMATS = {"JPG", "PNG", "TIFF", "BMP"};

    /**
     * Configurações de validação
     */
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 150;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;

    /**
     * Padrões de validação (regex)
     */
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String OPERATOR_ID_PATTERN = "^OP[0-9]{3}$"; // Ex: OP001
    public static final String SAMPLE_ID_PATTERN = "^SAMPLE_[0-9]+$"; // Ex: SAMPLE_001
    public static final String MEASUREMENT_ID_PATTERN = "^MEAS_[0-9]+$"; // Ex: MEAS_001

    /**
     * Mensagens de erro padrão
     */
    public static final String ERROR_CONNECTION_FAILED = "Falha ao conectar com o banco de dados";
    public static final String ERROR_INVALID_DATA = "Dados inválidos fornecidos";
    public static final String ERROR_DUPLICATE_ENTRY = "Registro duplicado encontrado";
    public static final String ERROR_RECORD_NOT_FOUND = "Registro não encontrado";
    public static final String ERROR_FOREIGN_KEY_VIOLATION = "Violação de chave estrangeira";

    /**
     * Mensagens de sucesso padrão
     */
    public static final String SUCCESS_INSERT = "Registro inserido com sucesso";
    public static final String SUCCESS_UPDATE = "Registro atualizado com sucesso";
    public static final String SUCCESS_DELETE = "Registro excluído com sucesso";
    public static final String SUCCESS_SELECT = "Consulta executada com sucesso";

    /**
     * Construtor privado para evitar instanciação
     */
    private DatabaseConfig() {
        throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada");
    }

    /**
     * Valida se um nível de acesso é válido.
     *
     * @param nivelAcesso Nível de acesso a ser validado
     * @return true se o nível é válido, false caso contrário
     */
    public static boolean isValidAccessLevel(String nivelAcesso) {
        if (nivelAcesso == null) return false;
        String nivel = nivelAcesso.toUpperCase();
        return ACCESS_LEVEL_ADMIN.equals(nivel) ||
               ACCESS_LEVEL_TECNICO.equals(nivel) ||
               ACCESS_LEVEL_OPERADOR.equals(nivel);
    }

    /**
     * Valida se um status de microscópio é válido.
     *
     * @param status Status a ser validado
     * @return true se o status é válido, false caso contrário
     */
    public static boolean isValidMicroscopeStatus(String status) {
        if (status == null) return false;
        String statusUpper = status.toUpperCase();
        return MICROSCOPE_STATUS_ATIVO.equals(statusUpper) ||
               MICROSCOPE_STATUS_INATIVO.equals(statusUpper) ||
               MICROSCOPE_STATUS_MANUTENCAO.equals(statusUpper);
    }

    /**
     * Valida se um status de amostra é válido.
     *
     * @param status Status a ser validado
     * @return true se o status é válido, false caso contrário
     */
    public static boolean isValidSampleStatus(String status) {
        if (status == null) return false;
        String statusUpper = status.toUpperCase();
        return SAMPLE_STATUS_ATIVA.equals(statusUpper) ||
               SAMPLE_STATUS_PROCESSADA.equals(statusUpper) ||
               SAMPLE_STATUS_ARQUIVADA.equals(statusUpper);
    }

    /**
     * Valida se um formato de imagem é suportado.
     *
     * @param format Formato a ser validado
     * @return true se o formato é suportado, false caso contrário
     */
    public static boolean isValidImageFormat(String format) {
        if (format == null) return false;
        String formatUpper = format.toUpperCase();
        for (String supportedFormat : SUPPORTED_IMAGE_FORMATS) {
            if (supportedFormat.equals(formatUpper)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Valida se um email tem formato válido.
     *
     * @param email Email a ser validado
     * @return true se o email é válido, false caso contrário
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches(EMAIL_PATTERN);
    }

    /**
     * Obtém próximo valor de uma sequence.
     *
     * @param sequenceName Nome da sequence
     * @return String com query para obter próximo valor
     */
    public static String getNextSequenceValue(String sequenceName) {
        return "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
    }

    /**
     * Obtém informações de configuração do sistema.
     *
     * @return String com informações formatadas
     */
    public static String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== CONFIGURAÇÕES DO SISTEMA ===\n");
        info.append("Sistema: ").append(SYSTEM_NAME).append("\n");
        info.append("Versão: ").append(SYSTEM_VERSION).append("\n");
        info.append("Schema DB: ").append(DATABASE_SCHEMA_VERSION).append("\n");
        info.append("URL Oracle: ").append(ORACLE_URL).append("\n");
        info.append("Usuário: ").append(ORACLE_USER).append("\n");
        info.append("Timeout Conexão: ").append(CONNECTION_TIMEOUT).append("s\n");
        info.append("Timeout Query: ").append(QUERY_TIMEOUT).append("s\n");
        info.append("Max Conexões: ").append(MAX_CONNECTIONS).append("\n");
        info.append("==============================");
        return info.toString();
    }

    /**
     * Método para debug das configurações.
     *
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        System.out.println(getSystemInfo());

        System.out.println("\n=== VALIDAÇÕES ===");
        System.out.println("Nível ADMIN válido: " + isValidAccessLevel("ADMIN"));
        System.out.println("Nível INVALID válido: " + isValidAccessLevel("INVALID"));
        System.out.println("Status ATIVO válido: " + isValidMicroscopeStatus("ATIVO"));
        System.out.println("Formato JPG válido: " + isValidImageFormat("JPG"));
        System.out.println("Email teste válido: " + isValidEmail("teste@example.com"));
    }
}