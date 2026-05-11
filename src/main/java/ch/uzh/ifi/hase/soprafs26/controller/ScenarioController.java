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
import org.springframework.web.server.ResponseStatusException;

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
        String userToken = stripPrefix(token);
        List<Scenario> scenarios = scenarioService.getScenarios(userToken);
        List<ScenarioGetDTO> scenarioGetDTOs = new ArrayList<>();

        for (Scenario scenario : scenarios) {
            ScenarioGetDTO dto = ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);
            redactDirectorToken(dto, scenario, userToken);
            scenarioGetDTOs.add(dto);
        }
        return scenarioGetDTOs;
    }

    @PostMapping("/scenarios")
    public ScenarioGetDTO createScenario(@RequestBody ScenarioPostDTO scenarioPostDTO, @RequestHeader("Authorization") String token){
        String userToken = stripPrefix(token);
        Scenario created = scenarioService.createScenario(userToken, scenarioPostDTO);
        // On creation the requester IS the director; keep the token in the response.
        return ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(created);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ScenarioGetDTO getScenarioById(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId){
        String userToken = stripPrefix(token);
        Scenario scenario = scenarioService.getScenarioById(userToken, scenarioId);
        ScenarioGetDTO dto = ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);
        redactDirectorToken(dto, scenario, userToken);
        return dto;
    }

    @PutMapping("/scenarios/{scenarioId}")
    public void updateScenario(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId, @RequestBody ScenarioPutDTO scenarioPutDTO){
        String userToken = stripPrefix(token);
        scenarioService.updateScenario(userToken, scenarioId, scenarioPutDTO);
    }

    @DeleteMapping("/scenarios/{scenarioId}")
    public void deleteScenario(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId){
        String userToken = stripPrefix(token);
        scenarioService.deleteScenario(userToken, scenarioId);
    }

    @GetMapping("/characters/{scenarioId}")
    public List<RoleGetDTO> retrieveAllRoles(@PathVariable Long scenarioId, @RequestHeader("Authorization") String token){
        String userToken = stripPrefix(token);
        List<Role> list = scenarioService.getRoles(scenarioId, userToken);
        ArrayList<RoleGetDTO> toReturn = new ArrayList<RoleGetDTO>();
        for(Role role : list){
            toReturn.add(PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(role));
        }
        return  toReturn;
    }

    @GetMapping("/characters/{scenarioId}/cabinet/{cabinetId}")
    public List<RoleGetDTO> retrieveAllRolesInCabinet(@PathVariable Long scenarioId, @PathVariable Long cabinetId, @RequestHeader("Authorization") String token){
        String userToken = stripPrefix(token);
        List<Role> list = scenarioService.getRolesPerCabinet(scenarioId, cabinetId, userToken);
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

        String userToken = stripPrefix(token);
        scenarioService.updateMastodonConfig(userToken, scenarioId, dto);
    }

    private String stripPrefix(String token){
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    private void redactDirectorToken(ScenarioGetDTO dto, Scenario scenario, String userToken){
        if (scenario.getDirector() == null
                || scenario.getDirector().getUser() == null
                || !userToken.equals(scenario.getDirector().getUser().getToken())) {
            dto.setDirectorToken(null);
        }
    }

    private String validate(String header, String type) {
        String[] tokens = splitToken(header);
        playerService.checkToken(tokens[1], type);
        return tokens[1];
    }

}
