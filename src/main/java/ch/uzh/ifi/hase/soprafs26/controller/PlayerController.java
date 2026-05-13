package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.*;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserAssignDTO;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.service.ActionPointService;
import org.h2.command.dml.BackupCommand;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PlayerController {

    private final PlayerService playerService;
    private final ActionPointService actionPointService;

    public PlayerController(PlayerService playerService, ActionPointService actionPointService){
        this.playerService = playerService;
        this.actionPointService = actionPointService;
    }

    @PutMapping("/characters/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateRole(@RequestBody RolePutDTO rolePutDTO, @RequestHeader("Authorization") String token, @PathVariable Long characterId){
        playerService.validate(token,"Director");
        Role holdsUpdates = PlayerDTOMapper.INSTANCE.convertRolePutDTOtoEntity(rolePutDTO);
        playerService.updateRole(holdsUpdates,characterId);
    }

    @GetMapping("/characters/{characterId}/detail")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoleGetDTO getRole(@RequestHeader("Authorization") String token, @PathVariable Long characterId){
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
    public BackroomerGetDTO createBackroomer(@RequestHeader("Authorization")String token, @PathVariable Long scenarioId, @RequestBody UserAssignDTO userAssignDTO){
        playerService.validate(token, "Bearer");
        return PlayerDTOMapper.INSTANCE.convertEntitytoBackroomerGetDTO(playerService.createBackroomer(userAssignDTO, scenarioId));
    }

    @PostMapping("/characters/{characterId}/assignment")
    @ResponseStatus(HttpStatus.OK)
    public RoleGetDTO selectCharacter(@RequestHeader("Authorization") String token,@PathVariable Long characterId, @RequestBody UserAssignDTO userAssignDTO){
        playerService.validate(token, "Bearer");
        Role r = (Role) playerService.updatePlayerAssociation(characterId,userAssignDTO);
        return  PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(r);
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
    ){
        playerService.validate(token,"Role");
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(
                playerService.syncPointsAndGetRole(scenarioId, characterId)
        );
    }
    @PostMapping("/directors")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public DirectorGetDTO createScenarioDirector(@RequestHeader("Authorization") String token, @RequestBody UserAssignDTO userAssignDTO){
        playerService.validate(token,"Bearer");
        //System.out.println(userAssignDTO);
        Director d = playerService.createDirector(userAssignDTO.getId());
        System.out.println(d.getToken());
        return PlayerDTOMapper.INSTANCE.convertEntityToDirectorGetDTO(d);
    }

    @PostMapping("/scenarios/{scenarioId}/characters/{characterId}/messages")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoleGetDTO buyMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId,
            @PathVariable Long characterId){
        playerService.validate(token, "Role");
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(actionPointService.buyMessage(scenarioId,characterId));
    }
}
