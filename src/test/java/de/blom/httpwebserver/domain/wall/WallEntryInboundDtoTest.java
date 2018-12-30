package de.blom.httpwebserver.domain.wall;

import de.blom.httpwebserver.exception.InvalidDataException;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.*;

public class WallEntryInboundDtoTest {

    private static final String AUTHOR = "Max Mustermann";
    private static final String TEXT = "Lorem ipsum dolor";

    private static String VALID_RAW_JSON = "{\n" +
            "\t\"author\": \"" + AUTHOR + "\",\n" +
            "\t\"text\": \"" + TEXT + "\"\n" +
            "}\n";

    private static String INVALID_RAW_JSON = "{\n" +
            "\t\"autsdhor\": \" 5 \",\n" +
            "\t\"autsdhor\": \"" + AUTHOR + "\",\n" +
            "\t\"tasdext\": \"" + TEXT + "\"\n" +
            "}\n";


    @Test
    public void expectCorrectValuesFromParse(){
        WallEntryInboundDto parsedDto = WallEntryInboundDto.parseFromRawJson(VALID_RAW_JSON);
        WallEntryInboundDto expectedDto = new WallEntryInboundDto(AUTHOR, TEXT);

        assertThat(parsedDto, Matchers.samePropertyValuesAs(expectedDto));
    }

    @Test(expected = InvalidDataException.class)
    public void expectIncorrectValuesFromParse(){
        WallEntryInboundDto.parseFromRawJson(INVALID_RAW_JSON);
    }


}