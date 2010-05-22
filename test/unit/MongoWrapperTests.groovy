import org.springframework.beans.BeanWrapperImpl;
import grails.converters.JSON
import groovy.util.GroovyTestCase;

import org.codehaus.groovy.grails.web.binding.DataBindingUtils;
import org.codehaus.groovy.grails.web.binding.GrailsDataBinder;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import grails.test.*;
import com.mongodb.*;
import grails.plugin.mongo.*;
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

/**
 * @author jack
 *
 */
class MongoWrapperTests extends GroovyTestCase {
	
	
	void testMixinMongoDynamicMethods() {
		def gcl = new GroovyClassLoader()
		def dc = gcl.parseClass("""
				import grails.plugin.mongo.MongoCollection
				import grails.plugin.mongo.MongoTypeName

				@MongoCollection("mydomains")
				@MongoTypeName("mydomain")
				class MyDomain { 
				Long id; Long version; String name; Long balance;
				}""")
		assertNotNull dc
		def domainClass = new DefaultGrailsDomainClass(dc)
		def mongo = new MongoDbWrapper() // ctx.getBean('mongo')
		def ga = new DefaultGrailsApplication(gcl.getLoadedClasses(),gcl)
		mongo.grailsApplication = ga
		mongo.afterPropertiesSet()
		mongo.addDomainClass(domainClass.clazz)
		assertEquals "mydomains", dc.collection.name
		assertTrue dc.metaClass.hasMetaMethod("mongoFind")
	}
}
