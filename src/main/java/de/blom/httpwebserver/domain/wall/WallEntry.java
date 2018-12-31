package de.blom.httpwebserver.domain.wall;

import com.mongodb.BasicDBObject;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import lombok.Getter;

import java.util.Date;

@Getter
class WallEntry {
    private Date created;
    private String author;
    private String text;

    WallEntry(WallEntryInboundDto dto){
        this(dto.getAuthor(), dto.getText());
    }

    WallEntry(String author, String text){
        this.author = author;
        this.text = text;
        this.created = new Date();
    }

    final BasicDBObject toMongoDbObject() {
        return new BasicDBObject("author", this.author)
                .append("text", this.text)
                .append("created", this.created);
    }
}
