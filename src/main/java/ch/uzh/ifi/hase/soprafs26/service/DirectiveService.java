package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Directive;
import ch.uzh.ifi.hase.soprafs26.repository.DirectiveRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DirectiveService {

    private final Logger log = LoggerFactory.getLogger(DirectiveService.class);

    private final DirectiveRepository directiveRepository;

    public DirectiveService(@Qualifier("directiveRepository") DirectiveRepository directiveRepository) {
        this.directiveRepository = directiveRepository;
    }

}