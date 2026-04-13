package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Directive;

@Repository("directiveRepository")
public interface DirectiveRepository extends JpaRepository<Directive, Long> {
    List<Directive> findByCreatorId(Long creatorId);

    List<Directive> findByScenarioId(Long scenarioId);
}