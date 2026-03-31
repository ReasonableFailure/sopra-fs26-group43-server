package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPutDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.userdto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;

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
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    UserGetDTO convertEntityToUserGetDTO(User user);

    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    User convertUserLoginDTOtoEntity(UserLoginDTO userLoginDTO);

    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "day", target = "day")
    @Mapping(source = "exchangeRate", target = "exchangeRate")
    ScenarioGetDTO convertEntityToScenarioGetDTO(Scenario scenario);

}
