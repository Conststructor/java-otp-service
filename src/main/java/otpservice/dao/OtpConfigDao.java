package otpservice.dao;

import otpservice.model.OtpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class OtpConfigDao {
    private static final Logger logger = LoggerFactory.getLogger(OtpConfigDao.class);

    public void initConfig() {
        String sql = "INSERT INTO otp_config (code_length, ttl_seconds) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, 6);
            stmt.setInt(2, 300);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error initializing OTP config: ", e);
        }
    }

    public OtpConfig getConfig() {
        String sql = "SELECT code_length, ttl_seconds FROM otp_config LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new OtpConfig(rs.getInt("code_length"), rs.getInt("ttl_seconds"));
            }
        } catch (SQLException e) {
            logger.error("Error getting OTP config: ", e);
        }
        return new OtpConfig(6, 300);
    }

    public void updateConfig(OtpConfig config) {
        String sql = "UPDATE otp_config SET code_length = ?, ttl_seconds = ? WHERE id = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, config.getCodeLength());
            stmt.setInt(2, config.getTtlSeconds());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating OTP config: ", e);
        }
    }

}