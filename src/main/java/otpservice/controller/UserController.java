package otpservice.controller;

import otpservice.model.User;
import otpservice.service.OtpService;
import otpservice.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class UserController extends BaseController {
    private final OtpService otpService = new OtpService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = (User) exchange.getAttribute("user");
        if (user == null) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("POST".equals(method) && path.equals("/api/user/otp/generate")) {
            generateOtp(exchange, user);
        }
        if ("POST".equals(method) && path.contains("/api/user/otp/generate/mail=")) {
            String ePoint = "/api/user/otp/generate/mail=";
            String destination = path.substring(ePoint.length());
            generateOtpEmail(exchange, user, destination);
        }
        if ("POST".equals(method) && path.contains("/api/user/otp/generate/sms=")) {
            String ePoint = "/api/user/otp/generate/mail=";
            String destination = path.substring(ePoint.length());
            generateOtpSms(exchange, user, destination);
        }
        else if ("POST".equals(method) && path.equals("/api/user/otp/validate")) {
            validateOtp(exchange, user);
        } else {
            sendError(exchange, 404, "Not Found");
        }
    }

    private void validateOtp(HttpExchange exchange, User user) throws IOException {
        String body = getRequestBody(exchange);
        Map<String, String> data = JsonUtil.fromJson(body, Map.class);
        String operationId = data.get("operationId");
        String code = data.get("code");
        if (operationId == null || code == null) {
            sendError(exchange, 400, "operationId and code required");
            return;
        }
        boolean valid = otpService.validate(user.getId(), operationId, code);
        sendResponse(exchange, 200, Map.of("valid", valid));
    }

    private void generateOtp(HttpExchange exchange, User user) throws IOException {
        String operationId = UUID.randomUUID().toString();
        otpService.generateAndSend(user.getId(), operationId);
        sendResponse(exchange, 200, Map.of(
            "operationId", operationId,
            "message", "OTP sent in Files"
        ));
    }

    private void generateOtpEmail(HttpExchange exchange, User user, String destination) throws IOException {
        String operationId = UUID.randomUUID().toString();
        otpService.generateAndSendEmail(user.getId(), operationId, destination);
        sendResponse(exchange, 200, Map.of(
                "operationId", operationId,
                "message", "OTP sent via Email"
        ));
    }

    private void generateOtpSms(HttpExchange exchange, User user, String destination) throws IOException {
        String operationId = UUID.randomUUID().toString();
        otpService.generateAndSendSms(user.getId(), operationId, destination);
        sendResponse(exchange, 200, Map.of(
                "operationId", operationId,
                "message", "OTP sent via SMS"
        ));
    }
}