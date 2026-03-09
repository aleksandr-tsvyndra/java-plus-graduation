package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.event.PublicEventApi;

@FeignClient(name = "event-service")
public interface PublicEventClient extends PublicEventApi {
}
