package br.com.micromedicao.dao;

import br.com.micromedicao.model.Operator;
import br.com.micromedicao.connection.ConnectionFactory;
import br.com.micromedicao.connection.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para a entidade Operator.
 * Implementa operações CRUD completas para operadores do sistema.
 *
 * Esta classe é responsável por toda interação com a tabela TB_OPERATORS
 * no banco de dados Oracle, seguindo as melhores práticas de DAO.
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class OperatorDAO {

    private ConnectionFactory connectionFactory;

    /**
     * Construtor que inicializa a ConnectionFactory.
     */
    public OperatorDAO() {
        this.connectionFactory = ConnectionFactory.getInstance();
    }

    /**
     * Insere um novo operador no banco de dados.
     *
     * @param operator Operador a ser inserido
     * @return true se a inserção foi bem-sucedida, false caso contrário
     * @throws SQLException se houver erro na operação
     */
    public boolean insert(Operator operator) throws SQLException {
        String sql = "INSERT INTO " + DatabaseConfig.TABLE_OPERATORS +
                " (ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, DATA_CRIACAO, ATIVO) " +
                "VALUES (" + DatabaseConfig.SEQUENCE_OPERATORS + ".NEXTVAL, ?, ?, ?, ?, SYSDATE, ?)";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, operator.getId());
            stmt.setString(2, operator.getNome());
            stmt.setString(3, operator.getEmail());
            stmt.setString(4, operator.getNivelAcesso());
            stmt.setString(5, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            System.out.println("Operador inserido com sucesso: " + operator.getId());
            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao inserir operador: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Busca um operador pelo ID do operador.
     *
     * @param operatorId ID do operador a ser buscado
     * @return Operator encontrado ou null se não existir
     * @throws SQLException se houver erro na operação
     */
    public Operator findByOperatorId(String operatorId) throws SQLException {
        String sql = "SELECT * FROM " + DatabaseConfig.TABLE_OPERATORS +
                " WHERE OPERATOR_ID = ? AND ATIVO = ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, operatorId);
            stmt.setString(2, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractOperatorFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar operador por ID: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Busca um operador pelo email.
     *
     * @param email Email do operador
     * @return Operator encontrado ou null se não existir
     * @throws SQLException se houver erro na operação
     */
    public Operator findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM " + DatabaseConfig.TABLE_OPERATORS +
                " WHERE EMAIL = ? AND ATIVO = ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractOperatorFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar operador por email: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista todos os operadores ativos.
     *
     * @return Lista de operadores ativos
     * @throws SQLException se houver erro na operação
     */
    public List<Operator> findAll() throws SQLException {
        String sql = "SELECT * FROM " + DatabaseConfig.TABLE_OPERATORS +
                " WHERE ATIVO = ? ORDER BY NOME";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Operator> operators = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            rs = stmt.executeQuery();

            while (rs.next()) {
                operators.add(extractOperatorFromResultSet(rs));
            }

            System.out.println("Encontrados " + operators.size() + " operadores ativos");
            return operators;

        } catch (SQLException e) {
            System.err.println("Erro ao listar operadores: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista operadores por nível de acesso.
     *
     * @param nivelAcesso Nível de acesso a filtrar
     * @return Lista de operadores do nível especificado
     * @throws SQLException se houver erro na operação
     */
    public List<Operator> findByAccessLevel(String nivelAcesso) throws SQLException {
        String sql = "SELECT * FROM " + DatabaseConfig.TABLE_OPERATORS +
                " WHERE NIVEL_ACESSO = ? AND ATIVO = ? ORDER BY NOME";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Operator> operators = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, nivelAcesso);
            stmt.setString(2, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            rs = stmt.executeQuery();

            while (rs.next()) {
                operators.add(extractOperatorFromResultSet(rs));
            }

            System.out.println("Encontrados " + operators.size() + " operadores com nível " + nivelAcesso);
            return operators;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar operadores por nível de acesso: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Atualiza um operador existente.
     *
     * @param operator Operador com dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     * @throws SQLException se houver erro na operação
     */
    public boolean update(Operator operator) throws SQLException {
        String sql = "UPDATE " + DatabaseConfig.TABLE_OPERATORS +
                " SET NOME = ?, EMAIL = ?, NIVEL_ACESSO = ? " +
                "WHERE OPERATOR_ID = ? AND ATIVO = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, operator.getNome());
            stmt.setString(2, operator.getEmail());
            stmt.setString(3, operator.getNivelAcesso());
            stmt.setString(4, operator.getId());
            stmt.setString(5, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            if (rowsAffected > 0) {
                System.out.println("Operador atualizado com sucesso: " + operator.getId());
            } else {
                System.out.println("Nenhum operador foi atualizado. ID: " + operator.getId());
            }

            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao atualizar operador: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Exclui logicamente um operador (marca como inativo).
     *
     * @param operatorId ID do operador a ser excluído
     * @return true se a exclusão foi bem-sucedida, false caso contrário
     * @throws SQLException se houver erro na operação
     */
    public boolean delete(String operatorId) throws SQLException {
        String sql = "UPDATE " + DatabaseConfig.TABLE_OPERATORS +
                " SET ATIVO = 'N' WHERE OPERATOR_ID = ? AND ATIVO = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, operatorId);
            stmt.setString(2, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            if (rowsAffected > 0) {
                System.out.println("Operador excluído (inativado) com sucesso: " + operatorId);
            } else {
                System.out.println("Nenhum operador foi excluído. ID: " + operatorId);
            }

            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao excluir operador: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Verifica se um operador existe e está ativo.
     *
     * @param operatorId ID do operador
     * @return true se o operador existe e está ativo, false caso contrário
     * @throws SQLException se houver erro na operação
     */
    public boolean exists(String operatorId) throws SQLException {
        String sql = "SELECT 1 FROM " + DatabaseConfig.TABLE_OPERATORS +
                " WHERE OPERATOR_ID = ? AND ATIVO = ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, operatorId);
            stmt.setString(2, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Erro ao verificar existência do operador: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Conta o número total de operadores ativos.
     *
     * @return Número de operadores ativos
     * @throws SQLException se houver erro na operação
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + DatabaseConfig.TABLE_OPERATORS +
                " WHERE ATIVO = ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, DatabaseConfig.DEFAULT_OPERATOR_STATUS);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            System.err.println("Erro ao contar operadores: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Autentica um operador pelo email.
     * Método de conveniência para verificação de login.
     *
     * @param email Email do operador
     * @return true se o operador existe e está ativo, false caso contrário
     * @throws SQLException se houver erro na operação
     */
    public boolean authenticate(String email) throws SQLException {
        Operator operator = findByEmail(email);
        return operator != null && operator.autenticar();
    }

    /**
     * Extrai um objeto Operator de um ResultSet.
     *
     * @param rs ResultSet contendo dados do operador
     * @return Operator criado a partir dos dados
     * @throws SQLException se houver erro ao extrair dados
     */
    private Operator extractOperatorFromResultSet(ResultSet rs) throws SQLException {
        String operatorId = rs.getString("OPERATOR_ID");
        String nome = rs.getString("NOME");
        String email = rs.getString("EMAIL");
        String nivelAcesso = rs.getString("NIVEL_ACESSO");

        return new Operator(operatorId, nome, email, nivelAcesso);
    }

    /**
     * Método para teste da classe DAO.
     *
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        System.out.println("=== TESTE OPERATOR DAO ===");

        OperatorDAO dao = new OperatorDAO();

        try {
            // Teste de busca
            System.out.println("\n1. Teste de busca por ID:");
            Operator operator = dao.findByOperatorId("OP001");
            if (operator != null) {
                System.out.println("Operador encontrado: " + operator.toString());
            } else {
                System.out.println("Operador não encontrado");
            }

            // Teste de listagem
            System.out.println("\n2. Teste de listagem:");
            List<Operator> operators = dao.findAll();
            System.out.println("Total de operadores: " + operators.size());

            // Teste de contagem
            System.out.println("\n3. Teste de contagem:");
            int count = dao.count();
            System.out.println("Contagem de operadores: " + count);

            // Teste de existência
            System.out.println("\n4. Teste de existência:");
            boolean exists = dao.exists("OP001");
            System.out.println("Operador OP001 existe: " + exists);

        } catch (SQLException e) {
            System.err.println("Erro no teste: " + e.getMessage());
        }
    }
}