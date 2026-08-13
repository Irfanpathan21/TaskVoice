package com.taskvoice.service;

import com.taskvoice.dao.AuditLogDAO;
import com.taskvoice.dao.UserDAO;
import com.taskvoice.dao.impl.AuditLogDAOImpl;
import com.taskvoice.dao.impl.UserDAOImpl;
import com.taskvoice.model.User;
import com.taskvoice.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserDAO userDAO       = new UserDAOImpl();
    private final AuditLogDAO auditDAO  = new AuditLogDAOImpl();

    /**
     * Authenticates a user by email + password.
     * Returns the User on success, empty on failure.
     * Never reveals whether email or password is wrong — returns generic failure.
     */
    public Optional<User> login(String email, String plainPassword, String ipAddress) {
        if (email == null || plainPassword == null) return Optional.empty();

        Optional<User> found = userDAO.findByEmail(email.trim().toLowerCase());
        if (found.isEmpty()) {
            log.warn("Login attempt with unknown email: {}", email);
            return Optional.empty();
        }

        User user = found.get();
        if (!user.isActive()) {
            log.warn("Login attempt on disabled account: {}", email);
            return Optional.empty();
        }

        if (!PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
            log.warn("Failed password check for: {}", email);
            return Optional.empty();
        }

        auditDAO.log(user.getId(), user.getName(), "USER_LOGIN", "USER", user.getId(),
                     "Login from " + ipAddress, ipAddress);
        log.info("User {} ({}) logged in from {}", user.getName(), user.getRoleName(), ipAddress);
        return Optional.of(user);
    }

    public void logout(User user, String ipAddress) {
        if (user != null) {
            auditDAO.log(user.getId(), user.getName(), "USER_LOGOUT", "USER", user.getId(),
                         "Logout", ipAddress);
        }
    }

    /**
     * Change password. Clears force_pw_change flag on success.
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        Optional<User> found = userDAO.findById(userId);
        if (found.isEmpty()) return false;
        User user = found.get();

        // If force_pw_change is set, skip current-password check (admin reset flow)
        if (!user.isForcePwChange()) {
            if (!PasswordUtil.verify(currentPassword, user.getPasswordHash())) return false;
        }

        if (newPassword == null || newPassword.length() < 8) return false;

        String newHash = PasswordUtil.hash(newPassword);
        userDAO.updatePassword(userId, newHash, false); // clears forcePwChange
        return true;
    }

    /** Admin-initiated password reset — sets a temp password and forces change on next login. */
    public String resetPassword(int adminId, String adminName, int targetUserId) {
        String tempPassword = generateTempPassword();
        String hash = PasswordUtil.hash(tempPassword);
        userDAO.updatePassword(targetUserId, hash, true);
        auditDAO.log(adminId, adminName, "PASSWORD_RESET", "USER", targetUserId,
                     "Admin reset password for user " + targetUserId, null);
        return tempPassword; // shown once to admin, never stored
    }

    private String generateTempPassword() {
        // 12-char alphanumeric temp password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#!";
        java.util.Random rng = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }
}
