package br.com.micromedicao.dao;

import br.com.micromedicao.model.Sample;
import br.com.micromedicao.connection.ConnectionFactory;
import br.com.micromedicao.connection.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a entidade Sample.
 * Implementa operações CRUD para amostras patológicas.
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class SampleDAO {

    private ConnectionFactory connectionFactory;

    public SampleDAO() {
        this.connectionFactory = ConnectionFactory.getInstance();
    }

    /**
     * Insere uma nova amostra.
     */
    public boolean insert(Sample sample) throws SQLException {
        String sql = "INSERT INTO " + DatabaseConfig.TABLE_SAMPLES +
                " (ID, SAMPLE_ID, NOME, TIPO, DATA_COLETA, OPERADOR_RESPONSAVEL, STATUS, DATA_CRIACAO) " +
                "VALUES (" + DatabaseConfig.SEQUENCE_SAMPLES + ".NEXTVAL, ?, ?, ?, ?, ?, ?, SYSDATE)";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, sample.getId());
            stmt.setString(2, sample.getNome());
            stmt.setString(3, sample.getTipo());
            stmt.setTimestamp(4, Timestamp.valueOf(sample.getDataColeta()));
            stmt.setString(5, sample.getOperadorResponsavel());
            stmt.setString(6, DatabaseConfig.DEFAULT_SAMPLE_STATUS);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            System.out.println("Amostra inserida com sucesso: " + sample.getId());
            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao inserir amostra: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Busca amostra por ID.
     */
    public Sample findBySampleId(String sampleId) throws SQLException {
        String sql = "SELECT * FROM " + DatabaseConfig.TABLE_SAMPLES + " WHERE SAMPLE_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, sampleId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractSampleFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar amostra: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista todas as amostras.
     */
    public List<Sample> findAll() throws SQLException {
        String sql = "SELECT * FROM " + DatabaseConfig.TABLE_SAMPLES + " ORDER BY DATA_COLETA DESC";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Sample> samples = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                samples.add(extractSampleFromResultSet(rs));
            }

            System.out.println("Encontradas " + samples.size() + " amostras");
            return samples;

        } catch (SQLException e) {
            System.err.println("Erro ao listar amostras: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista amostras por tipo.
     */
    public List<Sample> findByType(String tipo) throws SQLException {
        String sql = "SELECT * FROM " + DatabaseConfig.TABLE_SAMPLES +
                " WHERE TIPO = ? ORDER BY DATA_COLETA DESC";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Sample> samples = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, tipo);

            rs = stmt.executeQuery();

            while (rs.next()) {
                samples.add(extractSampleFromResultSet(rs));
            }

            return samples;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar amostras por tipo: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Atualiza uma amostra.
     */
    public boolean update(Sample sample) throws SQLException {
        String sql = "UPDATE " + DatabaseConfig.TABLE_SAMPLES +
                " SET NOME = ?, TIPO = ?, OPERADOR_RESPONSAVEL = ? WHERE SAMPLE_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, sample.getNome());
            stmt.setString(2, sample.getTipo());
            stmt.setString(3, sample.getOperadorResponsavel());
            stmt.setString(4, sample.getId());

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Exclui uma amostra (altera status).
     */
    public boolean delete(String sampleId) throws SQLException {
        String sql = "UPDATE " + DatabaseConfig.TABLE_SAMPLES +
                " SET STATUS = ? WHERE SAMPLE_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, DatabaseConfig.SAMPLE_STATUS_ARQUIVADA);
            stmt.setString(2, sampleId);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Conta amostras.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + DatabaseConfig.TABLE_SAMPLES +
                " WHERE STATUS != ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, DatabaseConfig.SAMPLE_STATUS_ARQUIVADA);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Extrai Sample do ResultSet.
     */
    private Sample extractSampleFromResultSet(ResultSet rs) throws SQLException {
        String sampleId = rs.getString("SAMPLE_ID");
        String nome = rs.getString("NOME");
        String tipo = rs.getString("TIPO");
        LocalDateTime dataColeta = rs.getTimestamp("DATA_COLETA").toLocalDateTime();
        String operadorResponsavel = rs.getString("OPERADOR_RESPONSAVEL");

        return new Sample(sampleId, nome, tipo, dataColeta, operadorResponsavel);
    }
}