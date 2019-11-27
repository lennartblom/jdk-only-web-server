package de.blom.persistence;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDbRepository {

  private static final Logger log = Logger.getLogger(MongoDbRepository.class.getName());

  private static final String MONGODB_COLLECTION_NAME = "wallentries";
  private static final String MONGODB_DB_NAME = "JDKOnlyWebserver";

  private final DBCollection dbCollection;

  public MongoDbRepository(final DBCollection dbCollection) {
    this.dbCollection = dbCollection;
  }

  public MongoDbRepository() throws UnknownHostException {
    final MongoClient mongoClient = new MongoClient();
    final DB database = mongoClient.getDB(MONGODB_DB_NAME);
    this.dbCollection = database.getCollection(MONGODB_COLLECTION_NAME);
  }

  public void save(final BasicDBObject object) {
    try {
      this.dbCollection.save(object);
    } catch (final MongoTimeoutException e) {
      log.log(Level.SEVERE, "Mongo DB not available", e);
      throw new ServiceNotAvailableException();
    }

  }

  public List<DBObject> getAll() {
    final List<DBObject> rawObjects = new ArrayList<>();
    try {
      final DBCursor dbCursor = this.dbCollection.find();
      while (dbCursor.hasNext()) {
        rawObjects.add(dbCursor.next());
      }
      return rawObjects;

    } catch (final MongoTimeoutException e) {
      log.log(Level.SEVERE, "Mongo DB not available", e);
      throw new ServiceNotAvailableException();
    }
  }
}
