package com.innowise.userservice.service.impl;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.*;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", true);
        user.setId(1L);
    }

    @Test
    void createUser_Success_ShouldReturnUser() throws Exception {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowDuplicateResourceException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithNullName_ShouldStillSave() throws Exception {
        user.setName(null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
    }

    @Test
    void getUserById_Success_ShouldReturnUser() throws Exception {
        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    void getUserById_NotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByIdWithCards(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void updateUser_Success_ShouldReturnUpdatedUser() throws Exception {
        User updatedUser = new User("Johnny", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(1L, updatedUser);

        assertEquals("Johnny", result.getName());
    }

    @Test
    void deleteUser_Success_ShouldDeleteUser() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).delete(any(User.class));
    }

    @Test
    void setActiveStatus_Success_ShouldUpdateStatus() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).setActiveStatus(1L, false);

        assertDoesNotThrow(() -> userService.setActiveStatus(1L, false));
        verify(userRepository).setActiveStatus(1L, false);
    }

    @Test
    void getAllUsers_WithNameAndSurnameFilters_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<User> result = userService.getAllUsers("John", "Doe", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getName());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }
}