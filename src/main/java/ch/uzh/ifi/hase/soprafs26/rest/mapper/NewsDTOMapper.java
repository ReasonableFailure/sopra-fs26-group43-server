package ch.uzh.ifi.hase.soprafs26.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NewsDTOMapper {

    NewsDTOMapper INSTANCE = Mappers.getMapper(NewsDTOMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "scenario", ignore = true)
    NewsStory convertPostDTOToNewsStory(NewsPostDTO dto);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "body", target = "body")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "postURI", target = "postURI")
    NewsGetDTO convertEntityToGetDTO(NewsStory entity);
}