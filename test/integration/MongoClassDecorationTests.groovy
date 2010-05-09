import groovy.util.GroovyTestCase;


import grails.test.*;
import com.mongodb.*;

class MongoClassDecorationTests extends GroovyTestCase {
	/**
	 * Nested list of domain objects and what ever to MongoDoc and back again.
	 * Note also that one of the User object also contains a Hash <code>measures</code>.
	 */
	void testDBListToDomain() {
		def dbList = new BasicDBList()
		def subDBList = new BasicDBList()
		def u1 = new User(username: "MJ")
		def u2 = new User(username: "TJ")
		u1.measures = [w:110, h: 5.8]
		dbList << "testdblist" << u1.toMongoDoc() << subDBList << 123
		subDBList << "subdblist" << u2.toMongoDoc()
		
		def domainList = dbList.toDomain()
		assertEquals 4, domainList.size()
		assertTrue domainList[1] instanceof User
		assertEquals "MJ", domainList[1].username
		
		assertTrue domainList[2] instanceof List
		assertTrue domainList[2][1] instanceof User
		assertEquals "TJ", domainList[2][1].username
		
		def list = domainList.toMongoDoc()
		assertEquals "testdblist", list[0]
		assertEquals "MJ", list[1].username
		assertEquals "TJ", list[2][1].username
		
		// do we get the map back
		
		assertEquals 110, list[1].measures.w
	}
}
