 package com.sdc.util;
 
 import com.sdc.cfg.constructions.ConditionalBlock;
 import com.sdc.cfg.constructions.Construction;
 import com.sdc.cfg.constructions.ElementaryBlock;
 import com.sdc.cfg.nodes.Node;
 import com.sdc.cfg.nodes.Switch;
 //import com.sdc.cfg.nodes.While;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ConstructionBuilder {
     private List<Node> myNodes;
     private final int size;
     private final int[] domi;
     private final int[] post;
     private int[] intersection;
 
     public ConstructionBuilder(final List<Node> myNodes, final int[] domi, final int[] post) {
         this.myNodes = myNodes;
         this.domi = domi;
         this.post = post;
         if (domi != null && domi.length > 1) {
            this.size = domi.length;
             this.intersection = new int[domi.length];
             for (int i = 0; i < domi.length; i++) {
                 intersection[i] = domi[i] == post[i] ? domi[i] : -1;
             }
        } else {
            this.size = 1;
         }
     }
 
     public Construction build() {
         return build(myNodes.get(0));
     }
 
     private Construction build(Node node) {
         Construction elementaryBlock = extractElementaryBlock(node);
         Construction currentConstruction = extractConstruction(node);
 
         if (node.getCondition() == null) {
             node.setConstruction(elementaryBlock);
         } else {
             node.setConstruction(currentConstruction);
         }
 
         elementaryBlock.setNextConstruction(currentConstruction);
         return elementaryBlock;
     }
 
     private Construction extractConstruction(Node node) {
         Construction result;
 
         result = extractConditionBlock(node);
         if (result != null) {
             return result;
         }
 
         result = extractSwitch(node);
         if (result != null) {
             return result;
         }
 
         return null;
     }
 
     private Construction extractElementaryBlock(final Node node) {
         ElementaryBlock elementaryBlock = new ElementaryBlock();
         elementaryBlock.setStatements(node.getStatements());
         return elementaryBlock;
     }
 
     private Construction extractSwitch(Node node) {
         if (node instanceof Switch) {
             Switch switchNode = (Switch) node;
             com.sdc.cfg.constructions.Switch switchConstruction = new com.sdc.cfg.constructions.Switch(switchNode.getExpr());
 
             Node nextNode = findNextNodeToSwitchWithDefaultCase(switchNode);
 
             if (nextNode == null) {
                 List<Node> tails = switchNode.getListOfTails();
                 Node past = switchNode.getListOfTails().get(0);
                 for (int i = 1; i < tails.size(); i++) {
                     switchConstruction.addCase(switchNode.getKeys()[i - 1], new ConstructionBuilder(myNodes.subList(getRelativeIndex(past), getRelativeIndex(tails.get(i))), domi, post).build());
                     past = tails.get(i);
                 }
 
                 nextNode = findNextNodeToSwitchWithoutDefaultCase(switchNode);
             } else {
                 switchNode.addTail(nextNode);
 
                 List<Node> tails = switchNode.getListOfTails();
                 Node past = switchNode.getListOfTails().get(0);
                 Node defaultNode = switchNode.getNodeByKey(-1);
                 for (int i = 1; i < tails.size(); i++) {
                     if (tails.get(i).getIndex() == defaultNode.getIndex()) {
                         switchConstruction.addCase(switchNode.getKeys()[i - 1], new ConstructionBuilder(myNodes.subList(getRelativeIndex(past), getRelativeIndex(defaultNode)), domi, post).build());
                         i++;
                         switchConstruction.setDefaultCase(new ConstructionBuilder(myNodes.subList(getRelativeIndex(defaultNode), getRelativeIndex(tails.get(i))), domi, post).build());
                     } else {
                         Construction construction = new ConstructionBuilder(myNodes.subList(getRelativeIndex(past), getRelativeIndex(tails.get(i))), domi, post).build();
                         if (past.getIndex() == defaultNode.getIndex()) {
                             switchConstruction.setDefaultCase(construction);
                         } else {
                             switchConstruction.addCase(switchNode.getKeys()[i - 1], construction);
                         }
                     }
                     past = tails.get(i);
                 }
 
                 switchNode.removeChild(nextNode);
             }
 
             addBreakToAncestors(nextNode);
 
             if (getRelativeIndex(nextNode.getIndex()) < myNodes.size()) {
                 switchNode.setNextNode(nextNode);
                 extractNextConstruction(switchConstruction, switchNode);
             }
 
             return switchConstruction;
         }
         return null;
     }
 
     private Node findNextNodeToSwitchWithDefaultCase(Switch switchNode) {
         return findNextNode(switchNode);
     }
 
     private Node findNextNode(Node node) {
         Node result = null;
 
         for (int i = 0; i < domi.length; i++) {
             if (domi[i] == node.getIndex()) {
                 boolean isTail = false;
                 for (final Node tail : node.getListOfTails()) {
                     if (i == tail.getIndex()) {
                         isTail = true;
                         break;
                     }
                 }
                 if (!isTail) {
                     result = myNodes.get(getRelativeIndex(i));
                     break;
                 }
             }
         }
 
         return result;
     }
 
 
     private Node findNextNodeToSwitchWithoutDefaultCase(Switch switchNode) {
         Node defaultBranch = switchNode.getNodeByKey(-1);
 
         switchNode.removeChild(defaultBranch);
         defaultBranch.removeAncestor(switchNode);
 
         return defaultBranch;
     }
 
     private boolean hasDefaultCase(Switch switchNode) {
         final Node probableDefaultNode = switchNode.getNodeByKey(-1);
 
         final List<Node> cases = switchNode.getListOfTails();
         for (int i = 0; i < cases.size() - 1; i++) {
             if (!cases.get(i).equals(probableDefaultNode) && !hasLinkFromCase(probableDefaultNode, cases.get(i), cases.get(i + 1)))  {
                 return true;
             }
         }
 
         return false;
     }
 
     private boolean hasLinkFromCase(final Node node, final Node caseNode, final Node nextCaseNode) {
         for (final Node ancestor : node.getAncestors()) {
             final int ancestorIndex = ancestor.getIndex();
             if (ancestorIndex >= caseNode.getIndex() && ancestorIndex < nextCaseNode.getIndex()) {
                 return true;
             }
         }
         return false;
     }
 
     private Construction extractConditionBlock(Node node) {
         if (node.getCondition() != null) {
             boolean fl1 = true;
             for (Node ancestor : node.getAncestors()) {
                 if (myNodes.indexOf(node) < myNodes.indexOf(ancestor)) {
                     if (domi[myNodes.indexOf(node)] != domi[myNodes.indexOf(node.getListOfTails().get(1))]) {
                         node.setNextNode(node.getListOfTails().get(1));
                     }
                     fl1 = false;
                     com.sdc.cfg.constructions.While whileConstruction = new com.sdc.cfg.constructions.While(node.getCondition());
 //                    myNodes.set(myNodes.indexOf(node), new While(node));
                     break;
                 }
             }
 
 
             /// IF
             if (fl1 && node.getIndex() < node.getListOfTails().get(1).getIndex()) {
                 com.sdc.cfg.constructions.ConditionalBlock conditionalBlock = new ConditionalBlock(node.getCondition());
 
                 boolean fl = false;
                 for (Node ancestor : node.getAncestors()) {
                     if (ancestor.getIndex() > node.getIndex()) {
                         fl = true;
                         break;
                     }
                 }
 
                 if (!fl) {
                     Node nextNode = findNextNode(node);
                     node.setNextNode(nextNode);
 
                     Node leftNode = node.getListOfTails().get(0);
                     Node rightNode = node.getListOfTails().get(1);
                     int rightIndex = getRelativeIndex(rightNode);
 
                     if (node.getNextNode() == null) {
                         if (rightNode.getAncestors().size() > 1) {
                             if (rightNode.getIndex() <= myNodes.get(size - 1).getIndex()) {
                                 node.setNextNode(rightNode);
                             }
 //                            int rightIndex = getRelativeIndex(rightNode);
                             conditionalBlock.setThenBlock(new ConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex > size ? size : rightIndex), domi, post).build());
                         } else {
                             conditionalBlock.setThenBlock(new ConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex), domi, post).build());
                             conditionalBlock.setElseBlock(new ConstructionBuilder(myNodes.subList(getRelativeIndex(rightNode), size), domi, post).build());
                         }
                     } else {
                         conditionalBlock.setThenBlock(new ConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex), domi, post).build());
                         conditionalBlock.setElseBlock(new ConstructionBuilder(myNodes.subList(rightIndex, getRelativeIndex(node.getNextNode())), domi, post).build());
                     }
                 }
                 // TODO: test second condition for switch with if in a case
                 if (node.getNextNode() != null && getRelativeIndex(node.getNextNode()) < size) {
                     extractNextConstruction(conditionalBlock, node);
                 }
                 return conditionalBlock;
             }
         }
         return null;
     }
 
     private int getRelativeIndex(Node node) {
         return getRelativeIndex(node.getIndex());
     }
 
     private int getRelativeIndex(int index) {
         return index - myNodes.get(0).getIndex();
     }
 
     private void extractNextConstruction(Construction construction, final Node currentNode) {
         construction.setNextConstruction(build(currentNode.getNextNode()));
     }
 
     protected void addBreakToAncestors(final Node child) {
         for (final Node parent : child.getAncestors()) {
             if (parent.getConstruction() != null) {
                 parent.getConstruction().setBreak("");
             }
         }
     }
 }
