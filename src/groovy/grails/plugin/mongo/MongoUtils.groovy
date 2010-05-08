package grails.plugin.mongo

import com.mongodb.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MongoUtils {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MongoUtils)

	static decorateBasicDBObject(wrapper) {
		def mc = BasicDBObject.metaClass
		mc.toDomain = {
			def typeName = delegate.get("_t")
			if (!typeName) {
				log.warn "No typeName for mongoDoc"
				delegate
			}
			
			def domainClass = wrapper.mongoDomainClass."$typeName"
			if (!domainClass) {
				log.warn "Cannot find DomainClass for $typeName"
				delegate
			}
			
			def domainObject = domainClass.newInstance()
			(delegate.keySet() - ["_t"]).each { key ->
				def value = delegate."$key"
				if (value instanceof DBRef) {
					domainObject."$key" = value.fetch().toDomain()
				} else 
					domainObject."$key" = value
			}
			domainObject
		}
	}
}
