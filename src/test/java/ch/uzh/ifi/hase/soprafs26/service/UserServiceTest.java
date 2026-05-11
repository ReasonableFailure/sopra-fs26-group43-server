package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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

		when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
		
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
        when(userRepository.findByUsername(Mockito.any())).thenReturn(Optional.empty());
		userService.createUser(testUser);

		// Second call: username now exists (duplicate)
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void createUser_duplicateInputs_throwsException() {
		// Mock: username already exists
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}
    @Test
    void testValidateUserToken_Success() {
            String validToken = "correct-token";
            User mockUser = new User();
            mockUser.setToken(validToken);

            when(userRepository.findByToken(validToken)).thenReturn(Optional.of(mockUser));

            // Should not throw any exception
            assertDoesNotThrow(() -> userService.validateUserToken(validToken));
        }

    @Test
    void testValidateUserToken_NullOrEmpty() {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.validateUserToken(null));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
            assertTrue(ex.getReason().contains("Token not there"));
        }

    @Test
    void testValidateUserToken_NotFoundInDb() {
            String unknownToken = "ghost-token";
            when(userRepository.findByToken(unknownToken)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.validateUserToken(unknownToken));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
            assertTrue(ex.getReason().contains("This is not a valid token"));
        }

    @Test
    void testValidateUserToken_UserHasNullToken() {
            String token = "some-token";
            User mockUser = new User();
            mockUser.setToken(null);

            when(userRepository.findByToken(token)).thenReturn(Optional.of(mockUser));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.validateUserToken(token));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
            assertTrue(ex.getReason().contains("not associated with any user"));
        }

    @Test
    void testValidateUserToken_MismatchedToken() {
            String inputToken = "token-A";
            User mockUser = new User();
            mockUser.setToken("token-B"); // Different from input

            when(userRepository.findByToken(inputToken)).thenReturn(Optional.of(mockUser));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.validateUserToken(inputToken));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
            assertTrue(ex.getReason().contains("Wrong token"));
        }
}
