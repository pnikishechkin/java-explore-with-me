package ru.practicum.ewm.main.comment;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "text")
    private String text;

    @Column(name = "edited")
    private Boolean edited;
}
