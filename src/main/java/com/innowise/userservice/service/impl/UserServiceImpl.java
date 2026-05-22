package com.innowise.userservice.service.impl;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.ServiceException;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public UserRepository userRepository;

    public User createUser(User user) throws ServiceException {
        if (user.getName().isBlank()) {
            throw new ServiceException();
        }
        return userRepository.save(user);
    }

    public User getUserById(Long id) throws ServiceException {
        return userRepository.findById(id)
                .orElseThrow(() -> new ServiceException("User not found with id: " + id));
    }

    public Page<User> getAllUsers(String name, String surname, Pageable pageable) {

        Specification<User> spec = Specification
                .where(UserSpecifications.hasName(name))
                .and(UserSpecifications.hasSurname(surname));

        return userRepository.findAll(spec, pageable);
    }

    public User updateUser(Long id, User updatedUser) throws ServiceException {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ServiceException("User not found with id: " + id));

        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getSurname() != null && !updatedUser.getSurname().isBlank()) {
            existingUser.setSurname(updatedUser.getSurname());
        }
        if (updatedUser.getBirthDate() != null) {
            existingUser.setBirthDate(updatedUser.getBirthDate());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        existingUser.setActive(updatedUser.isActive());

        return userRepository.save(existingUser);
    }

    @Transactional
    public void setActiveStatus(Long id, boolean active) throws ServiceException {
        if (!userRepository.existsById(id)) {
            throw new ServiceException("User not found with id: " + id);
        }
        userRepository.setActiveStatus(id, active);
    }

    @Transactional
    public void deleteUser(Long id) throws ServiceException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ServiceException("User not found with id: " + id));
        userRepository.delete(user);
    }
}
