package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.BusinessLogicException;
import com.innowise.userservice.exception.ResourceNotFoundException;
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

    private static final String DEFAULT_CARD_NUMBER = "1234567890123456";
    private static final String ALTERNATIVE_CARD_NUMBER = "9999888877776666";

    private static final String HOLDER_JOHN = "John Doe";
    private static final String HOLDER_JOHNNY = "Johnny Doe";

    private static final String USER_EMAIL = "john@example.com";
    private static final String USER_NAME = "John";
    private static final String USER_SURNAME = "Doe";

    private static final Long VALID_ID = 1L;
    private static final Long NOT_FOUND_ID = 999L;

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
        user = new User(USER_NAME, USER_SURNAME, LocalDate.of(1990, 1, 1), USER_EMAIL, true);
        user.setId(VALID_ID);

        paymentCardDto = new PaymentCardDto();
        paymentCardDto.setUserId(user.getId());
        paymentCardDto.setNumber(DEFAULT_CARD_NUMBER);
        paymentCardDto.setHolder(HOLDER_JOHN);
        paymentCardDto.setExpirationDate(LocalDate.of(2028, 12, 31));
        paymentCardDto.setActive(true);

        savedCard = new PaymentCard(user, DEFAULT_CARD_NUMBER, HOLDER_JOHN, LocalDate.of(2028, 12, 31), true);
        savedCard.setId(VALID_ID);
    }

    @Test
    void createPaymentCardSuccessShouldReturnCard() throws Exception {
        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countPaymentCardsByUserId(VALID_ID)).thenReturn(2);
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(savedCard);

        PaymentCard result = paymentCardService.createPaymentCard(paymentCardDto);

        assertNotNull(result);
        assertEquals(DEFAULT_CARD_NUMBER, result.getNumber());
    }

    @Test
    void createPaymentCardMoreThan5CardsShouldThrowBusinessLogicException() {
        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countPaymentCardsByUserId(VALID_ID)).thenReturn(5);

        assertThrows(BusinessLogicException.class, () -> paymentCardService.createPaymentCard(paymentCardDto));
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void createPaymentCardUserNotFoundShouldThrowResourceNotFoundException() {
        paymentCardDto.setUserId(NOT_FOUND_ID);
        when(userRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.createPaymentCard(paymentCardDto));
    }

    @Test
    void getPaymentCardByIdSuccessShouldReturnCard() throws Exception {
        when(paymentCardRepository.findById(VALID_ID)).thenReturn(Optional.of(savedCard));

        PaymentCard result = paymentCardService.getPaymentCardById(VALID_ID);

        assertNotNull(result);
        assertEquals(DEFAULT_CARD_NUMBER, result.getNumber());
    }

    @Test
    void getPaymentCardByIdNotFoundShouldThrowResourceNotFoundException() {
        when(paymentCardRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.getPaymentCardById(NOT_FOUND_ID));
    }

    @Test
    void updatePaymentCardSuccessShouldReturnUpdatedCard() throws Exception {
        when(paymentCardRepository.findById(VALID_ID)).thenReturn(Optional.of(savedCard));
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(savedCard);

        PaymentCardDto updateDto = new PaymentCardDto();
        updateDto.setNumber(ALTERNATIVE_CARD_NUMBER);
        updateDto.setHolder(HOLDER_JOHNNY);
        updateDto.setExpirationDate(LocalDate.of(2030, 12, 31));
        updateDto.setActive(false);

        PaymentCard result = paymentCardService.updatePaymentCard(VALID_ID, updateDto);

        assertEquals(ALTERNATIVE_CARD_NUMBER, result.getNumber());
        assertFalse(result.isActive());
    }

    @Test
    void updatePaymentCardNotFoundShouldThrowResourceNotFoundException() {
        when(paymentCardRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        PaymentCardDto updateDto = new PaymentCardDto();
        updateDto.setNumber(ALTERNATIVE_CARD_NUMBER);

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.updatePaymentCard(NOT_FOUND_ID, updateDto));
    }

    @Test
    void setActiveStatusSuccessShouldUpdateStatus() {
        when(paymentCardRepository.existsById(VALID_ID)).thenReturn(true);
        doNothing().when(paymentCardRepository).setActiveStatus(VALID_ID, false);

        assertDoesNotThrow(() -> paymentCardService.setActiveStatus(VALID_ID, false));
        verify(paymentCardRepository).setActiveStatus(VALID_ID, false);
    }

    @Test
    void setActiveStatusNotFoundShouldThrowResourceNotFoundException() {
        when(paymentCardRepository.existsById(NOT_FOUND_ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.setActiveStatus(NOT_FOUND_ID, false));
    }

    @Test
    void getCardsByUserIdShouldReturnList() {
        when(paymentCardRepository.findByUser_Id(VALID_ID)).thenReturn(List.of(savedCard));

        List<PaymentCard> result = paymentCardService.getCardsByUserId(VALID_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentCardRepository).findByUser_Id(VALID_ID);
    }

    @Test
    void getCardsByUserIdNoCardsShouldReturnEmptyList() {
        when(paymentCardRepository.findByUser_Id(VALID_ID)).thenReturn(List.of());

        List<PaymentCard> result = paymentCardService.getCardsByUserId(VALID_ID);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllPaymentCardsWithHolderFilterShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> expectedPage = new PageImpl<>(List.of(savedCard));
        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<PaymentCard> result = paymentCardService.getAllPaymentCards(USER_NAME, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
    }
}