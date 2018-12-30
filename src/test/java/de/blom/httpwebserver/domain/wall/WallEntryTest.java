package de.blom.httpwebserver.domain.wall;

import com.mongodb.DBObject;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class WallEntryTest {

    private static final String AUTHOR = "Max Mustermann";
    private static final String TEXT = "LoremIpsum";

    @Test
    public void expectToCreateWallEntryWithCurrentDate(){
        WallEntry wallEntry = new WallEntry(AUTHOR, TEXT);

        assertThat(wallEntry.getCreated(), any(Date.class));
        assertThat(wallEntry.getAuthor(), is(AUTHOR));
        assertThat(wallEntry.getText(), is(TEXT));
    }

    @Test
    public void expectToCreateEntryWithCorrectDtoData(){
        WallEntryInboundDto dto = new WallEntryInboundDto(AUTHOR, TEXT);
        WallEntry wallEntry = new WallEntry(dto);

        assertThat(wallEntry.getCreated(), any(Date.class));
        assertThat(wallEntry.getAuthor(), is(AUTHOR));
        assertThat(wallEntry.getText(), is(TEXT));
    }

    @Test
    public void expectToReturnValidDocument(){
        WallEntry wallEntry = new WallEntry(AUTHOR, TEXT);
        DBObject returnedObject = wallEntry.toMongoDbObject();

        assertThat(returnedObject.get("author"), is(AUTHOR));
        assertThat(returnedObject.get("created"), is(wallEntry.getCreated()));
        assertThat(returnedObject.get("text"), is(wallEntry.getText()));

    }

}