package grails.plugin.mongo

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList

class MongoDomainMethods {
	def collection

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
}
