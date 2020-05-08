 package org.jtalks.poulpe.web.controller.section;
 
 import org.jtalks.poulpe.model.entity.Jcommune;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.zkoss.zul.DefaultTreeModel;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.TreeNode;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author stanislav bashkirtsev
  */
 public class ForumStructureData {
     private ListModelList<PoulpeSection> sectionList = new ListModelList<PoulpeSection>();
     private ForumStructureItem selectedItem = new ForumStructureItem();
     private DefaultTreeModel<ForumStructureItem> sectionTree;
     private boolean showSectionDialog;
     private boolean showBranchDialog;
     private boolean fromMenu;
     private ForumStructureItem prevSelectedItem;
 
     public Jcommune getRootAsJcommune() {
         return (Jcommune) (Object) sectionTree.getRoot().getData();
     }
 
     public PoulpeSection getSectionSelectedInDropDown() {
         Set<PoulpeSection> selection = sectionList.getSelection();
         if (selection.isEmpty()) {
             return null;
         }
         return selection.iterator().next();
     }
 
     public ForumStructureData closeDialog() {
         showSectionDialog = false;
         showBranchDialog = false;
         return this;
     }
 
     public ForumStructureData showBranchDialog(boolean createNew, boolean fromMenu) {
        prevSelectedItem = new ForumStructureItem();
        prevSelectedItem.setItem(selectedItem.getItem(PoulpeSection.class));
         if (createNew) {
             selectedItem.setItem(new PoulpeBranch());
         }
         showBranchDialog = true;
         this.fromMenu = fromMenu;
         return this;
     }
 
     public ForumStructureData showSectionDialog(boolean createNew) {
         if (createNew) {
             selectedItem.setItem(new PoulpeSection());
         }
         showSectionDialog = true;
         return this;
     }
 
     public ListModelList<PoulpeSection> getSectionList() {
         return sectionList;
     }
 
     public ForumStructureItem getSelectedItem() {
         return selectedItem;
     }
 
     public void setSelectedItem(ForumStructureItem selectedItem) {
         this.selectedItem = selectedItem;
     }
 
     public boolean isShowBranchDialog() {
         boolean show = showBranchDialog && selectedItem.isBranch();
         this.showBranchDialog = false;
         return show;
     }
 
     public boolean isShowSectionDialog() {
         boolean show = showSectionDialog && !selectedItem.isBranch();
         this.showSectionDialog = false;
         return show;
     }
 
     public DefaultTreeModel<ForumStructureItem> getSectionTree() {
         return sectionTree;
     }
 
     public void setSectionTree(DefaultTreeModel<ForumStructureItem> sectionTree) {
         this.sectionTree = sectionTree;
         this.sectionList.clear();
         List<PoulpeSection> sections = unwrap(sectionTree.getRoot().getChildren());
         this.sectionList.addAll(sections);
         sectionList.clearSelection();
         if (fromMenu) {
             sectionList.addToSelection(prevSelectedItem.getItem(PoulpeSection.class));
         } else {
             sectionList.addToSelection(sections.get(0));
         }
     }
 
     private List<PoulpeSection> unwrap(List<TreeNode<ForumStructureItem>> sectionNodes) {
         List<PoulpeSection> sections = new ArrayList<PoulpeSection>();
         for (TreeNode<ForumStructureItem> sectionNode : sectionNodes) {
             sections.add(sectionNode.getData().getItem(PoulpeSection.class));
         }
         return sections;
     }
 }
