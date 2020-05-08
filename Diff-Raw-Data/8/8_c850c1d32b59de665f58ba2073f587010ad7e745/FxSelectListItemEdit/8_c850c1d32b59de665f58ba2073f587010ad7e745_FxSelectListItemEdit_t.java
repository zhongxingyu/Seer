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
 package com.flexive.shared.structure;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxString;
 
 import java.io.Serializable;
 
 /**
  * Editable select list item
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxSelectListItemEdit extends FxSelectListItem implements Serializable {
 
     private static final long serialVersionUID = -4068691002679713796L;
 
     /**
      * is this a new list item?
      */
     private boolean isNew;
     /**
      * The original item to compare for changes
      */
     private FxSelectListItem original;
 
     /**
      * Constructor to make an existing list item editable
      *
      * @param item the item to make editable
      */
     public FxSelectListItemEdit(FxSelectListItem item) {
         super(item.id, item.name, item.acl, item.list, item.parentItemId, item.label, item.data, item.color, item.iconId, item.iconVer, item.iconQuality, item.lifeCycleInfo, item.position);
         this.isNew = false;
         this.original = item;
         this.parentItem = item.parentItem;
     }
 
     /**
      * Internal constructor to make an existing list item editable and add it to an editable select list.
      *
      * @param item the item to make editable
      * @param list the new list to add the item to
      */
     FxSelectListItemEdit(FxSelectListItem item, FxSelectListEdit list) {
         super(item.id, item.name, item.acl, list, item.parentItemId, item.label, item.data, item.color, item.iconId, item.iconVer, item.iconQuality, item.lifeCycleInfo, item.position);
         this.isNew = false;
         this.original = item;
         this.parentItem = item.parentItem;
     }
 
     /**
      * Constructor for creating a new list item
      *
      * @param name  unique name of the item (within the list)
      * @param acl   the items ACL
      * @param list  the list this item belongs to
      * @param label this items label
      * @param data  optional data
      * @param color color for display
      */
     public FxSelectListItemEdit(String name, ACL acl, FxSelectList list, FxString label, String data, String color) {
         super(calcId(list), name, acl, list, -1, label, data, color, BinaryDescriptor.SYS_SELECTLIST_DEFAULT, 1, 1, null, list.getItemCount()+1);
         this.isNew = true;
         this.original = null;
     }
 
     /**
      * Calculate an unused id for the parent list to use for this item (for new items only)
      *
      * @param list parent list to check
      * @return calculated id
      */
     private static synchronized long calcId(FxSelectList list) {
         int curr = -1;
         while (list != null && list.containsItem(curr))
             curr--;
         return curr;
     }
 
     /**
      * Is this a new list item?
      *
      * @return is anew list item?
      */
     public boolean isNew() {
         return isNew;
     }
 
     /**
      * Have any changes been made?
      *
      * @return changes made?
      */
     public boolean changes() {
         return isNew || original == null || !this.name.equals(original.name) || !this.data.equals(original.data) || !this.color.equals(original.color) ||
                 this.iconId != original.iconId || this.iconVer != original.iconVer ||
                 this.iconQuality != original.iconQuality || !this.label.equals(original.label) ||
                 this.parentItemId != original.parentItemId || this.acl.getId() != original.acl.getId() ||
                 this.position != original.position;
     }
 
     /**
      * Set item name (has to be unique for the list)
      *
      * @param name item name (has to be unique for the list)
      */
     public void setName(String name) {
         this.name = name;
         if (this.name == null)
             this.name = "";
     }
 
     /**
      * Set this items label
      *
      * @param label label of this item
      */
     public void setLabel(FxString label) {
         this.label = label;
         if (this.label == null)
             this.label = new FxString("");
     }
 
     /**
      * Set this items acl
      *
      * @param acl acl of this item
      */
     public void setAcl(ACL acl) {
         this.acl = acl;
     }
 
     public void setAclId(long aclId) {
         this.acl = CacheAdmin.getEnvironment().getACL(aclId);
     }
 
     public long getAclId() {
         return this.acl.getId();
     }
 
     /**
      * Set optional item data
      *
      * @param data optional item data
      */
     public void setData(String data) {
         this.data = data;
         if (this.data == null)
             this.data = "";
     }
 
     /**
      * Set the items display color
      *
      * @param color display color
      */
     public void setColor(String color) {
         this.color = color;
         if (this.color == null)
             this.color = "";
     }
 
     /**
      * Set the id of icon used for user interfaces (reference to binaries, internal field!)
      *
      * @param iconId id of icon used for user interfaces (reference to binaries, internal field!)
      */
     public void setIconId(long iconId) {
         this.iconId = iconId;
     }
 
     /**
      * Set the version of icon used for user interfaces (reference to binaries, internal field!)
      *
      * @param iconVer version of icon used for user interfaces (reference to binaries, internal field!)
      */
     public void setIconVer(int iconVer) {
         this.iconVer = iconVer;
     }
 
     /**
      * Set the quality of icon used for user interfaces (reference to binaries, internal field!)
      *
      * @param iconQuality quality of icon used for user interfaces (reference to binaries, internal field!)
      */
     public void setIconQuality(int iconQuality) {
         this.iconQuality = iconQuality;
     }
 
     public FxSelectListItemEdit setDefaultItem() {
         this.getList()._setDefaultItem(this);
         return this;
     }
 
     /**
      * Helper method to create a new FxSelectListItemEdit instance
      *
      * @param name  unique name of the item (within the list)
      * @param acl   the items ACL
      * @param list  the list this item belongs to
      * @param label this items label
      * @param data  optional data
      * @param color color for display
      * @return FxSelectListItemEdit
      */
     public static FxSelectListItemEdit createNew(String name, ACL acl, FxSelectList list, FxString label, String data, String color) {
         return new FxSelectListItemEdit(name, acl, list, label, data, color);
     }
 
     /**
      * Clone an FxSelectListItem for editing
      *
      * @param item      the item to clone
      * @param markNew   mark this item as new?
      * @param applyList apply the item to the list of the original item?
      * @return FxSelectListItemEdit
      */
     public static FxSelectListItemEdit cloneItem(FxSelectListItem item, boolean markNew, boolean applyList) {
         FxSelectListItemEdit res = new FxSelectListItemEdit(item.getName(), item.getAcl(), applyList ? item.getList() : null,
                 item.getLabel().copy(), item.getData(), item.getColor());
         res.isNew = markNew;
         return res;
     }
 
     /**
      * Set a parent item
      *
      * @param item item from the parent list to assign as parent item
      * @return FxSelectListItemEdit
      * @throws FxInvalidParameterException if the parent item is not valid for this item
      */
     public FxSelectListItemEdit setParentItem(FxSelectListItem item) throws FxInvalidParameterException {
         if (item == null) {
             this.parentItem = null;
             this.parentItemId = -1L;
             return this;
         }
 //        if (!this.getList().hasParentList())
 //            throw new FxInvalidParameterException("item", "ex.structure.list.item.noParentList", this.getLabel().getBestTranslation());
         if (item.getList().getId() != this.getList().getId())
             throw new FxInvalidParameterException("item", "ex.structure.list.item.invalidParent", this.getList().getId(), item.getList().getId());
         this.parentItem = item;
         this.parentItemId = item.getId();
         return this;
     }
 
     /**
      * Check if the given list item is assignable as child (and not already assigned).
      * An item is assignable if it has no parent already, is different from this item and is assigned to the same list
      *
      * @param item item to check
      * @return assignable
      */
     public boolean isAssignable(FxSelectListItem item) {
         if( item.getParentItem() != null && this.getId() == item.getParentItem().getId())
             return true; //already assigned
         if( this.getList().getId() == item.getList().getId() && this.getId() != item.getId() && !item.hasParentItem() ) {
             //check if this item is not already a child of the item to check
            FxSelectListItem tmp = this.getParentItem();
            while (tmp != null) {
                if (tmp.getId() == item.getId()) {
                    return false;
                }
                tmp = tmp.getParentItem();
            }

             return !isChildOf(item);
         }
         return false;
     }
 
     /**
      * Check if this item is a child of the requested
      *
      * @param item item to check if this one is a child of
      * @return child of item
      */
     public boolean isChildOf(FxSelectListItem item) {
         if( item.getParentItem() == null )
             return false;
         for(FxSelectListItem check: item.getChildren())
             if( this.getId() == check.getId())
                 return true;
             else if(isChildOf(check))
                 return true;
         return false;
     }
 
     /**
      * Reset all changes to this item
      */
     public void resetChanges() {
         if( original == null )
             return;
         this.name = original.name;
         this.label = original.label.copy();
         this.acl = original.acl;
         this.data = original.data;
         this.color = original.color;
         this.parentItem = original.parentItem;
         this.parentItemId = original.parentItemId;
         this.isNew = false;
     }
 }
