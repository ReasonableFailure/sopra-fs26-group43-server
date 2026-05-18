package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.EngagementGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

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
        when(userRepository.findByUsername(Mockito.any())).thenReturn(Optional.empty());

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
        Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.empty());
        userService.createUser(testUser);

        // Second call: username now exists (duplicate)
        Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test
    void testValidateUserToken_Success() {
        String validToken = "correct-token";
        User mockUser = new User();
        mockUser.setToken(validToken);

        when(userRepository.findByToken(validToken)).thenReturn(Optional.of(mockUser));

        assertDoesNotThrow(() -> userService.validateUserToken(validToken));
    }

    @Test
    void testValidateUserToken_NullOrEmpty() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.validateUserToken(null));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void testValidateUserToken_NotFoundInDb() {
        String unknownToken = "ghost-token";
        when(userRepository.findByToken(unknownToken)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.validateUserToken(unknownToken));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
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
    }

    @Test
    public void createUser_invalidInput_throwsException() {
        User invalid = new User();
        invalid.setUsername("");
        invalid.setPassword("password");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(invalid));
    }

    @Test
    public void createUser_usernameTaken_throwsException() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test
    public void getProfileById_validInput_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getProfileById(1L);

        assertEquals(testUser, result);
    }

    @Test
    public void getProfileById_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getProfileById(99L));
    }

    @Test
    public void updateProfile_validInput_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        UserPutDTO putDTO = new UserPutDTO();
        putDTO.setBio("new bio");
        putDTO.setName("new name");

        userService.updateProfile(1L, putDTO);

        verify(userRepository).save(testUser);
        assertEquals("new bio", testUser.getBio());
        assertEquals("new name", testUser.getName());
    }

    @Test
    public void updateProfile_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserPutDTO putDTO = new UserPutDTO();
        putDTO.setBio("new bio");

        assertThrows(ResponseStatusException.class, () -> userService.updateProfile(99L, putDTO));
    }

    @Test
    public void updateProfile_invalidInput_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserPutDTO putDTO = new UserPutDTO();
        putDTO.setUsername("");

        assertThrows(ResponseStatusException.class, () -> userService.updateProfile(1L, putDTO));
    }

    @Test
    public void loginUser_validInput_success() {
        User stored = new User();
        stored.setUsername("testUsername");
        stored.setPassword("testPassword");
        stored.setToken("old-token");
        stored.setStatus(UserStatus.OFFLINE);

        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(stored));
        when(userRepository.save(stored)).thenReturn(stored);

        User login = new User();
        login.setUsername("testUsername");
        login.setPassword("testPassword");

        User result = userService.loginUser(login);

        assertNotNull(result.getToken());
        assertEquals(UserStatus.ONLINE, result.getStatus());
    }

    @Test
    public void loginUser_userNotFound_throwsException() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        User login = new User();
        login.setUsername("missing");
        login.setPassword("password");

        assertThrows(ResponseStatusException.class, () -> userService.loginUser(login));
    }

    @Test
    public void loginUser_passwordIncorrect_throwsException() {
        User stored = new User();
        stored.setUsername("testUsername");
        stored.setPassword("correctPassword");

        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(stored));

        User login = new User();
        login.setUsername("testUsername");
        login.setPassword("wrongPassword");

        assertThrows(ResponseStatusException.class, () -> userService.loginUser(login));
    }

    @Test
    public void logoutUser_validInput_success() {
        User stored = new User();
        stored.setId(1L);
        stored.setStatus(UserStatus.ONLINE);
        stored.setToken("token");

        when(userRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(userRepository.save(stored)).thenReturn(stored);

        userService.logoutUser(1L);

        verify(userRepository).save(stored);
        assertEquals(UserStatus.OFFLINE, stored.getStatus());
        assertNull(stored.getToken());
        assertFalse(stored.isPlaying());
    }

    @Test
    public void logoutUser_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.logoutUser(99L));
    }

    @Test
    public void deleteUser_validInput_success() {
        String token = "token";
        User stored = new User();
        stored.setId(1L);
        stored.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(stored));
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L, token);

        verify(userRepository).deleteById(1L);
    }

    @Test
    public void deleteUser_userNotFound_throwsException() {
        String token = "token";
        User stored = new User();
        stored.setId(1L);
        stored.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(stored));
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1L, token));
    }

    @Test
    public void getEngagements_validInput_success() {
        String token = "token";
        User stored = new User();
        stored.setId(1L);
        stored.setToken(token);

        // Use a minimal player object with a scenario to exercise mapping.
        Role role = new Role();
        role.setId(10L);
        role.setToken("role-token");
        role.setName("Character");
        role.setScenario(new Scenario());
        role.getScenario().setId(100L);
        role.getScenario().setTitle("Scenario");
        role.getScenario().setStatus(ScenarioStatus.UNSTARTED);
        role.setUser(stored);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(stored));
        when(userRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(playerRepository.findByUser_Id(1L)).thenReturn(List.of(role));

        List<EngagementGetDTO> engagements = userService.getEngagements(token, 1L);

        assertEquals(1, engagements.size());
        assertEquals(10L, engagements.get(0).getPlayerId());
    }

    @Test
    public void getEngagements_userNotFound_throwsException() {
        String token = "token";
        User stored = new User();
        stored.setId(1L);
        stored.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(stored));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getEngagements(token, 99L));
    }

    @Test
    public void getUsers_validInput_success() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("other");

        when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

        List<User> result = userService.getUsers();

        assertEquals(2, result.size());
    }

    @Test
    public void getByToken_validInput_success() {
        String token = "token";
        User stored = new User();
        stored.setId(1L);
        stored.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(stored), Optional.of(stored));

        User result = userService.getByToken(token);

        assertEquals(1L, result.getId());
    }

    @Test
    public void getByToken_userNotFound_throwsException() {
        String token = "token";
        User stored = new User();
        stored.setId(1L);
        stored.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(stored), Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getByToken(token));
    }

    @Test
    public void validateUserToken_validInput_success() {
        String token = "token";
        User stored = new User();
        stored.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(stored));

        assertDoesNotThrow(() -> userService.validateUserToken(token));
    }

    @Test
    public void validateUserToken_NotFoundInDb_throwsException() {
        String token = "token";
        when(userRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.validateUserToken(token));
    }

    @Test
    public void validateUserToken_userHasNullToken_throwsException() {
        String token = "some-token";
        User mockUser = new User();
        mockUser.setToken(null);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(mockUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.validateUserToken(token));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
