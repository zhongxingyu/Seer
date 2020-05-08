 package models.tm.approach;
 
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import controllers.ApproachTree;
 import controllers.ScriptCycleTreeDataHandler;
 import models.tm.ProjectModel;
import models.tree.jpa.TreeNode;
 import play.data.validation.MaxSize;
 import tree.persistent.Node;
 import tree.persistent.NodeName;
 
 /**
  * @author: Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"naturalId", "project_id"})})
 public class TestCycle extends ProjectModel implements Node {
 
     @NodeName
     public String name;
 
     @MaxSize(5000)
     public String description;
 
     public Date fromDate;
 
     public Date toDate;
 
     @Override
     public Object clone() throws CloneNotSupportedException {
         return super.clone();
     }
 
     public Release getRelease() {
        TreeNode n = TreeNode.find("from TreeNode n where n.nodeId = ? and n.treeId = ? and n.type = ?", getId(), ApproachTree.APPROACH_TREE, ScriptCycleTreeDataHandler.TEST_CYCLE).first();
         return Release.findById(n.getParent().getNodeId());
     }
 }
