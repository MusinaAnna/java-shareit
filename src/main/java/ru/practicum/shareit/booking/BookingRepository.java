package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Sort sort);
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Sort sort);
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now, Sort sort);
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now, Sort sort);
    List<Booking> findByBookerIdAndStatusInAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, List<BookingStatus> statuses, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Sort sort);
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Sort sort);
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now, Sort sort);
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now, Sort sort);
    List<Booking> findByItemOwnerIdAndStatusInAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, List<BookingStatus> statuses, LocalDateTime start, LocalDateTime end, Sort sort);

    boolean existsByItemIdAndBookerIdAndEndBeforeAndStatus(Long itemId, Long bookerId, LocalDateTime now, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.end < :now AND b.status = 'APPROVED' ORDER BY b.end DESC")
    List<Booking> findPastBookingsByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start > :now AND b.status = 'APPROVED' ORDER BY b.start ASC")
    List<Booking> findFutureBookingsByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now, Sort sort);
}
