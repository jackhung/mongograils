import org.springframework.beans.BeanWrapperImpl;
import grails.converters.JSON
import org.codehaus.groovy.grails.web.binding.DataBindingUtils;
import org.codehaus.groovy.grails.web.binding.GrailsDataBinder;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import grails.test.*;
import com.mongodb.*;

/**
 * @author jack
 *
 */
class MapReduceTests extends GrailsUnitTestCase {
		
	protected void setUp() {
		super.setUp()

		User.getCollection().drop()
		Project.getCollection().drop()
		
		def jack = new User(username: "Jack")
		def paul = new User(username: "Paul")
		def william = new User(username: "William")
		def proj = new Project(projectName: "testPrj1", leader: jack)
		proj.addToDevelopers(paul)
		proj.addToDevelopers(william)
		proj.mongoInsert()
		
		proj = new Project(projectName: "testPrj2", leader: paul)
		proj.addToDevelopers(jack)
		proj.addToDevelopers(william)
		proj.mongoInsert()
		
		proj = new Project(name: "testPrj3")
		proj.mongoInsert()
		
		proj = new Project(name: "testPrj4", leader: william)
		proj.mongoInsert()
		
	}
	
	
	def mapFunction = """
        function() {
                if (this.developers) {
                        this.developers.forEach(
                                function(d) {
                                        emit(d["username"], {count: 1});
                                }
                        );
                }
        }
                        """
	def reduceFunction = """
        function(key, vals) {
                var count = 0;
                for (var i = 0; i < vals.length; i++) {
                        count += vals[i].count;
                }
                return {count: count};
        }
                        """
	
	
	void testSimpleMapReduce() {
		/*
		 * Count the no. of projects developers belong to
		 * mapReduce( String map , String reduce , String outputCollection , DBObject query )
		 */
		def mrResult = Project.collection.mapReduce(
				mapFunction, reduceFunction, null, ["_t" : "project"] as BasicDBObject).results()
		assertEquals "Expect one entry per developer", 3, mrResult.count()
		def map = [:]
		mrResult.each { entry ->
			map."${entry._id}" = entry.value.count.intValue()
		}
		assertEquals 1, map["Jack"]
		assertEquals 1, map["Paul"]
		assertEquals 2, map["William"]
	}
	
}
