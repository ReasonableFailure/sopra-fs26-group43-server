package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Directive;
import ch.uzh.ifi.hase.soprafs26.service.DirectiveService;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class DirectiveController {

    private final DirectiveService directiveService;

    DirectiveController(DirectiveService directiveService) {
        this.directiveService = directiveService;
    }

}