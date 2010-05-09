package grails.plugin.mongo

import com.mongodb.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MongoUtils {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MongoUtils)
	
	static decorateClasses(wrapper) {
		decorateBasicDBObject wrapper
		decorateBasicDBList()
		decorateDBRef()
		decorateCollection()
	}
	
	// BasicDBObject.toDomain()
	static decorateBasicDBObject(wrapper) {
		def mc = BasicDBObject.metaClass
		mc.toDomain = { fetchRef = false ->
			def typeName = delegate.get("_t")
			if (!typeName) {
				log.warn "No typeName for mongoDoc"
				delegate
			}
			
			def domainClass = wrapper.getDomainClassForType(typeName)
			if (!domainClass) {
				log.warn "Cannot find DomainClass for $typeName"
				delegate
			}
			
			def domainObject = domainClass.newInstance()
			(delegate.keySet() - ["_t"]).each { key ->
				def value = delegate."$key"
				if (value.respondsTo("toDomain")) {
					if (value instanceof DBRef && fetchRef)
						domainObject."$key" = value.fetch().toDomain(fetchRef)
					else
						domainObject."$key" = value.toDomain(fetchRef)
				} else 
					domainObject."$key" = value
			}
			domainObject
		}
	}
	// BasicDBList.toDomain()
	static decorateBasicDBList() {
		def mc = BasicDBList.metaClass
		mc.toDomain = { fetchRef = false ->
			List oList = new ArrayList((int) delegate.size())
			delegate.each {
				if (it.respondsTo("toDomain") ) oList.add(it.toDomain(fetchRef))
				else oList.add(it)
			}
			return oList
		}
	}
	// DBRef.toDomain()
	static decorateDBRef() {
		DBRef.metaClass.toDomain = { fetchRef = false ->
			if (!fetchRef)
				return delegate
			delegate.fetch().toDomain(fetchRef)
		}
	}
	static decorateCollection() {
		Collection.metaClass.toMongoDoc = {
			BasicDBList newList = new BasicDBList()
			delegate.each {
				if (it.respondsTo("toMongoDoc")) newList << it.toMongoDoc()
				else newList << it
			}
			return newList
		}
	}
}
