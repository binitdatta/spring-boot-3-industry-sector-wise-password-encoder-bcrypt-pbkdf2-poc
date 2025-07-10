package com.rollingstone.config;

import com.rollingstone.model.AppUser;
import com.rollingstone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantPasswordEncoderFactory encoderFactory;

    @Override
    public void run(String... args) {
        addUser("alice_global", "password123", "USER");
        addUser("bob_global", "securePass456", "ADMIN");
    }

    private void addUser(String username, String rawPassword, String role) {
        PasswordEncoder encoder = encoderFactory.getEncoder();  // ✅ DelegatingPasswordEncoder expected
        if (userRepository.findByUsername(username).isEmpty()) {
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(encoder.encode(rawPassword));       // ✅ should include prefix like {bcrypt}
            user.setRoles(role);
            userRepository.save(user);
        }
    }
}
