package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsPostDTO;
import ch.uzh.ifi.hase.soprafs26.mapper.NewsDTOMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final ScenarioRepository scenarioRepository;
    private final RoleRepository roleRepository;
    private final MastodonClient mastodonClient;

    public NewsService(
            @Qualifier("newsRepository") NewsRepository newsRepository,
            @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository,
            @Qualifier("roleRepository") RoleRepository roleRepository,
            MastodonClient mastodonClient
    ) {
        this.newsRepository = newsRepository;
        this.scenarioRepository = scenarioRepository;
        this.roleRepository = roleRepository;
        this.mastodonClient = mastodonClient;
    }

    public NewsStory createNews(NewsPostDTO dto) {

        Scenario scenario = scenarioRepository.findById(dto.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));

        NewsStory entity;

        if (dto.getAuthorId() != null) {

            Role author = roleRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Author role not found"));

            Pronouncement p = new Pronouncement();
            p.setTitle(dto.getTitle());
            p.setBody(dto.getBody());
            p.setPostURI(dto.getPostURI());
            p.setCreatedAt(Instant.now());
            p.setLikes(0);
            p.setAuthor(author);
            p.setScenario(scenario);

            entity = p;

        } else {
            entity = NewsDTOMapper.INSTANCE.convertPostDTOToNewsStory(dto);
            entity.setCreatedAt(Instant.now());
            entity.setScenario(scenario);
        }

        entity = newsRepository.save(entity);

        scenario.getHistory().add(entity);
        scenarioRepository.save(scenario);

        try {
            mastodonClient.postStatus(
                    scenario.getMastodonBaseUrl(),
                    scenario.getMastodonAccessToken(),
                    entity.formatSelf()
            );
        } catch (Exception e) {
            System.err.println("Failed to post to Mastodon: " + e.getMessage());
        }

        return entity;
    }

    public NewsStory getNewsById(Long newsId) {

        NewsStory news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "News item not found"));

        return news;
    }

    public List<NewsStory> getNewsByScenario(Long scenarioId) {

        if (!scenarioRepository.existsById(scenarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Scenario not found");
        }

        return newsRepository.findByScenarioIdOrderByCreatedAtAsc(scenarioId);
    }
}