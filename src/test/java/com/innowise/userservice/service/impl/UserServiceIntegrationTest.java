package com.innowise.userservice.service.impl;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.DuplicateResourceException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class UserServiceIntegrationTest {

    private static final String DB_NAME = "testdb";
    private static final String DB_USER_PASS = "test";
    private static final String LIQUIBASE_CHANGELOG = "classpath:db/changelog/changelog-master.yml";

    private static final String USER_EMAIL_1 = "john@example.com";
    private static final String USER_NAME_1 = "John";
    private static final String USER_SURNAME_1 = "Doe";
    private static final String UPDATED_NAME = "Johnny";
    private static final String UPDATED_SURNAME = "Updated";

    private static final String USER_EMAIL_2 = "jane@example.com";
    private static final String USER_NAME_2 = "Jane";
    private static final String USER_SURNAME_2 = "Smith";

    private static final Long NOT_FOUND_ID = 999L;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER_PASS)
            .withPassword(DB_USER_PASS);

    private final UserServiceImpl userService;
    private final UserRepository userRepository;

    UserServiceIntegrationTest(UserServiceImpl userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> LIQUIBASE_CHANGELOG);
    }

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user = new User(USER_NAME_1, USER_SURNAME_1, LocalDate.of(1990, 1, 1), USER_EMAIL_1, true);
    }

    @Test
    void createUserShouldSaveUserToDatabase() throws Exception {
        User savedUser = userService.createUser(user);
        assertNotNull(savedUser.getId());
        assertEquals(USER_EMAIL_1, savedUser.getEmail());

        User found = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(USER_NAME_1, found.getName());
        assertEquals(USER_SURNAME_1, found.getSurname());
    }

    @Test
    void createUserDuplicateEmailShouldThrowDuplicateResourceException() throws Exception {
        userService.createUser(user);
        User duplicateUser = new User(USER_NAME_2, USER_SURNAME_2, LocalDate.of(1995, 5, 15), USER_EMAIL_1, true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(duplicateUser));
    }

    @Test
    void getUserByIdShouldReturnUser() throws Exception {
        User savedUser = userRepository.save(user);
        User found = userService.getUserById(savedUser.getId());

        assertNotNull(found);
        assertEquals(savedUser.getId(), found.getId());
        assertEquals(USER_NAME_1, found.getName());
    }

    @Test
    void getUserByIdNotFoundShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(NOT_FOUND_ID));
    }

    @Test
    void getAllUsersWithPaginationShouldReturnPage() throws Exception {
        userService.createUser(user);
        userService.createUser(new User(USER_NAME_2, USER_SURNAME_2, LocalDate.of(1995, 5, 15), USER_EMAIL_2, true));

        Page<User> page = userService.getAllUsers(null, null, PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void getAllUsersWithNameFilterShouldReturnFilteredResults() throws Exception {
        userService.createUser(user);
        userService.createUser(new User(USER_NAME_2, USER_SURNAME_2, LocalDate.of(1995, 5, 15), USER_EMAIL_2, true));

        Page<User> page = userService.getAllUsers(USER_NAME_1, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals(USER_NAME_1, page.getContent().get(0).getName());
    }

    @Test
    void getAllUsersWithSurnameFilterShouldReturnFilteredResults() throws Exception {
        userService.createUser(user);
        userService.createUser(new User(USER_NAME_2, USER_SURNAME_2, LocalDate.of(1995, 5, 15), USER_EMAIL_2, true));

        Page<User> page = userService.getAllUsers(null, USER_SURNAME_2, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals(USER_SURNAME_2, page.getContent().get(0).getSurname());
    }

    @Test
    void getAllUsersWithBothFiltersShouldReturnFilteredResults() throws Exception {
        userService.createUser(user);
        userService.createUser(new User(USER_NAME_2, USER_SURNAME_2, LocalDate.of(1995, 5, 15), USER_EMAIL_2, true));

        Page<User> page = userService.getAllUsers(USER_NAME_2, USER_SURNAME_2, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals(USER_NAME_2, page.getContent().get(0).getName());
        assertEquals(USER_SURNAME_2, page.getContent().get(0).getSurname());
    }

    @Test
    void updateUserShouldUpdateUser() throws Exception {
        User savedUser = userRepository.save(user);
        savedUser.setName(UPDATED_NAME);
        savedUser.setSurname(UPDATED_SURNAME);

        User updated = userService.updateUser(savedUser.getId(), savedUser);
        assertEquals(UPDATED_NAME, updated.getName());
        assertEquals(UPDATED_SURNAME, updated.getSurname());

        User fromDb = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(UPDATED_NAME, fromDb.getName());
        assertEquals(UPDATED_SURNAME, fromDb.getSurname());
    }

    @Test
    void updateUserNotFoundShouldThrowResourceNotFoundException() {
        user.setId(NOT_FOUND_ID);
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(NOT_FOUND_ID, user));
    }

    @Test
    void updateUserWithDuplicateEmailShouldThrowDuplicateResourceException() throws Exception {
        userService.createUser(user);
        User anotherUser = new User(USER_NAME_2, USER_SURNAME_2, LocalDate.of(1995, 5, 15), USER_EMAIL_2, true);
        User savedAnother = userService.createUser(anotherUser);
        savedAnother.setEmail(USER_EMAIL_1);

        assertThrows(DuplicateResourceException.class, () -> userService.updateUser(savedAnother.getId(), savedAnother));
    }

    @Test
    void deleteUserShouldDeleteUser() throws Exception {
        User savedUser = userRepository.save(user);
        userService.deleteUser(savedUser.getId());
        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }

    @Test
    void deleteUserNotFoundShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(NOT_FOUND_ID));
    }

    @Test
    void setActiveStatusShouldUpdateUserStatus() throws Exception {
        User savedUser = userRepository.save(user);
        userService.setActiveStatus(savedUser.getId(), false);

        User fromDb = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(fromDb);
        assertFalse(fromDb.isActive());
    }

    @Test
    void setActiveStatusNotFoundShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.setActiveStatus(NOT_FOUND_ID, false));
    }
}