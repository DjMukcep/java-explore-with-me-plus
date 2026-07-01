package ru.practicum.entity.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.event.EventService;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;


    @Override
    public Comment getById(Long id) {
        return commentRepository.findByIdWithAuthor(id).orElseThrow(
                () -> new NotFoundException("Comment not found with id: " + id)
        );
    }
}
