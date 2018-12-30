package de.blom.httpwebserver.domain.wall;

import com.mongodb.BasicDBObject;
import de.blom.httpwebserver.adapter.outbound.MongoDbRepository;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WallContentServiceTest {

    private static final String AUTHOR = "Max Mustermann";
    private static final String TEXT = "Lorem ipsum dolor";

    @Mock
    private MongoDbRepository mongoDbRepository;

    @InjectMocks
    private WallContentService wallContentService;


    @Test
    public void expectToCallMongoDbWithSuitableDocument() {
        WallEntryInboundDto dto = new WallEntryInboundDto(AUTHOR, TEXT);

        this.wallContentService.createNewWallEntry(dto);

        ArgumentCaptor<BasicDBObject> varArgs = ArgumentCaptor.forClass(BasicDBObject.class);
        verify(this.mongoDbRepository).save(varArgs.capture());
        BasicDBObject dbObject = varArgs.getValue();
        assertThat(dbObject.get("author"), is(AUTHOR));
        assertThat(dbObject.get("text"), is(TEXT));
        assertThat(dbObject.get("created"), instanceOf(Date.class));
    }

}