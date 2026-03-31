package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

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
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

    @GetMapping("/users/{userid}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO retrieveUser(@PathVariable Long userid, @RequestHeader("Authorization") String token){
        User user = userService.getProfile(userid,token);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PutMapping("/users/{userid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@PathVariable Long userid, @RequestHeader("Authorization") String token, @RequestBody UserPutDTO userPutDTO){
        User holdsUpdateData = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);
        userService.updateProfile(token, userid, holdsUpdateData);
        return;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO loginUser(@RequestBody UserLoginDTO userLoginDTO){
        User toLogin = DTOMapper.INSTANCE.convertUserLoginDTOtoEntity(userLoginDTO);
        User user = userService.loginUser(toLogin);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PostMapping("/logout/{userid}")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@PathVariable Long userid, @RequestHeader("Authorization") String token){
        userService.logoutUser(userid,token);
        return;
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers(@RequestHeader("Authorization") String token) {
        // fetch all users in the internal representation
        List<User> users = userService.getUsers(token);
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }
}
