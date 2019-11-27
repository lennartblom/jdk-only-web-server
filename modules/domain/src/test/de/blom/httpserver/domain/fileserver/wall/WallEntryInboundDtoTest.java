package de.blom.httpserver.domain.fileserver.wall;

import static org.junit.Assert.assertThat;

import de.blom.httpwebserver.crosscutting.exception.InvalidDataException;
import de.blom.httpwebserver.crosscutting.representation.wall.WallEntryInboundDto;
import org.hamcrest.Matchers;
import org.junit.Test;

public class WallEntryInboundDtoTest {

  private static final String AUTHOR = "Max Mustermann";
  private static final String TEXT = "Lorem ipsum dolor";

  private static final String VALID_RAW_JSON = "{\n" +
      "\t\"author\": \"" + AUTHOR + "\",\n" +
      "\t\"text\": \"" + TEXT + "\"\n" +
      "}\n";

  private static final String INVALID_RAW_JSON = "{\n" +
      "\t\"autsdhor\": \" 5 \",\n" +
      "\t\"autsdhor\": \"" + AUTHOR + "\",\n" +
      "\t\"tasdext\": \"" + TEXT + "\"\n" +
      "}\n";

  @Test
  public void expectCorrectValuesFromParse() {
    final WallEntryInboundDto parsedDto = WallEntryInboundDto.parseFromRawJson(VALID_RAW_JSON);
    final WallEntryInboundDto expectedDto = new WallEntryInboundDto(AUTHOR, TEXT);

    assertThat(parsedDto, Matchers.samePropertyValuesAs(expectedDto));
  }

  @Test(expected = InvalidDataException.class)
  public void expectIncorrectValuesFromParse() {
    WallEntryInboundDto.parseFromRawJson(INVALID_RAW_JSON);
  }

}
