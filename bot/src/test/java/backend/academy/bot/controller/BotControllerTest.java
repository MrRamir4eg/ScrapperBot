package backend.academy.bot.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.service.RateLimitService;
import backend.academy.bot.service.TelegramBotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = BotController.class)
public class BotControllerTest {

    @MockitoBean
    private TelegramBotService service;

    @MockitoBean
    private RateLimitService rateLimitService;

    @BeforeEach
    public void setup() {
        Mockito.when(rateLimitService.allowRequest(Mockito.any(String.class))).thenReturn(true);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Test
    public void testUpdate() {
        LinkUpdate update = new LinkUpdate(123L, "https://example.com", "Desc", List.of(993L, 1000L));
        mockMvc.perform(MockMvcRequestBuilders.post("/bot/api/updates")
                        .content(objectMapper.writeValueAsString(update))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void testUpdate_whenGivenIncorrectRequestBody_shouldReturnBadRequest() {
        LinkUpdate update = new LinkUpdate(123L, "https://example.com", "Desc", List.of(-993L, 1000L));
        mockMvc.perform(MockMvcRequestBuilders.post("/bot/api/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"));
    }
}
