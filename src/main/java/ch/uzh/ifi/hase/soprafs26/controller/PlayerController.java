package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.*;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserAssignDTO;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.service.ActionPointService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PlayerController {

    private final PlayerService playerService;
    private final ActionPointService actionPointService;
    private final UserService userService;

    public PlayerController(PlayerService playerService, ActionPointService actionPointService, UserService userService){
        this.playerService = playerService;
        this.actionPointService = actionPointService;
        this.userService = userService;
    }

    private String requireBearer(String token) {
        return playerService.validate(token, "Bearer");
    }

    private User requireBearerUser(String token, Long expectedUserId) {
        String raw = requireBearer(token);
        User bearer = userService.getByToken(raw);
        if (expectedUserId != null && !bearer.getId().equals(expectedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot act on behalf of another user");
        }
        return bearer;
    }

    @PutMapping("/characters/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateRole(@RequestBody RolePutDTO rolePutDTO, @RequestHeader("Authorization") String token, @PathVariable Long characterId){
        playerService.validate(token,"Director");
        playerService.updateRole(rolePutDTO,characterId);
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
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(playerService.createRole(rolePostDTO));
    }

    @DeleteMapping("/characters/{characterId}")
    public void deleteRole(@RequestHeader("Authorization")String token, @PathVariable Long characterId){
        playerService.validate(token, "Director");
        playerService.deleteRole(characterId);
    }

    @PostMapping("/scenario/{scenarioId}/backroomers")
    @ResponseStatus(HttpStatus.OK)
    public BackroomerGetDTO createBackroomer(@RequestHeader("Authorization")String token, @PathVariable Long scenarioId, @RequestBody UserAssignDTO userAssignDTO){
        if (userAssignDTO == null || userAssignDTO.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }
        requireBearerUser(token, userAssignDTO.getId());
        return PlayerDTOMapper.INSTANCE.convertEntitytoBackroomerGetDTO(playerService.createBackroomer(userAssignDTO, scenarioId));
    }

    @PutMapping("/characters/{characterId}/assignment")
    @ResponseStatus(HttpStatus.OK)
    public RoleGetDTO selectCharacter(@RequestHeader("Authorization") String token,@PathVariable Long characterId, @RequestBody UserAssignDTO userAssignDTO){
        if (userAssignDTO == null || userAssignDTO.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }
        requireBearerUser(token, userAssignDTO.getId());
        Role r = playerService.claimRole(characterId, userAssignDTO.getId());
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(r);
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
        requireBearerUser(token, userAssignDTO.getId());
        Director d = playerService.createDirector(userAssignDTO.getId());
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
