package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.BusinessLogicException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.exception.ServiceException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final PaymentCardService paymentCardService;
    private final UserMapper userMapper;
    private final PaymentCardMapper paymentCardMapper;

    public AdminController(UserService userService, PaymentCardService paymentCardService, UserMapper userMapper, PaymentCardMapper paymentCardMapper) {
        this.userService = userService;
        this.paymentCardService = paymentCardService;
        this.userMapper = userMapper;
        this.paymentCardMapper = paymentCardMapper;
    }

    @GetMapping("/users")
    public Page<UserDto> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.getAllUsers(null, null, PageRequest.of(page, size))
                .map(userMapper::toDto);
    }

    @GetMapping("/users/{id}")
    public UserDto getUserById(@PathVariable Long id) throws ResourceNotFoundException {
        User user = userService.getUserById(id);
        return userMapper.toDto(user);
    }

    @PutMapping("/users/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) throws ServiceException {
        User user = userMapper.toEntity(userDto);
        User updated = userService.updateUser(id, user);
        return userMapper.toDto(updated);
    }

    @PatchMapping("/users/{id}/activate")
    public void activateUser(@PathVariable Long id) throws ResourceNotFoundException {
        userService.setActiveStatus(id, true);
    }

    @PatchMapping("/users/{id}/deactivate")
    public void deactivateUser(@PathVariable Long id) throws ResourceNotFoundException {
        userService.setActiveStatus(id, false);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) throws ResourceNotFoundException {
        userService.deleteUser(id);
    }

    @GetMapping("/users/{userId}/cards")
    public List<PaymentCardDto> getUserCards(@PathVariable Long userId) {
        List<PaymentCard> cards = paymentCardService.getCardsByUserId(userId);
        return cards.stream()
                .map(paymentCardMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/cards")
    public Page<PaymentCardDto> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String holder) {
        return paymentCardService.getAllPaymentCards(holder, PageRequest.of(page, size))
                .map(paymentCardMapper::toDto);
    }

    @GetMapping("/cards/{id}")
    public PaymentCardDto getCardById(@PathVariable Long id) throws ResourceNotFoundException {
        PaymentCard card = paymentCardService.getPaymentCardById(id);
        return paymentCardMapper.toDto(card);
    }

    @PostMapping("/cards")
    public PaymentCardDto createCard(@RequestBody PaymentCardDto cardDto) throws BusinessLogicException, ResourceNotFoundException {
        PaymentCard card = paymentCardService.createPaymentCard(cardDto);
        return paymentCardMapper.toDto(card);
    }

    @PutMapping("/cards/{id}")
    public PaymentCardDto updateCard(@PathVariable Long id, @RequestBody PaymentCardDto cardDto) throws ResourceNotFoundException {
        PaymentCard card = paymentCardService.updatePaymentCard(id, cardDto);
        return paymentCardMapper.toDto(card);
    }

    @PatchMapping("/cards/{id}/activate")
    public void activateCard(@PathVariable Long id) throws ResourceNotFoundException {
        paymentCardService.setActiveStatus(id, true);
    }

    @PatchMapping("/cards/{id}/deactivate")
    public void deactivateCard(@PathVariable Long id) throws ResourceNotFoundException {
        paymentCardService.setActiveStatus(id, false);
    }
}