package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingCreateDto createDto;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setId(1L);
        booker.setName("Booker");

        owner = new User();
        owner.setId(2L);
        owner.setName("Owner");

        item = new Item();
        item.setId(10L);
        item.setName("Item");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.WAITING);

        createDto = new BookingCreateDto();
        createDto.setItemId(10L);
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void createBooking_shouldSaveAndReturn() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingMapper.toEntity(any(BookingCreateDto.class), any(Item.class), any(User.class))).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(new BookingDto());

        BookingDto result = bookingService.createBooking(1L, createDto);
        assertNotNull(result);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_shouldThrowIfItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThrows(ValidationException.class, () -> bookingService.createBooking(1L, createDto));
    }

    @Test
    void createBooking_shouldThrowIfOwnerBook() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThrows(ValidationException.class, () -> bookingService.createBooking(2L, createDto));
    }

    @Test
    void approveBooking_shouldChangeStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(new BookingDto());

        BookingDto result = bookingService.approveBooking(2L, 1L, true);
        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void approveBooking_shouldThrowIfNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(ForbiddenException.class, () -> bookingService.approveBooking(3L, 1L, true));
    }

    @Test
    void getBookingById_shouldReturn() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(new BookingDto());
        assertNotNull(bookingService.getBookingById(1L, 1L));
    }

    @Test
    void getUserBookings_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(new BookingDto());
        List<BookingDto> result = bookingService.getUserBookings(1L, BookingState.ALL);
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_shouldThrowIfUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(1L, BookingState.ALL));
    }

    @Test
    void getOwnerBookings_shouldReturnList() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(new BookingDto());
        List<BookingDto> result = bookingService.getOwnerBookings(2L, BookingState.ALL);
        assertEquals(1, result.size());
    }
}
