package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Communication;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.ScenarioDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ScenarioService {

    private final Logger log = LoggerFactory.getLogger(ScenarioService.class);
    private final PlayerRepository playerRepository;
    private final ScenarioRepository scenarioRepository;
    private final UserRepository userRepository;

    public ScenarioService(@Qualifier("playerRepository") PlayerRepository playerRepository, @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository, @Qualifier("userRepository") UserRepository userRepository) {
        this.scenarioRepository = scenarioRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    public List<Scenario> getScenarios(String token) {
        checkIfValidToken(token);
        return this.scenarioRepository.findAll();
    }

    public Scenario getScenarioById(String token, Long scenarioId) {
        checkIfValidToken(token);
        return this.scenarioRepository.findById(scenarioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario with id " + scenarioId + " not found"));
    }

    public Scenario createScenario(String token, ScenarioPostDTO scenarioPostDTO){
        checkIfValidToken(token);
        Scenario newScenario = ScenarioDTOMapper.INSTANCE.convertScenarioPostDTOtoEntity(scenarioPostDTO);
        newScenario.setDay(0);
        newScenario.setActive(true);
        newScenario.setPlayers(new ArrayList<Player>());
        newScenario.setHistory(new ArrayList<Communication>());
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

    private void checkIfValidToken(String token) throws ResponseStatusException {
        User foundByToken = userRepository.findByToken(token);
        if (foundByToken.getToken() == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access to scenario!");
        }
    }
}
