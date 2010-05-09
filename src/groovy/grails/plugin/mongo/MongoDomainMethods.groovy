package grails.plugin.mongo

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import com.mongodb.ObjectId
import com.mongodb.DBRef
import com.mongodb.DBCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MongoDomainMethods {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MongoDomainMethods)
	
	DBCollection collection
	
	MongoDomainMethods(collection) {
		this.collection = collection
	}
	
	public mongoFindOne(options = null) {
		if (options instanceof Map) {
			collection.findOne(options as BasicDBObject)
		} else
			collection.findOne()
	}
	
	public mongoFind(options = null) {
		if (options instanceof Map) {
			collection.find(options as BasicDBObject)
		} else
			collection.find()
	}
	
	public mongoFindAll() {
		"mongoFindAll not yet implemented"
	}
	
	public mongoInsert(delegate, options = null) {
		// TODO handle options
		def doc = delegate.toMongoDoc()
		collection.insert(doc)
		delegate._id = doc?._id	// TODO check error?
	}
	
	public mongoUpdate(delegate, options = null ) {
		// TODO handle options as selector
		collection.update(
		[_id: objectId(delegate._id)] as BasicDBObject,
		delegate.toMongoDoc(),
		false,
		false
		)
	}
	
	public mongoRemove(delegate, options = null) {
		// TODO handle options as selector
		if (delegate._id)
			collection.remove([_id : delegate._id] as BasicDBObject)
	}
	
	public toMongoRef(delegate) {
		// TODO should we use collection.fullName?
		new DBRef(collection.getDB(), collection.name, objectId(delegate._id))
	}
	
	def putField(String name, args, delegate) {
		if (delegate.metaClass.hasProperty(delegate, name)) {
			delegate."$name" = args
		} else {
			delegate.metaClass."$name" = args
		}
	}
	
	def getField(String name, delegate) {
		if (delegate.metaClass.hasProperty(delegate, name)) {
			delegate."$name"
		} else {
			null
		}
	}
	
	def ignoreProps = ["log", "class", "constraints", "properties", "id", "version", "errors", "collection", "mongoTypeName", "metaClass"]
	def toMongoDoc(delegate) {
		log.debug("${delegate}.toMongoDoc()")
		def props = delegate.metaClass.properties.name - ignoreProps
		def docMap = [_t: delegate.getMongoTypeName()]
		props.each { p -> 
			def val = delegate."$p"
			log.debug("\t$p -> ${val.getClass()} $val")
			if (val.respondsTo("toMongoDoc")) {
				docMap."$p" = val.toMongoDoc()
			} else {
				if (val instanceof ArrayList) {
					// TODO Bug does not respond to toMongoDoc but can invoke toMongoDoc !!
					log.error("FIXME: hack around $val of ${val.getClass().simpleName} not reponds to toMongoDoc()")
					docMap."$p" = val.toMongoDoc()
				} else
					docMap."$p" = val
			}
		}
		docMap as BasicDBObject
	}
	
	private objectId(id) {
		id instanceof ObjectId ? id : new ObjectId(id)
	}
}
