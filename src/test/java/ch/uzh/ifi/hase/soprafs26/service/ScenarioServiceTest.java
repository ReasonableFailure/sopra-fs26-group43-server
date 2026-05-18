package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import ch.uzh.ifi.hase.soprafs26.repository.DirectiveRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs26.repository.NewsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceTest {

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private UserService userService;

    @Mock
    private PlayerService playerService;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private DirectiveRepository directiveRepository;

    @Mock
    private MastodonClient mastodonClient;

    @InjectMocks
    private ScenarioService scenarioService;

    private Scenario existingScenario;
    private Director director;

    @BeforeEach
    void setup() {
        director = new Director();
        director.setId(1L);
        director.setToken("director-token");

        existingScenario = new Scenario();
        existingScenario.setId(1L);
        existingScenario.setTitle("Existing Scenario");
        existingScenario.setDescription("Existing description");
        existingScenario.setStatus(ScenarioStatus.UNSTARTED);
        existingScenario.setDayNumber(0);
        existingScenario.setExchangeRate(5);
        existingScenario.setStartingMessageCount(10);
        existingScenario.setPlayers(new ArrayList<>());
        existingScenario.setHistory(new ArrayList<>());
        existingScenario.setDirector(director);
        existingScenario.getPlayers().add(director);
        director.setScenario(existingScenario);
    }

    @Test
    void getAllScenarios_validInput_success() {
        when(scenarioRepository.findAll()).thenReturn(Collections.singletonList(existingScenario));

        List<Scenario> result = scenarioService.getScenarios();

        assertEquals(1, result.size());
        assertEquals(existingScenario, result.get(0));
    }

    @Test
    void createScenario_validInput_success() {
        ScenarioPostDTO postDTO = new ScenarioPostDTO();
        postDTO.setTitle("New Scenario");
        postDTO.setDescription("New description");
        postDTO.setExchangeRate(8);
        postDTO.setStartingMessageCount(12);
        postDTO.setDirector(1L);

        Director createdDirector = new Director();
        createdDirector.setId(1L);
        createdDirector.setToken("director-token");

        when(playerService.getDirectorByID(1L)).thenReturn(createdDirector);
        when(scenarioRepository.save(any(Scenario.class))).thenAnswer(invocation -> {
            Scenario scenario = invocation.getArgument(0);
            scenario.setId(2L);
            return scenario;
        });

        Scenario created = scenarioService.createScenario(postDTO);

        assertNotNull(created);
        assertEquals(2L, created.getId());
        assertEquals("New Scenario", created.getTitle());
        assertEquals("New description", created.getDescription());
        assertEquals(ScenarioStatus.UNSTARTED, created.getStatus());
        assertEquals(8, created.getExchangeRate());
        assertEquals(12, created.getStartingMessageCount());
        assertEquals(createdDirector, created.getDirector());
    }

    @Test
    void getScenarioById_validInput_success() {
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(existingScenario));

        Scenario result = scenarioService.getScenarioById(1L);

        assertEquals(existingScenario, result);
    }

    @Test
    void getScenarioById_scenarioNotFound_throwsException() {
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> scenarioService.getScenarioById(999L));
    }

    @Test
    void updateScenario_validInput_success() {
        ScenarioPutDTO putDTO = new ScenarioPutDTO();
        putDTO.setTitle("Updated Scenario");
        putDTO.setDescription("Updated description");
        putDTO.setStatus(ScenarioStatus.COMPLETED);
        putDTO.setDayNumber(7);
        putDTO.setExchangeRate(15);
        putDTO.setStartingMessageCount(20);

        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(existingScenario));

        scenarioService.updateScenario(1L, putDTO);

        verify(scenarioRepository, times(1)).save(existingScenario);
        assertEquals("Updated Scenario", existingScenario.getTitle());
        assertEquals("Updated description", existingScenario.getDescription());
        assertEquals(ScenarioStatus.COMPLETED, existingScenario.getStatus());
        assertEquals(7, existingScenario.getDayNumber());
        assertEquals(15, existingScenario.getExchangeRate());
        assertEquals(20, existingScenario.getStartingMessageCount());
    }

    @Test
    void addCommunicationToHistory_validInput_success() {
        Message message = new Message();
        message.setTitle("History item");
        message.setBody("History body");
        message.setCreatedAt(Instant.now());
        message.setDayNumber(0);
        message.setScenario(existingScenario);

        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(existingScenario));

        scenarioService.addCommunicationToHistory(1L, message);

        verify(scenarioRepository, times(1)).save(existingScenario);
        assertTrue(existingScenario.getHistory().contains(message));
    }

    @Test
    void addCommunicationToHistory_scenarioNotFound_throwsException() {
        Message message = new Message();
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> scenarioService.addCommunicationToHistory(999L, message));
    }

    @Test
    void deleteScenario_validInput_success() {
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(existingScenario));
        when(messageRepository.findByScenarioId(1L)).thenReturn(Collections.emptyList());
        when(directiveRepository.findByScenarioId(1L)).thenReturn(Collections.emptyList());
        when(newsRepository.findByScenarioIdOrderByCreatedAtAsc(1L)).thenReturn(Collections.emptyList());

        scenarioService.deleteScenario(1L);

        verify(messageRepository, times(1)).deleteAll(Collections.emptyList());
        verify(directiveRepository, times(1)).deleteAll(Collections.emptyList());
        verify(newsRepository, times(1)).deleteAll(Collections.emptyList());
        verify(scenarioRepository, times(1)).delete(existingScenario);
    }

    @Test
    void deleteScenario_scenarioNotFound_throwsException() {
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> scenarioService.deleteScenario(999L));
    }

    @Test
    void getRoles_validInput_success() {
        Role role = new Role();
        role.setId(2L);
        role.setToken("role-token");
        existingScenario.getPlayers().add(role);
        role.setScenario(existingScenario);

        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(existingScenario));

        List<Role> roles = scenarioService.getRoles(1L);

        assertEquals(1, roles.size());
        assertEquals(role, roles.get(0));
    }

    @Test
    void getRoles_scenarioNotFound_throwsException() {
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> scenarioService.getRoles(999L));
    }

    @Test
    void updateMastodonConfig_validInput_success() {
        ScenarioMastodonDTO dto = new ScenarioMastodonDTO();
        dto.setMastodonBaseUrl("https://mastodon.example");
        dto.setMastodonAccessToken("token-abc");

        NewsStory newsStory = new NewsStory();
        newsStory.setId(10L);
        newsStory.setTitle("News Title");
        newsStory.setBody("News Body");
        newsStory.setCreatedAt(Instant.now());
        newsStory.setDayNumber(0);
        newsStory.setScenario(existingScenario);

        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(existingScenario));
        when(newsRepository.findByScenarioIdOrderByCreatedAtAsc(1L)).thenReturn(Collections.singletonList(newsStory));
        when(mastodonClient.fetchMastodonProfileUrl(dto.getMastodonBaseUrl(), dto.getMastodonAccessToken())).thenReturn("https://mastodon.example/@director");
        when(mastodonClient.postStatus(dto.getMastodonBaseUrl(), dto.getMastodonAccessToken(), newsStory.formatSelf())).thenReturn("mastodon-123");

        scenarioService.updateMastodonConfig(1L, dto);

        assertEquals("https://mastodon.example", existingScenario.getMastodonBaseUrl());
        assertEquals("token-abc", existingScenario.getMastodonAccessToken());
        assertEquals("https://mastodon.example/@director", existingScenario.getMastodonProfileUrl());
        verify(scenarioRepository, times(1)).save(existingScenario);
        verify(mastodonClient, times(1)).postStatus(dto.getMastodonBaseUrl(), dto.getMastodonAccessToken(), newsStory.formatSelf());
    }

    @Test
    void updateMastodonConfig_scenarioNotFound_throwsException() {
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        ScenarioMastodonDTO dto = new ScenarioMastodonDTO();
        dto.setMastodonBaseUrl("https://mastodon.example");
        dto.setMastodonAccessToken("token-abc");

        assertThrows(ResponseStatusException.class, () -> scenarioService.updateMastodonConfig(999L, dto));
    }
}
