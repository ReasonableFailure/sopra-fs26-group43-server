package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;
import java.util.Optional;

import ch.uzh.ifi.hase.soprafs26.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("playerRepository")
public interface PlayerRepository extends JpaRepository<Player, Long>{
    Player findByToken(String token);
    List<Player> findByUser_Id(Long userId);
    Optional<Player> findFirstByUser_IdAndScenario_Id(Long userId, Long scenarioId);
}
