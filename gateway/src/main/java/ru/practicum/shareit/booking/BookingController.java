package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Headers;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BaseClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(Headers.USER_ID) Long userId,
                                         @Valid @RequestBody BookingCreateDto bookingCreateDto) {
        return client.post("/bookings", userId, bookingCreateDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> approve(@PathVariable Long id,
                                          @RequestHeader(Headers.USER_ID) Long userId,
                                          @RequestParam boolean approved) {
        Map<String, Object> params = Map.of("approved", approved);
        return client.patch("/bookings/" + id, userId, params, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id,
                                          @RequestHeader(Headers.USER_ID) Long userId) {
        return client.get("/bookings/" + id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getByBooker(@RequestHeader(Headers.USER_ID) Long userId,
                                              @RequestParam(defaultValue = "ALL") String state) {
        Map<String, Object> params = Map.of("state", state);
        return client.get("/bookings", userId, params);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getByOwner(@RequestHeader(Headers.USER_ID) Long userId,
                                             @RequestParam(defaultValue = "ALL") String state) {
        Map<String, Object> params = Map.of("state", state);
        return client.get("/bookings/owner", userId, params);
    }
}
