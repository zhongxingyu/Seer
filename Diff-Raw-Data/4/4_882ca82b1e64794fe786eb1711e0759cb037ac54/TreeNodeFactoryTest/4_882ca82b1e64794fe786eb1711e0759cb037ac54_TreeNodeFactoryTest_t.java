 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.web.controller.section;
 
 import static org.jtalks.poulpe.web.controller.utils.ObjectCreator.*;
 import static org.testng.Assert.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.jtalks.common.model.entity.Branch;
import org.jtalks.common.model.entity.Entity;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.model.entity.TopicType;
 import org.testng.annotations.Test;
 
 /**
  * The test class for {@link TreeNodeFactory}
  * 
  * @author Konstantin Akimov
  */
 public class TreeNodeFactoryTest {
 
     PoulpeSection emptySection = fakeSection();
     PoulpeSection section = sectionWithBranches();
     List<PoulpeSection> sections = Arrays.asList(section, section);
 
     PoulpeBranch branch = fakeBranch();
 
     @Test
     public void getTreeNodeEmptySection() {
         ExtendedTreeNode<PoulpeSection> testNode = TreeNodeFactory.getTreeNode(emptySection);
 
         assertEquals(testNode.getData(), emptySection);
         assertTrue(testNode.getChildren().isEmpty());
         assertTrue(testNode.isExpanded());
     }
 
     @Test
     public void getTreeNodeBranch() {
         ExtendedTreeNode<PoulpeBranch> testNode = TreeNodeFactory.getTreeNode(branch);
 
         assertEquals(testNode.getData(), branch);
         assertNull(testNode.getChildren());
         assertTrue(testNode.isExpanded());
         assertTrue(testNode.isLeaf());
     }
 
     @Test
     public void getTreeNodeNull() {
        ExtendedTreeNode<?> testNode = TreeNodeFactory.getTreeNode((Entity) null);
         assertNull(testNode);
     }
 
     @Test
     public void getTreeNodeNotSuitableEntity() {
         ExtendedTreeNode<?> testNode = TreeNodeFactory.getTreeNode(new TopicType());
         assertNull(testNode);
     }
 
     /**
      * Test suitable entities with relations
      */
     @Test
     public void getTreeNodeWithRelationsTest() {
         ExtendedTreeNode<?> testNode = TreeNodeFactory.getTreeNode(section);
 
         assertEquals(testNode.getData(), section);
         assertEquals(testNode.getChildren().size(), section.getPoulpeBranches().size());
         assertTrue(testNode.isExpanded());
 
         assertNotNull(testNode.getChildAt(0));
 
         assertTrue(testNode.getChildAt(0).getData() instanceof PoulpeBranch);
         assertEquals(testNode.getChildAt(0).getData(), section.getPoulpeBranches().get(0));
     }
 
     @Test
     public void getTreeNodesTest() {
         List<ExtendedTreeNode<PoulpeSection>> nodes = TreeNodeFactory.getTreeNodes(sections);
 
         assertEquals(nodes.size(), sections.size());
         assertSectionsContainsAllBranches(nodes);
     }
 
     private static void assertSectionsContainsAllBranches(List<ExtendedTreeNode<PoulpeSection>> nodes) {
         for (ExtendedTreeNode<PoulpeSection> node : nodes) {
             List<Branch> childBranches = node.getData().getBranches();
             assertEquals(node.getChildCount(), childBranches.size());
             assertChildrenAreLeafs(node);
         }
     }
 
     @SuppressWarnings("unchecked")
     private static void assertChildrenAreLeafs(ExtendedTreeNode<PoulpeSection> node) {
         for (Object obj : node.getChildren()) {
             ExtendedTreeNode<PoulpeBranch> subnode = (ExtendedTreeNode<PoulpeBranch>) obj;
             assertTrue(subnode.isLeaf());
         }
     }
 
     @Test
     public void getTreeNodesWithNullsTest() {
         List<PoulpeSection> sections = Arrays.asList(section, null, section, null);
         List<ExtendedTreeNode<PoulpeSection>> nodes = TreeNodeFactory.getTreeNodes(sections);
 
         assertEquals(nodes.size(), 2);
         assertSectionsContainsAllBranches(nodes);
     }
 
 }
