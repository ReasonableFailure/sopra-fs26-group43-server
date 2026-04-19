package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.ScenarioDTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ScenarioService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ScenarioController {

    private final ScenarioService scenarioService;

    ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
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

    @GetMapping("/scenarios/{scenarioID}")
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

    @PutMapping("/scenarios/{scenarioId}/mastodon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMastodonConfig(@PathVariable Long scenarioId, @RequestHeader("Authorization") String token, @RequestBody ScenarioMastodonDTO dto) {
        scenarioService.updateMastodonConfig(scenarioId, token, dto);
    }

}
