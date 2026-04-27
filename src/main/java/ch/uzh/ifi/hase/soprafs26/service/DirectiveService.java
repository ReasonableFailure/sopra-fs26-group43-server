package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
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
    private final ScenarioRepository scenarioRepository;
    private final RoleRepository roleRepository;

    public DirectiveService(
            @Qualifier("directiveRepository") DirectiveRepository directiveRepository,
            @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository,
            @Qualifier("roleRepository") RoleRepository roleRepository
    ) {
        this.directiveRepository = directiveRepository;
        this.scenarioRepository = scenarioRepository;
        this.roleRepository = roleRepository;
    }

    public Directive createDirective(DirectivePostDTO postDTO) {

        Scenario scenario = scenarioRepository.findById(postDTO.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));

        if (postDTO.getCreatorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator ID missing");
        }

        Role creator = roleRepository.findById(postDTO.getCreatorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role not found"));

        if (!scenario.getPlayers().contains(creator)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Role not part of scenario");
        }

        Directive directive = DirectiveDTOMapper.INSTANCE.convertPostDTOToEntity(postDTO);

        directive.setCreatedAt(Instant.now());
        directive.setStatus(CommsStatus.PENDING);
        directive.setCreator(creator);
        directive.setResponse(null);
        directive.setScenario(scenario);

        directive = directiveRepository.save(directive);

        scenario.getHistory().add(directive);
        scenarioRepository.save(scenario);

        return directive;
    }

    public Directive getDirectiveById(Long directiveId) {

        Directive directive = directiveRepository.findById(directiveId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Directive not found"));

        return directive;
    }

    public void updateDirectiveStatus(Long directiveId, DirectivePutDTO putDTO) {

        Directive directive = directiveRepository.findById(directiveId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Directive not found"));

        if (putDTO.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status must not be null");
        }

        directive.setStatus(putDTO.getStatus());
        directive.setResponse(putDTO.getResponse());

        directiveRepository.save(directive);
    }

    public List<Directive> getDirectivesByScenario(Long scenarioId) {

        if (!scenarioRepository.existsById(scenarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Scenario not found");
        }

        return directiveRepository.findByScenarioId(scenarioId);
    }

    public List<Directive> getDirectivesByCreator(Long characterId) {

        if (!roleRepository.existsById(characterId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Character not found");
        }

        return directiveRepository.findByCreatorId(characterId);
    }
}