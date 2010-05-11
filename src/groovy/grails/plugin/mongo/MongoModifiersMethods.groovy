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
		println builder.get()
		delegate.getCollection().update(selector, builder.get())
	}
}

class CustomBasicDBObjectBuilder {
	//@Delegate BasicDBObjectBuilder builder = new BasicDBObjectBuilder()
	def ops = [:]
	def set(f, v) {
		ops['$set'] = ops['$set'] ?: [:]
		ops['$set'][f] = v
		this
	}
	
	def increment(f, v = 1) {
		ops['$inc'] = ops['$inc'] ?: [:]
		ops['$inc'][f] = v
		this
	}
	
	def push(f, v) {
		ops['$push'] = ops['$push'] ?: [:]
		ops['$push'][f] = v
		this		
	}
	
	def get() {
		ops as BasicDBObject
	}
}