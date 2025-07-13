package com.rollingstone.config;

import com.rollingstone.model.AppUser;
import com.rollingstone.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    Logger logger  = LoggerFactory.getLogger("DataInitializer");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantPasswordEncoderFactory encoderFactory;

    @Override
    public void run(String... args) {
        userRepository.deleteAll(); // ❗ only for development
        addUser("alice_global", "password123", "USER");
        addUser("bob_global", "securePass456", "ADMIN");
    }

//    private void addUser(String username, String rawPassword, String role) {
//
//        PasswordEncoder encoder = encoderFactory.getEncoder();  // ✅ DelegatingPasswordEncoder expected
//        if (userRepository.findByUsername(username).isEmpty()) {
//            AppUser user = new AppUser();
//            user.setUsername(username);
//            user.setPassword(encoder.encode(rawPassword));       // ✅ should include prefix like {bcrypt}
//            user.setRoles(role);
//            logger.info(user.toString());
//            userRepository.save(user);
//        }
//    }

//    private void addUser(String username, String rawPassword, String role) {
//        PasswordEncoder encoder = encoderFactory.getEncoder();
//
//        logger.info("Encoder Bean ID :" + encoder.toString());
//
//        boolean exists = userRepository.findByUsername(username).isPresent();
//        logger.info("Checking if '{}' exists in DB: {}", username, exists);
//        String password = encoder.encode(rawPassword);
//        logger.info("Encoded Password: {}", password);
//
//        if (!exists) {
//            AppUser user = new AppUser();
//            user.setUsername(username);
//            user.setPassword(encoder.encode(rawPassword));
//            user.setRoles(role);
//            userRepository.save(user);
//            logger.info("✅ User '{}' added", username);
//        } else {
//            logger.info("❌ User '{}' already exists. Skipping insert.", username);
//        }
//    }

    private void addUser(String username, String rawPassword, String role) {
        PasswordEncoder encoder = encoderFactory.getEncoder();

        if (userRepository.findByUsername(username).isEmpty()) {
            String encoded = encoder.encode(rawPassword);
            logger.info("Encoded Password: {}", encoded);

            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(encoded); // ✅ use the already encoded one
            user.setRoles(role);
            userRepository.save(user);

            // Optional: log what is stored
            AppUser stored = userRepository.findByUsername(username).get();
            logger.info("🔍 Stored Password for '{}': {}", username, stored.getPassword());
        }
    }

}
