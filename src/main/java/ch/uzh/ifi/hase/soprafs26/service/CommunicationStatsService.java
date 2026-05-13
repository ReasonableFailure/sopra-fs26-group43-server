package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CommunicationStatsService {

    private final RoleRepository roleRepository;

    public CommunicationStatsService(@Qualifier("roleRepository") RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void registerCommunication(
            Role role,
            Communication communication
    ) {

        communication.applyStats(role);

        role.setTotalTextLength(
                role.getTotalTextLength()
                        + communication.totalTextLength()
        );

        roleRepository.save(role);
    }
}