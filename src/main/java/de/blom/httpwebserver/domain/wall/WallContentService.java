package de.blom.httpwebserver.domain.wall;

import com.mongodb.BasicDBObject;
import de.blom.httpwebserver.adapter.outbound.MongoDbRepository;
import de.blom.httpwebserver.exception.ServiceNotAvaliableException;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import lombok.AllArgsConstructor;

import java.net.UnknownHostException;

@AllArgsConstructor
public class WallContentService {

    private MongoDbRepository mongoDbRepository;

    public WallContentService() {
        try {
            this.mongoDbRepository = new MongoDbRepository();
        } catch (UnknownHostException e) {
            throw new ServiceNotAvaliableException();
        }
    }

    public void createNewWallEntry(WallEntryInboundDto data) {
        WallEntry newWallEntry = new WallEntry(data);
        BasicDBObject mongoDbObject = newWallEntry.toMongoDbObject();

        this.mongoDbRepository.save(mongoDbObject);
    }

}
