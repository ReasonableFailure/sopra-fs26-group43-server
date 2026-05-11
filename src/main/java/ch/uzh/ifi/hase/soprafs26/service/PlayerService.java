package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerPutDTO;
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

    private final NewsRepository newsRepository;
    private final RoleRepository roleRepository;
    private final PlayerRepository playerRepository;
    private final BackroomerRepository backroomerRepository;
    private final DirectorRepository directorRepository;
    private final MessageRepository messageRepository;
    private final ScenarioRepository scenarioRepository;
    private final UserService userService;
    private final MastodonClient mastodonClient;
    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository, @Qualifier("roleRepository") RoleRepository roleRepository,@Qualifier("backroomerRepository") BackroomerRepository backroomerRepository,@Qualifier("directorRepository") DirectorRepository directorRepository, @Qualifier("userService") UserService userService, @Qualifier("messageRepository") MessageRepository messageRepository, @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository, @Qualifier("newsRepository") NewsRepository newsRepository, @Qualifier("mastodonClient") MastodonClient mastodonClient) {
        this.playerRepository = playerRepository;
        this.backroomerRepository = backroomerRepository;
        this.directorRepository = directorRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.scenarioRepository = scenarioRepository;
        this.newsRepository = newsRepository;
        this.mastodonClient = mastodonClient;
    }

    public Role getRoleById( Long roleId)  {

        return roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role with id %d not found", roleId)));
    }

    public Director getDirectorByToken(String directorToken){

        return directorRepository.findByToken(directorToken).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This director does not exist"));
    }

    public Role updateMessagingStats(Long roleId, int initialMessageCount){
        Role toChange = roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role with id %d not found", roleId)));
        toChange.setMessageCount(initialMessageCount);
        roleRepository.save(toChange);
        roleRepository.flush();
        return toChange;
    }

    public Player updatePlayerAssociation(Long playerId, PlayerPutDTO playerPutDTO){
        //Assigns a user to an existing Player or child class
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), String.format("User %d cannot be assigned to player %d, this player does not exist", playerPutDTO.getNewAssignedUserId(), playerId)));
        player = PlayerDTOMapper.INSTANCE.convertPlayerPutDTOtoEntity(playerPutDTO, player);
        playerRepository.save(player);return player;
    }

    public void updateRole(RolePutDTO rolePutDTO, Long roleId){

        Role r = getRoleById( roleId);

        if (rolePutDTO.getName() != null) {
            r.setName(rolePutDTO.getName());
        }

        if (rolePutDTO.getTitle() != null) {
            r.setTitle(rolePutDTO.getTitle());
        }

        if (rolePutDTO.getDescription() != null) {
            r.setDescription(rolePutDTO.getDescription());
        }

        if (rolePutDTO.getPortrait() != null) {
            r.setPortrait(rolePutDTO.getPortrait());
        }

        if (rolePutDTO.getSecret() != null) {
            r.setSecret(rolePutDTO.getSecret());
        }

        if (rolePutDTO.isAlive() != null) {
            r.setAlive(rolePutDTO.isAlive());
        }

        roleRepository.save(r);
        roleRepository.flush();
    }

    public Role createRole( RolePostDTO rolePostDTO){
        Scenario scenario = scenarioRepository.findById(rolePostDTO.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));
        Role newRole = PlayerDTOMapper.INSTANCE.convertRolePostDTOtoEntity(rolePostDTO);
        newRole.setScenario(scenario);
        newRole.setAlive(true);
        newRole.setTotalPoints(0);
        newRole.setPointsBalance(0);
        newRole.setMessageCount(scenario.getStartingMessageCount());
        newRole.setToken(randomUUID().toString());
        newRole.setNumberDirectives(0);
        newRole.setNumberMessages(0);
        newRole.setNumberPronouncements(0);
        newRole.setTotalTextLength(0);
        roleRepository.save(newRole);
        roleRepository.flush();
        scenario.addPlayer(newRole);
        scenarioRepository.save(scenario);
        return newRole;
    }

    public void deleteRole(Long roleId){

        roleRepository.deleteById(roleId);
    }

    public Backroomer createBackroomer(PlayerPutDTO playerPutDTO){
        Backroomer b = new Backroomer();
        b.setToken(randomUUID().toString());
        b.setUser(userService.getProfileById(playerPutDTO.getNewAssignedUserId()));
        b.setDelegatedCharacters(new ArrayList<Role>());
        backroomerRepository.save(b);
        backroomerRepository.flush();
        b = (Backroomer) updatePlayerAssociation(b.getId(), playerPutDTO);
        return b;
    }
  
    public List<Role> getInterlocutors(String token, Long scenarioId, Long roleId) {
        //Where is this endpoint accessed by logged-in users?
        userService.validateUserToken(token);
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
//    public Role claimCharacter(String userToken, Long scenarioId, Long characterId) {
//        userService.validateUserToken(userToken);
//        User user = userService.getByToken(userToken);
//        Scenario scenario = scenarioRepository.findById(scenarioId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));
//        if (scenario.getStatus() == ScenarioStatus.COMPLETED) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Scenario has ended");
//        }
//        if (playerRepository.findFirstByUser_IdAndScenario_Id(user.getId(), scenarioId).isPresent()) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already engaged in this scenario");
//        }
//        Role role = roleRepository.findById(characterId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));
//        if (role.getUser() != null) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Character already taken");
//        }
//        role.setUser(user);
//        if (role.getToken() == null) {
//            role.setToken(randomUUID().toString());
//        }
//        return roleRepository.save(role);
//    }
//
//    public Backroomer becomeBackroomer(Long userId, Long scenarioId) {
//        Scenario scenario = scenarioRepository.findById(scenarioId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));
//        if (scenario.getStatus() == ScenarioStatus.COMPLETED) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Scenario has ended");
//        }
//        if (playerRepository.findFirstByUser_IdAndScenario_Id(userId, scenarioId).isPresent()) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already engaged in this scenario");
//        }
//        Backroomer b = new Backroomer();
//        User user = userService.getProfileById(userId);
//        b.setUser(user);
//        b.setScenario(scenario);
//        b.setToken(randomUUID().toString());
//        b.setDelegatedCharacters(new ArrayList<Role>());
//        return backroomerRepository.save(b);
//    }

    public Director createDirector(Long userId){
        Director d = new Director();
        d.setToken(randomUUID().toString());
        System.out.println("The user to be assigned is:" + userId);
        d.setUser(userService.getProfileById(userId));
        System.out.println("user has been found and assigned");
        directorRepository.save(d);
        directorRepository.flush();
        return d;
    }

    @Transactional
    public Role syncPointsAndGetRole(Long scenarioId, Long characterId) {
        Role role = getRoleById(characterId);
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        syncActionPoints(role, scenario);
        return roleRepository.save(role);
    }

    public void syncActionPoints(Role role, Scenario scenario) {
        List<Pronouncement> pronouncements = newsRepository.findPronouncementsByAuthorIdAndScenarioId(role.getId(), scenario.getId());
        int newTotal = 0;
        for (Pronouncement p : pronouncements) {
            Integer likes = mastodonClient.getLikes(scenario.getMastodonBaseUrl(), scenario.getMastodonAccessToken(), p.getMastodonStatusId());
            if (likes == null) {
                likes = 0;
            }
            newTotal += likes;
        }
        int oldTotal = role.getTotalPoints();
        if (newTotal > oldTotal) {
            int delta = newTotal - oldTotal;
            role.setTotalPoints(newTotal);
            role.setPointsBalance(role.getPointsBalance() + delta);
        } else {
            role.setTotalPoints(newTotal);
        }
        roleRepository.save(role);
    }

    @Transactional
    public Role buyMessage(Long scenarioId, Long characterId) {

        Role role = roleRepository.findById(characterId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Character not found"));

        Scenario scenario = scenarioRepository.findById(scenarioId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Scenario not found"));

        syncActionPoints(role, scenario);

        try {
            role.buyMessages(scenario.getExchangeRate(), 1);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }

        return roleRepository.save(role);
    }

    public String validate(String fromHeader, String intendedRole) {
        if(fromHeader == null || fromHeader.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is empty");
        }
        String[] tokens = fromHeader.split(" ");
        if (!tokens[0].equalsIgnoreCase(intendedRole) && !intendedRole.equalsIgnoreCase("any")) { //allow any of the player roles through
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your role in the scenario is not permitted to perform this action!");
        }else{
            if(tokens[0].equalsIgnoreCase("director")){
                Director director = directorRepository.findByToken(tokens[0]).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This director player does not exist"));
                if(!director.getToken().equals(tokens[1])){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong director player token");
                }
            } else if(tokens[0].equalsIgnoreCase("backroomer")){
                Backroomer backroomer = backroomerRepository.findByToken(tokens[0]).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "This backroomer player does not exist"));
                if(!backroomer.getToken().equals(tokens[1])){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong backroomer player token");
                }
            } else if(tokens[0].equalsIgnoreCase("role")) {
                Role role = roleRepository.findByToken(tokens[1]).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This character player does not exist"));
                if (!role.getToken().equals(tokens[0])) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong character player token");
                }
            } else if(tokens[0].equalsIgnoreCase("any")) {
                Player player = playerRepository.findByToken(tokens[1]).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This player does not exist"));
                if(!player.getToken().equals(tokens[0])){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong player token");
                }
            } else if(tokens[0].equalsIgnoreCase("Bearer")){
                userService.validateUserToken(tokens[1]);
            }
        }
        return tokens[1];
    }
}
