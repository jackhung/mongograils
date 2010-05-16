import groovy.util.GroovyTestCase;


import grails.test.*;
import com.mongodb.*;

/**
 * @author jack
 *
 */
class MongoCRUDTests extends MongoTestCase {
	void testDomainInsertUpdateAndRemove() {
		// Insert
		def u = new User(username: "crudTestUser")
		u.mongoInsert()
		assertNotNull u._id
		// Read
		def udoc = User.mongoFindOne(username: "crudTestUser")
		assertNotNull udoc
		assertEquals u._id, udoc._id
		// Update
		u.mongoPerform {
			set "password", "password"
		}
		def utmp = User.mongoFindOne(username: "crudTestUser").toDomain()
		assertNotNull utmp.password
		assertEquals "password", utmp.password
		// Remove
		u.mongoRemove()		
		udoc = User.mongoFindOne(username: "crudTestUser")
		assertNull udoc
	}
	
	/**
	 * Not working as expected, use perform
	 * @depreciated
	 */
	void testMongoUpdateWithOptions() {
		def u = new User(username: "updateTestUser1")
		u.salary = 10000.0
		u.mongoInsert()
		
		def doc = User.mongoFindOne(username: "updateTestUser1").toDomain()
		doc.mongoPerform {
			set "salary", 10500.0
		}
		assertEquals 10500.0, User.mongoFindOne(username: "updateTestUser1").toDomain().salary
		
		doc.mongoPerform {
			increment "salary", 50.0
		}
		assertEquals 10550.0, User.mongoFindOne(username: "updateTestUser1").toDomain().salary
	}
	
	void testEmbedded() {
		def u = new User(username: "crudTestUser1")
		u.buddy = new User(username: "crudTestUser2")
		u.mongoInsert()
		
		def doc = User.mongoFindOne(username: "crudTestUser1")
		assertTrue doc.toDomain().buddy instanceof User
	}
	
	void testTypeNameSaveWithDoc() {
		Account.collection.drop()
		def acct = new Account(accountNumber: "001234", accountCode: "PR")
		acct.mongoInsert()
		def acctDoc = Account.mongoFindOne(accountNumber:  "001234", accountCode: "PR")
		assertEquals Account.mongoTypeName, acctDoc._t
		
		// Account attached into User
		def u = new User(username: "acctUser1")
		u.accounts = []
		u.accounts << acct
		u.mongoInsert()		// TODO this cause an exception, the ArrayList does not response to toMongoDoc
							// see hack in MongoDomainMethods. Need fixing!!
		// TODO make this work u = User.mongoFindOne(_id: u._id)
//		u = User.mongoFindOne(username: "acctUser1")
		u = u.mongoRefresh()
		assertEquals "001234",u.accounts[0].accountNumber
		
//		assertEquals 1, Account.collection.count
		assertEquals 1, Account.mongoCount()
	}
	
	void testBeforeXXX() {
		def u = new User(username: "testBeforeXXX")
		u.mongoInsert()
		assertNotNull u.dateCreated
	}
	
	void testDBRef() {
		initUsers(drop: true)
		
		def williamDoc = User.mongoFindOne(username: "William")
		assertTrue williamDoc.father instanceof DBRef
		assertTrue williamDoc.mother instanceof DBRef
		
		// toDomain() default not to fetch DBRef
		assertTrue williamDoc.toDomain().father instanceof DBRef
		// fetch DBRef
		assertTrue williamDoc.toDomain(true).father instanceof User
	}
}
