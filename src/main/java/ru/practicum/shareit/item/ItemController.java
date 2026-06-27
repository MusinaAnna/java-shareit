package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Headers;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(Headers.USER_ID) Long ownerId) {
        log.info("GET /items для владельца id={}", ownerId);
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(Headers.USER_ID) Long userId,
                               @PathVariable Long itemId) {
        log.info("GET /items/{} от пользователя id={}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader(Headers.USER_ID) Long ownerId,
                              @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items от владельца id={}", ownerId);
        return itemService.createItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(Headers.USER_ID) Long ownerId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("PATCH /items/{} от владельца id={}", itemId, ownerId);
        return itemService.updateItem(itemId, ownerId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("GET /items/search?text={}", text);
        return itemService.searchAvailable(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestHeader(Headers.USER_ID) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        log.info("POST /items/{}/comment от пользователя id={}", itemId, userId);
        return itemService.addComment(userId, itemId, commentDto);
    }
}
