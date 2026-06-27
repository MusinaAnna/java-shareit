package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("GET /items для владельца id={}", ownerId);
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("GET /items/{}", itemId);
        return itemService.getItemById(itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @RequestBody ItemDto itemDto) {
        log.info("POST /items от владельца id={}", ownerId);
        return itemService.createItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @PathVariable Long itemId,
                              @RequestBody(required = false) ItemDto itemDto) {
        log.info("PATCH /items/{} от владельца id={}", itemId, ownerId);
        if (itemDto == null) {
            throw new ru.practicum.shareit.exception.ValidationException("Тело запроса не может быть пустым");
        }
        return itemService.updateItem(itemId, ownerId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("GET /items/search?text={}", text);
        return itemService.searchAvailable(text);
    }
}
