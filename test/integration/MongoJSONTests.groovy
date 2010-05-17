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

		// to JSON and back
		/* this approach does not handle ObjectId correctly
		def jsonMisc = miscDoc.encodeAsJSON()
		def miscDocFromJSON = JSON.parse(jsonMisc.toString()) as BasicDBObject
		
		assertTrue "Not expected to be instance of ${miscDocFromJSON.class}", miscDocFromJSON instanceof BasicDBObject
		*/
		def jsonStr = miscDoc.toString()
		def miscDOCFromJSON = JSON.parse(jsonStr) as BasicDBObject
		miscDOCFromJSON._id = new ObjectId(miscDOCFromJSON._id)	// this does the trick

		def misc2 = miscDOCFromJSON.toDomain()
		assertEquals "JSONTest001", misc2.code
		assertEquals miscDoc._id, miscDOCFromJSON._id
		
		// make some change on the dehydrated obj, to make sure it still behave as normal
		misc2.mongoPerform {
			set "dimension", [w: 100, h: 200]
		}
		misc2 = misc2.mongoRefresh()
		assertEquals 100, misc2.dimension.w
		assertEquals 200, misc2.dimension.h
	}
	
}
