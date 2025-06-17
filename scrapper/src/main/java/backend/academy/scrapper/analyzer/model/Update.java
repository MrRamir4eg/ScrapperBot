package backend.academy.scrapper.analyzer.model;

import java.time.Instant;

public record Update(String comment, Instant updatedAt) {}
