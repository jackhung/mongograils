package grails.plugin.mongo

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import com.mongodb.ObjectId
import com.mongodb.DBRef
import com.mongodb.DBCollection

class MongoDomainMethods {
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
		println "====> $doc"
		delegate._id = doc?._id	// TODO check error?
	}
	
	public mongoUpdate(delegate, options = null ) {
		collection.update(
			[_id: objectId(delegate._id)] as BasicDBObject,
			delegate.toMongoDoc(),
			false,
			false
			)
	}
	
	public mongoRemove(delegate, options = null) {
		// TODO handle options
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

	def ignoreProps = ["log", "class", "constraints", "properties", "id", "version", "errors", "collection", "metaClass"]
	def toMongoDoc(delegate) {
		def props = delegate.metaClass.properties.name - ignoreProps
		def docMap = [_t: "user"]
		props.each { p -> 
			def val = delegate."$p"
			if (val.respondsTo("toMongoDoc")) {
				docMap."$p" = val.toMongoDoc()
			} else {
					docMap."$p" = val
			}
		}
		docMap as BasicDBObject
	}
	
	private objectId(id) {
		id instanceof ObjectId ? id : new ObjectId(id)
	}
}
