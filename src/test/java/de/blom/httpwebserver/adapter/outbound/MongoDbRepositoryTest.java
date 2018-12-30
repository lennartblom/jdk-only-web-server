package de.blom.httpwebserver.adapter.outbound;

import com.mongodb.*;
import de.blom.httpwebserver.exception.ServiceNotAvaliableException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MongoDbRepositoryTest {

    @Mock
    private BasicDBObject mockedBasicDBObject;

    @Mock
    private DBCollection mockedDbCollection;

    private MongoDbRepository dbRepository;

    @Before
    public void setup(){
        this.dbRepository = new MongoDbRepository(this.mockedDbCollection);
    }

    @Test
    public void expectToCallDbCollectionSave(){
        this.dbRepository.save(this.mockedBasicDBObject);

        Mockito.verify(this.mockedDbCollection).save(this.mockedBasicDBObject);
    }

    @Test(expected = ServiceNotAvaliableException.class)
    public void expectToThrowServiceNotAvailableIfMongoTimeoutOccurs(){
        MongoTimeoutException mongoDbException = mock(MongoTimeoutException.class);
        Mockito.doThrow(mongoDbException)
                .when(this.mockedDbCollection)
                .save(this.mockedBasicDBObject);

        this.dbRepository.save(this.mockedBasicDBObject);
    }

    @Test(expected = ServiceNotAvaliableException.class)
    public void expectToThrowServiceNotAvailableIfCursorIsCalled(){
        MongoTimeoutException mongoDbException = mock(MongoTimeoutException.class);
        Mockito.doThrow(mongoDbException)
                .when(this.mockedDbCollection)
                .find();

        this.dbRepository.getAll();
    }

    @Test
    public void expectToReturnListOfDbObjects(){
        DBCursor mockedCursor = mock(DBCursor.class);
        DBObject first = mock(DBObject.class);
        DBObject second = mock(DBObject.class);

        when(this.mockedDbCollection.find()).thenReturn(mockedCursor);
        when(mockedCursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockedCursor.next()).thenReturn(first).thenReturn(second);

        List<DBObject> expectedObjects = Arrays.asList(first, second);


        List<DBObject> returned = this.dbRepository.getAll();


        assertThat(returned.size(), is(expectedObjects.size()));
        assertThat(returned.get(0), is(first));
        assertThat(returned.get(1), is(second));
    }

}