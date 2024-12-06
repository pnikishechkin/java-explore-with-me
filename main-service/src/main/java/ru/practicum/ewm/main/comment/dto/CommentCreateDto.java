package ru.practicum.ewm.main.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
    private Long userId;
    private Long eventId;
}
