package de.blom.httpwebserver.domain.wall;

import com.mongodb.BasicDBObject;
import de.blom.httpwebserver.adapter.outbound.MongoDb;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WallContentService {

    private MongoDb mongoDb;

    public void createNewWallEntry(WallEntryInboundDto data) {
        WallEntry newWallEntry = new WallEntry(data);
        BasicDBObject mongoDbObject = newWallEntry.toMongoDbObject();

        this.mongoDb.save(mongoDbObject);
    }

}
