package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePostDTO;
import ch.uzh.ifi.hase.soprafs26.mapper.DirectiveDTOMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Service
@Transactional
public class DirectiveService {

    private final DirectiveRepository directiveRepository;
    private final ScenarioRepository scenarioRepository;
    // private final CharacterRepository characterRepository;

    public DirectiveService(
            @Qualifier("directiveRepository") DirectiveRepository directiveRepository,
            @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository
    ) {
        this.directiveRepository = directiveRepository;
        this.scenarioRepository = scenarioRepository;
    }

    public Directive createDirective(DirectivePostDTO postDTO) {

        Scenario scenario = scenarioRepository.findById(postDTO.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));

        if (postDTO.getCreatorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator ID missing");
        }

        /*
        creator = characterRepository.findById(postDTO.getCreatorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Character not found"));
        */

        // Temporary fallback (REMOVE later)
        Character creator = new Character();
        creator.setId(postDTO.getCreatorId());

        Directive directive = DirectiveDTOMapper.INSTANCE.convertPostDTOToEntity(postDTO);

        directive.setCreatedAt(Instant.now());
        directive.setStatus(CommsStatus.PENDING);
        directive.setCreator(creator);

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

        directiveRepository.save(directive);
    }
}