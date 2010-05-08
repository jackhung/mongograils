import grails.test.*
import com.mongodb.*

class MongoTestCase extends GrailsUnitTestCase {

    def mongo
    def collection
  
    protected void setUp() {
        super.setUp()
        try {
            collection = mongo.collections."users"
            //collection.drop()
        } catch (MongoException e) {
            throw new MongoException('Could not connect to MongoDB - are you sure it is running?', e)
        }
    }

}

