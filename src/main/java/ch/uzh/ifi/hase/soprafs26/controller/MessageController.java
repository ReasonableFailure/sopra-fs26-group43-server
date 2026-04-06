package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.*;
import ch.uzh.ifi.hase.soprafs26.service.MessageService;
import ch.uzh.ifi.hase.soprafs26.mapper.MessageDTOMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class MessageController {

    private final MessageService messageService;

    MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageGetDTO createMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody MessagePostDTO postDTO) {

        // TODO: validate token

        Message message = messageService.createMessage(postDTO);

        return MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);
    }

    @GetMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public MessageGetDTO getMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId) {

        // TODO: validate token

        Message message = messageService.getMessageById(messageId);

        return MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);
    }

    @PutMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long messageId,
            @RequestBody MessagePutDTO putDTO) {

        // TODO: validate token

        messageService.updateMessageStatus(messageId, putDTO);
    }
}