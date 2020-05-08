 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
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
 package com.flexive.ejb.beans.structure;
 
 import com.flexive.core.Database;
 import static com.flexive.core.DatabaseConst.*;
 import com.flexive.core.LifeCycleInfoImpl;
 import com.flexive.core.flatstorage.FxFlatStorageManager;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.ejb.beans.EJBUtils;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxSystemSequencer;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.SelectListEngine;
 import com.flexive.shared.interfaces.SelectListEngineLocal;
 import com.flexive.shared.interfaces.SequencerEngineLocal;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxSelectList;
 import com.flexive.shared.structure.FxSelectListEdit;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.structure.FxSelectListItemEdit;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * SelectListEngine implementation
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "SelectListEngine", mappedName = "SelectListEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class SelectListEngineBean implements SelectListEngine, SelectListEngineLocal {
 
     private static final Log LOG = LogFactory.getLog(SelectListEngineBean.class);
     @Resource
     javax.ejb.SessionContext ctx;
 
     @EJB
     SequencerEngineLocal seq;
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxSelectListEdit list) throws FxApplicationException {
         final boolean newList = list.isNew();
         boolean changes = newList;
         long id = list.getId();
         if (newList) {
             id = createList(list);
         } else {
             if (list.changes()) {
                 updateList(list);
                 changes = true;
             }
         }
 
         if (!newList) {
             //remove selct items that are no longer referenced by this list
             List<FxSelectListItem> originalItems = CacheAdmin.getEnvironment().getSelectList(list.getId()).getItems();
             List<FxSelectListItem> editedItems = list.getItems();
             for (FxSelectListItem i : originalItems) {
                 boolean found = false;
                 for (FxSelectListItem j : editedItems) {
                     if (i.getId() == j.getId()) {
                         found = true;
                         break;
                     }
                 }
                 if (!found)
                     remove(i);
             }
         }
 
         FxSelectListItemEdit e;
         long defaultItemId = 0;
         long currentItemId;
         if (list.hasDefaultItem())
             defaultItemId = list.getDefaultItem().getId();
         Map<Long, Long> idMap = null;
         for (FxSelectListItem item : list.getItems()) {
             e = (item instanceof FxSelectListItemEdit ? (FxSelectListItemEdit) item : item.asEditable());
             if (!e.isNew())
                 continue;
             if (idMap == null)
                 idMap = new HashMap<Long, Long>(10);
             idMap.put(e.getId(), seq.getId(FxSystemSequencer.SELECTLIST_ITEM));
         }
         try {
             for (FxSelectListItemEdit item : sortItemsByParent(list.getItems())) {
                 e = item;
                 if (newList) {
                     currentItemId = createItem(e, idMap);
                     //get the new created default item id
                     if (defaultItemId < 0 && e.getId() == defaultItemId)
                         defaultItemId = currentItemId;
                 } else if (e.isNew()) {
                     createItem(e, idMap);
                     changes = true;
                 } else if (e.changes()) {
                     //check if the user is permitted to edit this specific item
                     if (FxContext.getUserTicket().isInRole(Role.SelectListEditor) || FxContext.getUserTicket().mayCreateACL(e.getAcl().getId(), -1)) {
                         updateItem(e);
                         changes = true;
                     } else
                         throw new FxNoAccessException("ex.role.notInRole", Role.SelectListEditor.getName());
                 }
             }
             updatePositions(list.getItems(), idMap);
             updateDefaultItem(id, defaultItemId);
             if (!changes) {
                 FxSelectListItem orgDef = CacheAdmin.getEnvironment().getSelectList(id).getDefaultItem();
                 if ((orgDef == null && defaultItemId != 0) || (orgDef != null && defaultItemId <= 0) ||
                         (orgDef != null && defaultItemId != 0 && orgDef.getId() != defaultItemId))
                     changes = true;
             }
             try {
                 if (changes)
                     StructureLoader.reload(null);
             } catch (FxCacheException e1) {
                 EJBUtils.rollback(ctx);
                 throw new FxCreateException(LOG, e1, "ex.cache", e1.getMessage());
             }
             return id;
         } catch (FxApplicationException fxApp) {
             ctx.setRollbackOnly();
             throw fxApp;
         }
     }
 
     /**
      * Sorts a list of SelectListItems according the parent ids
      *
      * Changes only the order to save, not the order of the selectList
      * @param items a list of the SelectListItems
      * @return a list sorted by the parent or an empty list if item was <code>null</code> or empty
      */
     private List<FxSelectListItemEdit> sortItemsByParent(List<FxSelectListItem> items) {
         if (items == null || items.size() <= 0) {
             return new ArrayList<FxSelectListItemEdit>();
         }
         List<FxSelectListItemEdit> sortedItems = new ArrayList<FxSelectListItemEdit>(items.size());
         List<FxSelectListItem> tmpItems = new ArrayList<FxSelectListItem>(items);
         List<FxSelectListItem> badItems = new ArrayList<FxSelectListItem>(items.size());
         Set<Long> knownParents = new HashSet<Long>();
         // Indicates if we have added one (or more) items into the result list
         boolean changed = true;
         int remainingLoops = 500;
 
         while (tmpItems.size() > 0 && changed && remainingLoops-->0) {
             changed = false;
             for (FxSelectListItem tmpItem : tmpItems) {
                 // If the item has no parent, we add it to the result list, and mark the id as 'known'
                 if (!tmpItem.hasParentItem()) {
                     sortedItems.add((tmpItem instanceof FxSelectListItemEdit ? (FxSelectListItemEdit) tmpItem : tmpItem.asEditable()));
                     knownParents.add(tmpItem.getId());
                     changed = true;
                 } else {
                     // If the item has a parent we already know, we add it to the result list, and mark the id as 'known'
                     if (knownParents.contains(tmpItem.getParentItem().getId())) {
                         sortedItems.add((tmpItem instanceof FxSelectListItemEdit ? (FxSelectListItemEdit) tmpItem : tmpItem.asEditable()));
                         knownParents.add(tmpItem.getId());
                         changed = true;
                     // If the item has a parent we don't know, we had to add it to the badItems and try again the next time
                     } else {
                         badItems.add(tmpItem);
                     }
                 }
             }
             tmpItems.clear();
             tmpItems.addAll(badItems);
             badItems.clear();
         }
 
         // Just in case some items has unknown parents add them to the list so that we keep consistent
         for (FxSelectListItem tmpItem : tmpItems) {
             sortedItems.add((tmpItem instanceof FxSelectListItemEdit ? (FxSelectListItemEdit) tmpItem : tmpItem.asEditable()));
         }
         return sortedItems;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxSelectListItemEdit item) throws FxApplicationException {
         long id = item.getId();
         if (item.isNew())
             id = createItem(item, null);
         else if (item.changes()) {
             FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.SelectListEditor);
             updateItem(item);
         } else
             return id;
         try {
             StructureLoader.reload(null);
         } catch (FxCacheException e1) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e1, "ex.cache", e1.getMessage());
         }
         return id;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxSelectList list) throws FxApplicationException {
 //        System.out.println("Removing list " + list.getLabel());
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.SelectListEditor);
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //fix list references
             StringBuilder sb = new StringBuilder(500).append("UPDATE ").append(TBL_STRUCT_SELECTLIST).append(" SET PARENTID=? WHERE PARENTID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setNull(1, java.sql.Types.INTEGER);
             ps.setLong(2, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //list translations
             sb.append("DELETE FROM ").append(TBL_STRUCT_SELECTLIST).append(ML).append(" WHERE ID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //item translations
             sb.append("DELETE FROM ").append(TBL_STRUCT_SELECTLIST_ITEM).append(ML).append(" WHERE ID IN(SELECT DISTINCT ID FROM ").
                     append(TBL_STRUCT_SELECTLIST_ITEM).append(" WHERE LISTID=?)");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //fix item references
             ps = con.prepareStatement(StorageManager.getSelectListItemReferenceFixStatement());
             ps.setNull(1, java.sql.Types.INTEGER);
             ps.setLong(2, list.getId());
             ps.executeUpdate();
             ps.close();
             //items
             sb.append("DELETE FROM ").append(TBL_STRUCT_SELECTLIST_ITEM).append(" WHERE LISTID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //the entry itself
             sb.append("DELETE FROM ").append(TBL_STRUCT_SELECTLIST).append(" WHERE ID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, list.getId());
             ps.executeUpdate();
             StructureLoader.reload(null);
         } catch (SQLException e) {
             e.printStackTrace();
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e1) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e1, "ex.cache", e1.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxSelectListItem item) throws FxApplicationException {
 //        System.out.println("Removing item " + item.getLabel());
         if (!(FxContext.getUserTicket().isInRole(Role.SelectListEditor) || FxContext.getUserTicket().mayDeleteACL(item.getList().getCreateItemACL().getId(), FxContext.getUserTicket().getUserId())))
             throw new FxNoAccessException("ex.selectlist.item.remove.noPerm", item.getList().getLabel(),
                     item.getAcl().getLabel());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //references (parent items)
             StringBuilder sb = new StringBuilder(500).append("UPDATE ").append(TBL_STRUCT_SELECTLIST_ITEM).append(" SET PARENTID=? WHERE PARENTID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setNull(1, java.sql.Types.INTEGER);
             ps.setLong(2, item.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //translations
             sb.append("DELETE FROM ").append(TBL_STRUCT_SELECTLIST_ITEM).append(ML).append(" WHERE ID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, item.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //the entry itself
             sb.append("DELETE FROM ").append(TBL_STRUCT_SELECTLIST_ITEM).append(" WHERE ID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, item.getId());
             ps.executeUpdate();
             StructureLoader.reload(null);
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e1) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e1, "ex.cache", e1.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     private long createList(FxSelectListEdit list) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.SelectListEditor);
         checkValidListParameters(list);
         long newId = seq.getId(FxSystemSequencer.SELECTLIST);
         list._synchronizeId(newId);
 //        System.out.println("Creating list " + list.getLabel() + " new id is " + newId);
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                            1  2        3    4
             ps = con.prepareStatement("INSERT INTO " + TBL_STRUCT_SELECTLIST + "(ID,PARENTID,NAME,ALLOW_ITEM_CREATE," +
                     //5              6            7            8     9
                     "ACL_CREATE_ITEM,ACL_ITEM_NEW,DEFAULT_ITEM,BCSEP,SAMELVLSELECT)VALUES(?,?,?,?,?,?,?,?,?)");
             ps.setLong(1, newId);
             if (list.hasParentList())
                 ps.setLong(2, list.getParentList().getId());
             else
                 ps.setNull(2, java.sql.Types.INTEGER);
             ps.setString(3, list.getName().trim());
             ps.setBoolean(4, list.isAllowDynamicItemCreation());
             ps.setLong(5, list.getCreateItemACL().getId());
             ps.setLong(6, list.getNewItemACL().getId());
             ps.setNull(7, java.sql.Types.INTEGER);
             ps.setString(8, list.getBreadcrumbSeparator());
             ps.setBoolean(9, list.isOnlySameLevelSelect());
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{list.getLabel(), list.getDescription()},
                     con, TBL_STRUCT_SELECTLIST, new String[]{"LABEL", "DESCRIPTION"}, "ID", newId);
             return newId;
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     /**
      * Update a select lists default item by setting it to <code>null</code> or its value for the list
      *
      * @param listId        the affected list
      * @param defaultItemId the default item to set (no checks for correct lists are made here!!)
      * @throws FxApplicationException on errors
      */
     private void updateDefaultItem(long listId, long defaultItemId) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                                        1          2
             ps = con.prepareStatement("UPDATE " + TBL_STRUCT_SELECTLIST + " SET DEFAULT_ITEM=? WHERE ID=?");
             if (defaultItemId <= 0)
                 ps.setNull(1, java.sql.Types.INTEGER);
             else
                 ps.setLong(1, defaultItemId);
             ps.setLong(2, listId);
             ps.executeUpdate();
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     /**
      * Update the positions of all items
      *
      * @param items select list items
      * @param idMap map of ids for new created items
      * @throws FxApplicationException on errors
      */
     private void updatePositions(List<FxSelectListItem> items, Map<Long, Long> idMap) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         int pos = 0;
         try {
             con = Database.getDbConnection();
             //                                                                    1          2
             ps = con.prepareStatement("UPDATE " + TBL_STRUCT_SELECTLIST_ITEM + " SET POS=? WHERE ID=?");
             for (FxSelectListItem item : items) {
                 ps.setInt(1, pos++);
                 ps.setLong(2, item.getId() < 0 && idMap != null
                         ? (idMap.containsKey(item.getId()) ? idMap.get(item.getId()) : -1)
                         : item.getId());
                 ps.addBatch();
             }
             ps.executeBatch();
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     private void updateList(FxSelectListEdit list) throws FxApplicationException {
         if (!list.changes())
             return;
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.SelectListEditor);
         checkValidListParameters(list);
 //        System.out.println("Updating list " + list.getLabel());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                                    1      2                   3
             ps = con.prepareStatement("UPDATE " + TBL_STRUCT_SELECTLIST + " SET PARENTID=?,NAME=?,ALLOW_ITEM_CREATE=?," +
                     //               4              5       6               7          8
                     "ACL_CREATE_ITEM=?,ACL_ITEM_NEW=?,BCSEP=?,SAMELVLSELECT=? WHERE ID=?");
             if (list.hasParentList())
                 ps.setLong(1, list.getParentList().getId());
             else
                 ps.setNull(1, java.sql.Types.INTEGER);
             ps.setString(2, list.getName().trim());
             ps.setBoolean(3, list.isAllowDynamicItemCreation());
             ps.setLong(4, list.getCreateItemACL().getId());
             ps.setLong(5, list.getNewItemACL().getId());
             ps.setString(6, list.getBreadcrumbSeparator());
             ps.setBoolean(7, list.isOnlySameLevelSelect());
             ps.setLong(8, list.getId());
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{list.getLabel(), list.getDescription()},
                     con, TBL_STRUCT_SELECTLIST, new String[]{"LABEL", "DESCRIPTION"}, "ID", list.getId());
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
 
     }
 
     /**
      * Create a new item
      *
      * @param item  the item to create
      * @param idMap map of temp. id to final db id
      * @return id
      * @throws FxApplicationException on errors
      */
     private long createItem(FxSelectListItemEdit item, Map<Long, Long> idMap) throws FxApplicationException {
         checkValidItemParameters(item);
         UserTicket ticket = FxContext.getUserTicket();
         if (!ticket.isInRole(Role.SelectListEditor)) {
             //check the lists ACL
             if (!(ticket.mayCreateACL(item.getList().getCreateItemACL().getId(), ticket.getUserId())))
                 throw new FxNoAccessException("ex.selectlist.item.create.noPerm", item.getList().getLabel(),
                         item.getList().getCreateItemACL().getLabel());
         }
         long newId;
         if (idMap == null || !idMap.containsKey(item.getId()))
             newId = seq.getId(FxSystemSequencer.SELECTLIST_ITEM);
         else
             newId = idMap.get(item.getId());
 //        System.out.println("Creating item " + item.getLabel() + " for list with id " + item.getList().getId());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                                        1  2    3   4        5      6    7
             ps = con.prepareStatement("INSERT INTO " + TBL_STRUCT_SELECTLIST_ITEM + "(ID,NAME,ACL,PARENTID,LISTID,DATA,COLOR," +
                     //8         9          10          11          12      13       14
                     "CREATED_BY,CREATED_AT,MODIFIED_BY,MODIFIED_AT,DBIN_ID,DBIN_VER,DBIN_QUALITY)VALUES" +
                     "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
             ps.setLong(1, newId);
             ps.setString(2, item.getName());
             ps.setLong(3, item.getAcl().getId());
             if (item.hasParentItem()) {
                 if (idMap != null && idMap.containsKey(item.getParentItem().getId()))
                     ps.setLong(4, idMap.get(item.getParentItem().getId()));
                 else
                     ps.setLong(4, item.getParentItem().getId());
             } else
                 ps.setNull(4, java.sql.Types.INTEGER);
             ps.setLong(5, item.getList().getId());
             ps.setString(6, item.getData());
             ps.setString(7, item.getColor());
             LifeCycleInfoImpl.store(ps, 8, 9, 10, 11);
             ps.setLong(12, item.getIconId());
             ps.setInt(13, item.getIconVer());
             ps.setInt(14, item.getIconQuality());
             ps.executeUpdate();
             Database.storeFxString(item.getLabel(),
                     con, TBL_STRUCT_SELECTLIST_ITEM, "LABEL", "ID", newId);
             return newId;
         } catch (SQLException e) {
             try {
                 if (StorageManager.isUniqueConstraintViolation(e))
                     throw new FxCreateException(LOG, e, "ex.selectlist.item.name.notUnique", item.getName());
                 throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
             } finally {
                 EJBUtils.rollback(ctx);
             }
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     /**
      * Update an existing item
      *
      * @param item the item to update
      * @throws FxApplicationException on errors
      */
     private void updateItem(FxSelectListItemEdit item) throws FxApplicationException {
         if (!item.changes())
             return;
         checkValidItemParameters(item);
 //        System.out.println("Updating item " + item.getLabel());
         if (!(FxContext.getUserTicket().isInRole(Role.SelectListEditor) || FxContext.getUserTicket().mayEditACL(item.getAcl().getId(), FxContext.getUserTicket().getUserId())))
             throw new FxNoAccessException("ex.selectlist.item.update.noPerm", item.getList().getLabel(),
                     item.getAcl().getLabel());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                                     1      2          3      4       5
             ps = con.prepareStatement("UPDATE " + TBL_STRUCT_SELECTLIST_ITEM + " SET NAME=?, ACL=?,PARENTID=?,DATA=?,COLOR=?," +
                     //           6             7         8          9              10         11
                     "MODIFIED_BY=?,MODIFIED_AT=?,DBIN_ID=?,DBIN_VER=?,DBIN_QUALITY=? WHERE ID=?");
             ps.setString(1, item.getName());
             ps.setLong(2, item.getAcl().getId());
             if (item.hasParentItem())
                 ps.setLong(3, item.getParentItem().getId());
             else
                 ps.setNull(3, java.sql.Types.INTEGER);
             ps.setString(4, item.getData());
             ps.setString(5, item.getColor());
             LifeCycleInfoImpl.updateLifeCycleInfo(ps, 6, 7);
             ps.setLong(8, item.getIconId());
             ps.setInt(9, item.getIconVer());
             ps.setInt(10, item.getIconQuality());
             ps.setLong(11, item.getId());
             ps.executeUpdate();
             Database.storeFxString(item.getLabel(),
                     con, TBL_STRUCT_SELECTLIST_ITEM, "LABEL", "ID", item.getId());
         } catch (SQLException e) {
             try {
                 if (StorageManager.isUniqueConstraintViolation(e))
                     throw new FxUpdateException(LOG, e, "ex.selectlist.item.name.notUnique", item.getName());
                 throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
             } finally {
                 EJBUtils.rollback(ctx);
             }
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     private void checkValidListParameters(FxSelectListEdit list) throws FxInvalidParameterException {
         if (list.getName() == null || list.getName().equals(""))
             throw new FxInvalidParameterException("Name", "ex.selectlist.name.empty");
         if (list.getName().indexOf('.') > 0)
            throw new FxInvalidParameterException("Name", "ex.selectlist.name.containsDot", list.getName());
         if (list.getName().indexOf(',') > 0)
            throw new FxInvalidParameterException("Name", "ex.selectlist.name.containsComma", list.getName());
     }
 
     private void checkValidItemParameters(FxSelectListItemEdit item) throws FxInvalidParameterException {
         if (StringUtils.isEmpty(item.getName()))
             throw new FxInvalidParameterException("Name", "ex.selectlist.item.name.empty");
         if (item.getLabel() == null || item.getLabel().getIsEmpty())
             throw new FxInvalidParameterException("Label", "ex.selectlist.item.label.empty");
         if (item.getName().indexOf(',') > 0)
             throw new FxInvalidParameterException("Name", "ex.selectlist.item.name.containsComma");
     }
 
     /**
      * {@inheritDoc}
      */
     public long getSelectListItemInstanceCount(long selectListItemId) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         long count = 0;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE FSELECT=?");
             ps.setLong(1, selectListItemId);
             ResultSet rs = ps.executeQuery();
             rs.next();
             count = rs.getLong(1);
             ps.close();
             if(EJBLookup.getDivisionConfigurationEngine().isFlatStorageEnabled()) {
                 //also examine flat storage entries
                 count += FxFlatStorageManager.getInstance().getSelectListItemInstanceCount(con, selectListItemId);
             }
         }
         catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         }
         finally {
             if (con != null)
                 Database.closeObjects(SelectListEngineBean.class, con, ps);
         }
         return count;
     }
 }
