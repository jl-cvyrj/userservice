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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PaymentCardControllerIntegrationTest {

    private static final String API_PAYMENT_CARDS = "/api/payment-cards";
    private static final String API_PAYMENT_CARDS_PATH = "/api/payment-cards/";
    private static final String API_USERS_PATH = "/api/users/";
    private static final String API_USERS = "/api/users";

    private static final String CARD_NUM_PREFIX = "111122223333440";
    private static final String DEFAULT_CARD_NUMBER = "1234567890123456";
    private static final String ALTERNATIVE_CARD_NUMBER = "9999888877776666";

    private static final String ID_999 = "999";
    private static final String PAYMENT_CARDS_SUBPATH = "/payment-cards";
    private static final String ACTIVE_FALSE_PARAM = "/active?active=false";

    private static final String DB_NAME = "testdb";
    private static final String DB_USER_PASS = "test";
    private static final String LIQUIBASE_CHANGELOG = "classpath:db/changelog/changelog-master.yml";
    private static final String USER_NAME = "John";
    private static final String USER_SURNAME = "Doe";
    private static final String USER_EMAIL = "john@example.com";
    private static final String HOLDER_NAME = "John Doe";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER_PASS)
            .withPassword(DB_USER_PASS);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> LIQUIBASE_CHANGELOG);
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
        userDto.setName(USER_NAME);
        userDto.setSurname(USER_SURNAME);
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail(USER_EMAIL);
        userDto.setActive(true);

        userId = restTemplate.postForEntity(API_USERS, userDto, UserDto.class).getBody().getId();
    }

    @Test
    void createCardShouldReturn201() {
        PaymentCardDto cardDto = createCardDto();

        ResponseEntity<PaymentCardDto> response = restTemplate.postForEntity(API_PAYMENT_CARDS, cardDto, PaymentCardDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(DEFAULT_CARD_NUMBER, response.getBody().getNumber());
    }

    @Test
    void createCardUserNotFoundShouldReturn404() {
        PaymentCardDto cardDto = createCardDto();
        cardDto.setUserId(999L);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_PAYMENT_CARDS,
                HttpMethod.POST,
                new HttpEntity<>(cardDto),
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createCardMoreThan5CardsShouldReturn400() {
        for (int i = 0; i < 5; i++) {
            PaymentCardDto cardDto = createCardDto();
            cardDto.setNumber(CARD_NUM_PREFIX + i);
            restTemplate.postForEntity(API_PAYMENT_CARDS, cardDto, PaymentCardDto.class);
        }

        PaymentCardDto sixthCard = createCardDto();
        sixthCard.setNumber(ALTERNATIVE_CARD_NUMBER);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_PAYMENT_CARDS,
                HttpMethod.POST,
                new HttpEntity<>(sixthCard),
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getCardByIdShouldReturn200() {
        PaymentCardDto saved = createCard();

        ResponseEntity<PaymentCardDto> response = restTemplate.getForEntity(API_PAYMENT_CARDS_PATH + saved.getId(), PaymentCardDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getId(), response.getBody().getId());
    }

    @Test
    void getCardByIdNotFoundShouldReturn404() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_PAYMENT_CARDS_PATH + ID_999,
                HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCardsByUserIdShouldReturn200() {
        createCard();

        ResponseEntity<PaymentCardDto[]> response = restTemplate.getForEntity(
                API_USERS_PATH + userId + PAYMENT_CARDS_SUBPATH, PaymentCardDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    void getCardsByUserIdNoCardsShouldReturnEmptyList() {
        ResponseEntity<PaymentCardDto[]> response = restTemplate.getForEntity(
                API_USERS_PATH + userId + PAYMENT_CARDS_SUBPATH, PaymentCardDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void updateCardShouldReturn200() {
        PaymentCardDto saved = createCard();
        saved.setNumber(ALTERNATIVE_CARD_NUMBER);
        saved.setActive(false);

        ResponseEntity<PaymentCardDto> response = restTemplate.exchange(
                API_PAYMENT_CARDS_PATH + saved.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(saved),
                PaymentCardDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ALTERNATIVE_CARD_NUMBER, response.getBody().getNumber());
        falseAssert(response.getBody().isActive());
    }

    private void falseAssert(boolean condition) {
        assertFalse(condition);
    }

    @Test
    void updateCardNotFoundShouldReturn404() {
        PaymentCardDto cardDto = createCardDto();
        cardDto.setId(999L);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_PAYMENT_CARDS_PATH + ID_999,
                HttpMethod.PUT,
                new HttpEntity<>(cardDto),
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void setActiveStatusShouldReturn204() {
        PaymentCardDto saved = createCard();

        ResponseEntity<Void> response = restTemplate.exchange(
                API_PAYMENT_CARDS_PATH + saved.getId() + ACTIVE_FALSE_PARAM,
                HttpMethod.PATCH,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<PaymentCardDto> getResponse = restTemplate.getForEntity(
                API_PAYMENT_CARDS_PATH + saved.getId(), PaymentCardDto.class);
        assertNotNull(getResponse.getBody());
        assertFalse(getResponse.getBody().isActive());
    }

    @Test
    void setActiveStatusNotFoundShouldReturn404() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_PAYMENT_CARDS_PATH + ID_999 + ACTIVE_FALSE_PARAM,
                HttpMethod.PATCH,
                null,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private PaymentCardDto createCardDto() {
        PaymentCardDto cardDto = new PaymentCardDto();
        cardDto.setUserId(userId);
        cardDto.setNumber(DEFAULT_CARD_NUMBER);
        cardDto.setHolder(HOLDER_NAME);
        cardDto.setExpirationDate(LocalDate.of(2028, 12, 31));
        cardDto.setActive(true);
        return cardDto;
    }

    private PaymentCardDto createCard() {
        PaymentCardDto cardDto = createCardDto();
        return restTemplate.postForEntity(API_PAYMENT_CARDS, cardDto, PaymentCardDto.class).getBody();
    }
}