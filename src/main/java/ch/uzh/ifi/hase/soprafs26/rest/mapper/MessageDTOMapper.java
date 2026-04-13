package ch.uzh.ifi.hase.soprafs26.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.rest.messagedto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageDTOMapper {

    MessageDTOMapper INSTANCE = Mappers.getMapper(MessageDTOMapper.class);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "body", target = "body")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "recipient", ignore = true)
    @Mapping(target = "scenario", ignore = true)
    Message convertPostDTOToEntity(MessagePostDTO dto);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "body", target = "body")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "creator.id", target = "creatorId")
    @Mapping(source = "recipient.id", target = "recipientId")
    MessageGetDTO convertEntityToGetDTO(Message message);
}