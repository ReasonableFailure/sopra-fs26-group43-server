package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.PlayerDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RoleGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.ScenarioDTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ScenarioService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.service.NewsService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ScenarioController {

    private final PlayerService playerService;
    private final ScenarioService scenarioService;
    private final NewsService newsService;

    ScenarioController(ScenarioService scenarioService,
                    PlayerService playerService, NewsService newsService) {
        this.scenarioService = scenarioService;
        this.playerService = playerService;
        this.newsService = newsService;
    }

    private static final String DIRECTOR = "Director";

    @GetMapping("/scenarios")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ScenarioGetDTO> getAllScenarios(@RequestHeader("Authorization") String token) {
        // fetch all scenarios in the internal representation
        playerService.validate(token, "any");
        List<Scenario> scenarios = scenarioService.getScenarios();
        List<ScenarioGetDTO> scenarioGetDTOs = new ArrayList<>();

        // convert each scenario to the API representation
        for (Scenario scenario : scenarios) {
            scenarioGetDTOs.add(ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario));
        }
        return scenarioGetDTOs;
    }

    @PostMapping("/scenarios")
    public ScenarioGetDTO createScenario(@RequestBody ScenarioPostDTO scenarioPostDTO, @RequestHeader("Authorization") String token){
        String rawToken = playerService.validate(token, DIRECTOR);
        Director director = playerService.getDirectorByToken(rawToken);
        if (scenarioPostDTO.getDirector() == null || !director.getId().equals(scenarioPostDTO.getDirector())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Director id does not match authenticated director");
        }
        Scenario created = scenarioService.createScenario(scenarioPostDTO);
        return ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(created);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ScenarioGetDTO getScenarioById(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId){
        playerService.validate(token,"any");
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        return ScenarioDTOMapper.INSTANCE.convertEntityToScenarioGetDTO(scenario);
    }

    @PutMapping("/scenarios/{scenarioId}")
    public void updateScenario(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId, @RequestBody ScenarioPutDTO scenarioPutDTO){
        String rawToken = playerService.validate(token, DIRECTOR);
        requireDirectorOf(scenarioId, rawToken);
        scenarioService.updateScenario(scenarioId,scenarioPutDTO);
    }

    @DeleteMapping("/scenarios/{scenarioId}")
    public void deleteScenario(@RequestHeader("Authorization") String token, @PathVariable Long scenarioId){
        String rawToken = playerService.validate(token, DIRECTOR);
        requireDirectorOf(scenarioId, rawToken);
        scenarioService.deleteScenario(scenarioId);
    }

    private void requireDirectorOf(Long scenarioId, String rawToken) {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario.getDirector() == null || !rawToken.equals(scenario.getDirector().getToken())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the director of this scenario");
        }
    }

    @GetMapping("/characters/scenario/{scenarioId}")
    public List<RoleGetDTO> retrieveAllRoles(@PathVariable Long scenarioId, @RequestHeader("Authorization") String token){
        playerService.validate(token, "any");
        List<Role> list = scenarioService.getRoles(scenarioId);
        ArrayList<RoleGetDTO> toReturn = new ArrayList<RoleGetDTO>();
        for(Role role : list){
            toReturn.add(PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(role));
        }
        return  toReturn;
    }

    @PutMapping("/scenarios/{scenarioId}/mastodon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMastodonConfig(
            @PathVariable Long scenarioId,
            @RequestHeader("Authorization") String token,
            @RequestBody ScenarioMastodonDTO dto) {

        String rawToken = playerService.validate(token, DIRECTOR);
        requireDirectorOf(scenarioId, rawToken);

        scenarioService.updateMastodonConfig(scenarioId, dto);
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        List<NewsStory> newsList = newsService.getNewsByScenario(scenarioId);
    }
}
