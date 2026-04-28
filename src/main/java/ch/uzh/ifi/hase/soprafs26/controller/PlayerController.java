package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.*;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PutMapping("/player/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    public void assignUserToPlayer(@PathVariable Long playerId, @RequestBody PlayerPutDTO playerPutDTO,  @RequestHeader("Authorization") String token) {
        String[] tokens = splitToken(token);
        if(tokens[0].equals("Bearer")){
            playerService.updatePlayerAssociation(playerId,playerPutDTO,tokens[1]);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/characters")
    @ResponseStatus(HttpStatus.OK)
    public RoleGetDTO createRole(@RequestBody RolePostDTO rolePostDTO, @RequestHeader("Authorization") String authToken){
        String[] tokens = splitToken(authToken);
        if(tokens[0].equals("Director")){
            Role role = playerService.createRole(tokens[1], rolePostDTO);
            return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(role);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/backroomers")
    @ResponseStatus(HttpStatus.OK)
    public PlayerGetDTO createBackroomer(@RequestHeader("Authorization") String token, PlayerPutDTO playerPutDTO){
        String[] tokens = splitToken(token);
        if(tokens[0].equals("Bearer")){
            Backroomer b = playerService.createBackroomer(tokens[1]);
            return PlayerDTOMapper.INSTANCE.convertEntitytoPlayerGetDTO(b);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("/characters/{characterId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteRole(@RequestHeader("Authorization")String token, @PathVariable Long characterId){
        String[] tokens = splitToken(token);
        if (tokens[0].equals("Director")){
            playerService.deleteRole(token,characterId);
        } else throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    @PutMapping("/characters/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateRole(@RequestBody RolePutDTO rolePutDTO, @RequestHeader("Authorization") String token, @PathVariable Long characterId) {
        String[] tokens = splitToken(token);
        if(tokens[0].equals("Director")){
            playerService.updateRole(tokens[1], rolePutDTO,characterId);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/character/{characterId}/points")
    @ResponseStatus(HttpStatus.OK)
    public void updateLikeCounter(@PathVariable Long characterId, @RequestBody RoleIncrementationDTO roleIncrementationDTO, @RequestHeader("Authorization") String token) {
        String[] tokens = splitToken(token);
        if(tokens[0].equals("Role")){
            playerService.addLikes(tokens[1], characterId,roleIncrementationDTO.getIncrementBy());
        }
    }

    @GetMapping("/characters/{characterId}/detail")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoleGetDTO getRole(@RequestHeader("Authorization") String token, @PathVariable Long characterId) {
        String[] tokens = splitToken(token);
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(playerService.getRole(tokens[1],characterId));
    }

    @GetMapping("/characters/{scenarioId}/{characterId}/interlocutors")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<RoleGetDTO> getInterlocutors(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId,
            @PathVariable Long characterId) {
        List<Role> roles =
                playerService.getInterlocutors(token, scenarioId, characterId);
        return roles.stream()
                .map(PlayerDTOMapper.INSTANCE::convertEntitytoRoleGetDTO)
                .toList();
    }





     public static String[] splitToken(String token){
        if(token == null || token.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is empty");
        }
        String[] thingy = token.split(" ");
        if(thingy[0].equals("Role") || thingy[0].equals("Backroomer") || thingy[0].equals("Director")){
            return thingy;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is invalid");
    }

}
