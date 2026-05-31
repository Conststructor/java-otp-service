package otpservice.service;

import otpservice.dao.OtpCodeDao;
import otpservice.dao.UserDao;
import otpservice.model.Role;
import otpservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao = new UserDao();
    private final OtpCodeDao otpCodeDao = new OtpCodeDao();

    public List<User> getAllNonAdminUsers() {
        return userDao.findByRole(Role.USER);
    }

    public boolean deleteUser(Long userId) {
        User user = userDao.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot delete admin");
        }
        otpCodeDao.delByUserId(userId);
        boolean deleted = userDao.delete(userId);
        logger.info("Deleted user {} and associated OTP codes", userId);
        return deleted;
    }
}