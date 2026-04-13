package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Player;
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
        Player found = playerService.getPlayer(token,characterId);
        return new RoleGetDTO();
    }

    @PostMapping("/characters")
    public RoleGetDTO createRole(@RequestBody RolePostDTO rolePostDTO){
        //TODO: implement stub
        return new RoleGetDTO();
    }

    @DeleteMapping("/characters/{characterId}")
    public void deleteRole(@PathVariable("characterId") long characterId ){
        //TODO: implement stub
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
