package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Pronouncement;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.*;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.NewsService;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.NewsDTOMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
public class NewsController {

    private final NewsService newsService;
    private final UserService userService;

    NewsController(NewsService newsService,
                   UserService userService) {
        this.newsService = newsService;
        this.userService = userService;
    }

    @PostMapping("/news")
    @ResponseStatus(HttpStatus.CREATED)
    public NewsGetDTO createNews(
            @RequestHeader("Authorization") String token,
            @RequestBody NewsPostDTO dto) {

        requireUser(token);

        NewsStory entity = newsService.createNews(dto);

        NewsGetDTO output =
                NewsDTOMapper.INSTANCE.convertEntityToGetDTO(entity);

        if (entity instanceof Pronouncement p && p.getAuthor() != null) {
            output.setAuthorId(p.getAuthor().getId());
            output.setLikes(p.getLikes());
        }

        return output;
    }

    @GetMapping("/news/{newsId}")
    @ResponseStatus(HttpStatus.OK)
    public NewsGetDTO getNews(
            @RequestHeader("Authorization") String token,
            @PathVariable Long newsId) {

        requireUser(token);

        NewsStory entity = newsService.getNewsById(newsId);

        NewsGetDTO dto =
                NewsDTOMapper.INSTANCE.convertEntityToGetDTO(entity);

        if (entity instanceof Pronouncement p) {
            dto.setAuthorId(p.getAuthor().getId());
            dto.setLikes(p.getLikes());
        }

        return dto;
    }

    @GetMapping("/news/scenario/{scenarioId}")
    @ResponseStatus(HttpStatus.OK)
    public List<NewsGetDTO> getNewsByScenario(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scenarioId) {

        requireUser(token);

        List<NewsStory> newsList =
                newsService.getNewsByScenario(scenarioId);

        return newsList.stream().map(entity -> {

            NewsGetDTO dto =
                    NewsDTOMapper.INSTANCE.convertEntityToGetDTO(entity);

            if (entity instanceof Pronouncement p) {
                dto.setAuthorId(p.getAuthor().getId());
                dto.setLikes(p.getLikes());
            }

            return dto;

        }).toList();
    }

    @DeleteMapping("/news/{newsId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNews(
            @RequestHeader("Authorization") String token,
            @PathVariable Long newsId) {

        requireUser(token);

        newsService.deleteNews(newsId);
    }

    private void requireUser(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        userService.validateUserToken(header.substring(7));
    }
}