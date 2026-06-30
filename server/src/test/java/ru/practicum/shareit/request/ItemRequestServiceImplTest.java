package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl service;

    private User requester;
    private ItemRequest request;
    private Item item;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setName("Test User");
        requester.setEmail("test@user.com");

        request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Нужна книга");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        item = new Item();
        item.setId(1L);
        item.setName("Книга");
        item.setDescription("Учебник по Java");
        item.setAvailable(true);
        item.setOwnerId(2L);
        item.setRequestId(1L);
    }

    @Test
    void createRequest_shouldCreateAndReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto result = service.createRequest(1L, "Нужна книга");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Нужна книга", result.getDescription());
        assertNotNull(result.getCreated());
        verify(userRepository).findById(1L);
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createRequest(1L, "Нужна книга"));
        verify(userRepository).findById(1L);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void getUserRequests_shouldReturnListOfRequestsWithItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findByRequesterIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        List<ItemRequestDto> result = service.getUserRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Нужна книга", result.get(0).getDescription());
        assertNotNull(result.get(0).getItems());
        assertEquals(1, result.get(0).getItems().size());
        verify(userRepository).findById(1L);
        verify(requestRepository).findByRequesterIdOrderByCreatedDesc(1L);
        verify(itemRepository).findByRequestId(1L);
    }

    @Test
    void getUserRequests_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getUserRequests(1L));
        verify(userRepository).findById(1L);
        verify(requestRepository, never()).findByRequesterIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequests_shouldReturnRequestsOfOtherUsers() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findByRequesterIdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        List<ItemRequestDto> result = service.getAllRequests(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(1L);
        verify(requestRepository).findByRequesterIdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class));
        verify(itemRepository).findByRequestId(1L);
    }

    @Test
    void getAllRequests_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getAllRequests(1L, 0, 10));
        verify(userRepository).findById(1L);
        verify(requestRepository, never()).findByRequesterIdNotOrderByCreatedDesc(anyLong(), any());
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        ItemRequestDto result = service.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Нужна книга", result.getDescription());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        verify(userRepository).findById(1L);
        verify(requestRepository).findById(1L);
        verify(itemRepository).findByRequestId(1L);
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getRequestById(1L, 1L));
        verify(userRepository).findById(1L);
        verify(requestRepository, never()).findById(anyLong());
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getRequestById(1L, 1L));
        verify(userRepository).findById(1L);
        verify(requestRepository).findById(1L);
    }
}
