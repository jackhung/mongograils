import org.springframework.beans.BeanWrapperImpl;
import grails.converters.JSON
import groovy.util.GroovyTestCase;

import org.codehaus.groovy.grails.web.binding.DataBindingUtils;
import org.codehaus.groovy.grails.web.binding.GrailsDataBinder;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import grails.test.*;
import com.mongodb.*;
import grails.plugin.mongo.*;

/**
 * @author jack
 *
 */
class CustomQueryBuilderTests extends GroovyTestCase {		
	
	void testElementMatchQuery() {
		def expected = '{ "info" : { "$elemMatch" : { "code" : "A" , "value" : "VA"}}}'
		def q = new MyQueryBuilder()
		q.elemMatch("info") { where("code").is("A").and("value").is("VA") }
		
		assertEquals expected, q.get().toString()
	}
	
}
