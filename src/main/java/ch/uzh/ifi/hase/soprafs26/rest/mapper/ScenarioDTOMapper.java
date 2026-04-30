package ch.uzh.ifi.hase.soprafs26.rest.mapper;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScenarioDTOMapper {
    ScenarioDTOMapper INSTANCE = Mappers.getMapper(ScenarioDTOMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "dayNumber", target = "dayNumber")
    @Mapping(source = "exchangeRate", target = "exchangeRate")
    @Mapping(source = "director.token", target = "directorToken")
    ScenarioGetDTO convertEntityToScenarioGetDTO(Scenario scenario);

    @Mapping(target = "players", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dayNumber", ignore = true)
    @Mapping(target = "history", ignore = true)
    @Mapping(target = "mastodonBaseUrl", ignore = true)
    @Mapping(target = "mastodonAccessToken", ignore = true)
    @Mapping(target = "director", ignore = true)
    @Mapping(target = "mastodonProfileUrl", ignore = true)
    Scenario convertScenarioPostDTOtoEntity(ScenarioPostDTO scenarioPostDTO);
}
