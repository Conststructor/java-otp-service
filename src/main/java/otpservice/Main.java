package otpservice;

import otpservice.controller.AdminController;
import otpservice.controller.AuthController;
import otpservice.controller.UserController;
import otpservice.dao.OtpConfigDao;
import otpservice.middleware.AuthFilter;
import otpservice.middleware.LoggingFilter;
import otpservice.service.ExpiredOtpCleanupService;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        initDatabase();
        initOtpConfig();

        // Создание HTTP-сервера
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Создание эндпоинтов
        server.createContext("/api/auth", new AuthController()).getFilters().add(new LoggingFilter());
        server.createContext("/api/admin", new AdminController()).getFilters().addAll(List.of(new LoggingFilter(), new AuthFilter()));
        server.createContext("/api/user", new UserController()).getFilters().addAll(List.of(new LoggingFilter(), new AuthFilter()));

        server.setExecutor(null);

        // Фоновый сервис просрочки OTP-кодов
        ExpiredOtpCleanupService cleanupService = new ExpiredOtpCleanupService();
        cleanupService.start();

        // Запуск сервера
        server.start();
        logger.info("Server started on port {}", PORT);
        logger.info("API endpoints available at http://localhost:{}", PORT);

        // Хук для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            cleanupService.stop();
            server.stop(0);
            logger.info("Server stopped");
        }));
    }

    private static void initDatabase() {
        String url = "jdbc:postgresql://localhost:5432/";
        String dbName = "otp_db";
        String user = "postgres";
        String password = "postgres";

        // Создание БД данных, если её нет
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + dbName);
            logger.info("Database '{}' created successfully", dbName);
        } catch (Exception e) {
            // База данных уже существует или нет прав — продолжаем
            logger.info("Database '{}' already exists or cannot be created: {}", dbName, e.getMessage());
        }

        // Создание таблиц
        try (Connection conn = DriverManager.getConnection(url + dbName, user, password);
             Statement stmt = conn.createStatement()) {

            // Пользователи
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGSERIAL PRIMARY KEY,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL
                )
            """);

            // Конфигурации OTP
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS otp_config (
                    id INT PRIMARY KEY DEFAULT 1,
                    code_length INT NOT NULL,
                    ttl_seconds INT NOT NULL,
                    CONSTRAINT single_row CHECK (id = 1)
                )
            """);

            // OTP-коды
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS otp_codes (
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    operation_id VARCHAR(255) NOT NULL,
                    code VARCHAR(255) NOT NULL,
                    status VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    expires_at TIMESTAMP NOT NULL
                )
            """);

            logger.info("Create table: success");
        } catch (Exception e) {
            logger.error("DB init error: ", e);
            throw new RuntimeException("DB init fail: ", e);
        }
    }

    private static void initOtpConfig() {
        // дефолтный конфиг
        new OtpConfigDao().initConfig();
    }
}