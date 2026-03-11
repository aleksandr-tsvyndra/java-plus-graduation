package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.category.PublicCategoryApi;

@FeignClient(name = "category-service")
public interface CategoryServiceClient extends PublicCategoryApi {
}
