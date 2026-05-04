package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		testUser = new User();
		testUser.setId(1L);
		testUser.setPassword("testName");
		testUser.setUsername("testUsername");

		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
		
		User createdUser = userService.createUser(testUser);

		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getPassword(), createdUser.getPassword());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(Optional.empty());
		userService.createUser(testUser);

		// Second call: username now exists (duplicate)
        Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void createUser_duplicateInputs_throwsException() {
		// Mock: username already exists
        Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

}
