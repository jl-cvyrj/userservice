package com.innowise.userservice.service.impl;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.*;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    private User user;
    private PaymentCard card;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", true);
        user.setId(1L);

        card = new PaymentCard(user, "1234567890123456", "John Doe", LocalDate.of(2028, 12, 31), true);
        card.setId(1L);
    }

    @Test
    void createPaymentCard_Success_ShouldReturnCard() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countActiveCardsByUserId(1L)).thenReturn(2);
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(card);

        PaymentCard result = paymentCardService.createPaymentCard(card);

        assertNotNull(result);
        assertEquals("1234567890123456", result.getNumber());
    }

    @Test
    void createPaymentCard_MoreThan5Cards_ShouldThrowException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countActiveCardsByUserId(1L)).thenReturn(5);

        assertThrows(BusinessLogicException.class, () -> paymentCardService.createPaymentCard(card));
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void createPaymentCard_UserNotFound_ShouldThrowException() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        card.setUser(user);
        card.getUser().setId(999L);

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.createPaymentCard(card));
    }

    @Test
    void getPaymentCardById_Success_ShouldReturnCard() throws Exception {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));

        PaymentCard result = paymentCardService.getPaymentCardById(1L);

        assertNotNull(result);
        assertEquals("1234567890123456", result.getNumber());
    }

    @Test
    void getPaymentCardById_NotFound_ShouldThrowException() {
        when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.getPaymentCardById(999L));
    }
}