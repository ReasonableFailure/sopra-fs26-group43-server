package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.PlayerPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RoleGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = PlayerMapper.class)
 public interface PlayerDTOMapper {


    PlayerDTOMapper INSTANCE = Mappers.getMapper(PlayerDTOMapper.class);

    Role convertRolePutDTOtoEntity(RolePutDTO rolePutDTO, @MappingTarget Role role);

    RoleGetDTO convertEntitytoRoleGetDTO(Role role);

    Player convertPlayerPutDTOtoEntity(PlayerPutDTO playerPutDTO, @MappingTarget Player player);


}
