package ch.uzh.ifi.hase.soprafs26.rest.mapper;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScenarioDTOMapper {
    ScenarioDTOMapper INSTANCE = Mappers.getMapper(ScenarioDTOMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "day", target = "day")
    @Mapping(source = "exchangeRate", target = "exchangeRate")
    ScenarioGetDTO convertEntityToScenarioGetDTO(Scenario scenario);


}
