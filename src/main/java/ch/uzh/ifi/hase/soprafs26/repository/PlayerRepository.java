package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Player;


@Repository("playerRepository")
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByUser_Id(Long userId);
    Optional<Player> findById(Long userId);
    Optional<Player> findByToken(String token);
}
