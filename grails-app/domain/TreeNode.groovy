import grails.plugin.mongo.MongoCollection
import grails.plugin.mongo.MongoTypeName

@MongoCollection("treenodes")
@MongoTypeName("treenode")
class TreeNode {
	String nodeName
	
// Don't seem to work
//	def addDecendent(child) {
//		child.mongoPerform {
//			set "parent", nodeName
//			set "ancestors", (ancestors ?: []) << nodeName
//		}
//		this
//	}
	
	String toString() {
		"TreeNode: $nodeName"
	}
}
