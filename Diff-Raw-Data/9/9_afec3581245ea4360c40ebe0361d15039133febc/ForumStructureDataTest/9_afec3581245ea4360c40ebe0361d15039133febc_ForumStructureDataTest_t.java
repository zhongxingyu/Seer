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
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.IsInstanceOf.instanceOf;
 import static org.jtalks.poulpe.web.controller.section.TreeNodeFactory.buildForumStructure;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 import org.jtalks.poulpe.model.dao.GroupDao;
 import org.jtalks.poulpe.model.entity.Jcommune;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.test.fixtures.TestFixtures;
 import org.jtalks.poulpe.web.controller.section.dialogs.BranchEditingDialog;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import org.zkoss.zul.TreeNode;
 
 /**
  * @author stanislav bashkirtsev
  */
 public class ForumStructureDataTest {
     private ForumStructureData sut;
     private BranchEditingDialog branchEditingDialog;
 
     @BeforeMethod
     public void setUp() throws Exception {
         branchEditingDialog = mock(BranchEditingDialog.class);
         sut = new ForumStructureData(branchEditingDialog);
     }
 
     @Test
     public void testGetSelectedItemFirstTime() throws Exception {
         ForumStructureItem item = new ForumStructureItem();
         ForumStructureItem previous = sut.setSelectedItem(item);
         assertNull(previous.getItem());
     }
 
     @Test(dataProvider = "provideSutWithTree", enabled = false)
     public void testPutSelectedBranchToSectionFromDropdown_withNewBranchAddedToDestinationSection(ForumStructureData sut) {
         ForumStructureItem branchToPut = new ForumStructureItem(new PoulpeBranch());
         sut.setSelectedItem(branchToPut);
         sut.getBranchDialog().getSectionList().addToSelection(sut.getBranchDialog().getSectionList().get(1));//set destination section
 
         sut.putSelectedBranchToSectionInDropdown();
         assertSame(sut.getStructureTree().find(branchToPut).getParent(), sut.getStructureTree().getRoot().getChildAt(1));
     }
 
     @Test(dataProvider = "provideSutWithTree", enabled = false)
     public void testPutSelectedBranchToSectionFromDropdown_withExistingBranchAddedToDestinationSection(ForumStructureData sut) {
         ForumStructureItem branchToPut = sut.getStructureTree().getChild(0, 0).getData();
         sut.setSelectedItem(branchToPut);
         sut.getBranchDialog().getSectionList().addToSelection(sut.getBranchDialog().getSectionList().get(1));//set destination section
 
         sut.putSelectedBranchToSectionInDropdown();
         assertSame(sut.getStructureTree().find(branchToPut).getParent(), sut.getStructureTree().getRoot().getChildAt(1));
         assertNotSame(sut.getStructureTree().getChild(0, 0), branchToPut);
     }
 
     @Test(dataProvider = "provideSutWithMockedTree", enabled = false)
     public void testPutSelectedBranchToSectionFromDropdown_verifyNodeIsOpenAndSelected(ForumStructureData sut) {
         ForumStructureTreeModel treeModel = sut.getStructureTree();
         ForumStructureItem branchToPut = new ForumStructureItem(new PoulpeBranch());
         sut.setSelectedItem(branchToPut);
 
         ForumStructureItem destinationSection = new ForumStructureItem(new PoulpeSection());
         doReturn(destinationSection).when(branchEditingDialog).getSectionSelectedInDropdown();
 
         sut.putSelectedBranchToSectionInDropdown();
         verify(treeModel).putBranch(branchToPut, destinationSection);
     }
 
     @Test
     public void testGetSelectedItemReturnsPreviousValue() throws Exception {
         ForumStructureItem item = new ForumStructureItem();
         sut.setSelectedItem(item);
         assertSame(item, sut.setSelectedItem(new ForumStructureItem()));
     }
 
     @Test(dataProvider = "provideTreeModelWithSectionsAndBranches", enabled = false)
     public void testAddSelectedSectionToTreeIfNew_notPersistentItemSelected(ForumStructureTreeModel tree) {
         ForumStructureItem newSection = new ForumStructureItem(new PoulpeSection());
         sut.setStructureTree(tree);
         sut.setSelectedItem(newSection);
 
         sut.addSelectedSectionToTreeIfNew();
         assertSame(tree.getSelection().iterator().next().getData(), newSection);
     }
 
     @Test(dataProvider = "provideTreeModelWithSectionsAndBranches")
     public void testRemoveSelectedItem_selectedSection(ForumStructureTreeModel treeModel) {
         TreeNode<ForumStructureItem> selectedNode = treeModel.getRoot().getChildAt(1);
         treeModel.addSelectionPath(new int[]{1});
         sut.setSelectedItem(selectedNode.getData());
         sut.setStructureTree(treeModel);
 
         assertSame(selectedNode.getData(), sut.removeSelectedItem());
         assertEquals(-1, treeModel.getRoot().getIndex(selectedNode));
     }
 
     @Test(dataProvider = "provideTreeModelWithSectionsAndBranches")
     public void testRemoveSelectedItem_selectedBranch(ForumStructureTreeModel treeModel) {
         TreeNode<ForumStructureItem> selectedNode = treeModel.getRoot().getChildAt(1).getChildAt(1);
        treeModel.addSelectionPath(new int[] { 1, 1 });
         sut.setSelectedItem(selectedNode.getData());
         sut.setStructureTree(treeModel);
 
         assertSame(selectedNode.getData(), sut.removeSelectedItem());
         assertEquals(-1, treeModel.getRoot().getChildAt(1).getIndex(selectedNode));
     }
 
     @Test(dataProvider = "provideTreeModelWithSectionsAndBranches", enabled = false)
     public void testGetSectionSelectedInDropDown(ForumStructureTreeModel treeModel) throws Exception {
         sut.setStructureTree(treeModel);
         ForumStructureItem itemSelectedInDropdown = treeModel.getRoot().getChildAt(1).getData();
         sut.getBranchDialog().getSectionList().addToSelection(itemSelectedInDropdown);
         assertSame(itemSelectedInDropdown.getItem(), sut.getSectionSelectedInDropdown().getItem());
     }
 
     @Test
     public void testShowSectionDialog_editingExisting() throws Exception {
         ForumStructureItem selectedItem = new ForumStructureItem(new PoulpeSection());
         sut.setSelectedItem(selectedItem);
         sut.showSectionDialog(false);
         assertTrue(sut.isShowSectionDialog());
         assertSame(sut.getSelectedItem(), selectedItem);
     }
 
     @Test
     public void testShowSectionDialog_creatingNew() throws Exception {
         ForumStructureItem selectedItem = new ForumStructureItem();
         sut.setSelectedItem(selectedItem);
 
         sut.showSectionDialog(true);
         assertTrue(sut.isShowSectionDialog());
         assertNotSame(sut.getSelectedItem(), selectedItem);
         assertThat(sut.getSelectedItem().getItem(), instanceOf(PoulpeSection.class));
     }
 
     @Test(dataProvider = "provideSutWithSpiedBranchDialog", enabled = false)
     public void testShowBranchDialog(ForumStructureData sut) {
         doReturn(sut.getBranchDialog()).when(sut.getBranchDialog()).renewSectionsFromTree(any(ForumStructureTreeModel.class));
         ForumStructureItem selectedBranch = givenSelectedItemAsBranch(sut);
         ForumStructureTreeModel treeModel = givenMockedTreeModel(sut);
         ForumStructureItem sectionContainingBranch = new ForumStructureItem(new PoulpeSection());
         doReturn(sectionContainingBranch).when(treeModel).getSelectedData(0);
 
         sut.showBranchDialog(false);
         assertSame(branchEditingDialog.getSectionSelectedInDropdown(), sectionContainingBranch);
         assertSame(branchEditingDialog.getEditedBranch(), selectedBranch);
         assertTrue(branchEditingDialog.isShowDialog());
     }
 
     private ForumStructureTreeModel givenMockedTreeModel(ForumStructureData sut) {
         ForumStructureTreeModel treeModel = mock(ForumStructureTreeModel.class);
         sut.setStructureTree(treeModel);
         return treeModel;
     }
 
     @Test(dataProvider = "provideTreeModelWithSectionsAndBranches", enabled = false)
     public void testShowBranchDialog_createNew(ForumStructureTreeModel treeModel) {
         ForumStructureItem selectedSection = treeModel.getRoot().getChildAt(0).getData();
         treeModel.addSelectionPath(new int[]{0, 1});
         sut.setStructureTree(treeModel);
 
         sut.showBranchDialog(true);
         assertTrue(treeModel.getSelection().isEmpty());
         assertTrue(sut.getBranchDialog().getSectionList().isSelected(selectedSection));
         assertTrue(sut.getBranchDialog().isShowDialog());
         assertTrue(sut.getSelectedItem().isBranch());
         assertEquals(sut.getSelectedItem().getItem().getId(), 0);
     }
 
     @Test(dataProvider = "provideTreeModelWithSectionsAndBranches", enabled = false)
     public void testCloseDialogWithBranch(ForumStructureTreeModel treeModel) throws Exception {
         treeModel.addSelectionPath(new int[]{0});
         sut.setStructureTree(treeModel);
         sut.showBranchDialog(true);
         sut.closeDialog();
         assertFalse(sut.getBranchDialog().isShowDialog());
     }
 
     @Test
     public void testIsShowSectionDialog() throws Exception {
         sut.setSelectedItem(new ForumStructureItem(new PoulpeSection()));
         sut.showSectionDialog(false);
         assertTrue(sut.isShowSectionDialog());
         assertFalse(sut.isShowSectionDialog());
     }
 
     @Test(dataProvider = "provideTreeModelWithSectionsAndBranches")
     public void testCloseDialogWithSection(ForumStructureTreeModel treeModel) throws Exception {
         treeModel.addSelectionPath(new int[]{0});
         sut.setStructureTree(treeModel);
         sut.showSectionDialog(true);
         sut.closeDialog();
         assertFalse(sut.isShowSectionDialog());
     }
 
     @Test
     public void testSetSectionTree() throws Exception {
         ForumStructureTreeModel treeModel = mock(ForumStructureTreeModel.class);
         sut.setStructureTree(treeModel);
         verify(branchEditingDialog).renewSectionsFromTree(treeModel);
     }
 
     @DataProvider
     public Object[][] provideTreeModelWithSectionsAndBranches() {
         Jcommune jcommune = TestFixtures.jcommuneWithSections();
         return new Object[][] { { new ForumStructureTreeModel(buildForumStructure(jcommune)) } };
     }
 
     @DataProvider
     public Object[][] provideSutWithSpiedBranchDialog() {
         BranchEditingDialog branchDialog = new BranchEditingDialog(mock(GroupDao.class));
         BranchEditingDialog spy = spy(branchDialog);
         ForumStructureData data = new ForumStructureData(spy);
         data.setStructureTree((ForumStructureTreeModel) provideTreeModelWithSectionsAndBranches()[0][0]);
         return new Object[][] { { data } };
     }
 
 
     /**
      * Provides the {@link ForumStructureData} with the tree already set.
      *
      * @return the {@link ForumStructureData} with the tree already set
      */
     @DataProvider
     public Object[][] provideSutWithTree() {
         ForumStructureData data = new ForumStructureData(mock(BranchEditingDialog.class));
         data.setStructureTree((ForumStructureTreeModel) provideTreeModelWithSectionsAndBranches()[0][0]);
         return new Object[][]{{data}};
     }
 
     /**
      * Provides the {@link ForumStructureData} with the tree set to mock.
      *
      * @return the {@link ForumStructureData} with the tree set to mock
      */
     @DataProvider
     public Object[][] provideSutWithMockedTree() {
         ForumStructureData data = new ForumStructureData(mock(BranchEditingDialog.class));
         data.setStructureTree(mock(ForumStructureTreeModel.class));
         return new Object[][]{{data}};
     }
 
     /**
      * Creates a {@link ForumStructureItem} with branch as the entity inside and sets it to the specified {@code sut}.
      *
      * @param sut the newly created item will be set here
      * @return the created item
      */
     private ForumStructureItem givenSelectedItemAsBranch(ForumStructureData sut) {
         ForumStructureItem selectedItem = new ForumStructureItem(new PoulpeBranch());
         sut.setSelectedItem(selectedItem);
         return selectedItem;
     }
 }
