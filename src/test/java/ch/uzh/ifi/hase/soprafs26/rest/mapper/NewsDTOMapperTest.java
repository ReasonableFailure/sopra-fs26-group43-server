package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Pronouncement;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.NewsDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsPostDTO;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NewsDTOMapperTest {

	@Test
	public void testConvertPostDTOToNewsStory_success() {
		NewsPostDTO newsPostDTO = new NewsPostDTO();
		newsPostDTO.setTitle("Test News");
		newsPostDTO.setBody("Test Body");
		newsPostDTO.setScenarioId(1L);
		newsPostDTO.setAuthorId(1L);

		NewsStory newsStory = NewsDTOMapper.INSTANCE.convertPostDTOToNewsStory(newsPostDTO);

		assertEquals(newsPostDTO.getTitle(), newsStory.getTitle());
		assertEquals(newsPostDTO.getBody(), newsStory.getBody());
		assertNull(newsStory.getId());
		assertNull(newsStory.getCreatedAt());
		assertNull(newsStory.getScenario());
	}

	@Test
	public void testConvertEntityToGetDTO_fromNewsStory_success() {
		Role author = new Role();
		author.setId(1L);
		author.setName("Test Role");

		Scenario scenario = new Scenario();
		scenario.setId(1L);

		NewsStory newsStory = new NewsStory();
		newsStory.setId(1L);
		newsStory.setTitle("Test News");
		newsStory.setBody("Test Body");
		newsStory.setMastodonStatusId("http://example.com");
		newsStory.setCreatedAt(Instant.now());
		newsStory.setScenario(scenario);

		NewsGetDTO newsGetDTO = NewsDTOMapper.INSTANCE.convertEntityToGetDTO(newsStory);

		assertEquals(newsStory.getId(), newsGetDTO.getId());
		assertEquals(newsStory.getTitle(), newsGetDTO.getTitle());
		assertEquals(newsStory.getBody(), newsGetDTO.getBody());
		assertEquals(newsStory.getCreatedAt(), newsGetDTO.getCreatedAt());
		assertNull(newsGetDTO.getAuthorId());
		assertNull(newsGetDTO.getLikes());
	}

	@Test
	public void testConvertEntityToGetDTO_fromPronouncement_success() {
		Role author = new Role();
		author.setId(1L);
		author.setName("Test Role");

		Scenario scenario = new Scenario();
		scenario.setId(1L);

		Pronouncement pronouncement = new Pronouncement();
		pronouncement.setId(2L);
		pronouncement.setTitle("Test Pronouncement");
		pronouncement.setBody("Test Body");
		pronouncement.setMastodonStatusId("http://example.com");
		pronouncement.setCreatedAt(Instant.now());
		pronouncement.setAuthor(author);
		pronouncement.setLikes(5);
		pronouncement.setScenario(scenario);

		NewsGetDTO newsGetDTO = NewsDTOMapper.INSTANCE.convertEntityToGetDTO(pronouncement);

		assertEquals(pronouncement.getId(), newsGetDTO.getId());
		assertEquals(pronouncement.getTitle(), newsGetDTO.getTitle());
		assertEquals(pronouncement.getBody(), newsGetDTO.getBody());
		assertEquals(pronouncement.getCreatedAt(), newsGetDTO.getCreatedAt());
		assertNull(newsGetDTO.getAuthorId());
		assertNull(newsGetDTO.getLikes());
	}
}
