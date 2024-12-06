package ru.practicum.ewm.main.comment;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.main.comment.dto.*;

import java.util.List;

public interface CommentService {
    CommentTextDto create(CommentCreateDto commentCreateDto);

    void delete(Long userId, Long commentId);

    CommentTextDto update(CommentUpdateDto commentUpdateDto);

    List<CommentTextDto> getByUserId(Long userId);

    void deleteByAdmin(Long commentId);

    CommentFullDto getById(Long commentId);

    List<CommentShortDto> getByEventId(Long eventId, Pageable pageable);
}
