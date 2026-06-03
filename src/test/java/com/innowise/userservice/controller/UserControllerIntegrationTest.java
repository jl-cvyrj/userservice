package com.innowise.userservice.controller;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserControllerIntegrationTest {

    private static final String API_USERS = "/api/users";
    private static final String API_USERS_PATH = "/api/users/";

    private static final String ID_999 = "999";
    private static final String ACTIVE_FALSE_PARAM = "/active?active=false";
    private static final String PAGE_PARAMS = "?page=0&size=10";
    private static final String FILTER_PARAMS = "?name=John&page=0&size=10";

    private static final String DB_NAME = "testdb";
    private static final String DB_USER_PASS = "test";
    private static final String LIQUIBASE_CHANGELOG = "classpath:db/changelog/changelog-master.yml";

    private static final String USER_NAME_1 = "John";
    private static final String USER_SURNAME_1 = "Doe";
    private static final String USER_EMAIL_1 = "john@example.com";

    private static final String USER_NAME_2 = "Jane";
    private static final String USER_SURNAME_2 = "Smith";
    private static final String USER_EMAIL_2 = "jane@example.com";

    private static final String INVALID_EMAIL = "invalid-email";
    private static final String UPDATED_NAME = "Johnny";
    private static final String ERROR_KEY = "error";
    private static final String CONTENT_KEY = "content";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER_PASS)
            .withPassword(DB_USER_PASS);

    private final TestRestTemplate restTemplate;
    private final UserRepository userRepository;

    UserControllerIntegrationTest(TestRestTemplate restTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> LIQUIBASE_CHANGELOG);
    }

    private final ParameterizedTypeReference<Map<String, Object>> mapTypeRef = new ParameterizedTypeReference<>() {};

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUserShouldReturn201() {
        UserDto userDto = new UserDto();
        userDto.setName(USER_NAME_1);
        userDto.setSurname(USER_SURNAME_1);
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail(USER_EMAIL_1);
        userDto.setActive(true);

        ResponseEntity<UserDto> response = restTemplate.postForEntity(API_USERS, userDto, UserDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(USER_EMAIL_1, response.getBody().getEmail());
    }

    @Test
    void createUserDuplicateEmailShouldReturn409() {
        UserDto userDto = new UserDto();
        userDto.setName(USER_NAME_1);
        userDto.setSurname(USER_SURNAME_1);
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail(USER_EMAIL_1);
        userDto.setActive(true);

        restTemplate.postForEntity(API_USERS, userDto, UserDto.class);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_USERS,
                HttpMethod.POST,
                new HttpEntity<>(userDto),
                mapTypeRef
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey(ERROR_KEY));
    }

    @Test
    void createUserInvalidDataShouldReturn400() {
        UserDto userDto = new UserDto();
        userDto.setName("");
        userDto.setSurname("");
        userDto.setEmail(INVALID_EMAIL);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_USERS,
                HttpMethod.POST,
                new HttpEntity<>(userDto),
                mapTypeRef
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getUserByIdShouldReturn200() {
        UserDto created = createTestUser();

        ResponseEntity<UserDto> response = restTemplate.getForEntity(API_USERS_PATH + created.getId(), UserDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_NAME_1, response.getBody().getName());
    }

    @Test
    void getUserByIdNotFoundShouldReturn404() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_USERS_PATH + ID_999,
                HttpMethod.GET,
                null,
                mapTypeRef
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllUsersShouldReturnPage() {
        createTestUser();
        createTestUser2();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_USERS + PAGE_PARAMS,
                HttpMethod.GET,
                null,
                mapTypeRef
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get(CONTENT_KEY));
    }

    @Test
    void getAllUsersWithFiltersShouldReturnFilteredResults() {
        createTestUser();
        createTestUser2();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_USERS + FILTER_PARAMS,
                HttpMethod.GET,
                null,
                mapTypeRef
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get(CONTENT_KEY));
    }

    @Test
    void updateUserShouldReturn200() {
        UserDto created = createTestUser();
        created.setName(UPDATED_NAME);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                API_USERS_PATH + created.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(created),
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UPDATED_NAME, response.getBody().getName());
    }

    @Test
    void deleteUserShouldReturn204() {
        UserDto created = createTestUser();

        ResponseEntity<Void> response = restTemplate.exchange(
                API_USERS_PATH + created.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<Map<String, Object>> getResponse = restTemplate.exchange(
                API_USERS_PATH + created.getId(),
                HttpMethod.GET,
                null,
                mapTypeRef
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void setActiveStatusShouldReturn204() {
        UserDto created = createTestUser();

        ResponseEntity<Void> response = restTemplate.exchange(
                API_USERS_PATH + created.getId() + ACTIVE_FALSE_PARAM,
                HttpMethod.PATCH,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<UserDto> getResponse = restTemplate.getForEntity(API_USERS_PATH + created.getId(), UserDto.class);
        assertNotNull(getResponse.getBody());
        assertFalse(getResponse.getBody().isActive());
    }

    private UserDto createTestUser() {
        UserDto userDto = new UserDto();
        userDto.setName(USER_NAME_1);
        userDto.setSurname(USER_SURNAME_1);
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail(USER_EMAIL_1);
        userDto.setActive(true);
        return restTemplate.postForEntity(API_USERS, userDto, UserDto.class).getBody();
    }

    private UserDto createTestUser2() {
        UserDto userDto = new UserDto();
        userDto.setName(USER_NAME_2);
        userDto.setSurname(USER_SURNAME_2);
        userDto.setBirthDate(LocalDate.of(1995, 5, 15));
        userDto.setEmail(USER_EMAIL_2);
        userDto.setActive(true);
        return restTemplate.postForEntity(API_USERS, userDto, UserDto.class).getBody();
    }
}