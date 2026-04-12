package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import org.mapstruct.*;

@Mapper
public interface PlayerDTOMapper {

    @Mapping(source = "userId", target = "id")
    Role convertRolePutDTOtoEntity(RolePutDTO rolePutDTO);
}
