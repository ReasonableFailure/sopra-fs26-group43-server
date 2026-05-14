package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.BackroomerJoinPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for the PlayerController endpoints introduced by the
 * "limit backroomers + join code" feature.
 */
@WebMvcTest(PlayerController.class)
public class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    private ObjectMapper objectMapper;
    private BackroomerJoinPostDTO joinDTO;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        joinDTO = new BackroomerJoinPostDTO();
        joinDTO.setCode("SAFE-42");
    }

    @Test
    public void joinBackroom_validCodeAndCapacity_returns201AndPrefixedToken() throws Exception {
        Backroomer issued = new Backroomer();
        issued.setId(7L);
        issued.setToken("token-xyz");
        given(playerService.joinBackroom(anyString(), anyLong(), anyString()))
                .willReturn(issued);

        mockMvc.perform(post("/scenarios/1/backroomers")
                        .header("Authorization", "Bearer userToken123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.authToken", is("Backroomer token-xyz")));
    }

    @Test
    public void joinBackroom_wrongCode_returns403() throws Exception {
        given(playerService.joinBackroom(anyString(), anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Incorrect backroomer code"));

        mockMvc.perform(post("/scenarios/1/backroomers")
                        .header("Authorization", "Bearer userToken123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void joinBackroom_backroomFull_returns409() throws Exception {
        given(playerService.joinBackroom(anyString(), anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "The backroom is full"));

        mockMvc.perform(post("/scenarios/1/backroomers")
                        .header("Authorization", "Bearer userToken123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void joinBackroom_missingBearerPrefix_returns401() throws Exception {
        mockMvc.perform(post("/scenarios/1/backroomers")
                        .header("Authorization", "Director userToken123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinDTO)))
                .andExpect(status().isUnauthorized());
    }
}
