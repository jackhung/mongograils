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
		assertEquals userCount - 50, cursor.itcount()
		
		cursor = User.mongoFind().limit(10)
		assertEquals 10, cursor.itcount()

		cursor = User.mongoFind{ where("username").regex(~/USER12?/) }.limit(5)
		assertEquals 5, cursor.itcount()
		
		cursor = User.mongoFind{where("username").regex(~/USER13.*/)}.skip(5).sort(["username": 1] as com.mongodb.BasicDBObject)
//		assertEquals 5, cursor.itcount()
		assertEquals 10, cursor.count()
		def u = cursor.next().toDomain()
		assertEquals "USER135", u.username
	}
}
