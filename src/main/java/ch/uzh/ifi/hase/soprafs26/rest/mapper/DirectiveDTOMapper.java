package ch.uzh.ifi.hase.soprafs26.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.Directive;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectiveGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePostDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DirectiveDTOMapper {

    DirectiveDTOMapper INSTANCE = Mappers.getMapper(DirectiveDTOMapper.class);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "body", target = "body")
    @Mapping(source = "category", target = "category")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "scenario", ignore = true)
    @Mapping(target = "response", ignore = true)
    Directive convertPostDTOToEntity(DirectivePostDTO dto);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "body", target = "body")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "creator.id", target = "creatorId")
    @Mapping(source = "category", target = "category")
    DirectiveGetDTO convertEntityToGetDTO(Directive directive);
}