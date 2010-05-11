
import grails.test.*;
import com.mongodb.*;

class MongoOperationsTests extends MongoTestCase {
	
	protected void setUp() {
		super.setUp()
		initUsers(drop: true)
		Account.collection.drop()
	}
	
	def deposit(acct, amount) {
		acct.mongoPerform {
			increment "balance", amount
			increment "txCount"
			push "txLog", [date: new Date(), amount: amount]
		}		
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
		
		// test multiple set(s) on same operation is correct
		def tmpAcct = Account.mongoFindOne(accountNumber: "OTEST001").toDomain()
		assertEquals 100.0, tmpAcct.balance
		assertEquals 0, tmpAcct.txCount
				
		deposit acct, 50.0
		tmpAcct = Account.mongoFindOne(accountNumber: "OTEST001").toDomain()
		assertEquals 1, tmpAcct.txCount
		assertEquals 150.0, tmpAcct.balance
		
		deposit acct, 25.0
		tmpAcct = Account.mongoFindOne(accountNumber: "OTEST001").toDomain()
		assertEquals 2, tmpAcct.txCount
		assertEquals 175.0, tmpAcct.balance
		assertEquals 2, tmpAcct.txLog.size()
				
		tmpAcct = Account.mongoFindOne(accountNumber: "OTEST002").toDomain()
		assertNull tmpAcct.balance
		assertNull tmpAcct.txCount
	}
	
	def createTestAccount(acctNo) {
		def acct = new Account(accountNumber : acctNo, accountCode: "OTST")
		acct.tags = ["NOSql", "MongoDB", "Grails", "Groovy"]
		acct.rating = 3
		acct.mongoInsert()
		acct		
	}
	
	void testUnSetOperation() {
		createTestAccount "UNSET001"
		
		def acct = Account.mongoFindOne(accountNumber: "UNSET001").toDomain()
		assertTrue acct.tags && acct.rating
		acct.mongoPerform {
			unset "tags"
			unset "rating"
		}
		
		acct = Account.mongoFindOne(accountNumber: "UNSET001").toDomain()
		assertFalse acct.tags || acct.rating
	}
	
	void testPullOperation() {
		createTestAccount "PULL001"
		
		def acct = Account.mongoFindOne(accountNumber: "PULL001").toDomain()
		assertTrue acct.tags.contains("Groovy")
		assertTrue acct.tags.contains("Groovy")
		assertTrue acct.tags.contains("Groovy")
		
		acct.mongoPerform {
			pull "tags", "Groovy"
		}
		acct = Account.mongoFindOne(accountNumber: "PULL001").toDomain()
		assertFalse acct.tags.contains("Groovy")
		
		acct.mongoPerform {
			pullAll "tags", ["Grails", "NOSql"]
		}
		acct = Account.mongoFindOne(accountNumber: "PULL001").toDomain()
		assertEquals 1, acct.tags.size()
				
	}
}
