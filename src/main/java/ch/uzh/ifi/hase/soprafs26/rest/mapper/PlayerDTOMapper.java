package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Base64;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
 public interface PlayerDTOMapper {


    PlayerDTOMapper INSTANCE = Mappers.getMapper(PlayerDTOMapper.class);
    //since they are named the same, automatic mapping
    @Mapping(target = "portrait", source = "portrait", qualifiedByName = "bytesToBase64")
    RoleGetDTO convertEntitytoRoleGetDTO(Role role);

    @Mapping(target = "portrait", source = "portrait", qualifiedByName = "base64ToBytes")
    Role convertRolePostDTOtoEntity(RolePostDTO rolePostDTO);

    @Mapping(target = "portrait", source = "portrait", qualifiedByName = "base64ToBytes")
    Role convertRolePutDTOtoEntity(RolePutDTO rolePutDTO);

    BackroomerGetDTO convertEntitytoBackroomerGetDTO(Backroomer Backroomer);

    @Mapping(source = "id", target = "directorId")
    @Mapping(source = "token", target = "directorToken")
    DirectorGetDTO convertEntityToDirectorGetDTO(Director director);

    @AfterMapping
    default void addRolePrefix(@MappingTarget RoleGetDTO roleGetDTO, Role entity) {
        if (entity.getToken() == null) return;
        roleGetDTO.setToken("Role " + entity.getToken());
    }

    @AfterMapping
    default void addPrefix(@MappingTarget BackroomerGetDTO backroomerGetDTO, Backroomer entity) {
        if (entity.getToken() == null) return;
        bacroomerGetDTO.setToken("Backroomer " + entity.getToken());}


        @AfterMapping
    default void addPrefix(@MappingTarget DirectorGetDTO directorGetDTO, Director entity) {
        if (entity.getToken() == null) return;
        directorGetDTO.setToken("Director " + entity.getToken());
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
