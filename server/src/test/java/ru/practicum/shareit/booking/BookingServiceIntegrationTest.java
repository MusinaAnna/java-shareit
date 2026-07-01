package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.com");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Booking Item");
        item.setDescription("Desc");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void createBooking_shouldSaveAndReturn() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(item.getId());
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.createBooking(booker.getId(), createDto);
        assertNotNull(created.getId());
        assertEquals(BookingStatus.WAITING, created.getStatus());
    }

    @Test
    void createBooking_shouldThrowIfItemNotAvailable() {
        item.setAvailable(false);
        itemRepository.save(item);

        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(item.getId());
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(booker.getId(), createDto));
    }

    @Test
    void approveBooking_shouldChangeStatus() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(item.getId());
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.createBooking(booker.getId(), createDto);

        BookingDto approved = bookingService.approveBooking(owner.getId(), created.getId(), true);
        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void getBookingById_shouldReturn() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(item.getId());
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.createBooking(booker.getId(), createDto);
        BookingDto found = bookingService.getBookingById(booker.getId(), created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getUserBookings_shouldReturnList() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(item.getId());
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingService.createBooking(booker.getId(), createDto);

        List<BookingDto> bookings = bookingService.getUserBookings(booker.getId(), BookingState.ALL);
        assertEquals(1, bookings.size());
    }
}
