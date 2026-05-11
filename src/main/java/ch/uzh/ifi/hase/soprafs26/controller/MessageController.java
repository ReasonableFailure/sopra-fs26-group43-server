package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.*;
import ch.uzh.ifi.hase.soprafs26.service.MessageService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.mapper.MessageDTOMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    MessageController(MessageService messageService,
                      UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageGetDTO createMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody MessagePostDTO postDTO) {

        requireUser(token);

        Message message = messageService.createMessage(postDTO);

        return MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);
    }

    @GetMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public MessageGetDTO getMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId) {

        requireUser(token);

        Message message = messageService.getMessageById(messageId);

        return MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);
    }

    @PutMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId,
            @RequestBody MessagePutDTO putDTO) {

        requireUser(token);

        messageService.updateMessageStatus(messageId, putDTO);
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId) {

        requireUser(token);

        messageService.deleteMessage(messageId);
    }

    @GetMapping("/messages/scenario/{scenarioId}/pairs")
    @ResponseStatus(HttpStatus.OK)
    public List<MessagePairDTO> getMessagePairs(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId) {

        requireUser(token);

        return messageService.getMessagePairsByScenario(scenarioId);
    }

    @GetMapping("/messages/between/{characterAId}/{characterBId}")
    @ResponseStatus(HttpStatus.OK)
    public List<MessageGetDTO> getMessagesBetween(
            @RequestHeader("Authorization") String token,
            @PathVariable Long characterAId,
            @PathVariable Long characterBId) {

        requireUser(token);

        List<Message> messages =
                messageService.getMessagesBetween(characterAId, characterBId);

        return messages.stream()
                .map(MessageDTOMapper.INSTANCE::convertEntityToGetDTO)
                .toList();
    }

    private void requireUser(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        userService.validateUserToken(header.substring(7));
    }
}