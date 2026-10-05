package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

}
