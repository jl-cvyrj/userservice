package com.innowise.userservice.service.impl;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.DuplicateResourceException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String USER_EMAIL = "john@example.com";
    private static final String USER_NAME = "John";
    private static final String USER_SURNAME = "Doe";
    private static final String UPDATED_NAME = "Johnny";

    private static final Long VALID_ID = 1L;
    private static final Long NOT_FOUND_ID = 999L;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(USER_NAME, USER_SURNAME, LocalDate.of(1990, 1, 1), USER_EMAIL, true);
        user.setId(VALID_ID);
    }

    @Test
    void createUserSuccessShouldReturnUser() throws Exception {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertEquals(USER_EMAIL, result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserDuplicateEmailShouldThrowDuplicateResourceException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWithNullNameShouldStillSave() throws Exception {
        user.setName(null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
    }

    @Test
    void getUserByIdSuccessShouldReturnUser() throws Exception {
        when(userRepository.findByIdWithCards(VALID_ID)).thenReturn(Optional.of(user));

        User result = userService.getUserById(VALID_ID);

        assertNotNull(result);
        assertEquals(USER_NAME, result.getName());
    }

    @Test
    void getUserByIdNotFoundShouldThrowResourceNotFoundException() {
        when(userRepository.findByIdWithCards(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(NOT_FOUND_ID));
    }

    @Test
    void updateUserSuccessShouldReturnUpdatedUser() throws Exception {
        User updatedUser = new User(UPDATED_NAME, USER_SURNAME, LocalDate.of(1990, 1, 1), USER_EMAIL, true);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(VALID_ID, updatedUser);

        assertEquals(UPDATED_NAME, result.getName());
    }

    @Test
    void deleteUserSuccessShouldDeleteUser() {
        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        assertDoesNotThrow(() -> userService.deleteUser(VALID_ID));
        verify(userRepository).delete(any(User.class));
    }

    @Test
    void setActiveStatusSuccessShouldUpdateStatus() {
        when(userRepository.existsById(VALID_ID)).thenReturn(true);
        doNothing().when(userRepository).setActiveStatus(VALID_ID, false);

        assertDoesNotThrow(() -> userService.setActiveStatus(VALID_ID, false));
        verify(userRepository).setActiveStatus(VALID_ID, false);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllPaymentCardsWithHolderFilterShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<User> result = userService.getAllUsers(USER_NAME, USER_SURNAME, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(USER_NAME, result.getContent().get(0).getName());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }
}