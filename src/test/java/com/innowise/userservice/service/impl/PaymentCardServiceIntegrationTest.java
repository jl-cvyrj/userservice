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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class PaymentCardServiceIntegrationTest {

    private static final String DEFAULT_CARD_NUMBER = "1234567890123456";
    private static final String ALTERNATIVE_CARD_NUMBER = "9999888877776666";
    private static final String LOOP_CARD_PREFIX = "11112222333344";

    private static final String HOLDER_JOHN = "John Doe";
    private static final String HOLDER_JOHNNY = "Johnny Doe";

    private static final String USER_EMAIL = "john@example.com";
    private static final String USER_NAME = "John";
    private static final String USER_SURNAME = "Doe";

    private static final String DB_NAME = "testdb";
    private static final String DB_USER_PASS = "test";
    private static final String LIQUIBASE_CHANGELOG = "classpath:db/changelog/changelog-master.yml";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER_PASS)
            .withPassword(DB_USER_PASS);

    @Autowired
    private PaymentCardServiceImpl cardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository cardRepository;

    private User user;
    private PaymentCardDto paymentCardDto;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> LIQUIBASE_CHANGELOG);
    }

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        user = new User(USER_NAME, USER_SURNAME, LocalDate.of(1990, 1, 1), USER_EMAIL, true);
        user = userRepository.save(user);

        paymentCardDto = new PaymentCardDto();
        paymentCardDto.setUserId(user.getId());
        paymentCardDto.setNumber(DEFAULT_CARD_NUMBER);
        paymentCardDto.setHolder(HOLDER_JOHN);
        paymentCardDto.setExpirationDate(LocalDate.of(2028, 12, 31));
        paymentCardDto.setActive(true);
    }

    @Test
    void createPaymentCardShouldSaveCardToDatabase() throws Exception {
        PaymentCard saved = cardService.createPaymentCard(paymentCardDto);

        assertNotNull(saved.getId());
        assertEquals(DEFAULT_CARD_NUMBER, saved.getNumber());

        PaymentCard fromDb = cardRepository.findById(saved.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(HOLDER_JOHN, fromDb.getHolder());
        assertEquals(user.getId(), fromDb.getUser().getId());
    }

    @Test
    void createPaymentCardUserNotFoundShouldThrowResourceNotFoundException() {
        paymentCardDto.setUserId(999L);

        assertThrows(ResourceNotFoundException.class, () -> cardService.createPaymentCard(paymentCardDto));
    }

    @Test
    void createPaymentCardMoreThan5CardsShouldThrowBusinessLogicException() throws Exception {
        for (int i = 0; i < 5; i++) {
            PaymentCardDto cardDto = new PaymentCardDto();
            cardDto.setUserId(user.getId());
            cardDto.setNumber(LOOP_CARD_PREFIX + i);
            cardDto.setHolder(HOLDER_JOHN);
            cardDto.setExpirationDate(LocalDate.of(2028, 12, 31));
            cardDto.setActive(true);
            cardService.createPaymentCard(cardDto);
        }

        PaymentCardDto sixthCard = new PaymentCardDto();
        sixthCard.setUserId(user.getId());
        sixthCard.setNumber(ALTERNATIVE_CARD_NUMBER);
        sixthCard.setHolder(HOLDER_JOHN);
        sixthCard.setExpirationDate(LocalDate.of(2028, 12, 31));
        sixthCard.setActive(true);

        assertThrows(BusinessLogicException.class, () -> cardService.createPaymentCard(sixthCard));

        long count = cardRepository.countPaymentCardsByUserId(user.getId());
        assertEquals(5, count);
    }

    @Test
    void getPaymentCardByIdShouldReturnCard() throws Exception {
        PaymentCard saved = cardService.createPaymentCard(paymentCardDto);

        PaymentCard found = cardService.getPaymentCardById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals(DEFAULT_CARD_NUMBER, found.getNumber());
    }

    @Test
    void getPaymentCardByIdNotFoundShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> cardService.getPaymentCardById(999L));
    }

    @Test
    void getCardsByUserIdShouldReturnListOfCards() throws Exception {
        cardService.createPaymentCard(paymentCardDto);

        PaymentCardDto card2Dto = new PaymentCardDto();
        card2Dto.setUserId(user.getId());
        card2Dto.setNumber(ALTERNATIVE_CARD_NUMBER);
        card2Dto.setHolder(HOLDER_JOHN);
        card2Dto.setExpirationDate(LocalDate.of(2028, 12, 31));
        card2Dto.setActive(true);
        cardService.createPaymentCard(card2Dto);

        List<PaymentCard> cards = cardService.getCardsByUserId(user.getId());

        assertEquals(2, cards.size());
    }

    @Test
    void getCardsByUserIdNoCardsShouldReturnEmptyList() {
        List<PaymentCard> cards = cardService.getCardsByUserId(user.getId());

        assertEquals(0, cards.size());
    }

    @Test
    void updatePaymentCardShouldUpdateCard() throws Exception {
        PaymentCard saved = cardService.createPaymentCard(paymentCardDto);

        PaymentCardDto updateDto = new PaymentCardDto();
        updateDto.setNumber(ALTERNATIVE_CARD_NUMBER);
        updateDto.setHolder(HOLDER_JOHNNY);
        updateDto.setExpirationDate(LocalDate.of(2030, 12, 31));
        updateDto.setActive(false);

        PaymentCard updated = cardService.updatePaymentCard(saved.getId(), updateDto);

        assertEquals(ALTERNATIVE_CARD_NUMBER, updated.getNumber());
        assertEquals(HOLDER_JOHNNY, updated.getHolder());
        assertFalse(updated.isActive());

        PaymentCard fromDb = cardRepository.findById(saved.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(ALTERNATIVE_CARD_NUMBER, fromDb.getNumber());
        assertFalse(fromDb.isActive());
    }

    @Test
    void updatePaymentCardNotFoundShouldThrowResourceNotFoundException() {
        PaymentCardDto updateDto = new PaymentCardDto();
        updateDto.setNumber(ALTERNATIVE_CARD_NUMBER);

        assertThrows(ResourceNotFoundException.class, () -> cardService.updatePaymentCard(999L, updateDto));
    }

    @Test
    void setActiveStatusShouldUpdateCardStatus() throws Exception {
        PaymentCard saved = cardService.createPaymentCard(paymentCardDto);

        cardService.setActiveStatus(saved.getId(), false);

        PaymentCard fromDb = cardRepository.findById(saved.getId()).orElse(null);
        assertNotNull(fromDb);
        assertFalse(fromDb.isActive());
    }

    @Test
    void setActiveStatusNotFoundShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> cardService.setActiveStatus(999L, false));
    }
}