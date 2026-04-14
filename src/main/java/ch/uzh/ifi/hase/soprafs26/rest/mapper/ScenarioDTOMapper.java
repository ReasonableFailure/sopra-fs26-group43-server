package ch.uzh.ifi.hase.soprafs26.rest.mapper;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import org.springframework.web.bind.annotation.RequestHeader;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioDeleteDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScenarioDTOMapper {
    ScenarioDTOMapper INSTANCE = Mappers.getMapper(ScenarioDTOMapper.class);

    ScenarioGetDTO convertEntityToScenarioGetDTO(Scenario scenario);

    Scenario convertScenarioPostDTOtoEntity(ScenarioPostDTO scenarioPostDTO);

    Scenario convertScenarioPutDTOtoEntity(ScenarioPutDTO scenarioPutDTO, @MappingTarget Scenario scenario);
}
