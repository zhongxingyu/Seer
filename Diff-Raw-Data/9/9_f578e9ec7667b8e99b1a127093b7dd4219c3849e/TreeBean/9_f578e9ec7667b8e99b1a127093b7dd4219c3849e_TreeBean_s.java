 package mybeans.mydb.compare;
 
 import java.util.ArrayList;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import org.primefaces.model.TreeNode;  
 import org.primefaces.model.DefaultTreeNode; 
 
 @ManagedBean(name = "treeDisp")
 @SessionScoped
 
 public class TreeBean {
 	
 	private TreeNode root;
 
 	
 	public TreeBean(String name, Component treeData) {
 		//root = new DefaultTreeNode("Original File", null);
 		
 		root = new DefaultTreeNode("Root", null);  
 		TreeNode level = new DefaultTreeNode(name, root);
 		level.setExpanded(false);
 		generateTreeNode((DefaultTreeNode) level, treeData);
 	}
 	
 	public TreeNode generateTreeNode(DefaultTreeNode top, Component treeData) {
 		
 		ArrayList<DefaultTreeNode> node = new ArrayList<DefaultTreeNode>();
 		
 		int i;
		
 		if(treeData.list.size() > 0) {
 			for(i = 0; i < treeData.list.size(); i++) {
				TreeNode temp = new DefaultTreeNode(treeData.list.get(i).getType(), top);
				temp.setExpanded(false);
 				node.add((DefaultTreeNode) temp);
 				generateTreeNode(node.get(i), treeData.list.get(i));
 			}
 		}
 		
 //		if(treeData.list.size() > 0) {
 //			for(i=0;i<treeData.list.size();i++) {
 //				TreeNode temp = new DefaultTreeNode(treeData.list.get(i).getType(), top);
 //			}
 //		}
 				
 		return top;
 	}
 	
 
 	public TreeNode getRoot() {
 		return root;
 	}
 
 }
