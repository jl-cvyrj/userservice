package com.innowise.userservice.service.impl;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.*;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public UserRepository userRepository;

    public User createUser(User user) throws DuplicateResourceException, ServiceException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Cacheable(value = "user", key = "#id")
    public User getUserById(Long id) throws ResourceNotFoundException {
        return userRepository.findByIdWithCards(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public Page<User> getAllUsers(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecifications.hasName(name))
                .and(UserSpecifications.hasSurname(surname));
        return userRepository.findAll(spec, pageable);
    }

    @Transactional
    @CachePut(value = "user", key = "#id")
    public User updateUser(Long id, User updatedUser) throws ResourceNotFoundException, DuplicateResourceException, ServiceException {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

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
            if (!updatedUser.getEmail().equals(existingUser.getEmail()) &&
                    userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + updatedUser.getEmail());
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        existingUser.setActive(updatedUser.isActive());

        return userRepository.save(existingUser);
    }

    @CacheEvict(value = "user", key = "#id")
    @Transactional
    public void setActiveStatus(Long id, boolean active) throws ResourceNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.setActiveStatus(id, active);
    }

    @CacheEvict(value = "user", key = "#id")
    @Transactional
    public void deleteUser(Long id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
}