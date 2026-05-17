package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import ch.uzh.ifi.hase.soprafs26.repository.BackroomerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs26.repository.NewsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.DirectorRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserAssignDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebAppConfiguration
@SpringBootTest
@Transactional
public class PlayerServiceIntegrationTest {

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BackroomerRepository backroomerRepository;

    @Autowired
    private DirectorRepository directorRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerService playerService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ActionPointService actionPointService;

    @MockitoBean
    private MastodonClient mastodonClient;

    private Scenario testScenario;

    @BeforeEach
    public void setup() {
        messageRepository.deleteAll();
        newsRepository.deleteAll();
        backroomerRepository.deleteAll();
        directorRepository.deleteAll();
        roleRepository.deleteAll();
        scenarioRepository.deleteAll();

        testScenario = new Scenario();
        testScenario.setTitle("Integration Scenario");
        testScenario.setDescription("Integration description");
        testScenario.setStatus(ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus.UNSTARTED);
        testScenario.setDayNumber(0);
        testScenario.setStartingMessageCount(10);
        testScenario.setPlayers(new java.util.ArrayList<>());
        testScenario = scenarioRepository.save(testScenario);
    }

    @Test
    public void createRole_persistsNewRoleWithScenario() {
        RolePostDTO postDTO = new RolePostDTO();
        postDTO.setName("New Role");
        postDTO.setTitle("New Title");
        postDTO.setDescription("New description");
        postDTO.setSecret("secret");
        postDTO.setScenarioId(testScenario.getId());

        Role created = playerService.createRole(postDTO);

        assertNotNull(created.getId());
        assertEquals("New Role", created.getName());
        assertEquals(testScenario.getId(), created.getScenario().getId());
        assertTrue(roleRepository.findById(created.getId()).isPresent());
    }

    @Test
    public void createBackroomer_withValidUser_savesBackroomer() {
        UserAssignDTO userAssignDTO = new UserAssignDTO();
        userAssignDTO.setId(42L);

        User persistedUser = new User();
        persistedUser.setToken("user-token-42");
        persistedUser.setUsername("user42");
        persistedUser.setPassword("pw");
        persistedUser.setStatus(ch.uzh.ifi.hase.soprafs26.constant.UserStatus.OFFLINE);
        persistedUser.setCreationDate(new java.util.Date());
        persistedUser.setPlaying(false);
        persistedUser = userRepository.save(persistedUser);

        when(userService.getProfileById(42L)).thenReturn(persistedUser);

        Backroomer created = playerService.createBackroomer(userAssignDTO, testScenario.getId());

        assertNotNull(created.getId());
        assertEquals(persistedUser, created.getUser());
        assertEquals(testScenario.getId(), created.getScenario().getId());
    }

    @Test
    public void claimRole_withExistingRole_assignsUserToRole() {
        Role role = new Role();
        role.setToken("role-token-43");
        role.setName("Candidate");
        role.setTitle("Candidate Title");
        role.setDescription("Candidate description");
        role.setSecret("secret");
        role.setAlive(true);
        role.setMessageCount(1);
        role.setTotalPoints(0);
        role.setPointsBalance(0);
        role.setNumberMessages(0);
        role.setNumberPronouncements(0);
        role.setTotalTextLength(0);
        role.setScenario(testScenario);
        role = roleRepository.save(role);

        User persistedUser = new User();
        persistedUser.setToken("user-token-43");
        persistedUser.setUsername("user43");
        persistedUser.setPassword("pw");
        persistedUser.setStatus(ch.uzh.ifi.hase.soprafs26.constant.UserStatus.OFFLINE);
        persistedUser.setCreationDate(new java.util.Date());
        persistedUser.setPlaying(false);
        persistedUser = userRepository.save(persistedUser);

        when(userService.getProfileById(43L)).thenReturn(persistedUser);

        Role claimed = playerService.claimRole(role.getId(), 43L);

        assertEquals(persistedUser.getId(), claimed.getUser().getId());
        assertEquals(role.getId(), claimed.getId());
    }

    @Test
    public void syncPointsAndGetRole_invokesActionPointService() {
        Role role = new Role();
        role.setToken("sync-role-token");
        role.setName("SyncRole");
        role.setTitle("Sync Title");
        role.setDescription("Sync description");
        role.setSecret("secret");
        role.setAlive(true);
        role.setMessageCount(1);
        role.setTotalPoints(0);
        role.setPointsBalance(0);
        role.setNumberMessages(0);
        role.setNumberPronouncements(0);
        role.setTotalTextLength(0);
        role.setScenario(testScenario);
        role = roleRepository.save(role);

        doNothing().when(actionPointService).syncActionPoints(any(Role.class), eq(testScenario));

        Role result = playerService.syncPointsAndGetRole(testScenario.getId(), role.getId());

        assertEquals(role.getId(), result.getId());
        org.mockito.Mockito.verify(actionPointService).syncActionPoints(any(Role.class), eq(testScenario));
    }
}
