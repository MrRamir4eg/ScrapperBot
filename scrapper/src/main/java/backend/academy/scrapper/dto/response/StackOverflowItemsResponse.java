package backend.academy.scrapper.dto.response;

import java.util.List;

public record StackOverflowItemsResponse(List<StackOverflowItemResponse> items) {}
