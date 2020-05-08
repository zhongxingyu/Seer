 package org.jtalks.poulpe.web.controller.group;
 
 import java.util.List;
 
 import org.jtalks.poulpe.model.entity.Group;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Components;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.ext.AfterCompose;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listcell;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.ListitemRenderer;
 import org.zkoss.zul.Textbox;
 import org.zkoss.zul.Window;
 
 /**
  * @author Konstantin Akimov
  */
 @SuppressWarnings("serial")
 public class GroupViewImpl extends Window implements AfterCompose {
 
     private GroupPresenter presenter;
 
     private Window editDialog;
 
     private Listbox groupsListbox;
     private ListModelList<Group> groupsListboxModel;
     private Textbox searchTextbox;
 
     public void setPresenter(GroupPresenter presenter) {
         this.presenter = presenter;
     }
 
     @Override
     public void afterCompose() {
         Components.addForwards(this, this);
         Components.wireVariables(this, this);
 
         groupsListbox.setItemRenderer(new ListitemRenderer<Group>() {
             @Override
             public void render(Listitem listItem, Group group) throws Exception {
                 new Listcell(group.getName()).setParent(listItem);
                 new Listcell("Not specified yet").setParent(listItem);
                 listItem.setId(String.valueOf(group.getId()));
             }
         });
 
         presenter.initView(this);
     }
 
     public void updateView(List<Group> groups) {
         groupsListboxModel = new ListModelList<Group>(groups);
         groupsListbox.setModel(groupsListboxModel);
     }
 
     public void onDoubleClick$groupsListbox() {
         presenter.onEditGroup(getSelectedGroup());
     }
 
     public void onClick$addButton() {
         presenter.onAddGroup();
     }
 
     public void onClick$removeButton() {
         presenter.deleteGroup(getSelectedGroup());
     }
 
     public void onSearchAction() {
         presenter.doSearch(searchTextbox.getText());
     }
 
     public void openNewDialog() {
         EditGroupDialogView component = getEditView();
         component.show();
     }
 
     private EditGroupDialogView getEditView() {
         EditGroupDialogViewImpl component = getEditDialogComponent();
         component.setAttribute("presenter", presenter);
         component.setAttribute("backWindow", this);
         return component;
     }
 
     private EditGroupDialogViewImpl getEditDialogComponent() {
         return (EditGroupDialogViewImpl) getDesktop().getPage("GroupDialog").getFellow("editWindow");
     }
 
     public void openEditDialog(Group group) {
        Component receiver = getDesktop().getPage("GroupDialog").getFellow("editWindow");
        Event onOpenEditDialog = new Event("onOpenEditDialog", receiver, group);
        Events.postEvent(onOpenEditDialog);
     }
 
     public void onHideDialog() {
         presenter.updateView();
     }
 
     public Group getSelectedGroup() {
         return (Group) groupsListboxModel.getElementAt(groupsListbox.getSelectedIndex());
     }
 
 }
