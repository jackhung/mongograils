import groovy.util.GroovyTestCase;


import grails.test.*;
import com.mongodb.*;

class MongoCRUDTests extends GroovyTestCase {
	void testDomainInsertUpdateAndRemove() {
		// Insert
		def u = new User(username: "crudTestUser")
		u.mongoInsert()
		assertNotNull u._id
		// Read
		def udoc = User.mongoFindOne([username: "crudTestUser"])
		assertNotNull udoc
		assertEquals u._id, udoc._id
		// Update
		u.password = "password"
		u.mongoUpdate()
		def utmp = User.mongoFindOne([username: "crudTestUser"]).toDomain()
		assertNotNull utmp.password
		assertEquals "password", utmp.password
		// Remove
		u.mongoRemove()		
		udoc = User.mongoFindOne([username: "crudTestUser"])
		assertNull udoc
	}
	
	void testDBRef() {
		User.collection.drop()
		def users = [:]
		users.william = new User(username: "William")
		users.pete = new User(username: "Pete")
		users.june = new User(username: "June")
		users.each {k, v -> v.mongoInsert()}
		
		users.william.father = users.pete.toMongoRef()
		users.william.mother = users.june.toMongoRef()
		users.william.mongoUpdate()
		
		def williamDoc = User.mongoFindOne([username: "William"])
		assertTrue williamDoc.father instanceof DBRef
		assertTrue williamDoc.mother instanceof DBRef
		
		def william = williamDoc.toDomain()
	}
}
