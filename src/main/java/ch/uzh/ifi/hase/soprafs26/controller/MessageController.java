package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.*;
import ch.uzh.ifi.hase.soprafs26.service.MessageService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import static ch.uzh.ifi.hase.soprafs26.controller.PlayerController.splitToken;
import ch.uzh.ifi.hase.soprafs26.mapper.MessageDTOMapper;

import org.springframework.web.bind.annotation.*;
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

        //validate(token, "Role");

        Message message = messageService.createMessage(postDTO);

        return MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);
    }

    @GetMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public MessageGetDTO getMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId) {

        //validate(token, "any");

        Message message = messageService.getMessageById(messageId);

        return MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);
    }

    @PutMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId,
            @RequestBody MessagePutDTO putDTO) {

        //validate(token, "Backroomer");

        messageService.updateMessageStatus(messageId, putDTO);
    }

    @GetMapping("/messages/scenario/{scenarioId}/pairs")
    @ResponseStatus(HttpStatus.OK)
    public List<MessagePairDTO> getMessagePairs(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId) {

        //validate(token, "any");

        return messageService.getMessagePairsByScenario(scenarioId);
    }

    @GetMapping("/messages/between/{characterAId}/{characterBId}")
    @ResponseStatus(HttpStatus.OK)
    public List<MessageGetDTO> getMessagesBetween(
            @RequestHeader("Authorization") String token,
            @PathVariable Long characterAId,
            @PathVariable Long characterBId) {

        //validate(token, "any");

        List<Message> messages =
                messageService.getMessagesBetween(characterAId, characterBId);

        return messages.stream()
                .map(MessageDTOMapper.INSTANCE::convertEntityToGetDTO)
                .toList();
    }

    private String validate(String header, String type) {
        String[] tokens = splitToken(header);
        playerService.checkToken(tokens[1], type);
        return tokens[1];
    }
}