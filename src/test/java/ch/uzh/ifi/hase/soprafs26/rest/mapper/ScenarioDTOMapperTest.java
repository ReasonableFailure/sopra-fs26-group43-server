package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScenarioDTOMapperTest {

    @Test
    public void testConvertEntityToScenarioGetDTO_success() {
        Director director = new Director();
        director.setToken("Director test-director-token");

        Scenario scenario = new Scenario();
        scenario.setId(1L);
        scenario.setTitle("Test Scenario");
        scenario.setDescription("Test Description");
        scenario.setActive(true);
        scenario.setDayNumber(2);
        scenario.setExchangeRate(5);
        scenario.setStartingMessageCount(15);
        scenario.setDirector(director);

        ScenarioGetDTO dto = ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);

        assertEquals(scenario.getId(), dto.getId());
        assertEquals(scenario.getTitle(), dto.getTitle());
        assertEquals(scenario.getDescription(), dto.getDescription());
        assertEquals(scenario.getActive(), dto.getActive());
        assertEquals(scenario.getDayNumber(), dto.getDayNumber());
        assertEquals(scenario.getExchangeRate(), dto.getExchangeRate());
        assertEquals(scenario.getStartingMessageCount(), dto.getStartingMessageCount());
        assertEquals(director.getToken(), dto.getDirectorToken());
    }

    @Test
    public void testConvertEntityToScenarioGetDTO_noDirector_directorTokenNull() {
        Scenario scenario = new Scenario();
        scenario.setId(1L);
        scenario.setTitle("Test Scenario");
        scenario.setActive(false);
        scenario.setDayNumber(0);
        scenario.setExchangeRate(10);
        scenario.setStartingMessageCount(10);
        scenario.setDirector(null);

        ScenarioGetDTO dto = ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);

        assertNull(dto.getDirectorToken());
    }

    @Test
    public void testConvertScenarioPostDTOtoEntity_success() {
        ScenarioPostDTO postDTO = new ScenarioPostDTO();
        postDTO.setTitle("New Scenario");
        postDTO.setExchangeRate(3);
        postDTO.setStartingMessageCount(20);

        Scenario scenario = ScenarioDTOMapper.INSTANCE.convertScenarioPostDTOtoEntity(postDTO);

        assertEquals(postDTO.getTitle(), scenario.getTitle());
        assertEquals(postDTO.getExchangeRate(), scenario.getExchangeRate());
        assertEquals(postDTO.getStartingMessageCount(), scenario.getStartingMessageCount());
        assertNull(scenario.getId());
        assertNull(scenario.getDirector());
        assertNull(scenario.getPlayers());
    }

    @Test
    public void testConvertScenarioPutDTOtoEntity_success() {
        Scenario existing = new Scenario();
        existing.setId(1L);
        existing.setTitle("Old Title");
        existing.setActive(false);
        existing.setDayNumber(0);
        existing.setExchangeRate(1);
        existing.setStartingMessageCount(10);

        Director director = new Director();
        director.setToken("Director original-token");
        existing.setDirector(director);

        ScenarioPutDTO putDTO = new ScenarioPutDTO();
        putDTO.setTitle("New Title");
        putDTO.setActive(true);
        putDTO.setDayNumber(3);
        putDTO.setExchangeRate(7);

        ScenarioDTOMapper.INSTANCE.convertScenarioPutDTOtoEntity(putDTO, existing);

        assertEquals("New Title", existing.getTitle());
        assertEquals(true, existing.getActive());
        assertEquals(3, existing.getDayNumber());
        assertEquals(7, existing.getExchangeRate());
        assertEquals(10, existing.getStartingMessageCount());
        assertEquals(director, existing.getDirector());
        assertEquals(1L, existing.getId());
    }
}
