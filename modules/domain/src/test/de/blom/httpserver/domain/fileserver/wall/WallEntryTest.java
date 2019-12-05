package de.blom.httpserver.domain.fileserver.wall;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mongodb.DBObject;

import de.blom.httpserver.crosscutting.representation.wall.WallEntryInboundDto;
import de.blom.httpwebserver.fileserver.wall.WallEntry;
import java.util.Date;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WallEntryTest {

  private static final String AUTHOR = "Max Mustermann";
  private static final String TEXT = "LoremIpsum";

  @Test
  public void expectToCreateWallEntryWithCurrentDate() {
    final WallEntry wallEntry = new WallEntry(AUTHOR, TEXT);

    assertThat(wallEntry.getCreated(), CoreMatchers.any(Date.class));
    assertThat(wallEntry.getAuthor(), CoreMatchers.is(AUTHOR));
    assertThat(wallEntry.getText(), CoreMatchers.is(TEXT));
  }

  @Test
  public void expectToCreateEntryWithCorrectDtoData() {
    final WallEntryInboundDto dto = new WallEntryInboundDto(AUTHOR, TEXT);
    final WallEntry wallEntry = new WallEntry(dto);

    assertThat(wallEntry.getCreated(), CoreMatchers.any(Date.class));
    assertThat(wallEntry.getAuthor(), CoreMatchers.is(AUTHOR));
    assertThat(wallEntry.getText(), CoreMatchers.is(TEXT));
  }

  @Test
  public void expectToReturnValidDocument() {
    final WallEntry wallEntry = new WallEntry(AUTHOR, TEXT);
    final DBObject returnedObject = wallEntry.toMongoDbObject();

    MatcherAssert.assertThat(returnedObject.get("author"), CoreMatchers.is(AUTHOR));
    assertThat(returnedObject.get("created"), is(wallEntry.getCreated()));
    assertThat(returnedObject.get("text"), is(wallEntry.getText()));

  }

}
