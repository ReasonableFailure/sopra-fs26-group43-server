package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Base64;
import org.mapstruct.Named;
import org.mapstruct.Mapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = PlayerMapper.class)
 public interface PlayerDTOMapper {


    PlayerDTOMapper INSTANCE = Mappers.getMapper(PlayerDTOMapper.class);

    Role convertRolePutDTOtoEntity(RolePutDTO rolePutDTO, @MappingTarget Role role);

    @Mapping(target = "portrait", source = "portrait", qualifiedByName = "bytesToBase64")
    RoleGetDTO convertEntitytoRoleGetDTO(Role role);

    Player convertPlayerPutDTOtoEntity(PlayerPutDTO playerPutDTO, @MappingTarget Player player);

    @Mapping(target = "portrait", source = "portrait", qualifiedByName = "base64ToBytes")
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

    @Named("base64ToBytes")
    default byte[] base64ToBytes(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }

        String[] parts = base64.split(",");

        String data = parts.length > 1 ? parts[1] : parts[0];

        return Base64.getDecoder().decode(data);
    }

    @Named("bytesToBase64")
    default String bytesToBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        String base64 = Base64.getEncoder().encodeToString(bytes);

        return "data:image/jpeg;base64," + base64;
    }
}
