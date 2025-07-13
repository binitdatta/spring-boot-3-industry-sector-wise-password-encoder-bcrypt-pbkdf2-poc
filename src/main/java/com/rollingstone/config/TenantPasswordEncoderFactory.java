package com.rollingstone.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TenantPasswordEncoderFactory {

    private static final String GLOBAL_SECRET = "StrongPepperUsedAcrossAllPBKDF2Hashes";
    Logger logger = LoggerFactory.getLogger("TenantPasswordEncoderFactory");

    @Value("${app.security.tenant-type:retail}")
    private String tenantType;

    private PasswordEncoder delegatingEncoder;

    @PostConstruct
    public void init() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder(
                GLOBAL_SECRET,
                16,
                310000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256
        ));

        // Set default based on global tenant type
        String defaultId = tenantType.equalsIgnoreCase("healthcare") ? "pbkdf2" : "bcrypt";

        logger.info(" Default Industry Sector Type :" + defaultId);

        this.delegatingEncoder = new DelegatingPasswordEncoder(defaultId, encoders);
    }

    /**
     * This encoder will prefix encoded passwords with {bcrypt} or {pbkdf2}
     */
    public PasswordEncoder getEncoder() {
        return this.delegatingEncoder;
    }
}
