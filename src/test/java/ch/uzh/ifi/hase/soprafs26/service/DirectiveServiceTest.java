package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DirectiveServiceTest {

	@Mock
	private DirectiveRepository directiveRepository;

	@Mock
	private ScenarioRepository scenarioRepository;

	@Mock
	private RoleRepository roleRepository;

	@InjectMocks
	private DirectiveService directiveService;

	private DirectivePostDTO testDirectivePostDTO;
	private DirectivePutDTO testDirectivePutDTO;
	private Scenario testScenario;
	private Role testRole;
	private Directive testDirective;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		testDirectivePostDTO = new DirectivePostDTO();
		testDirectivePostDTO.setTitle("Test Directive");
		testDirectivePostDTO.setBody("Test directive body");
		testDirectivePostDTO.setCreatorId(1L);
		testDirectivePostDTO.setScenarioId(1L);

		testDirectivePutDTO = new DirectivePutDTO();
		testDirectivePutDTO.setStatus(CommsStatus.ACCEPTED);
		testDirectivePutDTO.setResponse("Test response");

		testScenario = new Scenario();
		testScenario.setId(1L);
		testScenario.setPlayers(new ArrayList<>());
		testScenario.setHistory(new ArrayList<>());

		testRole = new Role();
		testRole.setId(1L);
		testRole.setName("Test Role");

		testScenario.getPlayers().add(testRole);

		testDirective = new Directive();
		testDirective.setId(1L);
		testDirective.setTitle("Test Directive");
		testDirective.setBody("Test directive body");
		testDirective.setStatus(CommsStatus.PENDING);
		testDirective.setCreator(testRole);
		testDirective.setScenario(testScenario);
		testDirective.setCreatedAt(Instant.now());

		Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
		Mockito.when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
		Mockito.when(directiveRepository.save(Mockito.any())).thenReturn(testDirective);
		Mockito.when(scenarioRepository.save(Mockito.any())).thenReturn(testScenario);
	}

	@Test
	public void createDirective_validInputs_success() {
		Directive createdDirective = directiveService.createDirective(testDirectivePostDTO);

		Mockito.verify(directiveRepository, Mockito.times(1)).save(Mockito.any());
		Mockito.verify(scenarioRepository, Mockito.times(1)).save(testScenario);

		assertEquals(testDirectivePostDTO.getTitle(), createdDirective.getTitle());
		assertEquals(testDirectivePostDTO.getBody(), createdDirective.getBody());
		assertEquals(CommsStatus.PENDING, createdDirective.getStatus());
		assertEquals(testRole, createdDirective.getCreator());
		assertEquals(testScenario, createdDirective.getScenario());
		assertNotNull(createdDirective.getCreatedAt());
	}

	@Test
	public void createDirective_invalidInput_creatorIdNull_throwsException() {
		testDirectivePostDTO.setCreatorId(null);

		assertThrows(ResponseStatusException.class, () -> directiveService.createDirective(testDirectivePostDTO));
	}

	@Test
	public void createDirective_scenarioNotFound_throwsException() {
		Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> directiveService.createDirective(testDirectivePostDTO));
	}

	@Test
	public void createDirective_roleNotFound_throwsException() {
		Mockito.when(roleRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> directiveService.createDirective(testDirectivePostDTO));
	}

	@Test
	public void createDirective_roleNotPartOfScenario_throwsException() {
		Scenario scenarioWithoutRole = new Scenario();
		scenarioWithoutRole.setId(1L);
		scenarioWithoutRole.setPlayers(new ArrayList<>());
		scenarioWithoutRole.setHistory(new ArrayList<>());
		
		Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenarioWithoutRole));

		assertThrows(ResponseStatusException.class, () -> directiveService.createDirective(testDirectivePostDTO));
	}

	@Test
	public void getDirectiveById_validId_success() {
		Mockito.when(directiveRepository.findById(1L)).thenReturn(Optional.of(testDirective));

		Directive result = directiveService.getDirectiveById(1L);

		assertEquals(testDirective, result);
		Mockito.verify(directiveRepository, Mockito.times(1)).findById(1L);
	}

	@Test
	public void getDirectiveById_directiveNotFound_throwsException() {
		Mockito.when(directiveRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> directiveService.getDirectiveById(1L));
	}

	@Test
	public void updateDirectiveStatus_validInputs_success() {
		Mockito.when(directiveRepository.findById(1L)).thenReturn(Optional.of(testDirective));

		directiveService.updateDirectiveStatus(1L, testDirectivePutDTO);

		Mockito.verify(directiveRepository, Mockito.times(1)).findById(1L);
		Mockito.verify(directiveRepository, Mockito.times(1)).save(testDirective);
		assertEquals(CommsStatus.ACCEPTED, testDirective.getStatus());
		assertEquals("Test response", testDirective.getResponse());
	}

	@Test
	public void updateDirectiveStatus_invalidInput_statusNull_throwsException() {
		testDirectivePutDTO.setStatus(null);

		assertThrows(ResponseStatusException.class, () -> directiveService.updateDirectiveStatus(1L, testDirectivePutDTO));
	}

	@Test
	public void updateDirectiveStatus_directiveNotFound_throwsException() {
		Mockito.when(directiveRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> directiveService.updateDirectiveStatus(1L, testDirectivePutDTO));
	}

	@Test
	public void getDirectivesByScenario_validScenarioId_success() {
		List<Directive> expectedDirectives = Arrays.asList(testDirective);
		Mockito.when(scenarioRepository.existsById(1L)).thenReturn(true);
		Mockito.when(directiveRepository.findByScenarioId(1L)).thenReturn(expectedDirectives);

		List<Directive> result = directiveService.getDirectivesByScenario(1L);

		assertEquals(expectedDirectives, result);
		Mockito.verify(scenarioRepository, Mockito.times(1)).existsById(1L);
		Mockito.verify(directiveRepository, Mockito.times(1)).findByScenarioId(1L);
	}

	@Test
	public void getDirectivesByScenario_scenarioNotFound_throwsException() {
		Mockito.when(scenarioRepository.existsById(1L)).thenReturn(false);

		assertThrows(ResponseStatusException.class, () -> directiveService.getDirectivesByScenario(1L));
	}

	@Test
	public void getDirectivesByCreator_validCharacterId_success() {
		List<Directive> expectedDirectives = Arrays.asList(testDirective);
		Mockito.when(roleRepository.existsById(1L)).thenReturn(true);
		Mockito.when(directiveRepository.findByCreatorId(1L)).thenReturn(expectedDirectives);

		List<Directive> result = directiveService.getDirectivesByCreator(1L);

		assertEquals(expectedDirectives, result);
		Mockito.verify(roleRepository, Mockito.times(1)).existsById(1L);
		Mockito.verify(directiveRepository, Mockito.times(1)).findByCreatorId(1L);
	}

	@Test
	public void getDirectivesByCreator_characterNotFound_throwsException() {
		Mockito.when(roleRepository.existsById(1L)).thenReturn(false);

		assertThrows(ResponseStatusException.class, () -> directiveService.getDirectivesByCreator(1L));
	}
}
