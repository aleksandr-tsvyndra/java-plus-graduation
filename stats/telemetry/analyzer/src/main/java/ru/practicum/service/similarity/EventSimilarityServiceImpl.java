package ru.practicum.service.similarity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.Similarity;
import ru.practicum.repository.SimilarityRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {
    private final SimilarityRepository similarityRepo;

    @Override
    @Transactional
    public void addSimilarity(EventSimilarityAvro value) {
        Similarity eventSimilarity = buildSimilarity(value);
        similarityRepo.findByEvent1AndEvent2(eventSimilarity.getEventId1(), eventSimilarity.getEventId2())
                .ifPresent(oldEventSimilarity -> eventSimilarity.setId(oldEventSimilarity.getId()));
        similarityRepo.save(eventSimilarity);
    }

    private Similarity buildSimilarity(EventSimilarityAvro value) {
        Similarity eventSimilarity = new Similarity();
        eventSimilarity.setEventId1(value.getEventA());
        eventSimilarity.setEventId2(value.getEventB());
        eventSimilarity.setSimilarity(value.getScore());
        eventSimilarity.setTimestamp(LocalDateTime.ofInstant(value.getTimestamp(), ZoneId.systemDefault()));
        return eventSimilarity;
    }
}
