package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Directive;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.service.DirectiveService;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectiveGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePutDTO;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DirectiveController.class)
public class DirectiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DirectiveService directiveService;

    private ObjectMapper objectMapper;
    private Directive testDirective;
    private DirectivePostDTO directivePostDTO;
    private DirectivePutDTO directivePutDTO;
    private Role testRole;
    private Scenario testScenario;

    @BeforeEach
    public void setupTest() {
        objectMapper = new ObjectMapper();

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("Test Role");

        testScenario = new Scenario();
        testScenario.setId(1L);
        testScenario.setPlayers(new ArrayList<>());
        testScenario.getPlayers().add(testRole);

        testDirective = new Directive();
        testDirective.setId(1L);
        testDirective.setTitle("Test Directive");
        testDirective.setBody("Test Body");
        testDirective.setCreatedAt(Instant.now());
        testDirective.setStatus(CommsStatus.PENDING);
        testDirective.setCreator(testRole);
        testDirective.setScenario(testScenario);

        directivePostDTO = new DirectivePostDTO();
        directivePostDTO.setTitle("Test Directive");
        directivePostDTO.setBody("Test Body");
        directivePostDTO.setCreatorId(1L);
        directivePostDTO.setScenarioId(1L);

        directivePutDTO = new DirectivePutDTO();
        directivePutDTO.setStatus(CommsStatus.ACCEPTED);
        directivePutDTO.setResponse("Test Response");
    }

    @Test
    public void createDirective_validInput_directiveCreated() throws Exception {
        given(directiveService.createDirective(any(DirectivePostDTO.class)))
                .willReturn(testDirective);

        MockHttpServletRequestBuilder postRequest = post("/directives")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token123")
                .content(asJsonString(directivePostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Directive")))
                .andExpect(jsonPath("$.body", is("Test Body")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.creatorId", is(1)));

        verify(directiveService, times(1)).createDirective(any(DirectivePostDTO.class));
    }

    @Test
    public void createDirective_creatorIdNull_throwsException() throws Exception {
        directivePostDTO.setCreatorId(null);
        given(directiveService.createDirective(any(DirectivePostDTO.class)))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator ID missing"));

        MockHttpServletRequestBuilder postRequest = post("/directives")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token123")
                .content(asJsonString(directivePostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());

        verify(directiveService, times(1)).createDirective(any(DirectivePostDTO.class));
    }

    @Test
    public void createDirective_scenarioNotFound_throwsException() throws Exception {
        given(directiveService.createDirective(any(DirectivePostDTO.class)))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));

        MockHttpServletRequestBuilder postRequest = post("/directives")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token123")
                .content(asJsonString(directivePostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());

        verify(directiveService, times(1)).createDirective(any(DirectivePostDTO.class));
    }

    @Test
    public void createDirective_roleNotFound_throwsException() throws Exception {
        given(directiveService.createDirective(any(DirectivePostDTO.class)))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        MockHttpServletRequestBuilder postRequest = post("/directives")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token123")
                .content(asJsonString(directivePostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());

        verify(directiveService, times(1)).createDirective(any(DirectivePostDTO.class));
    }

    @Test
    public void createDirective_roleNotInScenario_throwsException() throws Exception {
        given(directiveService.createDirective(any(DirectivePostDTO.class)))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not part of scenario"));

        MockHttpServletRequestBuilder postRequest = post("/directives")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token123")
                .content(asJsonString(directivePostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());

        verify(directiveService, times(1)).createDirective(any(DirectivePostDTO.class));
    }

    @Test
    public void getDirective_validId_directiveReturned() throws Exception {
        given(directiveService.getDirectiveById(1L))
                .willReturn(testDirective);

        MockHttpServletRequestBuilder getRequest = get("/directives/1")
                .header("Authorization", "Bearer token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Directive")))
                .andExpect(jsonPath("$.body", is("Test Body")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.creatorId", is(1)));

        verify(directiveService, times(1)).getDirectiveById(1L);
    }

    @Test
    public void getDirective_directiveNotFound_throwsException() throws Exception {
        given(directiveService.getDirectiveById(999L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Directive not found"));

        MockHttpServletRequestBuilder getRequest = get("/directives/999")
                .header("Authorization", "Bearer token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(directiveService, times(1)).getDirectiveById(999L);
    }


    @Test
    public void updateDirective_validInput_success() throws Exception {
        doNothing().when(directiveService).updateDirectiveStatus(anyLong(), any(DirectivePutDTO.class));

        MockHttpServletRequestBuilder putRequest = put("/directives/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token123")
                .content(asJsonString(directivePutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());

        verify(directiveService, times(1)).updateDirectiveStatus(anyLong(), any(DirectivePutDTO.class));
    }

    @Test
    public void updateDirective_statusNull_throwsException() throws Exception {
        DirectivePutDTO putDTOWithNullStatus = new DirectivePutDTO();
        putDTOWithNullStatus.setStatus(null);
        putDTOWithNullStatus.setResponse("Test Response");
        
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must not be null"))
                .when(directiveService).updateDirectiveStatus(anyLong(), any(DirectivePutDTO.class));

        MockHttpServletRequestBuilder putRequest = put("/directives/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token123")
                .content(asJsonString(putDTOWithNullStatus));

        mockMvc.perform(putRequest)
                .andExpect(status().isBadRequest());

        verify(directiveService, times(1)).updateDirectiveStatus(anyLong(), any(DirectivePutDTO.class));
    }

    @Test
    public void updateDirective_directiveNotFound_throwsException() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Directive not found"))
                .when(directiveService).updateDirectiveStatus(anyLong(), any(DirectivePutDTO.class));

        MockHttpServletRequestBuilder putRequest = put("/directives/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token123")
                .content(asJsonString(directivePutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());

        verify(directiveService, times(1)).updateDirectiveStatus(anyLong(), any(DirectivePutDTO.class));
    }


    @Test
    public void getDirectivesByScenario_validId_directivesReturned() throws Exception {
        List<Directive> directives = new ArrayList<>();
        directives.add(testDirective);

        Role secondRole = new Role();
        secondRole.setId(2L);
        secondRole.setName("Second Role");
        testScenario.getPlayers().add(secondRole);

        Directive secondDirective = new Directive();
        secondDirective.setId(2L);
        secondDirective.setTitle("Second Directive");
        secondDirective.setBody("Second Body");
        secondDirective.setCreatedAt(Instant.now());
        secondDirective.setStatus(CommsStatus.ACCEPTED);
        secondDirective.setCreator(secondRole);
        secondDirective.setScenario(testScenario);
        directives.add(secondDirective);

        given(directiveService.getDirectivesByScenario(1L))
                .willReturn(directives);

        MockHttpServletRequestBuilder getRequest = get("/directives/scenario/1")
                .header("Authorization", "Bearer token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Directive")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Second Directive")));

        verify(directiveService, times(1)).getDirectivesByScenario(1L);
    }

    @Test
    public void getDirectivesByScenario_scenarioNotFound_throwsException() throws Exception {
        given(directiveService.getDirectivesByScenario(999L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));

        MockHttpServletRequestBuilder getRequest = get("/directives/scenario/999")
                .header("Authorization", "Bearer token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(directiveService, times(1)).getDirectivesByScenario(999L);
    }


    @Test
    public void getDirectivesByCharacter_validId_directivesReturned() throws Exception {
        List<Directive> directives = new ArrayList<>();
        directives.add(testDirective);

        given(directiveService.getDirectivesByCreator(1L))
                .willReturn(directives);

        MockHttpServletRequestBuilder getRequest = get("/directives/character/1")
                .header("Authorization", "Bearer token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Directive")))
                .andExpect(jsonPath("$[0].creatorId", is(1)));

        verify(directiveService, times(1)).getDirectivesByCreator(1L);
    }

    @Test
    public void getDirectivesByCharacter_characterNotFound_throwsException() throws Exception {
        given(directiveService.getDirectivesByCreator(999L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));

        MockHttpServletRequestBuilder getRequest = get("/directives/character/999")
                .header("Authorization", "Bearer token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(directiveService, times(1)).getDirectivesByCreator(999L);
    }


    private String asJsonString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
