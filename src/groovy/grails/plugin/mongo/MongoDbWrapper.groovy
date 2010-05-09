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
		def collectionName = clazz.getAnnotation(MongoCollection.class)?.value()	// TODO :? default
		def coll = getDBCollection(collectionName)
		def typeName = clazz.getAnnotation (MongoTypeName.class)?.value()			// TODO :? default
		typeToDomainClassMap[typeName] = clazz
		
		/*
		 * MongonDomainMethods should simply be mixin-ed into all DomainClass, but
		 * there seemed to be some problem in using mixin and GORM :(
		 */
		def domainMethods = new MongoDomainMethods(coll)
		mc.static.getCollection = { coll }		// TODO put coll and typeName in MongoMetaInfo so not to clobber the namespace
		mc.static.getMongoTypeName = { typeName }
		mc.static.mongoFind = domainMethods.mongoFind
		mc.static.mongoFindOne = domainMethods.mongoFindOne
		mc.static.mongoFindAll = domainMethods.mongoFindAll
		mc.static.mongoTestMedhod = domainMethods.mongoTestMedhod
		mc.mongoInsert = domainMethods.mongoInsert
		mc.mongoRemove = domainMethods.mongoRemove
		mc.mongoUpdate = domainMethods.mongoUpdate
		mc.toMongoDoc = domainMethods.toMongoDoc
		mc.toMongoRef = domainMethods.toMongoRef
		mc.putField = domainMethods.putField
		mc.getField = domainMethods.getField
		mc.propertyMissing = { String name, val ->
			putField(name, val)
		}
		mc.propertyMissing = { String name ->
			getField(name)
		}
	}
}
