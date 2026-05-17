package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Pronouncement;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.repository.NewsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsPostDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;

@WebAppConfiguration
@SpringBootTest
public class NewsServiceIntegrationTest {

	@Qualifier("newsRepository")
	@Autowired
	private NewsRepository newsRepository;

	@Qualifier("scenarioRepository")
	@Autowired
	private ScenarioRepository scenarioRepository;

	@Qualifier("roleRepository")
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private NewsService newsService;

	@MockitoBean
	private MastodonClient mastodonClient;

	private Scenario testScenario;
	private Role testRole;

	@BeforeEach
	public void setup() {
		newsRepository.deleteAll();
		scenarioRepository.deleteAll();
		roleRepository.deleteAll();

		testRole = new Role();
		testRole.setToken("test-token");
		testRole.setName("Test Role");
		testRole.setTitle("Test Title");
		testRole.setDescription("Test description");
		testRole.setSecret("secret");
		testRole.setAlive(true);
		testRole.setMessageCount(5);
		testRole.setTotalPoints(10);
		testRole.setPointsBalance(10);

		testScenario = new Scenario();
		testScenario.setTitle("Test Scenario");
		testScenario.setDescription("Test scenario description");
		testScenario.setStatus(ScenarioStatus.UNSTARTED);
		testScenario.setPlayers(new ArrayList<>());
		testScenario.setHistory(new ArrayList<>());
		testScenario.getPlayers().add(testRole);
		testScenario = scenarioRepository.save(testScenario);
		testRole = (Role) testScenario.getPlayers().get(0);
	}

	@Test
	public void createNews_validInputs_pronouncement_success() {
		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(testScenario.getId());
		postDTO.setAuthorId(testRole.getId());

		NewsStory createdNews = newsService.createNews(postDTO);

		assertNotNull(createdNews.getId());
		assertEquals(postDTO.getTitle(), createdNews.getTitle());
		assertEquals(postDTO.getBody(), createdNews.getBody());
		assertEquals(testScenario.getId(), createdNews.getScenario().getId());
		assertNotNull(createdNews.getCreatedAt());
		assertTrue(createdNews instanceof Pronouncement);
		Pronouncement p = (Pronouncement) createdNews;
		assertEquals(testRole.getId(), p.getAuthor().getId());
		assertEquals(0, p.getLikes());
	}

	@Test
	public void createNews_validInputs_newsStory_success() {
		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(testScenario.getId());
		postDTO.setAuthorId(null);

		NewsStory createdNews = newsService.createNews(postDTO);

		assertNotNull(createdNews.getId());
		assertEquals(postDTO.getTitle(), createdNews.getTitle());
		assertEquals(postDTO.getBody(), createdNews.getBody());
		assertEquals(testScenario.getId(), createdNews.getScenario().getId());
		assertNotNull(createdNews.getCreatedAt());
		assertTrue(createdNews instanceof NewsStory);
		assertFalse(createdNews instanceof Pronouncement);
	}

	@Test
	public void createNews_scenarioNotFound_throwsException() {
		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(999L);
		postDTO.setAuthorId(testRole.getId());

		assertThrows(ResponseStatusException.class, () -> newsService.createNews(postDTO));
	}

	@Test
	public void createNews_authorNotFound_throwsException() {
		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(testScenario.getId());
		postDTO.setAuthorId(999L);

		assertThrows(ResponseStatusException.class, () -> newsService.createNews(postDTO));
	}

	@Test
	public void deleteNews_validInput_success() {
		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(testScenario.getId());
		postDTO.setAuthorId(testRole.getId());

		NewsStory createdNews = newsService.createNews(postDTO);

		assertDoesNotThrow(() -> newsService.deleteNews(createdNews.getId()));
		assertFalse(newsRepository.findById(createdNews.getId()).isPresent());
	}

	@Test
	public void deleteNews_NewsNotFound_ThrowsException() {
		assertThrows(ResponseStatusException.class, () -> newsService.deleteNews(999L));
	}

	@Test
	public void postToMastodon_success() {
		testScenario.setMastodonBaseUrl("https://mastodon.example");
		testScenario.setMastodonAccessToken("token");
		testScenario = scenarioRepository.save(testScenario);
		Mockito.doReturn("mastodon-123")
			.when(mastodonClient).postStatus(
			Mockito.anyString(),
			Mockito.anyString(),
			Mockito.anyString()
		);

		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(testScenario.getId());
		postDTO.setAuthorId(null);

		NewsStory createdNews = newsService.createNews(postDTO);

		Mockito.verify(mastodonClient, Mockito.times(1))
			.postStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		assertEquals("mastodon-123",
			newsRepository.findById(createdNews.getId())
				.orElseThrow()
				.getMastodonStatusId());
	}

	@Test
	public void postToMastodon_throwsException() {
		testScenario.setMastodonBaseUrl("https://mastodon.example");
		testScenario.setMastodonAccessToken("token");
		testScenario = scenarioRepository.save(testScenario);
		Mockito.doThrow(new RuntimeException("boom"))
			.when(mastodonClient).postStatus(
			Mockito.anyString(),
			Mockito.anyString(),
			Mockito.anyString()
		);

		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(testScenario.getId());
		postDTO.setAuthorId(null);

		NewsStory createdNews = newsService.createNews(postDTO);

		assertNull(createdNews.getMastodonStatusId());
	}

	@Test
	public void getNewsById_validId_pronouncement_success() {
		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(testScenario.getId());
		postDTO.setAuthorId(testRole.getId());

		NewsStory createdNews = newsService.createNews(postDTO);

		NewsStory retrievedNews = newsService.getNewsById(createdNews.getId());

		assertEquals(createdNews.getId(), retrievedNews.getId());
		assertEquals(createdNews.getTitle(), retrievedNews.getTitle());
		assertEquals(createdNews.getBody(), retrievedNews.getBody());
		assertEquals(createdNews.getScenario().getId(), retrievedNews.getScenario().getId());
		assertTrue(retrievedNews instanceof Pronouncement);
		Pronouncement p = (Pronouncement) retrievedNews;
		assertEquals(testRole.getId(), p.getAuthor().getId());
	}

	@Test
	public void getNewsById_validId_newsStory_success() {
		NewsPostDTO postDTO = new NewsPostDTO();
		postDTO.setTitle("Test News");
		postDTO.setBody("Test news body");
		postDTO.setScenarioId(testScenario.getId());
		postDTO.setAuthorId(null);

		NewsStory createdNews = newsService.createNews(postDTO);

		NewsStory retrievedNews = newsService.getNewsById(createdNews.getId());

		assertEquals(createdNews.getId(), retrievedNews.getId());
		assertEquals(createdNews.getTitle(), retrievedNews.getTitle());
		assertEquals(createdNews.getBody(), retrievedNews.getBody());
		assertEquals(createdNews.getScenario().getId(), retrievedNews.getScenario().getId());
		assertTrue(retrievedNews instanceof NewsStory);
		assertFalse(retrievedNews instanceof Pronouncement);
	}

	@Test
	public void getNewsById_newsNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> newsService.getNewsById(999L));
	}

	@Test
	public void getNewsByScenario_validScenarioId_success() {
		NewsPostDTO postDTO1 = new NewsPostDTO();
		postDTO1.setTitle("Test News 1");
		postDTO1.setBody("Test news body 1");
		postDTO1.setScenarioId(testScenario.getId());
		postDTO1.setAuthorId(testRole.getId());

		NewsPostDTO postDTO2 = new NewsPostDTO();
		postDTO2.setTitle("Test News 2");
		postDTO2.setBody("Test news body 2");
		postDTO2.setScenarioId(testScenario.getId());
		postDTO2.setAuthorId(null);

		newsService.createNews(postDTO1);
		newsService.createNews(postDTO2);

		List<NewsStory> newsList = newsService.getNewsByScenario(testScenario.getId());

		assertEquals(2, newsList.size());
		assertTrue(newsList.get(0) instanceof Pronouncement);
		assertTrue(newsList.get(1) instanceof NewsStory);
		assertFalse(newsList.get(1) instanceof Pronouncement);
	}

	@Test
	public void getNewsByScenario_scenarioNotFound_throwsException() {
		assertThrows(ResponseStatusException.class, () -> newsService.getNewsByScenario(999L));
	}
}
