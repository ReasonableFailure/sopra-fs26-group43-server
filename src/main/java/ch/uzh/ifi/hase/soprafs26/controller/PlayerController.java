package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RoleGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PlayerController {

    @PutMapping("/characters/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateRole() {
        //TODO: implement stub
    }

    @GetMapping("/characters/{characterId}")
    @ResponseStatus
    @ResponseBody
    public RolePutDTO getRole(){
        //TODO: implement stub
        return new RolePutDTO();
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
