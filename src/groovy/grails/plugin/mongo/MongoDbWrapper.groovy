package grails.plugin.mongo

import org.springframework.beans.factory.InitializingBean
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import com.mongodb.Mongo;
import com.mongodb.DBApiLayer
import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import com.mongodb.DBCollection
import com.mongodb.ObjectId
import com.mongodb.gridfs.GridFS
import com.mongodb.DB
import com.mongodb.gridfs.GridFSInputFile
import com.mongodb.DBObject
import com.mongodb.DBRef

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MongoDbWrapper implements InitializingBean {
	GrailsApplication grailsApplication
	Mongo mongo
	private DB database
	private Map<String, Class> typeToDomainClassMap = [:]	//["user": User.class]

    void afterPropertiesSet() {
		def host = grailsApplication.config.mongo?.host ?: "localhost"
		def port = grailsApplication.config.mongo?.port ?: 27017
		def dbname = grailsApplication.config.mongo?.dbname ?: "demoapp"
		this.mongo = new Mongo(host, port)
		this.database = mongo.getDB(dbname)
		MongoUtils.decorateClasses(this)
	}
	
	public Class getDomainClassForType(String type) {
		typeToDomainClassMap[type]
	}
	
	private getDB() {
		database
	}
	
	private getDBCollection(name) {
		return getDB().getCollection(name)
	}

	void addDomainClass(Class clazz) {
		def mc = clazz.metaClass
		def collectionName = clazz.getAnnotation(MongoCollection.class)?.value()
		def coll = getDBCollection(collectionName)
		def typeName = clazz.getAnnotation (MongoTypeName.class)?.value()
		typeToDomainClassMap[typeName] = clazz
		
		/*
		 * MongonDomainMethods should simple be mixin-ed into all DomainClass, but
		 * there seemed to be some problem in using mixin and GORM :(
		 */
		def domainMethods = new MongoDomainMethods(coll)
		mc.static.getCollection = { coll }
		mc.static.getMongoTypeName = { typeName }
				mc.static.getMongoTypeName = { typeName }
		mc.static.mongoFind = { opts = null ->
			domainMethods.mongoFind(opts)
		}
		mc.static.mongoFindOne = { opts = null ->
			domainMethods.mongoFindOne(opts)
		}
		mc.static.mongoFindAll = { domainMethods.mongoFindAll() }
		mc.mongoInsert = { domainMethods.mongoInsert(delegate) }
		mc.mongoRemove = { domainMethods.mongoRemove(delegate) }
		mc.mongoUpdate = { domainMethods.mongoUpdate(delegate) }
		mc.toMongoDoc = { domainMethods.toMongoDoc(delegate) }
		mc.toMongoRef = { domainMethods.toMongoRef(delegate) }
		mc.putField = { String name, val ->
			domainMethods.putField(name, val, delegate)
		}
		mc.getField = { String name ->
			domainMethods.getField(name, delegate)
		}
		mc.propertyMissing = { String name, val ->
			domainMethods.putField(name, val, delegate)
		}
		mc.propertyMissing = { String name ->
			domainMethods.getField(name, delegate)
		}
	}
}
