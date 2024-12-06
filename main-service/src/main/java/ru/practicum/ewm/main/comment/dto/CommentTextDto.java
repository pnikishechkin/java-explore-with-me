package ru.practicum.ewm.main.comment.dto;

import lombok.Data;
import ru.practicum.ewm.main.event.dto.EventShortDto;

import java.time.LocalDateTime;

@Data
public class CommentTextDto {
    private Long id;
    private EventShortDto event;
    private String text;
    private LocalDateTime date;
    private Boolean edited;
}
