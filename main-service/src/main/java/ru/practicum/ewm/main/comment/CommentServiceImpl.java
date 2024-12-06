package ru.practicum.ewm.main.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.main.comment.dto.*;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.EventState;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentTextDto create(CommentCreateDto commentCreateDto) {
        Comment comment = CommentMapper.toEntity(commentCreateDto);
        comment.setDate(LocalDateTime.now());

        User user = userRepository.findById(commentCreateDto.getUserId()).orElseThrow(() -> new NotFoundException(
                "Ошибка! Пользователя с заданным идентификатором не существует!"));

        Event event = eventRepository.findById(commentCreateDto.getEventId()).orElseThrow(() -> new NotFoundException(
                "Ошибка! События с заданным идентификатором не существует!"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя добавить комментарий к неопубликованному событию");
        }

        comment.setUser(user);
        comment.setEvent(event);

        return CommentMapper.toTextDto(commentRepository.save(comment));
    }

    @Override
    public void delete(Long userId, Long commentId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Пользователя с заданным идентификатором не существует!"));

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Комментария с заданным идентификатором не существует!"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ConflictException("Удалить комментарий может только пользователь, опубликовавший его");
        }

        commentRepository.delete(comment);
    }

    @Override
    public CommentTextDto update(CommentUpdateDto commentUpdateDto) {

        User user = userRepository.findById(commentUpdateDto.getUserId()).orElseThrow(() -> new NotFoundException(
                "Ошибка! Пользователя с заданным идентификатором не существует!"));

        Comment comment = commentRepository.findById(commentUpdateDto.getId()).orElseThrow(() -> new NotFoundException(
                "Ошибка! Комментария с заданным идентификатором не существует!"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ConflictException("Отредактировать комментарий может только пользователь, опубликовавший его");
        }

        if (commentUpdateDto.getText() != null) {
            comment.setText(commentUpdateDto.getText());
        }
        comment.setEdited(true);

        return CommentMapper.toTextDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentTextDto> getByUserId(Long userId) {
        return commentRepository.findByUserId(userId).stream().map(CommentMapper::toTextDto).toList();
    }

    @Override
    public void deleteByAdmin(Long commentId) {
        commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Комментария с заданным идентификатором не существует!"));

        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentFullDto getById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Комментария с заданным идентификатором не существует!"));
        return CommentMapper.toFullDto(comment);
    }

    @Override
    public List<CommentShortDto> getByEventId(Long eventId, Pageable pageable) {
        return commentRepository.findByEventId(eventId, pageable).stream().map(CommentMapper::toShortDto).toList();
    }
}
