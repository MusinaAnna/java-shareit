package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final BaseClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemRequestDto requestDto) {
        return client.post("/requests", userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.get("/requests", userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> params = Map.of("from", from, "size", size);
        return client.get("/requests/all", userId, params);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.get("/requests/" + id, userId);
    }
}
