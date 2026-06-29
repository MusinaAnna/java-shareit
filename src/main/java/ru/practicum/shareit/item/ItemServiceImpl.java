package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        List<ItemDto> dtos = items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());

        List<Long> itemIds = dtos.stream()
                .map(ItemDto::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // Загружаем все бронирования одним запросом
        Map<Long, List<Booking>> bookingsByItem;
        if (!itemIds.isEmpty()) {
            bookingsByItem = bookingRepository.findApprovedByItemIds(itemIds)
                    .stream()
                    .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        } else {
            bookingsByItem = Collections.emptyMap();
        }

        // Загружаем все комментарии одним запросом
        Map<Long, List<Comment>> commentsByItem;
        if (!itemIds.isEmpty()) {
            commentsByItem = commentRepository.findByItemIdIn(itemIds)
                    .stream()
                    .collect(Collectors.groupingBy(c -> c.getItem().getId()));
        } else {
            commentsByItem = Collections.emptyMap();
        }

        for (ItemDto dto : dtos) {
            List<Booking> bookings = bookingsByItem.getOrDefault(dto.getId(), Collections.emptyList());
            List<Comment> comments = commentsByItem.getOrDefault(dto.getId(), Collections.emptyList());
            enrichItemDto(dto, ownerId, bookings, comments);
        }

        return dtos;
    }

    @Override
    public ItemDto getItemById(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + id + " не найдена"));
        ItemDto dto = ItemMapper.toDto(item);

        List<Booking> bookings;
        List<Comment> comments;
        if (dto.getId() != null) {
            bookings = bookingRepository.findApprovedByItemIds(List.of(dto.getId()));
            comments = commentRepository.findByItemIdIn(List.of(dto.getId()));
        } else {
            bookings = Collections.emptyList();
            comments = Collections.emptyList();
        }
        enrichItemDto(dto, userId, bookings, comments);

        return dto;
    }

    @Override
    @Transactional
    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(ownerId);
        Item saved = itemRepository.save(item);
        log.info("Создана вещь id={} для пользователя id={}", saved.getId(), ownerId);
        return ItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Long ownerId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!item.getOwnerId().equals(ownerId)) {
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
        return ItemMapper.toDto(updated);
    }

    @Override
    public List<ItemDto> searchAvailable(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchByText(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId, userId, LocalDateTime.now(), BookingStatus.APPROVED);
        if (!hasBooked) {
            throw new ValidationException("Пользователь не может оставить отзыв на эту вещь, т.к. не арендовал её");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        CommentDto response = new CommentDto();
        response.setId(saved.getId());
        response.setText(saved.getText());
        response.setAuthorName(author.getName());
        response.setCreated(saved.getCreated());

        log.info("Добавлен комментарий id={} к вещи id={}", saved.getId(), itemId);
        return response;
    }

    private void enrichItemDto(ItemDto dto, Long userId, List<Booking> bookings, List<Comment> comments) {
        if (dto.getId() == null) {
            log.warn("ItemDto id is null, skipping enrichment");
            return;
        }

        List<CommentDto> commentDtos = comments.stream()
                .map(c -> {
                    CommentDto cd = new CommentDto();
                    cd.setId(c.getId());
                    cd.setText(c.getText());
                    cd.setAuthorName(c.getAuthor().getName());
                    cd.setCreated(c.getCreated());
                    return cd;
                })
                .collect(Collectors.toList());
        dto.setComments(commentDtos);

        if (dto.getOwnerId() != null && dto.getOwnerId().equals(userId) && bookings != null) {
            try {
                LocalDateTime now = LocalDateTime.now();

                dto.setLastBooking(
                        bookings.stream()
                                .filter(b -> b.getEnd().isBefore(now))
                                .max((b1, b2) -> b1.getEnd().compareTo(b2.getEnd()))
                                .map(bookingMapper::toDto)
                                .orElse(null)
                );

                dto.setNextBooking(
                        bookings.stream()
                                .filter(b -> b.getStart().isAfter(now))
                                .min((b1, b2) -> b1.getStart().compareTo(b2.getStart()))
                                .map(bookingMapper::toDto)
                                .orElse(null)
                );
            } catch (Exception e) {
                log.error("Error processing bookings for item id {}: {}", dto.getId(), e.getMessage());
            }
        }
    }
}
