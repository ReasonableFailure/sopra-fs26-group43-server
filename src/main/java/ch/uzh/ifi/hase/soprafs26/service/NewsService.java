package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.NewsPostDTO;
import ch.uzh.ifi.hase.soprafs26.mapper.NewsDTOMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final ScenarioRepository scenarioRepository;

    public NewsService(
            @Qualifier("newsRepository") NewsRepository newsRepository,
            @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository
    ) {
        this.newsRepository = newsRepository;
        this.scenarioRepository = scenarioRepository;
    }

    public NewsStory createNews(NewsPostDTO dto) {

        Scenario scenario = scenarioRepository.findById(dto.getScenarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));

        NewsStory entity;

        if (dto.getAuthorId() != null) {

            Pronouncement p = new Pronouncement();
            p.setTitle(dto.getTitle());
            p.setBody(dto.getBody());
            p.setPostURI(dto.getPostURI());
            p.setCreatedAt(Instant.now());
            p.setLikes(0);

            // TODO: replace with RoleRepository
            Role author = new Role();
            author.setId(dto.getAuthorId());
            p.setAuthor(author);

            entity = p;

        } else {
            entity = NewsDTOMapper.INSTANCE.convertPostDTOToNewsStory(dto);
            entity.setCreatedAt(Instant.now());
        }

        entity = newsRepository.save(entity);

        scenario.getHistory().add(entity);
        scenarioRepository.save(scenario);

        return entity;
    }

    public NewsStory getNewsById(Long newsId) {

        NewsStory news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "News item not found"));

        return news;
    }
}