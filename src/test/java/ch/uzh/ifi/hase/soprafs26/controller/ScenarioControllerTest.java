package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.service.ScenarioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScenarioController.class)
public class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScenarioService scenarioService;
    /*
    @Test
    public void getAllScenarios_returnsListOfScenarios() throws Exception {
        // given
        Scenario scenario = new Scenario();
        scenario.setId(1L);
        scenario.setTitle("The Trojan War");
        scenario.setDescription("A crisis simulation of the Trojan War");
        scenario.setIsActive(false);
        scenario.setdayNumber(0);
        scenario.setExchangeRate(10);

        List<Scenario> allScenarios = Collections.singletonList(scenario);

        given(scenarioService.getScenarios()).willReturn(allScenarios);

        // when/then
        mockMvc.perform(get("/scenarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(scenario.getTitle())))
                .andExpect(jsonPath("$[0].description", is(scenario.getDescription())))
                .andExpect(jsonPath("$[0].getActive", is(false)))
                .andExpect(jsonPath("$[0].dayNumber", is(0)))
                .andExpect(jsonPath("$[0].exchangeRate", is(10)));
    }

    @Test
    public void getAllScenarios_emptyList_returnsEmptyArray() throws Exception {
        // given
        given(scenarioService.getScenarios()).willReturn(Collections.emptyList());

        // when/then
        mockMvc.perform(get("/scenarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

     */
}
