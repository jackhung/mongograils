
import grails.test.*;
import com.mongodb.*;

class MongoDomainTests extends MongoTestCase {

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
		def u = User.mongoFindOne([username: "Mary"])
		assert u.username == "Mary"
		assert u instanceof BasicDBObject

		u = User.mongoFindOne([username: "Nobody"])
		assert u == null
	}

	void testBasicDBObjectToDomain() {
		def doc = User.mongoFindOne([username: "William"])
		def user = doc.toDomain()

		assert user.username == "William"
		assert user._id instanceof ObjectId
		assert user.father.username == "Pete"
		assert user.father instanceof User
		assert user.mother.username == "Mary"
	}
}
