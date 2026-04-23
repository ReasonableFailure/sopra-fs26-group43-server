package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RoleGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.ScenarioDTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ScenarioService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import static ch.uzh.ifi.hase.soprafs26.controller.PlayerController.splitToken;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ScenarioController {

    private final PlayerService playerService;
    private final ScenarioService scenarioService;

    ScenarioController(ScenarioService scenarioService,
                    PlayerService playerService) {
        this.scenarioService = scenarioService;
        this.playerService = playerService;
    }

    @GetMapping("/scenarios")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ScenarioGetDTO> getAllScenarios(@RequestHeader("Authorization") String token) {
        // fetch all scenarios in the internal representation
        List<Scenario> scenarios = scenarioService.getScenarios(token);
        List<ScenarioGetDTO> scenarioGetDTOs = new ArrayList<>();

        // convert each scenario to the API representation
        for (Scenario scenario : scenarios) {
            scenarioGetDTOs.add(ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario));
        }
        return scenarioGetDTOs;
    }

    @PostMapping("/scenarios")
    public ScenarioGetDTO createScenario(@RequestBody ScenarioPostDTO scenarioPostDTO, @RequestHeader("Authorization") String token){
        Scenario created = scenarioService.createScenario(token,scenarioPostDTO);
        return ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(created);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ScenarioGetDTO getScenarioById(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId){
        Scenario scenario = scenarioService.getScenarioById(token,scenarioId);
        return ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);
    }

    @PutMapping("/scenarios/{scenarioId}")
    public void updateScenario(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId, @RequestBody ScenarioPutDTO scenarioPutDTO){
        scenarioService.updateScenario(token,scenarioId,scenarioPutDTO);
    }

    @DeleteMapping("/scenarios/{scenarioId}")
    public void deleteScenario(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId){
        scenarioService.deleteScenario(token,scenarioId);
    }

    @GetMapping("/characters/{scenarioId}")
    public List<RoleGetDTO> retrieveAllRoles(@PathVariable Long scenarioId, @RequestHeader("Authorization") String token){
        List<Role> list = scenarioService.getRoles(scenarioId,token);
        ArrayList<RoleGetDTO> toReturn = new ArrayList<RoleGetDTO>();
        for(Role role : list){
            toReturn.add(PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(role));
        }
        return  toReturn;
    }

    @GetMapping("/characters/{scenarioId}/cabinet/{cabinetId}")
    public List<RoleGetDTO> retrieveAllRolesInCabinet(@PathVariable Long scenarioId, @PathVariable Long cabinetId, @RequestHeader("Authorization") String token){
        List<Role> list = scenarioService.getRolesPerCabinet(scenarioId,cabinetId,token);
        ArrayList<RoleGetDTO> toReturn = new ArrayList<RoleGetDTO>();
        for (Role role : list){
            toReturn.add(PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(role));
        }
        return toReturn;
    }


    @PutMapping("/scenarios/{scenarioId}/mastodon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMastodonConfig(
            @PathVariable Long scenarioId,
            @RequestHeader("Authorization") String token,
            @RequestBody ScenarioMastodonDTO dto) {

        validate(token, "Director");

        scenarioService.updateMastodonConfig(scenarioId, dto);
    }

    private String validate(String header, String type) {
        String[] tokens = splitToken(header);
        playerService.checkToken(tokens[1], type);
        return tokens[1];
    }

}
