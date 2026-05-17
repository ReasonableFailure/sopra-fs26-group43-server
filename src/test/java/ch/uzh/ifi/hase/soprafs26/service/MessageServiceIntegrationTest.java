package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;

import java.util.Optional;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePairDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
public class MessageServiceIntegrationTest {

	@Qualifier("messageRepository")
	@Autowired
	private MessageRepository messageRepository;

	@Qualifier("scenarioRepository")
	@Autowired
	private ScenarioRepository scenarioRepository;

	@Qualifier("roleRepository")
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private MessageService messageService;

	/** Bypass requester-role lookup so these tests stay focused on persistence + filtering. */
	@MockitoBean
	private PlayerRepository playerRepository;

	private Scenario testScenario;
	private Role testCreator;
	private Role testRecipient;

	@BeforeEach
	public void setup() {
		messageRepository.deleteAll();
		scenarioRepository.deleteAll();
		roleRepository.deleteAll();

		testCreator = new Role();
		testCreator.setToken("test-creator-token");
		testCreator.setName("Creator");
		testCreator.setTitle("Creator Title");
		testCreator.setDescription("Creator description");
		testCreator.setSecret("creator-secret");
		testCreator.setAlive(true);
		testCreator.setMessageCount(5);
		testCreator.setTotalPoints(10);
		testCreator.setPointsBalance(10);

		testRecipient = new Role();
		testRecipient.setToken("test-recipient-token");
		testRecipient.setName("Recipient");
		testRecipient.setTitle("Recipient Title");
		testRecipient.setDescription("Recipient description");
		testRecipient.setSecret("recipient-secret");
		testRecipient.setAlive(true);
		testRecipient.setMessageCount(5);
		testRecipient.setTotalPoints(10);
		testRecipient.setPointsBalance(10);

		testScenario = new Scenario();
		testScenario.setTitle("Test Scenario");
		testScenario.setDescription("Test scenario description");
		testScenario.setStatus(ScenarioStatus.UNSTARTED);
		testScenario.setDayNumber(0);
		testScenario.setExchangeRate(1);
		testScenario.setPlayers(new ArrayList<>());
		testScenario.setHistory(new ArrayList<>());
		testScenario.getPlayers().add(testCreator);
		testScenario.getPlayers().add(testRecipient);
		// Set the inverse FK explicitly — mappedBy means JPA won't infer it.
		testCreator.setScenario(testScenario);
		testRecipient.setScenario(testScenario);

		testScenario = scenarioRepository.save(testScenario);
		testCreator = (Role) testScenario.getPlayers().get(0);
		testRecipient = (Role) testScenario.getPlayers().get(1);

		// Backroomer requesters see everything — matches the read-path branch
		// that returns all messages regardless of status.
		Mockito.when(playerRepository.findByToken(Mockito.anyString()))
				.thenReturn(Optional.of(new Backroomer()));
	}

	@Test
	public void createMessage_validInputs_success() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		Message createdMessage = messageService.createMessage(postDTO);

		assertNotNull(createdMessage.getId());
		assertEquals(postDTO.getTitle(), createdMessage.getTitle());
		assertEquals(postDTO.getBody(), createdMessage.getBody());
		assertEquals(CommsStatus.PENDING, createdMessage.getStatus());
		assertEquals(testCreator.getId(), createdMessage.getCreator().getId());
		assertEquals(testRecipient.getId(), createdMessage.getRecipient().getId());
		assertEquals(testScenario.getId(), createdMessage.getScenario().getId());
		assertNotNull(createdMessage.getCreatedAt());
	}

	@Test
	public void createMessage_invalidInput_throwsException() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(null);
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(postDTO));
	}

	@Test
	public void createMessage_scenarioNotFound_throwsException() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(999L);

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(postDTO));
	}

	@Test
	public void createMessage_scenarioCompleted_throwsException() {
		testScenario.setStatus(ScenarioStatus.COMPLETED);
		scenarioRepository.save(testScenario);

		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(postDTO));
	}

	@Test
	public void createMessage_sentToSelf_throwsException() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testCreator.getId());
		postDTO.setScenarioId(testScenario.getId());

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(postDTO));
	}

	@Test
	public void getMessagesBetween_AAndBAreTheSame_throwsException() {
		assertThrows(ResponseStatusException.class, () -> messageService.getMessagesBetween(testCreator.getId(), testCreator.getId()));
	}

	@Test
	public void getCharacterInbox_validInput_Success() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		Message createdMessage = messageService.createMessage(postDTO);

		List<Message> inbox = messageService.getInbox(testRecipient.getId(), testScenario.getId());

		assertEquals(1, inbox.size());
		assertEquals(createdMessage.getId(), inbox.get(0).getId());
	}

	@Test
	public void getCharacterInbox_roleNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> messageService.getInbox(999L, testScenario.getId()));
	}

	@Test
	public void getCharacterInbox_ScenarioNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> messageService.getInbox(testRecipient.getId(), 999L));
	}

	@Test
	public void deleteMessage_messageNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> messageService.deleteMessage(999L));
	}

	@Test
	public void createMessage_creatorNotFound_throwsException() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(999L);
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		assertThrows(ResponseStatusException.class, () -> messageService.createMessage(postDTO));
	}

	@Test
	public void getMessageById_validId_success() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		Message createdMessage = messageService.createMessage(postDTO);

		Message retrievedMessage = messageService.getMessageById(createdMessage.getId());

		assertEquals(createdMessage.getId(), retrievedMessage.getId());
		assertEquals(createdMessage.getTitle(), retrievedMessage.getTitle());
	}

	@Test
	public void getMessageById_messageNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> messageService.getMessageById(999L));
	}

	@Test
	public void updateMessageStatus_validInputs_success() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		Message createdMessage = messageService.createMessage(postDTO);

		MessagePutDTO putDTO = new MessagePutDTO();
		putDTO.setStatus(CommsStatus.ACCEPTED);

		messageService.updateMessageStatus(createdMessage.getId(), putDTO);

		Message updatedMessage = messageRepository.findById(createdMessage.getId()).orElseThrow();
		assertEquals(CommsStatus.ACCEPTED, updatedMessage.getStatus());
	}

	@Test
	public void updateMessageStatus_invalidInput_throwsException() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		Message createdMessage = messageService.createMessage(postDTO);

		MessagePutDTO putDTO = new MessagePutDTO();
		putDTO.setStatus(null);

		assertThrows(ResponseStatusException.class, () -> messageService.updateMessageStatus(createdMessage.getId(), putDTO));
	}

	@Test
	public void updateMessageStatus_messageNotFound_throwsException() {
		MessagePutDTO putDTO = new MessagePutDTO();
		putDTO.setStatus(CommsStatus.ACCEPTED);

		assertThrows(ResponseStatusException.class, () -> messageService.updateMessageStatus(999L, putDTO));
	}

	@Test
	public void getMessagesBetween_validIds_success() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		messageService.createMessage(postDTO);

		// Caller is the creator, so they can see their own PENDING message.
		List<Message> result = messageService.getMessagesBetween(
			testCreator.getId(), testRecipient.getId(), testCreator.getId());

		assertEquals(1, result.size());
		assertEquals(testCreator.getId(), result.get(0).getCreator().getId());
		assertEquals(testRecipient.getId(), result.get(0).getRecipient().getId());
	}

	@Test
	public void getMessagePairsByScenario_validId_success() {
		MessagePostDTO postDTO = new MessagePostDTO();
		postDTO.setTitle("Test Message");
		postDTO.setBody("Test Body");
		postDTO.setCreatorId(testCreator.getId());
		postDTO.setRecipientId(testRecipient.getId());
		postDTO.setScenarioId(testScenario.getId());

		messageService.createMessage(postDTO);

		List<MessagePairDTO> result = messageService.getMessagePairsByScenario(testScenario.getId());

		assertEquals(1, result.size());
		assertEquals(Math.min(testCreator.getId(), testRecipient.getId()), result.get(0).getRoleAId());
		assertEquals(Math.max(testCreator.getId(), testRecipient.getId()), result.get(0).getRoleBId());
	}

	@Test
	public void getMessagePairsByScenario_scenarioNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> messageService.getMessagePairsByScenario(999L));
	}
}

