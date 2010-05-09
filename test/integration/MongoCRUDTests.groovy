import groovy.util.GroovyTestCase;


import grails.test.*;
import com.mongodb.*;

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
		u.password = "password"
		u.mongoUpdate()
		def utmp = User.mongoFindOne(username: "crudTestUser").toDomain()
		assertNotNull utmp.password
		assertEquals "password", utmp.password
		// Remove
		u.mongoRemove()		
		udoc = User.mongoFindOne(username: "crudTestUser")
		assertNull udoc
	}
	
	void testEmbedded() {
		def u = new User(username: "crudTestUser1")
		u.buddy = new User(username: "crudTestUser2")
		u.mongoInsert()
		
		def doc = User.mongoFindOne(username: "crudTestUser1")
		println "============== $doc.toDomain()"
		assertTrue doc.toDomain().buddy instanceof User
	}
	
	void testTypeNameSaveWithDoc() {
		Account.collection.drop()
		def acct = new Account(accountNumber: "001234", accountCode: "PR")
		acct.mongoInsert()
		def acctDoc = Account.mongoFindOne(accountNumber:  "001234", accountCode: "PR")
		assertEquals Account.mongoTypeName, acctDoc._t
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
