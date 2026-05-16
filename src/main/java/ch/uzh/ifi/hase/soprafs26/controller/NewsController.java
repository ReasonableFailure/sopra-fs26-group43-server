package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.NewsStory;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.Pronouncement;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.newsdto.*;
import ch.uzh.ifi.hase.soprafs26.service.NewsService;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.NewsDTOMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
public class NewsController {

    private final NewsService newsService;
    private final PlayerService playerService;

    NewsController(NewsService newsService,
                   PlayerService playerService) {
        this.newsService = newsService;
        this.playerService = playerService;
    }

    @PostMapping("/news")
    @ResponseStatus(HttpStatus.CREATED)
    public NewsGetDTO createNews(
            @RequestHeader("Authorization") String token,
            @RequestBody NewsPostDTO dto) {

        // Pronouncement with authorId set must be created by the authoring Role;
        // News story no authorId must be created by a Backroomer (which
        // covers Directors too — Director extends Backroomer).
        if (dto.getAuthorId() != null) {
            playerService.validate(token, "Role");
            Player requester = playerService.resolvePlayerFromHeader(token);
            if (!(requester instanceof Role) || !requester.getId().equals(dto.getAuthorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Pronouncement author must match the authenticated role");
            }
        } else {
            playerService.validate(token, "Backroomer");
        }

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

        playerService.validate(token, "any");

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

        playerService.validate(token, "any");

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

        playerService.validate(token, "Backroomer");

        newsService.deleteNews(newsId);
    }


}
