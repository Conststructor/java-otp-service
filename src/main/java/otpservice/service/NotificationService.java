package otpservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void saveToFile(String code) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("otp_codes.txt", true))) {
            writer.println("Generated OTP: " + code);
            logger.info("OTP saved to file");
        } catch (IOException e) {
            logger.error("Error saving OTP to file", e);
        }
    }
}