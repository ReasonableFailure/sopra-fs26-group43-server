package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.EngagementGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	public void createUser_validInput_success() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setPassword("Test User");
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setPassword("Test User");
		userPostDTO.setUsername("testUsername");

		doReturn(user).when(userService).createUser(Mockito.any());

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

	@Test
	public void retrieveUser_validInput_success() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setPassword("Test User");
		user.setToken("token");
		user.setStatus(UserStatus.ONLINE);

		doNothing().when(userService).validateUserToken("token");
		doReturn(user).when(userService).getProfileById(1L);

		mockMvc.perform(get("/users/1")
				.header("Authorization", "Bearer token")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("testUsername")))
				.andExpect(jsonPath("$.status", is("ONLINE")));
	}

	@Test
	public void updateUser_validInput_success() throws Exception {
		User bearer = new User();
		bearer.setId(1L);
		bearer.setToken("token");

		UserPutDTO userPutDTO = new UserPutDTO();
		userPutDTO.setBio("updated bio");

		doReturn(bearer).when(userService).getByToken("token");
		doNothing().when(userService).updateProfile(eq(1L), Mockito.any(UserPutDTO.class));

		mockMvc.perform(put("/users/1")
				.header("Authorization", "Bearer token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPutDTO)))
				.andExpect(status().isNoContent());

		verify(userService).updateProfile(eq(1L), Mockito.any(UserPutDTO.class));
	}

	@Test
	public void loginUser_validInput_success() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setPassword("Test User");
		user.setToken("token");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userLoginDTO = new UserPostDTO();
		userLoginDTO.setUsername("testUsername");
		userLoginDTO.setPassword("Test User");

		doReturn(user).when(userService).loginUser(Mockito.any());

		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userLoginDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("testUsername")))
				.andExpect(jsonPath("$.status", is("ONLINE")));
	}

	@Test
	public void logout_validInput_success() throws Exception {
		User bearer = new User();
		bearer.setId(1L);
		bearer.setToken("token");

		doReturn(bearer).when(userService).getByToken("token");
		doNothing().when(userService).logoutUser(1L);

		mockMvc.perform(post("/logout/1")
				.header("Authorization", "Bearer token")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(userService).logoutUser(1L);
	}

	@Test
	public void deleteUser_validInput_success() throws Exception {
		doNothing().when(userService).deleteUser(1L, "token");

		mockMvc.perform(delete("/users/1")
				.header("Authorization", "Bearer token")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

		verify(userService).deleteUser(1L, "token");
	}

	@Test
	public void getEngagements_validInput_success() throws Exception {
		User bearer = new User();
		bearer.setId(1L);
		bearer.setToken("token");

		EngagementGetDTO engagement = new EngagementGetDTO();
		engagement.setPlayerId(11L);
		engagement.setScenarioId(22L);
		engagement.setRoleType("CHARACTER");
		engagement.setToken("character-token");

		doReturn(bearer).when(userService).getByToken("token");
		doReturn(Collections.singletonList(engagement)).when(userService).getEngagements("token", 1L);

		mockMvc.perform(get("/users/1/engagements")
				.header("Authorization", "Bearer token")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].playerId", is(11)))
				.andExpect(jsonPath("$[0].roleType", is("CHARACTER")));
	}

	@Test
	public void getAllUsers_validInput_success() throws Exception {
		User user1 = new User();
		user1.setId(1L);
		user1.setUsername("user1");
		user1.setStatus(UserStatus.ONLINE);

		User user2 = new User();
		user2.setId(2L);
		user2.setUsername("user2");
		user2.setStatus(UserStatus.OFFLINE);

		doNothing().when(userService).validateUserToken("token");
		doReturn(List.of(user1, user2)).when(userService).getUsers();

		mockMvc.perform(get("/users")
				.header("Authorization", "Bearer token")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].username", is("user1")))
				.andExpect(jsonPath("$[1].username", is("user2")));
	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}