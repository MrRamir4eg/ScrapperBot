package backend.academy.scrapper.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowOwnerResponse(@JsonProperty("display_name") String displayName) {}
