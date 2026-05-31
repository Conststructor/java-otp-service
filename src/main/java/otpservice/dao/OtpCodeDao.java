package otpservice.dao;

import otpservice.model.OtpCode;
import otpservice.model.OtpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class OtpCodeDao {
    private static final Logger logger = LoggerFactory.getLogger(OtpCodeDao.class);

    private OtpCode mapRow(ResultSet rs) throws SQLException {
        return new OtpCode(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("operation_id"),
                rs.getString("code"),
                OtpStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("expires_at").toLocalDateTime()
        );
    }

    public Optional<OtpCode> findByUIdandOpreId(Long userId, String operationId) {
        String sql = "SELECT id, user_id, operation_id, code, status, created_at, expires_at " +
                "FROM otp_codes WHERE operation_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, operationId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding OTP code: ", e);
        }
        return Optional.empty();
    }

    public OtpCode save(OtpCode code) {
        String sql = "INSERT INTO otp_codes (user_id, operation_id, code, status, created_at, expires_at) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, code.getUserId());
            stmt.setString(2, code.getOperationId());
            stmt.setString(3, code.getCode());
            stmt.setString(4, code.getStatus().name());
            stmt.setTimestamp(5, Timestamp.valueOf(code.getCreatedAt()));
            stmt.setTimestamp(6, Timestamp.valueOf(code.getExpiresAt()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                code.setId(rs.getLong("id"));
            }
        } catch (SQLException e) {
            logger.error("Error saving OTP code: ", e);
        }
        return code;
    }

    public void updateStatus(Long id, OtpStatus status) {
        String sql = "UPDATE otp_codes SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating OTP status: ", e);
        }
    }

    public void expireOldCodes() {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' " +
                "WHERE status = 'ACTIVE' AND expires_at < ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                logger.info("Expired {} OTP codes", updated);
            }
        } catch (SQLException e) {
            logger.error("Error expiring old OTP codes: ", e);
        }
    }

    public void delByUserId(Long userId) {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting OTP codes for user: ", e);
        }
    }

}