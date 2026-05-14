package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.NewsDTOMapper;

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
    private final ScenarioService scenarioService;
    private final PlayerService playerService;
    private final CommunicationStatsService communicationStatsService;
    private final MastodonClient mastodonClient;

    public NewsService(
            @Qualifier("newsRepository") NewsRepository newsRepository,
            @Qualifier("scenarioService") ScenarioService scenarioService,
            @Qualifier("playerService") PlayerService playerService,
            @Qualifier("communicationStatsService") CommunicationStatsService communicationStatsService,
            @Qualifier("mastodonClient") MastodonClient mastodonClient
    ) {
        this.newsRepository = newsRepository;
        this.scenarioService = scenarioService;
        this.playerService = playerService;
        this.communicationStatsService = communicationStatsService;
        this.mastodonClient = mastodonClient;
    }

    public NewsStory createNews(NewsPostDTO dto) {

        Scenario scenario = scenarioService.getScenarioById(dto.getScenarioId());

        if (scenario.getStatus() == ScenarioStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot post news to a completed scenario");
        }

        NewsStory entity;

        if (dto.getAuthorId() != null) {

            Role author = playerService.getRoleById(dto.getAuthorId());

            Pronouncement p = new Pronouncement();
            p.setTitle(dto.getTitle());
            p.setBody(dto.getBody());
            p.setCreatedAt(Instant.now());
            p.setDayNumber(scenario.getDayNumber());
            p.setLikes(0);
            p.setAuthor(author);
            p.setScenario(scenario);

            communicationStatsService.registerCommunication(author, p);

            entity = p;

        } else {
            entity = NewsDTOMapper.INSTANCE.convertPostDTOToNewsStory(dto);
            entity.setCreatedAt(Instant.now());
            entity.setDayNumber(scenario.getDayNumber());
            entity.setScenario(scenario);
        }

        entity = newsRepository.save(entity);

        scenarioService.addCommunicationToHistory(scenario.getId(), entity);

        postToMastodon(scenario, entity);

        return entity;
    }

    public NewsStory getNewsById(Long newsId) {

        return newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "News item not found"));


    }

    public List<NewsStory> getNewsByScenario(Long scenarioId) {

        scenarioService.getScenarioById(scenarioId);

        return newsRepository.findByScenarioIdOrderByCreatedAtAsc(scenarioId);
    }

    public void postToMastodon (Scenario scenario, NewsStory news) {
        try {
            String statusId = mastodonClient.postStatus(
                    scenario.getMastodonBaseUrl(),
                    scenario.getMastodonAccessToken(),
                    news.formatSelf()
            );

            news.setMastodonStatusId(statusId);
            newsRepository.save(news);

        } catch (Exception e) {
            System.err.println("Failed to post to Mastodon: " + e.getMessage());
        }
    }

    public void deleteNews(Long newsId) {
        newsRepository.deleteById(newsId);
    }
}