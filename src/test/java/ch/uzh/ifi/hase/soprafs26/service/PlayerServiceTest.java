package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserAssignDTO;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private BackroomerRepository backroomerRepository;

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private UserService userService;

    @Mock
    private MastodonClient mastodonClient;

    @Mock
    private ActionPointService actionPointService;

    @InjectMocks
    private PlayerService playerService;

    private Scenario scenario;
    private Role role;
    private User user;

    @BeforeEach
    void setup() {
        scenario = new Scenario();
        scenario.setId(1L);
        scenario.setTitle("Scenario");
        scenario.setStartingMessageCount(5);
        scenario.setPlayers(new ArrayList<>());

        role = new Role();
        role.setId(1L);
        role.setName("Test Role");
        role.setTitle("Title");
        role.setDescription("Description");
        role.setSecret("secret");
        role.setAlive(true);
        role.setMessageCount(2);
        role.setTotalPoints(0);
        role.setPointsBalance(0);
        role.setScenario(scenario);

        scenario.getPlayers().add(role);

        user = new User();
        user.setId(2L);
        user.setToken("user-token");
    }

    @Test
    void getRoleById_withExistingRole_returnsRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        Role result = playerService.getRoleById(1L);

        assertEquals(role, result);
    }

    @Test
    void getRoleById_withUnknownRole_throwsException() {
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> playerService.getRoleById(99L));
    }

    @Test
    void updateRole_withPatchValues_savesUpdatedRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        doNothing().when(roleRepository).flush();

        RolePutDTO putDTO = new RolePutDTO();
        putDTO.setDescription("Updated description");
        putDTO.setAlive(false);

        playerService.updateRole(putDTO, 1L);

        verify(roleRepository).save(role);
        assertEquals("Updated description", role.getDescription());
        assertFalse(role.getAlive());
    }

    @Test
    void createRole_withValidScenario_returnsRole() {
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenario));
        when(scenarioRepository.save(any(Scenario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RolePostDTO postDTO = new RolePostDTO();
        postDTO.setName("New Role");
        postDTO.setTitle("New Title");
        postDTO.setDescription("New description");
        postDTO.setSecret("secret");
        postDTO.setScenarioId(1L);

        Role created = playerService.createRole(postDTO);

        assertNotNull(created.getToken());
        assertEquals("New Role", created.getName());
        assertEquals(5, created.getMessageCount());
        assertTrue(created.getAlive());
        assertTrue(scenario.getPlayers().contains(created));
    }

    @Test
    void deleteRole_callsRepositoryDelete() {
        doNothing().when(roleRepository).deleteById(1L);

        playerService.deleteRole(1L);

        verify(roleRepository).deleteById(1L);
    }

    @Test
    void createBackroomer_withValidUser_returnsBackroomer() {
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenario));
        when(userService.getProfileById(2L)).thenReturn(user);
        when(backroomerRepository.save(any(Backroomer.class))).thenAnswer(invocation -> {
            Backroomer backroomer = invocation.getArgument(0);
            backroomer.setId(3L);
            return backroomer;
        });

        UserAssignDTO assignDTO = new UserAssignDTO();
        assignDTO.setId(2L);

        Backroomer created = playerService.createBackroomer(assignDTO, 1L);

        assertEquals(3L, created.getId());
        assertEquals(user, created.getUser());
        assertEquals(scenario, created.getScenario());
    }

    @Test
    void claimRole_withUnclaimedRole_assignsUser() {
        role.setUser(null);
        role.setScenario(scenario);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(playerRepository.findByUser_Id(2L)).thenReturn(Collections.emptyList());
        when(userService.getProfileById(2L)).thenReturn(user);
        when(roleRepository.save(role)).thenReturn(role);

        Role assigned = playerService.claimRole(1L, 2L);

        assertEquals(user, assigned.getUser());
        verify(roleRepository).save(role);
    }

    @Test
    void getInterlocutors_withMessages_returnsUniqueRoleList() {
        Role otherRole = new Role();
        otherRole.setId(3L);
        otherRole.setName("Other Role");

        Message outgoing = new Message();
        outgoing.setCreator(role);
        outgoing.setRecipient(otherRole);
        outgoing.setScenario(scenario);

        Message incoming = new Message();
        incoming.setCreator(otherRole);
        incoming.setRecipient(role);
        incoming.setScenario(scenario);

        doNothing().when(userService).validateUserToken("user-token");
        when(roleRepository.existsById(1L)).thenReturn(true);
        when(scenarioRepository.existsById(1L)).thenReturn(true);
        when(messageRepository.findAllByScenarioAndRole(1L, 1L))
                .thenReturn(List.of(outgoing, incoming));

        List<Role> interlocutors = playerService.getInterlocutors("user-token", 1L, 1L);

        assertEquals(1, interlocutors.size());
        assertEquals(3L, interlocutors.get(0).getId());
    }

    @Test
    void createDirector_withValidUser_createsDirector() {
        when(userService.getProfileById(2L)).thenReturn(user);
        when(directorRepository.save(any(Director.class))).thenAnswer(invocation -> {
            Director director = invocation.getArgument(0);
            director.setId(4L);
            return director;
        });

        Director director = playerService.createDirector(2L);

        assertEquals(4L, director.getId());
        assertEquals(user, director.getUser());
        assertNotNull(director.getToken());
    }

    @Test
    void syncPointsAndGetRole_callsActionPointService() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenario));
        doNothing().when(actionPointService).syncActionPoints(role, scenario);
        when(roleRepository.save(role)).thenReturn(role);

        Role result = playerService.syncPointsAndGetRole(1L, 1L);

        assertEquals(role, result);
        verify(actionPointService).syncActionPoints(role, scenario);
    }
}
