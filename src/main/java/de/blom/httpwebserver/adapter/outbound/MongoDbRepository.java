package de.blom.httpwebserver.adapter.outbound;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import de.blom.httpwebserver.exception.ServiceNotAvaliableException;

import java.net.UnknownHostException;

public class MongoDbRepository {

    private static final String MONGODB_COLLECTION_NAME = "wallentries";
    private static final String MONGODB_DB_NAME = "JDKOnlyWebserver";

    private DBCollection dbCollection;

    public MongoDbRepository(DBCollection dbCollection) {
        this.dbCollection = dbCollection;
    }

    public MongoDbRepository() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient();
        DB database = mongoClient.getDB(MONGODB_DB_NAME);
        this.dbCollection = database.getCollection(MONGODB_COLLECTION_NAME);
    }

    public void save(BasicDBObject object) {
        this.dbCollection.save(object);
    }
}
