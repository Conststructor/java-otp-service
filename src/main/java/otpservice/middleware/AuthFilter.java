package otpservice.middleware;

import otpservice.model.User;
import otpservice.service.AuthService;
import otpservice.util.JwtUtil;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class AuthFilter extends Filter {
    private final AuthService authService = new AuthService();

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.startsWith("/api/auth/")) {
            chain.doFilter(exchange);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        User user = authService.getUserByToken(token);
        if (user == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        exchange.setAttribute("user", user);
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "JWT Authentication Filter";
    }
}