package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.PaymentCardDto;
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
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    private User user;
    private PaymentCardDto paymentCardDto;
    private PaymentCard savedCard;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", true);
        user.setId(1L);

        paymentCardDto = new PaymentCardDto();
        paymentCardDto.setUserId(user.getId());
        paymentCardDto.setNumber("1234567890123456");
        paymentCardDto.setHolder("John Doe");
        paymentCardDto.setExpirationDate(LocalDate.of(2028, 12, 31));
        paymentCardDto.setActive(true);

        savedCard = new PaymentCard(user, "1234567890123456", "John Doe", LocalDate.of(2028, 12, 31), true);
        savedCard.setId(1L);
    }

    @Test
    void createPaymentCard_Success_ShouldReturnCard() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countPaymentCardsByUserId(1L)).thenReturn(2);
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(savedCard);

        PaymentCard result = paymentCardService.createPaymentCard(paymentCardDto);

        assertNotNull(result);
        assertEquals("1234567890123456", result.getNumber());
    }

    @Test
    void createPaymentCard_MoreThan5Cards_ShouldThrowBusinessLogicException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countPaymentCardsByUserId(1L)).thenReturn(5);

        assertThrows(BusinessLogicException.class, () -> paymentCardService.createPaymentCard(paymentCardDto));
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void createPaymentCard_UserNotFound_ShouldThrowResourceNotFoundException() throws Exception {
        paymentCardDto.setUserId(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.createPaymentCard(paymentCardDto));
    }

    @Test
    void getPaymentCardById_Success_ShouldReturnCard() throws Exception {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(savedCard));

        PaymentCard result = paymentCardService.getPaymentCardById(1L);

        assertNotNull(result);
        assertEquals("1234567890123456", result.getNumber());
    }

    @Test
    void getPaymentCardById_NotFound_ShouldThrowResourceNotFoundException() {
        when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.getPaymentCardById(999L));
    }

    @Test
    void updatePaymentCard_Success_ShouldReturnUpdatedCard() throws Exception {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(savedCard));
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(savedCard);

        PaymentCardDto updateDto = new PaymentCardDto();
        updateDto.setNumber("9999888877776666");
        updateDto.setHolder("Johnny Doe");
        updateDto.setExpirationDate(LocalDate.of(2030, 12, 31));
        updateDto.setActive(false);

        PaymentCard result = paymentCardService.updatePaymentCard(1L, updateDto);

        assertEquals("9999888877776666", result.getNumber());
        assertFalse(result.isActive());
    }

    @Test
    void updatePaymentCard_NotFound_ShouldThrowResourceNotFoundException() {
        when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

        PaymentCardDto updateDto = new PaymentCardDto();
        updateDto.setNumber("9999888877776666");

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.updatePaymentCard(999L, updateDto));
    }

    @Test
    void setActiveStatus_Success_ShouldUpdateStatus() throws Exception {
        when(paymentCardRepository.existsById(1L)).thenReturn(true);
        doNothing().when(paymentCardRepository).setActiveStatus(1L, false);

        assertDoesNotThrow(() -> paymentCardService.setActiveStatus(1L, false));
        verify(paymentCardRepository).setActiveStatus(1L, false);
    }

    @Test
    void setActiveStatus_NotFound_ShouldThrowResourceNotFoundException() throws Exception {
        when(paymentCardRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.setActiveStatus(999L, false));
    }

    @Test
    void getCardsByUserId_ShouldReturnList() {
        when(paymentCardRepository.findByUser_Id(1L)).thenReturn(List.of(savedCard));

        List<PaymentCard> result = paymentCardService.getCardsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentCardRepository).findByUser_Id(1L);
    }

    @Test
    void getCardsByUserId_NoCards_ShouldReturnEmptyList() {
        when(paymentCardRepository.findByUser_Id(1L)).thenReturn(List.of());

        List<PaymentCard> result = paymentCardService.getCardsByUserId(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getAllPaymentCards_WithHolderFilter_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> expectedPage = new PageImpl<>(List.of(savedCard));

        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<PaymentCard> result = paymentCardService.getAllPaymentCards("John", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
    }
}