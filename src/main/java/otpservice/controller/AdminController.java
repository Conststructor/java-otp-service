package otpservice.controller;

import otpservice.dao.OtpConfigDao;
import otpservice.model.OtpConfig;
import otpservice.model.Role;
import otpservice.model.User;
import otpservice.service.UserService;
import otpservice.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminController extends BaseController {
    private final UserService userService = new UserService();
    private final OtpConfigDao otpConfigDao = new OtpConfigDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = (User) exchange.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            sendError(exchange, 403, "Forbidden");
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equals(method) && path.equals("/api/admin/users")) {
            getUsers(exchange);
        } else if ("DELETE".equals(method) && path.matches("/api/admin/users/\\d+")) {
            deleteUser(exchange);
        } else if ("PUT".equals(method) && path.equals("/api/admin/config")) {
            updateConfig(exchange);
        } else {
            sendError(exchange, 404, "Not Found");
        }
    }

    private void getUsers(HttpExchange exchange) throws IOException {
        List<User> users = userService.getAllNonAdminUsers();
        sendResponse(exchange, 200, users);
    }

    private void updateConfig(HttpExchange exchange) throws IOException {
        String body = getRequestBody(exchange);
        Map<String, Object> data = JsonUtil.fromJson(body, Map.class);
        Integer codeLength = (Integer) data.get("codeLength");
        Integer ttlSeconds = (Integer) data.get("ttlSeconds");
        if (codeLength == null || ttlSeconds == null) {
            sendError(exchange, 400, "codeLength and ttlSeconds required");
            return;
        }
        otpConfigDao.updateConfig(new OtpConfig(codeLength, ttlSeconds));
        sendResponse(exchange, 200, Map.of("message", "Config updated"));
    }

    private void deleteUser(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Long userId = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
        try {
            boolean deleted = userService.deleteUser(userId);
            if (deleted) {
                sendResponse(exchange, 200, Map.of("message", "User deleted"));
            } else {
                sendError(exchange, 404, "User not found");
            }
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        }
    }

}