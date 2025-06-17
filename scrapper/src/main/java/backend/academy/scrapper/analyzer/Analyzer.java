package backend.academy.scrapper.analyzer;

import backend.academy.scrapper.analyzer.model.Update;
import backend.academy.scrapper.model.Link;
import java.util.List;

public interface Analyzer {

    List<Update> analyze(Link link, String... args);

    String getDomain();
}
