package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("Integration Test User");
        userDto.setEmail("integration@test.com");
    }

    @Test
    void createUser_shouldSaveAndReturn() {
        UserDto created = userService.createUser(userDto);
        assertNotNull(created.getId());
        assertEquals("integration@test.com", created.getEmail());

        var saved = userRepository.findById(created.getId());
        assertTrue(saved.isPresent());
        assertEquals("Integration Test User", saved.get().getName());
    }

    @Test
    void getUserById_shouldReturnUser() {
        UserDto created = userService.createUser(userDto);
        UserDto found = userService.getUserById(created.getId());
        assertEquals(created.getEmail(), found.getEmail());
    }

    @Test
    void getUserById_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void updateUser_shouldUpdateFields() {
        UserDto created = userService.createUser(userDto);
        UserDto update = new UserDto();
        update.setName("Updated Name");
        update.setEmail("updated@test.com");

        UserDto updated = userService.updateUser(created.getId(), update);
        assertEquals("Updated Name", updated.getName());
        assertEquals("updated@test.com", updated.getEmail());

        var saved = userRepository.findById(created.getId());
        assertEquals("updated@test.com", saved.get().getEmail());
    }

    @Test
    void deleteUser_shouldDelete() {
        UserDto created = userService.createUser(userDto);
        userService.deleteUser(created.getId());
        assertTrue(userRepository.findById(created.getId()).isEmpty());
    }
}
