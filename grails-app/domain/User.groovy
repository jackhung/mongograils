import grails.plugin.mongo.MongoCollection
import grails.plugin.mongo.MongoTypeName

@MongoCollection("users")
@MongoTypeName("user")
class User {
	String username
	
	def beforeInsert() {
		dateCreated = new Date()
	}
	
	def beforeUpdate() {
		lastUpdated = new Date()
	}
	
	String toString() {
		"User: $username"
	}
}
