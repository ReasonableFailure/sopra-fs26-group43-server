package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class ScenarioServiceTest {

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private UserService userService;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private ScenarioService scenarioService;

    private Scenario testScenario;
    private Director testDirector;
    private ScenarioPostDTO testPostDTO;
    private ScenarioPutDTO testPutDTO;
    private ScenarioMastodonDTO testMastodonDTO;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testDirector = new Director();
        testDirector.setToken("Director test-director-token");

        testScenario = new Scenario();
        testScenario.setId(1L);
        testScenario.setTitle("Test Scenario");
        testScenario.setDescription("Test Description");
        testScenario.setActive(true);
        testScenario.setDayNumber(1);
        testScenario.setExchangeRate(5);
        testScenario.setStartingMessageCount(15);
        testScenario.setPlayers(new ArrayList<>());
        testScenario.setHistory(new ArrayList<>());
        testScenario.setDirector(testDirector);

        testPostDTO = new ScenarioPostDTO();
        testPostDTO.setTitle("Test Scenario");
        testPostDTO.setExchangeRate(5);
        testPostDTO.setStartingMessageCount(15);

        testPutDTO = new ScenarioPutDTO();
        testPutDTO.setTitle("Updated Title");
        testPutDTO.setActive(false);
        testPutDTO.setDayNumber(2);
        testPutDTO.setExchangeRate(10);

        testMastodonDTO = new ScenarioMastodonDTO();
        testMastodonDTO.setMastodonBaseUrl("https://mastodon.example.com");
        testMastodonDTO.setMastodonAccessToken("mastodon-token-123");

        Mockito.when(scenarioRepository.save(any())).thenReturn(testScenario);
        Mockito.when(playerService.createDirector(any())).thenReturn(testDirector);
    }

    @Test
    public void getScenarios_validToken_returnsScenarios() {
        Mockito.when(scenarioRepository.findAll()).thenReturn(List.of(testScenario));

        List<Scenario> result = scenarioService.getScenarios("valid-token");

        assertEquals(1, result.size());
        assertEquals(testScenario.getTitle(), result.get(0).getTitle());
    }

    @Test
    public void getScenarioById_validId_success() {
        Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));

        Scenario result = scenarioService.getScenarioById("valid-token", 1L);

        assertEquals(testScenario.getId(), result.getId());
        assertEquals(testScenario.getTitle(), result.getTitle());
    }

    @Test
    public void getScenarioById_scenarioNotFound_throwsException() {
        Mockito.when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> scenarioService.getScenarioById("valid-token", 999L));
    }

    @Test
    public void createScenario_validInput_success() {
        Scenario created = scenarioService.createScenario("valid-token", testPostDTO);

        Mockito.verify(scenarioRepository, Mockito.times(1)).save(any());
        Mockito.verify(playerService, Mockito.times(1)).createDirector("valid-token");
        assertNotNull(created);
    }

    @Test
    public void deleteScenario_validInput_success() {
        Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));

        scenarioService.deleteScenario("valid-token", 1L);

        Mockito.verify(scenarioRepository, Mockito.times(1)).delete(testScenario);
    }

    @Test
    public void deleteScenario_scenarioNotFound_throwsException() {
        Mockito.when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> scenarioService.deleteScenario("valid-token", 999L));
    }

    @Test
    public void updateScenario_validInput_success() {
        Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));

        scenarioService.updateScenario("valid-token", 1L, testPutDTO);

        assertEquals("Updated Title", testScenario.getTitle());
        assertEquals(false, testScenario.getActive());
        assertEquals(2, testScenario.getDayNumber());
        assertEquals(10, testScenario.getExchangeRate());
    }

    @Test
    public void updateScenario_scenarioNotFound_throwsException() {
        Mockito.when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> scenarioService.updateScenario("valid-token", 999L, testPutDTO));
    }

    @Test
    public void getRoles_validScenarioId_returnsOnlyRoles() {
        Role role = new Role();
        role.setId(10L);
        role.setName("Test Role");

        List<Player> players = new ArrayList<>();
        players.add(role);
        players.add(testDirector);
        testScenario.setPlayers(players);

        Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));

        List<Role> result = scenarioService.getRoles(1L, "valid-token");

        assertEquals(1, result.size());
        assertEquals(role.getId(), result.get(0).getId());
    }

    @Test
    public void getRolesPerCabinet_validIds_returnsMatchingCabinetRoles() {
        Role roleA = new Role();
        roleA.setId(10L);
        roleA.setAssignedCabinet(1L);

        Role roleB = new Role();
        roleB.setId(11L);
        roleB.setAssignedCabinet(2L);

        List<Player> players = new ArrayList<>();
        players.add(roleA);
        players.add(roleB);
        testScenario.setPlayers(players);

        Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));

        List<Role> result = scenarioService.getRolesPerCabinet(1L, 1L, "valid-token");

        assertEquals(1, result.size());
        assertEquals(roleA.getId(), result.get(0).getId());
    }

    @Test
    public void updateMastodonConfig_validInput_success() {
        Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));

        scenarioService.updateMastodonConfig(1L, "valid-token", testMastodonDTO);

        assertEquals("https://mastodon.example.com", testScenario.getMastodonBaseUrl());
        assertEquals("mastodon-token-123", testScenario.getMastodonAccessToken());
        Mockito.verify(scenarioRepository, Mockito.times(1)).save(testScenario);
    }

    @Test
    public void updateMastodonConfig_scenarioNotFound_throwsException() {
        Mockito.when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> scenarioService.updateMastodonConfig(999L, "valid-token", testMastodonDTO));
    }
}
