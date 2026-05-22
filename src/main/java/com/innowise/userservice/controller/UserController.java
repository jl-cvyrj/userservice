package com.innowise.userservice.controller;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.ServiceException;
import com.innowise.userservice.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @PostMapping
    public User createUser(@RequestBody User user) throws ServiceException {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) throws ServiceException {
        return userService.getUserById(id);
    }

    @GetMapping
    public Page<User> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws ServiceException {
        return userService.getAllUsers(name, surname, PageRequest.of(page, size));
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) throws ServiceException {
        return userService.updateUser(id, user);
    }

    @PatchMapping("/{id}/active")
    public void setActiveStatus(@PathVariable Long id, @RequestParam boolean active) throws ServiceException {
        userService.setActiveStatus(id, active);
    }
}