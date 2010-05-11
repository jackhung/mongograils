
import grails.test.*;
import com.mongodb.*;

class MongoOperationsTests extends MongoTestCase {
	
	protected void setUp() {
		super.setUp()
		initUsers(drop: true)
		Account.collection.drop()
	}
	
	/**
	 * User.operate(filter, operation, ..
	 * user.operate(
	 */
	void testBasicOperation() {
		def acct = new Account(accountNumber : "OTEST001", accountCode: "OTST")
		acct.mongoInsert()
		def acct2 = new Account(accountNumber : "OTEST002", accountCode: "OTST")
		acct2.mongoInsert()
		
		acct.mongoPerform {
			set "balance", 100.0
			set "txCount", 0
		}
		acct.mongoPerform {
			increment "balance", 50.0
			increment "txCount"
			push "txLog", [date: new Date(), amount: 50.0]
		}
		def tmpAcct = Account.mongoFindOne(accountNumber: "OTEST001").toDomain()
		assertEquals 1, tmpAcct.txCount
		assertEquals 150.0, tmpAcct.balance
				
		acct.mongoPerform {
			increment "balance", 25.0
			increment "txCount"
			push "txLog", [date: new Date(), amount: 25.0]
		}
		tmpAcct = Account.mongoFindOne(accountNumber: "OTEST001").toDomain()
		assertEquals 2, tmpAcct.txCount
		assertEquals 175.0, tmpAcct.balance
				
		tmpAcct = Account.mongoFindOne(accountNumber: "OTEST002").toDomain()
		assertNull tmpAcct.balance
		assertNull tmpAcct.txCount
	}
	
}
