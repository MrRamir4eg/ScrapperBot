package backend.academy.scrapper.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubResponse(
        String title, String body, @JsonProperty("created_at") String createdAt, GitHubUserResponse user) {}
