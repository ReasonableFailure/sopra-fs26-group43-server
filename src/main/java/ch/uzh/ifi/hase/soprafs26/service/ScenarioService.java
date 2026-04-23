package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.ScenarioDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ScenarioService {

    private final Logger log = LoggerFactory.getLogger(ScenarioService.class);
    private final PlayerService playerService;
    private final UserService userService;
    private final ScenarioRepository scenarioRepository;

    public ScenarioService(@Qualifier("scenarioRepository") ScenarioRepository scenarioRepository, @Qualifier("userService") UserService userService, @Qualifier("playerService") PlayerService playerService) {
        this.scenarioRepository = scenarioRepository;
        this.userService = userService;
        this.playerService = playerService;
    }

    public List<Scenario> getScenarios(String token) {
        userService.checkIfValidToken(token);
        return this.scenarioRepository.findAll();
    }

    public Scenario getScenarioById(String token, Long scenarioId) {
        userService.checkIfValidToken(token);
        return this.scenarioRepository.findById(scenarioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario with id " + scenarioId + " not found"));
    }

    public Scenario createScenario(String token, ScenarioPostDTO scenarioPostDTO){
        //checkIfValidToken(token);
        Scenario newScenario = ScenarioDTOMapper.INSTANCE.convertScenarioPostDTOtoEntity(scenarioPostDTO);
        newScenario.setDayNumber(0);
        newScenario.setActive(true);
        newScenario.setPlayers(new ArrayList<Player>());
        newScenario.setHistory(new ArrayList<Communication>());
        newScenario.setDirector(playerService.createDirector(token));
        scenarioRepository.save(newScenario);
        scenarioRepository.flush();
        return newScenario;
    }

    public void deleteScenario(String token, Long scenarioId){
        Scenario toDelete = getScenarioById(token,scenarioId);
        scenarioRepository.delete(toDelete);
        scenarioRepository.flush();
    }
    public void updateScenario(String token, Long scenarioId, ScenarioPutDTO scenarioPutDTO){
        Scenario toUpdate = getScenarioById(token,scenarioId);
        ScenarioDTOMapper.INSTANCE.convertScenarioPutDTOtoEntity(scenarioPutDTO, toUpdate);
    }

    public void addPlayerToScenario(String token, Long scenarioId, Long playerId){
        userService.checkIfValidToken(token);
        Role toAdd = playerService.getRole(token,playerId);
        Scenario scenario = getScenarioById(token,scenarioId);
        toAdd = playerService.updateMessagingStats(playerId, scenario.getStartingMessageCount());
        scenario.addPlayer(toAdd);
        scenarioRepository.save(scenario);
        scenarioRepository.flush();
    }

    public void addCommunicationToHistory(String token, Long scenarioId, Long communicationId){
        Scenario toAddTo =  getScenarioById(token,scenarioId);
        toAddTo.addComm(null);
        //TODO: @HalaiRhea
    }

    public List<Role> getRoles(Long scenarioId, String token) {
        userService.checkIfValidToken(token);
        Scenario scenario = getScenarioById(token,scenarioId);
        List<Role> toReturn = new ArrayList<Role>();
        for(Player player : scenario.getPlayers()){
            if(player instanceof Role){
                toReturn.add((Role) player);
            }
        }
        return toReturn;
    }

    public List<Role> getRolesPerCabinet(Long scenarioId, Long cabinetId, String token){
        userService.checkIfValidToken(token);
        Scenario scenario = getScenarioById(token,scenarioId);
        List<Role> toReturn = new ArrayList<Role>();
        for(Player player : scenario.getPlayers()){
            if(player instanceof Role && (((Role) player).getAssignedCabinet() == cabinetId)){
                toReturn.add((Role) player);
            }
        }
        return toReturn;
    }

    public void updateMastodonConfig(Long scenarioId, ScenarioMastodonDTO dto) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));

        scenario.setMastodonBaseUrl(dto.getMastodonBaseUrl());
        scenario.setMastodonAccessToken(dto.getMastodonAccessToken());

        scenarioRepository.save(scenario);
    }
}
