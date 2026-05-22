package com.innowise.userservice.service.impl;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.DuplicateResourceException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/changelog-master.yml");
    }

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user = new User("John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", true);
    }

    @Test
    void createUser_ShouldSaveUserToDatabase() throws Exception {
        User savedUser = userService.createUser(user);

        assertNotNull(savedUser.getId());
        assertEquals("john@example.com", savedUser.getEmail());

        User found = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("John", found.getName());
        assertEquals("Doe", found.getSurname());
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowDuplicateResourceException() throws Exception {
        userService.createUser(user);

        User duplicateUser = new User("Jane", "Smith", LocalDate.of(1995, 5, 15), "john@example.com", true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(duplicateUser));
    }

    @Test
    void createUser_NullName_ShouldThrowException() {
        user.setName(null);

        assertThrows(Exception.class, () -> userService.createUser(user));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        User savedUser = userRepository.save(user);

        User found = userService.getUserById(savedUser.getId());

        assertNotNull(found);
        assertEquals(savedUser.getId(), found.getId());
        assertEquals("John", found.getName());
    }

    @Test
    void getUserById_NotFound_ShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_WithPagination_ShouldReturnPage() throws Exception {
        userService.createUser(user);
        userService.createUser(new User("Jane", "Smith", LocalDate.of(1995, 5, 15), "jane@example.com", true));

        Page<User> page = userService.getAllUsers(null, null, PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void getAllUsers_WithNameFilter_ShouldReturnFilteredResults() throws Exception {
        userService.createUser(user);
        userService.createUser(new User("Jane", "Smith", LocalDate.of(1995, 5, 15), "jane@example.com", true));

        Page<User> page = userService.getAllUsers("John", null, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("John", page.getContent().get(0).getName());
    }

    @Test
    void getAllUsers_WithSurnameFilter_ShouldReturnFilteredResults() throws Exception {
        userService.createUser(user);
        userService.createUser(new User("Jane", "Smith", LocalDate.of(1995, 5, 15), "jane@example.com", true));

        Page<User> page = userService.getAllUsers(null, "Smith", PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Smith", page.getContent().get(0).getSurname());
    }

    @Test
    void getAllUsers_WithBothFilters_ShouldReturnFilteredResults() throws Exception {
        userService.createUser(user);
        userService.createUser(new User("Jane", "Smith", LocalDate.of(1995, 5, 15), "jane@example.com", true));

        Page<User> page = userService.getAllUsers("Jane", "Smith", PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Jane", page.getContent().get(0).getName());
        assertEquals("Smith", page.getContent().get(0).getSurname());
    }

    @Test
    void updateUser_ShouldUpdateUser() throws Exception {
        User savedUser = userRepository.save(user);
        savedUser.setName("Johnny");
        savedUser.setSurname("Updated");

        User updated = userService.updateUser(savedUser.getId(), savedUser);

        assertEquals("Johnny", updated.getName());
        assertEquals("Updated", updated.getSurname());

        User fromDb = userRepository.findById(savedUser.getId()).orElse(null);
        assertEquals("Johnny", fromDb.getName());
        assertEquals("Updated", fromDb.getSurname());
    }

    @Test
    void updateUser_NotFound_ShouldThrowResourceNotFoundException() {
        user.setId(999L);

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(999L, user));
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowDuplicateResourceException() throws Exception {
        userService.createUser(user);

        User anotherUser = new User("Jane", "Smith", LocalDate.of(1995, 5, 15), "jane@example.com", true);
        User savedAnother = userService.createUser(anotherUser);

        savedAnother.setEmail("john@example.com");

        assertThrows(DuplicateResourceException.class, () -> userService.updateUser(savedAnother.getId(), savedAnother));
    }

    @Test
    void deleteUser_ShouldDeleteUser() throws Exception {
        User savedUser = userRepository.save(user);

        userService.deleteUser(savedUser.getId());

        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }

    @Test
    void deleteUser_NotFound_ShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999L));
    }

    @Test
    void setActiveStatus_ShouldUpdateUserStatus() throws Exception {
        User savedUser = userRepository.save(user);

        userService.setActiveStatus(savedUser.getId(), false);

        User fromDb = userRepository.findById(savedUser.getId()).orElse(null);
        assertFalse(fromDb.isActive());
    }

    @Test
    void setActiveStatus_NotFound_ShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.setActiveStatus(999L, false));
    }
}