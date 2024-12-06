package ru.practicum.ewm.main.comment.dto;

import lombok.Data;

@Data
public class CommentUpdateDto extends CommentCreateDto {
    private Long id;
}
