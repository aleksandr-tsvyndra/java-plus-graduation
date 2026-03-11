package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.request.PrivateRequestApi;

@FeignClient(name = "request-service")
public interface ParticipationRequestServiceClient extends PrivateRequestApi {
}
