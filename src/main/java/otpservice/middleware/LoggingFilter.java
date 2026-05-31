package otpservice.middleware;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoggingFilter extends Filter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String fullPath = query == null ? path : path + "?" + query;

        logger.info("--> {} {}", method, fullPath);

        try {
            chain.doFilter(exchange);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponseCode();
            if (statusCode >= 400) {
                logger.warn("<-- {} {} {} ({} ms)", method, fullPath, statusCode, duration);
            } else {
                logger.info("<-- {} {} {} ({} ms)", method, fullPath, statusCode, duration);
            }
        }
    }

    @Override
    public String description() {
        return "HTTP Logging Filter";
    }
}