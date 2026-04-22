package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.UserDTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = UserDTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return UserDTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

    @GetMapping("/users/{userid}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO retrieveUser(@PathVariable Long userid, @RequestHeader("Authorization") String token){
        String strippedToken = stripPrefix(token);
        User user = userService.getProfileById(userid,strippedToken);
        return UserDTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PutMapping("/users/{userid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@PathVariable Long userid, @RequestHeader("Authorization") String token, @RequestBody UserPutDTO userPutDTO){
        User holdsUpdateData = UserDTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);
        String strippedToken = stripPrefix(token);
        userService.updateProfile(strippedToken, userid, holdsUpdateData);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO loginUser(@RequestBody UserLoginDTO userLoginDTO){
        User toLogin = UserDTOMapper.INSTANCE.convertUserLoginDTOtoEntity(userLoginDTO);
        User user = userService.loginUser(toLogin);
        return UserDTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PostMapping("/logout/{userid}")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@PathVariable Long userid, @RequestHeader("Authorization") String token){
        String strippedToken = stripPrefix(token);
        userService.logoutUser(userid,strippedToken);
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers(@RequestHeader("Authorization") String token) {
        // fetch all users in the internal representation
        String strippedToken = stripPrefix(token);
        List<User> users = userService.getUsers(strippedToken);
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(UserDTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    private String stripPrefix(String token){
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        } else throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
}
