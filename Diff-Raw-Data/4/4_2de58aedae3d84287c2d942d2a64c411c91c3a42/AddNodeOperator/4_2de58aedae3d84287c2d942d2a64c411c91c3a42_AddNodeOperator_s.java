 package org.kevoree.genetic.library.operator;
 
 import org.kevoree.ContainerNode;
 import org.kevoree.ContainerRoot;
 import org.kevoree.TypeDefinition;
 import org.kevoree.modeling.api.KMFContainer;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 26/03/13
  * Time: 19:18
  */
 public class AddNodeOperator extends AbstractKevoreeOperator {
 
     private String nodeTypeName = null;
 
     public String getNodeTypeName() {
         return nodeTypeName;
     }
 
     public AddNodeOperator setNodeTypeName(String nodeTypeName) {
         this.nodeTypeName = nodeTypeName;
         this.setSelectorQuery("fakeQuery");
         return this;
     }
 
     @Override
    public ContainerRoot mutate(ContainerRoot parent) {
        return super.mutate(parent);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     protected void applyMutation(Object target, ContainerRoot root) {
 
         TypeDefinition nodeType = root.findTypeDefinitionsByID(nodeTypeName);
         if (nodeType == null) {
             System.out.println("TypeDefinition " + nodeTypeName + " , not found : bad configuration ?");
             return;
         }
         ContainerNode node = factory.createContainerNode();
         node.setName(generateName());
         node.setTypeDefinition(nodeType);
         root.addNodes(node);
         /* Set sucessor directly on this node */
         if (getSuccessor() != null) {
             ((AbstractKevoreeOperator) getSuccessor()).setSelectorQuery(node.path());
         }
     }
 
     protected String generateName() {
         Random r = new Random();
         return nodeTypeName + "_" + Math.abs(r.nextInt());
     }
 
     @Override
     protected List<Object> selectTarget(KMFContainer root, String query) {
         return Collections.singletonList((Object) ((ContainerRoot) root).getTypeDefinitions().get(0));
     }
 }
