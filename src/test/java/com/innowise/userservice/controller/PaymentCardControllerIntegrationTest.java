package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PaymentCardControllerIntegrationTest {

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
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository cardRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        UserDto userDto = new UserDto();
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail("john@example.com");
        userDto.setActive(true);

        userId = restTemplate.postForEntity("/api/users", userDto, UserDto.class).getBody().getId();
    }

    @Test
    void createCard_ShouldReturn201() {
        PaymentCardDto cardDto = createCardDto();

        ResponseEntity<PaymentCardDto> response = restTemplate.postForEntity("/api/payment-cards", cardDto, PaymentCardDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("1234567890123456", response.getBody().getNumber());
    }

    @Test
    void createCard_UserNotFound_ShouldReturn404() {
        PaymentCardDto cardDto = createCardDto();
        cardDto.setUserId(999L);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/payment-cards", cardDto, Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createCard_MoreThan5Cards_ShouldReturn400() {
        for (int i = 0; i < 5; i++) {
            PaymentCardDto cardDto = createCardDto();
            cardDto.setNumber("11112222333344" + i);
            restTemplate.postForEntity("/api/payment-cards", cardDto, PaymentCardDto.class);
        }

        PaymentCardDto sixthCard = createCardDto();
        sixthCard.setNumber("9999888877776666");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/payment-cards", sixthCard, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("error").toString().contains("cannot have more than 5"));
    }

    @Test
    void getCardById_ShouldReturn200() {
        PaymentCardDto saved = createCard();

        ResponseEntity<PaymentCardDto> response = restTemplate.getForEntity("/api/payment-cards/" + saved.getId(), PaymentCardDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getId(), response.getBody().getId());
    }

    @Test
    void getCardById_NotFound_ShouldReturn404() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/payment-cards/999", Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCardsByUserId_ShouldReturn200() {
        createCard();

        ResponseEntity<List> response = restTemplate.getForEntity("/api/payment-cards/user/" + userId, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }

    @Test
    void getCardsByUserId_NoCards_ShouldReturnEmptyList() {
        ResponseEntity<List> response = restTemplate.getForEntity("/api/payment-cards/user/" + userId, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void updateCard_ShouldReturn200() {
        PaymentCardDto saved = createCard();
        saved.setNumber("9999888877776666");
        saved.setActive(false);

        ResponseEntity<PaymentCardDto> response = restTemplate.exchange(
                "/api/payment-cards/" + saved.getId(),
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(saved),
                PaymentCardDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("9999888877776666", response.getBody().getNumber());
        assertFalse(response.getBody().isActive());
    }

    @Test
    void updateCard_NotFound_ShouldReturn404() {
        PaymentCardDto cardDto = createCardDto();
        cardDto.setId(999L);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/payment-cards/999",
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(cardDto),
                Map.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void setActiveStatus_ShouldReturn204() {
        PaymentCardDto saved = createCard();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/payment-cards/" + saved.getId() + "/active?active=false",
                HttpMethod.PATCH,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<PaymentCardDto> getResponse = restTemplate.getForEntity("/api/payment-cards/" + saved.getId(), PaymentCardDto.class);
        assertFalse(getResponse.getBody().isActive());
    }

    @Test
    void setActiveStatus_NotFound_ShouldReturn404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/payment-cards/999/active?active=false",
                HttpMethod.PATCH,
                null,
                Map.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private PaymentCardDto createCardDto() {
        PaymentCardDto cardDto = new PaymentCardDto();
        cardDto.setUserId(userId);
        cardDto.setNumber("1234567890123456");
        cardDto.setHolder("John Doe");
        cardDto.setExpirationDate(LocalDate.of(2028, 12, 31));
        cardDto.setActive(true);
        return cardDto;
    }

    private PaymentCardDto createCard() {
        PaymentCardDto cardDto = createCardDto();
        ResponseEntity<PaymentCardDto> response = restTemplate.postForEntity("/api/payment-cards", cardDto, PaymentCardDto.class);
        return response.getBody();
    }
}