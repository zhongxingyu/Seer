 package models.tm.approach;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import controllers.ApproachTree;
 import controllers.Lookups;
 import controllers.ScriptCycleTreeDataHandler;
 import models.tm.Project;
 import models.tm.ProjectModel;
 import models.tm.ProjectTreeNode;
 import models.tree.jpa.TreeNode;
 import tree.JSTreeNode;
 import tree.persistent.Node;
 import tree.persistent.NodeName;
 
 /**
  * @author: Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @Entity
@Table(name = "tm_approach_Release", uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"naturalId", "project_id"})})
 public class Release extends ProjectModel implements Node {
 
     @NodeName
     public String name;
 
     public Release(Project project) {
         super(project);
     }
 
     @Override
     public Object clone() throws CloneNotSupportedException {
         // TODO
         return super.clone();    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     public List<TestCycle> getTestCycles() {
         ProjectTreeNode n = TreeNode.find("from ProjectTreeNode n where n.nodeId = ? and n.treeId = ? and n.type = ?", getId(), ApproachTree.APPROACH_TREE, ScriptCycleTreeDataHandler.RELEASE).first();
         List<JSTreeNode> children = n.getChildren();
         List<TestCycle> cycles = new ArrayList<TestCycle>();
         for (JSTreeNode child : children) {
             cycles.add(Lookups.getCycle(((ProjectTreeNode) child).nodeId));
         }
         return cycles;
     }
 }
