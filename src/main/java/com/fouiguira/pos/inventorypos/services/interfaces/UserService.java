package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.User;

import java.util.List;

public interface UserService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User.Role getCurrentUserRole(); // For role-based access

    // Additional methods to match UserServiceImpl
    User authenticate(String username, String password); // Authenticate a user
    User getCurrentUser(); // Get the currently logged-in user
    void logout(); // Logout the current user
}