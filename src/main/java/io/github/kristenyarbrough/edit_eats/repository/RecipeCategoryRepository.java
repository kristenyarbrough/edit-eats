package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory, Long> {

    List<RecipeCategory> findAllByUserIdIsNullOrUserIdOrderByNameAsc(
            Long userId
    );

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM RecipeCategory c
            WHERE LOWER(c.name) = LOWER(:name)
                AND (c.user IS NULL OR c.user.id = :userId)
            """)
    boolean existsAvailableCategoryForUser(
            Long userId,
            String name
    );

}
