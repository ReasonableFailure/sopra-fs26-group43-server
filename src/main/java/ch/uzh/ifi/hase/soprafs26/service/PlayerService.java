package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerGetDTO;
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
import java.util.UUID;
import java.util.ArrayList;

import static java.util.UUID.randomUUID;

@Service
@Transactional
public class PlayerService {

    private final RoleRepository roleRepository;
    private final PlayerRepository playerRepository;
    private final BackroomerRepository backroomerRepository;
    private final DirectorRepository directorRepository;
    private final UserService userService;
    private final int initialActionPoints = 0;

    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository, @Qualifier("roleRepository") RoleRepository roleRepository,@Qualifier("backroomerRepository") BackroomerRepository backroomerRepository,@Qualifier("directorRepository") DirectorRepository directorRepository, @Qualifier("userService") UserService userService) {
        this.playerRepository = playerRepository;
        this.backroomerRepository = backroomerRepository;
        this.directorRepository = directorRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
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

    public PlayerGetDTO updatePlayerAssociation(Long playerId, PlayerPutDTO playerPutDTO, String token){
        //Assigns a user to an existing Player or child class
        checkToken(token,"any");
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), String.format("User %d cannot be assigned to player %d, this player does not exist", playerPutDTO.getNewAssignedUserId(), playerId)));
        player = PlayerDTOMapper.INSTANCE.convertPlayerPutDTOtoEntity(playerPutDTO, player);
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
        Role newRole = PlayerDTOMapper.INSTANCE.convertRolePostDTOtoEntity(rolePostDTO);
        newRole.setAlive(true);
        newRole.setActionPoints(initialActionPoints);
        newRole.setMessageCount((Integer) null);
        newRole.setAuthToken(randomUUID().toString());
        roleRepository.save(newRole);
        roleRepository.flush();//magic number comes from user story
        return newRole;
    }

    public void deleteRole(String token, Long roleId){
        checkToken(token, "Director");
        roleRepository.deleteById(roleId);
    }

    public Backroomer createBackroomer(String userToken){
        userService.checkIfValidToken(userToken);
        Backroomer b = new Backroomer();
        b.setAuthToken(randomUUID().toString());
        b.setDelegatedCharacters(new ArrayList<Role>());
        backroomerRepository.save(b);
        backroomerRepository.flush();
        return b;
    }
    public Director createDirector(String userToken){
        userService.checkIfValidToken(userToken);
        Director d = new Director();
        d.setAuthToken(randomUUID().toString());
        d.setUser(userService.getByToken(userToken));
        directorRepository.save(d);
        directorRepository.flush();
        return d;
    }

    protected void checkToken(String token, String type){
        Role toReturn = roleRepository.findByToken(token);
        Backroomer toReturnBackroomer = backroomerRepository.findByToken(token);
        Director toReturnDirector = directorRepository.findByToken(token);
        Player toReturnPlayer = playerRepository.findByToken(token);
        if(type.equals("Role") && toReturn == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else if(type.equals("Backroomer") && toReturnBackroomer == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else if (type.equals("Director") && toReturnDirector == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else if (type.equals("any") && toReturnPlayer == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

}
