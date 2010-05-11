
import grails.test.*;
import com.mongodb.*;

class MongoFindAndModifyTests extends MongoTestCase {
	
	protected void setUp() {
		super.setUp()
		Account.collection.drop()
	}
	
	void createSequence() {
		Account.collection.save([seqId: 1, _t : Account.mongoTypeName + "SeqId"] as BasicDBObject)
	}
	
	def nextSeqId() {
		def seqObj = Account.collection.findOne([_t : Account.mongoTypeName + "SeqId"] as BasicDBObject)
		def id = seqObj.seqId
		def cmd = [findandmodify: Account.collection.name , query: [seqId : id] , update: [ '$inc' : [ seqId : 1]]] 
		def result = Account.collection.getDB().command(cmd as BasicDBObject)
		// result is { "value" : { "_id" : "4be951826ca83838eb159067" , "seqId" : 2.0} , "ok" : 1.0}
		if (result.ok == 1.0) {
			return id
		}
		return -1
	}

	void testFindAndModify() {
		createSequence()
		def id = nextSeqId()
		assertEquals 1, id
		id = nextSeqId()
		assertEquals 2, id
		id = nextSeqId()
		assertEquals 3, id
	}
	
}
