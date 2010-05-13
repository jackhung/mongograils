
import grails.test.*;
import com.mongodb.*;

/**
 * @author jack
 *
 */
class TreeModelTests extends GrailsUnitTestCase {
	def nodes = [:]
		
	protected void setUp() {
		super.setUp()
		TreeNode.collection.drop()
		("A".."G").each { c ->
			nodes[c] = new TreeNode(nodeName: c).mongoInsert()
		}
		
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
		def decendentNames = decendents.collect { it.nodeName }
		assertTrue decendentNames.contains("C")
		assertTrue decendentNames.contains("D")
		assertTrue decendentNames.contains("G")
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
