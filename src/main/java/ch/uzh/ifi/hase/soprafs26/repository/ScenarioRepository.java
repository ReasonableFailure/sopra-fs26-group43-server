package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Scenario;

import java.util.Optional;

@Repository("scenarioRepository")
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    Optional<Scenario> findByTitle(String title);
    Optional<Scenario> findById(Long id);

}
