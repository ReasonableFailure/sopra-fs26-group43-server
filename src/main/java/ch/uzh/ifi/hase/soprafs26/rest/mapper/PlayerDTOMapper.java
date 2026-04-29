package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = PlayerMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
 public interface PlayerDTOMapper {


    PlayerDTOMapper INSTANCE = Mappers.getMapper(PlayerDTOMapper.class);

    Role convertRolePutDTOtoEntity(RolePutDTO rolePutDTO, @MappingTarget Role role);

    RoleGetDTO convertEntitytoRoleGetDTO(Role role);

    @Mapping(source = "newAssignedUserId", target = "id")
    Player convertPlayerPutDTOtoEntity(PlayerPutDTO playerPutDTO);

    Role convertRolePostDTOtoEntity(RolePostDTO rolePostDTO);

    @Mapping(source = "token", target = "authToken")
    @Mapping(source = "id", target = "id")
    PlayerGetDTO convertEntitytoPlayerGetDTO(Player player);

    @AfterMapping
    default void addPrefix(@MappingTarget PlayerGetDTO playerGetDTO, Player entity) {
        if (entity.getToken() == null) return;

        String prefix = "";
        if (entity instanceof Role) {
            prefix = "Role ";
        } else if (entity instanceof Director) {
            prefix = "Director ";
        } else if (entity instanceof Backroomer) {
            prefix = "Backroomer ";
        }

        playerGetDTO.setAuthToken(prefix + entity.getToken());
    }
}
