package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Pronouncement;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.service.NewsService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsPostDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
public class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsService newsService;

    @MockitoBean
    private PlayerService playerService;

    private ObjectMapper objectMapper;
    private NewsStory testNewsStory;
    private Pronouncement testPronouncement;
    private NewsPostDTO newsPostDTO;
    private Role testRole;
    private Scenario testScenario;

    @BeforeEach
    public void setupTest() {
        objectMapper = new ObjectMapper();

        Mockito.lenient().doNothing().when(playerService).checkToken(anyString(), anyString());

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("Test Role");

        testScenario = new Scenario();
        testScenario.setId(1L);
        testScenario.setPlayers(new ArrayList<>());
        testScenario.getPlayers().add(testRole);

        testNewsStory = new NewsStory();
        testNewsStory.setId(1L);
        testNewsStory.setTitle("Test News");
        testNewsStory.setBody("Test news body");
        testNewsStory.setMastodonStatusId("http://example.com");
        testNewsStory.setCreatedAt(Instant.now());
        testNewsStory.setScenario(testScenario);

        testPronouncement = new Pronouncement();
        testPronouncement.setId(2L);
        testPronouncement.setTitle("Test Pronouncement");
        testPronouncement.setBody("Test pronouncement body");
        testPronouncement.setMastodonStatusId("http://example.com");
        testPronouncement.setCreatedAt(Instant.now());
        testPronouncement.setAuthor(testRole);
        testPronouncement.setLikes(0);
        testPronouncement.setScenario(testScenario);

        newsPostDTO = new NewsPostDTO();
        newsPostDTO.setTitle("Test News");
        newsPostDTO.setBody("Test news body");
        newsPostDTO.setScenarioId(1L);
        newsPostDTO.setAuthorId(1L);
    }

    @Test
    public void createNews_validInputs_pronouncement_success() throws Exception {
        when(newsService.createNews(any(NewsPostDTO.class)))
                .thenReturn(testPronouncement);

        MockHttpServletRequestBuilder postRequest = post("/news")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Role token123")
                .content(asJsonString(newsPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.title", is("Test Pronouncement")))
                .andExpect(jsonPath("$.body", is("Test pronouncement body")))
                .andExpect(jsonPath("$.authorId", is(1)))
                .andExpect(jsonPath("$.likes", is(0)));

        verify(newsService, times(1)).createNews(any(NewsPostDTO.class));
    }

    @Test
    public void createNews_validInputs_newsStory_success() throws Exception {
        newsPostDTO.setAuthorId(null);
        when(newsService.createNews(any(NewsPostDTO.class)))
                .thenReturn(testNewsStory);

        MockHttpServletRequestBuilder postRequest = post("/news")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Role token123")
                .content(asJsonString(newsPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test News")))
                .andExpect(jsonPath("$.body", is("Test news body")))
                .andExpect(jsonPath("$.authorId").doesNotExist())
                .andExpect(jsonPath("$.likes").doesNotExist());

        verify(newsService, times(1)).createNews(any(NewsPostDTO.class));
    }

    @Test
    public void createNews_scenarioNotFound_throwsException() throws Exception {
        when(newsService.createNews(any(NewsPostDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));

        MockHttpServletRequestBuilder postRequest = post("/news")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Role token123")
                .content(asJsonString(newsPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());

        verify(newsService, times(1)).createNews(any(NewsPostDTO.class));
    }

    @Test
    public void createNews_authorNotFound_throwsException() throws Exception {
        when(newsService.createNews(any(NewsPostDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Author role not found"));

        MockHttpServletRequestBuilder postRequest = post("/news")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Role token123")
                .content(asJsonString(newsPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());

        verify(newsService, times(1)).createNews(any(NewsPostDTO.class));
    }

    @Test
    public void getNews_validId_pronouncement_success() throws Exception {
        when(newsService.getNewsById(2L))
                .thenReturn(testPronouncement);

        MockHttpServletRequestBuilder getRequest = get("/news/2")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.title", is("Test Pronouncement")))
                .andExpect(jsonPath("$.body", is("Test pronouncement body")))
                .andExpect(jsonPath("$.authorId", is(1)))
                .andExpect(jsonPath("$.likes", is(0)));

        verify(newsService, times(1)).getNewsById(2L);
    }

    @Test
    public void getNews_validId_newsStory_success() throws Exception {
        when(newsService.getNewsById(1L))
                .thenReturn(testNewsStory);

        MockHttpServletRequestBuilder getRequest = get("/news/1")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test News")))
                .andExpect(jsonPath("$.body", is("Test news body")))
                .andExpect(jsonPath("$.authorId").doesNotExist())
                .andExpect(jsonPath("$.likes").doesNotExist());

        verify(newsService, times(1)).getNewsById(1L);
    }

    @Test
    public void getNews_newsNotFound_throwsException() throws Exception {
        when(newsService.getNewsById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "News item not found"));

        MockHttpServletRequestBuilder getRequest = get("/news/999")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(newsService, times(1)).getNewsById(999L);
    }

    @Test
    public void getNewsByScenario_validScenarioId_success() throws Exception {
        List<NewsStory> newsList = Arrays.asList(testNewsStory, testPronouncement);
        when(newsService.getNewsByScenario(1L))
                .thenReturn(newsList);

        MockHttpServletRequestBuilder getRequest = get("/news/scenario/1")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test News")))
                .andExpect(jsonPath("$[0].authorId").doesNotExist())
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Test Pronouncement")))
                .andExpect(jsonPath("$[1].authorId", is(1)))
                .andExpect(jsonPath("$[1].likes", is(0)));

        verify(newsService, times(1)).getNewsByScenario(1L);
    }

    @Test
    public void getNewsByScenario_scenarioNotFound_throwsException() throws Exception {
        when(newsService.getNewsByScenario(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found"));

        MockHttpServletRequestBuilder getRequest = get("/news/scenario/999")
                .header("Authorization", "Role token123");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        verify(newsService, times(1)).getNewsByScenario(999L);
    }

    private String asJsonString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
