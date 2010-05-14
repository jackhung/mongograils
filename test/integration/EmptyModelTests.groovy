
import grails.test.*;
import com.mongodb.*;

/**
 * @author jack
 *
 */
class EmptyModelTests extends GrailsUnitTestCase {
	def nodes = [:]
		
	protected void setUp() {
		super.setUp()
		Misc.collection.drop()
	}
	
	void testCRUDforEmptyDomainClass() {
		(100..199).each {
			def misc = new Misc()
			misc.code = "CODE:$it" as String
			misc.value = it
			misc.mongoInsert()
		}
		
		assertEquals 100, Misc.mongoFind().count()
		assertEquals 10, Misc.mongoFind{ where("code").regex(~/CODE:12\n?/) }.count()
			
		def misc = Misc.mongoFindOne{ where("code").is("CODE:120") }.toDomain()
		misc.mongoPerform {
			increment "value"	
		}
		assertEquals 121, Misc.mongoFindOne{ where("code").is("CODE:120") }.toDomain().value
	}
	
	void testSkipTransientField() {
		def misc = new Misc()
		misc.code = "DoNotSaveTest"
		misc.mongoInsert()
		
		def misc2 = Misc.mongoFindOne{ where("code").is("DoNotSaveTest") }.toDomain()
		assertNull misc2.doNotSave
	}
	
}
