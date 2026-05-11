package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Directive;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.*;
import ch.uzh.ifi.hase.soprafs26.service.DirectiveService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.mapper.DirectiveDTOMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
@RestController
public class DirectiveController {

    private final DirectiveService directiveService;
    private final UserService userService;

    DirectiveController(DirectiveService directiveService,
                        UserService userService) {
        this.directiveService = directiveService;
        this.userService = userService;
    }

    @PostMapping("/directives")
    @ResponseStatus(HttpStatus.CREATED)
    public DirectiveGetDTO createDirective(
            @RequestHeader("Authorization") String token,
            @RequestBody DirectivePostDTO postDTO) {

        requireUser(token);

        Directive directive = directiveService.createDirective(postDTO);

        return DirectiveDTOMapper.INSTANCE.convertEntityToGetDTO(directive);
    }

    @GetMapping("/directives/{directiveId}")
    @ResponseStatus(HttpStatus.OK)
    public DirectiveGetDTO getDirective(
            @RequestHeader("Authorization") String token,
            @PathVariable Long directiveId) {

        requireUser(token);

        Directive directive = directiveService.getDirectiveById(directiveId);

        return DirectiveDTOMapper.INSTANCE.convertEntityToGetDTO(directive);
    }

    @PutMapping("/directives/{directiveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDirective(
            @RequestHeader("Authorization") String token,
            @PathVariable Long directiveId,
            @RequestBody DirectivePutDTO putDTO) {

        requireUser(token);

        directiveService.updateDirectiveStatus(directiveId, putDTO);
    }

    @DeleteMapping("/directives/{directiveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirective(
            @RequestHeader("Authorization") String token,
            @PathVariable Long directiveId) {

        requireUser(token);

        directiveService.deleteDirective(directiveId);
    }

    @GetMapping("/directives/scenario/{scenarioId}")
    @ResponseStatus(HttpStatus.OK)
    public List<DirectiveGetDTO> getDirectivesByScenario(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId) {

        requireUser(token);

        List<Directive> directives = directiveService.getDirectivesByScenario(scenarioId);

        return directives.stream()
                .map(DirectiveDTOMapper.INSTANCE::convertEntityToGetDTO)
                .toList();
    }

    @GetMapping("/directives/character/{characterId}")
    @ResponseStatus(HttpStatus.OK)
    public List<DirectiveGetDTO> getDirectivesByCharacter(
            @RequestHeader("Authorization") String token,
            @PathVariable Long characterId) {

        requireUser(token);

        List<Directive> directives = directiveService.getDirectivesByCreator(characterId);

        return directives.stream()
                .map(DirectiveDTOMapper.INSTANCE::convertEntityToGetDTO)
                .toList();
    }

    private void requireUser(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        userService.validateUserToken(header.substring(7));
    }
}