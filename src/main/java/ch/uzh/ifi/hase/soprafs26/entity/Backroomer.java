package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "backroomers")
public class Backroomer extends Player{
    @JoinColumn(name = "backroomer_id")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Role> delegatedCharacters;

    public List<Role> getDelegatedCharacters() {
        return delegatedCharacters;
    }

    public void setDelegatedCharacters(List<Role> delegatedCharacters) {
        this.delegatedCharacters = delegatedCharacters;
    }
}
