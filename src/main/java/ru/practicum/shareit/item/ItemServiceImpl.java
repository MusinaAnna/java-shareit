package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        return itemRepository.findByOwner(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + id + " не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        if (ownerId == null) {
            throw new ValidationException("Заголовок X-Sharer-User-Id обязателен");
        }
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с id=" + ownerId + " не существует");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности (available) обязателен");
        }
        Item item = ItemMapper.toItem(itemDto, ownerId);
        item = itemRepository.save(item);
        log.info("Создана вещь id={} для пользователя id={}", item.getId(), ownerId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, Long ownerId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
        if (!item.getOwner().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        Item updated = itemRepository.save(item);
        log.info("Обновлена вещь id={}", updated.getId());
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public List<ItemDto> searchAvailable(String text) {
        return itemRepository.searchAvailable(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
