package grails.plugin.mongo

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import com.mongodb.ObjectId
import com.mongodb.DBRef
import com.mongodb.DBCollection
import com.mongodb.QueryBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author jack
 *
 */
class MongoDomainMethods {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MongoDomainMethods)

	static enhanceClass = { Class domainClass ->
		def mc = domainClass.metaClass
		mc.static.mongoFind = mongoFind
		mc.static.mongoFind = mongoFindWithQueryBuilder
		mc.static.mongoFind = mongoClosureFindWithQueryBuilder
		
		mc.static.mongoFindOne = MongoDomainMethods.mongoFindOne
		mc.static.mongoFindOne = MongoDomainMethods.mongoFindOneWithQueryBuilder
		mc.static.mongoFindOne = MongoDomainMethods.mongoClosureFindOneWithQueryBuilder
		
		mc.static.mongoFindAll = MongoDomainMethods.mongoFindAll
		mc.static.mongoQuery = MongoDomainMethods.mongoQuery
		
		//		mc.static.mongoTestMedhod = domainMethods.mongoTestMedhod	// TODO need to re-think update
		
		mc.mongoInsert = MongoDomainMethods.mongoInsert
		mc.mongoRemove = MongoDomainMethods.mongoRemove
		mc.mongoUpdate = MongoDomainMethods.mongoUpdate	// Do not use
		mc.toMongoDoc = MongoDomainMethods.toMongoDoc
		mc.toMongoRef = MongoDomainMethods.toMongoRef
		mc.putField = MongoDomainMethods.putField
		mc.getField = MongoDomainMethods.getField
		mc.propertyMissing = { String name, val ->
			putField(name, val)
		}
		mc.propertyMissing = { String name ->
			getField(name)
		}
	}

	static mongoFindOne = { options = null ->
		if (options instanceof Map) {
			delegate.getCollection().findOne(options as BasicDBObject)
		} else
			delegate.getCollection().findOne()
	}
	
	static mongoFind = {options = null ->
		if (options instanceof Map) {
			delegate.getCollection().find(options as BasicDBObject)
		} else
			delegate.getCollection().find()
	}
	
	// with QueryBuilder
	static mongoFindOneWithQueryBuilder = { QueryBuilder qb ->
		delegate.getCollection().findOne(qb.get())
	}
	
	static mongoFindWithQueryBuilder = { QueryBuilder qb ->
		delegate.getCollection().find(qb.get())
	}
	
	// with QueryBuilderClosure  User.mongoFind { that("mother.username").is("Mary") }
	static mongoClosureFindOneWithQueryBuilder = { Closure c ->
		def qb = evaluateQueryBuilderClosure(c, delegate)
		delegate.getCollection().findOne(qb.get())
	}
	
	static mongoClosureFindWithQueryBuilder = { Closure c ->
		def qb = evaluateQueryBuilderClosure(c, delegate)
		delegate.getCollection().find(qb.get())
	}

	static evaluateQueryBuilderClosure(c, delegate) {
		def qb = new MyQueryBuilder()
		qb.start ("_t").is(delegate.getMongoTypeName())
		c.delegate = qb
		c(qb)
	}
	
	static mongoFindAll = { ->
		"mongoFindAll not yet implemented"
	}
	
	static mongoQuery = { key, byType = true ->
		if (byType)
			return QueryBuilder.start("_t").is(delegate.getMongoTypeName()).and(key)
		else
			return QueryBuilder.start(key)
	}

	// TODO remove me
	static mongoTestMedhod = { arg ->
		"$delegate mongoTestMethod $arg"
	}
	
	static mongoInsert = { options = null ->
		// TODO handle options
		if (delegate.respondsTo("beforeInsert")) delegate.beforeInsert()
		def doc = delegate.toMongoDoc()
		delegate.getCollection().insert(doc)
		delegate._id = doc?._id	// TODO check error?
		delegate
	}

	/**
	 * Warn: do not use, not working as expected!!
	 */
	static mongoUpdate = { Map options = [:], Closure c = null ->
		// TODO handle options as selector
		log.warn "Lot of problem with mongoUpdate!! Do not use: ${delegate.toMongoDoc()}"
		if (delegate.respondsTo("beforeUpdate")) delegate.beforeUpdate()
		delegate.getCollection().update(
			[_id: objectId(delegate._id), "_t" : delegate.getMongoTypeName()] as BasicDBObject,
			delegate.toMongoDoc(),
			options.upsert?: false,
			options.multi?: false
		)
	}
	
	static mongoRemove = { options = null ->
		// TODO handle options as selector
		if (delegate.respondsTo("beforeRemove")) delegate.beforeUpdate()
		if (delegate.respondsTo("beforeDelete")) delegate.beforeDelete()
		if (delegate._id)
			delegate.getCollection().remove([_id : delegate._id] as BasicDBObject)
	}
	
	static toMongoRef = {
		// TODO should we use collection.fullName?
		new DBRef(delegate.getCollection().getDB(), delegate.getCollection().name, objectId(delegate._id))
	}
	
	static putField = { String name, args ->
		if (delegate.metaClass.hasProperty(delegate, name)) {
			delegate."$name" = args
		} else {
			delegate.metaClass."$name" = args
		}
	}
	
	static getField = { String name ->
		if (delegate.metaClass.hasProperty(delegate, name)) {
			delegate."$name"
		} else {
			null
		}
	}
	
	static ignoreProps = ["log", "class", "constraints", "properties", "id", "version", "errors", "collection", "mongoTypeName", "metaClass"]
	static toMongoDoc = {
		log.debug("${delegate}.toMongoDoc()")
		def props = delegate.metaClass.properties.name - ignoreProps
		def docMap = [_t: delegate.getMongoTypeName()]
		props.each { p -> 
			def val = delegate."$p"
			if (val instanceof Closure)
			log.debug("\t$p -> ${val.getClass()} $val")
			if (val.respondsTo("toMongoDoc")) {
				docMap."$p" = val.toMongoDoc()
			} else {
				if (val instanceof Collection) {
					// TODO Bug does not respond to toMongoDoc but can invoke toMongoDoc !!
					log.error("FIXME: hack around $val of ${val.getClass().simpleName} not reponds to toMongoDoc()")
					docMap."$p" = val.toMongoDoc()
				} else {
					log.debug("default to val itself for ${val.getClass()}")
					docMap."$p" = val
				}
			}
		}
		docMap as BasicDBObject
	}
	
	static private objectId(id) {
		id instanceof ObjectId ? id : new ObjectId(id)
	}
}

class MyQueryBuilder extends QueryBuilder {
	def where(e) {
		and(e)
	}
	
	def exists() {
		exists true
	}
	
	def notExists() {
		exists false
	}
	
	def between(a, b) {
		greaterThan(a).lessThan(b)
	}
	
}