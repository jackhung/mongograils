package grails.plugin.mongo

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBObjectBuilder
import com.mongodb.BasicDBList
import com.mongodb.ObjectId
import com.mongodb.DBRef
import com.mongodb.DBCollection
import com.mongodb.QueryBuilder
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
		
	private static objectId(id) {
		id instanceof ObjectId ? id : new ObjectId(id)
	}
	
	static mongoPerform = { Closure c ->
		def builder = new CustomBasicDBObjectBuilder()
		c.delegate = builder
		c.call()
		def selector = ["_id": objectId(delegate._id), "_t": delegate.getMongoTypeName()] as BasicDBObject
		log.debug "${builder.get()}"
		delegate.getCollection().update(selector, builder.get())
	}
}

class CustomBasicDBObjectBuilder {
	//@Delegate BasicDBObjectBuilder builder = new BasicDBObjectBuilder()
	def ops = [:]
	
	def opMap(name) {
		if (!ops[name])
			ops[name] = [:]
		ops[name]
	}
	
	def set(f, v) {
		opMap('$set')[f] = v
		this
	}
	
	def unset(f, v = 1) {
		opMap('$unset')[f] = v
		this
	}
	
	def increment(f, v = 1) {
		opMap('$inc')[f] = v
		this
	}
	
	def push(f, v) {
		opMap('$push')[f] = v
		this		
	}
	
	// v should be an array
	def pushAll(f, v) {
		opMap('$pushAll')[f] = v
		this		
	}
	
	// default pop last, v = -1 for first
	def pop(f, v = 1) {
		opMap('$pop')[f] = v
		this
	}
	
	def pull(f, v) {
		opMap('$pull')[f] = v
	}
	
	// v should be an array
	def pullAll(f, v) {
		opMap('$pullAll')[f] = v
	}
	
	// v can be a single element or a collection
	def addToSet(f, v) {
		if (v instanceof Collection) 
			opMap('$addToSet')[f] = ['$each': v]
		else
			opMap('$addToSet')[f] = v
	}
	
	def get() {
		ops as BasicDBObject
	}
}