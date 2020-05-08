 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
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
 package com.flexive.war.beans.admin.main;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.faces.model.FxJSFSelectItem;
 import com.flexive.shared.*;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.FxEntryInUseException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.structure.FxSelectList;
 import com.flexive.shared.structure.FxSelectListEdit;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.structure.FxSelectListItemEdit;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.faces.event.ActionEvent;
 import javax.faces.model.SelectItem;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Bean to display and edit FxSelectList objects and FxSelectListItem objects
  *
  * @author Gerhard Glos (gerhard.glos@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 public class SelectListBean implements Serializable {
     private static final long serialVersionUID = -5927666279497485356L;
 
     private static final Log LOG = LogFactory.getLog(SelectListBean.class);
 
     //used to filter out the select lists with id's 0 up to DELIMITER and prevent them from being edited
     private static final long SYSTEM_INTERNAL_LISTS_DELIMITER = 0;
     private final static long UNSELECTED_ID = -100000L;
     private FxSelectListEdit selectList = null;
     private long selectListId = UNSELECTED_ID;
     private String selectListName = null;
     private FxString selectListLabel = new FxString("");
     private FxString selectListDescription = new FxString("");
     private boolean selectListAllowDynamicCreation = true;
     private String selectListBreadcrumbSeparator = " > ";
     private boolean selectListOnlySameLevelSelect = false;
 
     private long listItemId = UNSELECTED_ID;
     private String itemName = null;
     private FxString itemLabel = new FxString("");
     private ACL itemACL = null;
     private String itemData = null;
     private String itemColor = FxFormatUtils.DEFAULT_COLOR;
 
     private int rowsPerPage = 10;
     private int currentPage = 1;
     private long editListItemId = UNSELECTED_ID;
     private long moveListItemId = UNSELECTED_ID;
     private boolean editNew = false;
     Map<Long, Long> originalParents = null;
     private FxSelectListItemEdit editListItem = null;
 
     private FxSharedUtils.ItemPositionSorter sorter = new FxSharedUtils.ItemPositionSorter();
 
     //for new lists
     private long newCreateItemACL;
     private long newDefaultItemACL;
 
     public int getRowsPerPage() {
         return rowsPerPage;
     }
 
     public void setRowsPerPage(int rowsPerPage) {
         this.rowsPerPage = rowsPerPage;
     }
 
     public int getCurrentPage() {
         return currentPage;
     }
 
     public void setCurrentPage(int currentPage) {
         this.currentPage = currentPage;
     }
 
     /**
      * hack to generate unique id for the UI delete button, which can be used in java script
      *
      * @return unique id string for delete button
      */
     public Map<Long, String> getIdMap() {
         return FxSharedUtils.getMappedFunction(new FxSharedUtils.ParameterMapper<Long, String>() {
             public String get(Object key) {
                 long id = (Long) key;
                 if (id >= 0)
                     return String.valueOf(id);
                 else
                     return "N" + String.valueOf(id * -1);
             }
         });
     }
 
     public long getUnselectedId() {
         return UNSELECTED_ID;
     }
 
     public FxString getItemLabel() {
         return itemLabel;
     }
 
     public void setItemLabel(FxString itemLabel) {
         this.itemLabel = itemLabel;
     }
 
     public String getItemName() {
         return itemName;
     }
 
     public void setItemName(String itemName) {
         if (itemName != null)
             itemName = itemName.trim();
         this.itemName = itemName;
     }
 
     public ACL getItemACL() {
         return itemACL;
     }
 
     public void setItemACL(ACL itemACL) {
         this.itemACL = itemACL;
     }
 
     public String getItemData() {
         return itemData;
     }
 
     public void setItemData(String itemData) {
         this.itemData = itemData;
     }
 
     public String getItemColor() {
         return itemColor;
     }
 
     public void setItemColor(String itemColor) {
         this.itemColor = itemColor;
     }
 
     public long getListItemId() {
         return listItemId;
     }
 
     public void setListItemId(long listItemId) {
         this.listItemId = listItemId;
     }
 
     public long getSelectListId() {
         return selectListId;
     }
 
     public void setSelectListId(long selectListId) {
         this.selectListId = selectListId;
     }
 
     public String getSelectListName() {
         return selectListName;
     }
 
     public void setSelectListName(String selectListName) {
         this.selectListName = selectListName;
     }
 
     public FxString getSelectListLabel() {
         return selectListLabel;
     }
 
     public void setSelectListLabel(FxString selectListLabel) {
         this.selectListLabel = selectListLabel;
     }
 
     public FxString getSelectListDescription() {
         return selectListDescription;
     }
 
     public void setSelectListDescription(FxString selectListDescription) {
         this.selectListDescription = selectListDescription;
     }
 
     public boolean isSelectListAllowDynamicCreation() {
         return selectListAllowDynamicCreation;
     }
 
     public void setSelectListAllowDynamicCreation(boolean selectListAllowDynamicCreation) {
         this.selectListAllowDynamicCreation = selectListAllowDynamicCreation;
     }
 
     public long getSelectListCreateItemACL() {
         return selectList.getCreateItemACL().getId();
     }
 
     public void setSelectListCreateItemACL(long id) {
         selectList.setCreateItemACL(CacheAdmin.getEnvironment().getACL(id));
     }
 
     public long getSelectListDefaultItemACL() {
         return selectList.getNewItemACL().getId();
     }
 
     public void setSelectListDefaultItemACL(long id) {
         selectList.setNewItemACL(CacheAdmin.getEnvironment().getACL(id));
     }
 
     public long getNewCreateItemACL() {
         return newCreateItemACL;
     }
 
     public void setNewCreateItemACL(long newCreateItemACL) {
         this.newCreateItemACL = newCreateItemACL;
     }
 
     public long getNewDefaultItemACL() {
         return newDefaultItemACL;
     }
 
     public void setNewDefaultItemACL(long newDefaultItemACL) {
         this.newDefaultItemACL = newDefaultItemACL;
     }
 
     public String getSelectListBreadcrumbSeparator() {
         return selectListBreadcrumbSeparator;
     }
 
     public void setSelectListBreadcrumbSeparator(String selectListBreadcrumbSeparator) {
         this.selectListBreadcrumbSeparator = selectListBreadcrumbSeparator;
     }
 
     public boolean isSelectListOnlySameLevelSelect() {
         return selectListOnlySameLevelSelect;
     }
 
     public void setSelectListOnlySameLevelSelect(boolean selectListOnlySameLevelSelect) {
         this.selectListOnlySameLevelSelect = selectListOnlySameLevelSelect;
     }
 
 
     public long getMoveListItemId() {
         return moveListItemId;
     }
 
     public void setMoveListItemId(long moveListItemId) {
         this.moveListItemId = moveListItemId;
     }
 
     public FxSelectListEdit getSelectList() {
         return selectList;
     }
 
     public void setEditListItemId(long editListItemId) {
         this.editListItemId = editListItemId;
     }
 
     public long getEditListItemId() {
         return editListItemId;
     }
 
     public void editListItem(ActionEvent event) {
         editListItem = selectList.getItem(editListItemId).asEditable();
         originalParents = new HashMap<Long, Long>(editListItem.getChildCount());
         editNew = false;
         for (FxSelectListItem child : editListItem.getChildren())
             originalParents.put(child.getId(), child.getParentItem().getId());
     }
 
     public void createListItem(ActionEvent event) {
         editListItem = FxSelectListItemEdit.createNew(null, selectList.getNewItemACL(), selectList, new FxString(true, ""), null, FxFormatUtils.DEFAULT_COLOR);
         editListItemId = editListItem.getId();
         editNew = true;
     }
 
     public void commitItemEditing(ActionEvent event) {
         editListItemId = UNSELECTED_ID;
         editListItem = null;
         originalParents = null;
         editNew = false;
     }
 
     public void cancelItemEditing(ActionEvent event) throws FxInvalidParameterException {
         if (editNew) {
             selectList.removeItem(editListItemId);
             editNew = false;
         }
         if (!editListItem.isNew()) {
             if (editListItem.getHasChildren()) {
                 for (FxSelectListItem child : editListItem.getChildren())
                     child.asEditable().setParentItem(null);
             }
             editListItem.resetChanges();
             selectList.replaceItem(editListItem.getId(), editListItem);
             //restore child items
             if (originalParents != null) {
                 for (Long itemId : originalParents.keySet())
                     selectList.getItem(itemId).asEditable().setParentItem(selectList.getItem(originalParents.get(itemId)));
                 originalParents = null;
             }
         }
         editListItemId = UNSELECTED_ID;
         editListItem = null;
         originalParents = null;
     }
 
     public void moveItemUp(ActionEvent event) throws FxInvalidParameterException {
         selectList.moveItemUp(moveListItemId);
     }
 
     public void moveItemDown(ActionEvent event) throws FxInvalidParameterException {
         selectList.moveItemDown(moveListItemId);
     }
 
     public FxSelectListItemEdit getEditListItem() {
         return editListItem;
     }
 
     public Long[] getEditListItemChildren() {
         if (editListItem == null)
             return new Long[0];
         List<Long> result = new ArrayList<Long>(20);
         for (FxSelectListItem assigned : editListItem.getChildren())
             result.add(assigned.getId());
         return FxArrayUtils.toLongArray(result);
     }
 
     public void setEditListItemChildren(Long[] children) throws FxInvalidParameterException {
         for (FxSelectListItem child : selectList.getChildItems(editListItem.getId()))
             child.asEditable().setParentItem(null);
         for (Long id : children) {
             FxSelectListItem child = selectList.getItem(id);
             child.asEditable().setParentItem(editListItem);
         }
     }
 
     public List<SelectItem> getAssignableEditListItemChildren() {
         if (editListItem == null)
             return new ArrayList<SelectItem>(0);
         List<SelectItem> result = new ArrayList<SelectItem>(20);
         for (FxSelectListItem assignable : editListItem.getList().getItems())
             if (editListItem.isAssignable(assignable))
                 result.add(new FxJSFSelectItem(assignable).forceDisplay(true));
         return result;
     }
 
     /**
      * filters out select list items which the current user is not allowed to see (==user doesn't have read permission)
      * and sorts them by id.
      *
      * @return filtered select list items sorted by id.
      */
     public List<FxSelectListItemEdit> getItems() {
         List<FxSelectListItemEdit> items = new ArrayList<FxSelectListItemEdit>();
         for (FxSelectListItemEdit i : selectList.getEditableItems())
             if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor) ||
                     FxJsfUtils.getRequest().getUserTicket().mayReadACL(i.getAcl().getId(), FxJsfUtils.getRequest().getUserTicket().getUserId()))
                 items.add(i);
         Collections.sort(items, sorter);
         return items;
     }
 
     /**
      * Map containing boolean values, if the current user may create select list items
      * for a given select list (id of the select list is used as key).
      *
      * @return Map containing id's of select lists as keys and if the current user may create
      *         select list items as values.
      */
     public Map<Long, Boolean> getMayCreateItems() {
         return new HashMap<Long, Boolean>() {
             public Boolean get(Object key) {
                 return FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor) ||
                         FxJsfUtils.getRequest().getUserTicket().
                                 mayCreateACL(CacheAdmin.getEnvironment().getSelectList((Long) key).getCreateItemACL().getId(), FxJsfUtils.getRequest().getUserTicket().getUserId());
             }
         };
     }
 
     public boolean getMayDeleteItems() {
         return FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor)
                 || FxJsfUtils.getRequest().getUserTicket().mayDeleteACL(
                 selectList.getCreateItemACL().getId(), FxJsfUtils.getRequest().getUserTicket().getUserId());
     }
 
     /**
      * Returns if the current user may edit a specific select list item.
      *
      * @return if the current user may edit a specific select list item
      */
     public Map<FxSelectListItemEdit, Boolean> getMayEditItem() {
         return new HashMap<FxSelectListItemEdit, Boolean>() {
             public Boolean get(Object key) {
                 try {
                     return FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor) ||
                             FxJsfUtils.getRequest().getUserTicket().
                                     mayEditACL(((FxSelectListItemEdit) key).getAcl().getId(), FxJsfUtils.getRequest().getUserTicket().getUserId());
                 } catch (Exception e) {
                     LOG.error(e);
                     return false;
                 }
             }
         };
     }
 
     /**
      * Returns all available select item acl's.
      *
      * @return all available select list item acl's
      */
 
     public List<SelectItem> getSelectListItemACLs() {
         return FxJsfUtils.asSelectListWithLabel(CacheAdmin.getEnvironment().getACLs(ACLCategory.SELECTLISTITEM));
     }
 
 
     public List<FxSelectList> getSelectLists() {
         return doFilter(CacheAdmin.getEnvironment().getSelectLists());
     }
 
     private List<FxSelectList> doFilter(List<FxSelectList> selectLists) {
         List<FxSelectList> filtered = new ArrayList<FxSelectList>();
         for (FxSelectList s : selectLists) {
             if (s.getId() < 0 || s.getId() > SYSTEM_INTERNAL_LISTS_DELIMITER)
                 filtered.add(s);
         }
         return filtered;
     }
 
     public String showSelectListOverview() {
         reset();
         return "selectListOverview";
     }
 
     /**
      * Function is called ONLY from create.xhtml hence it can be also
      * used to check permissions
      *
      * @return navigation outcome
      */
     public String getResetSelectList() {
         reset();
         if (!FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor))
             //noinspection ThrowableInstanceNeverThrown
             new FxFacesMsgErr(new FxNoAccessException("ex.role.notInRole", Role.SelectListEditor.getName())).addToContext();
         return null;
     }
 
     public String showCreateSelectList() {
         reset();
         return "createSelectList";
     }
 
     public String showEditSelectList() {
         return "editSelectList";
     }
 
     private void reset() {
         selectListName = null;
         selectListLabel = new FxString("");
         selectListDescription = new FxString("");
         selectListAllowDynamicCreation = true;
         selectListBreadcrumbSeparator = " > ";
         selectListOnlySameLevelSelect = false;
         itemLabel = new FxString("");
         itemName = null;
         itemACL = CacheAdmin.getEnvironment().getACL(ACLCategory.SELECTLISTITEM.getDefaultId());
         itemData = null;
         itemColor = FxFormatUtils.DEFAULT_COLOR;
         editListItemId = UNSELECTED_ID;
         editListItem = null;
     }
 
     public String createSelectList() {
         try {
             FxPermissionUtils.checkRole(FxJsfUtils.getRequest().getUserTicket(), Role.SelectListEditor);
             FxSelectListEdit list = FxSelectListEdit.createNew(selectListName.trim(), selectListLabel,
                     selectListDescription, selectListAllowDynamicCreation,
                     CacheAdmin.getEnvironment().getACL(newCreateItemACL),
                     CacheAdmin.getEnvironment().getACL(newDefaultItemACL));
             list.setBreadcrumbSeparator(selectListBreadcrumbSeparator);
             list.setOnlySameLevelSelect(selectListOnlySameLevelSelect);
             selectListId = EJBLookup.getSelectListEngine().save(list);
             reset();
             return initEditing();
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return null;
         }
     }
 
     public String initEditing() {
         selectList = CacheAdmin.getEnvironment().getSelectList(selectListId).asEditable();
         setSelectListAllowDynamicCreation(selectList.isAllowDynamicItemCreation());
         setSelectListDescription(selectList.getDescription() == null ? new FxString("") : selectList.getDescription());
         setSelectListLabel(selectList.getLabel() == null ? new FxString("") : selectList.getLabel());
         setSelectListName(selectList.getName());
         setSelectListBreadcrumbSeparator(selectList.getBreadcrumbSeparator());
         setSelectListOnlySameLevelSelect(selectList.isOnlySameLevelSelect());
         setItemACL(selectList.getNewItemACL());
 
         itemName = null;
         itemLabel = new FxString("");
         itemACL = selectList.getNewItemACL();
         itemData = null;
         itemColor = FxFormatUtils.DEFAULT_COLOR;
 
         editListItemId = UNSELECTED_ID;
         editListItem = null;
 
         return showEditSelectList();
     }
 
     public void deleteSelectList() {
         try {
             FxPermissionUtils.checkRole(FxJsfUtils.getRequest().getUserTicket(), Role.SelectListEditor);
             EJBLookup.getSelectListEngine().remove(CacheAdmin.getEnvironment().getSelectList(selectListId));
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
    public void deleteListItem() {
         try {
             //check if the user has permission
             if (getMayDeleteItems()) {
                 //check if the item is used in content instances
                 if (EJBLookup.getSelectListEngine().getSelectListItemInstanceCount(listItemId) != 0)
                     throw new FxEntryInUseException("ex.selectlist.item.itemInUse", selectList.getItem(listItemId).getLabel());
 
                 selectList.removeItem(listItemId);
                FxJsfUtils.resetFaceletsComponent("frm");
             } else
                 throw new FxNoAccessException("ex.selectlist.item.remove.noPerm", selectList.getLabel(), selectList.getCreateItemACL().getLabel());
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public void addListItem() {
         try {
             //check if the user has permission
             if (getMayCreateItems().get(getSelectListId())) {
                 //check for empty label
                 if (itemLabel.getIsEmpty())
                     throw new FxInvalidParameterException("Label", "ex.selectlist.item.label.empty");
 
                 //convert color string to uppercase
                 if (itemColor != null)
                     itemColor = itemColor.toUpperCase();
 
                 new FxSelectListItemEdit(itemName, itemACL, selectList, itemLabel, itemData, FxFormatUtils.processColorString("Color", itemColor));
                 itemName = null;
                 itemLabel = new FxString(true, "");
                 itemACL = selectList.getNewItemACL();
                 itemData = null;
                 itemColor = FxFormatUtils.DEFAULT_COLOR;
                 FxJsfUtils.resetFaceletsComponent("frm");
             }
             //else provoke exception
             else
                 throw new FxNoAccessException("ex.selectlist.item.create.noPerm", selectList.getLabel(), selectList.getCreateItemACL().getLabel());
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public Map<String, String> getColor() {
         return new HashMap<String, String>() {
             public String get(Object key) {
                 String RGBCode = null;
                 try {
                     RGBCode = FxFormatUtils.processColorString("color", (String) key);
                 }
                 catch (Exception e) {
                     //exception is ok, original values are used
                 }
                 return (RGBCode == null ? (String) key : RGBCode);
             }
         };
     }
 
     public String saveSelectList() {
         try {
             //set and check default colors if the user has permission,
             // if not just store them to DB as set
             for (FxSelectListItemEdit i : selectList.getEditableItems()) {
                 if (getMayEditItem().get(i))
                     i.setColor(FxFormatUtils.processColorString("Color", i.getColor()));
             }
             if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor)) {
                 selectList.setName(selectListName);
                 selectList.setLabel(selectListLabel);
                 selectList.setDescription(selectListDescription);
                 selectList.setAllowDynamicItemCreation(selectListAllowDynamicCreation);
                 selectList.setBreadcrumbSeparator(selectListBreadcrumbSeparator);
                 selectList.setOnlySameLevelSelect(selectListOnlySameLevelSelect);
             }
             EJBLookup.getSelectListEngine().save(selectList);
             reset();
             new FxFacesMsgInfo("SelectList.nfo.saved").addToContext();
             return initEditing();
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return initEditing();
         }
     }
 }
