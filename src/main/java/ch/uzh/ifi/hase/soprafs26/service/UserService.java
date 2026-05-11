package ch.uzh.ifi.hase.soprafs26.service;

//import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.EngagementGetDTO;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
	private final PlayerRepository playerRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository,
	                   @Qualifier("playerRepository") PlayerRepository playerRepository) {
		this.userRepository = userRepository;
		this.playerRepository = playerRepository;
	}

	public List<User> getUsers(String token) {
        checkIfValidToken(token);
		return this.userRepository.findAll();
	}

    public User getProfileById(Long idToBeFound, String authToken){
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
        User fromStore = userRepository.findByUsername(user.getUsername());
        if(fromStore == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with username %s not found", user.getUsername()));
        }
        if(!checkPwd(user.getUsername(),user.getPassword())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid token"));
        }
        fromStore.setToken(UUID.randomUUID().toString());
        fromStore.setStatus(UserStatus.ONLINE);
        fromStore = userRepository.save(fromStore);
        userRepository.flush();
        return fromStore;
    }

    public void logoutUser(Long ID, String token){
        //200, 401, 403, 404
        checkIfValidToken(token);
        User requestsLogout = userRepository.findByToken(token);
        if (requestsLogout == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (!requestsLogout.getId().equals(ID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Path user does not match token holder");
        }
        requestsLogout.setStatus(UserStatus.OFFLINE);
        requestsLogout.setToken(null);
        requestsLogout.setPlaying(false);
        userRepository.save(requestsLogout);
        userRepository.flush();
    }

    public void deleteUser(Long ID, String token){
        checkIfValidToken(token);
        User requester = userRepository.findByToken(token);
        if (requester == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (!requester.getId().equals(ID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another user");
        }
        userRepository.deleteById(ID);
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

    protected User getByToken(String token){
        checkIfValidToken(token);
        return userRepository.findByToken(token);
    }

    protected void checkIfValidToken(String token){
        if (token == null || token.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid token"));
        User foundByToken = userRepository.findByToken(token);
        if (foundByToken == null || foundByToken.getToken() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid token"));
    }

    public void validateUserToken(String token) {
        checkIfValidToken(token);
    }

    public List<EngagementGetDTO> getEngagements(String token, Long userId) {
        checkIfValidToken(token);
        if (!checkIfUserExistsByID(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with id %d not found", userId));
        }
        List<Player> players = playerRepository.findByUser_Id(userId);
        return players.stream()
            .map(this::toEngagementDTO)
            .filter(dto -> dto.getScenarioId() != null)
            .sorted(engagementComparator())
            .collect(Collectors.toList());
    }

    private EngagementGetDTO toEngagementDTO(Player player) {
        EngagementGetDTO dto = new EngagementGetDTO();
        dto.setPlayerId(player.getId());
        Scenario scenario = player.getScenario();
        if (scenario != null) {
            dto.setScenarioId(scenario.getId());
            dto.setScenarioTitle(scenario.getTitle());
            dto.setScenarioStatus(scenario.getStatus());
            dto.setFinishTime(scenario.getFinishTime());
        }
        if (player instanceof Director) {
            dto.setRoleType("DIRECTOR");
        } else if (player instanceof Role) {
            dto.setRoleType("CHARACTER");
            dto.setCharacterName(((Role) player).getName());
        } else if (player instanceof Backroomer) {
            dto.setRoleType("BACKROOMER");
        }
        return dto;
    }

    private Comparator<EngagementGetDTO> engagementComparator() {
        Comparator<EngagementGetDTO> byCompletedLast = Comparator.comparing(
            dto -> dto.getScenarioStatus() == ScenarioStatus.COMPLETED);
        Comparator<EngagementGetDTO> byFinishTimeDesc = Comparator.comparing(
            EngagementGetDTO::getFinishTime,
            Comparator.nullsLast(Comparator.reverseOrder()));
        return byCompletedLast.thenComparing(byFinishTimeDesc);
    }

}
