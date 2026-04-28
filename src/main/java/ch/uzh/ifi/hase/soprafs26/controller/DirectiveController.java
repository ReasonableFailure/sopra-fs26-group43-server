package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Directive;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.*;
import ch.uzh.ifi.hase.soprafs26.service.DirectiveService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import static ch.uzh.ifi.hase.soprafs26.controller.PlayerController.splitToken;
import ch.uzh.ifi.hase.soprafs26.mapper.DirectiveDTOMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
@RestController
public class DirectiveController {

    private final DirectiveService directiveService;
    private final PlayerService playerService;

    DirectiveController(DirectiveService directiveService,
                        PlayerService playerService) {
        this.directiveService = directiveService;
        this.playerService = playerService;
    }

    @PostMapping("/directives")
    @ResponseStatus(HttpStatus.CREATED)
    public DirectiveGetDTO createDirective(
            @RequestHeader("Authorization") String token,
            @RequestBody DirectivePostDTO postDTO) {

        //validate(token, "Role");

        Directive directive = directiveService.createDirective(postDTO);

        return DirectiveDTOMapper.INSTANCE.convertEntityToGetDTO(directive);
    }

    @GetMapping("/directives/{directiveId}")
    @ResponseStatus(HttpStatus.OK)
    public DirectiveGetDTO getDirective(
            @RequestHeader("Authorization") String token,
            @PathVariable Long directiveId) {

        //validate(token, "any");

        Directive directive = directiveService.getDirectiveById(directiveId);

        return DirectiveDTOMapper.INSTANCE.convertEntityToGetDTO(directive);
    }

    @PutMapping("/directives/{directiveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDirective(
            @RequestHeader("Authorization") String token,
            @PathVariable Long directiveId,
            @RequestBody DirectivePutDTO putDTO) {

        //validate(token, "Backroomer");

        directiveService.updateDirectiveStatus(directiveId, putDTO);
    }

    @GetMapping("/directives/scenario/{scenarioId}")
    @ResponseStatus(HttpStatus.OK)
    public List<DirectiveGetDTO> getDirectivesByScenario(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId) {

        //validate(token, "any");

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

        //validate(token, "any");

        List<Directive> directives = directiveService.getDirectivesByCreator(characterId);

        return directives.stream()
                .map(DirectiveDTOMapper.INSTANCE::convertEntityToGetDTO)
                .toList();
    }

    private String validate(String header, String type) {
        String[] tokens = splitToken(header);
        playerService.checkToken(tokens[1], type);
        return tokens[1];
    }
}