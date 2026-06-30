package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Headers;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(Headers.USER_ID) Long userId,
                             @RequestBody BookingCreateDto createDto) {
        log.info("POST /bookings от пользователя id={}", userId);
        return bookingService.createBooking(userId, createDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(Headers.USER_ID) Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam boolean approved) {
        log.info("PATCH /bookings/{}?approved={} от пользователя id={}", bookingId, approved, userId);
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(Headers.USER_ID) Long userId,
                              @PathVariable Long bookingId) {
        log.info("GET /bookings/{} от пользователя id={}", bookingId, userId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader(Headers.USER_ID) Long userId,
                                            @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings?state={} для пользователя id={}", state, userId);
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader(Headers.USER_ID) Long userId,
                                             @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings/owner?state={} для владельца id={}", state, userId);
        return bookingService.getOwnerBookings(userId, state);
    }
}
