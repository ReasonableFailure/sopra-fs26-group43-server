package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
public class ScenarioServiceIntegrationTest {

    @Qualifier("scenarioRepository")
    @Autowired
    private ScenarioRepository scenarioRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScenarioService scenarioService;

    private User testUser;
    private ScenarioPostDTO testPostDTO;

    @BeforeEach
    public void setup() {
        scenarioRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setToken("valid-integration-token");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setCreationDate(new Date());
        testUser = userRepository.save(testUser);

        testPostDTO = new ScenarioPostDTO();
        testPostDTO.setTitle("Integration Test Scenario");
        testPostDTO.setExchangeRate(5);
        testPostDTO.setStartingMessageCount(15);
    }

    @Test
    public void createScenario_validInput_success() {
        Scenario created = scenarioService.createScenario(testUser.getToken(), testPostDTO);

        assertNotNull(created.getId());
        assertEquals(testPostDTO.getTitle(), created.getTitle());
        assertTrue(created.getActive());
        assertEquals(0, created.getDayNumber());
        assertNotNull(created.getDirector());
        assertEquals(testPostDTO.getExchangeRate(), created.getExchangeRate());
        assertEquals(testPostDTO.getStartingMessageCount(), created.getStartingMessageCount());
    }

    @Test
    public void getScenarios_validToken_returnsScenarios() {
        scenarioService.createScenario(testUser.getToken(), testPostDTO);

        List<Scenario> result = scenarioService.getScenarios(testUser.getToken());

        assertFalse(result.isEmpty());
        assertEquals(testPostDTO.getTitle(), result.get(0).getTitle());
    }

    @Test
    public void getScenarioById_validId_success() {
        Scenario created = scenarioService.createScenario(testUser.getToken(), testPostDTO);

        Scenario found = scenarioService.getScenarioById(testUser.getToken(), created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(testPostDTO.getTitle(), found.getTitle());
    }

    @Test
    public void getScenarioById_scenarioNotFound_throwsException() {
        assertThrows(ResponseStatusException.class,
                () -> scenarioService.getScenarioById(testUser.getToken(), 999L));
    }

    @Test
    public void deleteScenario_validInput_success() {
        Scenario created = scenarioService.createScenario(testUser.getToken(), testPostDTO);
        Long id = created.getId();

        scenarioService.deleteScenario(testUser.getToken(), id);

        assertFalse(scenarioRepository.existsById(id));
    }

    @Test
    public void deleteScenario_scenarioNotFound_throwsException() {
        assertThrows(ResponseStatusException.class,
                () -> scenarioService.deleteScenario(testUser.getToken(), 999L));
    }

    @Test
    public void updateScenario_validInput_success() {
        Scenario created = scenarioService.createScenario(testUser.getToken(), testPostDTO);

        ScenarioPutDTO putDTO = new ScenarioPutDTO();
        putDTO.setTitle("Updated Title");
        putDTO.setActive(false);
        putDTO.setDayNumber(3);
        putDTO.setExchangeRate(10);

        scenarioService.updateScenario(testUser.getToken(), created.getId(), putDTO);

        Scenario updated = scenarioService.getScenarioById(testUser.getToken(), created.getId());
        assertEquals("Updated Title", updated.getTitle());
        assertFalse(updated.getActive());
        assertEquals(3, updated.getDayNumber());
        assertEquals(10, updated.getExchangeRate());
    }

    @Test
    public void updateMastodonConfig_validInput_success() {
        Scenario created = scenarioService.createScenario(testUser.getToken(), testPostDTO);

        ScenarioMastodonDTO mastodonDTO = new ScenarioMastodonDTO();
        mastodonDTO.setMastodonBaseUrl("https://mastodon.example.com");
        mastodonDTO.setMastodonAccessToken("mastodon-token-abc");

        scenarioService.updateMastodonConfig(created.getId(), testUser.getToken(), mastodonDTO);

        Scenario updated = scenarioService.getScenarioById(testUser.getToken(), created.getId());
        assertEquals("https://mastodon.example.com", updated.getMastodonBaseUrl());
        assertEquals("mastodon-token-abc", updated.getMastodonAccessToken());
    }
}
