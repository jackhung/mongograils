import grails.plugin.mongo.MongoCollection
import grails.plugin.mongo.MongoTypeName

@MongoCollection("users")
@MongoTypeName("user")
class User {
	String username

	String toString() {
		"User: $username"
	}
}
