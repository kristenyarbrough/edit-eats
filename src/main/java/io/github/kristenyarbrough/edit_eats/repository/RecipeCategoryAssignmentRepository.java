package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategoryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeCategoryAssignmentRepository extends JpaRepository<RecipeCategoryAssignment, Long> {
}
