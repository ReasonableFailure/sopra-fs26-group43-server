package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ScenarioService;

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
    public List<ScenarioGetDTO> getAllScenarios() {
        // fetch all scenarios in the internal representation
        List<Scenario> scenarios = scenarioService.getScenarios();
        List<ScenarioGetDTO> scenarioGetDTOs = new ArrayList<>();

        // convert each scenario to the API representation
        for (Scenario scenario : scenarios) {
            scenarioGetDTOs.add(DTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario));
        }
        return scenarioGetDTOs;
    }
}
