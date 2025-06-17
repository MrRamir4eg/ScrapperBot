package backend.academy.scrapper.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.ObjectNotFoundException;
import backend.academy.scrapper.exception.handler.ScrapperExceptionHandler;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.RateLimitService;
import backend.academy.scrapper.service.TgChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ContextConfiguration(classes = {ScrapperController.class, ScrapperExceptionHandler.class})
@WebMvcTest(ScrapperController.class)
public class ScrapperControllerTest {

    @MockitoBean
    private LinkService linkService;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private TgChatService tgChatService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @BeforeEach
    public void setup() {
        Mockito.when(rateLimitService.allowRequest(Mockito.any(String.class))).thenReturn(true);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @SneakyThrows
    @Test
    public void testRegisterChat() {
        mockMvc.perform(MockMvcRequestBuilders.post("/scrapper/api/tg-chat/{id}", "100"))
                .andExpect(status().isOk());
    }

    @Test
    public void testRegisterChat_shouldReturnBadRequestOnBadParams() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/scrapper/api/tg-chat/{id}", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    @SneakyThrows
    @Test
    public void testDeleteChat() {
        mockMvc.perform(MockMvcRequestBuilders.delete("/scrapper/api/tg-chat/{id}", "100"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteChat_whenChatDoesNotExist_shouldReturnNotFound() throws Exception {
        Mockito.doThrow(new ObjectNotFoundException("Чат не существует"))
                .when(tgChatService)
                .deleteChat(100L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/scrapper/api/tg-chat/{id}", "100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description").value("Чат не существует"))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    @SneakyThrows
    @Test
    public void testAllLinks() {
        Mockito.when(linkService.getAllLinks(100L)).thenReturn(new ListLinksResponse(List.of(getLinkResponse()), 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/scrapper/api/links").header("Tg-Chat-Id", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.links").isArray());
    }

    @SneakyThrows
    @Test
    public void testAllLinks_whenBadRequestParamGiven_shouldReturnBadRequest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/scrapper/api/links"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    @SneakyThrows
    @Test
    public void testAddLink() {
        Mockito.when(linkService.addLink(100L, getLinkRequest())).thenReturn(getLinkResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/scrapper/api/links")
                        .header("Tg-Chat-Id", "100")
                        .content(mapper.writeValueAsString(getLinkRequest()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").value(getGenericURI().toString()));
    }

    @SneakyThrows
    @Test
    public void testAddLink_whenBadParamsGiven_shouldReturnBadRequest() {
        mockMvc.perform(MockMvcRequestBuilders.post("/scrapper/api/links")
                        .header("Tg-Chat-Id", "100")
                        .content(mapper.writeValueAsString(new RemoveLinkRequest(getGenericURI())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    @SneakyThrows
    @Test
    public void testDeleteLinks() {
        Mockito.when(linkService.deleteLink(100L, new RemoveLinkRequest(getGenericURI())))
                .thenReturn(getLinkResponse());

        mockMvc.perform(MockMvcRequestBuilders.delete("/scrapper/api/links")
                        .header("Tg-Chat-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RemoveLinkRequest(getGenericURI()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(getGenericURI().toString()));
    }

    @SneakyThrows
    @Test
    public void testDeleteLinks_whenLinkDoesNotExist_shouldReturnNotFound() {
        Mockito.when(linkService.deleteLink(100L, new RemoveLinkRequest(getGenericURI())))
                .thenThrow(new ObjectNotFoundException("Ссылка не найдена"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/scrapper/api/links")
                        .header("Tg-Chat-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RemoveLinkRequest(getGenericURI()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description").value("Ссылка не найдена"))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    private static LinkResponse getLinkResponse() {
        return new LinkResponse(123L, getGenericURI(), List.of(), List.of());
    }

    private static AddLinkRequest getLinkRequest() {
        return new AddLinkRequest(getGenericURI(), List.of(), List.of());
    }

    private static URI getGenericURI() {
        return URI.create("https://localhost.com");
    }
}
