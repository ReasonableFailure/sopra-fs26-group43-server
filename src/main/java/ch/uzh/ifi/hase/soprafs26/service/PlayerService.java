package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserAssignDTO;
import io.netty.handler.codec.http2.Http2PushPromiseFrame;
import jakarta.transaction.Transactional;
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
    private final ActionPointService actionPointService;
    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository, 
    @Qualifier("roleRepository") RoleRepository roleRepository,
    @Qualifier("backroomerRepository") BackroomerRepository backroomerRepository,
    @Qualifier("directorRepository") DirectorRepository directorRepository, 
    @Qualifier("userService") UserService userService, 
    @Qualifier("messageRepository") MessageRepository messageRepository, 
    @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository, 
    @Qualifier("newsRepository") NewsRepository newsRepository, 
    @Qualifier("mastodonClient") MastodonClient mastodonClient,
    @Qualifier("actionPointService") ActionPointService actionPointService
) {
        this.playerRepository = playerRepository;
        this.backroomerRepository = backroomerRepository;
        this.directorRepository = directorRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.scenarioRepository = scenarioRepository;
        this.newsRepository = newsRepository;
        this.mastodonClient = mastodonClient;
        this.actionPointService = actionPointService;
    }

    public Role getRoleById( Long roleId)  {
        return roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role with id %d not found", roleId)));
    }

    public Director getDirectorByID(Long id){
        return directorRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Director " + id + " not found in repository"));
    }

    public Role updateMessagingStats(Long roleId, int initialMessageCount){
        Role toChange = roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role with id %d not found", roleId)));
        toChange.setMessageCount(initialMessageCount);
        roleRepository.save(toChange);
        roleRepository.flush();
        return toChange;
    }

    public Player updatePlayerAssociation(Long playerId, UserAssignDTO userAssignDTO){
        //Assigns a user to an existing Player or child class
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), String.format("User cannot be assigned to player %d, this player does not exist", playerId)));
        player.setUser(userService.getProfileById(userAssignDTO.getId()));
        playerRepository.save(player);
        playerRepository.flush();
        return player;
    }

    public void updateRole(Role role, Long roleId){

        Role r = getRoleById(roleId);
        if (role.getName() != null) {
            r.setName(role.getName());
        }
        if (role.getTitle() != null) {
            r.setTitle(role.getTitle());
        }
        if (role.getDescription() != null) {
            r.setDescription(role.getDescription());
        }
        if (role.getPortrait() != null) {
            r.setPortrait(role.getPortrait());
        }
        if (role.getSecret() != null) {
            r.setSecret(role.getSecret());
        }
        if (role.getAlive()) {
            r.setAlive(role.getAlive());
        }
        roleRepository.save(r);
        roleRepository.flush();
    }

    public Role createRole( RolePostDTO rolePostDTO){
        Scenario scenario = scenarioRepository.findById(rolePostDTO.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));
        Role newRole = PlayerDTOMapper.INSTANCE.convertRolePostDTOtoEntity(rolePostDTO);
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

    public Backroomer createBackroomer(UserAssignDTO userAssignDTO, long scenarioId){
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));
        Backroomer b = new Backroomer();
        b.setToken(randomUUID().toString());
        backroomerRepository.save(b);
        backroomerRepository.flush();
        scenario.addPlayer(b);
        scenarioRepository.save(scenario);
        b = (Backroomer) updatePlayerAssociation(b.getId(), userAssignDTO);
        return b;
    }
  
    public List<Role> getInterlocutors(String token, Long scenarioId, Long roleId) {
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

    public Director createDirector(Long userId){
        //userService.validateUserToken(userToken);
        //System.out.println("In createDirector Function");
        Director d = new Director();
        d.setToken(randomUUID().toString());
        //System.out.println(userId);
        d.setUser(userService.getProfileById(userId));
        //System.out.println("new entity successfully created");
        directorRepository.save(d);
        directorRepository.flush();
        return d;
    }

    public String validate(String fromHeader, String intendedRole) {
        if (fromHeader == null || fromHeader.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is empty");
        }
        String[] parts = fromHeader.split(" ", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token format must be '<Type> <token>'");
        }
        String type = parts[0];
        String rawToken = parts[1];

        boolean typeOk = type.equalsIgnoreCase(intendedRole) || intendedRole.equalsIgnoreCase("any");
        if (!typeOk) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Your role is not permitted to perform this action");
        }

        if (type.equalsIgnoreCase("Director")) {
            if (directorRepository.findByToken(rawToken) == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid director token");
            }
        } else if (type.equalsIgnoreCase("Backroomer")) {
            if (backroomerRepository.findByToken(rawToken) == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid backroomer token");
            }
        } else if (type.equalsIgnoreCase("Role")) {
            if (roleRepository.findByToken(rawToken) == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid role token");
            }
        } else if (type.equalsIgnoreCase("Bearer")) {
            userService.validateUserToken(rawToken);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown token type: " + type);
        }
        return rawToken;
    }

    @Transactional
    public Role syncPointsAndGetRole(Long scenarioId, Long characterId) {

        Role role = getRoleById(characterId);

        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        actionPointService.syncActionPoints(role, scenario);

        return roleRepository.save(role);
    }

    public void consumeMessageSlot(Role role) {
        role.useMessageSlot();
        roleRepository.save(role);
    }
}
