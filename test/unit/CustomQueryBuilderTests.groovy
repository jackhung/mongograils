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
	
	void testBetweenQuery() {
		def expected = '{ "value" : { "$gt" : 1 , "$lt" : 100}}'
		def q = new MyQueryBuilder()
		q.where("value").between(1, 100)
		
		assertEquals expected, q.get().toString()
	}
	
	void testNestedQuery() {
		def expected = '{ "info" : { "code" : "A"}}'
		def q = new MyQueryBuilder()
		q.where("info"){ where("code").is("A")}	// this is not too intuitive
		
		assertEquals expected, q.get().toString()
	}
	
	void testElementMatchQuery() {
		def expected = '{ "info" : { "$elemMatch" : { "code" : "A" , "value" : "VA"}}}'
		def q = new MyQueryBuilder()
		q.elemMatch("info") { where("code").is("A").and("value").is("VA") }
		
		assertEquals expected, q.get().toString()
	}
	
}
