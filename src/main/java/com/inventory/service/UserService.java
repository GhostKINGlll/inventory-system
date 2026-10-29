package com.inventory.service;

import com.inventory.dao.UserRepository;
import com.inventory.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User register(String username, String password, String email, String code) {
        if (!emailService.verifyCode(email, code)) {
            throw new RuntimeException("邮箱验证码错误或已过期");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setVip(false);
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        boolean matches;
        if (passwordEncoder.matches(password, user.getPassword())) {
            matches = true;
        } else if (user.getPassword().equals(password)) {
            matches = true;
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        } else {
            matches = false;
        }
        if (!matches) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!user.getVip()) {
            throw new RuntimeException("该账号没有VIP权限，无法登录");
        }
        return user;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void setVip(Integer id, boolean vip) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setVip(vip);
        userRepository.save(user);
    }

    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public void updateSessionToken(Integer userId, String token) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setSessionToken(token);
            userRepository.save(user);
        });
    }

    public void resetPassword(String email, String code, String newPassword) {
        if (!emailService.verifyCode(email, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("该邮箱未注册"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setSessionToken(null);
        userRepository.save(user);
    }
}
