package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePairDTO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

	@Mock private MessageRepository messageRepository;

	@Mock private ScenarioService scenarioService;

	@Mock private PlayerService playerService;

	@Mock private CommunicationStatsService communicationStatsService;

	@InjectMocks
	private MessageService messageService;

	private Message testMessage;
	private MessagePostDTO testPostDTO;
	private MessagePutDTO testPutDTO;
	private Role testCreator;
	private Role testRecipient;
	private Scenario testScenario;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		testCreator = new Role();
		testCreator.setMessageCount(5);
		testCreator.setId(1L);

		testRecipient = new Role();
		testRecipient.setId(2L);

		testScenario = new Scenario();
		testScenario.setId(1L);
		testScenario.setHistory(new ArrayList<>());

		Mockito.when(scenarioService.getScenarioById(1L)).thenReturn(testScenario);

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
	}

	@Test
	void createMessage_validInputs_success() {

		Mockito.when(scenarioService.getScenarioById(1L))
			.thenReturn(testScenario);

		Mockito.when(playerService.getRoleById(1L))
			.thenReturn(testCreator);

		Mockito.when(playerService.getRoleById(2L))
			.thenReturn(testRecipient);

		Message createdMessage = messageService.createMessage(testPostDTO);

		Mockito.verify(messageRepository, Mockito.times(1)).save(Mockito.any());
		Mockito.verify(scenarioService, Mockito.times(1))
			.addCommunicationToHistory(Mockito.eq(1L), Mockito.any(Message.class));

		assertEquals(testMessage.getId(), createdMessage.getId());
		assertEquals(testMessage.getTitle(), createdMessage.getTitle());
		assertEquals(testMessage.getBody(), createdMessage.getBody());
		assertEquals(CommsStatus.PENDING, createdMessage.getStatus());
		assertEquals(testCreator, createdMessage.getCreator());
		assertEquals(testRecipient, createdMessage.getRecipient());
		assertEquals(testScenario, createdMessage.getScenario());
	}

	@Test
	void createMessage_invalidInput_throwsException() {
		testPostDTO.setCreatorId(null);

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(testPostDTO));
	}

	@Test
	void createMessage_scenarioNotFound_throwsException() {
		Mockito.when(scenarioService.getScenarioById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(testPostDTO));
	}

	@Test
	void createMessage_scenarioCompleted_throwsException() {
		testScenario.setStatus(ScenarioStatus.COMPLETED);
		Mockito.when(scenarioService.getScenarioById(1L)).thenReturn(testScenario);

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(testPostDTO));
	}

	@Test
	void createMessage_sentToSelf_throwsException() {
		testPostDTO.setRecipientId(1L);
		Mockito.when(playerService.getRoleById(1L)).thenReturn(testCreator);

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(testPostDTO));
	}

	@Test
	void getMessagesBetween_AAndBAreTheSame_throwsException() {
		Mockito.when(playerService.getRoleById(1L)).thenReturn(testCreator);

		assertThrows(ResponseStatusException.class, () -> messageService.getMessagesBetween(1L, 1L));
	}

	@Test
	void getCharacterInbox_validInput_Success() {
		Mockito.when(playerService.getRoleById(2L)).thenReturn(testRecipient);
		Mockito.when(scenarioService.getScenarioById(1L)).thenReturn(testScenario);
		Mockito.when(messageRepository.findByRecipientIdAndScenarioId(2L, 1L))
				.thenReturn(List.of(testMessage));

		List<Message> inbox = messageService.getInbox(2L, 1L);

		assertEquals(1, inbox.size());
		assertEquals(testMessage, inbox.get(0));
	}

	@Test
	void getCharacterInbox_roleNotFound_throwsException() {
		Mockito.when(playerService.getRoleById(2L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		assertThrows(ResponseStatusException.class, () -> messageService.getInbox(2L, 1L));
	}

	@Test
	void getCharacterInbox_ScenarioNotFound_throwsException() {
		Mockito.when(playerService.getRoleById(2L)).thenReturn(testRecipient);
		Mockito.when(scenarioService.getScenarioById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		assertThrows(ResponseStatusException.class, () -> messageService.getInbox(2L, 1L));
	}

	@Test
	void deleteMessage_messageNotFound_throwsException() {
		Mockito.when(messageRepository.existsById(1L)).thenReturn(false);

		assertThrows(ResponseStatusException.class, () -> messageService.deleteMessage(1L));
	}

	@Test
	void getMessageById_validId_success() {
		Mockito.when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

		Message retrievedMessage = messageService.getMessageById(1L);

		assertEquals(testMessage, retrievedMessage);
	}

	@Test
	void getMessageById_messageNotFound_throwsException() {
		Mockito.when(messageRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> messageService.getMessageById(1L));
	}

	@Test
	void updateMessageStatus_validInputs_success() {
		Mockito.when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

		messageService.updateMessageStatus(1L, testPutDTO);

		Mockito.verify(messageRepository, Mockito.times(1)).save(testMessage);
		assertEquals(CommsStatus.ACCEPTED, testMessage.getStatus());
	}

	@Test
	void updateMessageStatus_invalidInput_throwsException() {
		testPutDTO.setStatus(null);

		assertThrows(ResponseStatusException.class, () -> messageService.updateMessageStatus(1L, testPutDTO));
	}

	@Test
	void updateMessageStatus_messageNotFound_throwsException() {
		Mockito.when(messageRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> messageService.updateMessageStatus(1L, testPutDTO));
	}

	@Test
	void getMessagesBetween_validIds_success() {
		List<Message> messages = new ArrayList<>();
		messages.add(testMessage);

		Mockito.when(playerService.getRoleById(1L)).thenReturn(testCreator);
		Mockito.when(playerService.getRoleById(2L)).thenReturn(testRecipient);
		Mockito.when(messageRepository.findConversation(1L, 2L)).thenReturn(messages);

		List<Message> result = messageService.getMessagesBetween(1L, 2L);

		assertEquals(messages, result);
	}

	@Test
	void getMessagesBetween_charactersNotFound_throwsException() {
		Mockito.when(playerService.getRoleById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		assertThrows(ResponseStatusException.class, () -> messageService.getMessagesBetween(1L, 2L));
	}

	@Test
	void getMessagePairsByScenario_validId_success() {
		List<Message> messages = new ArrayList<>();
		messages.add(testMessage);

		Mockito.when(scenarioService.getScenarioById(1L)).thenReturn(testScenario);
		Mockito.when(messageRepository.findByScenarioId(1L)).thenReturn(messages);

		List<MessagePairDTO> result = messageService.getMessagePairsByScenario(1L);

		assertEquals(1, result.size());
		MessagePairDTO pair = result.get(0);
		assertEquals(1L, pair.getRoleAId());
		assertEquals(2L, pair.getRoleBId());
	}

	@Test
	void getMessagePairsByScenario_scenarioNotFound_throwsException() {
		Mockito.when(scenarioService.getScenarioById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		assertThrows(ResponseStatusException.class, () -> messageService.getMessagePairsByScenario(1L));
	}
}
