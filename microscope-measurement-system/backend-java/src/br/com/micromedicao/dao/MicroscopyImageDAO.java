package br.com.micromedicao.dao;

import br.com.micromedicao.model.MicroscopyImage;
import br.com.micromedicao.model.Sample;
import br.com.micromedicao.connection.ConnectionFactory;
import br.com.micromedicao.connection.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a entidade MicroscopyImage.
 * Implementa operações CRUD para imagens microscópicas.
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class MicroscopyImageDAO {

    private ConnectionFactory connectionFactory;

    public MicroscopyImageDAO() {
        this.connectionFactory = ConnectionFactory.getInstance();
    }

    /**
     * Insere uma nova imagem microscópica.
     */
    public boolean insert(MicroscopyImage image) throws SQLException {
        String sql = "INSERT INTO " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES +
                " (ID, IMAGE_ID, SAMPLE_ID_FK, MICROSCOPE_ID_FK, ARQUIVO, FORMATO, " +
                "DATA_CAPTURA, OPERADOR_CAPTURA, DATA_CRIACAO) " +
                "VALUES (" + DatabaseConfig.SEQUENCE_MICROSCOPY_IMAGES + ".NEXTVAL, ?, " +
                "(SELECT ID FROM " + DatabaseConfig.TABLE_SAMPLES + " WHERE SAMPLE_ID = ?), " +
                "1, ?, ?, ?, ?, SYSDATE)";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, image.getId());
            stmt.setString(2, "SAMPLE_001"); // Default sample - ajustar conforme necessário
            stmt.setString(3, image.getArquivo());
            stmt.setString(4, DatabaseConfig.DEFAULT_IMAGE_FORMAT);
            stmt.setTimestamp(5, Timestamp.valueOf(image.getDataCaptura()));
            stmt.setString(6, "Sistema");

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            System.out.println("Imagem inserida com sucesso: " + image.getId());
            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao inserir imagem: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Busca imagem por ID.
     */
    public MicroscopyImage findByImageId(String imageId) throws SQLException {
        String sql = "SELECT i.*, s.SAMPLE_ID, s.NOME as SAMPLE_NOME, s.TIPO as SAMPLE_TIPO " +
                "FROM " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " i " +
                "INNER JOIN " + DatabaseConfig.TABLE_SAMPLES + " s ON i.SAMPLE_ID_FK = s.ID " +
                "WHERE i.IMAGE_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, imageId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractImageFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar imagem: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista todas as imagens.
     */
    public List<MicroscopyImage> findAll() throws SQLException {
        String sql = "SELECT i.*, s.SAMPLE_ID, s.NOME as SAMPLE_NOME, s.TIPO as SAMPLE_TIPO " +
                "FROM " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " i " +
                "INNER JOIN " + DatabaseConfig.TABLE_SAMPLES + " s ON i.SAMPLE_ID_FK = s.ID " +
                "ORDER BY i.DATA_CAPTURA DESC";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<MicroscopyImage> images = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                images.add(extractImageFromResultSet(rs));
            }

            System.out.println("Encontradas " + images.size() + " imagens");
            return images;

        } catch (SQLException e) {
            System.err.println("Erro ao listar imagens: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista imagens por amostra.
     */
    public List<MicroscopyImage> findBySampleId(String sampleId) throws SQLException {
        String sql = "SELECT i.*, s.SAMPLE_ID, s.NOME as SAMPLE_NOME, s.TIPO as SAMPLE_TIPO " +
                "FROM " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " i " +
                "INNER JOIN " + DatabaseConfig.TABLE_SAMPLES + " s ON i.SAMPLE_ID_FK = s.ID " +
                "WHERE s.SAMPLE_ID = ? ORDER BY i.DATA_CAPTURA DESC";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<MicroscopyImage> images = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, sampleId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                images.add(extractImageFromResultSet(rs));
            }

            return images;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar imagens por amostra: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Atualiza uma imagem.
     */
    public boolean update(MicroscopyImage image) throws SQLException {
        String sql = "UPDATE " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES +
                " SET ARQUIVO = ?, OBSERVACOES = ? WHERE IMAGE_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, image.getArquivo());
            stmt.setString(2, "Sem observações"); // Default - método não existe na classe
            stmt.setString(3, image.getId());

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
     * Exclui uma imagem.
     */
    public boolean delete(String imageId) throws SQLException {
        String sql = "DELETE FROM " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " WHERE IMAGE_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, imageId);

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
     * Conta imagens.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES;

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
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
     * Extrai MicroscopyImage do ResultSet.
     */
    private MicroscopyImage extractImageFromResultSet(ResultSet rs) throws SQLException {
        String imageId = rs.getString("IMAGE_ID");
        String arquivo = rs.getString("ARQUIVO");
        LocalDateTime dataCaptura = rs.getTimestamp("DATA_CAPTURA").toLocalDateTime();

        // Criar sample relacionado
        String sampleId = rs.getString("SAMPLE_ID");
        String sampleNome = rs.getString("SAMPLE_NOME");
        String sampleTipo = rs.getString("SAMPLE_TIPO");
        Sample sample = new Sample(sampleId, sampleNome, sampleTipo, "Sistema");

        return new MicroscopyImage(imageId, arquivo, null, dataCaptura);
    }
}