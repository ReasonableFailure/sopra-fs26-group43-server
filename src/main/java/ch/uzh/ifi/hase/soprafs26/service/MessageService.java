package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs26.mapper.MessageDTOMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ScenarioRepository scenarioRepository;
    // private final CharacterRepository characterRepository;

    public MessageService(
            @Qualifier("messageRepository") MessageRepository messageRepository,
            @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository
    ) {
        this.messageRepository = messageRepository;
        this.scenarioRepository = scenarioRepository;
    }

    public Message createMessage(MessagePostDTO postDTO) {

        Scenario scenario = scenarioRepository.findById(postDTO.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));

        if (postDTO.getCreatorId() == null || postDTO.getRecipientId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Sender or recipient missing");
        }

        /*
        Character creator = characterRepository.findById(postDTO.getCreatorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Creator Character not found"));

        Character recipient = characterRepository.findById(postDTO.getRecipientId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recipient Character not found"));
        */

        // Temporary fallback (REMOVE later)
        Character creator = new Character();
        creator.setId(postDTO.getCreatorId());

        Character recipient = new Character();
        recipient.setId(postDTO.getRecipientId());

        Message message = MessageDTOMapper.INSTANCE.convertPostDTOToEntity(postDTO);

        message.setCreatedAt(Instant.now());
        message.setStatus(CommsStatus.PENDING);
        message.setCreator(creator);
        message.setRecipient(recipient);

        message = messageRepository.save(message);

        scenario.getHistory().add(message);
        scenarioRepository.save(scenario);

        return message;
    }

    public Message getMessageById(Long messageId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message not found"));

        return message;
    }

    public void updateMessageStatus(Long messageId, MessagePutDTO putDTO) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message not found"));

        if (putDTO.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status must not be null");
        }

        message.setStatus(putDTO.getStatus());

        messageRepository.save(message);
    }
}