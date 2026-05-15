package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Base64;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserDTOMapper {

    UserDTOMapper INSTANCE = Mappers.getMapper(UserDTOMapper.class);

    @Mapping(target = "profilePic", source = "profilePic", qualifiedByName = "userPicBytesToBase64")
    UserGetDTO convertEntityToUserGetDTO(User user);

    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    User convertUserLoginDTOtoEntity(UserLoginDTO userLoginDTO);

    // UserPutDTO.profilePic (String) → User.profilePic (byte[]) is handled
    // explicitly in UserService.updateProfile so we can distinguish
    // "field omitted" (no change) from "field present and null" (clear it).
    // Mapping the String → byte[] field directly here would always overwrite.
    @Mapping(target = "profilePic", ignore = true)
    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

    @Named("userPicBytesToBase64")
    default String userPicBytesToBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:image/jpeg;base64," + base64;
    }
}
