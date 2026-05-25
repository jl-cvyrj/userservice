package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.*;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentCardService paymentCardService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) throws DuplicateResourceException, ServiceException {
        User user = userMapper.toEntity(userDto);
        User savedUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(savedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) throws ResourceNotFoundException {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.getAllUsers(name, surname, PageRequest.of(page, size));
        return ResponseEntity.ok(users.map(userMapper::toDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) throws ResourceNotFoundException, DuplicateResourceException, ServiceException {
        User user = userMapper.toEntity(userDto);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActiveStatus(@PathVariable Long id, @RequestParam boolean active) throws ResourceNotFoundException {
        userService.setActiveStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/payment-cards")
    public ResponseEntity<List<PaymentCardDto>> getCardsByUserId(@PathVariable Long userId) {
        List<PaymentCard> cards = paymentCardService.getCardsByUserId(userId);
        List<PaymentCardDto> cardDtos = cards.stream()
                .map(paymentCardMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cardDtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws ResourceNotFoundException {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}