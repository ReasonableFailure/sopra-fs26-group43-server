package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.rest.playerdto.CharacterPutDTO;
import org.mapstruct.*;

@Mapper
public interface PlayerDTOMapper {

    @Mapping(source = "id", target = "userId")
    Character convertCharacterPutDTOtoEntity(CharacterPutDTO characterPutDTO);
}
