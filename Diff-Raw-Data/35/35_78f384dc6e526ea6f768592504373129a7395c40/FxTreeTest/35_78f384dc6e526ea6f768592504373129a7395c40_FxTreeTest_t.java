 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.*;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.tree.FxTreeRemoveOp;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.ReferencedContent;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import com.flexive.core.storage.genericSQL.GenericTreeStorageSpreaded;
 import com.flexive.core.Database;
 import org.testng.Assert;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.fail;
 import static org.testng.Assert.assertTrue;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import org.apache.commons.lang.StringUtils;
 import java.util.List;
 import java.sql.Connection;
 
 /**
  * Tree engine tests.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"ejb", "tree"})
 public class FxTreeTest {
     TreeEngine tree;
     ScriptingEngine scripting;
     ContentEngine ce;
     TypeEngine ty;
 
     @BeforeClass
     public void beforeClass() throws Exception {
         login(TestUsers.SUPERVISOR);
         tree = EJBLookup.getTreeEngine();
         scripting = EJBLookup.getScriptingEngine();
         ce = EJBLookup.getContentEngine();
         ty = EJBLookup.getTypeEngine();
         EJBLookup.getDivisionConfigurationEngine().put(SystemParameters.TREE_CHECKS_ENABLED, true);
     }
 
     @AfterClass
     public void afterClass() throws FxLogoutFailedException {
         try {
             tree.clear(FxTreeMode.Live);
             tree.clear(FxTreeMode.Edit);
             EJBLookup.getDivisionConfigurationEngine().put(SystemParameters.TREE_CHECKS_ENABLED, false);
         } catch (FxApplicationException e) {
             //ignore
         }
         logout();
     }
 
     /**
      * Helper to build a node name
      *
      * @param number running number
      * @return generated node name
      */
     private static String getNodeName(int number) {
         return "Node" + number;
     }
 
     /**
      * Helper to build a node label
      *
      * @param number running number
      * @return generated node label
      */
     private static FxString getNodeLabel(int number) {
         return new FxString(true, "Node" + number + "Description");
     }
 
     /**
      * Edit tests
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void treeTestEdit() throws FxApplicationException {
         treeCRUD(FxTreeMode.Edit);
         createPath(FxTreeMode.Edit);
     }
 
     /**
      * Test path creation
      *
      * @param mode FxTreeMode
      * @throws FxApplicationException on errors
      */
     private void createPath(FxTreeMode mode) throws FxApplicationException {
         final long[] nodes = tree.createNodes(mode, FxTreeNode.ROOT_NODE, 0, "my/virtual/directory");
         Assert.assertEquals(nodes.length, 3, "Should have created 3 nodes");
         Assert.assertEquals(tree.getNode(mode, nodes[0]).getName(), "my");
         Assert.assertEquals(tree.getNode(mode, nodes[1]).getName(), "virtual");
         Assert.assertEquals(tree.getNode(mode, nodes[2]).getName(), "directory");
 
         // create subfolder, reuse two folders
         final long[] tmpNodes = tree.createNodes(mode, FxTreeNode.ROOT_NODE, 0, "my/virtual/tmp");
         Assert.assertEquals(tmpNodes.length, 3, "Should have returned 3 nodes");
         Assert.assertEquals(tree.getNode(mode, tmpNodes[0]).getName(), "my");
         Assert.assertEquals(tree.getNode(mode, tmpNodes[1]).getName(), "virtual");
         Assert.assertEquals(tree.getNode(mode, tmpNodes[2]).getName(), "tmp");
         Assert.assertEquals(nodes[0], tmpNodes[0]);
         Assert.assertEquals(nodes[1], tmpNodes[1]);
 
         // use /my/virtual as root node
         final long[] nestedNodes = tree.createNodes(mode, nodes[1], 0, "/directory/tmp2");
         Assert.assertEquals(nestedNodes.length, 2);
         Assert.assertEquals(tree.getNode(mode, nestedNodes[0]).getName(), "directory");
         Assert.assertEquals(tree.getNode(mode, nestedNodes[1]).getName(), "tmp2");
         Assert.assertEquals(nestedNodes[0], nodes[2]);  // check "directory" node
     }
 
     /**
      * Test most methods of the simplified API
      *
      * @param mode FxTreeMode to test
      * @throws FxApplicationException on errors
      */
     private void treeCRUD(FxTreeMode mode) throws FxApplicationException {
         //clear the tree
         tree.clear(mode);
         Assert.assertTrue(tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() == 0,
                 "Expected to have 0 children, got: [" + tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() + "]");
         //create new node
         FxTreeNodeEdit node1 = FxTreeNodeEdit.createNew(getNodeName(1));
         node1.setLabel(getNodeLabel(1));
         node1.setMode(mode);
         long id1 = tree.save(node1);
         Assert.assertTrue(tree.exist(mode, id1));
         Assert.assertTrue(tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() == 1,
                 "Expected to have 1 child, got: [" + tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() + "]");
         //load and check if all well
         FxTreeNode node1_loaded = tree.getNode(mode, id1);
         Assert.assertTrue(node1_loaded.getName().equals(node1.getName()));
         Assert.assertEquals(node1_loaded.getLabel(), node1.getLabel());
         //rename name
         tree.save(new FxTreeNodeEdit(node1_loaded).setName("abcd"));
         node1_loaded = tree.getNode(mode, id1);
         Assert.assertTrue(node1_loaded.getName().equals("abcd"), "Expected [abcd] - got [" + node1_loaded.getName() + "]");
         Assert.assertEquals(node1_loaded.getLabel(), node1.getLabel());
         //rename label
         tree.save(new FxTreeNodeEdit(node1_loaded).setLabel(getNodeLabel(42)));
         node1_loaded = tree.getNode(mode, id1);
         Assert.assertTrue(node1_loaded.getName().equals("abcd"));
         Assert.assertTrue(node1_loaded.getLabel().equals(getNodeLabel(42)));
         //create child
         FxTreeNodeEdit node1_1 = FxTreeNodeEdit.createNewChildNode(node1_loaded).setName("1").setLabel(getNodeLabel(1)).setMode(mode);
         long id1_1 = tree.save(node1_1);
         FxTreeNode node1_1_loaded = tree.getNode(mode, id1_1);
         Assert.assertTrue(node1_1_loaded.getParentNodeId() == node1_loaded.getId());
         //verify path
         Assert.assertTrue(node1_1_loaded.getPath().equals("/abcd/1"));
         //verify label
         Assert.assertEquals(tree.getLabels(mode, node1_1_loaded.getId()).get(0), "/" + getNodeLabel(42).getBestTranslation() + "/" + getNodeLabel(1).getBestTranslation());
         //create 2 other children for positioning tests
         FxTreeNodeEdit node1_2 = FxTreeNodeEdit.createNewChildNode(node1_loaded).setName("2").setLabel(getNodeLabel(2)).setMode(mode);
         long id1_2 = tree.save(node1_2);
         FxTreeNode node1_2_loaded = tree.getNode(mode, id1_2);
         //verify path
         Assert.assertTrue(node1_2_loaded.getPath().equals("/abcd/2"), "Expected [/abcd/2] got: [" + node1_2_loaded.getPath() + "]");
         //verify label
         Assert.assertTrue( ("/" + getNodeLabel(42).getBestTranslation() + "/" + getNodeLabel(2).getBestTranslation()).
                 equals(tree.getLabels(mode, node1_2_loaded.getId()).get(0)),
                 "Expected [/" + getNodeLabel(42).getBestTranslation() + "/" + getNodeLabel(2).getBestTranslation() + "] got: [" +
                         tree.getLabels(mode, node1_2_loaded.getId()).get(0) + "]");
         // verify unknown id return value
         final int invalidId = 999999;
         final String expectedOutcome = "/<unknown:" + invalidId + ">";
         assertEquals(tree.getLabels(mode, invalidId).get(0), expectedOutcome);
         FxTreeNodeEdit node1_3 = FxTreeNodeEdit.createNewChildNode(node1_loaded).setName("3").setLabel(getNodeLabel(3)).setMode(mode);
         long id1_3 = tree.save(node1_3);
         FxTreeNode node1_3_loaded = tree.getNode(mode, id1_3);
         Assert.assertTrue(tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() == 4,
                 "Expected to have 4 children, got: [" + tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() + "]");
         //verify positions - should be 1-2-3
         Assert.assertTrue(node1_1_loaded.getPosition() == 0, "Expected [0] got: [" + node1_1_loaded.getPosition() + "]");
         Assert.assertTrue(node1_2_loaded.getPosition() == 1, "Expected [1] got: [" + node1_2_loaded.getPosition() + "]");
         Assert.assertTrue(node1_3_loaded.getPosition() == 2, "Expected [2] got: [" + node1_3_loaded.getPosition() + "]");
 
         //swap positions of 1 and 3 to net 3-2-1
         tree.save(new FxTreeNodeEdit(node1_3_loaded).setPosition(-1));
         tree.save(new FxTreeNodeEdit(node1_1_loaded).setPosition(100));
         node1_1_loaded = tree.getNode(mode, id1_1);
         node1_2_loaded = tree.getNode(mode, id1_2);
         node1_3_loaded = tree.getNode(mode, id1_3);
         Assert.assertTrue(node1_1_loaded.getPosition() == 2, "Expected [2] got: [" + node1_1_loaded.getPosition() + "]");
         Assert.assertTrue(node1_2_loaded.getPosition() == 1, "Expected [1] got: [" + node1_2_loaded.getPosition() + "]");
         Assert.assertTrue(node1_3_loaded.getPosition() == 0, "Expected [0] got: [" + node1_3_loaded.getPosition() + "]");
         //3-2-1 => 3-1-2
         tree.save(new FxTreeNodeEdit(node1_1_loaded).setPosition(1));
         node1_1_loaded = tree.getNode(mode, id1_1);
         node1_2_loaded = tree.getNode(mode, id1_2);
         node1_3_loaded = tree.getNode(mode, id1_3);
         Assert.assertTrue(node1_1_loaded.getPosition() == 1, "Expected [1] got: [" + node1_1_loaded.getPosition() + "]");
         Assert.assertTrue(node1_2_loaded.getPosition() == 2, "Expected [2] got: [" + node1_2_loaded.getPosition() + "]");
         Assert.assertTrue(node1_3_loaded.getPosition() == 0, "Expected [0] got: [" + node1_3_loaded.getPosition() + "]");
         //3-1-2 => 1-2-3
         tree.save(new FxTreeNodeEdit(node1_3_loaded).setPosition(4));
         node1_1_loaded = tree.getNode(mode, id1_1);
         node1_2_loaded = tree.getNode(mode, id1_2);
         node1_3_loaded = tree.getNode(mode, id1_3);
         Assert.assertTrue(node1_1_loaded.getPosition() == 0, "Expected [0] got: [" + node1_1_loaded.getPosition() + "]");
         Assert.assertTrue(node1_2_loaded.getPosition() == 1, "Expected [1] got: [" + node1_2_loaded.getPosition() + "]");
         Assert.assertTrue(node1_3_loaded.getPosition() == 2, "Expected [2] got: [" + node1_3_loaded.getPosition() + "]");
         //delete 1_2 and check positions
         tree.remove(new FxTreeNodeEdit(node1_2_loaded), FxTreeRemoveOp.Remove, false);
         Assert.assertTrue(!tree.exist(mode, id1_2));
         node1_1_loaded = tree.getNode(mode, id1_1);
         node1_3_loaded = tree.getNode(mode, id1_3);
         Assert.assertTrue(node1_1_loaded.getPosition() == 0, "Expected [0] got: [" + node1_1_loaded.getPosition() + "]");
         Assert.assertTrue(node1_3_loaded.getPosition() == 1, "Expected [1] got: [" + node1_3_loaded.getPosition() + "]");
         Assert.assertTrue(tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() == 3,
                 "Expected to have 3 children, got: [" + tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() + "]");
         if (mode == FxTreeMode.Live)
             return; //children are to be removed in live mode
         //delete parent but not children and check if they moved up in hierarchy
         tree.remove(new FxTreeNodeEdit(node1_loaded), FxTreeRemoveOp.Remove, false);
         node1_1_loaded = tree.getNode(mode, id1_1);
         node1_3_loaded = tree.getNode(mode, id1_3);
         Assert.assertTrue(node1_1_loaded.getParentNodeId() == FxTreeNode.ROOT_NODE);
         Assert.assertTrue(node1_3_loaded.getParentNodeId() == FxTreeNode.ROOT_NODE);
         //attach 1_3 as child to 1_2
         tree.save(new FxTreeNodeEdit(node1_3_loaded).setParentNodeId(node1_1_loaded.getId()));
         node1_1_loaded = tree.getTree(mode, id1_1, 3);
         node1_3_loaded = tree.getNode(mode, id1_3);
         Assert.assertTrue(node1_1_loaded.getChildren().size() == 1);
         Assert.assertTrue(node1_1_loaded.getChildren().get(0).getId() == node1_3_loaded.getId());
         Assert.assertTrue(node1_3_loaded.getPath().equals("/1/3"), "Expected [/1/3] got: [" + node1_3_loaded.getPath() + "]");
         //delete 1_1 with children and check that 1_3 is gone too
         tree.remove(new FxTreeNodeEdit(node1_1_loaded), FxTreeRemoveOp.Remove, true);
         Assert.assertTrue(!tree.exist(mode, id1_3));
         Assert.assertTrue(tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() == 0,
                 "Expected to have 0 children, got: [" + tree.getNode(mode, FxTreeNode.ROOT_NODE).getTotalChildCount() + "]");
         tree.clear(mode);
 
         //test changing a referenced content
         ContentEngine co = EJBLookup.getContentEngine();
         FxPK testFolder = co.save(co.initialize(FxType.FOLDER));
         try {
             long nodeId = tree.save(FxTreeNodeEdit.createNew("Test").setParentNodeId(FxTreeNode.ROOT_NODE).setMode(mode));
             FxTreeNode node = tree.getNode(mode, nodeId);
             Assert.assertNotSame(node.getReference(), testFolder, "Expected to have a different folder pk!");
             tree.save(node.asEditable().setReference(testFolder));
             node = tree.getNode(mode, nodeId);
             Assert.assertEquals(node.getReference(), testFolder, "Node reference should have been updated!");
         } finally {
             co.remove(testFolder);
         }
         tree.clear(mode);
     }
 
     /**
      * Test node and subtree activation
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void activationTest() throws FxApplicationException {
         //clear live and edit tree
         tree.clear(FxTreeMode.Edit);
         tree.clear(FxTreeMode.Live);
         //test activation without subtree
         long[] ids = tree.createNodes(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, 0, "/Test1/Test2/Test3");
         tree.activate(FxTreeMode.Edit, ids[0], false);
         Assert.assertEquals(true, tree.exist(FxTreeMode.Live, ids[0]));
         Assert.assertEquals(false, tree.exist(FxTreeMode.Live, ids[1]));
         Assert.assertEquals(false, tree.exist(FxTreeMode.Live, ids[2]));
         //test activation with subtree
         ids = tree.createNodes(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, 0, "/ATest1/ATest2/ATest3");
         tree.activate(FxTreeMode.Edit, ids[0], true);
         Assert.assertEquals(true, tree.exist(FxTreeMode.Live, ids[0]));
         Assert.assertEquals(true, tree.exist(FxTreeMode.Live, ids[1]));
         Assert.assertEquals(true, tree.exist(FxTreeMode.Live, ids[2]));
         //there should be 2 nodes not active (Test2 and Test3), see if they get activated after activating all
         tree.activate(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, true);
         Assert.assertEquals(6, tree.getTree(FxTreeMode.Live, FxTreeNode.ROOT_NODE, 3).getTotalChildCount());
     }
 
     /**
      * Test behaviour when contents that are references of a tree node are removed
      * Expected behaviour: every tree node is a contentinstance itself. Removing the respective
      * content instance should therefore remove the relevant entry from the tree;
      * Live tree: childNodes are always removed!
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void contentRemoval() throws FxApplicationException {
         //clear live and edit tree
         FxPK contentPK;
         FxTreeMode mode = FxTreeMode.Edit;
         tree.clear(mode);
         tree.clear(FxTreeMode.Live);
         long[] nodes = createTestTree(mode);
 
         FxTreeNode n1 = tree.getNode(mode, nodes[0]);
         FxTreeNode n2 = tree.getNode(mode, nodes[1]);
         FxTreeNode n3 = tree.getNode(mode, nodes[2]);
         FxTreeNode childNode = tree.findChild(mode, nodes[1], "node_2_1");
 
         assertEquals(tree.getPathById(mode, n1.getId()), "/node_1");
         assertEquals(tree.getPathById(mode, n2.getId()), "/node_1/node_2");
         assertEquals(tree.getPathById(mode, n3.getId()), "/node_1/node_2/node_3");
         assertEquals(tree.getPathById(mode, childNode.getId()), "/node_1/node_2/node_2_1");
 
         // get the content reference of node2 and remove the content
         contentPK = n2.getReference();
         ce.remove(contentPK);
 
         // get the child node of nodes[0] which now must be "node_2_1"
         FxTreeNode n4 = tree.findChild(mode, nodes[0], "node_2_1");
         assertEquals(n4.getId(), childNode.getId());
         // check the new paths
         assertEquals(tree.getPathById(mode, n1.getId()), "/node_1");
         assertEquals(tree.getPathById(mode, n3.getId()), "/node_1/node_3");
         assertEquals(tree.getPathById(mode, childNode.getId()), "/node_1/node_2_1");
         assertEquals(tree.getPathById(mode, n2.getId()), "_"); // no path for the removed node
 
         try {
             tree.getNode(mode, nodes[1]);
             fail("Retrieving node " + nodes[1] + " should have thrown an exception");
         } catch (FxLoadException e) {
             // expected
         }
 
         // clean up
         tree.remove(mode, nodes[0], FxTreeRemoveOp.Remove, true);
 
         // perform tests in live tree: Removing a node from the live tree removes all its children (to avoid
         // inconsistencies with the edit tree)
         mode = FxTreeMode.Live;
         nodes = createTestTree(mode);
 
         n1 = tree.getNode(mode, nodes[0]);
         n2 = tree.getNode(mode, nodes[1]);
         n3 = tree.getNode(mode, nodes[2]);
         childNode = tree.findChild(mode, nodes[1], "node_2_1");
 
         assertEquals(tree.getPathById(mode, n1.getId()), "/node_1");
         assertEquals(tree.getPathById(mode, n2.getId()), "/node_1/node_2");
         assertEquals(tree.getPathById(mode, n3.getId()), "/node_1/node_2/node_3");
         assertEquals(tree.getPathById(mode, childNode.getId()), "/node_1/node_2/node_2_1");
 
         contentPK = n2.getReference();
         ce.remove(contentPK);
 
         try { // trying to find the same child node as in the edit tree should throw an exception
             tree.findChild(mode, nodes[0], "node_2_1");
             fail("Trying to retrieve node_2_1 should have thrown an exception");
         } catch (FxNotFoundException e) {
             // expected
         }
         assertEquals(tree.getPathById(mode, n1.getId()), "/node_1");
         assertEquals(tree.getPathById(mode, childNode.getId()), "_");
 
         // clean up
         tree.remove(mode, nodes[0], FxTreeRemoveOp.Remove, true);
     }
 
     /**
      * Create a lot of nodes trying to provoke errors
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void massCreateTest() throws FxApplicationException {
         FxTreeMode mode = FxTreeMode.Edit;
         tree.clear(mode);
         String path = "/t1/t2/t3/t4/t5/t6/t7/t8/t9/t10";
         for (int i = 0; i < 3; i++) {
             path = path + "/f" + (i + 1);
             tree.createNodes(mode, FxTreeNode.ROOT_NODE, 0, path);
             FxTreeNode parent = tree.getNode(mode, tree.getIdByPath(mode, path));
             createSubNodes(parent, 40);
         }
     }
 
     /**
      * Create count subnodes attached to parent
      *
      * @param parent parent node
      * @param count  number of subnodes to create
      * @throws FxApplicationException on errors
      */
     private void createSubNodes(FxTreeNode parent, int count) throws FxApplicationException {
         FxTreeNodeEdit node;
         for (int i = 0; i < count; i++) {
             node = FxTreeNodeEdit.createNewChildNode(parent).setName("Node" + i);
             tree.save(node);
         }
     }
 
     public static long scriptCounter = 0;
 
     /**
      * Test add/remove node scripting
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void scriptingAddRemoveTest() throws FxApplicationException {
         FxTreeMode mode = FxTreeMode.Edit;
         tree.clear(mode);
         scriptCounter = 0;
         String code = "println \"[Groovy script]=== After adding node ${node.id}: ${node.path} ===\"\n" +
                 "com.flexive.tests.embedded.FxTreeTest.scriptCounter++";
         FxScriptInfo siAdd = EJBLookup.getScriptingEngine().createScript(FxScriptEvent.AfterTreeNodeAdded,
                 "afterNodeAdded.gy", "Test script", code);
         code = "println \"[Groovy script]=== Before removing node ${node.id}: ${node.path} ===\"\n" +
                 "com.flexive.tests.embedded.FxTreeTest.scriptCounter -= 2";
         FxScriptInfo siBeforeRemove = EJBLookup.getScriptingEngine().createScript(FxScriptEvent.BeforeTreeNodeRemoved,
                 "beforeNodeRemoved.gy", "Test script", code);
         code = "println \"[Groovy script]=== After removing node ${node.id}: ${node.path} ===\"\n" +
                 "com.flexive.tests.embedded.FxTreeTest.scriptCounter++";
         FxScriptInfo siAfterRemove = EJBLookup.getScriptingEngine().createScript(FxScriptEvent.AfterTreeNodeRemoved,
                 "afterNodeRemoved.gy", "Test script", code);
         try {
             Assert.assertEquals(scriptCounter, 0);
             long nodeId = tree.save(FxTreeNodeEdit.createNew("Test1").setMode(mode).setParentNodeId(FxTreeNode.ROOT_NODE));
             Assert.assertEquals(scriptCounter, 1);
             long topNode = tree.createNodes(mode, FxTreeNode.ROOT_NODE, 0, "/A/B/C")[0];
             Assert.assertEquals(scriptCounter, 4);
             tree.copy(mode, topNode, nodeId, 0);
             Assert.assertEquals(scriptCounter, 7);
             tree.remove(tree.getNode(mode, topNode), FxTreeRemoveOp.Unfile, true);
             Assert.assertEquals(scriptCounter, 4);
             tree.remove(tree.getNode(mode, nodeId), FxTreeRemoveOp.Unfile, true);
             Assert.assertEquals(scriptCounter, 0);
         } finally {
             scripting.remove(siAdd.getId());
             scripting.remove(siBeforeRemove.getId());
             scripting.remove(siAfterRemove.getId());
         }
     }
 
     /**
      * Test activate node scripting
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void scriptingActivateTest() throws FxApplicationException {
         FxTreeMode mode = FxTreeMode.Edit;
         tree.clear(mode);
         tree.clear(FxTreeMode.Live);
         scriptCounter = 0;
         String code = "println \"[Groovy script]=== Before activating node ${node.id}: ${node.path} ===\"\n" +
                 "com.flexive.tests.embedded.FxTreeTest.scriptCounter+=2";
         FxScriptInfo siBeforeActivate = EJBLookup.getScriptingEngine().createScript(FxScriptEvent.BeforeTreeNodeActivated,
                 "beforeNodeActivate.gy", "Test script", code);
         code = "println \"[Groovy script]=== After activating node ${node.id}: ${node.path} ===\"\n" +
                 "com.flexive.tests.embedded.FxTreeTest.scriptCounter--";
         FxScriptInfo siAfterActivate = EJBLookup.getScriptingEngine().createScript(FxScriptEvent.AfterTreeNodeActivated,
                 "afterNodeActivate.gy", "Test script", code);
         try {
             Assert.assertEquals(scriptCounter, 0);
             long nodeId = tree.save(FxTreeNodeEdit.createNew("Test1").setMode(mode).setParentNodeId(FxTreeNode.ROOT_NODE));
             tree.activate(FxTreeMode.Edit, nodeId, false);
             Assert.assertEquals(scriptCounter, 1);
 
             long topNode = tree.createNodes(mode, FxTreeNode.ROOT_NODE, 0, "/A/B/C")[0];
             tree.activate(FxTreeMode.Edit, topNode, true);
             Assert.assertEquals(scriptCounter, 4);
         } finally {
             scripting.remove(siBeforeActivate.getId());
             scripting.remove(siAfterActivate.getId());
             tree.clear(FxTreeMode.Edit);
             tree.clear(FxTreeMode.Live);
         }
     }
 
     /**
      * Test replacing a content with a folder scripting
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void scriptingFolderReplacementTest() throws FxApplicationException {
         FxTreeMode mode = FxTreeMode.Edit;
         tree.clear(mode);
         scriptCounter = 0;
         ContentEngine co = EJBLookup.getContentEngine();
         FxPK testContent = co.save(co.initialize(FxType.ROOT_ID));
         String code = "println \"[Groovy script]=== AfterTreeNodeFolderReplacement node ${node.id}: ${node.path}. co-pk: ${content}, folder-pk: ${node.reference} ===\"\n" +
                 "com.flexive.tests.embedded.FxTreeTest.scriptCounter=42\n" +
                 "if(content.id == node.reference.id) com.flexive.tests.embedded.FxTreeTest.scriptCounter=-1";
         FxScriptInfo siAfterTreeNodeFolderReplacement = EJBLookup.getScriptingEngine().createScript(FxScriptEvent.AfterTreeNodeFolderReplacement,
                 "treeNodeFolderReplacement.gy", "Test script", code);
         try {
             Assert.assertEquals(scriptCounter, 0);
             tree.save(FxTreeNodeEdit.createNew("Test1").
                     setMode(mode).
                     setParentNodeId(FxTreeNode.ROOT_NODE).
                     setReference(testContent));
             tree.createNodes(mode, FxTreeNode.ROOT_NODE, 0, "/Test1/A");
             co.remove(testContent);
             Assert.assertEquals(scriptCounter, 42);
         } finally {
             scripting.remove(siAfterTreeNodeFolderReplacement.getId());
             tree.clear(mode);
             try {
                 co.remove(testContent);
             } catch (FxApplicationException e) {
                 //ignore
             }
         }
     }
 
     @Test
     public void treeIteratorTest() throws FxApplicationException {
         tree.clear(FxTreeMode.Edit);
         final String[] names = {"my", "virtual", "directory"};
         final long[] nodes = tree.createNodes(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, 0, StringUtils.join(names, "/"));
         int index = 0;
         for (FxTreeNode node : tree.getTree(FxTreeMode.Edit, nodes[0], 5)) {
             Assert.assertTrue(node.getName().equals(names[index]), "Expected node name: " + names[index] + ", got: " + node.getName());
             index++;
         }
 
         // test mixed folder/node access
         tree.createNodes(FxTreeMode.Edit, nodes[1], 999, "new/directory");
         final String[] expected = {"my", "virtual", "directory", "new", "directory"};
         /* new folder structure:
             my/
                 virtual/
                     directory/
                     new/
                         directory/
          */
         index = 0;
         for (FxTreeNode node : tree.getTree(FxTreeMode.Edit, nodes[0], 5)) {
             Assert.assertTrue(node.getName().equals(expected[index]), "Expected node name: " + expected[index] + ", got: " + node.getName());
             index++;
         }
     }
 
     @Test
     public void treeCaptionPathToIdTest() throws FxApplicationException {
         tree.clear(FxTreeMode.Edit);
         FxTreeNodeEdit tn = FxTreeNodeEdit.createNew("NodeName");
         tn.setLabel(new FxString(false, "NodeLabel"));
         tn.setParentNodeId(FxTreeNode.ROOT_NODE);
         long nodeId = tree.save(tn);
         Assert.assertTrue(tree.getIdByLabelPath(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, "/NodeLabel") == nodeId);
         FxTreeNodeEdit tn2 = FxTreeNodeEdit.createNew("NodeName2");
         tn2.setLabel(new FxString(false, "NodeLabel2"));
         tn2.setParentNodeId(nodeId);
         long nodeId2 = tree.save(tn2);
         Assert.assertTrue(tree.getIdByLabelPath(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, "/NodeLabel/NodeLabel2") == nodeId2);
     }
 
     /**
      * Tests #findChild and #getNodesWithReference methods
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void findChildrenTest() throws FxApplicationException {
         final String node2 = "node_2";
         FxTreeMode mode1, mode2;
         FxTreeNode result;
         List<FxTreeNode> nodesWithRefResults;
         FxPK pk;
         final String contentXPath = "TREETESTTYPE/TESTPROP";
         final String testData = "testdata, default lang 1234567890";
         mode1 = FxTreeMode.Edit;
         mode2 = FxTreeMode.Live;
         tree.clear(mode1);
         tree.clear(mode2);
         long[] nodes = createTestTree(mode1);
 
         // find by name
         result = tree.findChild(mode1, nodes[0], node2);
         assertEquals(result.getName(), node2);
         try { // let's cause an exception
             tree.findChild(mode2, nodes[0], node2);
             fail("Should not be able to find " + node2 + " in Live tree");
         } catch (FxNotFoundException e) {
             // expected
         }
 
         pk = result.getReference();
         // find by reference id
         result = tree.findChild(mode1, nodes[0], pk.getId());
         assertEquals(result.getName(), node2);
         try { // let's cause an exception
             tree.findChild(mode2, nodes[0], pk.getId());
             fail("Should not be able to find " + node2 + " in Live tree");
         } catch (FxNotFoundException e) {
             // expected
         }
 
         // find by pk (reference)
         result = tree.findChild(mode1, nodes[0], pk);
         assertEquals(result.getName(), node2);
         try { // let's cause an exception
             tree.findChild(mode2, nodes[0], pk);
             fail("Should not be able to find " + node2 + " in Live tree");
         } catch (FxNotFoundException e) {
             // expected
         }
 
         // find by FxReference
         // this also tests creating a content instance of a type and using it as a tree node
         FxTreeNode parent = tree.getNode(mode1, nodes[0]);
         long typeId = createTestType();
         FxPK contentPK = createTestContent(parent, typeId, testData);
         FxReference ref = new FxReference(false, new ReferencedContent(contentPK));
         result = tree.findChild(mode1, nodes[0], ref);
         assertEquals(result.getReference(), contentPK);
         try { // let's cause an exception
             tree.findChild(mode2, nodes[0], ref);
             fail("Should not be able to find " + contentPK + " in Live tree");
         } catch (FxNotFoundException e) {
             // expected
         }
 
         // retrieve all nodes with references
         nodesWithRefResults = tree.getNodesWithReference(mode1, ref.getBestTranslation().getId());
         int resultOK = 0;
         FxTreeNode tmp = null;
         for (FxTreeNode node : nodesWithRefResults) {
             if (node.getParentNodeId() == nodes[0]) {
                 resultOK++;
                 tmp = node;
             }
         }
         assertEquals(resultOK, 1); // should find exactly one node with a content reference
         try {
             FxContent co = ce.load(tmp.getReference());
             assertEquals(co.getValue(contentXPath).toString(), testData);
         } catch (NullPointerException e) { // Idea satisfaction guaranteed
             fail("The FxContent should have been found");
         }
 
         // clean up
         ce.remove(contentPK);
         ty.remove(typeId);
         tree.remove(mode1, nodes[0], FxTreeRemoveOp.Remove, true);
     }
 
     /**
      * Tests the #getPaths() and #getPathById methods
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void getPathsTest() throws FxApplicationException {
         FxTreeMode mode= FxTreeMode.Edit;
         tree.clear(mode);
         long[] nodes = createTestTree(mode);
         final String node2_2 = "node_2_2";
         final String expectedPath1 = "/node_1/node_2/node_2_2";
         final String expectedPath2 = "/node_1/node_2/node_3";
         final long invalidNodeId = 1234567890;
         int pathsOK = 0;
 
         FxTreeNode node = tree.findChild(mode, nodes[1], node2_2);
 
         // retrieve the paths for node_2.2 and node_3
         for (String path : tree.getPaths(mode, nodes[2], node.getId())) {
             if (path.equals(expectedPath1) || path.equals(expectedPath2))
                 pathsOK++;
         }
         assertEquals(pathsOK, 2); // should find 2 correct paths
         pathsOK = 0; // reset
 
         // assert that an invalid node id will result in an empty path (represented by "_")
         String returnedPath = "";
         for (String path : tree.getPaths(mode, invalidNodeId)) {
             if (path == null || path.equals("_")) // returns an underscore if no path was found
                 returnedPath = path;
             pathsOK++;
         }
         assertTrue(pathsOK > 0);
         assertEquals(returnedPath, "_");
 
         // test getPathById()
         String path = tree.getPathById(mode, nodes[2]);
         assertEquals(path, expectedPath2);
 
         // clean up
         tree.remove(mode, nodes[0], FxTreeRemoveOp.Remove, true);
     }
 
     /**
      * STUB TEST: #setData and #getData are not implemented yet!!
      * Tests the #setData and #getData methods
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void setAndGetDataTest() throws FxApplicationException {
         FxTreeMode mode = FxTreeMode.Edit;
         final String data1 = "Template A";
         final String data2 = "Template B";
         long nodes[] = createTestTree(mode);
 
         try {
             tree.setData(mode, nodes[0], data1);
             tree.setData(mode, nodes[1], data2);
         } catch (RuntimeException e) {
             // expected, method throws UnsupportedOperationException
         }
 
         try {
             tree.getDatas(mode, nodes[2]);
         } catch (RuntimeException e) {
             // expected, method throws UnsupportedOperationException
         }
         // clean up
         tree.remove(mode, nodes[0], FxTreeRemoveOp.Remove, true);
     }
 
     /**
      * Tests the #getIdChain and #getReverseIdChainmethods
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void getIdsTest() throws FxApplicationException {
         FxTreeMode mode = FxTreeMode.Edit;
         long nodes[] = createTestTree(mode);
         long invalidId = 1234567890;
 
         long[] ids = tree.getIdChain(mode, nodes[2]); // should return 1, nodes[0], nodes[1], nodes[2]
         assertEquals(ids[0], 1);
         assertEquals(ids[1], nodes[0]);
         assertEquals(ids[2], nodes[1]);
         assertEquals(ids[3], nodes[2]);
 
         assertEquals(tree.getIdChain(mode, invalidId), null); // should be null
 
         ids = tree.getReverseIdChain(mode, nodes[2]); // should return nodes[2], nodes[1], nodes[0], 1
         assertEquals(ids[0], nodes[2]);
         assertEquals(ids[1], nodes[1]);
         assertEquals(ids[2], nodes[0]);
         assertEquals(ids[3], 1);
 
         // clean up
         tree.remove(mode, nodes[0], FxTreeRemoveOp.Remove, true);
     }
 
     /**
      * This tests the #populate method
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void populateTest() throws FxApplicationException {
         // create the testdata in the live tree, assert that names of the root nodes
         FxTreeMode mode = FxTreeMode.Live;
         tree.clear(mode);
         FxTreeNode n1, n2, n3;
         int maxLevel = 1;
         final String node1 = "Level2_0_";
         final String node2 = "Level3_0_9_";
         final String node3 = "Level4_0_9_99_";
 
         try {
             tree.populate(mode, maxLevel);
         } catch (FxApplicationException e) {
             fail(e.getMessage());
         }
         n1 = tree.findChild(mode, 1, node1);
         n2 = tree.findChild(mode, n1.getId(), node2);
         n3 = tree.findChild(mode, n2.getId(), node3);
 
         assertEquals(n1.getName(), node1);
         assertEquals(n2.getName(), node2);
         assertEquals(n3.getName(), node3);
 
         //clean up
         tree.remove(mode, n1.getId(), FxTreeRemoveOp.Remove, true);
     }
 
 
     /**
      * GenericTreeStorage: #setData method
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void genericTreeStorageSetDataTest() throws FxApplicationException {
         FxTreeMode mode = FxTreeMode.Edit;
         tree.clear(mode);
         long[] nodes = createTestTree(mode);
         String data = "TEMPLATE A";
         TreeStorage treeStorage = new TreeStorage();
 
         Connection con = null;
         try {
             con = Database.getDbConnection();
             treeStorage.setData(con, mode, nodes[0], data);
 
             FxTreeNode n = tree.getNode(mode, nodes[0]);
             assertEquals(n.getData(), data);
 
         } catch (Throwable t) {
             fail("GenericTreeStorage #setData shouldn't throw an exception. Exception: " + t.getMessage());
             t.printStackTrace();
         } finally {
             Database.closeObjects(FxTreeTest.class, con, null);
             // clean up
             tree.remove(mode, nodes[0], FxTreeRemoveOp.Remove, true);
         }
     }
 
     /**
      * GenericTreeStorage: #copy & #getCopyOfCount (explicitly)
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void genericTreeStorageGetCopyOfCountTest() throws FxApplicationException {
         FxTreeMode mode = FxTreeMode.Edit;
         tree.clear(mode);
         long[] nodes = createTestTree(mode);
         long typeId = createTestType();
         FxTreeNode node = tree.getNode(mode, nodes[0]);
         final String testData = "TESTDATA TO BE COPIED";
         final int expectedCopyCount = 1; // expected copy count result - we will copy a node exactly once
         TreeStorage treeStorage = new TreeStorage();
         FxPK contentPK = createTestContent(node, typeId, testData);
         FxTreeNode contentNode1 = tree.findChild(mode, nodes[0], contentPK);
 
         long copyId = tree.copy(mode, contentNode1.getId(), nodes[2], 0);
         FxTreeNode contentNode2 = tree.findChild(mode, nodes[2], contentPK); // get the new tree node
 
         assertEquals(copyId, contentNode2.getId());
         assertEquals(contentNode2.getName(), contentNode1.getName());
         assertEquals(contentNode1.getReference(), contentNode2.getReference());
 
         Connection con = null;
         try {
             con = Database.getDbConnection();
             int actualCopyCount = treeStorage.getCopyOfCount(con, mode, "", nodes[2], copyId);
             assertEquals(actualCopyCount, expectedCopyCount);
         } catch (Exception e) {
             fail("GenericTreeStorage #getCopyOfCount shouldn't throw an exception. Exception: " + e.getMessage());
         } finally {
             Database.closeObjects(FxTreeTest.class, con, null);
             // clean up
             ce.remove(contentPK);
             ty.remove(typeId);
             tree.remove(mode, nodes[0], FxTreeRemoveOp.Remove, true);
         }
     }
 
     /**
      * GenericTreeStorage: #flagDirty method
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void genericTreeStorageFlagDirtyTest() throws FxApplicationException {
         TreeStorage treeStorage = new TreeStorage();
         FxTreeMode mode = FxTreeMode.Edit;
         tree.clear(mode);
         long nodeId = createTestTree(mode)[0];
 
         Connection con = null;
         try {
             con = Database.getDbConnection();
             treeStorage.flagDirty(con, mode, nodeId);
             FxTreeNode n = tree.getNode(mode, nodeId);
             assertTrue(n.isDirty());
         } catch (Exception e) {
             fail("Failed to flag node " + nodeId + " dirty. Exception: " + e.getMessage());
         } finally {
             Database.closeObjects(FxTreeTest.class, con, null);
             // clean up
             tree.remove(mode, nodeId, FxTreeRemoveOp.Remove, true);
         }
     }
 
     /**
      * Helper class for testing GenericTreeStorage methods
      */
     class TreeStorage extends GenericTreeStorageSpreaded {
         public TreeStorage() {
             super();
         }
 
         public int getCopyOfCount(Connection con, FxTreeMode mode, String copyOfPrefix, long parentNodeId, long nodeId) throws FxTreeException {
             return super.getCopyOfCount(con, mode, copyOfPrefix, parentNodeId, nodeId);
         }
 
         public void setData(Connection con, FxTreeMode mode, long nodeId, String data) throws FxApplicationException {
             super.setData(con, mode, nodeId, data);
         }
 
         public void flagDirty(Connection con, FxTreeMode mode, long nodeId) throws FxUpdateException {
             super.flagDirty(con, mode, nodeId);
         }
     }
 
 
     /**
      * Helper method creates a test tree
      *
      * @param mode FxTreeMode (e.g. .Edit)
      * @return long[] array of tree node ids
      * @throws FxApplicationException on errors
      */
     private long[] createTestTree(FxTreeMode mode) throws FxApplicationException {
        FxTreeMode _mode = mode;
        if( mode == FxTreeMode.Live)
            _mode = FxTreeMode.Edit;
        final long[] nodes = tree.createNodes(_mode, FxTreeNode.ROOT_NODE, 0, "node_1/node_2/node_3");
        tree.createNodes(_mode, (int) nodes[1], 0, "node_2_1");
        tree.createNodes(_mode, (int) nodes[1], 1, "node_2_2");
        tree.createNodes(_mode, (int) nodes[2], 0, "node_3_1");
        if( mode == FxTreeMode.Live)
            tree.activate(_mode, nodes[0], true);
         return nodes;
     }
 
     /**
      * Creates a test type
      *
      * @return the id of the test type
      * @throws FxApplicationException on errors
      */
     private long createTestType() throws FxApplicationException {
         long typeId = EJBLookup.getTypeEngine().save(FxTypeEdit.createNew("TREETESTTYPE", new FxString("Tree test type"),
                 CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()), null));
 
         EJBLookup.getAssignmentEngine().createProperty(typeId, FxPropertyEdit.createNew(
                 "TESTPROP", new FxString(true, FxLanguage.ENGLISH, "TESTPROP"), new FxString(true, FxLanguage.ENGLISH, "TESTPROP"),
                 new FxMultiplicity(0, 5), CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.String1024).setMultiLang(false), "/");
 
         return typeId;
     }
 
     /**
      * Creates a test content instance for the reference search and attaches the content to a given FxTreeNode
      *
      * @param parentNode the parent FxTreeNode to which the content should be attached
      * @param typeId     the id of the type for which the content instance will be created
      * @param testData   a String value for test data
      * @return FxPK the content instance PK
      * @throws FxApplicationException on errors
      */
     private FxPK createTestContent(FxTreeNode parentNode, long typeId, String testData) throws FxApplicationException {
         // create content for testing - will produce console output
         FxString data = new FxString(false, testData);
 
         FxContent co = ce.initialize(typeId);
         FxPK contentPK = ce.save(co);
         co = ce.load(contentPK);
         co.setValue("/TESTPROP[1]", data);
         contentPK = ce.save(co);
         tree.save(FxTreeNodeEdit.createNew(String.valueOf(contentPK)).setParentNodeId(parentNode.getId()).setReference(contentPK));
 
         return contentPK;
     }
 }
