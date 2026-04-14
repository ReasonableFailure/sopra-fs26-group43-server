package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RoleGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    @PutMapping("/player/{playerid}")
    public void assignUsertoPlayer(@PathVariable Long playerId, @RequestBody PlayerPutDTO playerPutDTO,  @RequestHeader("Authorization") String token) {
        playerService.updatePlayerAssociation(playerId,playerPutDTO,token);
    }

    @PutMapping("/characters/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody

    public void updateRole(@RequestBody RolePutDTO rolePutDTO, @RequestHeader("Authorization") String token, @PathVariable Long characterId) {
        playerService.updateRole(token, rolePutDTO,characterId);
    }

    @GetMapping("/characters/{characterId}")
    @ResponseStatus
    @ResponseBody
    public RoleGetDTO getRole(@RequestHeader("Authorization") String token, @PathVariable Long characterId) {
        Role found = playerService.getRole(token,characterId);
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(found);
    }

    @PostMapping("/characters")
    public RoleGetDTO createRole(@RequestBody RolePostDTO rolePostDTO, @RequestHeader("Authorization") String token){
        Role created =playerService.createRole(token, rolePostDTO);
        return PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(created);
    }

    @DeleteMapping("/characters/{characterId}")
    public void deleteRole(@RequestHeader("Authorization")String token, @PathVariable Long characterId){
        playerService.deleteRole(token,characterId);

    }

    @GetMapping("/characters/{scenarioId}")
    public List<RoleGetDTO> getAllRoles(){
        //TODO: implement stub
        return new ArrayList<>();
    }

    @GetMapping("/characters/{scenarioId}/cabinet/{cabinetId}")
    public List<RoleGetDTO> getAllRolesPerCabinet(){
        return new ArrayList<>();
    }

}
