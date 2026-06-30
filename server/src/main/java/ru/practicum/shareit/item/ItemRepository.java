package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i " +
            "WHERE (UPPER(i.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "   OR UPPER(i.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true")
    List<Item> searchByText(@Param("text") String text);

    List<Item> findByRequestId(Long requestId);
}
