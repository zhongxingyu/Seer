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
 
 import org.jtalks.poulpe.model.entity.Jcommune;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.service.ComponentService;
 import org.jtalks.poulpe.service.ForumStructureService;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
 import org.jtalks.poulpe.web.controller.branch.BranchPermissionManagementVm;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.GlobalCommand;
 import org.zkoss.bind.annotation.Init;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.zk.ui.event.DropEvent;
 import org.zkoss.zul.TreeNode;
 import org.zkoss.zul.Treeitem;
 
 import javax.annotation.Nonnull;
 import java.util.List;
 
 import static org.jtalks.poulpe.web.controller.section.TreeNodeFactory.buildForumStructure;
 
 /**
  * Is used in order to work with page that allows admin to manage sections and branches (moving them, reordering,
  * removing, editing, etc.). Note, that this class is responsible for back-end of the operations (presenter,
  * controller), so it stores all the changes to the database using {@link ComponentService}.
  *
  * @author stanislav bashkirtsev
  * @author Guram Savinov
  */
 public class ForumStructureVm {
    private static final String SELECTED_ITEM_PROP = "selectedItemInTree", VIEW_DATA_PROP = "viewData",
            TREE_MODEL = "treeModel";
     private final ForumStructureService forumStructureService;
     private final WindowManager windowManager;
     private ForumStructureItem selectedItemInTree = new ForumStructureItem(null);
     private SelectedEntity<PoulpeBranch> selectedBranchForPermissions;
     private ForumStructureTreeModel treeModel;
 
     public ForumStructureVm(@Nonnull ForumStructureService forumStructureService, @Nonnull WindowManager windowManager,
                             @Nonnull SelectedEntity<PoulpeBranch> selectedBranchForPermissions) {
         this.forumStructureService = forumStructureService;
         this.windowManager = windowManager;
         this.selectedBranchForPermissions = selectedBranchForPermissions;
     }
 
     /**
      * Creates the whole sections and branches structure. Always hits database. Is executed each time a page is
      * opening.
      */
     @Init
     public void init() {
         treeModel = new ForumStructureTreeModel(buildForumStructure(loadJcommune()));
     }
 
     @GlobalCommand
     @NotifyChange({TREE_MODEL, SELECTED_ITEM_PROP})
     public void refreshTree() {
     }
 
     /**
      * Opens a separate page - Branch Permissions where admin can edit what Groups have wha Permissions on the selected
      * branch.
      */
     @Command
     public void openBranchPermissions() {
         selectedBranchForPermissions.setEntity(getSelectedItemInTree().getBranchItem());
         BranchPermissionManagementVm.showPage(windowManager);
     }
 
     public void removeBranchFromTree(PoulpeBranch branch) {
         treeModel.removeBranch(branch);
     }
 
     public void removeSectionFromTree(PoulpeSection section) {
         treeModel.removeSection(section);
     }
 
     public void updateBranchInTree(PoulpeBranch branch) {
         treeModel.moveBranchIfSectionChanged(branch);
         selectedItemInTree = new ForumStructureItem(branch);
     }
 
     public void updateSectionInTree(PoulpeSection section) {
         treeModel.addIfAbsent(section);
         selectedItemInTree = new ForumStructureItem(section);
     }
 
     public ForumStructureItem getSelectedItemInTree() {
         return selectedItemInTree;
     }
 
     /**
      * Is used by ZK binder to inject the section that is currently selected.
      *
      * @param selectedNode the section that is currently selected
      */
     @NotifyChange(SELECTED_ITEM_PROP)
     public void setSelectedNode(TreeNode<ForumStructureItem> selectedNode) {
         this.selectedItemInTree = selectedNode.getData();
     }
 
     /**
      * Loads instance of JCommune from database.
      *
      * @return instance of JCommune from database
      */
     private Jcommune loadJcommune() {
         Jcommune jcommune = forumStructureService.getJcommune();
         if (jcommune == null) {
             throw new IllegalStateException("Please, create a Forum Component first.");
         }
         return jcommune;
     }
 
     /**
      * Handler of event when one item was dragged and dropped to another.
      *
      * @param event contains all needed info about event
      */
     @Command
    @NotifyChange(VIEW_DATA_PROP)
     public void onDropItem(@BindingParam("event") DropEvent event) {
         TreeNode<ForumStructureItem> draggedNode = ((Treeitem) event.getDragged()).getValue();
         TreeNode<ForumStructureItem> targetNode = ((Treeitem) event.getTarget()).getValue();
         ForumStructureItem draggedItem = draggedNode.getData();
         if (draggedItem.isBranch()) {
             onDropBranch(draggedNode, targetNode);
         } else if (draggedItem.isSection()) {
             onDropSection(draggedNode, targetNode);
         }
 
     }
 
     /**
      * Handler of event when branch node was dragged and dropped to another node.
      * 
      * @param draggedNode the node represents dragged branch item
      * @param targetNode the node represents target item (it can be branch or section item)
      */
     private void onDropBranch(TreeNode<ForumStructureItem> draggedNode,
             TreeNode<ForumStructureItem> targetNode) {
         ForumStructureItem targetItem = targetNode.getData();
         PoulpeBranch draggedBranch = draggedNode.getData().getBranchItem();
         if (noEffectAfterDropBranch(draggedBranch, targetItem)) {
             return;
         }
         if (targetItem.isBranch()) {
             forumStructureService.moveBranch(draggedBranch, targetItem.getBranchItem());
             treeModel.dropNodeBefore(draggedNode, targetNode);
             treeModel.setSelectedNode(draggedNode);
         }
         else {
             forumStructureService.moveBranch(draggedBranch, targetItem.getSectionItem());
             treeModel.dropNodeIn(draggedNode, targetNode);
             treeModel.setSelectedNode(draggedNode);
         }
     }
 
     /**
      * Handler of event when section node was dragged and dropped to another section node.
      * 
      * @param draggedNode the node represents dragged section item
      * @param targetNode the node represents target section item
      */
     private void onDropSection(TreeNode<ForumStructureItem> draggedNode,
             TreeNode<ForumStructureItem> targetNode) {
         ForumStructureItem draggedItem = draggedNode.getData();
         ForumStructureItem targetItem = targetNode.getData();
         if (noEffectAfterDropSection(draggedItem, targetItem)) {
             return;
         }
         
         PoulpeSection draggedSection = draggedItem.getSectionItem();
         PoulpeSection targetSection = targetItem.getSectionItem();
         getRootAsJcommune().moveSection(draggedSection, targetSection);
         forumStructureService.saveJcommune(getRootAsJcommune());
         treeModel.dropNodeBefore(draggedNode, targetNode);
         treeModel.setSelectedNode(draggedNode);
     }
 
     /**
      * Checks that dropping branch haven't effect.
      *
      * @param draggedBranch the branch to move
      * @param targetItem    the target item, may be branch as well as section
      * @return {@code true} if dropping have no effect, otherwise return {@code false}
      */
     private boolean noEffectAfterDropBranch(PoulpeBranch draggedBranch, ForumStructureItem targetItem) {
         PoulpeSection draggedSection = draggedBranch.getPoulpeSection();
         if (targetItem.isSection()) {
             return draggedSection.equals(targetItem.getSectionItem());
         }
 
         PoulpeBranch targetBranch = targetItem.getBranchItem();
         PoulpeSection targetSection = targetBranch.getPoulpeSection();
         if (draggedSection.equals(targetSection)) {
             List<PoulpeBranch> branches = draggedSection.getPoulpeBranches();
             int draggedIndex = branches.indexOf(draggedBranch);
             int targetIndex = branches.indexOf(targetBranch);
             if (targetIndex - 1 == draggedIndex) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Checks that dropping section haven't effect.
      *
      * @param draggedItem the section item to move
      * @param targetItem  the target section item
      * @return {@code true} if dropping have no effect, otherwise return {@code false}
      */
     private boolean noEffectAfterDropSection(ForumStructureItem draggedItem, ForumStructureItem targetItem) {
         PoulpeSection draggedSection = draggedItem.getSectionItem();
         List<PoulpeSection> sections = getRootAsJcommune().getSections();
         int draggedIndex = sections.indexOf(draggedSection);
         int targetIndex = sections.indexOf(targetItem.getSectionItem());
         return targetIndex - 1 == draggedIndex;
     }
 
     public ForumStructureTreeModel getTreeModel() {
         return treeModel;
     }
 
     /**
      * Since the {@link Jcommune} object is the root of the Forum Structure tree, this method can fetch this object from
      * the tree and return it.
      *
      * @return the {@link Jcommune} object that is the root of the Forum Structure tree
      */
     public Jcommune getRootAsJcommune() {
         return (Jcommune) (Object) treeModel.getRoot().getData();
     }
 
     public void setTreeModel(ForumStructureTreeModel treeModel) {
         this.treeModel = treeModel;
     }
 }
