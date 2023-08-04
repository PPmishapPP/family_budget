package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {
}
