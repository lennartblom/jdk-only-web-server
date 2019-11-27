package de.blom.httpwebserver.fileserver.wall;

import com.mongodb.BasicDBObject;
import de.blom.httpwebserver.crosscutting.representation.wall.WallEntryInboundDto;
import java.util.Date;
import lombok.Getter;

@Getter
public class WallEntry {
  private final Date created;
  private final String author;
  private final String text;

  public WallEntry(final WallEntryInboundDto dto) {
    this(dto.getAuthor(), dto.getText());
  }

  public WallEntry(final String author, final String text) {
    this.author = author;
    this.text = text;
    this.created = new Date();
  }

  public final BasicDBObject toMongoDbObject() {
    return new BasicDBObject("author", this.author)
        .append("text", this.text)
        .append("created", this.created);
  }
}
