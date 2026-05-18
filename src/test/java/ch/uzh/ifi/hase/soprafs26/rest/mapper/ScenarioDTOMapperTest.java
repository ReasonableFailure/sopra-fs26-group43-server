package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScenarioDTOMapperTest {

    @Test
    public void testConvertEntityToScenarioGetDTO_success() {
        Scenario scenario = new Scenario();
        scenario.setId(10L);
        scenario.setTitle("Scenario Title");
        scenario.setDescription("Scenario Description");
        scenario.setStatus(ScenarioStatus.UNSTARTED);
        scenario.setDayNumber(3);
        scenario.setExchangeRate(12);
        scenario.setStartingMessageCount(5);
        scenario.setFinishTime(LocalDateTime.of(2025, 1, 1, 12, 0));
        scenario.setMastodonProfileUrl("https://mastodon.example.com/test");

        ScenarioGetDTO dto = ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);

        assertEquals(scenario.getId(), dto.getId());
        assertEquals(scenario.getTitle(), dto.getTitle());
        assertEquals(scenario.getDescription(), dto.getDescription());
        assertEquals(scenario.getStatus(), dto.getStatus());
        assertEquals(scenario.getDayNumber(), dto.getDayNumber());
        assertEquals(scenario.getExchangeRate(), dto.getExchangeRate());
        assertEquals(scenario.getStartingMessageCount(), dto.getStartingMessageCount());
        assertEquals(scenario.getFinishTime(), dto.getFinishTime());
        assertEquals(scenario.getMastodonProfileUrl(), dto.getMastodonProfileUrl());
    }

    @Test
    public void testConvertScenarioPostDTOtoEntity_setsDirectorId_success() {
        ScenarioPostDTO scenarioPostDTO = new ScenarioPostDTO();
        scenarioPostDTO.setTitle("Scenario Title");
        scenarioPostDTO.setDescription("Scenario Description");
        scenarioPostDTO.setExchangeRate(15);
        scenarioPostDTO.setStartingMessageCount(8);
        scenarioPostDTO.setDirector(99L);

        Scenario scenario = ScenarioDTOMapper.INSTANCE.convertScenarioPostDTOtoEntity(scenarioPostDTO);

        assertEquals(scenarioPostDTO.getTitle(), scenario.getTitle());
        assertEquals(scenarioPostDTO.getDescription(), scenario.getDescription());
        assertEquals(scenarioPostDTO.getExchangeRate(), scenario.getExchangeRate());
        assertEquals(scenarioPostDTO.getStartingMessageCount(), scenario.getStartingMessageCount());
        assertNotNull(scenario.getDirector());
        assertEquals(scenarioPostDTO.getDirector(), scenario.getDirector().getId());
    }
}
