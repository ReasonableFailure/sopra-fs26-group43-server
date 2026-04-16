package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Directive;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.mapper.DirectiveDTOMapper;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectiveGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.directivedto.DirectivePostDTO;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirectiveDTOMapperTest {
	@Test
	public void testCreateDirective_fromDirectivePostDTO_toDirective_success() {
		DirectivePostDTO directivePostDTO = new DirectivePostDTO();
		directivePostDTO.setTitle("Test Directive");
		directivePostDTO.setBody("Test Body");
		directivePostDTO.setCreatorId(1L);
		directivePostDTO.setScenarioId(1L);

		Directive directive = DirectiveDTOMapper.INSTANCE.convertPostDTOToEntity(directivePostDTO);

		assertEquals(directivePostDTO.getTitle(), directive.getTitle());
		assertEquals(directivePostDTO.getBody(), directive.getBody());
	}

	@Test
	public void testGetDirective_fromDirective_toDirectiveGetDTO_success() {
		Role creator = new Role();
		creator.setId(1L);
		creator.setName("Test Role");

		Directive directive = new Directive();
		directive.setId(1L);
		directive.setTitle("Test Directive");
		directive.setBody("Test Body");
		directive.setCreatedAt(Instant.now());
		directive.setStatus(CommsStatus.PENDING);
		directive.setCreator(creator);

		DirectiveGetDTO directiveGetDTO = DirectiveDTOMapper.INSTANCE.convertEntityToGetDTO(directive);

		assertEquals(directive.getId(), directiveGetDTO.getId());
		assertEquals(directive.getTitle(), directiveGetDTO.getTitle());
		assertEquals(directive.getBody(), directiveGetDTO.getBody());
		assertEquals(directive.getCreatedAt(), directiveGetDTO.getCreatedAt());
		assertEquals(directive.getStatus(), directiveGetDTO.getStatus());
		assertEquals(directive.getCreator().getId(), directiveGetDTO.getCreatorId());
	}
}
