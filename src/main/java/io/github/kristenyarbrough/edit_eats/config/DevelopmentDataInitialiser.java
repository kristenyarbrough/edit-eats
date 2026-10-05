package io.github.kristenyarbrough.edit_eats.config;

import io.github.kristenyarbrough.edit_eats.domain.User;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DevelopmentDataInitialiser implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        userRepository.findByUsername("development-user")
                .orElseGet(() ->
                        userRepository.save(
                                User.builder()
                                        .username("development-user")
                                        .build()
                        )
                );

    }

}
