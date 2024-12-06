package ru.practicum.ewm.main.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.comment.CommentService;
import ru.practicum.ewm.main.comment.dto.CommentCreateDto;
import ru.practicum.ewm.main.comment.dto.CommentTextDto;
import ru.practicum.ewm.main.comment.dto.CommentUpdateDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentControllerPrivate {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentTextDto post(@PathVariable Long userId,
                               @RequestParam Long eventId,
                               @RequestBody @Validated CommentCreateDto commentCreateDto) {
        commentCreateDto.setUserId(userId);
        commentCreateDto.setEventId(eventId);
        return commentService.create(commentCreateDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void post(@PathVariable Long userId,
                     @PathVariable Long commentId) {
        commentService.delete(userId, commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentTextDto patch(@PathVariable Long userId,
                                @PathVariable Long commentId,
                                @RequestBody @Validated CommentUpdateDto commentUpdateDto) {
        commentUpdateDto.setId(commentId);
        commentUpdateDto.setUserId(userId);
        return commentService.update(commentUpdateDto);
    }

    @GetMapping
    public List<CommentTextDto> get(@PathVariable Long userId) {
        return commentService.getByUserId(userId);
    }

}
