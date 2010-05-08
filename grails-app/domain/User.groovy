import grails.plugin.mongo.MongoCollection

@MongoCollection("users")
class User {
	String username

	String toString() {
		"User: $username"
	}
}
