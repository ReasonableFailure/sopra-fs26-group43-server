package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.*;
import ch.uzh.ifi.hase.soprafs26.service.MessageService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.MessageDTOMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;
    private final PlayerService playerService;

    MessageController(MessageService messageService,
                      PlayerService playerService) {
        this.messageService = messageService;
        this.playerService = playerService;
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageGetDTO createMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody MessagePostDTO postDTO) {

        playerService.validate(token, "Role");

        Message message = messageService.createMessage(postDTO);

        return MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);
    }

    @GetMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public MessageGetDTO getMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId) {

        playerService.validate(token, "any");

        Message message = messageService.getMessageById(messageId);

        return MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);
    }

    @PutMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId,
            @RequestBody MessagePutDTO putDTO) {

        playerService.validate(token, "Backroomer");

        messageService.updateMessageStatus(messageId, putDTO);
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId) {

        playerService.validate(token, "Backroomer");

        messageService.deleteMessage(messageId);
    }

    @GetMapping("/messages/scenario/{scenarioId}/pairs")
    @ResponseStatus(HttpStatus.OK)
    public List<MessagePairDTO> getMessagePairs(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId) {

        playerService.validate(token, "any");

        return messageService.getMessagePairsByScenario(scenarioId);
    }

    @GetMapping("/messages/character/{characterId}/inbox")
    @ResponseStatus(HttpStatus.OK)
    public List<MessageGetDTO> getCharacterInbox(
            @RequestHeader("Authorization") String token,
            @PathVariable Long characterId,
            @RequestParam Long scenarioId) {

        // Inbox is for the role itself — only the matching Role token may
        // read it. Backroomer/Director can already see everything via
        // getMessagesBetween if they need a broader view.
        playerService.validate(token, "Role");
        Player requester = playerService.resolvePlayerFromHeader(token);
        if (!(requester instanceof Role) || !requester.getId().equals(characterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Inbox is restricted to the recipient character");
        }

        List<Message> inbox = messageService.getInbox(characterId, scenarioId);
        return inbox.stream()
                .map(MessageDTOMapper.INSTANCE::convertEntityToGetDTO)
                .toList();
    }

    @GetMapping("/messages/between/{characterAId}/{characterBId}")
    @ResponseStatus(HttpStatus.OK)
    public List<MessageGetDTO> getMessagesBetween(
            @RequestHeader("Authorization") String token,
            @PathVariable Long characterAId,
            @PathVariable Long characterBId) {

        playerService.validate(token, "any");

        // If the requester is one of the two characters (Role token), hide
        // PENDING/REJECTED/FAILED inbound messages from them. Backroomer/
        // Director viewers see everything.
        Player requester = playerService.resolvePlayerFromHeader(token);
        Long requesterRoleId = (requester instanceof Role) ? requester.getId() : null;

        List<Message> messages =
                messageService.getMessagesBetween(characterAId, characterBId, requesterRoleId);

        return messages.stream()
                .map(MessageDTOMapper.INSTANCE::convertEntityToGetDTO)
                .toList();
    }

}
