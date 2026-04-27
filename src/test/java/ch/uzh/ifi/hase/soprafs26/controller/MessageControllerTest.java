package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePairDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePutDTO;
import ch.uzh.ifi.hase.soprafs26.service.MessageService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;

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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.Mockito;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private PlayerService playerService;

    private ObjectMapper objectMapper;
    private Message testMessage;
    private MessagePostDTO messagePostDTO;
    private MessagePutDTO messagePutDTO;
    private Role testCreator;
    private Role testRecipient;
    private Scenario testScenario;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();

        Mockito.lenient().doNothing().when(playerService).checkToken(anyString(), anyString());

        testCreator = new Role();
        testCreator.setId(1L);
        testCreator.setName("Creator");

        testRecipient = new Role();
        testRecipient.setId(2L);
        testRecipient.setName("Recipient");

        testScenario = new Scenario();
        testScenario.setId(1L);

        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setTitle("Test Message");
        testMessage.setBody("Test Body");
        testMessage.setCreatedAt(Instant.now());
        testMessage.setStatus(CommsStatus.PENDING);
        testMessage.setCreator(testCreator);
        testMessage.setRecipient(testRecipient);
        testMessage.setScenario(testScenario);

        messagePostDTO = new MessagePostDTO();
        messagePostDTO.setTitle("Test Message");
        messagePostDTO.setBody("Test Body");
        messagePostDTO.setCreatorId(1L);
        messagePostDTO.setRecipientId(2L);
        messagePostDTO.setScenarioId(1L);

        messagePutDTO = new MessagePutDTO();
        messagePutDTO.setStatus(CommsStatus.ACCEPTED);
    }

    @Test
    public void createMessage_validInput_messageCreated() throws Exception {
        given(messageService.createMessage(any(MessagePostDTO.class)))
                .willReturn(testMessage);

        MockHttpServletRequestBuilder postRequest = post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Role token123")
                .content(asJsonString(messagePostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Message")))
                .andExpect(jsonPath("$.body", is("Test Body")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.creatorId", is(1)))
                .andExpect(jsonPath("$.recipientId", is(2)));

        verify(messageService, times(1)).createMessage(any(MessagePostDTO.class));
    }

    @Test
    public void createMessage_invalidInput_throwsException() throws Exception {
        messagePostDTO.setCreatorId(null);
        given(messageService.createMessage(any(MessagePostDTO.class)))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender or recipient missing"));

        MockHttpServletRequestBuilder postRequest = post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Role token123")
                .content(asJsonString(messagePostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());

        verify(messageService, times(1)).createMessage(any(MessagePostDTO.class));
    }

    @Test
    public void createMessage_scenarioNotFound_throwsException() throws Exception {
        given(messageService.createMessage(any(MessagePostDTO.class)))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));

        MockHttpServletRequestBuilder postRequest = post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Role token123")
                .content(asJsonString(messagePostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).createMessage(any(MessagePostDTO.class));
    }

    @Test
    public void getMessageById_validId_messageReturned() throws Exception {
        given(messageService.getMessageById(1L))
                .willReturn(testMessage);

        MockHttpServletRequestBuilder getRequest = get("/messages/1")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Message")))
                .andExpect(jsonPath("$.body", is("Test Body")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.creatorId", is(1)))
                .andExpect(jsonPath("$.recipientId", is(2)));

        verify(messageService, times(1)).getMessageById(1L);
    }

    @Test
    public void getMessageById_messageNotFound_throwsException() throws Exception {
        given(messageService.getMessageById(999L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        MockHttpServletRequestBuilder getRequest = get("/messages/999")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).getMessageById(999L);
    }

    @Test
    public void updateMessage_validInput_success() throws Exception {
        doNothing().when(messageService).updateMessageStatus(anyLong(), any(MessagePutDTO.class));

        MockHttpServletRequestBuilder putRequest = put("/messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Backroomer token123")
                .content(asJsonString(messagePutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isOk());

        verify(messageService, times(1)).updateMessageStatus(anyLong(), any(MessagePutDTO.class));
    }

    @Test
    public void updateMessage_statusNull_throwsException() throws Exception {
        MessagePutDTO invalidPutDTO = new MessagePutDTO();
        invalidPutDTO.setStatus(null);

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must not be null"))
                .when(messageService).updateMessageStatus(anyLong(), any(MessagePutDTO.class));

        MockHttpServletRequestBuilder putRequest = put("/messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Backroomer token123")
                .content(asJsonString(invalidPutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isBadRequest());

        verify(messageService, times(1)).updateMessageStatus(anyLong(), any(MessagePutDTO.class));
    }

    @Test
    public void updateMessage_messageNotFound_throwsException() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"))
                .when(messageService).updateMessageStatus(anyLong(), any(MessagePutDTO.class));

        MockHttpServletRequestBuilder putRequest = put("/messages/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Backroomer token123")
                .content(asJsonString(messagePutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).updateMessageStatus(anyLong(), any(MessagePutDTO.class));
    }

    @Test
    public void getMessagesBetween_validIds_messagesReturned() throws Exception {
        given(messageService.getMessagesBetween(1L, 2L))
                .willReturn(List.of(testMessage));

        MockHttpServletRequestBuilder getRequest = get("/messages/between/1/2")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].creatorId", is(1)))
                .andExpect(jsonPath("$[0].recipientId", is(2)));

        verify(messageService, times(1)).getMessagesBetween(1L, 2L);
    }

    @Test
    public void getMessagesBetween_charactersNotFound_throwsException() throws Exception {
        given(messageService.getMessagesBetween(999L, 2L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "One or both characters not found"));

        MockHttpServletRequestBuilder getRequest = get("/messages/between/999/2")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).getMessagesBetween(999L, 2L);
    }

    @Test
    public void getMessagePairsByScenario_validId_pairsReturned() throws Exception {
        MessagePairDTO pairDTO = new MessagePairDTO();
        pairDTO.setRoleAId(1L);
        pairDTO.setRoleBId(2L);

        given(messageService.getMessagePairsByScenario(1L))
                .willReturn(List.of(pairDTO));

        MockHttpServletRequestBuilder getRequest = get("/messages/scenario/1/pairs")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roleAId", is(1)))
                .andExpect(jsonPath("$[0].roleBId", is(2)));

        verify(messageService, times(1)).getMessagePairsByScenario(1L);
    }

    @Test
    public void getMessagePairsByScenario_scenarioNotFound_throwsException() throws Exception {
        given(messageService.getMessagePairsByScenario(999L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));

        MockHttpServletRequestBuilder getRequest = get("/messages/scenario/999/pairs")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).getMessagePairsByScenario(999L);
    }

    private String asJsonString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}

