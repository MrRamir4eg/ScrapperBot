package backend.academy.scrapper.parser;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LinkParser {

    private static final Pattern GITHUB = Pattern.compile("https://github.com/\\S+/\\S+");
    private static final Pattern STACKOVERFLOW = Pattern.compile("https://stackoverflow.com/questions/\\d+/\\S+");

    public boolean checkLink(String uri) {
        return GITHUB.matcher(uri).matches() || STACKOVERFLOW.matcher(uri).matches();
    }

    public String[] parseLink(String uri) {
        return Arrays.stream(uri.split("/")).filter(e -> !e.isEmpty()).toArray(String[]::new);
    }
}
