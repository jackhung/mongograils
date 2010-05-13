import grails.test.*
import com.mongodb.*

/**
 * @author jack
 *
 */
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
		users.william.info = [weight: 140, height: 6.2, birthYear: 1990]
		users.pete = new User(username: "Pete")
		users.pete.info = [weight: 160, height: 5.8, birthYear: 1960 ]
		users.june = new User(username: "June")
		users.june.info = [weight: 120, height: 5.6, birthYear: 1962]
		users.each {k, v -> v.mongoInsert()}
		
		users.william.mongoPerform { 
			set "father", users.pete.toMongoRef() 
			set "mother", users.june.toMongoRef()
		}
//		users.william.mongoUpdate()
	}

}

