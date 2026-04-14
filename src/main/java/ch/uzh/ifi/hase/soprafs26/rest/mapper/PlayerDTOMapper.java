package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RoleGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import org.mapstruct.*;

@Mapper
public interface PlayerDTOMapper {
    Character convertCharacterPutDTOtoEntity(RolePutDTO rolePutDTO, @MappingTarget Character character);
    RoleGetDTO convertEntitytoCharacterGetDTO(Character character);

}
