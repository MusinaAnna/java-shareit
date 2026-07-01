package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User requester;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setName("Test User");
        requester.setEmail("test@user.com");
        requester = userRepository.save(requester);
    }

    @Test
    void createRequest_shouldSaveAndReturnDto() {
        ItemRequestDto result = requestService.createRequest(requester.getId(), "Нужна книга");

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Нужна книга", result.getDescription());
        assertNotNull(result.getCreated());

        var saved = requestRepository.findById(result.getId());
        assertTrue(saved.isPresent());
        assertEquals("Нужна книга", saved.get().getDescription());
    }

    @Test
    void createRequest_shouldThrowNotFoundException_whenUserNotFound() {
        assertThrows(NotFoundException.class, () -> requestService.createRequest(999L, "Нужна книга"));
    }

    @Test
    void getUserRequests_shouldReturnList() {
        requestService.createRequest(requester.getId(), "Запрос 1");
        requestService.createRequest(requester.getId(), "Запрос 2");

        var result = requestService.getUserRequests(requester.getId());

        assertEquals(2, result.size());
        assertEquals("Запрос 2", result.get(0).getDescription());
        assertEquals("Запрос 1", result.get(1).getDescription());
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        User otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("other@user.com");
        otherUser = userRepository.save(otherUser);
        requestService.createRequest(otherUser.getId(), "Запрос другого");

        requestService.createRequest(requester.getId(), "Мой запрос");

        var result = requestService.getAllRequests(requester.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals("Запрос другого", result.get(0).getDescription());
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        var dto = requestService.createRequest(requester.getId(), "Нужна книга");

        var found = requestService.getRequestById(requester.getId(), dto.getId());

        assertNotNull(found);
        assertEquals(dto.getId(), found.getId());
        assertEquals("Нужна книга", found.getDescription());
        assertNotNull(found.getItems());
        assertTrue(found.getItems().isEmpty());
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenRequestNotFound() {
        assertThrows(NotFoundException.class, () -> requestService.getRequestById(requester.getId(), 999L));
    }
}
