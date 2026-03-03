package ru.practicum.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.error_processing.exceptions.ValidationException;
import ru.practicum.model.entity.Stat;
import ru.practicum.model.mapper.StatMapper;
import ru.practicum.model.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;
    private final StatMapper statMapper;

    @Override
    @Transactional
    public void addHit(EndpointHitDto endpointHitDto) {
        Stat stat = statMapper.toStat(endpointHitDto);
        statRepository.save(stat);
    }

    @Override
    public List<StatResponseDto> getStats(LocalDateTime start, LocalDateTime end,
                                          List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Start date is after end date");
        }

        if (unique) {
            if (uris == null || uris.isEmpty()) {
                return statRepository.findUniqueStats(start, end);
            } else {
                return statRepository.findUniqueStatsByUris(start, end, uris);
            }
        } else {
            if (uris == null || uris.isEmpty()) {
                return statRepository.findStats(start, end);
            } else {
                return statRepository.findStatsByUris(start, end, uris);
            }
        }
    }
}