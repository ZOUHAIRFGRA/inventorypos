package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.repositories.UserRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private User currentUser; // Track the logged-in user

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        // Initialize admin user if no users exist
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // Plain text for now; hash in production
            admin.setRole(User.Role.OWNER);
            admin.setCreatedAt(new Date());
            admin.setUpdatedAt(new Date());
            userRepository.save(admin);
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username '" + user.getUsername() + "' already exists");
        }
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existing = getUserById(id);
        existing.setUsername(user.getUsername());
        existing.setPassword(user.getPassword());
        existing.setRole(user.getRole());
        existing.setUpdatedAt(new Date());
        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    public User.Role getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    // New method for authentication
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) { // Plain text comparison; hash in production
            currentUser = user; // Set the current user
            return user;
        }
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }
}