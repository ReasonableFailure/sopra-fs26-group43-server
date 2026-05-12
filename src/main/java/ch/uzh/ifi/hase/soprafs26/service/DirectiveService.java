package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DirectiveDTOMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
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
    private final PlayerService playerService;

    public DirectiveService(
            @Qualifier("directiveRepository") DirectiveRepository directiveRepository,
            @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository,
            @Qualifier("roleRepository") RoleRepository roleRepository,
            @Lazy PlayerService playerService
    ) {
        this.directiveRepository = directiveRepository;
        this.scenarioRepository = scenarioRepository;
        this.roleRepository = roleRepository;
        this.playerService = playerService;
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

    public Directive getDirectiveById(Long directiveId, String userToken) {

        Directive directive = directiveRepository.findById(directiveId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Directive not found"));

        Long scenarioId = directive.getScenario() != null
                ? directive.getScenario().getId() : null;
        if (scenarioId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Directive has no scenario");
        }
        Player requester = playerService.resolveRequesterInScenario(userToken, scenarioId);
        if (requester instanceof Role
                && (directive.getCreator() == null
                    || !requester.getId().equals(directive.getCreator().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Directive not visible");
        }
        return directive;
    }

    public void updateDirectiveStatus(Long directiveId, DirectivePutDTO putDTO) {

        Directive directive = directiveRepository.findById(directiveId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Directive not found"));
        
        Role creator = directive.getCreator();
        creator.setNumberDirectives(creator.getNumberDirectives() + 1);
        creator.setTotalTextLength(creator.getTotalTextLength() + directive.totalTextLength());
        roleRepository.save(creator);

        if (putDTO.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status must not be null");
        }

        directive.setStatus(putDTO.getStatus());
        directive.setResponse(putDTO.getResponse());

        directiveRepository.save(directive);
    }

    public List<Directive> getDirectivesByScenario(Long scenarioId, String userToken) {

        if (!scenarioRepository.existsById(scenarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Scenario not found");
        }

        Player requester = playerService.resolveRequesterInScenario(userToken, scenarioId);
        List<Directive> directives = directiveRepository.findByScenarioId(scenarioId);

        // Backroomer / Director see every directive (queue for review).
        // A Role (character) only sees their own — directives are private
        // requests, not broadcast.
        if (requester instanceof Role) {
            final Long requesterRoleId = requester.getId();
            return directives.stream()
                    .filter(d -> d.getCreator() != null
                              && requesterRoleId.equals(d.getCreator().getId()))
                    .toList();
        }
        return directives;
    }

    public List<Directive> getDirectivesByCreator(Long characterId, String userToken) {

        Role character = roleRepository.findById(characterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Character not found"));

        Long scenarioId = character.getScenario() != null
                ? character.getScenario().getId() : null;
        if (scenarioId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Character has no scenario");
        }
        Player requester = playerService.resolveRequesterInScenario(userToken, scenarioId);
        // A Role can only query their own directives this way.
        if (requester instanceof Role && !requester.getId().equals(characterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Directives not visible");
        }
        return directiveRepository.findByCreatorId(characterId);
    }

    public void deleteDirective(Long directiveId) {
        if (!directiveRepository.existsById(directiveId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Directive not found");
        }
        directiveRepository.deleteById(directiveId);
    }
}