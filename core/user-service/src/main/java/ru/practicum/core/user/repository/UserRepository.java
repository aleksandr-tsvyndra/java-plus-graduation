package ru.practicum.core.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.core.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

}
