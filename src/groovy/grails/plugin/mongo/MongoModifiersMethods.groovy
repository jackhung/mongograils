package grails.plugin.mongo

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import com.mongodb.ObjectId
import com.mongodb.DBRef
import com.mongodb.DBCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Implements $inc, $set, $unset, $push, $pushAll, $addToSet, $pop, $pull, $pullAll
 * 
 * @author jack
 *
 */
class MongoModifiersMethods {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MongoModifiersMethods)
	
	DBCollection collection
	
	MongoModifiersMethods(collection) {
		this.collection = collection
	}
		
	private objectId(id) {
		id instanceof ObjectId ? id : new ObjectId(id)
	}
}
