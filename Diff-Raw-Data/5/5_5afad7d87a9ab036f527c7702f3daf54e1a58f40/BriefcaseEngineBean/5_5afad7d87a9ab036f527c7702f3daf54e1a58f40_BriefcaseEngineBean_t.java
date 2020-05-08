 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
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
 package com.flexive.ejb.beans;
 
 import com.flexive.core.Database;
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.LifeCycleInfoImpl;
 import com.flexive.shared.FxArrayUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.*;
 import com.flexive.shared.search.Briefcase;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.LifeCycleInfo;
 import com.flexive.shared.security.UserTicket;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Bean handling Briefcases.
  * <p/>
  * A briefcase is a object store which may be accessed with flexive SQL
  * or the API provided by this beans.
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "BriefcaseEngine")
 @TransactionAttribute(TransactionAttributeType.REQUIRED)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class BriefcaseEngineBean implements BriefcaseEngine, BriefcaseEngineLocal {
 
     private static transient Log LOG = LogFactory.getLog(BriefcaseEngineBean.class);
     @Resource
     javax.ejb.SessionContext ctx;
     @EJB
     SequencerEngineLocal seq;
     @EJB
     ACLEngineLocal acl;
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long create(String name, String description, Long aclId) throws FxApplicationException {
         final UserTicket ticket = FxContext.get().getTicket();
 
         if (description == null) {
             description = "";
         }
         if (name == null || name.trim().length() == 0) {
             throw new FxInvalidParameterException("ex.briefcase.nameMissing", "name");
         }
        if (aclId != null && aclId != -1) {
             ACL acl;
             try {
                 acl = new ACLEngineBean().load(aclId);
             } catch (Throwable t) {
                 throw new FxInvalidParameterException("ex.briefcase.invalidAcl", "acl");
             }
             if (!ticket.mayCreateACL(aclId, ticket.getUserId())) {
                 throw new FxNoAccessException("ex.briefcase.noCreatePermission", acl.getLabel());
             }
 
         }
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         String sourceQuery = "";
 
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Obtain a new id
             long newId = seq.getId(SequencerEngine.System.BRIEFCASE);
 
             sql = "INSERT INTO " + DatabaseConst.TBL_BRIEFCASE + "(" +
                     //1,   2,  3        ,  4         , 5 ,    6        7         8              9      , 10     , 11
                     "ID,NAME,DESCRIPTION,SOURCE_QUERY,ACL,CREATED_BY,CREATED_AT,MODIFIED_BY,MODIFIED_AT,MANDATOR,ICON_ID)" +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,1)";
             final long NOW = System.currentTimeMillis();
             ps = con.prepareStatement(sql);
             ps.setLong(1, newId);
             ps.setString(2, name);
             ps.setString(3, description);
             ps.setString(4, sourceQuery);
            if (aclId != null && aclId != -1) {
                 ps.setLong(5, aclId);
             } else {
                 ps.setNull(5, java.sql.Types.NUMERIC);
             }
             ps.setLong(6, ticket.getUserId());
             ps.setLong(7, NOW);
             ps.setLong(8, ticket.getUserId());
             ps.setLong(9, NOW);
             ps.setLong(10, ticket.getMandatorId());
             ps.executeUpdate();
             return newId;
         } catch (SQLException exc) {
             final boolean uniqueConstraintViolation = Database.isUniqueConstraintViolation(exc);
             if (ctx != null)
                 ctx.setRollbackOnly();
             else
                 try {
                     con.rollback();
                 } catch (SQLException e) {
                     LOG.warn(e.getMessage(), e);
                 }
             if (uniqueConstraintViolation) {
                 throw new FxEntryExistsException(LOG, "ex.briefcase.nameAlreadyExists", name);
             } else {
                 throw new FxCreateException(LOG, exc, "ex.briefcase.createFailed");
             }
         } finally {
             Database.closeObjects(BriefcaseEngineBean.class, con, ps);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void modify(long id, String name, String description, Long aclId) throws FxApplicationException {
 
         // Anything to do?
         if (name != null && name.trim().length() == 0) {
             name = null;
         }
         if (name == null && description == null && aclId == null) {
             return;
         }
 
         // Lookup the briefcase
         Briefcase br = load(id);
         if (br == null) {
             throw new FxNotFoundException("ex.briefcase.notFound", ("#" + id));
         }
         // Permission checks
         checkEditBriefcase(br);
         // Delete operation
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             String sSql = "update " + DatabaseConst.TBL_BRIEFCASE + " set" +
                     ((name == null) ? "" : " name=?, ") +
                     ((aclId == null) ? "" : " acl=?, ") +
                     ((description == null) ? "" : " description=?, ") +
                     "mandator=mandator where id=" + id;
             ps = con.prepareStatement(sSql);
             int pos = 1;
             if (name != null) ps.setString(pos++, name);
             if (aclId != null) {
                 if (aclId == -1) {
                     ps.setNull(pos++, java.sql.Types.NUMERIC);
                 } else {
                     ps.setLong(pos++, aclId);
                 }
             }
             if (description != null) ps.setString(pos, description);
             ps.executeUpdate();
         } catch (SQLException exc) {
             ctx.setRollbackOnly();
             throw new FxLoadException(LOG, exc, "ex.briefcase.modifyFailed", br.getName());
         } finally {
             Database.closeObjects(BriefcaseEngineBean.class, con, ps);
         }
     }
 
     private void checkEditBriefcase(Briefcase br) throws FxNotFoundException {
         final UserTicket ticket = FxContext.get().getTicket();
         if (!ticket.isGlobalSupervisor() && br.getMandator() != ticket.getMandatorId()) {
             if (!ticket.mayEditACL((br.getAcl()), br.getLifeCycleInfo().getCreatorId())) {
                 throw new FxNotFoundException("ex.briefcase.noEditPermission", br.getName());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(long id) throws FxApplicationException {
         // Lookup the briefcase
         Briefcase br = load(id);
         if (br == null) {
             throw new FxNotFoundException("ex.briefcase.notFound", ("#" + id));
         }
         // Permission checks
         final UserTicket ticket = FxContext.get().getTicket();
         if (!ticket.isGlobalSupervisor() && br.getMandator() != ticket.getMandatorId()) {
             if (!ticket.mayDeleteACL(br.getAcl(), br.getLifeCycleInfo().getCreatorId())) {
                 throw new FxNotFoundException("ex.briefcase.noDeletePermission", br.getName());
             }
         }
         // Delete operation
         Connection con = null;
         Statement stmt = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             stmt = con.createStatement();
             stmt.addBatch("delete from " + DatabaseConst.TBL_BRIEFCASE_DATA + " where briefcase_id=" + id);
             stmt.addBatch("delete from " + DatabaseConst.TBL_BRIEFCASE + " where id=" + id);
             stmt.executeBatch();
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.briefcase.deleteFailed", br.getName());
         } finally {
             Database.closeObjects(BriefcaseEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public List<Briefcase> getList(boolean includeShared) throws FxApplicationException {
         return getList(null, includeShared);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public Briefcase load(long id) throws FxApplicationException {
         List<Briefcase> l = getList(id, true);
         if (l != null && l.size() > 0) {
             return l.get(0);
         } else {
             throw new FxNotFoundException(LOG, "ex.briefcase.notFound.id", id);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void clear(long id) throws FxApplicationException {
         Connection con = null;
         PreparedStatement stmt = null;
         final Briefcase br = load(id);
         checkEditBriefcase(br);
         try {
             con = Database.getDbConnection();
             stmt = con.prepareStatement("DELETE FROM " + DatabaseConst.TBL_BRIEFCASE_DATA + " WHERE briefcase_id=?");
             stmt.setLong(1, id);
             stmt.executeUpdate();
         } catch (Exception e) {
             ctx.setRollbackOnly();
             throw new FxUpdateException(LOG, e, "ex.briefcase.clear", br.getName(), e);
         } finally {
             Database.closeObjects(BriefcaseEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void addItems(long id, long[] objectIds) throws FxApplicationException {
         Connection con = null;
         PreparedStatement stmt = null;
         final Briefcase br = load(id);
         checkEditBriefcase(br);
         try {
             con = Database.getDbConnection();
 
             // keep lookup table of existing items to avoid adding an item twice
             final Set<Long> existingItems = new HashSet<Long>();
             final long[] items = getItems(id);
             for (long item : items) {
                 existingItems.add(item);
             }
 
             stmt = con.prepareStatement("SELECT MAX(pos) FROM " + DatabaseConst.TBL_BRIEFCASE_DATA + " WHERE briefcase_id=?");
             stmt.setLong(1, id);
             final ResultSet rs = stmt.executeQuery();
             int pos = rs.next() ? rs.getInt(1) : 0;
             stmt.close();
             stmt = con.prepareStatement("INSERT INTO " + DatabaseConst.TBL_BRIEFCASE_DATA
                     + "(briefcase_id, id, pos, amount) VALUES (?, ?, ?, 1)");
             stmt.setLong(1, id);
             for (long objectId : objectIds) {
                 if (!existingItems.contains(objectId)) {
                     stmt.setLong(2, objectId);
                     stmt.setLong(3, ++pos);
                     stmt.addBatch();
                     existingItems.add(objectId);
                 }
             }
             stmt.executeBatch();
         } catch (Exception e) {
             ctx.setRollbackOnly();
             throw new FxUpdateException(LOG, e, "ex.briefcase.addItems", br.getName(), e);
         } finally {
             Database.closeObjects(BriefcaseEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeItems(long id, long[] objectIds) throws FxApplicationException {
         if (objectIds == null || objectIds.length == 0) {
             return;
         }
         Connection con = null;
         PreparedStatement stmt = null;
         final Briefcase br = load(id);
         checkEditBriefcase(br);
         try {
             con = Database.getDbConnection();
             stmt = con.prepareStatement("DELETE FROM " + DatabaseConst.TBL_BRIEFCASE_DATA + " WHERE briefcase_id=?" +
                     " AND id IN (" + FxArrayUtils.toSeparatedList(objectIds, ',') + ")");
             stmt.setLong(1, id);
             stmt.executeUpdate();
         } catch (Exception e) {
             ctx.setRollbackOnly();
             throw new FxUpdateException(LOG, e, "ex.briefcase.removeItems", br.getName(), e);
         } finally {
             Database.closeObjects(BriefcaseEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void updateItems(long id, long[] addObjectIds, long[] removeObjectIds) throws FxApplicationException {
         removeItems(id, removeObjectIds);
         addItems(id, addObjectIds);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setItems(long id, long[] objectIds) throws FxApplicationException {
         clear(id);
         addItems(id, objectIds);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long[] getItems(long id) throws FxApplicationException {
         Connection con = null;
         PreparedStatement stmt = null;
         final Briefcase br = load(id);
         try {
             con = Database.getDbConnection();
             stmt = con.prepareStatement("SELECT id FROM " + DatabaseConst.TBL_BRIEFCASE_DATA + " WHERE briefcase_id=?");
             stmt.setLong(1, id);
             final ResultSet rs = stmt.executeQuery();
             final List<Long> result = new ArrayList<Long>();
             while (rs.next()) {
                 result.add(rs.getLong(1));
             }
             return ArrayUtils.toPrimitive(result.toArray(new Long[result.size()]));
         } catch (Exception e) {
             ctx.setRollbackOnly();
             throw new FxUpdateException(LOG, e, "ex.briefcase.getItems", br.getName(), e);
         } finally {
             Database.closeObjects(BriefcaseEngineBean.class, con, stmt);
         }
     }
 
     /**
      * Builds a sql filer that only selects briefcases that the calling user has permissions on.
      *
      * @param briefcaseTblAlias the alias of the briefcase table, or null
      * @param includeShared     true if shared briefcases should be included
      * @param perms             the permissions that are needed
      * @return a sql filter, eg '([briefcaseTblAlias.]CREATED_BY=12 OR ACL IS NOT NULL)'
      */
     public static String getSqlAccessFilter(String briefcaseTblAlias, boolean includeShared, ACL.Permission... perms) {
         final UserTicket ticket = FxContext.get().getTicket();
         StringBuffer filter = new StringBuffer(1024);
         if (briefcaseTblAlias == null) {
             briefcaseTblAlias = "";
         }
         if (briefcaseTblAlias.length() > 0) {
             briefcaseTblAlias = briefcaseTblAlias + ".";
         }
         final String colMANDATOR = briefcaseTblAlias + "MANDATOR";
         final String colACL = briefcaseTblAlias + "ACL";
         final String colCREATED_BY = briefcaseTblAlias + "CREATED_BY";
         filter.append("(").append(colCREATED_BY).append("=").append(ticket.getUserId());
         if (includeShared) {
             if (ticket.isGlobalSupervisor()) {
                 // add all shared
                 filter.append(" OR ").append(colACL).append(" IS NOT null");
             } else if (ticket.isMandatorSupervisor()) {
                 // add all shared(match by ACL or mandator)
                 String acls = ticket.getACLsCSV(0/*owner is irrelevant here*/, ACL.Category.INSTANCE, perms);
                 filter.append((acls.length() > 0) ? (" OR " + colACL + " IN (" + acls + ") ") : "").
                         append(" OR (").append(colACL).append(" IS NOT null AND ").
                         append(colMANDATOR).append("=").append(ticket.getMandatorId()).append(")");
             } else {
                 // add all shared(match by ACL)
                 String acls = ticket.getACLsCSV(0/*owner is irrelevant here*/, ACL.Category.INSTANCE, perms);
                 if (acls.length() > 0) {
                     filter.append(" OR ").append(colACL).append(" IN (").append(acls).append(") ");
                 }
             }
         }
         filter.append(")");
         return filter.toString();
     }
 
     /**
      * Gets a list of all briefcase for the calling user.
      *
      * @param idFilter      if set only the pricelist with the given id will be loaded
      * @param includeShared if enabled shared briefcases will be included, if disabled only
      *                      the briefcases created by the calling user will be returned
      * @return the briefcases
      * @throws FxApplicationException if the function fails
      */
     private List<Briefcase> getList(Long idFilter, boolean includeShared) throws FxApplicationException {
         Connection con = null;
         Statement stmt = null;
         String sql;
         final ArrayList<Briefcase> result = new ArrayList<Briefcase>(500);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             sql = "select " +
                     //1,   2,  3        ,  4         , 5 ,    6        7         8              9      ,  10    , 11
                     "ID,NAME,DESCRIPTION,SOURCE_QUERY,ACL,CREATED_BY,CREATED_AT,MODIFIED_BY,MODIFIED_AT,MANDATOR,ICON_ID, " +
                     // 12
                     "(SELECT COUNT(*) FROM " + DatabaseConst.TBL_BRIEFCASE_DATA + " bd WHERE bd.briefcase_id=b.id) AS size " +
                     "from " + DatabaseConst.TBL_BRIEFCASE + " b where ";
             sql += getSqlAccessFilter(null, includeShared, ACL.Permission.READ);
             if (idFilter != null) {
                 sql += " and id=" + idFilter;
             }
 
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             while (rs != null && rs.next()) {
                 final long id = rs.getLong(1);
                 final String name = rs.getString(2);
                 final String desc = rs.getString(3);
                 final String src = rs.getString(4);
                 long acl = rs.getLong(5);
                 if (rs.wasNull()) {
                     acl = -1;
                 }
                 final LifeCycleInfo lc = LifeCycleInfoImpl.load(rs, 6, 7, 8, 9);
                 final long mandator = rs.getLong(10);
                 final long iconId = rs.getLong(11);
                 final int size = rs.getInt(12);
                 result.add(new Briefcase(id, name, mandator, desc, src, acl, lc, iconId, size));
             }
             result.trimToSize();
             return result;
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.briefcase.failedToLoadList", exc.getMessage());
         } finally {
             Database.closeObjects(BriefcaseEngineBean.class, con, stmt);
         }
     }
 
 }
