package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserAssignDTO;
import ch.uzh.ifi.hase.soprafs26.service.ActionPointService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private ActionPointService actionPointService;

    @MockitoBean
    private UserService userService;

    private Role testRole;
    private Director testDirector;
    private Backroomer testBackroomer;
    private User testUser;

    @BeforeEach
    public void setupTests() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("Test Role");
        testRole.setTitle("Title");
        testRole.setDescription("Description");
        testRole.setSecret("secret");
        testRole.setAlive(true);
        testRole.setMessageCount(3);
        testRole.setTotalPoints(5);
        testRole.setPointsBalance(2);
        testRole.setToken("role-token");

        testDirector = new Director();
        testDirector.setId(2L);
        testDirector.setToken("director-token");

        testBackroomer = new Backroomer();
        testBackroomer.setId(3L);
        testBackroomer.setToken("backroomer-token");

        testUser = new User();
        testUser.setId(4L);
        testUser.setToken("user-token");
    }

    @Test
    void updateRole_whenValidDirectorToken_returnsNoContent() throws Exception {
        RolePutDTO putDTO = new RolePutDTO();
        putDTO.setDescription("Updated description");
        putDTO.setAlive(false);

        when(playerService.validate("Director director-token", "Director")).thenReturn("director-token");
        doNothing().when(playerService).updateRole(any(RolePutDTO.class), eq(1L));

        mockMvc.perform(put("/characters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Updated description\",\"alive\":false}")
                        .header("Authorization", "Director director-token"))
                .andExpect(status().isNoContent());

        verify(playerService).validate("Director director-token", "Director");
        verify(playerService).updateRole(any(RolePutDTO.class), eq(1L));
    }

    @Test
    void getRole_whenAuthorized_returnsRoleDetail() throws Exception {
        when(playerService.validate("Bearer user-token", "any")).thenReturn("user-token");
        when(playerService.getRoleById(1L)).thenReturn(testRole);

        mockMvc.perform(get("/characters/1/detail")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Role"))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(playerService).validate("Bearer user-token", "any");
    }

    @Test
    void createRole_whenDirectorToken_returnsCreatedRole() throws Exception {
        RolePostDTO postDTO = new RolePostDTO();
        postDTO.setName("New Role");
        postDTO.setTitle("New Title");
        postDTO.setDescription("New description");
        postDTO.setSecret("secret");
        postDTO.setScenarioId(1L);

        Role createdRole = new Role();
        createdRole.setId(5L);
        createdRole.setName("New Role");
        createdRole.setTitle("New Title");
        createdRole.setDescription("New description");
        createdRole.setSecret("secret");
        createdRole.setAlive(true);
        createdRole.setToken("new-role-token");

        when(playerService.validate("Director director-token", "Director")).thenReturn("director-token");
        when(playerService.createRole(any(RolePostDTO.class))).thenReturn(createdRole);

        mockMvc.perform(post("/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Role\",\"title\":\"New Title\",\"description\":\"New description\",\"secret\":\"secret\",\"scenarioId\":1}")
                        .header("Authorization", "Director director-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New Role"))
                .andExpect(jsonPath("$.token").value("new-role-token"));

        verify(playerService).validate("Director director-token", "Director");
        verify(playerService).createRole(any(RolePostDTO.class));
    }

    @Test
    void deleteRole_whenDirectorToken_returnsOk() throws Exception {
        when(playerService.validate("Director director-token", "Director")).thenReturn("director-token");
        doNothing().when(playerService).deleteRole(1L);

        mockMvc.perform(delete("/characters/1")
                        .header("Authorization", "Director director-token"))
                .andExpect(status().isOk());

        verify(playerService).validate("Director director-token", "Director");
        verify(playerService).deleteRole(1L);
    }

    @Test
    void createBackroomer_whenUserOwnsToken_returnsBackroomer() throws Exception {
        UserAssignDTO userAssignDTO = new UserAssignDTO();
        userAssignDTO.setId(4L);

        when(playerService.validate("Bearer user-token", "Bearer")).thenReturn("user-token");
        when(userService.getByToken("user-token")).thenReturn(testUser);

        Backroomer createdBackroomer = new Backroomer();
        createdBackroomer.setId(6L);
        createdBackroomer.setToken("backroomer-token");

        when(playerService.createBackroomer(any(UserAssignDTO.class), eq(1L))).thenReturn(createdBackroomer);

        mockMvc.perform(post("/scenario/1/backroomers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":4}")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.token").value("backroomer-token"));

        verify(playerService).validate("Bearer user-token", "Bearer");
        verify(userService).getByToken("user-token");
        verify(playerService).createBackroomer(any(UserAssignDTO.class), eq(1L));
    }

    @Test
    void selectCharacter_whenUserOwnsToken_returnsAssignedRole() throws Exception {
        UserAssignDTO userAssignDTO = new UserAssignDTO();
        userAssignDTO.setId(4L);

        when(playerService.validate("Bearer user-token", "Bearer")).thenReturn("user-token");
        when(userService.getByToken("user-token")).thenReturn(testUser);

        Role assignedRole = new Role();
        assignedRole.setId(7L);
        assignedRole.setName("Assigned Role");
        assignedRole.setToken("assigned-role-token");

        when(playerService.claimRole(eq(1L), eq(4L))).thenReturn(assignedRole);

        mockMvc.perform(put("/characters/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":4}")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Assigned Role"));

        verify(playerService).validate("Bearer user-token", "Bearer");
        verify(userService).getByToken("user-token");
        verify(playerService).claimRole(1L, 4L);
    }

    @Test
    void getInterlocutors_whenAuthorized_returnsRoleList() throws Exception {
        Role otherRole = new Role();
        otherRole.setId(8L);
        otherRole.setName("Other Role");

        when(playerService.getInterlocutors("Bearer user-token", 1L, 1L)).thenReturn(java.util.List.of(otherRole));

        mockMvc.perform(get("/characters/1/1/interlocutors")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()" ).value(1))
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].name").value("Other Role"));

        verify(playerService).getInterlocutors("Bearer user-token", 1L, 1L);
    }

    @Test
    void syncAndGetPoints_whenRoleToken_returnsUpdatedRole() throws Exception {
        when(playerService.validate("Role role-token", "Role")).thenReturn("role-token");
        when(playerService.syncPointsAndGetRole(1L, 1L)).thenReturn(testRole);

        mockMvc.perform(get("/characters/1/1/points")
                        .header("Authorization", "Role role-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Role"));

        verify(playerService).validate("Role role-token", "Role");
        verify(playerService).syncPointsAndGetRole(1L, 1L);
    }

    @Test
    void createScenarioDirector_whenUserOwnsToken_returnsDirector() throws Exception {
        UserAssignDTO userAssignDTO = new UserAssignDTO();
        userAssignDTO.setId(4L);

        when(playerService.validate("Bearer user-token", "Bearer")).thenReturn("user-token");
        when(userService.getByToken("user-token")).thenReturn(testUser);

        Director director = new Director();
        director.setId(9L);
        director.setToken("new-director-token");

        when(playerService.createDirector(4L)).thenReturn(director);

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":4}")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.token").value("new-director-token"));

        verify(playerService).validate("Bearer user-token", "Bearer");
        verify(userService).getByToken("user-token");
        verify(playerService).createDirector(4L);
    }

    @Test
    void buyMessage_whenRoleToken_returnsRole() throws Exception {
        when(playerService.validate("Role role-token", "Role")).thenReturn("role-token");
        when(actionPointService.buyMessage(1L, 1L)).thenReturn(testRole);

        mockMvc.perform(post("/scenarios/1/characters/1/messages")
                        .header("Authorization", "Role role-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Role"));

        verify(playerService).validate("Role role-token", "Role");
        verify(actionPointService).buyMessage(1L, 1L);
    }
}
