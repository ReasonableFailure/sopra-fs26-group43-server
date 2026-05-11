package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePairDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.MessageDTOMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ScenarioRepository scenarioRepository;
    private final RoleRepository roleRepository;
    private final PlayerRepository playerRepository;

    public MessageService(
            @Qualifier("messageRepository") MessageRepository messageRepository,
            @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository,
            @Qualifier("roleRepository") RoleRepository roleRepository,
            @Qualifier("playerRepository") PlayerRepository playerRepository
    ) {
        this.messageRepository = messageRepository;
        this.scenarioRepository = scenarioRepository;
        this.roleRepository = roleRepository;
        this.playerRepository = playerRepository;
    }

    public Message createMessage(MessagePostDTO postDTO) {

        Scenario scenario = scenarioRepository.findById(postDTO.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));

        if (postDTO.getCreatorId() == null || postDTO.getRecipientId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Sender or recipient missing");
        }

        Role creator = roleRepository.findById(postDTO.getCreatorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Creator role not found"));

        Role recipient = roleRepository.findById(postDTO.getRecipientId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recipient role not found"));

        if (creator.getId().equals(recipient.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot send message to self");
        }

        creator.useMessageSlot();
        roleRepository.save(creator);

        Message message = MessageDTOMapper.INSTANCE.convertPostDTOToEntity(postDTO);

        message.setCreatedAt(Instant.now());
        message.setStatus(CommsStatus.PENDING);
        message.setCreator(creator);
        message.setRecipient(recipient);
        message.setScenario(scenario);

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

        Role creator = message.getCreator();
        creator.setNumberMessages(creator.getNumberMessages() + 1);
        creator.setTotalTextLength(creator.getTotalTextLength() + message.totalTextLength());
        roleRepository.save(creator);

        message.setStatus(putDTO.getStatus());

        messageRepository.save(message);
    }

    public List<Message> getMessagesBetween(String callerToken, Long characterAId, Long characterBId) {

        if (!roleRepository.existsById(characterAId) ||
                !roleRepository.existsById(characterBId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "One or both characters not found");
        }

        if (characterAId.equals(characterBId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot retrieve conversation with self");
        }

        Player caller = playerRepository.findByToken(callerToken).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (caller == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Unknown caller token");
        }

        List<Message> raw = messageRepository.findConversation(characterAId, characterBId);

        // Backroomer (and Director, which extends Backroomer) need full visibility
        // for moderation. Cross-scenario scope checks are deferred to B2.
        if (caller instanceof Backroomer) {
            return raw;
        }

        // Player Roles may only view conversations they participate in,
        // and may only see messages they sent or messages addressed to them
        // that have been approved (status == ACCEPTED).
        Long callerId = caller.getId();
        if (!callerId.equals(characterAId) && !callerId.equals(characterBId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only conversation participants may view this thread");
        }

        return raw.stream()
                .filter(m -> m.getCreator().getId().equals(callerId)
                          || m.getStatus() == CommsStatus.ACCEPTED)
                .toList();
    }

    public List<MessagePairDTO> getMessagePairsByScenario(Long scenarioId) {

        if (!scenarioRepository.existsById(scenarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Scenario not found");
        }

        List<Message> messages = messageRepository.findByScenarioId(scenarioId);

        Set<String> seenPairs = new HashSet<>();
        List<MessagePairDTO> result = new ArrayList<>();

        for (Message msg : messages) {

            Long a = msg.getCreator().getId();
            Long b = msg.getRecipient().getId();

            Long min = Math.min(a, b);
            Long max = Math.max(a, b);

            String key = min + "-" + max;

            if (!seenPairs.contains(key)) {
                seenPairs.add(key);

                MessagePairDTO dto = new MessagePairDTO();
                dto.setRoleAId(min);
                dto.setRoleBId(max);

                result.add(dto);
            }
        }

        return result;
    }
}