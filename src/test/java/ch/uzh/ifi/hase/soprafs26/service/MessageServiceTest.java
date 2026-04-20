package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePairDTO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MessageServiceTest {

	@Mock
	private MessageRepository messageRepository;

	@Mock
	private ScenarioRepository scenarioRepository;

	@Mock
	private RoleRepository roleRepository;

	@InjectMocks
	private MessageService messageService;

	private Message testMessage;
	private MessagePostDTO testPostDTO;
	private MessagePutDTO testPutDTO;
	private Role testCreator;
	private Role testRecipient;
	private Scenario testScenario;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		testCreator = new Role();
		testCreator.setId(1L);

		testRecipient = new Role();
		testRecipient.setId(2L);

		testScenario = new Scenario();
		testScenario.setId(1L);
		testScenario.setHistory(new ArrayList<>());

		testMessage = new Message();
		testMessage.setId(1L);
		testMessage.setTitle("Test Message");
		testMessage.setBody("Test Body");
		testMessage.setCreatedAt(Instant.now());
		testMessage.setStatus(CommsStatus.PENDING);
		testMessage.setCreator(testCreator);
		testMessage.setRecipient(testRecipient);
		testMessage.setScenario(testScenario);

		testPostDTO = new MessagePostDTO();
		testPostDTO.setTitle("Test Message");
		testPostDTO.setBody("Test Body");
		testPostDTO.setCreatorId(1L);
		testPostDTO.setRecipientId(2L);
		testPostDTO.setScenarioId(1L);

		testPutDTO = new MessagePutDTO();
		testPutDTO.setStatus(CommsStatus.ACCEPTED);

		Mockito.when(messageRepository.save(Mockito.any())).thenReturn(testMessage);
		Mockito.when(scenarioRepository.save(Mockito.any())).thenReturn(testScenario);
	}

	@Test
	public void createMessage_validInputs_success() {
		Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
		Mockito.when(roleRepository.findById(1L)).thenReturn(Optional.of(testCreator));
		Mockito.when(roleRepository.findById(2L)).thenReturn(Optional.of(testRecipient));

		Message createdMessage = messageService.createMessage(testPostDTO);

		Mockito.verify(messageRepository, Mockito.times(1)).save(Mockito.any());
		Mockito.verify(scenarioRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testMessage.getId(), createdMessage.getId());
		assertEquals(testMessage.getTitle(), createdMessage.getTitle());
		assertEquals(testMessage.getBody(), createdMessage.getBody());
		assertEquals(CommsStatus.PENDING, createdMessage.getStatus());
		assertEquals(testCreator, createdMessage.getCreator());
		assertEquals(testRecipient, createdMessage.getRecipient());
		assertEquals(testScenario, createdMessage.getScenario());
	}

	@Test
	public void createMessage_invalidInput_throwsException() {
		testPostDTO.setCreatorId(null);

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(testPostDTO));
	}

	@Test
	public void createMessage_scenarioNotFound_throwsException() {
		Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(testPostDTO));
	}

	@Test
	public void getMessageById_validId_success() {
		Mockito.when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

		Message retrievedMessage = messageService.getMessageById(1L);

		assertEquals(testMessage, retrievedMessage);
	}

	@Test
	public void getMessageById_messageNotFound_throwsException() {
		Mockito.when(messageRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> messageService.getMessageById(1L));
	}

	@Test
	public void updateMessageStatus_validInputs_success() {
		Mockito.when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

		messageService.updateMessageStatus(1L, testPutDTO);

		Mockito.verify(messageRepository, Mockito.times(1)).save(testMessage);
		assertEquals(CommsStatus.ACCEPTED, testMessage.getStatus());
	}

	@Test
	public void updateMessageStatus_invalidInput_throwsException() {
		testPutDTO.setStatus(null);

		assertThrows(ResponseStatusException.class, () -> messageService.updateMessageStatus(1L, testPutDTO));
	}

	@Test
	public void updateMessageStatus_messageNotFound_throwsException() {
		Mockito.when(messageRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> messageService.updateMessageStatus(1L, testPutDTO));
	}

	@Test
	public void getMessagesBetween_validIds_success() {
		List<Message> messages = new ArrayList<>();
		messages.add(testMessage);

		Mockito.when(roleRepository.existsById(1L)).thenReturn(true);
		Mockito.when(roleRepository.existsById(2L)).thenReturn(true);
		Mockito.when(messageRepository.findConversation(1L, 2L)).thenReturn(messages);

		List<Message> result = messageService.getMessagesBetween(1L, 2L);

		assertEquals(messages, result);
	}

	@Test
	public void getMessagesBetween_charactersNotFound_throwsException() {
		Mockito.when(roleRepository.existsById(1L)).thenReturn(false);

		assertThrows(ResponseStatusException.class, () -> messageService.getMessagesBetween(1L, 2L));
	}

	@Test
	public void getMessagePairsByScenario_validId_success() {
		List<Message> messages = new ArrayList<>();
		messages.add(testMessage);

		Mockito.when(scenarioRepository.existsById(1L)).thenReturn(true);
		Mockito.when(messageRepository.findByScenarioId(1L)).thenReturn(messages);

		List<MessagePairDTO> result = messageService.getMessagePairsByScenario(1L);

		assertEquals(1, result.size());
		MessagePairDTO pair = result.get(0);
		assertEquals(1L, pair.getRoleAId());
		assertEquals(2L, pair.getRoleBId());
	}

	@Test
	public void getMessagePairsByScenario_scenarioNotFound_throwsException() {
		Mockito.when(scenarioRepository.existsById(1L)).thenReturn(false);

		assertThrows(ResponseStatusException.class, () -> messageService.getMessagePairsByScenario(1L));
	}
}
