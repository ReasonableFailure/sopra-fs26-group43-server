package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
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
		testCreator.setActionPoints(10);
		testCreator.setAssignedCabinet(0L);

		testRecipient = new Role();
		testRecipient.setToken("test-recipient-token");
		testRecipient.setName("Recipient");
		testRecipient.setTitle("Recipient Title");
		testRecipient.setDescription("Recipient description");
		testRecipient.setSecret("recipient-secret");
		testRecipient.setAlive(true);
		testRecipient.setMessageCount(5);
		testRecipient.setActionPoints(10);
		testRecipient.setAssignedCabinet(1L);

		testScenario = new Scenario();
		testScenario.setTitle("Test Scenario");
		testScenario.setDescription("Test scenario description");
		testScenario.setActive(true);
		testScenario.setDayNumber(1);
		testScenario.setExchangeRate(1);
		testScenario.setPlayers(new ArrayList<>());
		testScenario.setHistory(new ArrayList<>());
		testScenario.getPlayers().add(testCreator);
		testScenario.getPlayers().add(testRecipient);

		testScenario = scenarioRepository.save(testScenario);
		testCreator = (Role) testScenario.getPlayers().get(0);
		testRecipient = (Role) testScenario.getPlayers().get(1);
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

		List<Message> result = messageService.getMessagesBetween(testCreator.getId(), testRecipient.getId());

		assertEquals(1, result.size());
		assertEquals(testCreator.getId(), result.get(0).getCreator().getId());
		assertEquals(testRecipient.getId(), result.get(0).getRecipient().getId());
	}

	@Test
	public void getMessagesBetween_charactersNotFound_throwsException() {
		assertThrows(ResponseStatusException.class,
				() -> messageService.getMessagesBetween(999L, testRecipient.getId()));
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

