package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ActionPointService {

    private final NewsRepository newsRepository;
    private final RoleRepository roleRepository;
    private final ScenarioRepository scenarioRepository;
    private final MastodonClient mastodonClient;

    public ActionPointService(
            @Qualifier("newsRepository") NewsRepository newsRepository,
            @Qualifier("roleRepository") RoleRepository roleRepository,
            @Qualifier("scenarioRepository") ScenarioRepository scenarioRepository,
            @Qualifier("mastodonClient") MastodonClient mastodonClient
    ) {
        this.newsRepository = newsRepository;
        this.roleRepository = roleRepository;
        this.scenarioRepository = scenarioRepository;
        this.mastodonClient = mastodonClient;
    }

    public void syncActionPoints(Role role, Scenario scenario) {

        List<Pronouncement> pronouncements =
                newsRepository.findPronouncementsByAuthorIdAndScenarioId(
                        role.getId(),
                        scenario.getId()
                );

        int newTotal = 0;

        for (Pronouncement p : pronouncements) {

            Integer likes = mastodonClient.getLikes(
                    scenario.getMastodonBaseUrl(),
                    scenario.getMastodonAccessToken(),
                    p.getMastodonStatusId()
            );

            if (likes == null) {
                likes = 0;
            }

            newTotal += likes;
        }

        int oldTotal = role.getTotalPoints();

        if (newTotal > oldTotal) {

            int delta = newTotal - oldTotal;

            role.setPointsBalance(
                    role.getPointsBalance() + delta
            );
        }

        role.setTotalPoints(newTotal);

        roleRepository.save(role);
    }

    public Role buyMessage(Long scenarioId, Long characterId) {

        Role role = roleRepository.findById(characterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Character not found"
                ));

        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Scenario not found"
                ));

        syncActionPoints(role, scenario);

        try {
            role.buyMessages(
                    scenario.getExchangeRate(),
                    1
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }

        return roleRepository.save(role);
    }
}