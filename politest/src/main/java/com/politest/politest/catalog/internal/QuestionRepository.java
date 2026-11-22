package com.politest.politest.catalog.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("select q.id from Question q where q.active = true order by q.id")
    List<Long> findActiveIds();

    long countByActiveTrue();
}
