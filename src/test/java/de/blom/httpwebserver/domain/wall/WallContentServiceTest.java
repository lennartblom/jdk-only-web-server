package de.blom.httpwebserver.domain.wall;

import de.blom.httpwebserver.adapter.outbound.MongoDb;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class WallContentServiceTest {

    @Mock
    private MongoDb mongoDb;

    @InjectMocks
    private WallContentService wallContentService;




    @Test
    public void expectToCallMongoDbWithSuitableDocument(){
        WallEntryInboundDto dto = new WallEntryInboundDto("Max Mustermann", "Lorem ipsum dolor");

        this.wallContentService.createNewWallEntry(dto);
    }

}