package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Directive;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.repository.DirectiveRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePutDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
public class DirectiveServiceIntegrationTest {

	@Qualifier("directiveRepository")
	@Autowired
	private DirectiveRepository directiveRepository;

	@Qualifier("scenarioRepository")
	@Autowired
	private ScenarioRepository scenarioRepository;

	@Qualifier("roleRepository")
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private DirectiveService directiveService;

	private Scenario testScenario;
	private Role testRole;
	private Directive testDirective;

	@BeforeEach
	public void setup() {
		directiveRepository.deleteAll();
		scenarioRepository.deleteAll();
		roleRepository.deleteAll();

		testRole = new Role();
		testRole.setToken("test-token");
		testRole.setName("Test Role");
		testRole.setTitle("Test Title");
		testRole.setDescription("Test description");
		testRole.setSecret("secret");
		testRole.setAlive(true);
		testRole.setMessageCount(5);
		testRole.setTotalPoints(10);
		testRole.setPointsBalance(10);
		testRole.setAssignedCabinet(0L);

		testScenario = new Scenario();
		testScenario.setTitle("Test Scenario");
		testScenario.setDescription("Test scenario description");
		testScenario.setActive(true);
		testScenario.setPlayers(new ArrayList<>());
		testScenario.setHistory(new ArrayList<>());
		testScenario.getPlayers().add(testRole);
		testScenario = scenarioRepository.save(testScenario);
		testRole = (Role) testScenario.getPlayers().get(0);
	}

	@Test
	public void createDirective_validInputs_success() {

		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(testRole.getId());
		postDTO.setScenarioId(testScenario.getId());

		Directive createdDirective = directiveService.createDirective(postDTO);

		assertNotNull(createdDirective.getId());
		assertEquals(postDTO.getTitle(), createdDirective.getTitle());
		assertEquals(postDTO.getBody(), createdDirective.getBody());
		assertEquals(CommsStatus.PENDING, createdDirective.getStatus());
		assertEquals(testRole.getId(), createdDirective.getCreator().getId());
		assertEquals(testScenario.getId(), createdDirective.getScenario().getId());
		assertNotNull(createdDirective.getCreatedAt());
	}

	@Test
	public void createDirective_invalidInput_creatorIdNull_throwsException() {
		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(null);
		postDTO.setScenarioId(testScenario.getId());

		assertThrows(ResponseStatusException.class, () -> directiveService.createDirective(postDTO));
	}

	@Test
	public void createDirective_scenarioNotFound_throwsException() {
		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(testRole.getId());
		postDTO.setScenarioId(999L);

		assertThrows(ResponseStatusException.class, () -> directiveService.createDirective(postDTO));
	}

	@Test
	public void createDirective_roleNotFound_throwsException() {
		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(999L);
		postDTO.setScenarioId(testScenario.getId());

		assertThrows(ResponseStatusException.class, () -> directiveService.createDirective(postDTO));
	}

	@Test
	public void createDirective_roleNotPartOfScenario_throwsException() {
		Role otherRole = new Role();
		otherRole.setToken("other-token");
		otherRole.setName("Other Role");
		otherRole.setTitle("Other Title");
		otherRole.setDescription("Other description");
		otherRole.setSecret("other-secret");
		otherRole.setAlive(true);
		otherRole.setMessageCount(5);
		otherRole.setTotalPoints(10);
		otherRole.setPointsBalance(10);
		otherRole.setAssignedCabinet(99L);
		otherRole = roleRepository.save(otherRole);

		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(otherRole.getId());
		postDTO.setScenarioId(testScenario.getId());

		assertThrows(ResponseStatusException.class, () -> directiveService.createDirective(postDTO));
	}

	@Test
	public void getDirectiveById_validId_success() {
		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(testRole.getId());
		postDTO.setScenarioId(testScenario.getId());

		Directive createdDirective = directiveService.createDirective(postDTO);

		Directive retrievedDirective = directiveService.getDirectiveById(createdDirective.getId());

		assertEquals(createdDirective.getId(), retrievedDirective.getId());
		assertEquals(createdDirective.getTitle(), retrievedDirective.getTitle());
		assertEquals(createdDirective.getBody(), retrievedDirective.getBody());
		assertEquals(createdDirective.getStatus(), retrievedDirective.getStatus());
		assertEquals(createdDirective.getCreator().getId(), retrievedDirective.getCreator().getId());
		assertEquals(createdDirective.getScenario().getId(), retrievedDirective.getScenario().getId());
	}

	@Test
	public void getDirectiveById_directiveNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> directiveService.getDirectiveById(999L));
	}

	@Test
	public void updateDirectiveStatus_validInputs_success() {
		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(testRole.getId());
		postDTO.setScenarioId(testScenario.getId());

		Directive createdDirective = directiveService.createDirective(postDTO);

		DirectivePutDTO putDTO = new DirectivePutDTO();
		putDTO.setStatus(CommsStatus.ACCEPTED);
		putDTO.setResponse("Accepted response");

		directiveService.updateDirectiveStatus(createdDirective.getId(), putDTO);

		Directive updatedDirective = directiveRepository.findById(createdDirective.getId()).get();
		assertEquals(CommsStatus.ACCEPTED, updatedDirective.getStatus());
		assertEquals("Accepted response", updatedDirective.getResponse());
	}

	@Test
	public void updateDirectiveStatus_invalidInput_statusNull_throwsException() {
		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(testRole.getId());
		postDTO.setScenarioId(testScenario.getId());

		Directive createdDirective = directiveService.createDirective(postDTO);

		DirectivePutDTO putDTO = new DirectivePutDTO();
		putDTO.setStatus(null); 
		putDTO.setResponse("Some response");

		assertThrows(ResponseStatusException.class,
			() -> directiveService.updateDirectiveStatus(createdDirective.getId(), putDTO));
	}

	@Test
	public void updateDirectiveStatus_directiveNotFound_throwsException() {
		DirectivePutDTO putDTO = new DirectivePutDTO();
		putDTO.setStatus(CommsStatus.ACCEPTED);
		putDTO.setResponse("Accepted response");

		assertThrows(ResponseStatusException.class, () -> directiveService.updateDirectiveStatus(999L, putDTO));
	}

	@Test
	public void getDirectivesByScenario_validScenarioId_success() {
		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(testRole.getId());
		postDTO.setScenarioId(testScenario.getId());

		directiveService.createDirective(postDTO);

		List<Directive> directives = directiveService.getDirectivesByScenario(testScenario.getId());

		assertEquals(1, directives.size());
		assertEquals("Test Directive", directives.get(0).getTitle());
	}

	@Test
	public void getDirectivesByScenario_scenarioNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> directiveService.getDirectivesByScenario(999L));
	}

	@Test
	public void getDirectivesByCreator_validCharacterId_success() {
		DirectivePostDTO postDTO = new DirectivePostDTO();
		postDTO.setTitle("Test Directive");
		postDTO.setBody("Test directive body");
		postDTO.setCreatorId(testRole.getId());
		postDTO.setScenarioId(testScenario.getId());

		directiveService.createDirective(postDTO);

		List<Directive> directives = directiveService.getDirectivesByCreator(testRole.getId());

		assertEquals(1, directives.size());
		assertEquals("Test Directive", directives.get(0).getTitle());
	}

	@Test
	public void getDirectivesByCreator_characterNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> directiveService.getDirectivesByCreator(999L));
	}
}