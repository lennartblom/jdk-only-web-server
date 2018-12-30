package de.blom.httpwebserver.adapter.outbound;

import com.mongodb.*;
import de.blom.httpwebserver.exception.ServiceNotAvaliableException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDbRepository {

    private static final Logger log = Logger.getLogger(MongoDbRepository.class.getName());

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
        try {
            this.dbCollection.save(object);
        }catch (MongoTimeoutException e){
            log.log(Level.SEVERE, "Mongo DB not available", e);
            throw new ServiceNotAvaliableException();
        }

    }

    public List<DBObject> getAll() {
        List<DBObject> rawObjects = new ArrayList<>();
        try {
            DBCursor dbCursor = this.dbCollection.find();
            while (dbCursor.hasNext()){
                rawObjects.add(dbCursor.next());
            }
            return rawObjects;

        }catch (MongoTimeoutException e){
            log.log(Level.SEVERE, "Mongo DB not available", e);
            throw new ServiceNotAvaliableException();
        }
    }
}
