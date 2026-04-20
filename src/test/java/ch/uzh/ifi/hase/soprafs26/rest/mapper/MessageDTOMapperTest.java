package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.mapper.MessageDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessageGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.MessagePostDTO;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageDTOMapperTest {

    @Test
    public void testCreateMessage_fromMessagePostDTO_toMessage_success() {
        MessagePostDTO messagePostDTO = new MessagePostDTO();
        messagePostDTO.setTitle("Test Message");
        messagePostDTO.setBody("Test Body");
        messagePostDTO.setCreatorId(1L);
        messagePostDTO.setRecipientId(2L);
        messagePostDTO.setScenarioId(3L);

        Message message = MessageDTOMapper.INSTANCE.convertPostDTOToEntity(messagePostDTO);

        assertEquals(messagePostDTO.getTitle(), message.getTitle());
        assertEquals(messagePostDTO.getBody(), message.getBody());
    }

    @Test
    public void testGetMessage_fromMessage_toMessageGetDTO_success() {
        Role creator = new Role();
        creator.setId(1L);
        creator.setName("Test Creator");

        Role recipient = new Role();
        recipient.setId(2L);
        recipient.setName("Test Recipient");

        Message message = new Message();
        message.setId(1L);
        message.setTitle("Test Message");
        message.setBody("Test Body");
        message.setCreatedAt(Instant.now());
        message.setStatus(CommsStatus.PENDING);
        message.setCreator(creator);
        message.setRecipient(recipient);

        MessageGetDTO messageGetDTO = MessageDTOMapper.INSTANCE.convertEntityToGetDTO(message);

        assertEquals(message.getId(), messageGetDTO.getId());
        assertEquals(message.getTitle(), messageGetDTO.getTitle());
        assertEquals(message.getBody(), messageGetDTO.getBody());
        assertEquals(message.getCreatedAt(), messageGetDTO.getCreatedAt());
        assertEquals(message.getStatus(), messageGetDTO.getStatus());
        assertEquals(message.getCreator().getId(), messageGetDTO.getCreatorId());
        assertEquals(message.getRecipient().getId(), messageGetDTO.getRecipientId());
    }
}

