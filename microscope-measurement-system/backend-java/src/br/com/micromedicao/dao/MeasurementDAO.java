package br.com.micromedicao.dao;

import br.com.micromedicao.model.Measurement;
import br.com.micromedicao.model.Sample;
import br.com.micromedicao.model.MicroscopyImage;
import br.com.micromedicao.connection.ConnectionFactory;
import br.com.micromedicao.connection.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para a entidade Measurement.
 * Implementa operações CRUD completas para medições do sistema.
 *
 * Esta classe é responsável por toda interação com a tabela TB_MEASUREMENTS
 * no banco de dados Oracle, incluindo relacionamentos com outras entidades.
 *
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class MeasurementDAO {

    private ConnectionFactory connectionFactory;
    private SampleDAO sampleDAO;
    private MicroscopyImageDAO imageDAO;

    /**
     * Construtor que inicializa as dependências.
     */
    public MeasurementDAO() {
        this.connectionFactory = ConnectionFactory.getInstance();
        this.sampleDAO = new SampleDAO();
        this.imageDAO = new MicroscopyImageDAO();
    }

    /**
     * Insere uma nova medição no banco de dados.
     *
     * @param measurement Medição a ser inserida
     * @return true se a inserção foi bem-sucedida, false caso contrário
     * @throws SQLException se houver erro na operação
     */
    public boolean insert(Measurement measurement) throws SQLException {
        String sql = "INSERT INTO " + DatabaseConfig.TABLE_MEASUREMENTS +
                " (ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK, MICROSCOPE_ID_FK, OPERATOR_ID_FK, " +
                "AREA_PIXELS, AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO, " +
                "METODO_PROCESSAMENTO, PARAMETROS_PROCESSAMENTO, OBSERVACOES, VALIDADA, DATA_CRIACAO) " +
                "VALUES (" + DatabaseConfig.SEQUENCE_MEASUREMENTS + ".NEXTVAL, ?, " +
                "(SELECT ID FROM " + DatabaseConfig.TABLE_SAMPLES + " WHERE SAMPLE_ID = ?), " +
                "(SELECT ID FROM " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " WHERE IMAGE_ID = ?), " +
                "(SELECT ID FROM " + DatabaseConfig.TABLE_DIGITAL_MICROSCOPES + " WHERE MICROSCOPE_ID = ?), " +
                "(SELECT ID FROM " + DatabaseConfig.TABLE_OPERATORS + " WHERE OPERATOR_ID = ?), " +
                "?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, measurement.getId());
            stmt.setString(2, measurement.getSample().getId()); // Sample ID
            stmt.setString(3, measurement.getImagem().getId()); // Image ID
            // Aqui assumimos que temos acesso ao microscope ID - pode precisar de ajuste
            stmt.setString(4, "MICRO001"); // Default microscope - ajustar conforme necessário
            // Para operator, precisamos determinar como obter o ID - ajustar conforme necessário
            stmt.setString(5, "OP001"); // Default operator - ajustar conforme necessário
            stmt.setDouble(6, measurement.getArea()); // Area pixels (assumindo que é o mesmo valor)
            stmt.setDouble(7, measurement.getArea()); // Area micrometers
            stmt.setDouble(8, 10.0); // Scale - valor padrão, ajustar conforme necessário
            stmt.setTimestamp(9, Timestamp.valueOf(measurement.getDataHora()));
            stmt.setString(10, "OpenCV Threshold + Contour Detection"); // Método padrão
            stmt.setString(11, "{}"); // Parâmetros padrão
            stmt.setString(12, "Medição automática"); // Observações padrão
            stmt.setString(13, DatabaseConfig.DEFAULT_MEASUREMENT_VALIDATED);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            System.out.println("Medição inserida com sucesso: " + measurement.getId());
            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao inserir medição: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Insere uma medição com IDs numéricos das entidades relacionadas.
     *
     * @param measurementId ID da medição
     * @param sampleIdFk ID numérico da amostra
     * @param imageIdFk ID numérico da imagem
     * @param microscopeIdFk ID numérico do microscópio
     * @param operatorIdFk ID numérico do operador
     * @param areaPixels Área em pixels
     * @param areaMicrometers Área em micrômetros
     * @param scalePixelsPerUm Escala pixels por micrômetro
     * @param dataMedicao Data da medição
     * @param observacoes Observações
     * @return true se a inserção foi bem-sucedida
     * @throws SQLException se houver erro na operação
     */
    public boolean insertWithForeignKeys(String measurementId, long sampleIdFk, long imageIdFk,
                                        long microscopeIdFk, long operatorIdFk, double areaPixels,
                                        double areaMicrometers, double scalePixelsPerUm,
                                        LocalDateTime dataMedicao, String observacoes) throws SQLException {

        String sql = "INSERT INTO " + DatabaseConfig.TABLE_MEASUREMENTS +
                " (ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK, MICROSCOPE_ID_FK, OPERATOR_ID_FK, " +
                "AREA_PIXELS, AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO, " +
                "METODO_PROCESSAMENTO, OBSERVACOES, VALIDADA, DATA_CRIACAO) " +
                "VALUES (" + DatabaseConfig.SEQUENCE_MEASUREMENTS + ".NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, measurementId);
            stmt.setLong(2, sampleIdFk);
            stmt.setLong(3, imageIdFk);
            stmt.setLong(4, microscopeIdFk);
            stmt.setLong(5, operatorIdFk);
            stmt.setDouble(6, areaPixels);
            stmt.setDouble(7, areaMicrometers);
            stmt.setDouble(8, scalePixelsPerUm);
            stmt.setTimestamp(9, Timestamp.valueOf(dataMedicao));
            stmt.setString(10, "OpenCV Threshold + Contour Detection");
            stmt.setString(11, observacoes);
            stmt.setString(12, DatabaseConfig.DEFAULT_MEASUREMENT_VALIDATED);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            System.out.println("Medição inserida com sucesso: " + measurementId);
            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao inserir medição: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Busca uma medição pelo ID.
     *
     * @param measurementId ID da medição
     * @return Measurement encontrada ou null se não existir
     * @throws SQLException se houver erro na operação
     */
    public Measurement findByMeasurementId(String measurementId) throws SQLException {
        String sql = "SELECT m.*, s.SAMPLE_ID, s.NOME as SAMPLE_NOME, s.TIPO as SAMPLE_TIPO, " +
                "i.IMAGE_ID, i.ARQUIVO as IMAGE_ARQUIVO " +
                "FROM " + DatabaseConfig.TABLE_MEASUREMENTS + " m " +
                "INNER JOIN " + DatabaseConfig.TABLE_SAMPLES + " s ON m.SAMPLE_ID_FK = s.ID " +
                "INNER JOIN " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " i ON m.IMAGE_ID_FK = i.ID " +
                "WHERE m.MEASUREMENT_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, measurementId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMeasurementFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar medição por ID: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista todas as medições.
     *
     * @return Lista de medições
     * @throws SQLException se houver erro na operação
     */
    public List<Measurement> findAll() throws SQLException {
        String sql = "SELECT m.*, s.SAMPLE_ID, s.NOME as SAMPLE_NOME, s.TIPO as SAMPLE_TIPO, " +
                "i.IMAGE_ID, i.ARQUIVO as IMAGE_ARQUIVO " +
                "FROM " + DatabaseConfig.TABLE_MEASUREMENTS + " m " +
                "INNER JOIN " + DatabaseConfig.TABLE_SAMPLES + " s ON m.SAMPLE_ID_FK = s.ID " +
                "INNER JOIN " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " i ON m.IMAGE_ID_FK = i.ID " +
                "ORDER BY m.DATA_MEDICAO DESC";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Measurement> measurements = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                measurements.add(extractMeasurementFromResultSet(rs));
            }

            System.out.println("Encontradas " + measurements.size() + " medições");
            return measurements;

        } catch (SQLException e) {
            System.err.println("Erro ao listar medições: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista medições por amostra.
     *
     * @param sampleId ID da amostra
     * @return Lista de medições da amostra
     * @throws SQLException se houver erro na operação
     */
    public List<Measurement> findBySampleId(String sampleId) throws SQLException {
        String sql = "SELECT m.*, s.SAMPLE_ID, s.NOME as SAMPLE_NOME, s.TIPO as SAMPLE_TIPO, " +
                "i.IMAGE_ID, i.ARQUIVO as IMAGE_ARQUIVO " +
                "FROM " + DatabaseConfig.TABLE_MEASUREMENTS + " m " +
                "INNER JOIN " + DatabaseConfig.TABLE_SAMPLES + " s ON m.SAMPLE_ID_FK = s.ID " +
                "INNER JOIN " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " i ON m.IMAGE_ID_FK = i.ID " +
                "WHERE s.SAMPLE_ID = ? ORDER BY m.DATA_MEDICAO DESC";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Measurement> measurements = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, sampleId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                measurements.add(extractMeasurementFromResultSet(rs));
            }

            System.out.println("Encontradas " + measurements.size() + " medições para amostra " + sampleId);
            return measurements;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar medições por amostra: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Lista medições por período.
     *
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Lista de medições no período
     * @throws SQLException se houver erro na operação
     */
    public List<Measurement> findByDateRange(LocalDateTime dataInicio, LocalDateTime dataFim) throws SQLException {
        String sql = "SELECT m.*, s.SAMPLE_ID, s.NOME as SAMPLE_NOME, s.TIPO as SAMPLE_TIPO, " +
                "i.IMAGE_ID, i.ARQUIVO as IMAGE_ARQUIVO " +
                "FROM " + DatabaseConfig.TABLE_MEASUREMENTS + " m " +
                "INNER JOIN " + DatabaseConfig.TABLE_SAMPLES + " s ON m.SAMPLE_ID_FK = s.ID " +
                "INNER JOIN " + DatabaseConfig.TABLE_MICROSCOPY_IMAGES + " i ON m.IMAGE_ID_FK = i.ID " +
                "WHERE m.DATA_MEDICAO BETWEEN ? AND ? ORDER BY m.DATA_MEDICAO DESC";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Measurement> measurements = new ArrayList<>();

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(dataInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(dataFim));

            rs = stmt.executeQuery();

            while (rs.next()) {
                measurements.add(extractMeasurementFromResultSet(rs));
            }

            System.out.println("Encontradas " + measurements.size() + " medições no período especificado");
            return measurements;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar medições por período: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Atualiza uma medição existente.
     *
     * @param measurement Medição com dados atualizados
     * @return true se a atualização foi bem-sucedida
     * @throws SQLException se houver erro na operação
     */
    public boolean update(Measurement measurement) throws SQLException {
        String sql = "UPDATE " + DatabaseConfig.TABLE_MEASUREMENTS +
                " SET AREA_PIXELS = ?, AREA_MICROMETERS = ?, OBSERVACOES = ?, VALIDADA = ? " +
                "WHERE MEASUREMENT_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);

            stmt.setDouble(1, measurement.getArea());
            stmt.setDouble(2, measurement.getArea());
            stmt.setString(3, "Medição atualizada");
            stmt.setString(4, "S"); // Marca como validada após atualização
            stmt.setString(5, measurement.getId());

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            if (rowsAffected > 0) {
                System.out.println("Medição atualizada com sucesso: " + measurement.getId());
            } else {
                System.out.println("Nenhuma medição foi atualizada. ID: " + measurement.getId());
            }

            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao atualizar medição: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Valida uma medição.
     *
     * @param measurementId ID da medição
     * @param operatorId ID do operador que está validando
     * @return true se a validação foi bem-sucedida
     * @throws SQLException se houver erro na operação
     */
    public boolean validateMeasurement(String measurementId, String operatorId) throws SQLException {
        String sql = "UPDATE " + DatabaseConfig.TABLE_MEASUREMENTS +
                " SET VALIDADA = 'S', DATA_VALIDACAO = SYSDATE, OPERADOR_VALIDACAO = ? " +
                "WHERE MEASUREMENT_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, operatorId);
            stmt.setString(2, measurementId);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            if (rowsAffected > 0) {
                System.out.println("Medição validada com sucesso: " + measurementId);
            }

            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao validar medição: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Exclui uma medição.
     *
     * @param measurementId ID da medição a ser excluída
     * @return true se a exclusão foi bem-sucedida
     * @throws SQLException se houver erro na operação
     */
    public boolean delete(String measurementId) throws SQLException {
        String sql = "DELETE FROM " + DatabaseConfig.TABLE_MEASUREMENTS +
                " WHERE MEASUREMENT_ID = ?";

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, measurementId);

            int rowsAffected = stmt.executeUpdate();
            ConnectionFactory.commitTransaction(connection);

            if (rowsAffected > 0) {
                System.out.println("Medição excluída com sucesso: " + measurementId);
            } else {
                System.out.println("Nenhuma medição foi excluída. ID: " + measurementId);
            }

            return rowsAffected > 0;

        } catch (SQLException e) {
            ConnectionFactory.rollbackTransaction(connection);
            System.err.println("Erro ao excluir medição: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Conta o número total de medições.
     *
     * @return Número de medições
     * @throws SQLException se houver erro na operação
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + DatabaseConfig.TABLE_MEASUREMENTS;

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
            System.err.println("Erro ao contar medições: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Calcula estatísticas das medições.
     *
     * @return Array com [média, mínimo, máximo] das áreas em micrômetros
     * @throws SQLException se houver erro na operação
     */
    public double[] getAreaStatistics() throws SQLException {
        String sql = "SELECT AVG(AREA_MICROMETERS) as MEDIA, " +
                "MIN(AREA_MICROMETERS) as MINIMO, " +
                "MAX(AREA_MICROMETERS) as MAXIMO " +
                "FROM " + DatabaseConfig.TABLE_MEASUREMENTS;

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = connectionFactory.getConnection();
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                double[] stats = new double[3];
                stats[0] = rs.getDouble("MEDIA");
                stats[1] = rs.getDouble("MINIMO");
                stats[2] = rs.getDouble("MAXIMO");
                return stats;
            }

            return new double[]{0.0, 0.0, 0.0};

        } catch (SQLException e) {
            System.err.println("Erro ao calcular estatísticas: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            ConnectionFactory.closeConnection(connection);
        }
    }

    /**
     * Extrai um objeto Measurement de um ResultSet.
     *
     * @param rs ResultSet contendo dados da medição
     * @return Measurement criado a partir dos dados
     * @throws SQLException se houver erro ao extrair dados
     */
    private Measurement extractMeasurementFromResultSet(ResultSet rs) throws SQLException {
        // Criar objetos relacionados simplificados
        String sampleId = rs.getString("SAMPLE_ID");
        String sampleNome = rs.getString("SAMPLE_NOME");
        String sampleTipo = rs.getString("SAMPLE_TIPO");
        Sample sample = new Sample(sampleId, sampleNome, sampleTipo, "Sistema");

        String imageId = rs.getString("IMAGE_ID");
        String arquivo = rs.getString("IMAGE_ARQUIVO");
        MicroscopyImage image = new MicroscopyImage(imageId, arquivo, null, null);

        // Criar measurement
        String measurementId = rs.getString("MEASUREMENT_ID");
        double area = rs.getDouble("AREA_MICROMETERS");
        LocalDateTime dataHora = rs.getTimestamp("DATA_MEDICAO").toLocalDateTime();

        return new Measurement(measurementId, sample, area, dataHora, image);
    }

    /**
     * Método para teste da classe DAO.
     *
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        System.out.println("=== TESTE MEASUREMENT DAO ===");

        MeasurementDAO dao = new MeasurementDAO();

        try {
            // Teste de busca
            System.out.println("\n1. Teste de busca por ID:");
            Measurement measurement = dao.findByMeasurementId("MEAS_1749780022");
            if (measurement != null) {
                System.out.println("Medição encontrada: " + measurement.toString());
            } else {
                System.out.println("Medição não encontrada");
            }

            // Teste de listagem
            System.out.println("\n2. Teste de listagem:");
            List<Measurement> measurements = dao.findAll();
            System.out.println("Total de medições: " + measurements.size());

            // Teste de estatísticas
            System.out.println("\n3. Teste de estatísticas:");
            double[] stats = dao.getAreaStatistics();
            System.out.printf("Área - Média: %.2f, Mín: %.2f, Máx: %.2f μm²%n",
                            stats[0], stats[1], stats[2]);

            // Teste de contagem
            System.out.println("\n4. Teste de contagem:");
            int count = dao.count();
            System.out.println("Contagem de medições: " + count);

        } catch (SQLException e) {
            System.err.println("Erro no teste: " + e.getMessage());
        }
    }
}