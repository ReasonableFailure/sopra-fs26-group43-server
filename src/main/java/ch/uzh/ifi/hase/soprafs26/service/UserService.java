package ch.uzh.ifi.hase.soprafs26.service;

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
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;

import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private static final String USER_NOT_FOUND = "User with id %d not found";

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User getProfileById(Long idToBeFound) {
        if(idToBeFound != null){
        return userRepository.findById(idToBeFound)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(USER_NOT_FOUND, idToBeFound)));} else {
            System.out.println("id for finding user is null");return new User();}

   }

    public User createUser(User newUser) {
        if(!isValidProfileData(newUser.getUsername(),newUser.getPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username or password");
        }
        checkIfUsernameTaken(newUser.getUsername());
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

    public User loginUser(User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password required");
        }
        User fromStore = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!fromStore.getPassword().equals(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        fromStore.setToken(UUID.randomUUID().toString());
        fromStore.setStatus(UserStatus.ONLINE);
        fromStore = userRepository.save(fromStore);
        userRepository.flush();
        return fromStore;
    }

    public void logoutUser(Long id) {
        User requestsLogout = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(USER_NOT_FOUND, id)));
        requestsLogout.setStatus(UserStatus.OFFLINE);
        requestsLogout.setToken(null);
        requestsLogout.setPlaying(false);
        userRepository.save(requestsLogout);
        userRepository.flush();
    }

    public void deleteUser(Long id, String token) {
        validateUserToken(token);
        User requester = userRepository.findByToken(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,String.format(USER_NOT_FOUND, id)));

        if (!requester.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another user");
        }
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USER_NOT_FOUND, id));
        }
        userRepository.deleteById(id);
        userRepository.flush();
    }

    /**
     * Merge-update a user profile from a UserPutDTO. Only fields that
     * are explicitly present (non-null) are applied; missing fields keep
     * their current value. A null `name`/`bio` keeps the prior value
     * (callers should send empty string "" to clear those text fields).
     * For `profilePic`, an empty string clears the pic; a data URL or
     * raw base64 sets it.
     */
    public void updateProfile(Long id, UserPutDTO dto) {
        User toBeModified = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(USER_NOT_FOUND, id)));

        if (dto.getUsername() != null) {
            if (dto.getUsername().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be blank");
            }
            if (!dto.getUsername().equals(toBeModified.getUsername())) {
                checkIfUsernameTaken(dto.getUsername());
            }
            toBeModified.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null) {
            if (dto.getPassword().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be blank");
            }
            toBeModified.setPassword(dto.getPassword());
        }
        if (dto.getBio() != null) {
            toBeModified.setBio(dto.getBio());
        }
        if (dto.getName() != null) {
            toBeModified.setName(dto.getName());
        }
        if (dto.getProfilePic() != null) {
            toBeModified.setProfilePic(decodeProfilePic(dto.getProfilePic()));
        }
        userRepository.save(toBeModified);
        userRepository.flush();
    }

    /**
     * Decode a profile-pic data-URL ("data:image/jpeg;base64,XXX") or bare
     * base64 string into the raw bytes we store in `User.profilePic`. An
     * empty string returns null — the caller's way of clearing the pic.
     */
    private byte[] decodeProfilePic(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return null;
        String data = trimmed;
        int comma = trimmed.indexOf(',');
        if (trimmed.startsWith("data:") && comma > 0) {
            data = trimmed.substring(comma + 1);
        }
        try {
            return Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "profilePic is not a valid base64 image payload");
        }
    }

    /*List of Failure conditions:
    * Invalid profile input data || Done
    * wrong password Done
    * username already exists Done
    * unauthorised token |||| Done
    * userid does not exist |||| Done
    * invalid data in update Done
    * */
    private void checkIfUsernameTaken(String username) {
        if(userRepository.findByUsername(username).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username " + username + " taken!");
        }
    }

    private boolean isValidProfileData(String uname, String pwd){

        boolean isValid = uname != null && pwd != null;
        //fields must exist
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

    public boolean checkIfUserExistsByID(Long id) {
        return userRepository.findById(id).isPresent();
    }

    public User getByToken(String token){
        validateUserToken(token);
        return userRepository.findByToken(token).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with this token not found(2)")));
    }

    public void validateUserToken(String token){
        if (token == null || token.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Token not there"));
        User foundByToken = userRepository.findByToken(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("This is not a valid token")));
        if (foundByToken.getToken() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("This token is not associated with any user."));
        if(!token.equals(foundByToken.getToken())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Wrong token"));
    }


    public List<EngagementGetDTO> getEngagements(String token, Long userId) {
        validateUserToken(token);
        if (!checkIfUserExistsByID(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(USER_NOT_FOUND, userId));
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
        dto.setToken(player.getToken());
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
