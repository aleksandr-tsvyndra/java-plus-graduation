package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.user.AdminUserApi;

@FeignClient(name = "user-service")
public interface AdminUserServiceClient extends AdminUserApi {
}
