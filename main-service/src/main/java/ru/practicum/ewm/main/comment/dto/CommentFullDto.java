package ru.practicum.ewm.main.comment.dto;

import lombok.Data;
import ru.practicum.ewm.main.event.dto.EventShortDto;
import ru.practicum.ewm.main.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
public class CommentFullDto {
    private Long id;
    private UserShortDto user;
    private EventShortDto event;
    private String text;
    private LocalDateTime date;
    private Boolean edited;
}
