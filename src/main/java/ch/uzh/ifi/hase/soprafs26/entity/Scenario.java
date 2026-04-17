package ch.uzh.ifi.hase.soprafs26.entity;
import  jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "scenarios")
public class Scenario implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable=false, unique=true)
    private Long id;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = true)
    private String description;
    @Column(nullable = true)
    private String title;
    @Column(nullable = false)
    private int dayNumber;
    @Column(nullable = true)
    private int exchangeRate;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "scenario_id")
    private List<Player> players;

    @OneToMany(mappedBy = "scenario")
    private List<Communication> history;

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player){
        this.players.add(player);
    }

    public void addComm(Communication comm){
        this.history.add(comm);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(int exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public List<Communication> getHistory() {
        return history;
    }

    public void setHistory(List<Communication> history) {
        this.history = history;
    }
}
