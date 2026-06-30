package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Headers;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader(Headers.USER_ID) Long userId,
                                        @RequestBody ItemRequestDto requestDto) {
        log.info("POST /requests от пользователя id={}", userId);
        return requestService.createRequest(userId, requestDto.getDescription());
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(Headers.USER_ID) Long userId) {
        log.info("GET /requests для пользователя id={}", userId);
        return requestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(Headers.USER_ID) Long userId,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        log.info("GET /requests/all от пользователя id={}, from={}, size={}", userId, from, size);
        return requestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{id}")
    public ItemRequestDto getRequestById(@RequestHeader(Headers.USER_ID) Long userId,
                                         @PathVariable Long id) {
        log.info("GET /requests/{} от пользователя id={}", id, userId);
        return requestService.getRequestById(userId, id);
    }
}
