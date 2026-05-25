package com.innowise.userservice.controller;

import com.innowise.userservice.dto.UserDto;
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
class UserControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldReturn201() {
        UserDto userDto = new UserDto();
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail("john@example.com");
        userDto.setActive(true);

        ResponseEntity<UserDto> response = restTemplate.postForEntity("/api/users", userDto, UserDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("john@example.com", response.getBody().getEmail());
    }

    @Test
    void createUser_DuplicateEmail_ShouldReturn409() {
        UserDto userDto = new UserDto();
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail("john@example.com");
        userDto.setActive(true);

        restTemplate.postForEntity("/api/users", userDto, UserDto.class);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/users", userDto, Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void createUser_InvalidData_ShouldReturn400() {
        UserDto userDto = new UserDto();
        userDto.setName("");
        userDto.setSurname("");
        userDto.setEmail("invalid-email");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/users", userDto, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getUserById_ShouldReturn200() {
        UserDto created = createTestUser();

        ResponseEntity<UserDto> response = restTemplate.getForEntity("/api/users/" + created.getId(), UserDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getName());
    }

    @Test
    void getUserById_NotFound_ShouldReturn404() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/users/999", Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllUsers_ShouldReturnPage() {
        createTestUser();
        createTestUser2();

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/users?page=0&size=10", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("content"));
    }

    @Test
    void getAllUsers_WithFilters_ShouldReturnFilteredResults() {
        createTestUser();
        createTestUser2();

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/users?name=John&page=0&size=10", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("content"));
    }

    @Test
    void updateUser_ShouldReturn200() {
        UserDto created = createTestUser();
        created.setName("Johnny");

        ResponseEntity<UserDto> response = restTemplate.exchange(
                "/api/users/" + created.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(created),
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Johnny", response.getBody().getName());
    }

    @Test
    void deleteUser_ShouldReturn204() {
        UserDto created = createTestUser();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/" + created.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<Map> getResponse = restTemplate.getForEntity("/api/users/" + created.getId(), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void setActiveStatus_ShouldReturn204() {
        UserDto created = createTestUser();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/" + created.getId() + "/active?active=false",
                HttpMethod.PATCH,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<UserDto> getResponse = restTemplate.getForEntity("/api/users/" + created.getId(), UserDto.class);
        assertNotNull(getResponse.getBody());
        assertFalse(getResponse.getBody().isActive());
    }

    private UserDto createTestUser() {
        UserDto userDto = new UserDto();
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail("john@example.com");
        userDto.setActive(true);
        return restTemplate.postForEntity("/api/users", userDto, UserDto.class).getBody();
    }

    private UserDto createTestUser2() {
        UserDto userDto = new UserDto();
        userDto.setName("Jane");
        userDto.setSurname("Smith");
        userDto.setBirthDate(LocalDate.of(1995, 5, 15));
        userDto.setEmail("jane@example.com");
        userDto.setActive(true);
        return restTemplate.postForEntity("/api/users", userDto, UserDto.class).getBody();
    }
}