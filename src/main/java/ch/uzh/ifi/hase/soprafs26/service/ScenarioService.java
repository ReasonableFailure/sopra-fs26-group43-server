package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.ScenarioDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ScenarioService {

    private final Logger log = LoggerFactory.getLogger(ScenarioService.class);
    private final PlayerService playerService;
    private final UserService userService;
    private final ScenarioRepository scenarioRepository;
    private final NewsService newsService;

    public ScenarioService(@Qualifier("scenarioRepository") ScenarioRepository scenarioRepository, @Qualifier("userService") UserService userService, @Qualifier("playerService") PlayerService playerService, @Qualifier("newsService") NewsService newsService) {
        this.scenarioRepository = scenarioRepository;
        this.userService = userService;
        this.playerService = playerService;
        this.newsService = newsService;
    }

    public List<Scenario> getScenarios() {
        return this.scenarioRepository.findAll();
    }

    public Scenario getScenarioById(Long scenarioId) {

        return this.scenarioRepository.findById(scenarioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario with id " + scenarioId + " not found"));
    }

    @Transactional
    public Scenario createScenario(String token, ScenarioPostDTO scenarioPostDTO){
        //TODO: there must be some way to identify the director creating the scenario
        Scenario newScenario = ScenarioDTOMapper.INSTANCE.convertScenarioPostDTOtoEntity(scenarioPostDTO);
        newScenario.setDayNumber(0);
        newScenario.setStatus(ScenarioStatus.UNSTARTED);
        newScenario.setPlayers(new ArrayList<Player>());
        newScenario.setHistory(new ArrayList<Communication>());
        newScenario = scenarioRepository.save(newScenario);
        Director director = playerService.createDirector(token, newScenario);
        newScenario.setDirector(director);
        newScenario.addPlayer(director);
        newScenario.setDirector(playerService.getDirectorByToken(token));
        scenarioRepository.save(newScenario);
        scenarioRepository.flush();
        return newScenario;
    }

    public void deleteScenario(Long scenarioId){
        Scenario toDelete = getScenarioById(scenarioId);
        scenarioRepository.delete(toDelete);
        scenarioRepository.flush();
    }

    public void updateScenario(Long scenarioId, ScenarioPutDTO dto){
        Scenario s = getScenarioById(scenarioId);

        if (dto.getTitle() != null) {
            s.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            s.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            if (dto.getStatus() == ScenarioStatus.COMPLETED && s.getStatus() != ScenarioStatus.COMPLETED) {
                s.setFinishTime(LocalDateTime.now());
            }
            s.setStatus(dto.getStatus());
        }
        if (dto.getDayNumber() != null) {
            s.setDayNumber(dto.getDayNumber());
        }
        if (dto.getExchangeRate() != null) {
            s.setExchangeRate(dto.getExchangeRate());
        }
        scenarioRepository.save(s);
        scenarioRepository.flush();
    }

    public void addPlayerToScenario(Long scenarioId, Long playerId){

        Role toAdd = playerService.getRoleById(playerId);
        Scenario scenario = getScenarioById(scenarioId);
        toAdd = playerService.updateMessagingStats(playerId, scenario.getStartingMessageCount());
        scenario.addPlayer(toAdd);
        scenarioRepository.save(scenario);
        scenarioRepository.flush();
    }

    public void addCommunicationToHistory(Long scenarioId, Long communicationId){
        Scenario toAddTo =  getScenarioById(scenarioId);
        toAddTo.addComm(null);
        //TODO: @HalaiRhea
    }

    public List<Role> getRoles(Long scenarioId) {
        Scenario scenario = getScenarioById(scenarioId);
        List<Role> toReturn = new ArrayList<Role>();
        for(Player player : scenario.getPlayers()){
            if(player instanceof Role){
                toReturn.add((Role) player);
            }
        }
        return toReturn;
    }

    public List<Role> getRolesPerCabinet(Long scenarioId, Long cabinetId){

        Scenario scenario = getScenarioById(scenarioId);
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

        String profileUrl = MastodonClient.fetchMastodonProfileUrl(
                dto.getMastodonBaseUrl(),
                dto.getMastodonAccessToken()
        );

        scenario.setMastodonProfileUrl(profileUrl);

        List<NewsStory> newsList = newsService.getNewsByScenario(scenarioId);

        for (NewsStory news : newsList) {
            try {
                String content = news.formatSelf();

                MastodonClient.postStatus(
                        dto.getMastodonBaseUrl(),
                        dto.getMastodonAccessToken(),
                        content
                );
            } catch (Exception e) {
                System.err.println("Failed to post news id " + news.getId());
            }
        }

        scenarioRepository.save(scenario);
    }
}
