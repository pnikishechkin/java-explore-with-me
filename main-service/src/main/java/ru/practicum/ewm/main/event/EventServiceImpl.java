package ru.practicum.ewm.main.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.category.CategoryRepository;
import ru.practicum.ewm.main.event.dto.*;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.request.*;
import ru.practicum.ewm.main.request.dto.RequestDto;
import ru.practicum.ewm.main.request.dto.RequestsStatusUpdateDto;
import ru.practicum.ewm.main.request.dto.RequestsStatusUpdateResultDto;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final EventStatsHandler eventStatsHandler;

    @Override
    public EventFullDto create(EventCreateDto eventCreateDto) {

        User user = userRepository.findById(eventCreateDto.getInitiatorId()).orElseThrow(() -> new NotFoundException(
                "Ошибка! Пользователя с заданным идентификатором не существует"));

        Category category = categoryRepository.findById(eventCreateDto.getCategory()).orElseThrow(() -> new NotFoundException(
                "Ошибка! Категории с заданным идентификатором не существует"));

        Event event = EventMapper.toEntity(eventCreateDto);
        event.setState(EventState.PENDING);
        event.setCategory(category);
        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());

        Event newEvent = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toDto(newEvent);

        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Пользователя с заданным идентификатором не существует"));

        List<Event> res = eventRepository.findByInitiatorIdFromSize(userId, from, size);

        List<EventCountConfirmedRequests> eventRequestsCount =
                requestRepository.getCountRequests(res.stream().map(Event::getId).toList());

        List<EventShortDto> eventShortDtos = res.stream().map(EventMapper::toShortDto).toList();

        eventShortDtos.stream().forEach(e ->
                e.setConfirmedRequests(
                        eventRequestsCount.stream()
                                .filter(er -> er.getEventId().equals(e.getId()))
                                .findFirst().get().getCount()
                )
        );

        return eventShortDtos;
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Пользователя с заданным идентификатором не существует"));

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Ошибка! События с заданным идентификатором не существует"));

        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Ошибка! Событие принадлежит другому пользователю!");
        }

        EventFullDto eventFullDto = EventMapper.toDto(event);
        eventSetCountRequestsAndViews(eventFullDto);

        return eventFullDto;
    }

    @Override
    public EventFullDto update(Long userId, EventUserUpdateDto eventUserUpdateDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Пользователя с заданным идентификатором не существует"));

        Event event = eventRepository.findById(eventUserUpdateDto.getId()).orElseThrow(() -> new NotFoundException(
                "Ошибка! События с заданным идентификатором не существует"));

        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Ошибка! Событие принадлежит другому пользователю!");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя отредактировать опубликованное событие");
        }

        if (eventUserUpdateDto.getAnnotation() != null) {
            event.setAnnotation(eventUserUpdateDto.getAnnotation());
        }
        if (eventUserUpdateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(eventUserUpdateDto.getCategoryId()).orElseThrow(() -> new NotFoundException(
                    "Ошибка! Категории с заданным идентификатором не существует"));
            event.setCategory(category);
        }
        if (eventUserUpdateDto.getDescription() != null) {
            event.setDescription(eventUserUpdateDto.getDescription());
        }
        if (eventUserUpdateDto.getEventDate() != null) {
            event.setEventDate(eventUserUpdateDto.getEventDate());
        }
        if (eventUserUpdateDto.getLocation() != null) {
            event.setLocation(eventUserUpdateDto.getLocation());
        }
        if (eventUserUpdateDto.getPaid() != null) {
            event.setPaid(eventUserUpdateDto.getPaid());
        }
        if (eventUserUpdateDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventUserUpdateDto.getParticipantLimit());
        }
        if (eventUserUpdateDto.getRequestModeration() != null) {
            event.setRequestModeration(eventUserUpdateDto.getRequestModeration());
        }
        if (eventUserUpdateDto.getTitle() != null) {
            event.setTitle(eventUserUpdateDto.getTitle());
        }
        if (eventUserUpdateDto.getStateAction() == EventStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        } else if (eventUserUpdateDto.getStateAction() == EventStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }

        Event updEvent = eventRepository.save(event);
        return EventMapper.toDto(updEvent);
    }

    @Override
    public EventFullDto updateByAdmin(EventAdminUpdateDto eventAdminUpdateDto) {

        Event event = eventRepository.findById(eventAdminUpdateDto.getId()).orElseThrow(() -> new NotFoundException(
                "Ошибка! События с заданным идентификатором не существует"));

        if (eventAdminUpdateDto.getAnnotation() != null) {
            event.setAnnotation(eventAdminUpdateDto.getAnnotation());
        }
        if (eventAdminUpdateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(eventAdminUpdateDto.getCategoryId()).orElseThrow(() -> new NotFoundException(
                    "Ошибка! Категории с заданным идентификатором не существует"));
            event.setCategory(category);
        }
        if (eventAdminUpdateDto.getDescription() != null) {
            event.setDescription(eventAdminUpdateDto.getDescription());
        }
        if (eventAdminUpdateDto.getEventDate() != null) {
            event.setEventDate(eventAdminUpdateDto.getEventDate());
        }
        if (eventAdminUpdateDto.getLocation() != null) {
            event.setLocation(eventAdminUpdateDto.getLocation());
        }
        if (eventAdminUpdateDto.getPaid() != null) {
            event.setPaid(eventAdminUpdateDto.getPaid());
        }
        if (eventAdminUpdateDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventAdminUpdateDto.getParticipantLimit());
        }
        if (eventAdminUpdateDto.getRequestModeration() != null) {
            event.setRequestModeration(eventAdminUpdateDto.getRequestModeration());
        }
        if (eventAdminUpdateDto.getTitle() != null) {
            event.setTitle(eventAdminUpdateDto.getTitle());
        }
        if (eventAdminUpdateDto.getStateAction() == EventAdminStateAction.PUBLISH_EVENT) {
            if (event.getState() == EventState.CANCELED || event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Отмененное событие нельзя опубликовать");
            }
            event.setState(EventState.PUBLISHED);
        } else if (eventAdminUpdateDto.getStateAction() == EventAdminStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Опубликованное событие нельзя отменить");
            }
            event.setState(EventState.CANCELED);
        }

        Event updEvent = eventRepository.save(event);
        log.info("updEvent.getState() = " + updEvent.getState());
        return EventMapper.toDto(updEvent);
    }

    @Override
    public List<EventShortDto> getPublicEventsByParams(EventParams eventParams, HttpServletRequest request) {

        List<Event> events = new ArrayList<>();
        List<EventShortDto> eventShortDtos = new ArrayList<>();

        LocalDateTime start = eventParams.getRangeStart() == null ? LocalDateTime.now() :
                LocalDateTime.parse(eventParams.getRangeStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd " +
                        "HH:mm:ss"));
        LocalDateTime end = eventParams.getRangeEnd() != null ? end = LocalDateTime.parse(eventParams.getRangeEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd " +
                "HH:mm:ss")) : null;

        if (end != null && end.isBefore(start)) {
            throw new ValidationException("Дата окончания не может быть раньше начала");
        }

        if (eventParams.getCategories() == null) {
            eventParams.setCategories(List.of());
        }
        log.info("Поиск события, параметры: " + eventParams);
        EventSort eventSort;

        if (eventParams.getSort() != null) {
            eventSort = EventSort.valueOf(eventParams.getSort());
            if (eventSort.equals(EventSort.EVENT_DATE)) {
                events = eventRepository.findPublic(
                        eventParams.getText(),
                        start,
                        end,
                        eventParams.getCategories(),
                        eventParams.getPaid(),
                        eventParams.getOnlyAvailable(),
                        eventParams.getFrom(),
                        eventParams.getSize());

                eventShortDtos = events.stream().map(EventMapper::toShortDto).toList();
                eventsSetCountRequestsAndViews(eventShortDtos);

            } else if (eventSort.equals(EventSort.VIEWS)) {
                // Сортировка по количеству запросов:
                // Получаем из БД все результаты без ограничений
                events = eventRepository.findPublic(
                        eventParams.getText(),
                        start,
                        end,
                        eventParams.getCategories(),
                        eventParams.getPaid(),
                        eventParams.getOnlyAvailable());

                eventShortDtos = events.stream().map(EventMapper::toShortDto).collect(Collectors.toList());
                // Заполняем количество запросов и просмотров
                eventsSetCountRequestsAndViews(eventShortDtos);

                // Сортируем список по количеству просмотров событий
                Collections.sort(eventShortDtos, Comparator.comparing(EventShortDto::getViews));
                // Отсекаем лишнее в списке
                if (eventParams.getFrom() >= eventShortDtos.size()) {
                    eventShortDtos = List.of();
                } else {
                    int lastIndex = eventParams.getFrom() + eventParams.getSize();
                    eventShortDtos = eventShortDtos.subList(eventParams.getFrom(), Math.min(lastIndex,
                            eventShortDtos.size() - 1));
                }
            }
        } else {
            // Результаты без указания сортировки
            events = eventRepository.findPublicNoSort(
                    eventParams.getText(),
                    start,
                    end,
                    eventParams.getCategories(),
                    eventParams.getPaid(),
                    eventParams.getOnlyAvailable(),
                    eventParams.getFrom(),
                    eventParams.getSize());

            eventShortDtos = events.stream().map(EventMapper::toShortDto).toList();
            eventsSetCountRequestsAndViews(eventShortDtos);
        }

        // Запись запроса в статистику
        eventStatsHandler.addHit(request);

        return eventShortDtos;
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {
        Event event =
                eventRepository.findByIdAndState(eventId,
                        EventState.PUBLISHED).orElseThrow(() -> new NotFoundException(
                        "Ошибка! События с заданными параметрами не существует или оно еще не опубликовано"));
        EventFullDto eventFullDto = EventMapper.toDto(event);

        eventSetCountRequestsAndViews(eventFullDto);

        // Запись запроса в статистику
        eventStatsHandler.addHit(request);

        return eventFullDto;
    }

    private <T extends EventShortDto> void eventsSetCountRequestsAndViews(List<T> eventDtos) {
        List<Long> eventIds = eventDtos.stream().map(EventShortDto::getId).toList();

        // Заполнение количества подтвержденных запросов
        List<EventCountConfirmedRequests> eventRequestsCount =
                requestRepository.getCountRequests(eventIds);

        eventDtos.forEach(e -> e.setConfirmedRequests(eventRequestsCount.stream()
                .filter(er -> er.getEventId().equals(e.getId()))
                .findFirst().get().getCount()));

        // Заполнение количества просмотров событий
        Map<Long, Integer> eventView = eventStatsHandler.getCountHitsToEvents(eventIds);
        eventDtos.forEach(e -> e.setViews(eventView.getOrDefault(e.getId(), 0)));
    }

    private void eventSetCountRequestsAndViews(EventShortDto eventDto) {
        // Заполнение количества подтвержденных запросов
        eventDto.setConfirmedRequests(requestRepository.getCountConfirmedRequestByEvent(eventDto.getId()));

        // Заполнение количества просмотров события
        eventDto.setViews(eventStatsHandler.getCountHitsToEvent(eventDto.getId()));
    }

    @Override
    public List<RequestDto> getRequestsByEvent(Long userId, Long eventId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Пользователя с заданным идентификатором не существует"));

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Ошибка! Категории с заданным идентификатором не существует"));

        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Ошибка! Событие принадлежит другому пользователю!");
        }

        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream().map(RequestMapper::toDto).toList();
    }

    @Override
    public RequestsStatusUpdateResultDto patchRequestsStatus(Long userId, Long eventId,
                                                             RequestsStatusUpdateDto requestsStatusUpdateDto) {
        List<Request> requests = requestRepository.findAllById(requestsStatusUpdateDto.getRequestIds());

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Ошибка! События с заданным идентификатором не существует"));

        if (requests.size() != requestsStatusUpdateDto.getRequestIds().size()) {
            throw new NotFoundException("Ошибка! Не найдены запросы по указанным идентификаторам");
        }

        if (requestsStatusUpdateDto.getStatus() == RequestStatus.REJECTED) {
            // Если запрос на отклонение заявок...
            // Проверка на то, что в списке заявок есть уже подтвержденные
            requests.forEach(r -> {
                if (r.getStatus() == RequestStatus.CONFIRMED) {
                    throw new ConflictException("Попытка отменить уже подтвержденную заявку");
                }
            });
        } else if (requestsStatusUpdateDto.getStatus() == RequestStatus.CONFIRMED) {
            // Если идет запрос на подтверждение заявок...
            // Проверка, что хватает места на все заявки
            Integer countConfirmedRequest = requestRepository.getCountConfirmedRequestByEvent(eventId);

            log.info("Количество подтвержденных запросов на участие в событии: {}", countConfirmedRequest);

            if (event.getParticipantLimit() != 0 &&
                    event.getParticipantLimit() - countConfirmedRequest < requests.size()) {
                throw new ConflictException("На событие не осталось свободных мест");
            }
        }

        requests.forEach(r -> r.setStatus(requestsStatusUpdateDto.getStatus()));

        List<Request> res = requestRepository.saveAll(requests);

        List<RequestDto> resDto = res.stream().map(RequestMapper::toDto).toList();
        RequestsStatusUpdateResultDto result = new RequestsStatusUpdateResultDto();

        if (requestsStatusUpdateDto.getStatus().equals(RequestStatus.CONFIRMED)) {
            result.setConfirmedRequests(resDto);
        } else {
            result.setRejectedRequests(resDto);
        }

        return result;
    }

    public List<EventFullDto> getEventsByAdmin(EventAdminParams eap) {

        List<Integer> statesNum = List.of();
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (eap.getStates() != null) {
            statesNum = eap.getStates().stream().map(EventState::valueOf).map(Enum::ordinal).toList();
        }
        if (eap.getRangeStart() != null) {
            start = LocalDateTime.parse(eap.getRangeStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (eap.getRangeEnd() != null) {
            end = LocalDateTime.parse(eap.getRangeEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        if (eap.getUsersIds() == null) {
            eap.setUsersIds(List.of());
        }
        if (eap.getCategoriesIds() == null) {
            eap.setCategoriesIds(List.of());
        }

        List<Event> events = eventRepository.findByAdmin(eap.getUsersIds(), statesNum, eap.getCategoriesIds(),
                start, end, eap.getFrom(), eap.getSize());

        List<EventFullDto> eventFullDtos = events.stream().map(EventMapper::toDto).toList();

        this.eventsSetCountRequestsAndViews(eventFullDtos);

        return eventFullDtos;
    }
}