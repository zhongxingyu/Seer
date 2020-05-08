 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.shared.FxXMLUtils;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.configuration.parameters.ObjectParameter;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.search.query.*;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.value.FxBoolean;
 import com.flexive.shared.value.FxNumber;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import com.flexive.shared.value.mapper.InputMapper;
 import org.apache.commons.lang.StringUtils;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Search query tree tests (GUI query editor).
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class QueryNodeTreeTests {
     private static FxPropertyAssignment getTestAssignment() {
         return (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment("root/id");
     }
 
     /**
      * A primitive test node class.
      */
     public static class InnerTestNode extends QueryValueNode<FxValue, PropertyValueComparator> {
         private static final long serialVersionUID = -7628311116595464298L;
 
         public InnerTestNode() {
         }
 
         public InnerTestNode(int id) {
             this.id = id;
         }
 
         public boolean isValid() {
             return true;
         }
 
         public void buildSqlQuery(SqlQueryBuilder builder) {
         }
 
         public List<PropertyValueComparator> getNodeComparators() {
             return Arrays.asList(PropertyValueComparator.values());
         }
     }
 
     /**
      * Query node generator interface (used to customize tree-building
      * routines).
      */
     public static interface QueryNodeGenerator<TNode extends QueryNode> {
         TNode createNode(int nodeId);
     }
 
     /**
      * Internal node ID counter
      */
     private static int nodeId;
 
     @Test(groups = {"shared"})
     public void testNodeEquals() {
         assert new InnerTestNode().equals(new InnerTestNode()) : "Two new node instances should be equal.";
         //noinspection ObjectEqualsNull
         assert !new InnerTestNode().equals(null) : "A test node is not equal to null.";
         assert new InnerTestNode(10).equals(new InnerTestNode(10)) : "Node IDs not checked properly";
         assert !new InnerTestNode(10).equals(new InnerTestNode(9)) : "Nodes with different IDs should not be equal.";
     }
 
     /**
      * Checks if the given node is stored correctly in an XML string (including
      * its children) and throws a runtime exception if an error occured.
      *
      * @param root the root node of the tree to be checked
      */
     @Test(groups = {"shared"}, dataProvider = "basicQueries")
     public void checkTreeStore(QueryNode root) {
         try {
             QueryNode loadedRoot = (QueryNode) ObjectParameter.getDefaultXStream().fromXML(FxXMLUtils.toXML(ObjectParameter.getDefaultXStream(), root));
             assertEqualTrees(root, loadedRoot);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Checks the tree validation with miscellaneous tree input.
      */
     @Test(groups = {"shared"})
     public void checkTreeValidity() {
         assert buildFlatTree(
                 new AssignmentNodeGenerator(FxDataType.String1024, new FxString("Test")), 5)
                 .isValid() : "Simple string tree should be valid";
         StringBuilder bigString = new StringBuilder();
         for (int i = 0; i < 1000; i++) {
             bigString.append("1234567890");
         }
         assert bigString.length() == 10000 : "String length to be expected 10000";
         assert !buildFlatTree(
                 new AssignmentNodeGenerator(FxDataType.String1024, new FxString(bigString
                         .substring(0, 1100))), 5).isValid() : "Tree with 1100 character strings shouldn't be valid";
 //		assert !buildNestedTree(
 //				25,
 //				new AssignmentNodeGenerator(FxDataType.String1024, bigString
 //						.substring(0, 256))).isValid() : "256 character string should'nt be valid";
         assert buildNestedTree(
                 25,
                 new AssignmentNodeGenerator(FxDataType.String1024, new FxString(bigString
                         .substring(0, 255))), 5).isValid() : "255 character string should be valid";
         assert !buildFlatTree(
                 new AssignmentNodeGenerator(FxDataType.String1024, null), 5)
                 .isValid() : "Null string tree should'nt be valid.";
 
         // TODO check other data types
     }
 
     /**
      * Checks the tree node removal
      */
     @Test(groups = {"ejb", "search"})
     public void treeNodeRemoval() {
         QueryNode flatTree = buildFlatTree(new InnerNodeGenerator(), 5);
         int children = flatTree.getChildren().size();
         // remove first child
         QueryNode child = flatTree.getChildren().get(0);
         assert flatTree.removeChild(child).equals(flatTree) : "Incorrent parent node returned";
         assert flatTree.getChildren().indexOf(child) == -1 : "Child not removed";
         assert flatTree.getChildren().size() == children - 1 : "Incorrect number of children";
 
         // remove last child
         child = flatTree.getChildren().get(flatTree.getChildren().size() - 1);
         assert flatTree.removeChild(child).equals(flatTree) : "Incorrent parent node returned";
         assert flatTree.getChildren().indexOf(child) == -1 : "Child not removed";
         assert flatTree.getChildren().size() == children - 2 : "Incorrent number of children";
 
         while (flatTree.getChildren().size() > 0) {
             assert flatTree.removeChild(flatTree.getChildren().get(0)).equals(
                     flatTree) : "Incorrent parent node";
         }
 
         // test nested tree
         QueryNode nestedTree = buildNestedTree(5, new InnerNodeGenerator(), 5);
         for (QueryNode node : nestedTree.getChildren()) {
             if (node instanceof QueryOperatorNode) {
                 assert nestedTree.removeChild(node.getChildren().get(0))
                         .equals(node) : "Returned incorrent parent node in nested tree.";
             }
         }
 
         // test automatic removal of empty inner operator nodes
         QueryNode innerTree = buildNestedTree(5, new InnerNodeGenerator(), 5);
         QueryNode emptyNode = innerTree.getChild(innerTree.getChildren().size() - 1);
         assert emptyNode instanceof QueryOperatorNode : "Unexpected node type: " + emptyNode.getClass();
         assert innerTree.getChildren().indexOf(emptyNode) != -1 : "Test node not found";
         while (emptyNode.getChildren().size() > 0) {
             QueryNode node = emptyNode.getChild(0);
             emptyNode.removeChild(node);
         }
         assert innerTree.getChildren().indexOf(emptyNode) == -1 : "Empty test node not removed.";
     }
 
     @Test(groups = {"ejb", "search"})
     public void emptyOuterNodeRemoval() {
         // test automatic removal of empty outer operator nodes
         // (i.e. operator nodes that contain only a single operator node)
         QueryRootNode outerTree = buildNestedTree(5, new InnerNodeGenerator(), 5);
         QueryNode newRootNode = outerTree.getChild(outerTree.getChildren().size() - 1);
         assert newRootNode instanceof QueryOperatorNode : "Unexpected node type: " + newRootNode.getClass();
         for (int i = 0; i < 5; i++) {
             outerTree.removeChild(outerTree.getChild(0));
         }
         // now the children of newRootNode should be attached to the root node
         assert outerTree.getChildren().size() == 6 : "Unexpected number of children: " + outerTree.getChildren().size() + ",\n Tree=" + outerTree.toString();
     }
 
     @Test(groups = {"ejb", "search"})
     public void singleChildRemoval() {
         // test automatic removal of inner operator nodes with only one child
         QueryRootNode nestedTree2 = buildNestedTree(1, new InnerNodeGenerator(), 5);
         QueryNode operatorNode1 = nestedTree2.getChild(nestedTree2.getChildren().size() - 1);
         for (int i = 0; i < 3; i++) {
             operatorNode1.removeChild(operatorNode1.getChild(0));
         }
         assert operatorNode1.getChildren().size() == 2 : "Expected 2 children, got: " + operatorNode1.getChildren().size();
         assert nestedTree2.getChildren().size() == 6 : "Expected 6 children, got: " + nestedTree2.getChildren().size();
         QueryNode lastChild = operatorNode1.getChild(1);
         operatorNode1.removeChild(operatorNode1.getChild(0));
         assert lastChild.equals(nestedTree2.getChildren().get(nestedTree2.getChildren().size() - 1)) : "Tree not compacted properly: " + nestedTree2.toString();
     }
 
     /**
      * Checks searching inside query trees
      *
      * @throws FxNotFoundException on errors
      */
     @Test(groups = {"ejb", "search"})
     public void treeNodeFind() throws FxNotFoundException {
         QueryNode nestedTree = buildNestedTree(5, new InnerNodeGenerator(), 5);
         // check basic queries
         assert nestedTree.findChild(nestedTree.getChild(0).getId()) != null;
         assert nestedTree.findChild(nestedTree.getChild(nestedTree.getChildren().size() - 1).getId()) != null;
         assert nestedTree.findChild(nestedTree.getId()) != null;
 
         // check nested find queries
         QueryNode innerNode = nestedTree.getChild(nestedTree.getChildren().size() - 1);
         assert nestedTree.findChild(innerNode.getChild(0).getId()) != null;
 
         // check exception
         try {
             nestedTree.findChild(-1);
             assert false : "Invalid node found!";
         } catch (Exception e) {
             // pass
         }
         try {
             nestedTree.getChild(0).findChild(nestedTree.getChild(0).getId());
             assert false : "Searching in leaf nodes should not be allowed";
         } catch (Exception e) {
             // pass
         }
 
     }
 
     @Test(groups = {"ejb", "search"})
     public void simpleNodeVisitor() {
         CountingNodeVisitor visitor = new CountingNodeVisitor();
         buildFlatTree(new InnerNodeGenerator(), 25).visit(visitor);
         assert visitor.getOpNodes() == 1 : "Unexpected number of operator nodes: " + visitor.getOpNodes();
         assert visitor.getValueNodes() == 25 : "Unexpected number of value nodes: " + visitor.getValueNodes();
 
         visitor = new CountingNodeVisitor();
         buildNestedTree(50, new InnerNodeGenerator(), 10).visit(visitor);
         assert visitor.getOpNodes() == 51 : "Unexpected number of operator nodes: " + visitor.getOpNodes();
         assert visitor.getValueNodes() == 51 * 10 : "Unexpected number of value nodes: " + visitor.getValueNodes();
     }
 
     @Test(groups = {"ejb", "search"})
     public void maxIdVisitor() {
         MaxNodeIdVisitor maxIdVisitor = new MaxNodeIdVisitor();
         nodeId = 0;
         buildFlatTree(new InnerNodeGenerator(), 25).visit(maxIdVisitor);
         assert maxIdVisitor.getMaxId() == 25 : "Unexpected max ID: " + maxIdVisitor.getMaxId();
 
         maxIdVisitor = new MaxNodeIdVisitor();
         nodeId = 0;
         buildNestedTree(10, new InnerNodeGenerator(), 10).visit(maxIdVisitor);
         assert maxIdVisitor.getMaxId() == 11 * 11 - 1 : "Unexpected max ID: " + maxIdVisitor.getMaxId();
     }
 
     /**
      * Check if multiple calls to {@link com.flexive.shared.search.query.QueryOperatorNode#getSqlQuery()}
      * yield the same result.
      */
     @Test(groups = {"ejb", "search"})
     public void repeatableSqlQueryTest() {
         final QueryRootNode root = buildNestedTree(5, new AssignmentNodeGenerator(FxDataType.String1024, new FxString("test value")), 5);
         final String query = root.getSqlQuery();
         final String query2 = root.getSqlQuery();
         assert query.equals(query2) : "Second call to getSqlQuery() returned different result than first one : "
                 + "\n[1]: " + query + "\n[2]: " + query2;
     }
 
     /**
      * Check if multiple calls to {@link com.flexive.shared.search.query.QueryOperatorNode#getSqlQuery()}
      * yield the same result, this time with an external query builder.
      */
     @Test(groups = {"ejb", "search"})
     public void repeatableSqlQueryTest2() {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         final QueryRootNode root = buildNestedTree(5, new AssignmentNodeGenerator(FxDataType.String1024, new FxString("test value")), 5);
         root.buildSqlQuery(builder);
         final String query = builder.getQuery();
         root.buildSqlQuery(builder);
         final String query2 = builder.getQuery();
         assert StringUtils.isNotBlank(query) && query.equals(query2)
                 : "Second call to getQuery() returned different result than first one : "
                 + "\n[1]: " + query + "\n[2]: " + query2;
     }
 
     /**
      * Checks against a bug where an empty tree leads to the generation
      * of "WHERE ()" in the SQL query.
      */
     @Test(groups = {"ejb", "search"})
     public void emptyQueryTest() {
         final SqlQueryBuilder builder = new SqlQueryBuilder();
         final QueryRootNode root = new QueryRootNode(QueryRootNode.Type.CONTENTSEARCH);
         root.buildSqlQuery(builder);
         assert !builder.getQuery().contains("FROM WHERE") : "Query contains invalid FROM clause: " + builder.getQuery();
         assert !builder.getQuery().contains("WHERE ()") : "Query contains invalid WHERE clause: " + builder.getQuery();
     }
 
 
     /**
      * Testcase for a bug that duplicates tree nodes
      * for a certain order of commands (cause: invalid parent nodes).
      */
     @Test(groups = {"ejb", "search"})
     public void treeDeleteCloneBug() {
         QueryRootNode root = new QueryRootNode(QueryRootNode.Type.CONTENTSEARCH);
         root.addChild(new InnerTestNode(nodeId++));
         root.addChild(new InnerTestNode(nodeId++));
         combineNodes(root, root.getChild(1), new InnerTestNode(nodeId++));
         root.getChild(1).addChild(new InnerTestNode(nodeId++));
         combineNodes(root.getChild(1), root.getChild(1).getChild(1), new InnerTestNode(nodeId++));
 
         removeFromParent(root.getChild(1).getChild(1));
         removeFromParent(root.getChild(1).getChild(0));
         removeFromParent(root.getChild(1).getChild(0));
         assert root.getChildren().size() == 2 : "Expected 2 children, got: " + root.getChildren().size();
     }
 
     /**
      * Simple tests for the tree node level method.
      */
     @Test(groups = {"ejb", "search"})
     public static void nodeLevelTest() {
         QueryRootNode root = buildNestedTree(5, new InnerNodeGenerator(), 1);
         assert root.getLevel() == 0 : "Root level should be 0, got: " + root.getLevel();
         assert root.getChild(0).getLevel() == 1 : "First child level should be 1, got: " + root.getChild(0).getLevel();
         assert root.getChild(1).getLevel() == 1;
         assert root.getChild(1).getChild(0).getLevel() == 2;
         assert root.getChild(1).getChild(1).getLevel() == 2;
         assert root.getChild(1).getChild(1).getChild(0).getLevel() == 3;
     }
 
     @Test(groups = {"ejb", "search"})
     public void joinNodesFlat() {
         QueryRootNode root = buildNestedTree(0, new InnerNodeGenerator(), 3);
         QueryOperatorNode operatorNode = root.joinNodes(Arrays.asList(root.getChild(0).getId(), root.getChild(1).getId()), QueryOperatorNode.Operator.OR);
         assert operatorNode.getChildren().size() == 2 : "New operator node must have 2 children, got: " + operatorNode.getChildren().size();
         assert operatorNode.getParent() == root : "Operator node should be attached to root.";
         assert root.getChildren().size() == 2 : "Root node should have two children, got: " + root.getChildren().size();
     }
 
     @Test(groups = {"ejb", "search"})
     public void joinNodesNested() {
         QueryRootNode root = buildNestedTree(1, new InnerNodeGenerator(), 4);
         assert root.getChildren().size() == 5 : "Expected 5 root children, got: " + root.getChildren().size();
         assert root.getChild(4).getChildren().size() == 4 : "Expected 4 children, got: " + root.getChild(4).getChildren().size();
         QueryOperatorNode operatorNode = root.joinNodes(Arrays.asList(root.getChild(4).getChild(0).getId(), root.getChild(4).getChild(2).getId()), QueryOperatorNode.Operator.OR);
         assert root.getChildren().size() == 5 : "Expected 5 root children, got: " + root.getChildren().size();
         assert root.getChild(4).getChildren().size() == 3 : "Expected 2 children, got: " + root.getChild(4).getChildren().size();
         assert operatorNode.getChildren().size() == 2;
         assert operatorNode.getParent() == root.getChild(4);
     }
 
     @Test(groups = {"ejb", "search"})
     public void joinNodesNestedCompact() {
         QueryRootNode root = buildNestedTree(1, new InnerNodeGenerator(), 3);
         assert root.getChildren().size() == 4 : "Expected 4 root children, got: " + root.getChildren().size();
         assert root.getChild(3).getChildren().size() == 3 : "Expected 3 children, got: " + root.getChild(3).getChildren().size();
         // join 2 of the 3 childs in root child #3, then the remaining node should be reattached to the root node itself
         QueryNode compactedChild = root.getChild(3).getChild(2);    // this node should be reattached to root automatically
         assert compactedChild.getParent() != root;
         final QueryNode joinNode = root.getChild(3);
         QueryOperatorNode operatorNode = root.joinNodes(Arrays.asList(joinNode.getChild(0).getId
                 (), joinNode.getChild(1).getId(), joinNode.getParent().getChild(0).getId()), QueryOperatorNode.Operator.OR);
         assert compactedChild.getParent() == root : "Single child should have been reattached to root";
         assert root.getChildren().size() == 4 : "Expected 4 root children, got: " + root.getChildren().size();
         assert operatorNode.getChildren().size() == 3;
         assert operatorNode.getParent() == root;
     }
 
     @Test(groups = {"ejb", "search"})
     public void joinNodesNested2() {
         QueryRootNode root = buildNestedTree(2, new InnerNodeGenerator(), 3);
         assert root.getChildren().size() == 4;
         assert root.getChild(3).getChildren().size() == 4;
         assert root.getChild(3).getChild(3).getChildren().size() == 3;
         // combine two nodes in the third level - should attach as a child to the third level root, not the second level one
         final QueryNode joinRoot = root.getChild(3).getChild(3);
         final QueryOperatorNode operatorNode = root.joinNodes(Arrays.asList(joinRoot.getChild(0).getId(), joinRoot.getChild(1).getId()), QueryOperatorNode.Operator.OR);
         assert joinRoot.getChildren().size() == 2 : "Joined node should attach to previous parent";
         assert joinRoot.getParent().getChildren().size() == 4;
     }
 
     @Test(groups = {"ejb", "search"})
     public void joinNodesLevelAll() {
         QueryRootNode root = buildNestedTree(0, new InnerNodeGenerator(), 3);
         assert root.getChildren().size() == 3;
         QueryOperatorNode operatorNode = root.joinNodes(Arrays.asList(root.getChild(0).getId(),
                 root.getChild(1).getId(), root.getChild(2).getId()), QueryOperatorNode.Operator.OR);
         assert operatorNode == root : "Nodes should have been attached to the root node.";
         assert operatorNode.getChildren().size() == 3 : "All 3 nodes should be attached to the operator node";
         assert operatorNode.getOperator().equals(QueryOperatorNode.Operator.OR);
     }
 
     @Test(groups = {"ejb", "search"})
     public void joinNodesFromDistinctGroups() {
         QueryRootNode root = buildNestedTree(0, new InnerNodeGenerator(), 4);
         // create two subqueries
         final QueryNode child0_0 = root.getChild(0);
         final QueryNode child0_1 = root.getChild(1);
         root.joinNodes(Arrays.asList(child0_0.getId(), child0_1.getId()), QueryOperatorNode.Operator.OR);
         final QueryNode child1_0 = root.getChild(0);
         final QueryNode child1_1 = root.getChild(1);
         root.joinNodes(Arrays.asList(child1_0.getId(), child1_1.getId()), QueryOperatorNode.Operator.OR);
         assert root.getChildren().size() == 2;
         final QueryNode sub0 = root.getChild(0);    // subquery 0
         final QueryNode sub1 = root.getChild(1);    // subquery 1
         assert sub0.getChildren().containsAll(Arrays.asList(child0_0, child0_1));
         assert sub1.getChildren().containsAll(Arrays.asList(child1_0, child1_1));
 
         // join second node from sub0 with first node from sub1
         QueryOperatorNode joinRoot = root.joinNodes(Arrays.asList(child0_1.getId(), child1_0.getId()), QueryOperatorNode.Operator.AND);
         // expected result: [child0_0, child1_1, AND[child_0_1, child1_0]]
         assert root.getChildren().size() == 3 : "Root should have three children now.";
         assert joinRoot.getParent() == root : "Join root should have been attached to root";
         assert root.getChild(1).isValueNode() : "Node 1 should be value node";
         assert root.getChild(2).isValueNode() : "Node 2 should be value node";
         assert !root.getChild(0).isValueNode() : "Node 0 should not be value node";
     }
 
     // tests against a bug where a joined group is duplicated in itself
     // (caused by aliasing issues when moving the tree nodes in the same subtree)
     @Test(groups = {"ejb", "search"})
     public void joinNodesFromDistinctGroupsCopyBug() {
         QueryRootNode root = buildNestedTree(0, new InnerNodeGenerator(), 4);
         // create two subqueries
         final QueryNode child0_0 = root.getChild(0);
         final QueryNode child0_1 = root.getChild(1);
         root.joinNodes(Arrays.asList(child0_0.getId(), child0_1.getId()), QueryOperatorNode.Operator.OR);
         final QueryNode child1_0 = root.getChild(0);
         final QueryNode child1_1 = root.getChild(1);
         root.joinNodes(Arrays.asList(child1_0.getId(), child1_1.getId()), QueryOperatorNode.Operator.OR);
         assert root.getChildren().size() == 2;
         final QueryNode sub0 = root.getChild(0);    // subquery 0
         final QueryNode sub1 = root.getChild(1);    // subquery 1
         assert sub0.getChildren().containsAll(Arrays.asList(child0_0, child0_1));
         assert sub1.getChildren().containsAll(Arrays.asList(child1_0, child1_1));
 
         // join both nodes from sub1 again
         QueryOperatorNode joinRoot = root.joinNodes(Arrays.asList(child1_0.getId(), child1_1.getId()), QueryOperatorNode.Operator.AND);
         assert joinRoot.getParent() == root : "Joined nodes should have been attached to the root node";
     }
 
     @Test(groups = {"ejb", "search"})
     public void treeNodeTest() {
         QueryRootNode root = new QueryRootNode(0, QueryRootNode.Type.CONTENTSEARCH);
         final TreeValueNode child1 = new TreeValueNode(root.getNewId(), 1, FxTreeMode.Edit, new FxString(false, "node 1"));
         child1.setComparator(TreeValueNode.TreeValueComparator.DIRECTCHILD);
         root.addChild(child1);
         final TreeValueNode child2 = new TreeValueNode(root.getNewId(), 2, FxTreeMode.Edit, new FxString(false, "node 2"));
         child2.setComparator(TreeValueNode.TreeValueComparator.CHILD);
         root.addChild(child2);
         assert root.isValid();
         assert child1.getAvailableComparators().containsAll(Arrays.asList(TreeValueNode.TreeValueComparator.values()));
         assert root.getSqlQuery().contains("IS DIRECT CHILD OF 1 AND IS CHILD OF 2");
     }
 
     @Test(groups = {"ejb", "search"})
     public void selectListNodeTest() {
         FxSelectList list = new FxSelectList("test");
         final FxSelectListItem item1 = new FxSelectListItem(1, list, -1, new FxString("item 1"));
         final FxSelectListItem item2 = new FxSelectListItem(2, list, -1, new FxString("item 2"));
         QueryRootNode root = new QueryRootNode(0, QueryRootNode.Type.CONTENTSEARCH);
         final FxPropertyAssignment assignment = getTestAssignment();
         final SelectValueNode child1 = new SelectValueNode(root.getNewId(), assignment, item1);
         child1.setComparator(PropertyValueComparator.EQ);
         root.addChild(child1);
         final SelectValueNode child2 = new SelectValueNode(root.getNewId(), assignment, item2);
         child2.setComparator(PropertyValueComparator.NE);
         root.addChild(child2);
         assert root.isValid();
         assert root.getSqlQuery().contains("co.#" + assignment.getId() + " = " + item1.getId());
         assert root.getSqlQuery().contains("co.#" + assignment.getId() + " != " + item2.getId());
     }
 
     @Test(groups = {"ejb", "search"})
     public void propertyValueNodeTest() {
         QueryRootNode root = new QueryRootNode(0, QueryRootNode.Type.CONTENTSEARCH);
         final FxProperty property = getTestAssignment().getProperty();
         final PropertyValueNode child1 = new PropertyValueNode(root.getNewId(), property.getId());
         child1.setComparator(PropertyValueComparator.EQ);
         child1.setValue(new FxNumber(1));
         root.addChild(child1);
         final PropertyValueNode child2 = new PropertyValueNode(root.getNewId(), property.getId());
         child2.setComparator(PropertyValueComparator.NE);
         child2.setValue(new FxNumber(2));
         root.addChild(child2);
         assert root.isValid();
         final String conditions = "co." + property.getName() + " = 1 AND co." + property.getName() + " != 2";
         assert root.getSqlQuery().contains(conditions) : "Expected conditions: " + conditions + ", got: " + root.getSqlQuery();
     }
 
     @Test(groups = {"ejb", "search"})
     public void inputMapperComparatorTest() {
         final List<PropertyValueComparator> allowedComparators = Arrays.asList(PropertyValueComparator.EMPTY, PropertyValueComparator.EQ);
         final InnerTestNode node = new InnerTestNode(1);
         assert node.getAvailableComparators().equals(Arrays.asList(PropertyValueComparator.values()));
         node.setInputMapper(new InputMapper<FxString, FxBoolean>() {
             @Override
             public FxBoolean encode(FxString value) {
                 return new FxBoolean(Boolean.valueOf(value.getDefaultTranslation()));
             }
 
             @Override
             public List<? extends ValueComparator> getAvailableValueComparators() {
                 return allowedComparators;
             }
         });
         assert node.getAvailableComparators().containsAll(allowedComparators)
                 && allowedComparators.size() == node.getAvailableComparators().size()
                 : "Comparators should be " + allowedComparators + ", is: " + node.getAvailableComparators();
     }
 
     private void removeFromParent(QueryNode node) {
         node.getParent().removeChild(node);
     }
 
     private void combineNodes(QueryNode parent, QueryNode targetNode, QueryNode newNode) {
         QueryOperatorNode operatorNode = new QueryOperatorNode(nodeId++);
         parent.addChild(operatorNode);
         operatorNode.addChild(targetNode);
         operatorNode.addChild(newNode);
         parent.removeChild(targetNode);    // remove node that's now nested in level 1
     }
 
     /**
      * Asserts that both tree are equal.
      *
      * @param root1 root node of the first tree
      * @param root2 root node of the second tree
      */
     public static void assertEqualTrees(QueryNode root1, QueryNode root2) {
         if ((root1 == null && root2 != null) || !root1.equals(root2)) {
             assert false : "Trees are not equal: " + root1 + ", " + root2;
         }
         if (root1.getChildren().size() == root2.getChildren().size()) {
             for (int i = 0; i < root1.getChildren().size(); i++) {
                 assertEqualTrees(root1.getChildren().get(i), root2
                         .getChildren().get(i));
             }
         } else {
             assert false : "Tree children are different: "
                     + root1.getChildren().size() + " != "
                     + root2.getChildren().size();
         }
     }
 
     /**
      * Provides a basic set of "test" query trees.
      *
      * @return a basic set of "test" query trees.
      */
     @DataProvider(name = "basicQueries")
     public Object[][] getBasicQueries() {
         nodeId = 0; // reset global ID counter
         return new Object[][]{
                 {buildSimpleNode()},
                 {buildFlatTree(new InnerNodeGenerator(), 5)},
                 {buildNestedTree(5, new InnerNodeGenerator(), 5)},
                 {buildNestedTree(25, new InnerNodeGenerator(), 5)},
 
                 {buildFlatTree(new AssignmentNodeGenerator(FxDataType.String1024,
                         new FxString("Test")), 5)},
                 {buildNestedTree(5, new AssignmentNodeGenerator(
                         FxDataType.String1024, new FxString("Test")), 5)},
                 {buildNestedTree(25, new AssignmentNodeGenerator(
                         FxDataType.String1024, new FxString("Test")), 5)}};
     }
 
     /**
      * Creates a single test node
      *
      * @return node
      */
     public static QueryNode buildSimpleNode() {
         QueryNode node = new InnerTestNode();
         node.setId(42);
         node.setParent(null);
         return node;
     }
 
     /**
      * Build a flat tree containing some nodes under a root node.
      *
      * @param numNodes TODO
      */
     public static QueryRootNode buildFlatTree(QueryNodeGenerator generator, int numNodes) {
         QueryRootNode root = new QueryRootNode(nodeId++, QueryRootNode.Type.CONTENTSEARCH);
         for (int i = 0; i < numNodes; i++) {
             root.addChild(generator.createNode(nodeId++));
         }
         return root;
     }
 
     /**
      * Build a nested tree structure with up to maxLevel levels.
      *
      * @param maxLevel         the maximum tree depth
      * @param generator        the node generator(?)
      * @param numNodesPerLevel TODO
      * @return a nested tree structure with up to maxLevel levels.
      */
     public static QueryRootNode buildNestedTree(int maxLevel, QueryNodeGenerator generator, int numNodesPerLevel) {
         QueryRootNode root = buildFlatTree(generator, numNodesPerLevel);
         for (int level = 0; level < maxLevel; level++) {
             root.setParent(buildFlatTree(generator, numNodesPerLevel));
             root = (QueryRootNode) root.getParent();
         }
         return root;
     }
 
     /**
      * Generates InnerTestNode query nodes.
      */
     public static class InnerNodeGenerator implements QueryNodeGenerator<InnerTestNode> {
         public InnerTestNode createNode(int nodeId) {
             return new InnerTestNode(nodeId);
         }
     }
 
     public static class AssignmentNodeGenerator implements QueryNodeGenerator<AssignmentValueNode> {
         private final FxPropertyAssignment assignment;
 
         private final FxValue value;
 
         public AssignmentNodeGenerator(FxDataType dataType, FxValue value) {
             this.assignment = getTestAssignment();
             this.value = value;
         }
 
         public AssignmentValueNode createNode(int nodeId) {
             AssignmentValueNode node = new AssignmentValueNode(nodeId, assignment.getId());
             node.setValue(value != null ? value.copy() : null);
             return node;
         }
     }
 
     private static class CountingNodeVisitor implements QueryNodeVisitor {
         private int opNodes = 0;
         private int valueNodes = 0;
 
         public void visit(QueryOperatorNode operatorNode) {
             opNodes++;
         }
 
         public void visit(QueryValueNode valueNode) {
             valueNodes++;
         }
 
         public void setCurrentParent(QueryOperatorNode operatorNode) {
             // ignore
         }
 
         public int getOpNodes() {
             return opNodes;
         }
 
         public int getValueNodes() {
             return valueNodes;
         }
 
     }
 }
