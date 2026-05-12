package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.ScenarioDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RoleGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.service.ScenarioService;

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
        playerService.validate(token, "Bearer");
        List<Scenario> scenarios = scenarioService.getScenarios();
        List<ScenarioGetDTO> scenarioGetDTOs = new ArrayList<>();

        for (Scenario scenario : scenarios) {
            ScenarioGetDTO dto = ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);
            dto.setDirectorToken(null);
            scenarioGetDTOs.add(dto);
        }
        return scenarioGetDTOs;
    }

    @PostMapping("/scenarios")
    public ScenarioGetDTO createScenario(@RequestBody ScenarioPostDTO scenarioPostDTO,
                                         @RequestHeader("Authorization") String token) {
        String userToken = playerService.validate(token, "Bearer");
        Scenario created = scenarioService.createScenario(userToken, scenarioPostDTO);
        // On creation the requester IS the director; keep the token in the response.
        return ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(created);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ScenarioGetDTO getScenarioById(@RequestHeader("Authorization") String token,
                                          @PathVariable Long scenarioId) {
        playerService.validate(token, "Bearer");
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        ScenarioGetDTO dto = ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);
        dto.setDirectorToken(null);
        return dto;
    }

    @PutMapping("/scenarios/{scenarioId}")
    public void updateScenario(@RequestHeader("Authorization") String token,
                               @PathVariable Long scenarioId,
                               @RequestBody ScenarioPutDTO scenarioPutDTO) {
        playerService.validate(token, "Director");
        scenarioService.updateScenario(scenarioId, scenarioPutDTO);
    }

    @DeleteMapping("/scenarios/{scenarioId}")
    public void deleteScenario(@RequestHeader("Authorization") String token,
                               @PathVariable Long scenarioId) {
        playerService.validate(token, "Director");
        scenarioService.deleteScenario(scenarioId);
    }

    @GetMapping("/characters/{scenarioId}")
    public List<RoleGetDTO> retrieveAllRoles(@PathVariable Long scenarioId,
                                             @RequestHeader("Authorization") String token) {
        playerService.validate(token, "Bearer");
        List<Role> list = scenarioService.getRoles(scenarioId);
        ArrayList<RoleGetDTO> toReturn = new ArrayList<>();
        for (Role role : list) {
            toReturn.add(PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(role));
        }
        return toReturn;
    }

    @GetMapping("/characters/{scenarioId}/cabinet/{cabinetId}")
    public List<RoleGetDTO> retrieveAllRolesInCabinet(@PathVariable Long scenarioId,
                                                      @PathVariable Long cabinetId,
                                                      @RequestHeader("Authorization") String token) {
        playerService.validate(token, "any");
        List<Role> list = scenarioService.getRolesPerCabinet(scenarioId, cabinetId);
        ArrayList<RoleGetDTO> toReturn = new ArrayList<>();
        for (Role role : list) {
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
        playerService.validate(token, "Director");
        scenarioService.updateMastodonConfig(scenarioId, dto);
    }
}
