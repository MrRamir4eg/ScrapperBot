package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.request.LinkUpdate;

public interface BotClient {

    void sendUpdate(LinkUpdate update);
}
