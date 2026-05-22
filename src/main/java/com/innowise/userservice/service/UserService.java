package com.innowise.userservice.service;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    public User createUser(User user) throws ServiceException;

    User getUserById(Long id) throws ServiceException;

    public User updateUser(Long id, User updatedUser) throws ServiceException;

    public Page<User> getAllUsers(String name, String surname, Pageable pageable);

    public void setActiveStatus(Long id, boolean active) throws ServiceException;

    void deleteUser(Long id) throws ServiceException;
}