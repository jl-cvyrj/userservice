package com.innowise.userservice.service.impl;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PaymentCardServiceIntegrationTest {

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
    private PaymentCardServiceImpl cardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository cardRepository;

    private User user;
    private PaymentCard card;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        user = new User("John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", true);
        user = userRepository.save(user);

        card = new PaymentCard(user, "1234567890123456", "John Doe", LocalDate.of(2028, 12, 31), true);
    }

    @Test
    void createPaymentCard_ShouldSaveCardToDatabase() throws Exception {
        PaymentCard saved = cardService.createPaymentCard(card);

        assertNotNull(saved.getId());
        assertEquals("1234567890123456", saved.getNumber());

        PaymentCard fromDb = cardRepository.findById(saved.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("John Doe", fromDb.getHolder());
    }

    @Test
    void createPaymentCard_UserNotFound_ShouldThrowResourceNotFoundException() {
        card.getUser().setId(999L);

        assertThrows(ResourceNotFoundException.class, () -> cardService.createPaymentCard(card));
    }

    @Test
    void createPaymentCard_MoreThan5ActiveCards_ShouldThrowBusinessLogicException() throws Exception {
        for (int i = 0; i < 5; i++) {
            PaymentCard c = new PaymentCard(user, "11112222333344" + i, "John Doe", LocalDate.of(2028, 12, 31), true);
            cardService.createPaymentCard(c);
        }

        PaymentCard sixthCard = new PaymentCard(user, "9999888877776666", "John Doe", LocalDate.of(2028, 12, 31), true);

        assertThrows(BusinessLogicException.class, () -> cardService.createPaymentCard(sixthCard));

        long count = cardRepository.countActiveCardsByUserId(user.getId());
        assertEquals(5, count);
    }

    @Test
    void getPaymentCardById_ShouldReturnCard() throws Exception {
        PaymentCard saved = cardService.createPaymentCard(card);

        PaymentCard found = cardService.getPaymentCardById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("1234567890123456", found.getNumber());
    }

    @Test
    void getPaymentCardById_NotFound_ShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> cardService.getPaymentCardById(999L));
    }

    @Test
    void getCardsByUserId_ShouldReturnListOfCards() throws Exception {
        cardService.createPaymentCard(card);
        PaymentCard card2 = new PaymentCard(user, "9999888877776666", "John Doe", LocalDate.of(2028, 12, 31), true);
        cardService.createPaymentCard(card2);

        List<PaymentCard> cards = cardService.getCardsByUserId(user.getId());

        assertEquals(2, cards.size());
    }

    @Test
    void getCardsByUserId_NoCards_ShouldReturnEmptyList() {
        List<PaymentCard> cards = cardService.getCardsByUserId(user.getId());

        assertEquals(0, cards.size());
    }

    @Test
    void updatePaymentCard_ShouldUpdateCard() throws Exception {
        PaymentCard saved = cardService.createPaymentCard(card);
        saved.setNumber("9999888877776666");
        saved.setActive(false);

        PaymentCard updated = cardService.updatePaymentCard(saved.getId(), saved);

        assertEquals("9999888877776666", updated.getNumber());
        assertFalse(updated.isActive());

        PaymentCard fromDb = cardRepository.findById(saved.getId()).orElse(null);
        assertEquals("9999888877776666", fromDb.getNumber());
        assertFalse(fromDb.isActive());
    }

    @Test
    void updatePaymentCard_NotFound_ShouldThrowResourceNotFoundException() {
        card.setId(999L);

        assertThrows(ResourceNotFoundException.class, () -> cardService.updatePaymentCard(999L, card));
    }

    @Test
    void setActiveStatus_ShouldUpdateCardStatus() throws Exception {
        PaymentCard saved = cardService.createPaymentCard(card);

        cardService.setActiveStatus(saved.getId(), false);

        PaymentCard fromDb = cardRepository.findById(saved.getId()).orElse(null);
        assertFalse(fromDb.isActive());
    }

    @Test
    void setActiveStatus_NotFound_ShouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> cardService.setActiveStatus(999L, false));
    }
}