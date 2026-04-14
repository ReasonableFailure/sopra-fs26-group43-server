package ch.uzh.ifi.hase.soprafs26.entity;
import jakarta.persistence.*;

@Entity
@Table(name="characters")
public class Role extends Player{
    private String name;
    private String title;
    private String description;
    private String secret;
    private boolean alive;
    private int messageCount;
    private int actionPoints;
    private byte[] portrait;

    public void die(){
        this.alive = false;
    }

    public boolean buyMessages(int exchangeRate, int desiredIncrease) throws Exception{
        if(this.actionPoints >= exchangeRate*desiredIncrease){
            this.messageCount+=desiredIncrease;
            return true;
        } else {
            throw new Exception("You do not have enough action points for this purchase");
        }
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
}
