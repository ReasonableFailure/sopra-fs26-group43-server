package ch.uzh.ifi.hase.soprafs26.entity;
import java.util.List;

public class Cabinet {
    private Long id;
    private String cabinetName;
    private String cabinetDescription;
    private List<Character> cabinetMembers;
    public void addCharacter(Character character){
        cabinetMembers.add(character);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCabinetName() {
        return cabinetName;
    }

    public void setCabinetName(String cabinetName) {
        this.cabinetName = cabinetName;
    }

    public String getCabinetDescription() {
        return cabinetDescription;
    }

    public void setCabinetDescription(String cabinetDescription) {
        this.cabinetDescription = cabinetDescription;
    }

    public List<Character> getCabinetMembers() {
        return cabinetMembers;
    }

    public void setCabinetMembers(List<Character> cabinetMembers) {
        this.cabinetMembers = cabinetMembers;
    }
}
