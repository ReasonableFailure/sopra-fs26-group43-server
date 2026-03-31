package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Scenario;

@Repository("scenarioRepository")
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    Scenario findByTitle(String title);
}
