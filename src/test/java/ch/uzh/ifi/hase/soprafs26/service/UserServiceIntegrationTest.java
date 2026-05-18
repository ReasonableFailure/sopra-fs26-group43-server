package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
class UserServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private ScenarioRepository scenarioRepository;

	@Autowired
	private UserService userService;

	@BeforeEach
	public void setup() {
		playerRepository.deleteAll();
		scenarioRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void createUser_validInputs_success() {
		// given
		assertTrue(userRepository.findByUsername("testUsername").isEmpty());

		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");

		// when
		User createdUser = userService.createUser(testUser);

		// then
		assertNotNull(createdUser.getId());
		assertEquals(testUser.getPassword(), createdUser.getPassword());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	void createUser_duplicateUsername_throwsException() {
		assertTrue(userRepository.findByUsername("testUsername").isEmpty());

		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		userService.createUser(testUser);

		// attempt to create second user with same username
		User testUser2 = new User();

		// change the name but forget about the username
		testUser2.setPassword("testName2");
		testUser2.setUsername("testUsername");

		// check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}

	@Test
	void createUser_invalidInput_throwsException() {
		User invalidUser = new User();
		invalidUser.setPassword("");
		invalidUser.setUsername("testUsername");

		assertThrows(ResponseStatusException.class, () -> userService.createUser(invalidUser));
	}

	@Test
	void getProfileById_validInput_success() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		User created = userService.createUser(testUser);

		User retrieved = userService.getProfileById(created.getId());

		assertEquals(created.getId(), retrieved.getId());
	}

	@Test
	void getProfileById_userNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> userService.getProfileById(999L));
	}

	@Test
	void updateProfile_validInput_success() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		User created = userService.createUser(testUser);

		UserPutDTO putDTO = new UserPutDTO();
		putDTO.setBio("new bio");
		putDTO.setName("new name");

		userService.updateProfile(created.getId(), putDTO);

		User updated = userRepository.findById(created.getId()).orElseThrow();
		assertEquals("new bio", updated.getBio());
		assertEquals("new name", updated.getName());
	}

	@Test
	void updateProfile_userNotFound_throwsException() {
		UserPutDTO putDTO = new UserPutDTO();
		putDTO.setBio("new bio");

		assertThrows(ResponseStatusException.class, () -> userService.updateProfile(999L, putDTO));
	}

	@Test
	void updateProfile_invalidInput_throwsException() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		User created = userService.createUser(testUser);

		UserPutDTO putDTO = new UserPutDTO();
		putDTO.setUsername("");

		assertThrows(ResponseStatusException.class, () -> userService.updateProfile(created.getId(), putDTO));
	}

	@Test
	void loginUser_validInput_success() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		userService.createUser(testUser);

		User loginAttempt = new User();
		loginAttempt.setUsername("testUsername");
		loginAttempt.setPassword("testPassword");

		User loggedIn = userService.loginUser(loginAttempt);

		assertNotNull(loggedIn.getToken());
		assertEquals(UserStatus.ONLINE, loggedIn.getStatus());
	}

	@Test
	void loginUser_userNotFound_throwsException() {
		User loginAttempt = new User();
		loginAttempt.setUsername("missing");
		loginAttempt.setPassword("password");

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginAttempt));
	}

	@Test
	void loginUser_passwordIncorrect_throwsException() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		userService.createUser(testUser);

		User loginAttempt = new User();
		loginAttempt.setUsername("testUsername");
		loginAttempt.setPassword("wrongPassword");

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginAttempt));
	}

	@Test
	void logoutUser_validInput_success() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		User created = userService.createUser(testUser);

		userService.logoutUser(created.getId());

		User loggedOut = userRepository.findById(created.getId()).orElseThrow();
		assertEquals(UserStatus.OFFLINE, loggedOut.getStatus());
		assertNull(loggedOut.getToken());
		assertFalse(loggedOut.isPlaying());
	}

	@Test
	void logoutUser_userNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> userService.logoutUser(999L));
	}

	@Test
	void deleteUser_validInput_success() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		User created = userService.createUser(testUser);

		userService.deleteUser(created.getId(), created.getToken());

		assertTrue(userRepository.findById(created.getId()).isEmpty());
	}

	@Test
	void getEngagements_validInput_success() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		User created = userService.createUser(testUser);

		Scenario scenario = new Scenario();
		scenario.setTitle("Scenario");
		scenario.setDescription("Description");
		scenario.setStatus(ScenarioStatus.UNSTARTED);
		scenario.setDayNumber(0);
		scenario.setStartingMessageCount(1);
		scenario.setPlayers(new ArrayList<>());
		scenario = scenarioRepository.save(scenario);

		Role role = new Role();
		role.setToken("role-token");
		role.setName("Test Role");
		role.setTitle("Title");
		role.setDescription("Description");
		role.setSecret("secret");
		role.setAlive(true);
		role.setMessageCount(1);
		role.setTotalPoints(0);
		role.setPointsBalance(0);
		role.setNumberMessages(0);
		role.setNumberPronouncements(0);
		role.setTotalTextLength(0);
		role.setUser(created);
		scenario.addPlayer(role);
		scenarioRepository.save(scenario);

		assertFalse(userService.getEngagements(created.getToken(), created.getId()).isEmpty());
	}

	@Test
	void getEngagements_userNotFound_throwsException() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");

		User created = userService.createUser(testUser);
		String token = created.getToken();

		assertThrows(
			ResponseStatusException.class,
			() -> userService.getEngagements(token, 999L)
		);
	}

	@Test
	void getUsers_validInput_success() {
		User firstUser = new User();
		firstUser.setPassword("testPassword");
		firstUser.setUsername("testUsername");
		userService.createUser(firstUser);

		User secondUser = new User();
		secondUser.setPassword("testPassword2");
		secondUser.setUsername("testUsername2");
		userService.createUser(secondUser);

		List<User> users = userService.getUsers();

		assertTrue(users.size() >= 2);
	}

	@Test
	void getByToken_validInput_success() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		User created = userService.createUser(testUser);

		User byToken = userService.getByToken(created.getToken());

		assertEquals(created.getId(), byToken.getId());
	}

	@Test
	void getByToken_userNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> userService.getByToken("missing-token"));
	}

	@Test
	void validateUserToken_validInput_success() {
		User testUser = new User();
		testUser.setPassword("testPassword");
		testUser.setUsername("testUsername");
		User created = userService.createUser(testUser);

		assertDoesNotThrow(() -> userService.validateUserToken(created.getToken()));
	}

	@Test
	void validateUserToken_NotFoundInDb_throwsException() {
		assertThrows(ResponseStatusException.class, () -> userService.validateUserToken("missing-token"));
	}

	@Test
	void validateUserToken_NullOrEmpty_throwsException() {
		assertThrows(ResponseStatusException.class, () -> userService.validateUserToken(null));
	}
}
