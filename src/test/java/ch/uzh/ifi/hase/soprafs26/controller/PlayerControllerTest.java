package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Role;
import ch.uzh.ifi.hase.soprafs26.rest.playerdto.RolePutDTO;
import ch.uzh.ifi.hase.soprafs26.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import ch.uzh.ifi.hase.soprafs26.controller.PlayerController;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(PlayerController.class)
public class PlayerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    private Role testCharacter;

    @BeforeEach
    public void setupTests(){

    }

    /*@PutMapping("/characters/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateRole(@RequestBody RolePutDTO rolePutDTO, @RequestHeader("Authorization") String token, @PathVariable Long characterId){
        playerService.validate(token, DIRECTOR);
        playerService.updateRole(rolePutDTO,characterId);
    }*/

    public void updateRole_whenUpdate_notEmpty_andRoleExists() {
        when(playerService.updateRole(any(RolePutDTO.class), 1L));
    }

}
