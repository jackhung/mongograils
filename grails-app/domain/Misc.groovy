import grails.plugin.mongo.MongoCollection
import grails.plugin.mongo.MongoTypeName

@MongoCollection("miscs")
@MongoTypeName("misc")
class Misc {
	String toString() {
		"Misc: ..."
	}
}
