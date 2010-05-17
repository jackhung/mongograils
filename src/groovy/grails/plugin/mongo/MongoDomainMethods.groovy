package grails.plugin.mongo

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import com.mongodb.ObjectId
import com.mongodb.DBRef
import com.mongodb.DBCollection
import com.mongodb.QueryBuilder
import java.lang.reflect.Modifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author jack
 *
 */
class MongoDomainMethods {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MongoDomainMethods)
	private static final BasicDBObject emptyDBObject = new BasicDBObject()

	static enhanceClass = { Class domainClass ->
		def mc = domainClass.metaClass
		mc.static.mongoCount = mongoCount
		mc.static.mongoFind = mongoFind
		mc.static.mongoFind = mongoFindWithQueryBuilder
		mc.static.mongoFind = mongoClosureFindWithQueryBuilder
		
		mc.static.mongoFindOne = MongoDomainMethods.mongoFindOne
		mc.static.mongoFindOne = MongoDomainMethods.mongoFindOneWithQueryBuilder
		mc.static.mongoFindOne = MongoDomainMethods.mongoClosureFindOneWithQueryBuilder
		
//		mc.static.mongoFindAll = MongoDomainMethods.mongoFindAll
		mc.static.mongoQuery = MongoDomainMethods.mongoQuery
		
		//		mc.static.mongoTestMedhod = domainMethods.mongoTestMedhod	// TODO need to re-think update
		
		mc.mongoRefresh = MongoDomainMethods.mongoRefresh
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
		
		cacheDisallowedField(mc)
	}

	/**
	 * The conds parameter have the following implications:
	 * <ul>
	 * <li> default to emptyDBObject if not specified
	 * <li> if it is a Map, it will be converted to <link>BasicDBObject</link>
	 * <li> if it is a BasicDBObject, just passes on
	 * <li> if it is a String or ObjectId, then use it as '_id' value for find-by-id 
	 * </ul>
	 * 
	 * @param conds 
	 */
	static mongoFindOne = { conds = emptyDBObject, fields = emptyDBObject ->
//		if (conds instanceof Map) conds = conds as BasicDBObject
//		if (conds instanceof String) conds = [_id: new ObjectId(conds)] as BasicDBObject
//		if (conds instanceof ObjectId) conds = [_id: conds] as BasicDBObject
		conds = processRequestedCondition(conds)
		if (fields instanceof Map) fields = fields as BasicDBObject
		delegate.getCollection().findOne(conds, fields)
	}
	
	static mongoFind = {conds = emptyDBObject, fields = emptyDBObject ->
//		if (conds instanceof Map) conds = conds as BasicDBObject
		conds = processRequestedCondition(conds)
		if (fields instanceof Map) fields = fields as BasicDBObject
		delegate.getCollection().find(conds, fields)
	}

	static processRequestedCondition(conds) {
		if (conds == null) conds = emptyDBObject
		else if (conds instanceof BasicDBObject) { /* do nothing */}
		else if (conds instanceof Map) conds = conds as BasicDBObject
		else if (conds instanceof String) conds = [_id: new ObjectId(conds)] as BasicDBObject
		else if (conds instanceof ObjectId) conds = [_id: conds] as BasicDBObject
		conds	
	}
	
	// with QueryBuilder
	static mongoFindOneWithQueryBuilder = { QueryBuilder qb, fields = emptyDBObject ->
		if (fields instanceof Map) fields = fields as BasicDBObject
		delegate.getCollection().findOne(qb.get(), fields)
	}
	
	static mongoFindWithQueryBuilder = { QueryBuilder qb, fields = emptyDBObject ->
		if (fields instanceof Map) fields = fields as BasicDBObject
		delegate.getCollection().find(qb.get(), fields)
	}
	
	// with QueryBuilderClosure  User.mongoFind { where("mother.username").is("Mary") }
	static mongoClosureFindOneWithQueryBuilder = { fields = emptyDBObject, Closure c ->
		def qb = evaluateQueryBuilderClosure(c, delegate)
		if (fields instanceof Map) fields = fields as BasicDBObject
		delegate.getCollection().findOne(qb.get(), fields)
	}
	
	static mongoClosureFindWithQueryBuilder = { fields = emptyDBObject, Closure c ->
		def qb = evaluateQueryBuilderClosure(c, delegate)
		if (fields instanceof Map) fields = fields as BasicDBObject
		delegate.getCollection().find(qb.get(), fields)
	}

	static evaluateQueryBuilderClosure(c, delegate) {
		def qb = new MyQueryBuilder()
		qb.start ("_t").is(delegate.getMongoTypeName())
		c.delegate = qb
		c(qb)
	}

	/**
	 * return a new instance.
	 * TODO it would be better to update the delegate instead
	 */
	static mongoRefresh = { ->
		delegate.getCollection().findOne(["_id": delegate._id] as BasicDBObject)
	}
	
//	static mongoFindAll = { ->
//		"mongoFindAll not yet implemented"
//	}
	
	static mongoQuery = { key, byType = true ->
		if (byType)
			return QueryBuilder.start("_t").is(delegate.getMongoTypeName()).and(key)
		else
			return QueryBuilder.start(key)
	}

	// TODO remove me
	static staticTestMedhod(arg) {
		"staticTestMethod $arg"
	}

	def testMethod(arg) {
		"testMethod $arg"
	}
	
	static mongoCount = { ->
		delegate.getCollection().count
	}
	
	static mongoInsert = { options = null ->
		// TODO handle options
		if (delegate.respondsTo("beforeInsert")) delegate.beforeInsert()
		def doc = delegate.toMongoDoc()
		delegate.getCollection().insert(doc)
		delegate._id = doc?._id	// TODO check error?
		if (delegate.respondsTo("afterInsert")) delegate.afterInsert()
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
		if (delegate.respondsTo("afterUpdate")) delegate.afterUpdate()
	}
	
	static mongoRemove = { options = null ->
		// TODO handle options as selector
		if (delegate.respondsTo("beforeRemove")) delegate.beforeDelete()
		if (delegate.respondsTo("beforeDelete")) delegate.beforeDelete()
		if (delegate._id)
			delegate.getCollection().remove([_id : delegate._id] as BasicDBObject)
		if (delegate.respondsTo("afterRemove")) delegate.afterDelete()
		if (delegate.respondsTo("afterDelete")) delegate.afterDelete()
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
		def mc = delegate.metaClass
		def props = mc.properties.name - ignoreProps
		def docMap = [_t: delegate.getMongoTypeName()]
		props.each { p -> 
			def val = delegate."$p"
			if ( !fieldDisallowed(mc, p) ) {	
				log.debug("\t$p -> ${val.getClass()} $val")
				if (val.respondsTo("toMongoDoc")) {
					docMap."$p" = val.toMongoDoc()
				} else {
					if (val instanceof Collection) {
						// TODO Bug, does not respond to toMongoDoc but can invoke toMongoDoc !!
						log.error("FIXME: hack around $val of ${val.getClass().simpleName} not reponds to toMongoDoc()")
						docMap."$p" = val.toMongoDoc()
					} else {
						log.debug("default to val itself for ${val.getClass()}")
						docMap."$p" = val
					}
				}
			}
		}
		docMap as BasicDBObject
	}

	// TODO we need to listen to changeEvent and clean the cache
	static disallowedCache = [:]	// key: className + propertyName
	static cacheDisallowedField(mc) {
		def props = mc.properties.name - ignoreProps
		props.each { p -> 
			def prop = mc.getProperties().find { it.name == p }
			def m = prop.getField().getModifiers()
			if (Modifier.isTransient(m) || Modifier.isStatic(m)) {
				log.debug("Field: $p of Class: ${mc.getJavaClass()} is not persistent")
				disallowedCache.put(mc.getJavaClass().name + p, true)
			}
		}
	}
	                          
	static boolean fieldDisallowed(mc, propName) {
		disallowedCache[mc.getJavaClass().name + propName] != null
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