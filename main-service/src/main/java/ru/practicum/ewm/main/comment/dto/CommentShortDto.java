package ru.practicum.ewm.main.comment.dto;

import lombok.Data;
import ru.practicum.ewm.main.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
public class CommentShortDto {
    private Long id;
    private UserShortDto user;
    private String text;
    private LocalDateTime date;
    private Boolean edited;
}
