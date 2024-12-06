package ru.practicum.ewm.main.comment;

import ru.practicum.ewm.main.comment.dto.*;
import ru.practicum.ewm.main.event.EventMapper;
import ru.practicum.ewm.main.user.UserMapper;

public class CommentMapper {
    public static CommentShortDto toShortDto(Comment comment) {
        CommentShortDto commentShortDto = new CommentShortDto();
        commentShortDto.setId(comment.getId());
        commentShortDto.setText(comment.getText());
        commentShortDto.setUser(UserMapper.toDto(comment.getUser()));
        commentShortDto.setEdited(comment.getEdited());
        commentShortDto.setDate(comment.getDate());
        return commentShortDto;
    }

    public static CommentTextDto toTextDto(Comment comment) {
        CommentTextDto commentTextDto = new CommentTextDto();
        commentTextDto.setId(comment.getId());
        commentTextDto.setText(comment.getText());
        commentTextDto.setEvent(EventMapper.toDto(comment.getEvent()));
        commentTextDto.setEdited(comment.getEdited());
        commentTextDto.setDate(comment.getDate());
        return commentTextDto;
    }

    public static Comment toEntity(CommentUpdateDto commentUpdateDto) {
        Comment comment = new Comment();
        comment.setEdited(true);
        comment.setText(commentUpdateDto.getText());
        return comment;
    }

    public static Comment toEntity(CommentCreateDto commentCreateDtoDto) {
        Comment comment = new Comment();
        comment.setEdited(false);
        comment.setText(commentCreateDtoDto.getText());
        return comment;
    }

    public static CommentFullDto toFullDto(Comment comment) {
        CommentFullDto commentFullDto = new CommentFullDto();
        commentFullDto.setId(comment.getId());
        commentFullDto.setText(comment.getText());
        commentFullDto.setEvent(EventMapper.toDto(comment.getEvent()));
        commentFullDto.setUser(UserMapper.toDto(comment.getUser()));
        commentFullDto.setEdited(comment.getEdited());
        commentFullDto.setDate(comment.getDate());
        return commentFullDto;
    }

}
