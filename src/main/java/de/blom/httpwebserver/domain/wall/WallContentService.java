package de.blom.httpwebserver.domain.wall;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import de.blom.httpwebserver.adapter.outbound.MongoDbRepository;
import de.blom.httpwebserver.exception.ServiceNotAvaliableException;
import de.blom.httpwebserver.representation.wall.WallEntryOutboundDto;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import lombok.AllArgsConstructor;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public void createNewEntry(WallEntryInboundDto data) {
        WallEntry newWallEntry = new WallEntry(data);
        BasicDBObject mongoDbObject = newWallEntry.toMongoDbObject();

        this.mongoDbRepository.save(mongoDbObject);
    }

    public List<WallEntryOutboundDto> getAllEntries() {
        List<WallEntryOutboundDto> allEntries = new ArrayList<>();
        List<DBObject> rawElements = this.mongoDbRepository.getAll();

        rawElements.forEach(mongoDbObject -> {
            WallEntryOutboundDto dto = WallEntryOutboundDto.builder()
                    .author((String)mongoDbObject.get("author"))
                    .text((String)mongoDbObject.get("text"))
                    .created((Date)mongoDbObject.get("created"))
                    .build();

            allEntries.add(dto);
        });

        return allEntries;
    }
}
