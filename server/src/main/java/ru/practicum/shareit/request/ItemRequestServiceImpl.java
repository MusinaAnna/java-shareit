package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, String description) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = requestRepository.save(request);
        log.info("Создан запрос id={} от пользователя id={}", saved.getId(), userId);

        return mapToDto(saved);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        return requests.stream()
                .map(this::mapToDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> requests = requestRepository.findByRequesterIdNotOrderByCreatedDesc(userId, page);
        return requests.stream()
                .map(this::mapToDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return mapToDtoWithItems(request);
    }

    private ItemRequestDto mapToDto(ItemRequest request) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(null);
        return dto;
    }

    private ItemRequestDto mapToDtoWithItems(ItemRequest request) {
        ItemRequestDto dto = mapToDto(request);
        List<Item> items = itemRepository.findByRequestId(request.getId());
        dto.setItems(items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }
}
