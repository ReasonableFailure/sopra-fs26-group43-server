package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

    public User loginUser(User user){
        checkIfUserExistsByName(user.getUsername());
        checkCredentials(user);
        if(checkIfAlreadyLoggedIn(user)){
            return userRepository.findByUsername(user.getUsername());
            //throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The user %s is already logged in.", user.getUsername()));
        }
        User fromStore = userRepository.findByUsername(user.getUsername());
        fromStore.setToken(UUID.randomUUID().toString());
        fromStore.setStatus(UserStatus.ONLINE);
        fromStore = userRepository.save(fromStore);
        userRepository.flush();
        return fromStore;
    }

    public void logoutUser(Long ID, String token){
        if(!checkIfUserExistsByID(ID)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The user with ID %d cannot be found.", ID));
        }
        verifyToken(token);
        User requestsLogout = userRepository.findById(ID).orElse(null);
        requestsLogout.setToken(null);
        requestsLogout.setStatus(UserStatus.OFFLINE);
        requestsLogout = userRepository.save(requestsLogout);
        userRepository.flush();
    }

    public void updateProfile(Long userID, String token, UserPutDTO userPutDTO) {
        verifyToken(token);
        User byID = userRepository.findById(userID).orElse(null);
        if(!checkIfUserExistsByID(userID)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with ID %d cannot be found", userID));
        }
        User byToken = userRepository.findByToken(token);

        if(!(byID.equals(byToken))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,String.format("User %s may not edit other user %s", byID.getUsername(), byToken.getUsername()));
        }
        System.out.println(byID);
        byID.setPassword(userPutDTO.getNewPassword());
        logoutUser(byID.getId(), byID.getToken());
        log.debug("Changed assword of User: {}", byID);
    }

    public User createUser(User newUser) {

        if(!checkIfUserCanBeCreated(newUser)){
            String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
        }
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setCreationDate(new Date());
        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the Id
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     * throws 400
     */
    private boolean checkIfUserCanBeCreated(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
        return userByUsername == null;

    }

    private User checkIfUserExistsByName(String name){
        User user = userRepository.findByUsername(name);
        if (user==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The user %s cannot be found.", user.getUsername()));
        }
        return user;
    }

    private User checkIfUserExistsByID(Long ID){
       return userRepository.findById(ID).orElseThrow(new ResponseStatusException(HttpStatus.));
    }

    private void checkCredentials(User requestsAuth){
        //check if credentials exists
        //throws 400
        checkIfUserExistsByName(requestsAuth.getUsername());
        User userByUsername = userRepository.findByUsername(requestsAuth.getUsername());
        //check is credentials are correct
        //throws 403
        if(!userByUsername.getPassword().equals(requestsAuth.getPassword())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The password you entered was wrong!");
        }
    }

    private boolean checkIfAlreadyLoggedIn(User user){
        User byName = userRepository.findByUsername(user.getUsername());
        String token = byName.getToken();
        UserStatus status = byName.getStatus();
        if(token != null && status == UserStatus.ONLINE){
            return true;
        }
        return false;
    }

    //This is enough, since tokens are unique and only exist when the user is logged in
    public void verifyToken(String token){
        User byToken = userRepository.findByToken(token);
        if(byToken == null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This action is not authorized!");
        }
    }

}
