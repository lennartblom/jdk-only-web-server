package de.blom.httpwebserver.fileserver.wall;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import de.blom.persistence.ServiceNotAvailableException;
import de.blom.httpwebserver.crosscutting.representation.wall.WallEntryInboundDto;
import de.blom.httpwebserver.crosscutting.representation.wall.WallEntryOutboundDto;
import de.blom.persistence.MongoDbRepository;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WallContentService {

  private final MongoDbRepository mongoDbRepository;

  public WallContentService() {
    try {
      this.mongoDbRepository = new MongoDbRepository();
    } catch (final UnknownHostException e) {
      throw new ServiceNotAvailableException();
    }
  }

  public void createNewEntry(final WallEntryInboundDto data) {
    final WallEntry newWallEntry = new WallEntry(data);
    final BasicDBObject mongoDbObject = newWallEntry.toMongoDbObject();

    this.mongoDbRepository.save(mongoDbObject);
  }

  public List<WallEntryOutboundDto> getAllEntries() {
    final List<WallEntryOutboundDto> allEntries = new ArrayList<>();
    final List<DBObject> rawElements = this.mongoDbRepository.getAll();

    rawElements.forEach(mongoDbObject -> {
      final WallEntryOutboundDto dto = WallEntryOutboundDto.builder()
          .author((String) mongoDbObject.get("author"))
          .text((String) mongoDbObject.get("text"))
          .created((Date) mongoDbObject.get("created"))
          .build();

      allEntries.add(dto);
    });

    return allEntries;
  }
}
