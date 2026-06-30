package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        if (booking == null) return null;

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        Item item = booking.getItem();
        if (item != null) {
            ItemDto itemDto = new ItemDto();
            itemDto.setId(item.getId());
            itemDto.setName(item.getName());
            itemDto.setDescription(item.getDescription());
            itemDto.setAvailable(item.getAvailable());
            itemDto.setOwnerId(item.getOwnerId());
            itemDto.setRequestId(item.getRequestId());
            dto.setItem(itemDto);
        }

        User booker = booking.getBooker();
        if (booker != null) {
            UserDto userDto = new UserDto();
            userDto.setId(booker.getId());
            userDto.setName(booker.getName());
            userDto.setEmail(booker.getEmail());
            dto.setBooker(userDto);
        }

        return dto;
    }

    public Booking toEntity(BookingCreateDto createDto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(createDto.getStart());
        booking.setEnd(createDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}
