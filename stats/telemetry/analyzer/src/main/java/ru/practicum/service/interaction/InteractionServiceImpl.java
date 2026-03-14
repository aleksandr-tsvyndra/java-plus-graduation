package ru.practicum.service.interaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.Interaction;
import ru.practicum.repository.InteractionRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final InteractionRepository interactionRepo;

    @Override
    @Transactional
    public void addInteraction(UserActionAvro value) {
        Optional<Interaction> oldInteractionOpt = interactionRepo.findByUserIdAndEventId(value.getUserId(), value.getEventId());
        if (oldInteractionOpt.isEmpty()) {
            Interaction interaction = buildInteraction(value);
            interactionRepo.save(interaction);
        } else {
            Interaction oldInteraction = oldInteractionOpt.get();
            double oldRating = oldInteraction.getRating();
            double newRating = getRatingByActionType(value.getActionType());
            if (newRating >= oldRating) {
                oldInteraction.setRating(newRating);
                LocalDateTime oldTimestamp = oldInteraction.getTimestamp();
                if (oldTimestamp == null || oldTimestamp.isBefore(LocalDateTime.ofInstant(value.getTimestamp(), ZoneId.systemDefault()))) {
                    oldInteraction.setTimestamp(LocalDateTime.ofInstant(value.getTimestamp(), ZoneId.systemDefault()));
                }
                interactionRepo.save(oldInteraction);
            }
        }
    }

    private double getRatingByActionType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private Interaction buildInteraction(UserActionAvro value) {
        Interaction interaction = new Interaction();
        interaction.setUserId(value.getUserId());
        interaction.setEventId(value.getEventId());
        interaction.setRating(getRatingByActionType(value.getActionType()));
        interaction.setTimestamp(LocalDateTime.ofInstant(value.getTimestamp(), ZoneId.systemDefault()));
        return interaction;
    }
}
