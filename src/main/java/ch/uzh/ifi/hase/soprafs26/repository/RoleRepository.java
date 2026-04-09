package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Player;

import java.util.Optional;

@Repository("roleRepository")

public interface RoleRepository {
    Optional<Player> findById(Long roleId);
}
