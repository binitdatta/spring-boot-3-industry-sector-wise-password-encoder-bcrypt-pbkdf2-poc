package com.rollingstone.service;

import com.rollingstone.config.TenantPasswordEncoderFactory;
import com.rollingstone.model.AppUser;
import com.rollingstone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantPasswordEncoderFactory encoderFactory;

    public AppUser register(String username, String rawPassword) {
        PasswordEncoder encoder = encoderFactory.getEncoder();  // Uses globally-configured encoder
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(encoder.encode(rawPassword));
        user.setRoles("ROLE_USER");

        return userRepository.save(user);
    }
}
