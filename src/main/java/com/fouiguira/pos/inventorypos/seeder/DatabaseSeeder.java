package com.fouiguira.pos.inventorypos.seeder;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.repositories.UserRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // Ensure the transaction is committed

    public void run(String... args) {
        long userCount = userRepository.count();
        logger.info("⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡ Database contains {} user(s).", userCount);
        if (userCount == 0) {
            logger.info("⚡ Database is empty. Seeding default admin user...");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));            admin.setRole(User.Role.OWNER);
            admin.setCreatedAt(new Date());
            admin.setUpdatedAt(new Date());
            admin.setTemporaryPassword(false);
            userRepository.save(admin);
            System.out.println("✅ Default admin user created."+ admin);
            logger.info("✅ Default admin user created.");
            logger.info("✅✅✅✅✅new user count: {}", userRepository.count());
        } else {
            System.out.println("⚡ Database already contains " + userCount + " user(s). No seeding needed.");
            logger.info("⚡ Database already contains {} user(s). No seeding needed.", userCount);
        }
    }
}
