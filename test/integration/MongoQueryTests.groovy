
import grails.test.*;
import com.mongodb.*;

class MongoQueryTests extends MongoTestCase {
	
	protected void setUp() {
		super.setUp()
		initUsers(drop: true)
	}

	void testBasicQuery() {
		def q = User.mongoQuery("username").is("William")	//.list()
		def u = User.mongoFindOne(q)
		//assertEquals 1, rs.count()
		//def u = rs.next()
		assertEquals "William", u.username	
	}
	
	void testQueryWithout_t() {
		def q = User.mongoQuery("username", false).is("William")
		def rs = User.mongoFind(q)
		assertEquals 1, rs.count()
		def u = rs.next()
		assertEquals "William", u.username
	}
	
	void testFindOneWithQuery() {
		def q = User.mongoQuery("father").exists(false)
		def rs = User.mongoFind(q)
		assertEquals 2, rs.count()
	}
	
	/*
	 * How about supporting:
	 * result = User.mongoFindOne { qb -> qb.and("someField").is(someValue) }
	 */
	void testFindOneWithQueryBuilderClosure() {
		def user = User.mongoFindOne { qb -> qb.and("mother").exists(true) }
		assertNotNull user.mother
	}
}
