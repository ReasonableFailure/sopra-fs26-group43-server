package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.entity.Backroomer;
import ch.uzh.ifi.hase.soprafs26.entity.Director;
import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RoleGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePostDTO;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlayerDTOMapperTest {

    @Test
    public void testConvertRolePostDTOToEntity_withBase64Portrait_success() {
        byte[] portraitBytes = "test-portrait".getBytes();
        String base64 = Base64.getEncoder().encodeToString(portraitBytes);
        String dataUrl = "data:image/jpeg;base64," + base64;

        RolePostDTO rolePostDTO = new RolePostDTO();
        rolePostDTO.setName("Test Role");
        rolePostDTO.setTitle("Role Title");
        rolePostDTO.setDescription("Role Description");
        rolePostDTO.setPortrait(dataUrl);
        rolePostDTO.setSecret("secret123");

        Role role = PlayerDTOMapper.INSTANCE.convertRolePostDTOtoEntity(rolePostDTO);

        assertEquals(rolePostDTO.getName(), role.getName());
        assertEquals(rolePostDTO.getTitle(), role.getTitle());
        assertEquals(rolePostDTO.getDescription(), role.getDescription());
        assertEquals(rolePostDTO.getSecret(), role.getSecret());
        assertArrayEquals(portraitBytes, role.getPortrait());
    }

    @Test
    public void testConvertEntityToRoleGetDTO_mapsPortraitAndFields_success() {
        byte[] portraitBytes = "test-portrait".getBytes();

        Role role = new Role();
        role.setId(1L);
        role.setName("Test Role");
        role.setTitle("Role Title");
        role.setDescription("Role Description");
        role.setSecret("secret123");
        role.setAlive(true);
        role.setToken("token-123");
        role.setPortrait(portraitBytes);

        RoleGetDTO roleGetDTO = PlayerDTOMapper.INSTANCE.convertEntitytoRoleGetDTO(role);

        assertEquals(role.getId(), roleGetDTO.getId());
        assertEquals(role.getName(), roleGetDTO.getName());
        assertEquals(role.getTitle(), roleGetDTO.getTitle());
        assertEquals(role.getDescription(), roleGetDTO.getDescription());
        assertEquals(role.getSecret(), roleGetDTO.getSecret());
        assertEquals(role.getAlive(), roleGetDTO.isAlive());
        assertEquals(role.getToken(), roleGetDTO.getToken());
        assertEquals("data:image/jpeg;base64," + Base64.getEncoder().encodeToString(portraitBytes), roleGetDTO.getPortrait());
    }

    @Test
    public void testConvertEntityToDirectorGetDTO_success() {
        Director director = new Director();
        director.setId(42L);
        director.setToken("director-token");

        var dto = PlayerDTOMapper.INSTANCE.convertEntityToDirectorGetDTO(director);

        assertEquals(director.getId(), dto.getId());
        assertEquals(director.getToken(), dto.getToken());
    }

    @Test
    public void testConvertEntityToBackroomerGetDTO_success() {
        Backroomer backroomer = new Backroomer();
        backroomer.setId(7L);
        backroomer.setToken("backroomer-token");

        var dto = PlayerDTOMapper.INSTANCE.convertEntitytoBackroomerGetDTO(backroomer);

        assertNotNull(dto);
        assertEquals(backroomer.getId(), dto.getId());
        assertEquals(backroomer.getToken(), dto.getToken());
    }
}
