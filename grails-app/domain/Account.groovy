import grails.plugin.mongo.MongoCollection
import grails.plugin.mongo.MongoTypeName

@MongoCollection("accounts")
@MongoTypeName("acct")
class Account {
	String accountNumber
	String accountCode

	String toString() {
		"Acct: $accountCode:$accountNumber"
	}
}
