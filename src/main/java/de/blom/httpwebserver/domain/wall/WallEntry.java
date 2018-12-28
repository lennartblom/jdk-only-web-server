package de.blom.httpwebserver.domain.wall;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.Getter;

import java.util.Date;

@Getter
public class WallEntry {
    private Date created;
    private String author;
    private String text;

    public WallEntry(String author, String text){
        this.author = author;
        this.text = text;
        this.created = new Date();
    }

    public final DBObject toMongoDbObject() {
        return new BasicDBObject("author", this.author)
                .append("text", this.text)
                .append("created", this.created);
    }
}
