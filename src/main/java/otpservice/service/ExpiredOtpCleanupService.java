package otpservice.service;

import otpservice.dao.OtpCodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiredOtpCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(ExpiredOtpCleanupService.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final OtpCodeDao otpCodeDao = new OtpCodeDao();

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            logger.debug("Run expired OTP cleanup");
            otpCodeDao.expireOldCodes();
        }, 1, 1, TimeUnit.MINUTES);
        logger.info("Expired OTP cleanup service started");
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}