package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Headers;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final BaseClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(Headers.USER_ID) Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        return client.post("/items", userId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwner(@RequestHeader(Headers.USER_ID) Long userId) {
        return client.get("/items", userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id,
                                          @RequestHeader(Headers.USER_ID) Long userId) {
        return client.get("/items/" + id, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id,
                                         @RequestHeader(Headers.USER_ID) Long userId,
                                         @RequestBody ItemDto itemDto) {
        return client.patch("/items/" + id, userId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        Map<String, Object> params = Map.of("text", text);
        return client.get("/items/search", null, params);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long id,
                                             @RequestHeader(Headers.USER_ID) Long userId,
                                             @Valid @RequestBody CommentDto commentDto) {
        return client.post("/items/" + id + "/comment", userId, commentDto);
    }
}
