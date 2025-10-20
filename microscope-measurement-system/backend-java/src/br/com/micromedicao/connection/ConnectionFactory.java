package br.com.micromedicao.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável pelo gerenciamento de conexões com o banco de dados Oracle.
 * Implementa o padrão Factory para criação de conexões de banco de dados.
 *
 * Esta classe centraliza as configurações de conexão e fornece métodos
 * para obter e fechar conexões com o banco Oracle.
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class ConnectionFactory {

    /**
     * URL de conexão com o banco Oracle FIAP
     */
    private static final String ORACLE_URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl";

    /**
     * Usuário do banco de dados (conforme requisito: deve estar no código)
     */
    private static final String ORACLE_USER = "RM555211";

    /**
     * Senha do banco de dados (conforme requisito: deve estar no código)
     */
    private static final String ORACLE_PASSWORD = "281005";

    /**
     * Driver JDBC do Oracle
     */
    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    /**
     * Instância única da classe (Singleton)
     */
    private static ConnectionFactory instance;

    /**
     * Construtor privado para implementar o padrão Singleton.
     * Carrega o driver JDBC do Oracle durante a inicialização.
     */
    private ConnectionFactory() {
        try {
            // Carrega o driver JDBC do Oracle
            Class.forName(ORACLE_DRIVER);
            System.out.println("Driver Oracle carregado com sucesso!");
        } catch (ClassNotFoundException e) {
            System.err.println("Erro ao carregar driver Oracle: " + e.getMessage());
            throw new RuntimeException("Falha ao carregar driver Oracle", e);
        }
    }

    /**
     * Obtém a instância única da ConnectionFactory (padrão Singleton).
     *
     * @return Instância única da ConnectionFactory
     */
    public static synchronized ConnectionFactory getInstance() {
        if (instance == null) {
            instance = new ConnectionFactory();
        }
        return instance;
    }

    /**
     * Cria e retorna uma nova conexão com o banco de dados Oracle.
     *
     * @return Connection objeto de conexão com o banco
     * @throws SQLException se houver erro na conexão
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(ORACLE_URL, ORACLE_USER, ORACLE_PASSWORD);

            // Configura auto-commit como false para controle de transações
            connection.setAutoCommit(false);

            System.out.println("Conexão estabelecida com sucesso!");
            return connection;

        } catch (SQLException e) {
            System.err.println("Erro ao conectar com banco de dados:");
            System.err.println("URL: " + ORACLE_URL);
            System.err.println("Usuário: " + ORACLE_USER);
            System.err.println("Erro: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Testa a conexão com o banco de dados.
     *
     * @return true se a conexão foi bem-sucedida, false caso contrário
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            // Executa uma query simples para testar a conexão
            boolean isValid = connection.isValid(5); // timeout de 5 segundos

            if (isValid) {
                System.out.println("Teste de conexão: SUCESSO");
                System.out.println("Database: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("Versão: " + connection.getMetaData().getDatabaseProductVersion());
            } else {
                System.out.println("Teste de conexão: FALHOU");
            }

            return isValid;

        } catch (SQLException e) {
            System.err.println("Teste de conexão: FALHOU");
            System.err.println("Erro: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fecha uma conexão de forma segura.
     *
     * @param connection Conexão a ser fechada
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Conexão fechada com sucesso!");
                }
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }

    /**
     * Executa commit em uma conexão de forma segura.
     *
     * @param connection Conexão para fazer commit
     */
    public static void commitTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.commit();
                System.out.println("Transação confirmada (commit) com sucesso!");
            } catch (SQLException e) {
                System.err.println("Erro ao fazer commit: " + e.getMessage());
            }
        }
    }

    /**
     * Executa rollback em uma conexão de forma segura.
     *
     * @param connection Conexão para fazer rollback
     */
    public static void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
                System.out.println("Transação desfeita (rollback) com sucesso!");
            } catch (SQLException e) {
                System.err.println("Erro ao fazer rollback: " + e.getMessage());
            }
        }
    }

    /**
     * Obtém informações sobre a conexão atual.
     *
     * @param connection Conexão para obter informações
     * @return String com informações da conexão
     */
    public String getConnectionInfo(Connection connection) {
        if (connection == null) {
            return "Conexão não disponível";
        }

        try {
            StringBuilder info = new StringBuilder();
            info.append("=== INFORMAÇÕES DA CONEXÃO ===\n");
            info.append("URL: ").append(connection.getMetaData().getURL()).append("\n");
            info.append("Usuário: ").append(connection.getMetaData().getUserName()).append("\n");
            info.append("Database: ").append(connection.getMetaData().getDatabaseProductName()).append("\n");
            info.append("Versão: ").append(connection.getMetaData().getDatabaseProductVersion()).append("\n");
            info.append("Driver: ").append(connection.getMetaData().getDriverName()).append("\n");
            info.append("Versão Driver: ").append(connection.getMetaData().getDriverVersion()).append("\n");
            info.append("Auto-commit: ").append(connection.getAutoCommit()).append("\n");
            info.append("Somente leitura: ").append(connection.isReadOnly()).append("\n");
            info.append("Fechada: ").append(connection.isClosed()).append("\n");

            return info.toString();

        } catch (SQLException e) {
            return "Erro ao obter informações da conexão: " + e.getMessage();
        }
    }

    /**
     * Método utilitário para configurações de desenvolvimento.
     * Exibe informações de debug sobre a conexão.
     */
    public void debugConnectionInfo() {
        System.out.println("=== CONFIGURAÇÕES DE CONEXÃO ===");
        System.out.println("URL: " + ORACLE_URL);
        System.out.println("Usuário: " + ORACLE_USER);
        System.out.println("Driver: " + ORACLE_DRIVER);
        System.out.println("===============================");
    }

    /**
     * Método para testar a ConnectionFactory de forma independente.
     *
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        System.out.println("=== TESTE DA CONNECTION FACTORY ===");

        ConnectionFactory factory = ConnectionFactory.getInstance();
        factory.debugConnectionInfo();

        // Testa a conexão
        if (factory.testConnection()) {
            System.out.println("\n✅ ConnectionFactory funcionando corretamente!");

            // Teste adicional: criar e fechar conexão
            try (Connection conn = factory.getConnection()) {
                System.out.println("\n" + factory.getConnectionInfo(conn));
            } catch (SQLException e) {
                System.err.println("Erro no teste adicional: " + e.getMessage());
            }

        } else {
            System.out.println("\n❌ Problemas na ConnectionFactory!");
        }
    }
}