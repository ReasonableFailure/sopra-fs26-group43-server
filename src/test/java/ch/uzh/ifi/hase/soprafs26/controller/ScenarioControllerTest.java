package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.ScenarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScenarioController.class)
public class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScenarioService scenarioService;

    private ObjectMapper objectMapper;
    private Scenario testScenario;
    private Role testRole;
    private ScenarioPostDTO scenarioPostDTO;
    private ScenarioPutDTO scenarioPutDTO;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();

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
        testScenario.setDirector(null);

        testRole = new Role();
        testRole.setId(10L);
        testRole.setName("Test Role");
        testRole.setTitle("Test Role Title");
        testRole.setDescription("Test Role Description");
        testRole.setSecret("secret");
        testRole.setAlive(true);
        testRole.setMessageCount(15);
        testRole.setActionPoints(0);

        scenarioPostDTO = new ScenarioPostDTO();
        scenarioPostDTO.setTitle("Test Scenario");
        scenarioPostDTO.setExchangeRate(5);
        scenarioPostDTO.setStartingMessageCount(15);

        scenarioPutDTO = new ScenarioPutDTO();
        scenarioPutDTO.setTitle("Updated Title");
        scenarioPutDTO.setActive(false);
        scenarioPutDTO.setDayNumber(2);
        scenarioPutDTO.setExchangeRate(10);
    }

    @Test
    public void getAllScenarios_validToken_returnsScenarios() throws Exception {
        given(scenarioService.getScenarios(anyString())).willReturn(List.of(testScenario));

        MockHttpServletRequestBuilder getRequest = get("/scenarios")
                .header("Authorization", "valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Scenario")))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[0].dayNumber", is(1)))
                .andExpect(jsonPath("$[0].exchangeRate", is(5)));

        verify(scenarioService, times(1)).getScenarios(anyString());
    }

    @Test
    public void getAllScenarios_emptyList_returnsEmptyArray() throws Exception {
        given(scenarioService.getScenarios(anyString())).willReturn(Collections.emptyList());

        MockHttpServletRequestBuilder getRequest = get("/scenarios")
                .header("Authorization", "valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(scenarioService, times(1)).getScenarios(anyString());
    }

    @Test
    public void getScenarioById_validId_success() throws Exception {
        given(scenarioService.getScenarioById(anyString(), eq(1L))).willReturn(testScenario);

        MockHttpServletRequestBuilder getRequest = get("/scenarios/1")
                .header("Authorization", "valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Scenario")))
                .andExpect(jsonPath("$.exchangeRate", is(5)))
                .andExpect(jsonPath("$.startingMessageCount", is(15)));

        verify(scenarioService, times(1)).getScenarioById(anyString(), eq(1L));
    }

    @Test
    public void getScenarioById_scenarioNotFound_throwsException() throws Exception {
        given(scenarioService.getScenarioById(anyString(), eq(999L)))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));

        MockHttpServletRequestBuilder getRequest = get("/scenarios/999")
                .header("Authorization", "valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(scenarioService, times(1)).getScenarioById(anyString(), eq(999L));
    }

    @Test
    public void createScenario_validInput_success() throws Exception {
        given(scenarioService.createScenario(anyString(), any(ScenarioPostDTO.class)))
                .willReturn(testScenario);

        MockHttpServletRequestBuilder postRequest = post("/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "valid-token")
                .content(asJsonString(scenarioPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Scenario")))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.exchangeRate", is(5)))
                .andExpect(jsonPath("$.startingMessageCount", is(15)));

        verify(scenarioService, times(1)).createScenario(anyString(), any(ScenarioPostDTO.class));
    }

    @Test
    public void updateScenario_validInput_success() throws Exception {
        doNothing().when(scenarioService).updateScenario(anyString(), eq(1L), any(ScenarioPutDTO.class));

        MockHttpServletRequestBuilder putRequest = put("/scenarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "valid-token")
                .content(asJsonString(scenarioPutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isOk());

        verify(scenarioService, times(1)).updateScenario(anyString(), eq(1L), any(ScenarioPutDTO.class));
    }

    @Test
    public void updateScenario_scenarioNotFound_throwsException() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"))
                .when(scenarioService).updateScenario(anyString(), eq(999L), any(ScenarioPutDTO.class));

        MockHttpServletRequestBuilder putRequest = put("/scenarios/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "valid-token")
                .content(asJsonString(scenarioPutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());

        verify(scenarioService, times(1)).updateScenario(anyString(), eq(999L), any(ScenarioPutDTO.class));
    }

    @Test
    public void deleteScenario_validInput_success() throws Exception {
        doNothing().when(scenarioService).deleteScenario(anyString(), eq(1L));

        MockHttpServletRequestBuilder deleteRequest = delete("/scenarios/1")
                .header("Authorization", "valid-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isOk());

        verify(scenarioService, times(1)).deleteScenario(anyString(), eq(1L));
    }

    @Test
    public void deleteScenario_scenarioNotFound_throwsException() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"))
                .when(scenarioService).deleteScenario(anyString(), eq(999L));

        MockHttpServletRequestBuilder deleteRequest = delete("/scenarios/999")
                .header("Authorization", "valid-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound());

        verify(scenarioService, times(1)).deleteScenario(anyString(), eq(999L));
    }

    @Test
    public void retrieveAllRoles_validScenarioId_success() throws Exception {
        given(scenarioService.getRoles(eq(1L), anyString())).willReturn(List.of(testRole));

        MockHttpServletRequestBuilder getRequest = get("/characters/1")
                .header("Authorization", "valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].name", is("Test Role")));

        verify(scenarioService, times(1)).getRoles(eq(1L), anyString());
    }

    @Test
    public void updateMastodonConfig_validInput_success() throws Exception {
        doNothing().when(scenarioService).updateMastodonConfig(eq(1L), anyString(), any(ScenarioMastodonDTO.class));

        ScenarioMastodonDTO mastodonDTO = new ScenarioMastodonDTO();
        mastodonDTO.setMastodonBaseUrl("https://mastodon.example.com");
        mastodonDTO.setMastodonAccessToken("mastodon-token-abc");

        MockHttpServletRequestBuilder putRequest = put("/scenarios/1/mastodon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "valid-token")
                .content(asJsonString(mastodonDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());

        verify(scenarioService, times(1)).updateMastodonConfig(eq(1L), anyString(), any(ScenarioMastodonDTO.class));
    }

    private String asJsonString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
