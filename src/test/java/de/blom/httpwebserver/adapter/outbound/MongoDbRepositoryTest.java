package de.blom.httpwebserver.adapter.outbound;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoTimeoutException;
import de.blom.httpwebserver.exception.ServiceNotAvaliableException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
        MongoTimeoutException mongoDbException = Mockito.mock(MongoTimeoutException.class);
        Mockito.doThrow(mongoDbException)
                .when(this.mockedDbCollection)
                .save(this.mockedBasicDBObject);

        this.dbRepository.save(this.mockedBasicDBObject);
    }

}