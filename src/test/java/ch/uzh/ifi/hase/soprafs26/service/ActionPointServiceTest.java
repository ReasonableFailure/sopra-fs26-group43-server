package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Pronouncement;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.entity.Scenario;
import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import ch.uzh.ifi.hase.soprafs26.repository.NewsRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ScenarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ActionPointServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private MastodonClient mastodonClient;

    @InjectMocks
    private ActionPointService actionPointService;

    private Role testRole;
    private Scenario testScenario;
    private Pronouncement testPronouncement;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("Caesar");
        testRole.setTotalPoints(0);
        testRole.setPointsBalance(0);
        testRole.setMessageCount(0);

        testScenario = new Scenario();
        testScenario.setId(1L);
        testScenario.setTitle("Test Crisis");
        testScenario.setExchangeRate(5);
        testScenario.setMastodonBaseUrl("https://mastodon.example");
        testScenario.setMastodonAccessToken("token-abc");

        testPronouncement = new Pronouncement();
        testPronouncement.setId(10L);
        testPronouncement.setMastodonStatusId("status-1");

        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void syncActionPoints_newLikesIncreaseBalance_success() {
        when(newsRepository.findPronouncementsByAuthorIdAndScenarioId(1L, 1L))
                .thenReturn(Collections.singletonList(testPronouncement));
        when(mastodonClient.getLikes(eq("https://mastodon.example"), eq("token-abc"), eq("status-1")))
                .thenReturn(7);

        actionPointService.syncActionPoints(testRole, testScenario);

        assertEquals(7, testRole.getTotalPoints());
        assertEquals(7, testRole.getPointsBalance());
        verify(roleRepository, times(1)).save(testRole);
    }

    @Test
    void syncActionPoints_noNewLikes_balanceUnchanged() {
        testRole.setTotalPoints(5);
        testRole.setPointsBalance(3);

        when(newsRepository.findPronouncementsByAuthorIdAndScenarioId(1L, 1L))
                .thenReturn(Collections.singletonList(testPronouncement));
        when(mastodonClient.getLikes(any(), any(), any())).thenReturn(5);

        actionPointService.syncActionPoints(testRole, testScenario);

        assertEquals(5, testRole.getTotalPoints());
        assertEquals(3, testRole.getPointsBalance());
    }

    @Test
    void syncActionPoints_mastodonReturnsNull_treatedAsZero() {
        when(newsRepository.findPronouncementsByAuthorIdAndScenarioId(1L, 1L))
                .thenReturn(Collections.singletonList(testPronouncement));
        when(mastodonClient.getLikes(any(), any(), any())).thenReturn(null);

        actionPointService.syncActionPoints(testRole, testScenario);

        assertEquals(0, testRole.getTotalPoints());
        assertEquals(0, testRole.getPointsBalance());
    }

    @Test
    void syncActionPoints_multiplePronouncements_sumsAll() {
        Pronouncement p2 = new Pronouncement();
        p2.setId(11L);
        p2.setMastodonStatusId("status-2");

        when(newsRepository.findPronouncementsByAuthorIdAndScenarioId(1L, 1L))
                .thenReturn(Arrays.asList(testPronouncement, p2));
        when(mastodonClient.getLikes(any(), any(), eq("status-1"))).thenReturn(3);
        when(mastodonClient.getLikes(any(), any(), eq("status-2"))).thenReturn(4);

        actionPointService.syncActionPoints(testRole, testScenario);

        assertEquals(7, testRole.getTotalPoints());
        assertEquals(7, testRole.getPointsBalance());
    }

    @Test
    void buyMessage_validInputs_success() throws Exception {
        testRole.setPointsBalance(20);
        testRole.setTotalPoints(20);
        testRole.setMessageCount(2);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(newsRepository.findPronouncementsByAuthorIdAndScenarioId(1L, 1L))
                .thenReturn(Collections.emptyList());

        Role result = actionPointService.buyMessage(1L, 1L);

        assertEquals(3, result.getMessageCount());
        assertEquals(15, result.getPointsBalance());
        verify(roleRepository, times(2)).save(testRole);
    }

    @Test
    void buyMessage_characterNotFound_throwsNotFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> actionPointService.buyMessage(1L, 99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void buyMessage_scenarioNotFound_throwsNotFound() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(scenarioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> actionPointService.buyMessage(99L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void buyMessage_insufficientBalance_throwsBadRequest() {
        testRole.setPointsBalance(2);
        testRole.setTotalPoints(2);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(testScenario));
        when(newsRepository.findPronouncementsByAuthorIdAndScenarioId(any(), any()))
                .thenReturn(Collections.emptyList());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> actionPointService.buyMessage(1L, 1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
