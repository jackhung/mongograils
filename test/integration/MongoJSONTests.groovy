import org.springframework.beans.BeanWrapperImpl;
import grails.converters.JSON
import org.codehaus.groovy.grails.web.binding.DataBindingUtils;
import org.codehaus.groovy.grails.web.binding.GrailsDataBinder;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import grails.test.*;
import com.mongodb.*;

/**
 * @author jack
 *
 */
class MongoJSONTests extends GrailsUnitTestCase {
		
	protected void setUp() {
		super.setUp()

		Misc.getCollection().drop()
	}
	
	
	void testMongoAsJSON() {
		def misc = new Misc()
		misc.code = "JSONTest001"
		misc.mongoInsert()
		
		def miscDoc = Misc.mongoFindOne {where("code").is("JSONTest001")}
		def jsonMisc = miscDoc.encodeAsJSON()
		def miscDocFromJSON = JSON.parse(jsonMisc.toString()) as BasicDBObject
		
		assertTrue "Not expected to be instance of ${miscDocFromJSON.class}", miscDocFromJSON instanceof BasicDBObject
		
		misc = miscDocFromJSON.toDomain()
		assertEquals "JSONTest001", misc.code
	}
	
}
