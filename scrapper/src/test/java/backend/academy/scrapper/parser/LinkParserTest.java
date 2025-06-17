package backend.academy.scrapper.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LinkParserTest {

    private static LinkParser linkParser = new LinkParser();

    @BeforeAll
    public static void init() {
        linkParser = new LinkParser();
    }

    @Test
    public void checkForSupportedLinks() {
        String link1 = "https://github.com/Test/Me";
        String link2 = "https://stackoverflow.com/questions/24793069/what-does-do-in-bash";
        String link3 = "https://example.com";

        Assertions.assertTrue(linkParser.checkLink(link1));
        Assertions.assertTrue(linkParser.checkLink(link2));
        Assertions.assertFalse(linkParser.checkLink(link3));
    }

    @Test
    public void testLinkParsing() {
        String link1 = "https://github.com/Test/Me";
        String[] expected = new String[] {"https:", "github.com", "Test", "Me"};

        Assertions.assertArrayEquals(expected, linkParser.parseLink(link1));
    }
}
