package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.*;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.service.ActionPointService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PlayerController {
    private final PlayerService playerService;
    private final ActionPointService actionPointService;

    public PlayerController(PlayerService playerService, ActionPointService actionPointService) {
        this.playerService = playerService;
        this.actionPointService = actionPointService;
    }
    @PutMapping("/player/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignUserToPlayer(@PathVariable Long playerId, @RequestBody PlayerPutDTO playerPutDTO,  @RequestHeader("Authorization") String token) {
        playerService.validate(token,"Bearer");
        playerService.updatePlayerAssociation(playerId, playerPutDTO);

    }

    @PutMapping("/characters/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateRole(@RequestBody RolePutDTO rolePutDTO, @RequestHeader("Authorization") String token, @PathVariable Long characterId) {
        playerService.validate(token,"Director");
        playerService.updateRole(rolePutDTO,characterId);
    }

    @GetMapping("/characters/{characterId}/detail")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoleGetDTO getRole(@RequestHeader("Authorization") String token, @PathVariable Long characterId) {
        playerService.validate(token,"any");
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(playerService.getRoleById(characterId));
    }

    @PostMapping("/characters")
    @ResponseStatus(HttpStatus.OK)
    public RoleGetDTO createRole(@RequestBody RolePostDTO rolePostDTO, @RequestHeader("Authorization") String token){
        playerService.validate(token,"Director");
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(playerService.createRole( rolePostDTO));
    }

    @DeleteMapping("/characters/{characterId}")
    public void deleteRole(@RequestHeader("Authorization")String token, @PathVariable Long characterId){
        playerService.validate(token, "Director");
        playerService.deleteRole(characterId);
    }

    @PostMapping("/scenario/{scenarioId}/backroomers")
    @ResponseStatus(HttpStatus.OK)
    public PlayerGetDTO createBackroomer(@RequestHeader("Authorization")String token, @PathVariable Long scenarioId, @RequestBody PlayerPutDTO playerPutDTO){
        playerService.validate(token, "Bearer");
        return PlayerDTOMapper.INSTANCE.convertEntitytoPlayerGetDTO(playerService.createBackroomer(playerPutDTO, scenarioId));
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

    @GetMapping("/characters/{scenarioId}/{characterId}/points")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoleGetDTO syncAndGetPoints(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId,
            @PathVariable Long characterId
    ) {
        playerService.validate(token,"Role");
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(
                playerService.syncPointsAndGetRole(scenarioId, characterId)
        );
    }
    @PostMapping("/directors")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO createScenarioDirector(@RequestHeader("Authorization") String token, @RequestBody PlayerPutDTO playerPutDTO){
        playerService.validate(token,"Bearer");
        Director d = playerService.createDirector(playerPutDTO.getNewAssignedUserId());
        return PlayerDTOMapper.INSTANCE.convertEntitytoPlayerGetDTO(d);
    }

    @PostMapping("/scenarios/{scenarioId}/characters/{characterId}/messages")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoleGetDTO buyMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId,
            @PathVariable Long characterId) {
        playerService.validate(token, "Role");
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(actionPointService.buyMessage(scenarioId,characterId));
    }

}
