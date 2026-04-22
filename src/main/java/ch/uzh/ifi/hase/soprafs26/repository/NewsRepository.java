package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("newsRepository")
public interface NewsRepository extends JpaRepository<NewsStory, Long> {
    List<NewsStory> findByScenarioIdOrderByCreatedAtAsc(Long scenarioId);
    List<NewsStory> findAllByScenarioId(Long scenarioId);
}