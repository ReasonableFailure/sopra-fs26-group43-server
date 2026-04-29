package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Pronouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

@Repository("newsRepository")
public interface NewsRepository extends JpaRepository<NewsStory, Long> {
    List<NewsStory> findByScenarioIdOrderByCreatedAtAsc(Long scenarioId);
    @Query("""
        SELECT p
        FROM Pronouncement p
        WHERE p.author.id = :authorId
        AND p.scenario.id = :scenarioId
    """)
    List<Pronouncement> findPronouncementsByAuthorIdAndScenarioId(
            Long authorId,
            Long scenarioId
    );
}