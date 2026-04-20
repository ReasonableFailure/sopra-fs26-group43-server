package ch.uzh.ifi.hase.soprafs26.service;

//import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;
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

	public List<User> getUsers(String token) {
        checkIfValidToken(token);
		return this.userRepository.findAll();
	}

    public User getProfile(Long idToBeFound, String authToken){
        checkIfValidToken(authToken);
        return userRepository.findById(idToBeFound).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with id %d not found", idToBeFound)));
    }

    public User createUser(User newUser) {
        if(!isValidProfileData(newUser.getUsername(),newUser.getPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid username or password"));
        }
        if(checkIfUsernameTaken(newUser.getUsername())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Username already taken"));
        }
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setCreationDate(new Date());
        newUser.setPlaying(false);
        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User loginUser(User user){
        if(!isValidProfileData(user.getUsername(),user.getPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid username or password"));
        }
        if(!checkPwd(user.getUsername(),user.getPassword())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid token"));
        }
        if(!checkIfUserExistsByID(user.getId())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with id %d not found", user.getId()));
        }
        User fromStore = userRepository.findByUsername(user.getUsername());
        fromStore.setToken(UUID.randomUUID().toString());
        fromStore.setStatus(UserStatus.ONLINE);
        fromStore = userRepository.save(fromStore);
        userRepository.flush();
        return fromStore;
    }

    public void logoutUser(Long ID, String token){
        //200, 401, 404
        checkIfValidToken(token);
        if(!checkIfUserExistsByID(ID)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with id %d not found", ID));
        }
        User requestsLogout = userRepository.findByToken(token);
        requestsLogout.setStatus(UserStatus.OFFLINE);
        requestsLogout.setToken(null);
        requestsLogout.setPlaying(false);
        userRepository.save(requestsLogout);
        userRepository.flush();
    }

    public void updateProfile(String token, Long id, User holdsUpdate) {
        checkIfValidToken(token);
        if(!checkIfUserExistsByID(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with id %d not found", id));
        }
        if(!isValidProfileData(holdsUpdate.getUsername(),holdsUpdate.getPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid username or password"));
        }
        User toBeModified = userRepository.findById(id).get();
        toBeModified.setUsername(holdsUpdate.getUsername());
        toBeModified.setPassword(holdsUpdate.getPassword());
        toBeModified.setBio(holdsUpdate.getBio());
        userRepository.save(toBeModified);
        userRepository.flush();
    }

    /*List of Failure conditions:
    * Invalid profile input data || Done
    * wrong password Done
    * username already exists Done
    * unauthorised token |||| Done
    * userid does not exist |||| Done
    * invalid data in update Done
    * */

    private boolean isValidProfileData(String uname, String pwd){
        //TODO: in future: enforce pwd rules?
        boolean isValid = true;
        if(uname == null || pwd == null){
            //fields must exist
            isValid = false;
        }
        if(uname.isEmpty() || pwd.isEmpty()){
            //fields may not be empty
            isValid = false;
        }
        if(uname.isBlank() || pwd.isBlank()){
            //fields may not be just whitespace
            isValid = false;
        }
        return isValid;
    }

    private boolean checkIfUsernameTaken(String uname){
        User foundByUname = userRepository.findByUsername(uname);
        if (foundByUname == null) {
            return false;
        }
        return true;
    }
    /*
    * pre-condition: user must exist*/
    private boolean checkPwd(String uname, String pwd){
        User foundByUname = userRepository.findByUsername(uname);
        if (foundByUname.getPassword().equals(pwd)){
            return true;
        }
        return false;
    }

    private boolean checkIfUserExistsByID(Long ID){
        User foundById = userRepository.findById(ID).orElse(null);
        return foundById != null;
    }

    protected void checkIfValidToken(String token){
        if (token == null || token.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid token"));
        User foundByToken = userRepository.findByToken(token);
        if (foundByToken.getToken() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid token"));
    }

}
