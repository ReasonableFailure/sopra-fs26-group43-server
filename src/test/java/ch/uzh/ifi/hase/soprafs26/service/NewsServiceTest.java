package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Pronouncement;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.repository.NewsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsPostDTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class NewsServiceTest {

	@Mock
	private NewsRepository newsRepository;

	@Mock
	private ScenarioRepository scenarioRepository;

	@Mock
	private RoleRepository roleRepository;

	@InjectMocks
	private NewsService newsService;

	private NewsPostDTO testNewsPostDTO;
	private Scenario testScenario;
	private Role testRole;
	private NewsStory testNewsStory;
	private Pronouncement testPronouncement;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		testNewsPostDTO = new NewsPostDTO();
		testNewsPostDTO.setTitle("Test News");
		testNewsPostDTO.setBody("Test news body");
		testNewsPostDTO.setPostURI("http://example.com");
		testNewsPostDTO.setScenarioId(1L);
		testNewsPostDTO.setAuthorId(1L);

		testScenario = new Scenario();
		testScenario.setId(1L);
		testScenario.setPlayers(new ArrayList<>());
		testScenario.setHistory(new ArrayList<>());

		testRole = new Role();
		testRole.setId(1L);
		testRole.setName("Test Role");

		testScenario.getPlayers().add(testRole);

		testNewsStory = new NewsStory();
		testNewsStory.setId(1L);
		testNewsStory.setTitle("Test News");
		testNewsStory.setBody("Test news body");
		testNewsStory.setPostURI("http://example.com");
		testNewsStory.setCreatedAt(Instant.now());
		testNewsStory.setScenario(testScenario);

		testPronouncement = new Pronouncement();
		testPronouncement.setId(2L);
		testPronouncement.setTitle("Test News");
		testPronouncement.setBody("Test news body");
		testPronouncement.setPostURI("http://example.com");
		testPronouncement.setCreatedAt(Instant.now());
		testPronouncement.setAuthor(testRole);
		testPronouncement.setLikes(0);
		testPronouncement.setScenario(testScenario);

		Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
		Mockito.when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
		Mockito.when(newsRepository.save(Mockito.any())).thenAnswer(invocation -> {
			Object arg = invocation.getArgument(0);
			if (arg instanceof Pronouncement) {
				return testPronouncement;
			} else {
				return testNewsStory;
			}
		});
		Mockito.when(scenarioRepository.save(Mockito.any())).thenReturn(testScenario);
	}

	@Test
	public void createNews_validInputs_pronouncement_success() {
		NewsStory createdNews = newsService.createNews(testNewsPostDTO);

		Mockito.verify(newsRepository, Mockito.times(1)).save(Mockito.any());
		Mockito.verify(scenarioRepository, Mockito.times(1)).save(testScenario);

		assertEquals(testNewsPostDTO.getTitle(), createdNews.getTitle());
		assertEquals(testNewsPostDTO.getBody(), createdNews.getBody());
		assertEquals(testNewsPostDTO.getPostURI(), createdNews.getPostURI());
		assertEquals(testScenario, createdNews.getScenario());
		assertNotNull(createdNews.getCreatedAt());
		assertTrue(createdNews instanceof Pronouncement);
		Pronouncement p = (Pronouncement) createdNews;
		assertEquals(testRole, p.getAuthor());
		assertEquals(0, p.getLikes());
	}

	@Test
	public void createNews_validInputs_newsStory_success() {
		testNewsPostDTO.setAuthorId(null);
		Mockito.when(newsRepository.save(Mockito.any())).thenReturn(testNewsStory);

		NewsStory createdNews = newsService.createNews(testNewsPostDTO);

		Mockito.verify(newsRepository, Mockito.times(1)).save(Mockito.any());
		Mockito.verify(scenarioRepository, Mockito.times(1)).save(testScenario);

		assertEquals(testNewsPostDTO.getTitle(), createdNews.getTitle());
		assertEquals(testNewsPostDTO.getBody(), createdNews.getBody());
		assertEquals(testNewsPostDTO.getPostURI(), createdNews.getPostURI());
		assertEquals(testScenario, createdNews.getScenario());
		assertNotNull(createdNews.getCreatedAt());
		assertTrue(createdNews instanceof NewsStory);
		assertFalse(createdNews instanceof Pronouncement);
	}

	@Test
	public void createNews_scenarioNotFound_throwsException() {
		Mockito.when(scenarioRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> newsService.createNews(testNewsPostDTO));
	}

	@Test
	public void createNews_authorNotFound_throwsException() {
		Mockito.when(roleRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> newsService.createNews(testNewsPostDTO));
	}

	@Test
	public void getNewsById_validId_pronouncement_success() {
		Mockito.when(newsRepository.findById(2L)).thenReturn(Optional.of(testPronouncement));

		NewsStory result = newsService.getNewsById(2L);

		assertEquals(testPronouncement, result);
		Mockito.verify(newsRepository, Mockito.times(1)).findById(2L);
	}

	@Test
	public void getNewsById_validId_newsStory_success() {
		Mockito.when(newsRepository.findById(1L)).thenReturn(Optional.of(testNewsStory));

		NewsStory result = newsService.getNewsById(1L);

		assertEquals(testNewsStory, result);
		Mockito.verify(newsRepository, Mockito.times(1)).findById(1L);
	}

	@Test
	public void getNewsById_newsNotFound_throwsException() {
		Mockito.when(newsRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> newsService.getNewsById(1L));
	}

	@Test
	public void getNewsByScenario_validScenarioId_success() {
		List<NewsStory> expectedNews = Arrays.asList(testNewsStory, testPronouncement);
		Mockito.when(scenarioRepository.existsById(1L)).thenReturn(true);
		Mockito.when(newsRepository.findByScenarioIdOrderByCreatedAtAsc(1L)).thenReturn(expectedNews);

		List<NewsStory> result = newsService.getNewsByScenario(1L);

		assertEquals(expectedNews, result);
		Mockito.verify(scenarioRepository, Mockito.times(1)).existsById(1L);
		Mockito.verify(newsRepository, Mockito.times(1)).findByScenarioIdOrderByCreatedAtAsc(1L);
	}

	@Test
	public void getNewsByScenario_scenarioNotFound_throwsException() {
		Mockito.when(scenarioRepository.existsById(1L)).thenReturn(false);

		assertThrows(ResponseStatusException.class, () -> newsService.getNewsByScenario(1L));
	}
}
