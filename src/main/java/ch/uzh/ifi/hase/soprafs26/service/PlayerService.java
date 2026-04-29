package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerAssignDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

import static java.util.UUID.randomUUID;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class PlayerService {

    private final RoleRepository roleRepository;
    private final PlayerRepository playerRepository;
    private final BackroomerRepository backroomerRepository;
    private final DirectorRepository directorRepository;
    private final MessageRepository messageRepository;
    private final ScenarioRepository scenarioRepository;
    private final UserService userService;
    private final int initialActionPoints = 0;

    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository, @Qualifier("roleRepository") RoleRepository roleRepository,@Qualifier("backroomerRepository") BackroomerRepository backroomerRepository,@Qualifier("directorRepository") DirectorRepository directorRepository, @Qualifier("userService") UserService userService, @Qualifier("messageRepository") MessageRepository messageRepository, @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository) {
        this.playerRepository = playerRepository;
        this.backroomerRepository = backroomerRepository;
        this.directorRepository = directorRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.scenarioRepository = scenarioRepository;
    }

    public Role getRole(String token, Long roleId)  {
        checkToken(token,"any");
        return roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role with id %d not found", roleId)));
    }

    public Role updateMessagingStats(Long roleId, int initialMessageCount){
        Role toChange = roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role with id %d not found", roleId)));
        toChange.setMessageCount(initialMessageCount);
        roleRepository.save(toChange);
        roleRepository.flush();
        return toChange;
    }

    public PlayerGetDTO updatePlayerAssociation(Long playerId, PlayerAssignDTO playerAssignDTO, String token){
        //Assigns a user to an existing Player or child class
        userService.checkIfValidToken(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), String.format("User %d cannot be assigned to player %d, this player does not exist", playerAssignDTO.getNewAssignedUserId(), playerId)));
        User newAssignee = userService.getProfileById(playerAssignDTO.getNewAssignedUserId(), token);
        newAssignee.setPlaying(true);
        player.setUser(newAssignee);
        playerRepository.save(player);
        return PlayerDTOMapper.INSTANCE.convertEntitytoPlayerGetDTO(player);
    }

    public void updateRole(String token, RolePutDTO rolePutDTO, Long roleId){
        checkToken(token, "Director");
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Character with id %d not found", roleId)));
        PlayerDTOMapper.INSTANCE.convertRolePutDTOtoEntity(rolePutDTO, role);
    }

    public Role createRole(String token, RolePostDTO rolePostDTO){
        checkToken(token, "Director");
        Scenario scenario = scenarioRepository.findById(rolePostDTO.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));
        Role newRole = PlayerDTOMapper.INSTANCE.convertRolePostDTOtoEntity(rolePostDTO);
        newRole.setAlive(true);
        newRole.setActionPoints(initialActionPoints);
        newRole.setMessageCount(scenario.getStartingMessageCount());
        newRole.setToken(randomUUID().toString());
        roleRepository.save(newRole);
        roleRepository.flush();
        scenario.addPlayer(newRole);
        scenarioRepository.save(scenario);
        return newRole;
    }

    public void deleteRole(String token, Long roleId){
        checkToken(token, "Director");
        roleRepository.deleteById(roleId);
    }

     public Backroomer createBackroomer(String userToken, PlayerAssignDTO playerAssignDTO){
       userService.checkIfValidToken(userToken);
        Backroomer b = new Backroomer();
        b.setToken(randomUUID().toString());
        b.setDelegatedCharacters(new ArrayList<Role>());
        User assignee = userService.getProfileById(playerAssignDTO.getNewAssignedUserId(), userToken);
        assignee.setPlaying(true);
        b.setUser(assignee);
        backroomerRepository.save(b);
        backroomerRepository.flush();
        return b;
    }
  
    public List<Role> getInterlocutors(String token, Long scenarioId, Long roleId) {
        userService.checkIfValidToken(token);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Character with id %d not found", roleId)
                ));
        if (!scenarioRepository.existsById(scenarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Scenario not found");
        }
        List<Message> messages =
                messageRepository.findAllByScenarioAndRole(scenarioId, roleId);
        Set<Role> interlocutors = new HashSet<>();
        for (Message m : messages) {
            if (m.getCreator().getId().equals(roleId)) {
                interlocutors.add(m.getRecipient());
            } else {
                interlocutors.add(m.getCreator());
            }
        }
        return new ArrayList<>(interlocutors);
    }

    public Director createDirector(String userToken){
        userService.checkIfValidToken(userToken);
        Director d = new Director();
        d.setToken(randomUUID().toString());
        d.setUser(userService.getByToken(userToken));
        directorRepository.save(d);
        directorRepository.flush();
        return d;
    }

    public void addLikes(String token, Long characterId, int incrementBy){
        checkToken(token, "Role");
        Role role = roleRepository.findById(characterId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role with ID %d cannot be found", characterId)));
        role.gainActionPoints(incrementBy);
    }



    public void checkToken(String token, @NonNull String type){
        Role toReturn = roleRepository.findByToken(token);
        Backroomer toReturnBackroomer = backroomerRepository.findByToken(token);
        Director toReturnDirector = directorRepository.findByToken(token);
        Player toReturnPlayer = playerRepository.findByToken(token);
        if(type.equals("Role") && toReturn == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The Role token is unknown");
        } else if(type.equals("Backroomer") && toReturnBackroomer == null && toReturnDirector == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The Backroomer token is unknown");
        } else if (type.equals("Director") && toReturnDirector == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The Director token is unknown");
        } else if (type.equals("any") && toReturnPlayer == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The Player token is unknown");
        }
    }

}
