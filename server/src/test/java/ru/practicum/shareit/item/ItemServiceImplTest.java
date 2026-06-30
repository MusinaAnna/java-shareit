package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Desc");
        item.setAvailable(true);
        item.setOwnerId(1L);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Desc");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(1L);
    }

    @Test
    void getItemsByOwner_shouldReturnItems() {
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(item));
        when(bookingRepository.findApprovedByItemIds(anyList())).thenReturn(List.of());
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(List.of());

        List<ItemDto> result = itemService.getItemsByOwner(1L);
        assertEquals(1, result.size());
        assertEquals("Item", result.get(0).getName());
    }

    @Test
    void getItemById_shouldReturnItemWithBookingsAndComments() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findApprovedByItemIds(anyList())).thenReturn(List.of());
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        ItemDto result = itemService.getItemById(1L, 1L);
        assertEquals("Item", result.getName());
    }

    @Test
    void createItem_shouldSaveAndReturn() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(1L, itemDto);
        assertEquals("Item", result.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_shouldThrowNotFoundIfUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.createItem(1L, itemDto));
    }

    @Test
    void updateItem_shouldUpdateFields() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto update = new ItemDto();
        update.setName("NewName");
        update.setDescription("NewDesc");
        update.setAvailable(false);

        ItemDto result = itemService.updateItem(1L, 1L, update);
        assertEquals("NewName", result.getName());
        assertEquals("NewDesc", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItem_shouldThrowForbiddenIfNotOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ForbiddenException.class, () -> itemService.updateItem(1L, 2L, itemDto));
    }

    @Test
    void searchAvailable_shouldReturnItems() {
        when(itemRepository.searchByText("text")).thenReturn(List.of(item));
        List<ItemDto> result = itemService.searchAvailable("text");
        assertEquals(1, result.size());
    }

    @Test
    void addComment_shouldSaveComment() {
        User booker = new User();
        booker.setId(2L);
        booker.setName("Booker");

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(anyLong(), anyLong(), any(), any()))
                .thenReturn(true);
        Comment saved = new Comment();
        saved.setId(1L);
        saved.setText("Great!");
        saved.setAuthor(booker);
        saved.setItem(item);
        saved.setCreated(LocalDateTime.now());
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great!");
        CommentDto result = itemService.addComment(2L, 1L, commentDto);
        assertEquals("Great!", result.getText());
        assertEquals("Booker", result.getAuthorName());
    }

    @Test
    void addComment_shouldThrowIfUserNotBooked() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great!");
        assertThrows(RuntimeException.class, () -> itemService.addComment(2L, 1L, commentDto));
    }
}
