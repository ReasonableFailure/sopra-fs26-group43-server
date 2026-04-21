package ch.uzh.ifi.hase.soprafs26.entity;
import jakarta.persistence.*;

@Entity
@Table(name="characters")
public class Role extends Player{
    @Column(unique=false,nullable=false)
    private String name;
    @Column(unique=false,nullable=false)
    private String title;
    @Column(unique=false,nullable=false)
    private String description;
    @Column(unique=false,nullable=false)
    private String secret;
    @Column(nullable=false)
    private boolean alive;
    @Column(unique=false,nullable=false)
    private int messageCount;
    @Column(unique=false,nullable=false)
    private int actionPoints;
    @Column(unique=false,nullable=true)
    private byte[] portrait;
    @Column(unique = false, nullable = false)
    private Long assignedCabinet;

    public void die(){
        this.alive = false;
    }

    public void buyMessages(int exchangeRate, int desiredIncrease) throws Exception{
        if(this.actionPoints >= exchangeRate*desiredIncrease){
            this.messageCount+=desiredIncrease;
            this.actionPoints -= exchangeRate*desiredIncrease;
        } else {
            throw new Exception("You do not have enough action points for this purchase");
        }
    }

    public void gainActionPoints(int points){
        this.actionPoints += points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean getAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public int getActionPoints() {
        return actionPoints;
    }

    public void setActionPoints(int actionPoints) {
        this.actionPoints = actionPoints;
    }

    public byte[] getPortrait() {
        return portrait;
    }

    public void setPortrait(byte[] portrait) {
        this.portrait = portrait;
    }

    public Long getAssignedCabinet() {
        return assignedCabinet;
    }

    public void setAssignedCabinet(Long assignedCabinet) {
        this.assignedCabinet = assignedCabinet;
    }
}
