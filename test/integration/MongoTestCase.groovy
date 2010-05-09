import grails.test.*
import com.mongodb.*

class MongoTestCase extends GrailsUnitTestCase {

    def mongo
    //def collection
  
    protected void setUp() {
        super.setUp()
        try {
            //collection = mongo.collections."users"
            //collection.drop()
        } catch (MongoException e) {
            throw new MongoException('Could not connect to MongoDB - are you sure it is running?', e)
        }
    }
	
	public initUsers(opts = [:]) {
		if (opts.drop)
			User.collection.drop()
		def users = [:]
		users.william = new User(username: "William")
		users.pete = new User(username: "Pete")
		users.june = new User(username: "June")
		users.each {k, v -> v.mongoInsert()}
		
		users.william.father = users.pete.toMongoRef()
		users.william.mother = users.june.toMongoRef()
		users.william.mongoUpdate()
	}

}

