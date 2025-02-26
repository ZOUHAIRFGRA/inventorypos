package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserController {

    @Autowired
    private UserService userService;

    public User createUser(User user) {
        return userService.createUser(user);
    }

    public User getUserById(Long id) {
        return userService.getUserById(id);
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public User updateUser(Long id, User user) {
        return userService.updateUser(id, user);
    }

    public void deleteUser(Long id) {
        userService.deleteUser(id);
    }
}
