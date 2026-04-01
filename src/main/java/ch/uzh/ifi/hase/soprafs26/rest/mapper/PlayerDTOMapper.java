package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.rest.playerdto.CharacterPutDTO;
import org.mapstruct.Mapper;

@Mapper
public interface PlayerDTOMapper {
    Character convertCharacterPutDTOtoEntity(CharacterPutDTO characterPutDTO);
}
