package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.*;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
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
    @ResponseStatus(HttpStatus.OK)
    public void deleteRole(@RequestHeader("Authorization")String token, @PathVariable Long characterId){
        playerService.validate(token, "Director");
        playerService.deleteRole(characterId);
    }

    @PostMapping("/backroomers")
    @ResponseStatus(HttpStatus.OK)
    public PlayerGetDTO createBackroomer(@RequestHeader("Authorization")String token, @RequestBody PlayerPutDTO playerPutDTO){
        playerService.validate(token, "Bearer");
        return PlayerDTOMapper.INSTANCE.convertEntitytoPlayerGetDTO(playerService.createBackroomer(playerPutDTO));
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
        String auth = playerService.validate(token,"Bearer");
        Director d = playerService.createDirector(auth, playerPutDTO.getNewAssignedUserId());
        return PlayerDTOMapper.INSTANCE.convertEntitytoPlayerGetDTO(d);
    }

    @PostMapping("/characters/{scenarioId}/{characterId}/buy-message")
    @ResponseStatus(HttpStatus.OK)

    public RoleGetDTO buyMessage(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId, @PathVariable Long characterId) {
        playerService.validate(token,"Role");
        Role updatedRole = playerService.buyMessage(scenarioId, characterId);
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(updatedRole);
    }




}
