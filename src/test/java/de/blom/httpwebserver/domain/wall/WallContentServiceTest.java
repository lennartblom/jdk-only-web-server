package de.blom.httpwebserver.domain.wall;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import de.blom.httpwebserver.adapter.outbound.MongoDbRepository;
import de.blom.httpwebserver.representation.wall.WallEntryOutboundDto;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WallContentServiceTest {

    private static final String AUTHOR = "Max Mustermann";
    private static final String TEXT = "Lorem ipsum dolor";
    private static final Date CREATED = new Date();

    @Mock
    private MongoDbRepository mongoDbRepository;

    @InjectMocks
    private WallContentService wallContentService;


    @Test
    public void expectToCallMongoDbWithSuitableDocument() {
        WallEntryInboundDto dto = new WallEntryInboundDto(AUTHOR, TEXT);

        this.wallContentService.createNewEntry(dto);

        ArgumentCaptor<BasicDBObject> varArgs = ArgumentCaptor.forClass(BasicDBObject.class);
        verify(this.mongoDbRepository).save(varArgs.capture());
        BasicDBObject dbObject = varArgs.getValue();
        assertThat(dbObject.get("author"), is(AUTHOR));
        assertThat(dbObject.get("text"), is(TEXT));
        assertThat(dbObject.get("created"), instanceOf(Date.class));
    }

    @Test
    public void expectToGetDocumentsFromRepository(){
        DBObject mockedElement = Mockito.mock(DBObject.class);
        when(mockedElement.get("author")).thenReturn(AUTHOR);
        when(mockedElement.get("text")).thenReturn(TEXT);
        when(mockedElement.get("created")).thenReturn(CREATED);
        WallEntryOutboundDto expectedDto = WallEntryOutboundDto.builder()
                .author(AUTHOR)
                .text(TEXT)
                .created(CREATED)
                .build();

        List<DBObject> allObjects = Arrays.asList(mockedElement, mockedElement, mockedElement);

        when(this.mongoDbRepository.getAll()).thenReturn(allObjects);


        List<WallEntryOutboundDto> elements = this.wallContentService.getAllEntries();


        assertThat(elements.size(), is(allObjects.size()));
        assertThat(elements.get(0), Matchers.samePropertyValuesAs(expectedDto));
        assertThat(elements.get(1), Matchers.samePropertyValuesAs(expectedDto));
        assertThat(elements.get(2), Matchers.samePropertyValuesAs(expectedDto));

    }

}