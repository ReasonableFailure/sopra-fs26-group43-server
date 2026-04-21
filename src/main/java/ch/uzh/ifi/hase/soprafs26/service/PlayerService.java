package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PlayerService {

    private final RoleRepository roleRepository;
    private final PlayerRepository playerRepository;
    private final BackroomerRepository backroomerRepository;
    private final DirectorRepository directorRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final int initialActionPoints = 0;

    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository, @Qualifier("roleRepository") RoleRepository roleRepository,@Qualifier("backroomerRepository") BackroomerRepository backroomerRepository,@Qualifier("directorRepository") DirectorRepository directorRepository, @Qualifier("userService") UserService userService, @Qualifier("messageRepository") MessageRepository messageRepository) {
        this.playerRepository = playerRepository;
        this.backroomerRepository = backroomerRepository;
        this.directorRepository = directorRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.messageRepository = messageRepository;
    }

    public Role getRole(String token, Long roleId)  {
        userService.checkIfValidToken(token);
        return roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role with id %d not found", roleId)));
    }

    public void updatePlayerAssociation(Long playerId, PlayerPutDTO playerPutDTO, String token){
        userService.checkIfValidToken(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), String.format("User %d cannot be assigned to player %d, this player does not exist", playerPutDTO.getNewAssignedUserId(), playerId)));
        player = PlayerDTOMapper.INSTANCE.convertPlayerPutDTOtoEntity(playerPutDTO, player);
        playerRepository.save(player);
    }

    public void updateRole(String token, RolePutDTO rolePutDTO, Long roleId){
        userService.checkIfValidToken(token);
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Character with id %d not found", roleId)));
        PlayerDTOMapper.INSTANCE.convertRolePutDTOtoEntity(rolePutDTO, role);
    }

    public Role createRole(String token, RolePostDTO rolePostDTO){
        checkDirectorToken(token);
        userService.checkIfValidToken(token);
        Role newRole = PlayerDTOMapper.INSTANCE.convertRolePostDTOtoEntity(rolePostDTO);
        newRole.setAlive(true);
        newRole.setActionPoints(initialActionPoints);
        newRole.setMessageCount(15);
        roleRepository.save(newRole);
        roleRepository.flush();//magic number comes from user story
        return newRole;
    }

    public void deleteRole(String token, Long roleId){
        userService.checkIfValidToken(token);
        roleRepository.deleteById(roleId);
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

    protected void checkDirectorToken(String token){

    }

    protected void checkBackroomerToken(String token){

    }

    protected void checkRoleToken(String token){

    }

}
