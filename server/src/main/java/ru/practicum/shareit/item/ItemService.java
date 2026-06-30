package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    List<ItemDto> getItemsByOwner(Long ownerId);

    ItemDto getItemById(Long id, Long userId);

    ItemDto createItem(Long ownerId, ItemDto itemDto);

    ItemDto updateItem(Long itemId, Long ownerId, ItemDto itemDto);

    List<ItemDto> searchAvailable(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
