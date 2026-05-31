package otpservice.controller;

import otpservice.service.AuthService;
import otpservice.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public class AuthController extends BaseController {
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("POST".equals(method) && path.equals("/api/auth/register")) {
            register(exchange);
        } else if ("POST".equals(method) && path.equals("/api/auth/login")) {
            login(exchange);
        } else {
            sendError(exchange, 404, "Not Found");
        }
    }

    private void login(HttpExchange exchange) throws IOException {
        try {
            String body = getRequestBody(exchange);
            Map<String, String> data = JsonUtil.fromJson(body, Map.class);
            String username = data.get("username");
            String password = data.get("password");
            if (username == null || password == null) {
                sendError(exchange, 400, "Username and password required");
                return;
            }
            String token = authService.login(username, password);
            sendResponse(exchange, 200, Map.of("token", token));
        } catch (IllegalArgumentException e) {
            sendError(exchange, 401, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error");
        }
    }

    private void register(HttpExchange exchange) throws IOException {
        try {
            String body = getRequestBody(exchange);
            Map<String, String> data = JsonUtil.fromJson(body, Map.class);
            String username = data.get("username");
            String password = data.get("password");
            if (username == null || password == null) {
                sendError(exchange, 400, "Username and password required");
                return;
            }
            String token = authService.register(username, password);
            sendResponse(exchange, 200, Map.of("token", token));
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error");
        }
    }
}