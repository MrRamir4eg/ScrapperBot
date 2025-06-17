package backend.academy.scrapper.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowItemResponse(
        StackOverflowOwnerResponse owner,
        @JsonProperty("creation_date") Long creationDate,
        String title,
        String body) {}
