
import grails.test.*;
import com.mongodb.*;

class MongoDomainTests extends MongoTestCase {
	
	protected void setUp() {
		super.setUp()
		initUsers()
	}

	void testDynamicField() {
		def u = new User(username: "Jack")
		assert u.username == "Jack"

		u."newFld" = 1234
		assert u."newFld" == 1234
	}

	void testMongoFindOneNoQuery() {
		def u = User.mongoFindOne()
		assert u
	}

	void testMongoFindOneWithQuery() {
		def u = User.mongoFindOne([username: "June"])
		assert u.username == "June"
		assert u instanceof BasicDBObject

		u = User.mongoFindOne([username: "Nobody"])
		assert u == null
	}

	void testBasicDBObjectToDomain() {
		def doc = User.mongoFindOne([username: "William"])
		def user = doc.toDomain(true)

		assert user.username == "William"
		assert user._id instanceof ObjectId
		assert user.father.username == "Pete"
		assert user.father instanceof User
		assert user.mother.username == "June"
	}

	void testMogoDOcDomainConversion() {
		def mogoDoc = User.mongoFindOne([username: "William"])
		def domainObj = mogoDoc.toDomain(true)
		def mogoDoc2 = domainObj.toMongoDoc()
		def domainObj2 = mogoDoc2.toDomain(true)

		assert domainObj2.username == "William"
		assert domainObj2.father.username == "Pete"
		assert domainObj2.mother.username == "June"
	}
}
