package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setName("Item Owner");
        owner.setEmail("owner@test.com");
        owner = userRepository.save(owner);
        ownerId = owner.getId();
    }

    @Test
    void createItem_shouldSaveAndReturn() {
        ItemDto dto = new ItemDto();
        dto.setName("Test Item");
        dto.setDescription("Test Description");
        dto.setAvailable(true);

        ItemDto created = itemService.createItem(ownerId, dto);
        assertNotNull(created.getId());
        assertEquals("Test Item", created.getName());
    }

    @Test
    void getItemsByOwner_shouldReturnList() {
        ItemDto dto = new ItemDto();
        dto.setName("Item for Owner");
        dto.setDescription("Desc");
        dto.setAvailable(true);
        itemService.createItem(ownerId, dto);

        var items = itemService.getItemsByOwner(ownerId);
        assertEquals(1, items.size());
        assertEquals("Item for Owner", items.get(0).getName());
    }

    @Test
    void getItemById_shouldReturnItem() {
        ItemDto dto = new ItemDto();
        dto.setName("Get By Id");
        dto.setDescription("Desc");
        dto.setAvailable(true);
        ItemDto created = itemService.createItem(ownerId, dto);

        ItemDto found = itemService.getItemById(created.getId(), ownerId);
        assertEquals("Get By Id", found.getName());
    }

    @Test
    void updateItem_shouldUpdateFields() {
        ItemDto dto = new ItemDto();
        dto.setName("Old Name");
        dto.setDescription("Old Desc");
        dto.setAvailable(true);
        ItemDto created = itemService.createItem(ownerId, dto);

        ItemDto update = new ItemDto();
        update.setName("New Name");
        update.setDescription("New Desc");
        update.setAvailable(false);

        ItemDto updated = itemService.updateItem(created.getId(), ownerId, update);
        assertEquals("New Name", updated.getName());
        assertFalse(updated.getAvailable());
    }

    @Test
    void addComment_shouldThrowIfNoBooking() {
        ItemDto dto = new ItemDto();
        dto.setName("Item for Comment");
        dto.setDescription("Desc");
        dto.setAvailable(true);
        final ItemDto created = itemService.createItem(ownerId, dto);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.com");
        final User savedBooker = userRepository.save(booker);

        final CommentDto comment = new CommentDto();
        comment.setText("Great item!");

        assertThrows(RuntimeException.class,
                () -> itemService.addComment(savedBooker.getId(), created.getId(), comment));
    }
}
