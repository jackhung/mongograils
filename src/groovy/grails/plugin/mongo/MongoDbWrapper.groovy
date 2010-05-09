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
	Map<String, DBCollection> collections = [:]
	Map<String, Class> mongoDomainClass = ["user": User.class]

        void afterPropertiesSet() {
		this.mongo = new Mongo("localhost", 27017)
		this.collections."users" = mongo.getDB("demoapp").getCollection("users")
		MongoUtils.decorateClasses(this)
	}

	void addDomainClass(Class clazz) {
		def mc = clazz.metaClass
		def collectionName = clazz.getAnnotation(MongoCollection.class)?.value()
		mc.static."getCollection" = { this.collections."$collectionName" }
		/*
		 * MongonDomainMethods should simple be mixin-ed into all DomainClass, but
		 * there seemed to be some problem in using mixin and GORM :(
		 */
		def domainMethods = new MongoDomainMethods(this.collections."$collectionName")
		mc.static.mongoFind = { opts = null ->
			domainMethods.mongoFind(opts)
		}
		mc.static.mongoFindOne = { opts = null ->
			domainMethods.mongoFindOne(opts)
		}
		mc.static.mongoFindAll = {
			//mdc.invoke(mc.javaClass, "mongoFindAll", [] as Object[])
			domainMethods.mongoFindAll()
		}
		mc.toMongoDoc = {
			domainMethods.toMongoDoc(delegate)
		}
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
