
import grails.test.*;
import com.mongodb.*;

/**
 * @author jack
 *
 */
class TreeModelTests extends MongoTestCase {
	def nodes = [:]
		
	protected void setUp() {
		super.setUp()
		TreeNode.collection.drop()
		initTestTree()
	}
	
	void initTestTree() {
		("A".."G").each { c ->
			nodes[c] = new TreeNode(nodeName: c).mongoInsert()
		}
		
// Had a lot of problem doing it the following way!!
//		TreeNode.mongoFindOne([nodeName: "A"]).toDomain().addDecendent(nodes["B"]).addDecendent(nodes["E"])
//		TreeNode.mongoFindOne([nodeName: "B"]).toDomain().addDecendent(nodes["C"]).addDecendent(nodes["D"])
//		TreeNode.mongoFindOne([nodeName: "E"]).toDomain().addDecendent(nodes["F"]).addDecendent(nodes["G"])
		
		addDecendent(TreeNode.mongoFindOne([nodeName: "A"]).toDomain(), nodes["B"])
		addDecendent(TreeNode.mongoFindOne([nodeName: "A"]).toDomain(), nodes["E"])
		
		addDecendent(TreeNode.mongoFindOne([nodeName: "B"]).toDomain(), nodes["C"])
		addDecendent(TreeNode.mongoFindOne([nodeName: "B"]).toDomain(), nodes["D"])

		addDecendent(TreeNode.mongoFindOne([nodeName: "E"]).toDomain(), nodes["F"])
		addDecendent(TreeNode.mongoFindOne([nodeName: "D"]).toDomain(), nodes["G"])
		
	}
	
	def addDecendent(parent, child) {
		child.mongoPerform {
			set "parent", parent._id
			set "ancestors", (parent.ancestors ?: []) << parent._id
		}
	}
	
	void testFindDecendentsOfB() {
		def decendents = TreeNode.mongoFind(ancestors: nodes["B"]._id)
		assertEquals 3, decendents.count()
	}
	
	void testFindAncestorOfF() {
		def ancestorIds = TreeNode.mongoFindOne([nodeName: "F"]).toDomain().ancestors
		def ancestors = TreeNode.mongoFind { where("_id").in(ancestorIds)}
		assertEquals 2, ancestors.count()
		def ancestorNames = ancestors.collect { it.nodeName }
		assertTrue ancestorNames.contains("A")
		assertTrue ancestorNames.contains("E")
	}
	
}
