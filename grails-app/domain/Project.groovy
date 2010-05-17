import grails.plugin.mongo.MongoCollection
import grails.plugin.mongo.MongoTypeName

@MongoCollection("projects")
@MongoTypeName("project")
class Project {
	String projectName
	
	User leader
	List<User> developers
	
	static hasMany = [developers : User]
	
	String toString() {
		"Project: $projectName"
	}
}
