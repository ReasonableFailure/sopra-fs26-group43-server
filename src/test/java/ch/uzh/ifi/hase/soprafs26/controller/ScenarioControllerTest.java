package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.NewsService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.service.ScenarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScenarioController.class)
class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScenarioService scenarioService;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private NewsService newsService;

    private ObjectMapper objectMapper;
    private Scenario testScenario;
    private Director testDirector;
    private Role testRole;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        when(playerService.validate(anyString(), anyString())).thenReturn("token123");

        testDirector = new Director();
        testDirector.setId(1L);
        testDirector.setToken("token123");

        testScenario = new Scenario();
        testScenario.setId(1L);
        testScenario.setTitle("Test Scenario");
        testScenario.setDescription("Test description");
        testScenario.setStatus(ScenarioStatus.UNSTARTED);
        testScenario.setDayNumber(0);
        testScenario.setExchangeRate(10);
        testScenario.setStartingMessageCount(5);
        testScenario.setPlayers(new ArrayList<>());
        testScenario.setDirector(testDirector);
        testScenario.getPlayers().add(testDirector);
        testDirector.setScenario(testScenario);

        when(playerService.getDirectorByToken("token123")).thenReturn(testDirector);
    }

    @Test
    void getAllScenarios_validInput_success() throws Exception {
        when(scenarioService.getScenarios()).thenReturn(Collections.singletonList(testScenario));

        mockMvc.perform(get("/scenarios")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Scenario")))
                .andExpect(jsonPath("$[0].description", is("Test description")))
                .andExpect(jsonPath("$[0].dayNumber", is(0)))
                .andExpect(jsonPath("$[0].exchangeRate", is(10)));

        verify(scenarioService, times(1)).getScenarios();
    }

    @Test
    void createScenario_validInput_success() throws Exception {
        ScenarioPostDTO postDTO = new ScenarioPostDTO();
        postDTO.setTitle("Test Scenario");
        postDTO.setDescription("Test description");
        postDTO.setExchangeRate(10);
        postDTO.setStartingMessageCount(5);
        postDTO.setDirector(1L);

        when(scenarioService.createScenario(any(ScenarioPostDTO.class))).thenReturn(testScenario);

        mockMvc.perform(post("/scenarios")
                .header("Authorization", "Director token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Scenario")))
                .andExpect(jsonPath("$.description", is("Test description")))
                .andExpect(jsonPath("$.dayNumber", is(0)))
                .andExpect(jsonPath("$.exchangeRate", is(10)));

        verify(scenarioService, times(1)).createScenario(any(ScenarioPostDTO.class));
    }

    @Test
    void getScenarioById_validInput_success() throws Exception {
        when(scenarioService.getScenarioById(1L)).thenReturn(testScenario);

        mockMvc.perform(get("/scenarios/1")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Scenario")))
                .andExpect(jsonPath("$.description", is("Test description")));

        verify(scenarioService, times(1)).getScenarioById(1L);
    }

    @Test
    void updateScenario_validInput_success() throws Exception {
        ScenarioPutDTO putDTO = new ScenarioPutDTO();
        putDTO.setTitle("Updated Scenario");
        putDTO.setDescription("Updated description");

        when(scenarioService.getScenarioById(1L)).thenReturn(testScenario);
        doNothing().when(scenarioService).updateScenario(eq(1L), any(ScenarioPutDTO.class));

        mockMvc.perform(put("/scenarios/1")
                .header("Authorization", "Director token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk());

        verify(scenarioService, times(1)).updateScenario(eq(1L), any(ScenarioPutDTO.class));
    }

    @Test
    void deleteScenario_validInput_success() throws Exception {
        when(scenarioService.getScenarioById(1L)).thenReturn(testScenario);
        doNothing().when(scenarioService).deleteScenario(1L);

        mockMvc.perform(delete("/scenarios/1")
                .header("Authorization", "Director token123"))
                .andExpect(status().isOk());

        verify(scenarioService, times(1)).deleteScenario(1L);
    }

    @Test
    void retrieveAllRoles_validInput_success() throws Exception {
        testRole = new Role();
        testRole.setId(2L);
        testRole.setName("Test Role");

        when(scenarioService.getRoles(1L)).thenReturn(Collections.singletonList(testRole));

        mockMvc.perform(get("/characters/scenario/1")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("Test Role")));

        verify(scenarioService, times(1)).getRoles(1L);
    }

    @Test
    void updateMastodonConfig_validInput_success() throws Exception {
        ScenarioMastodonDTO mastodonDTO = new ScenarioMastodonDTO();
        mastodonDTO.setMastodonBaseUrl("https://mastodon.example");
        mastodonDTO.setMastodonAccessToken("token123");

        when(scenarioService.getScenarioById(1L)).thenReturn(testScenario);
        doNothing().when(scenarioService).updateMastodonConfig(eq(1L), any(ScenarioMastodonDTO.class));
        when(newsService.getNewsByScenario(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(put("/scenarios/1/mastodon")
                .header("Authorization", "Director token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mastodonDTO)))
                .andExpect(status().isNoContent());

        verify(scenarioService, times(1)).updateMastodonConfig(eq(1L), any(ScenarioMastodonDTO.class));
        verify(newsService, times(1)).getNewsByScenario(1L);
    }
}
