package ru.practicum.ewm.main.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByUserId(Long userId);

    @Query(value = "SELECT c.* FROM comments AS c LEFT JOIN users AS u ON c.user_id = u.id" +
            " LEFT JOIN events AS e ON c.event_id = e.id " +
            " WHERE c.event_id = :eventId" +
            "      OFFSET :from LIMIT :size", nativeQuery = true)
    List<Comment> findByEventIdNative(Long eventId, Integer from, Integer size);

    List<Comment> findByEventId(Long eventId, Pageable pageable);
}
