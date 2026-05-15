package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DirectiveDTOMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class DirectiveService {

    private final DirectiveRepository directiveRepository;
    private final ScenarioService scenarioService;
    private final PlayerService playerService;
    private final CommunicationStatsService communicationStatsService;

    public DirectiveService(
            @Qualifier("directiveRepository") DirectiveRepository directiveRepository,
            @Qualifier("scenarioService") ScenarioService scenarioService,
            @Qualifier("playerService") PlayerService playerService,
            @Qualifier("communicationStatsService") CommunicationStatsService communicationStatsService
    ) {
        this.directiveRepository = directiveRepository;
        this.scenarioService = scenarioService;
        this.playerService = playerService;
        this.communicationStatsService = communicationStatsService;
    }

    public Directive createDirective(DirectivePostDTO postDTO) {

        Scenario scenario = scenarioService.getScenarioById(postDTO.getScenarioId());

        if (scenario.getStatus() == ScenarioStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot submit directives in a completed scenario");
        }

        if (postDTO.getCreatorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator ID missing");
        }

        Role creator = playerService.getRoleById(postDTO.getCreatorId());

        if (!scenario.getPlayers().contains(creator)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Role not part of scenario");
        }

        Directive directive = DirectiveDTOMapper.INSTANCE.convertPostDTOToEntity(postDTO);

        directive.setCreatedAt(Instant.now());
        directive.setDayNumber(scenario.getDayNumber());
        directive.setStatus(CommsStatus.PENDING);
        directive.setCreator(creator);
        directive.setResponse(null);
        directive.setScenario(scenario);

        directive = directiveRepository.save(directive);

        scenarioService.addCommunicationToHistory(
            scenario.getId(),
            directive
        );

        return directive;
    }

    public Directive getDirectiveById(Long directiveId) {

        Directive directive = directiveRepository.findById(directiveId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Directive not found"));

        return directive;
    }

    public void updateDirectiveStatus(Long directiveId, DirectivePutDTO putDTO) {

        Directive directive = getDirectiveById(directiveId);
        
        communicationStatsService.registerCommunication(directive.getCreator(), directive);

        if (putDTO.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status must not be null");
        }

        directive.setStatus(putDTO.getStatus());
        directive.setResponse(putDTO.getResponse());

        directiveRepository.save(directive);
    }

    public List<Directive> getDirectivesByScenario(Long scenarioId) {

        scenarioService.getScenarioById(scenarioId);
        return directiveRepository.findByScenarioId(scenarioId);
    }

    public List<Directive> getDirectivesByCreator(Long characterId) {
        
        playerService.getRoleById(characterId);
        return directiveRepository.findByCreatorId(characterId);
    }

    public void deleteDirective(Long Id){
        directiveRepository.deleteById(Id);
    }
}