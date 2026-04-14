package ch.uzh.ifi.hase.soprafs26.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "directors")
public class Director extends Backroomer{

    @ManyToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

}
