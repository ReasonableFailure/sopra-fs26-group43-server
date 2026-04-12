package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerPutDTO;
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
    private final UserRepository userRepository;

    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(@Qualifier("playerRepository") RoleRepository playerRepository, @Qualifier("userRepository") UserRepository userRepository) {
        this.roleRepository = playerRepository;
        this.userRepository = userRepository;
    }

    public Player getPlayer(String token, Long roleId){
        if(!checkIfValidToken(token)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access to character profile");
        }
        return roleRepository.findById(roleId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Character with id %d not found", roleId)));
    }

    public void updatePlayerAssociation(Long playerId, PlayerPutDTO playerPutDTO, String token){
        if(!checkIfValidToken(token)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access to character profile");
        }
        Player player = roleRepository.findById(playerId).orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),String.format("User %d cannot be assigned to player %d, this player does not exist", playerPutDTO.getNewAssignedUserId(), playerId)));

        player = PlayerDTOMapper.INSTANCE.convertPlayerPutDTOtoEntity(playerPutDTO, player);
        roleRepository.save(player);
    }

    public void updateRole(String token){
        if(!checkIfValidToken(token)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access to character profile");
        }
    }

    private boolean checkIfValidToken(String token){
        User foundByToken = userRepository.findByToken(token);
        return  foundByToken.getToken() != null;
    }
}
