package otpservice.service;

import otpservice.dao.OtpCodeDao;
import otpservice.dao.OtpConfigDao;
import otpservice.model.OtpCode;
import otpservice.model.OtpConfig;
import otpservice.model.OtpStatus;
import otpservice.util.OtpGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final OtpCodeDao otpCodeDao = new OtpCodeDao();
    private final OtpConfigDao otpConfigDao = new OtpConfigDao();
    private final NotificationService notificationService = new NotificationService();
    private final EmailNotificationService emailNotificationService= new EmailNotificationService();

    public OtpCode generateAndSend(Long userId, String operationId) {
        OtpConfig config = otpConfigDao.getConfig();
        String code = OtpGenerator.generate(config.getCodeLength());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(config.getTtlSeconds());

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(userId);
        otpCode.setOperationId(operationId);
        otpCode.setCode(code);
        otpCode.setStatus(OtpStatus.ACTIVE);
        otpCode.setCreatedAt(now);
        otpCode.setExpiresAt(expiresAt);
        otpCodeDao.save(otpCode);

        notificationService.saveToFile(code);
        logger.info("Generated OTP for user {}, operation {}", userId, operationId);
        return otpCode;
    }

    public OtpCode generateAndSendEmail (Long userId, String operationId, String destination) {
        OtpConfig config = otpConfigDao.getConfig();
        String code = OtpGenerator.generate(config.getCodeLength());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(config.getTtlSeconds());

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(userId);
        otpCode.setOperationId(operationId);
        otpCode.setCode(code);
        otpCode.setStatus(OtpStatus.ACTIVE);
        otpCode.setCreatedAt(now);
        otpCode.setExpiresAt(expiresAt);
        otpCodeDao.save(otpCode);


        emailNotificationService.sendCode(destination, code);
        notificationService.saveToFile(code);
        logger.info("Generated OTP for user {}, operation {}, send to email: {}", userId, operationId, destination);
        return otpCode;
    }

    public OtpCode generateAndSendSms (Long userId, String operationId, String destination) {
        OtpConfig config = otpConfigDao.getConfig();
        String code = OtpGenerator.generate(config.getCodeLength());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(config.getTtlSeconds());

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(userId);
        otpCode.setOperationId(operationId);
        otpCode.setCode(code);
        otpCode.setStatus(OtpStatus.ACTIVE);
        otpCode.setCreatedAt(now);
        otpCode.setExpiresAt(expiresAt);
        otpCodeDao.save(otpCode);

        notificationService.saveToFile(code);
        logger.info("Generated OTP for user {}, operation {}, sent to number {}", userId, operationId, destination);
        return otpCode;
    }

    public boolean validate(Long userId, String operationId, String code) {
        return otpCodeDao.findByUIdandOpreId(userId, operationId)
                .map(otp -> {
                    if (otp.getStatus() != OtpStatus.ACTIVE) {
                        logger.warn("OTP not active: {}", otp.getStatus());
                        return false;
                    }
                    if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
                        otpCodeDao.updateStatus(otp.getId(), OtpStatus.EXPIRED);
                        logger.warn("OTP expired");
                        return false;
                    }
                    if (otp.getCode().equals(code)) {
                        otpCodeDao.updateStatus(otp.getId(), OtpStatus.USED);
                        logger.info("OTP validated successfully");
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}