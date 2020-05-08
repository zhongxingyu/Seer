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
 package org.jtalks.poulpe.web.controller.section.dialogs;
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.service.ForumStructureService;
 import org.jtalks.poulpe.service.GroupService;
 import org.jtalks.poulpe.web.controller.section.ForumStructureTreeModel;
 import org.jtalks.poulpe.web.controller.section.ForumStructureVm;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.GlobalCommand;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.zul.ListModelList;
 
 import javax.annotation.Nonnull;
 import javax.validation.constraints.NotNull;
 import java.util.List;
 
 /**
  * View model that manages branch editing dialog and its data (like section list in dropdown).
  *
  * @author stanislav bashkirtsev
  */
 public class BranchEditingDialog {
     private static final String SHOW_DIALOG = "showDialog", EDITED_BRANCH = "editedBranch";
     private static final String MODERATING_GROUP = "moderatingGroup", CANDIDATES_TO_MODERATE = "candidatesToModerate";
     private final GroupService groupService;
     private final ListModelList<PoulpeSection> sectionList = new ListModelList<PoulpeSection>();
     private final GroupList groupList = new GroupList();
     private final ForumStructureVm forumStructureVm;
     private final ForumStructureService forumStructureService;
     private PoulpeBranch editedBranch = new PoulpeBranch();
     private boolean showDialog;
 
     public BranchEditingDialog(GroupService groupService, ForumStructureVm forumStructureVm,
                                ForumStructureService forumStructureService) {
         this.groupService = groupService;
         this.forumStructureVm = forumStructureVm;
         this.forumStructureService = forumStructureService;
     }
 
     @GlobalCommand
     @NotifyChange({SHOW_DIALOG, EDITED_BRANCH, MODERATING_GROUP, CANDIDATES_TO_MODERATE})
     public void showBranchDialog(@BindingParam("selectedBranch") PoulpeBranch selectedBranch) {
         showDialog(selectedBranch);
     }
 
     @GlobalCommand
     @NotifyChange({SHOW_DIALOG, EDITED_BRANCH, MODERATING_GROUP, CANDIDATES_TO_MODERATE})
     public void showCreateBranchDialog() {
         showDialog(new PoulpeBranch());
     }
 
     @Command
     @NotifyChange(SHOW_DIALOG)
     public void saveBranch() {
         PoulpeBranch updatedBranch = storeSelectedBranch();
         forumStructureVm.updateBranchInTree(updatedBranch);
         closeDialog();
     }
 
     /**
      * Closes the dialog without removing any underlying state.
      */
     public void closeDialog() {
         showDialog = false;
     }
 
     private void showDialog(PoulpeBranch editedBranch) {
         groupList.setGroups(groupService.getAll());
         this.editedBranch = editedBranch;
         ForumStructureTreeModel treeModel = forumStructureVm.getTreeModel();
         renewSectionsFromTree(treeModel);
         selectSection(treeModel.getSelectedSection());
         showDialog = true;
     }
 
     /**
      * By this flag the ZUL decides whether to show the branch editing dialog or not.
      *
      * @return whether to show the branch editing dialog
      */
     public boolean isShowDialog() {
         boolean result = showDialog;
         showDialog = false;
         return result;
     }
 
     public void selectSection(PoulpeSection section) {
         sectionList.addToSelection(section);
     }
 
     /**
      * Gets the list of available sections so that it's possible to place the branch to some other section.
      *
      * @return the list of available sections so that it's possible to place the branch to some other section
      */
     public ListModelList<PoulpeSection> getSectionList() {
         return sectionList;
     }
 
     /**
      * Clears the previous sections and gets the new ones from the specified tree.
      *
      * @param forumTree a forum structure tree to get sections from it
      */
     void renewSectionsFromTree(@Nonnull ForumStructureTreeModel forumTree) {
         this.sectionList.clear();
         this.sectionList.addAll(forumTree.getSections());
     }
 
     public PoulpeBranch getEditedBranch() {
         return editedBranch;
     }
 
     PoulpeBranch storeSelectedBranch() {
         PoulpeSection section = sectionList.getSelection().iterator().next();
         return forumStructureService.saveBranch(section, editedBranch);
     }
 
     /**
      * Gets a list of all the groups in the database so that user can choose another moderating group from the list.
      *
      * @return a list of all the groups in the database
      */
     public List<Group> getCandidatesToModerate() {
         return groupList.getGroups();
     }
 
     /**
      * Gets the group that is equal to the one that is currently moderating the selected branch. A new group with the
      * empty fields will be created if there is no moderating group of the branch (it's a new branch). Note, that this
      * method will be used by ZK in order to identify currently selected item in Combo Box, which means that it doesn't
      * actually need a real object to be returned, but it will be enough if we return an equal object (in our case
      * equals means they have the same UUID). Due to this and due to the problem with ZK binding (Hibernate will return
      * a proxy here, but method {@link #getCandidatesToModerate()} returns non-proxies, and when ZK tries to set the
      * value, it throws a class cast exception because proxy != a usual class instance). That's why this method returns
      * an instance that is equal to the real moderating group, not the actual one.
      *
      * @return the group that is equal to the one that is currently moderating the selected branch
      */
     @NotNull(message = "{branch.moderating_group.not_null_constraint}")
     public Group getModeratingGroup() {
         Group currentModeratorsGroup = editedBranch.getModeratorsGroup();
         return groupList.getEqual(currentModeratorsGroup);
     }
 
     /**
      * Sets the moderating group to the branch after user changed it in the dialog's list.
      *
      * @param moderatingGroup a new moderating group to set to the branch (or the old one, but this doesn't change
      *                        anything)
      */
     public void setModeratingGroup(Group moderatingGroup) {
         editedBranch.setModeratorsGroup(moderatingGroup);
     }
 }
