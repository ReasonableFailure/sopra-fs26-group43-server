package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.DirectiveRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs26.repository.NewsRepository;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.ScenarioDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.scenariodto.ScenarioMastodonDTO;
import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ScenarioService {

    private final Logger log = LoggerFactory.getLogger(ScenarioService.class);
    private final PlayerService playerService;
    private final UserService userService;
    private final ScenarioRepository scenarioRepository;
    private final NewsRepository newsRepository;
    private final MessageRepository messageRepository;
    private final DirectiveRepository directiveRepository;
    private final MastodonClient mastodonClient;

    public ScenarioService(@Qualifier("scenarioRepository") ScenarioRepository scenarioRepository,
                           @Qualifier("userService") UserService userService,
                           @Qualifier("playerService") PlayerService playerService,
                           @Qualifier("newsRepository") NewsRepository newsRepository,
                           @Qualifier("messageRepository") MessageRepository messageRepository,
                           @Qualifier("directiveRepository") DirectiveRepository directiveRepository,
                           @Qualifier("mastodonClient") MastodonClient mastodonClient) {
        this.scenarioRepository = scenarioRepository;
        this.userService = userService;
        this.playerService = playerService;
        this.newsRepository = newsRepository;
        this.messageRepository = messageRepository;
        this.directiveRepository = directiveRepository;
        this.mastodonClient = mastodonClient;
    }

    public List<Scenario> getScenarios() {
        return this.scenarioRepository.findAll();
    }

    public Scenario getScenarioById(Long scenarioId) {
        return this.scenarioRepository.findById(scenarioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario with id " + scenarioId + " not found"));
    }

    @Transactional
    public Scenario createScenario( ScenarioPostDTO scenarioPostDTO){
        Scenario newScenario = ScenarioDTOMapper.INSTANCE.convertScenarioPostDTOtoEntity(scenarioPostDTO);
        newScenario.setDayNumber(0);
        newScenario.setStatus(ScenarioStatus.UNSTARTED);
        newScenario.setPlayers(new ArrayList<Player>());
        newScenario.setHistory(new ArrayList<Communication>());
        Director director = playerService.getDirectorByID(scenarioPostDTO.getDirector());
        newScenario.setDirector(director);
        newScenario.addPlayer(director);
        scenarioRepository.save(newScenario);
        scenarioRepository.flush();
        return newScenario;
    }

    public void deleteScenario(Long scenarioId){
        Scenario toDelete = getScenarioById(scenarioId);

        // We must delete in FK-dependency order. Otherwise the cascade on
        // scenario.players tries to delete Roles while Message.creator,
        // Directive.creator, Pronouncement.author, and pronouncement_likes
        // still reference them, which trips the H2 constraint
        // FKPAM9EGWMHBGTSAIYSOYGE4LEQ.
        //
        // Order:
        //   1. Clear Pronouncement.likedBy (owning side of pronouncement_likes)
        //   2. Delete messages (FK creator/recipient → characters)
        //   3. Delete directives (FK creator → characters)
        //   4. Delete news + pronouncements (FK author → characters)
        //   5. Delete the scenario (cascade on players + director cleans up
        //      Roles, Backroomers, and the Director itself)
        List<Communication> history = new ArrayList<>(toDelete.getHistory());
        for (Communication c : history) {
            if (c instanceof Pronouncement p) {
                p.getLikedBy().clear();
            }
        }
        scenarioRepository.flush();

        // Bulk-detach communications from the in-memory scenario.history list
        // before deletion so JPA doesn't re-cascade them when we delete the
        // scenario.
        toDelete.getHistory().clear();

        // Delete via the per-type repository so JOINED inheritance rows
        // (e.g. Pronouncement's parent NewsStory row, plus the Communication
        // parent row) are removed together.
        List<Message> messages = messageRepository.findByScenarioId(scenarioId);
        messageRepository.deleteAll(messages);
        messageRepository.flush();

        List<Directive> directives = directiveRepository.findByScenarioId(scenarioId);
        directiveRepository.deleteAll(directives);
        directiveRepository.flush();

        List<NewsStory> news = newsRepository.findByScenarioIdOrderByCreatedAtAsc(scenarioId);
        newsRepository.deleteAll(news);
        newsRepository.flush();

        // Now players have no incoming references; cascade can delete them.
        scenarioRepository.delete(toDelete);
        scenarioRepository.flush();
    }

    public void updateScenario(Long scenarioId, ScenarioPutDTO dto){
        Scenario s = getScenarioById(scenarioId);

        if (dto.getTitle() != null) {
            s.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            s.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            s.setStatus(dto.getStatus());
        }
        if (dto.getDayNumber() != null) {
            s.setDayNumber(dto.getDayNumber());
        }
        if (dto.getExchangeRate() != null) {
            s.setExchangeRate(dto.getExchangeRate());
        }
        if (dto.getStartingMessageCount() != null) {
            s.setStartingMessageCount(dto.getStartingMessageCount());
        }
        scenarioRepository.save(s);
        scenarioRepository.flush();
    }

    public void addCommunicationToHistory(Long scenarioId, Communication communication) {
        Scenario scenario = getScenarioById(scenarioId);

        scenario.addComm(communication);

        scenarioRepository.save(scenario);
    }

    public List<Role> getRoles(Long scenarioId) {
        Scenario scenario = getScenarioById(scenarioId);
        List<Role> toReturn = new ArrayList<Role>();
        for(Player player : scenario.getPlayers()){
            if(player instanceof Role){
                toReturn.add((Role) player);
            }
        }
        return toReturn;
    }

    public void updateMastodonConfig(Long scenarioId, ScenarioMastodonDTO dto) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));

        scenario.setMastodonBaseUrl(dto.getMastodonBaseUrl());
        scenario.setMastodonAccessToken(dto.getMastodonAccessToken());

        String profileUrl = mastodonClient.fetchMastodonProfileUrl(
                dto.getMastodonBaseUrl(),
                dto.getMastodonAccessToken()
        );

        scenario.setMastodonProfileUrl(profileUrl);

        List<NewsStory> newsList = newsRepository.findByScenarioIdOrderByCreatedAtAsc(scenarioId);

        for (NewsStory news : newsList) {
            try {
                String content = news.formatSelf();

                mastodonClient.postStatus(
                        dto.getMastodonBaseUrl(),
                        dto.getMastodonAccessToken(),
                        content
                );
            } catch (Exception e) {
                System.err.println("Failed to post news id " + news.getId());
            }
        }

        scenarioRepository.save(scenario);
    }
}
