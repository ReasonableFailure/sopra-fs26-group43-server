package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import ch.uzh.ifi.hase.soprafs26.repository.DirectiveRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs26.repository.NewsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebAppConfiguration
@SpringBootTest
@Transactional
class ScenarioServiceIntegrationTest {

    @Qualifier("scenarioRepository")
    @Autowired
    private ScenarioRepository scenarioRepository;

    @Qualifier("roleRepository")
    @Autowired
    private RoleRepository roleRepository;

    @Qualifier("messageRepository")
    @Autowired
    private MessageRepository messageRepository;

    @Qualifier("directiveRepository")
    @Autowired
    private DirectiveRepository directiveRepository;

    @Qualifier("newsRepository")
    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private ScenarioService scenarioService;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private MastodonClient mastodonClient;

    private Scenario testScenario;
    private Director testDirector;

    @BeforeEach
    void setup() {
        newsRepository.deleteAll();
        messageRepository.deleteAll();
        directiveRepository.deleteAll();
        scenarioRepository.deleteAll();
        roleRepository.deleteAll();

        testDirector = new Director();
        testDirector.setToken("director-token");

        testScenario = new Scenario();
        testScenario.setTitle("Integration Scenario");
        testScenario.setDescription("Integration description");
        testScenario.setStatus(ScenarioStatus.UNSTARTED);
        testScenario.setDayNumber(0);
        testScenario.setExchangeRate(5);
        testScenario.setStartingMessageCount(10);
        testScenario.setPlayers(new ArrayList<>());
        testScenario.setHistory(new ArrayList<>());
        testScenario.setDirector(testDirector);
        testScenario.getPlayers().add(testDirector);
        testDirector.setScenario(testScenario);

        testScenario = scenarioRepository.save(testScenario);
    }

    @Test
    void getAllScenarios_validInput_success() {
        List<Scenario> scenarios = scenarioService.getScenarios();

        assertEquals(1, scenarios.size());
        assertEquals(testScenario.getId(), scenarios.get(0).getId());
        assertEquals("Integration Scenario", scenarios.get(0).getTitle());
    }

    @Test
    void createScenario_validInput_success() {
        ScenarioPostDTO postDTO = new ScenarioPostDTO();
        postDTO.setTitle("Created Scenario");
        postDTO.setDescription("Created description");
        postDTO.setExchangeRate(8);
        postDTO.setStartingMessageCount(14);
        postDTO.setDirector(testDirector.getId());

        Director createDirector = new Director();
        createDirector.setToken("create-director-token");

        when(playerService.getDirectorByID(testDirector.getId())).thenReturn(createDirector);

        Scenario created = scenarioService.createScenario(postDTO);

        assertNotNull(created.getId());
        assertEquals("Created Scenario", created.getTitle());
        assertEquals(ScenarioStatus.UNSTARTED, created.getStatus());
        assertEquals(createDirector, created.getDirector());
    }

    @Test
    void getScenarioById_validInput_success() {
        Scenario scenario = scenarioService.getScenarioById(testScenario.getId());

        assertEquals(testScenario.getTitle(), scenario.getTitle());
        assertEquals(testScenario.getDescription(), scenario.getDescription());
    }

    @Test
    void getScenarioById_scenarioNotFound_throwsException() {
        assertThrows(ResponseStatusException.class, () -> scenarioService.getScenarioById(999L));
    }

    @Test
    void updateScenario_validInput_success() {
        ScenarioPutDTO putDTO = new ScenarioPutDTO();
        putDTO.setTitle("Updated Title");
        putDTO.setDescription("Updated description");
        putDTO.setStatus(ScenarioStatus.COMPLETED);
        putDTO.setDayNumber(5);
        putDTO.setExchangeRate(20);
        putDTO.setStartingMessageCount(22);

        scenarioService.updateScenario(testScenario.getId(), putDTO);

        Scenario updated = scenarioRepository.findById(testScenario.getId()).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(ScenarioStatus.COMPLETED, updated.getStatus());
        assertEquals(5, updated.getDayNumber());
        assertEquals(20, updated.getExchangeRate());
        assertEquals(22, updated.getStartingMessageCount());
    }

    @Test
    void addCommunicationToHistory_validInput_success() {
        NewsStory newsStory = new NewsStory();
        newsStory.setTitle("History News");
        newsStory.setBody("History Body");
        newsStory.setCreatedAt(Instant.now());
        newsStory.setDayNumber(0);
        newsStory.setScenario(testScenario);
        newsRepository.save(newsStory);

        scenarioService.addCommunicationToHistory(testScenario.getId(), newsStory);

        Scenario loaded = scenarioService.getScenarioById(testScenario.getId());
        assertEquals(1, loaded.getHistory().size());
        assertEquals(newsStory.getTitle(), loaded.getHistory().get(0).getTitle());
    }

    @Test
    void addCommunicationToHistory_scenarioNotFound_throwsException() {
        NewsStory newsStory = new NewsStory();
        newsStory.setTitle("History News");
        newsStory.setBody("History Body");
        newsStory.setCreatedAt(Instant.now());
        newsStory.setDayNumber(0);

        assertThrows(ResponseStatusException.class, () -> scenarioService.addCommunicationToHistory(999L, newsStory));
    }

    @Test
    void deleteScenario_validInput_success() {
        assertDoesNotThrow(() -> scenarioService.deleteScenario(testScenario.getId()));
        assertFalse(scenarioRepository.findById(testScenario.getId()).isPresent());
    }

    @Test
    void deleteScenario_scenarioNotFound_throwsException() {
        assertThrows(ResponseStatusException.class, () -> scenarioService.deleteScenario(999L));
    }

    @Test
    void getRoles_validInput_success() {
        Role role = new Role();
        role.setToken("role-token");
        role.setName("Test Role");
        role.setTitle("Role Title");
        role.setDescription("Role description");
        role.setSecret("secret");
        role.setAlive(true);
        role.setMessageCount(5);
        role.setTotalPoints(10);
        role.setPointsBalance(10);
        role.setNumberMessages(0);
        role.setNumberPronouncements(0);
        role.setTotalTextLength(0);
        role.setScenario(testScenario);
        testScenario.getPlayers().add(role);
        roleRepository.save(role);
        scenarioRepository.save(testScenario);

        List<Role> roles = scenarioService.getRoles(testScenario.getId());

        assertEquals(1, roles.size());
        assertEquals(role.getId(), roles.get(0).getId());
    }

    @Test
    void getRoles_scenarioNotFound_throwsException() {
        assertThrows(ResponseStatusException.class, () -> scenarioService.getRoles(999L));
    }

    @Test
    void updateMastodonConfig_validInput_success() {
        ScenarioMastodonDTO mastodonDTO = new ScenarioMastodonDTO();
        mastodonDTO.setMastodonBaseUrl("https://mastodon.example");
        mastodonDTO.setMastodonAccessToken("token-abc");

        when(mastodonClient.fetchMastodonProfileUrl(mastodonDTO.getMastodonBaseUrl(), mastodonDTO.getMastodonAccessToken()))
                .thenReturn("https://mastodon.example/@director");

        scenarioService.updateMastodonConfig(testScenario.getId(), mastodonDTO);

        Scenario updated = scenarioRepository.findById(testScenario.getId()).orElseThrow();
        assertEquals("https://mastodon.example", updated.getMastodonBaseUrl());
        assertEquals("token-abc", updated.getMastodonAccessToken());
        assertEquals("https://mastodon.example/@director", updated.getMastodonProfileUrl());
    }

    @Test
    void updateMastodonConfig_scenarioNotFound_throwsException() {
        ScenarioMastodonDTO mastodonDTO = new ScenarioMastodonDTO();
        mastodonDTO.setMastodonBaseUrl("https://mastodon.example");
        mastodonDTO.setMastodonAccessToken("token-abc");

        assertThrows(ResponseStatusException.class, () -> scenarioService.updateMastodonConfig(999L, mastodonDTO));
    }
}
