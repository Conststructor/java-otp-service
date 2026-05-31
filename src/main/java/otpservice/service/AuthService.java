package otpservice.service;

import otpservice.dao.UserDao;
import otpservice.model.Role;
import otpservice.model.User;
import otpservice.util.JwtUtil;
import otpservice.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao = new UserDao();

    public String register(String username, String password) {
        if (userDao.findByName(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        Role role = userDao.existsAdmin() ? Role.USER : Role.ADMIN;
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setRole(role);
        userDao.save(user);
        logger.info("Register new user: {}, role: {}", username, role);
        return JwtUtil.generateToken(username, role.name());
    }

    public String login(String username, String password) {
        Optional<User> userOpt = userDao.findByName(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        User user = userOpt.get();
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        logger.info("User {} logged", username);
        return JwtUtil.generateToken(username, user.getRole().name());
    }

    public User getUserByToken(String token) {
        if (!JwtUtil.validateToken(token)) {
            return null;
        }
        String username = JwtUtil.getUsername(token);
        return userDao.findByName(username).orElse(null);
    }
}