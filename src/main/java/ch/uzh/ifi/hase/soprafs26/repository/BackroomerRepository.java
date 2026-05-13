package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("backroomerRepository")
public interface BackroomerRepository extends JpaRepository<Backroomer, Long>{
    Optional<Backroomer> findByToken(String token);
}