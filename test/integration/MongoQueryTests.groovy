import javax.swing.text.StyledEditorKit.ItalicAction;


import grails.test.*;
import com.mongodb.*;

/**
 * @author jack
 *
 */
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
	
	void testFindOneWithNestElementQuery() {
		def q = User.mongoQuery("info.height").greaterThan(6.0)
		def rs = User.mongoFind(q)
		assertEquals 1, rs.count()
	}
	
	/*
	 * How about supporting:
	 * result = User.mongoFindOne { qb -> qb.and("someField").is(someValue) }
	 */
	void testFindOneWithQueryBuilderClosure() {
		def user = User.mongoFindOne { qb -> qb.where("mother").exists(true) }
		assertNotNull user.mother
		
		user = User.mongoFindOne { where("mother").exists(true) }
		assertNotNull user.mother
	}
	
	void testFindWithQueryBuilderClosure() {
		def res = User.mongoFind { qb -> qb.where("mother").exists(true) }
		assertEquals 1, res.count()
		
		res = User.mongoFind { where("mother").exists(true).and("father").exists(true) }
		assertEquals 1, res.count()
		def user = res.next().toDomain(fetchRef: true)
		assertEquals "June", user.mother.username
	}
	
	void testFindWithQueryBuilderUsingCustomGrammer() {
		def res = User.mongoFind { where("mother").exists() }
		assertEquals 1, res.count()
		
		res = User.mongoFind { where("mother").notExists() }
		assertEquals 2, res.count()
		
		res = User.mongoFind { where("info.weight").between(100, 150) }
		assertEquals 2, res.count()
		
		res = User.mongoFind { where("info.weight").between(100, 160) }
		assertEquals 2, res.count()
		
		res = User.mongoFind { where("info.weight").between(100, 161) }
		assertEquals 3, res.count()
	}
	
	void testSkipAndLimit() {
		(100..200).each { new User(username: "USER$it").mongoInsert() }
		
		def userCount = User.mongoFind().count()
		def cursor = User.mongoFind().skip(50)
		assertEquals "Should be 50 less", userCount - 50, cursor.itcount()
		
		cursor = User.mongoFind().limit(10)
		assertEquals "limit 10 requested", 10, cursor.itcount()

		cursor = User.mongoFind{ where("username").regex(~/USER12\n?/) }.limit(5)
		assertEquals 5, cursor.itcount()
		
		cursor = User.mongoFind{where("username").regex(~/USER13\n?/)}
					.skip(5).sort(["username": 1] as BasicDBObject)
		assertEquals 10, cursor.count()
		def u = cursor.next().toDomain()
		assertEquals "1st with skip-5-and-sorted should be USER135", "USER135", u.username
	}
	
	void testFindWithFieldsSelection() {
		def user = User.mongoFindOne([username: "Pete"], [info: true]).toDomain()
		assertNotNull "'info' field requested", user.info
		assertNull "Only 'info' field requested", user.username
		
		user = User.mongoFindOne([username: "Pete"], [info: false]).toDomain()
		assertNull "'info' field excluded", user.info
		assertNotNull "all fields except 'info'", user.username
		
		// using query closure
		user = User.mongoFindOne(info: 1) { where("username").is("Pete") }.toDomain()
		assertNotNull "'info' field requested", user.info
		assertNull "Only 'info' field requested", user.username
		
		user = User.mongoFindOne(info: 0) { where("username").is("Pete") }.toDomain()
		assertNull "'info' field excluded", user.info
		assertNotNull "all fields except 'info'", user.username
	}
	
	void testFindWithObjectId() {
		def user = User.mongoFindOne()
		def user2 = User.mongoFindOne(user._id)
		assertNotNull "Find-by-objectId should work", user2
		assertEquals user, user2
	}
	
	void testFindWithStringObjectId() {
		def user = User.mongoFindOne()
		def idStr = user._id.toString()
		def user2 = User.mongoFindOne(idStr)
		assertNotNull "Find-by-string-id should work", user2
		assertEquals user, user2
	}
}
