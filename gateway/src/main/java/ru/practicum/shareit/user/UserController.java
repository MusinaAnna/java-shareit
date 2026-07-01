package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Headers;
import ru.practicum.shareit.client.BaseClient;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final BaseClient client;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        return client.post("/users", userDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        return client.get("/users");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        return client.get("/users/" + id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody UserDto userDto) {
        return client.patch("/users/" + id, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        return client.delete("/users/" + id);
    }
}
