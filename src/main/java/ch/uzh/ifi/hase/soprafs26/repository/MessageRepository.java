package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;


@Repository("messageRepository")
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByScenarioId(Long scenarioId);
    @Query("""
    SELECT m FROM Message m
    WHERE (m.creator.id = :a AND m.recipient.id = :b)
       OR (m.creator.id = :b AND m.recipient.id = :a)
    ORDER BY m.createdAt ASC
""")
    List<Message> findConversation(Long a, Long b);
}