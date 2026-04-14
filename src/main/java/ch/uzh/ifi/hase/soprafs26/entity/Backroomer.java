package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "backroomers")
public class Backroomer extends Player{
    @Column(nullable = false)
            @OneToOne(cascade = CascadeType.ALL)
    List<Long> delegatedCharacters;
}
