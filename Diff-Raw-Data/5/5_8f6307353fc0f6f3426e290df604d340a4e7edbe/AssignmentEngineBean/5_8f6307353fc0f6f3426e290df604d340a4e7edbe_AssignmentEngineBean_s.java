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
 
 import com.flexive.core.structure.FxEnvironmentUtils;
 import com.flexive.core.Database;
 import com.flexive.core.conversion.ConversionEngine;
 import com.flexive.core.flatstorage.FxFlatStorage;
 import com.flexive.core.flatstorage.FxFlatStorageManager;
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.core.storage.FulltextIndexer;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.ejb.beans.EJBUtils;
 import com.flexive.shared.*;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.*;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static com.flexive.core.DatabaseConst.*;
 
 /**
  * Structure Assignment management
  * <p/>
  * TODO's:
  * -property/group removal
  * -check if modification/creation even possible in case instances exist
  * -implement all known changeable flags
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "AssignmentEngine", mappedName = "AssignmentEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class AssignmentEngineBean implements AssignmentEngine, AssignmentEngineLocal {
 
     private static final Log LOG = LogFactory.getLog(AssignmentEngineBean.class);
 
     @Resource
     javax.ejb.SessionContext ctx;
 
     @EJB
     SequencerEngineLocal seq;
 
     @EJB
     HistoryTrackerEngineLocal htracker;
 
     @EJB
     DivisionConfigurationEngineLocal divisionConfig;
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createProperty(FxPropertyEdit property, String parentXPath) throws FxApplicationException {
         return createProperty(FxType.ROOT_ID, property, parentXPath);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createProperty(long typeId, FxPropertyEdit property, String parentXPath) throws FxApplicationException {
         return createProperty(typeId, property, parentXPath, property.getName());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createProperty(long typeId, FxPropertyEdit property, String parentXPath, String assignmentAlias) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.StructureManagement);
         Connection con = null;
         PreparedStatement ps = null;
         StringBuilder sql = new StringBuilder(2000);
         long newPropertyId;
         long newAssignmentId;
         try {
             parentXPath = parentXPath.toUpperCase();
             assignmentAlias = assignmentAlias.toUpperCase();
             FxType type = CacheAdmin.getEnvironment().getType(typeId);
             FxAssignment tmp = type.getAssignment(parentXPath);
             if (tmp != null && tmp instanceof FxPropertyAssignment)
                 throw new FxInvalidParameterException("ex.structure.assignment.noGroup", parentXPath);
             property.checkConsistency();
             //parentXPath is valid, create the property, then assign it to root
             newPropertyId = seq.getId(FxSystemSequencer.TYPEPROP);
             FxValue defValue = property.getDefaultValue();
             ContentStorage storage = StorageManager.getContentStorage(type.getStorageMode());
             con = Database.getDbConnection();
             if (defValue instanceof FxBinary) {
                 storage.prepareBinary(con, (FxBinary) defValue);
             }
             final String _def = defValue == null || defValue.isEmpty() ? null : ConversionEngine.getXStream().toXML(defValue);
 
             if (_def != null && (property.getDefaultValue() instanceof FxReference)) {
                 //check if the type matches the instance
                 checkReferencedType(con, (FxReference) property.getDefaultValue(), property.getReferencedType());
             }
 
             // do not allow to add mandatory properties (i.e. min multiplicity > 0) to types for which content exists
             if (storage.getTypeInstanceCount(con, typeId) > 0 && property.getMultiplicity().getMin() > 0) {
                 throw new FxCreateException("ex.structure.property.creation.existingContentMultiplicityError", property.getName(), property.getMultiplicity().getMin());
             }
 
             //create property, no checks for existing names are performed as this is handled with unique keys
             sql.append("INSERT INTO ").append(TBL_STRUCT_PROPERTIES).
                     //               1  2    3          4          5               6        7
                             append("(ID,NAME,DEFMINMULT,DEFMAXMULT,MAYOVERRIDEMULT,DATATYPE,REFTYPE," +
                             //8                9   10             11      12
                             "ISFULLTEXTINDEXED,ACL,MAYOVERRIDEACL,REFLIST,UNIQUEMODE," +
                             //13         14
                             "SYSINTERNAL,DEFAULT_VALUE)VALUES(" +
                             "?,?,?,?,?," +
                             "?,?,?,?,?,?,?,?,?)");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, newPropertyId);
             ps.setString(2, property.getName());
             ps.setInt(3, property.getMultiplicity().getMin());
             ps.setInt(4, property.getMultiplicity().getMax());
             ps.setBoolean(5, property.mayOverrideBaseMultiplicity());
             ps.setLong(6, property.getDataType().getId());
             if (property.hasReferencedType())
                 ps.setLong(7, property.getReferencedType().getId());
             else
                 ps.setNull(7, java.sql.Types.NUMERIC);
             ps.setBoolean(8, property.isFulltextIndexed());
             ps.setLong(9, property.getACL().getId());
             ps.setBoolean(10, property.mayOverrideACL());
             if (property.hasReferencedList())
                 ps.setLong(11, property.getReferencedList().getId());
             else
                 ps.setNull(11, java.sql.Types.NUMERIC);
             ps.setInt(12, property.getUniqueMode().getId());
             ps.setBoolean(13, false);
             if (_def == null)
                 ps.setNull(14, java.sql.Types.VARCHAR);
             else
                 ps.setString(14, _def);
             if (!property.isAutoUniquePropertyName())
                 ps.executeUpdate();
             else {
                 //fetch used property names
                 Statement stmt = null;
                 try {
                     stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT NAME FROM " + TBL_STRUCT_PROPERTIES + " WHERE NAME LIKE '" +
                             property.getName() + "_%' OR NAME='" + property.getName() + "' ORDER BY NAME DESC");
                     if (rs.next()) {
                         String last = rs.getString(1);
                         boolean found = true;
                         final boolean differentProp = last.lastIndexOf('_') > 0 && !StringUtils.isNumeric(last.substring(last.lastIndexOf("_") + 1));
                         //since postgres handles underscores as wildcards, find the first relevant entry
                         if (differentProp || !(last.equals(property.getName()) || last.startsWith(property.getName() + "_"))) {
                             found = false;
                             while (rs.next()) {
                                 last = rs.getString(1);
                                 if (last.equals(property.getName())) {
                                     found = true;
                                     break;
                                 } else if (last.startsWith(property.getName() + "_")) {
                                     if (StringUtils.isNumeric(last.substring(last.lastIndexOf("_") + 1))) {
                                         //ignore since its a different property that contains an underscore
                                         found = true;
                                         break;
                                     }
                                 }
                             }
                         }
                         if (found) {
                             String autoName;
                             if (last.indexOf(property.getName() + "_") == -1)
                                 autoName = property.getName() + "_1";
                             else
                                 autoName = property.getName() + "_" +
                                         (Integer.parseInt(last.substring(last.lastIndexOf("_") + 1)) + 1);
                             ps.setString(2, autoName);
                             LOG.info("Assigning unique property name [" + autoName + "] to [" + type.getName() + "." + property.getName() + "]");
                         }
                     }
                 } finally {
                     if (stmt != null)
                         stmt.close();
                 }
                 ps.executeUpdate();
             }
             Database.storeFxString(new FxString[]{property.getLabel(), property.getHint()},
                     con, TBL_STRUCT_PROPERTIES, new String[]{"DESCRIPTION", "HINT"}, "ID", newPropertyId);
             ps.close();
             sql.setLength(0);
             //calc new position
             sql.append("SELECT COALESCE(MAX(POS)+1,0) FROM ").append(TBL_STRUCT_ASSIGNMENTS).
                     append(" WHERE PARENTGROUP=? AND TYPEDEF=?");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, (tmp == null ? FxAssignment.NO_PARENT : tmp.getId()));
             ps.setLong(2, typeId);
             ResultSet rs = ps.executeQuery();
             long pos = 0;
             if (rs != null && rs.next())
                 pos = rs.getLong(1);
             ps.close();
             storeOptions(con, TBL_STRUCT_PROPERTY_OPTIONS, "ID", newPropertyId, null, property.getOptions());
             sql.setLength(0);
             //create root assignment
             sql.append("INSERT INTO ").append(TBL_STRUCT_ASSIGNMENTS).
                     //               1  2     3       4       5       6       7       8   9     10    11    12          13
                             append("(ID,ATYPE,ENABLED,TYPEDEF,MINMULT,MAXMULT,DEFMULT,POS,XPATH,XALIAS,BASE,PARENTGROUP,APROPERTY," +
                             //14 15
                             "ACL,DEFAULT_VALUE)" +
                             "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
             ps = con.prepareStatement(sql.toString());
             newAssignmentId = seq.getId(FxSystemSequencer.ASSIGNMENT);
             ps.setLong(1, newAssignmentId);
             ps.setInt(2, FxAssignment.TYPE_PROPERTY);
             ps.setBoolean(3, true);
             ps.setLong(4, typeId);
             ps.setInt(5, property.getMultiplicity().getMin());
             ps.setInt(6, property.getMultiplicity().getMax());
             if (property.getMultiplicity().isValid(property.getAssignmentDefaultMultiplicity())) {
                 ps.setInt(7, property.getAssignmentDefaultMultiplicity());
             } else {
                 //default is min(min,1).
                 ps.setInt(7, property.getMultiplicity().getMin() > 1 ? property.getMultiplicity().getMin() : 1);
             }
             ps.setLong(8, pos);
             if (parentXPath == null || "/".equals(parentXPath))
                 parentXPath = "";
             ps.setString(9, type.getName() + XPathElement.stripType(parentXPath) + "/" + assignmentAlias);
             ps.setString(10, assignmentAlias);
             ps.setNull(11, Types.NUMERIC);
             if (tmp == null)
                 ps.setLong(12, FxAssignment.NO_PARENT);
             else
                 ps.setLong(12, tmp.getId());
             ps.setLong(13, newPropertyId);
             ps.setLong(14, property.getACL().getId());
             ps.setString(15, _def);
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{property.getLabel(), property.getHint()}, con,
                     TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", newAssignmentId);
             StructureLoader.reloadAssignments(FxContext.get().getDivisionId());
             if (divisionConfig.isFlatStorageEnabled() && divisionConfig.get(SystemParameters.FLATSTORAGE_AUTO) && !FxEnvironmentUtils.isNoImmediateFlattening()) {
                 final FxFlatStorage fs = FxFlatStorageManager.getInstance();
                 FxPropertyAssignment pa = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(newAssignmentId);
                 if (fs.isFlattenable(pa)) {
                     fs.flatten(con, fs.getDefaultStorage(), pa);
                     StructureLoader.reloadAssignments(FxContext.get().getDivisionId());
                 }
             }
             htracker.track(type, "history.assignment.createProperty", property.getName(), type.getId(), type.getName());
             if (type.getId() != FxType.ROOT_ID)
                 createInheritedAssignments(CacheAdmin.getEnvironment().getAssignment(newAssignmentId), con, sql, type.getDerivedTypes());
         } catch (FxNotFoundException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } catch (FxLoadException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(e);
             EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.property.exists", property.getName(), (parentXPath.length() == 0 ? "/" : parentXPath));
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, ps);
         }
         return newAssignmentId;
     }
 
     /*
     * Updates the options of an FxGroup
     * Before the options are updated, they are compared against the options that are
     * already stored in the DB. If there are changes, all present options are deleted
     * in the DB and newly created afterwards from the assignment's option list.
     *
     * @return if the options changed.
     */
 
     private boolean updateGroupOptions(Connection con, FxGroupEdit group) throws SQLException, FxInvalidParameterException {
         boolean changed = false;
         FxGroupEdit org = new FxGroupEdit(CacheAdmin.getEnvironment().getGroup(group.getId()));
         if (org.getOptions().size() != group.getOptions().size()) {
             changed = true;
         } else {
             for (int i = 0; i < org.getOptions().size(); i++) {
                 FxStructureOption orgOpt = org.getOptions().get(i);
                 FxStructureOption propOpt = group.getOption(orgOpt.getKey());
                 if (!orgOpt.equals(propOpt)) {
                     changed = true;
                     break;
                 }
             }
         }
         if (changed)
             storeOptions(con, TBL_STRUCT_GROUP_OPTIONS, "ID", group.getId(), null, group.getOptions());
         return changed;
     }
 
     /*
      * Updates the options of an FxProperty
      * Before the options are updated, they are compared against the options that are
      * already stored in the DB. If there are changes, all present options are deleted
      * in the DB and newly created afterwards from the assignment's option list.
      *
      * @return if the options changed.
      */
 
     private boolean updatePropertyOptions(Connection con, FxPropertyEdit prop) throws SQLException, FxInvalidParameterException {
         boolean changed = false;
         FxPropertyEdit org = new FxPropertyEdit(CacheAdmin.getEnvironment().getProperty(prop.getId()));
         if (org.getOptions().size() != prop.getOptions().size()) {
             changed = true;
         } else {
             for (int i = 0; i < org.getOptions().size(); i++) {
                 FxStructureOption orgOpt = org.getOptions().get(i);
                 FxStructureOption propOpt = prop.getOption(orgOpt.getKey());
                 if (!orgOpt.equals(propOpt)) {
                     changed = true;
                     break;
                 }
             }
         }
         if (changed)
             storeOptions(con, TBL_STRUCT_PROPERTY_OPTIONS, "ID", prop.getId(), null, prop.getOptions());
         return changed;
     }
 
     /*
     * Updates the options of an FxGroupAssignment
     * Before the options are updated, they are compared against the options that are
     * already stored in the DB. If there are changes, all present options are deleted
     * in the DB and newly created afterwards from the assignment's option list.
     *
     * @return if the options changed.
     */
 
     private boolean updateGroupAssignmentOptions(Connection con, FxGroupAssignment ga) throws SQLException, FxInvalidParameterException {
         boolean changed = false;
         FxGroupAssignmentEdit org = ((FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(ga.getId())).asEditable();
         FxGroupAssignmentEdit group = ga.asEditable();
         if (org.getOptions().size() != group.getOptions().size()) {
             changed = true;
         } else {
             for (int i = 0; i < org.getOptions().size(); i++) {
                 FxStructureOption orgOpt = org.getOptions().get(i);
                 FxStructureOption propOpt = group.getOption(orgOpt.getKey());
                 if (!orgOpt.equals(propOpt)) {
                     changed = true;
                     break;
                 }
             }
         }
         if (changed) {
             storeOptions(con, TBL_STRUCT_GROUP_OPTIONS, "ID", group.getGroup().getId(), group.getId(), group.getOptions());
             // propagate inherited options to derived assignments (only for derived types!)
             List<FxGroupAssignment> derivedAssignments = org.getDerivedAssignments(CacheAdmin.getEnvironment());
             if (derivedAssignments.size() > 0) {
                 // retrieve list of inherited options from the current modified assignment
                 for (FxGroupAssignment derived : derivedAssignments) {
                     final List<FxStructureOption> inheritedOpts = FxStructureOption.cloneOptions(group.getOptions(), true);
                     if (inheritedOpts.size() > 0) {
                         updateDerivedAssignmentOptions(con, TBL_STRUCT_GROUP_OPTIONS, derived, inheritedOpts);
                     }
                 }
             }
         }
         return changed;
     }
 
     /*
      * Updates the options of an FxPropertyAssignment
      * Before the options are updated, they are compared against the options that are
      * already stored in the DB. If there are changes, all present options are deleted
      * in the DB and newly created afterwards from the assignment's option list.
      *
      * @return if the options changed.
      */
 
     private boolean updatePropertyAssignmentOptions(Connection con, FxPropertyAssignment original, FxPropertyAssignment modified) throws SQLException, FxInvalidParameterException {
         boolean changed = false;
         FxPropertyAssignmentEdit org = original.asEditable();
         FxPropertyAssignmentEdit prop = modified.asEditable();
         if (org.getOptions().size() != prop.getOptions().size()) {
             changed = true;
         } else {
             for (int i = 0; i < org.getOptions().size(); i++) {
                 FxStructureOption orgOpt = org.getOptions().get(i);
                 FxStructureOption propOpt = prop.getOption(orgOpt.getKey());
                 if (!orgOpt.equals(propOpt)) {
                     changed = true;
                     break;
                 }
             }
         }
         if (changed) {
             storeOptions(con, TBL_STRUCT_PROPERTY_OPTIONS, "ID", original.getProperty().getId(), original.getId(), prop.getOptions());
             // propagate inherited options to derived assignments (only f. derived types!)
             List<FxPropertyAssignment> derivedAssignments = original.getDerivedAssignments(CacheAdmin.getEnvironment());
             if (derivedAssignments.size() > 0) {
                 // retrieve list of inherited options from the current modified assignment
                 for (FxPropertyAssignment derived : derivedAssignments) {
                     final List<FxStructureOption> inheritedOpts = FxStructureOption.cloneOptions(modified.getOptions(), true);
                     if (inheritedOpts.size() > 0) {
                         updateDerivedAssignmentOptions(con, TBL_STRUCT_PROPERTY_OPTIONS, derived, inheritedOpts);
                     }
                 }
             }
         }
         return changed;
     }
 
     /*
      * Helper to store options, (the information in brackets expalains how to use this method to store the options
      * for an FxPropertyAssignment)
      *
      * @param con               the DB connection
      * @param table             the table name to store the options (e.g. TBL_STRUCT_PROPERTY_OPTIONS)
      * @param primaryColumn     the column name of the primary key (where the property Id is stored, e.g. ID)
      * @param primaryId         the primary key itself (the property Id, e.g. FxPropertyAssignment.getProperty().getId())
      * @param assignmentId      the foreign key, may be <code>null</code> (the assignment Id, e.g. FxPropertyAssignment.getId())
      * @param options           the option list to store (e.g. FxPropertyAssignmentEdit.getOptions())
      */
 
     private void storeOptions(Connection con, String table, String primaryColumn, long primaryId, Long assignmentId,
                               List<FxStructureOption> options) throws SQLException, FxInvalidParameterException {
         PreparedStatement ps = null;
         try {
             if (assignmentId == null) {
                 ps = con.prepareStatement("DELETE FROM " + table + " WHERE " + primaryColumn + "=? AND ASSID IS NULL");
             } else {
                 ps = con.prepareStatement("DELETE FROM " + table + " WHERE " + primaryColumn + "=? AND ASSID=?");
                 ps.setLong(2, assignmentId);
             }
             ps.setLong(1, primaryId);
             ps.executeUpdate();
             if (options == null || options.size() == 0)
                 return;
             ps.close();
             ps = con.prepareStatement("INSERT INTO " + table + " (" + primaryColumn + ",ASSID,OPTKEY,MAYOVERRIDE,ISINHERITED,OPTVALUE)VALUES(?,?,?,?,?,?)");
             for (FxStructureOption option : options) {
                 ps.setLong(1, primaryId);
                 if (assignmentId != null)
                     ps.setLong(2, assignmentId);
                 else
                     ps.setNull(2, java.sql.Types.NUMERIC);
                 if (StringUtils.isEmpty(option.getKey()))
                     throw new FxInvalidParameterException("key", "ex.structure.option.key.empty", option.getValue());
                 ps.setString(3, option.getKey());
                 ps.setBoolean(4, option.isOverridable());
                 ps.setBoolean(5, option.getIsInherited());
                 ps.setString(6, option.getValue());
                 ps.addBatch();
             }
             ps.executeBatch();
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * Update the options of any derived assignments if their base versions acquire new options which are inherited
      * ONLY do this iff the respective options are not already set
      *
      * @param con           an open and valid connection
      * @param table         the table name
      * @param derived       the derived assignment t.b. updated
      * @param inheritedOpts the options of the source type t.b. inherited by the derived type
      * @return true if any changes had to be made
      * @throws SQLException                on errors
      * @throws FxInvalidParameterException on errors
      * @since 3.1.1
      */
     private boolean updateDerivedAssignmentOptions(Connection con, String table, FxAssignment derived, List<FxStructureOption> inheritedOpts)
             throws SQLException, FxInvalidParameterException {
         boolean changed = false;
         final List<FxStructureOption> current = FxStructureOption.cloneOptions(derived.getOptions());
         final List<FxStructureOption> newOpts = new ArrayList<FxStructureOption>(inheritedOpts.size());
 
         for (FxStructureOption o : inheritedOpts) {
             if (!FxStructureOption.hasOption(o.getKey(), current))
                 newOpts.add(o);
                 // update non overridable inherited option values if they differ
             else if (!o.isOverridable() && !FxStructureOption.getOption(o.getKey(), current).getValue().equals(o.getValue())) {
                 newOpts.add(new FxStructureOption(o.getKey(), o.isOverridable(), true, o.getIsInherited(), o.getValue()));
                 // remove the option from the current ones
                 FxStructureOption.clearOption(current, o.getKey());
             }
         }
         // add all remaining current options
         newOpts.addAll(current);
 
         if (newOpts.size() > 0) {
             if (derived instanceof FxPropertyAssignment)
                 storeOptions(con, table, "ID", ((FxPropertyAssignment) derived).getProperty().getId(), derived.getId(), newOpts);
             else if (derived instanceof FxGroupAssignment)
                 storeOptions(con, table, "ID", ((FxGroupAssignment) derived).getGroup().getId(), derived.getId(), newOpts);
 
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * Helper to process all derived types and derivates of derived types
      *
      * @param assignment the assignment processed
      * @param con        an open and valid connection
      * @param sb         StringBuilder
      * @param types      (derived) types to process - will recurse to derived types of these types
      * @throws FxApplicationException on errors
      */
     private void createInheritedAssignments(FxAssignment assignment, Connection con,
                                             StringBuilder sb, List<FxType> types) throws FxApplicationException {
         for (FxType derivedType : types) {
             if (assignment instanceof FxPropertyAssignment) {
                 createPropertyAssignment(con, sb,
                         FxPropertyAssignmentEdit.createNew((FxPropertyAssignment) assignment, derivedType,
                                 assignment.getAlias(),
                                 assignment.hasParentGroupAssignment()
                                         ? assignment.getParentGroupAssignment().getXPath()
                                         : "/")
                 );
             } else if (assignment instanceof FxGroupAssignment) {
                 createGroupAssignment(con, sb,
                         FxGroupAssignmentEdit.createNew((FxGroupAssignment) assignment, derivedType, assignment.getAlias(),
                                 assignment.hasParentGroupAssignment()
                                         ? assignment.getParentGroupAssignment().getXPath()
                                         : "/"),
                         true
                 );
             }
 //            if (derivedType.getDerivedTypes().size() > 0)  //one level deeper ...
 //                _inheritedAssignmentsCreate(assignment, con, ps, sb, derivedType.getDerivedTypes());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeProperty(long propertyId) throws FxApplicationException {
         List<FxPropertyAssignment> assignments = CacheAdmin.getEnvironment().getPropertyAssignments(propertyId, true);
         if (assignments.size() == 0)
             throw new FxNotFoundException(LOG, "ex.structure.assignment.notFound.id", propertyId);
 
         for (FxPropertyAssignment a : assignments) {
             if (!a.isDerivedAssignment()) {
                 removeAssignment(a.getId(), false, true, false, true);
             }
         }
     }
 
     /**
      * Update a group's attributes
      *
      * @param con   an existing sql connection
      * @param group the instance of FxGroupEdit whose attributes should be changed
      * @return true if changes were made to the group
      * @throws FxApplicationException on errors
      */
     private boolean updateGroup(Connection con, FxGroupEdit group) throws FxApplicationException {
         final StringBuilder sql = new StringBuilder(1000);
         final StringBuilder changesDesc = new StringBuilder(200);
         final FxGroup org = CacheAdmin.getEnvironment().getGroup(group.getId());
         PreparedStatement ps = null;
         boolean changes = false;
         boolean success = false;
         try {
             sql.setLength(0);
 
             // change the group's override base multiplicity flag
             if (org.mayOverrideBaseMultiplicity() != group.mayOverrideBaseMultiplicity()) {
                 if (!group.mayOverrideBaseMultiplicity()) {
                     if (getGroupInstanceMultiplicity(con, org.getId(), true) < group.getMultiplicity().getMin())
                         throw new FxUpdateException("ex.structure.modification.contentExists", "minimumMultiplicity");
                     if (getGroupInstanceMultiplicity(con, org.getId(), false) > group.getMultiplicity().getMax())
                         throw new FxUpdateException("ex.structure.modification.contentExists", "maximumMultiplicity");
                 }
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_GROUPS + " SET MAYOVERRIDEMULT=? WHERE ID=?");
                 ps.setBoolean(1, group.mayOverrideBaseMultiplicity());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 ps.close();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("mayOverrideMultiplicity=").append(group.mayOverrideBaseMultiplicity());
                 changes = true;
             }
             // check and change the group's minimum and/or maximum multiplicity
             if (org.getMultiplicity().getMin() != group.getMultiplicity().getMin() ||
                     org.getMultiplicity().getMax() != group.getMultiplicity().getMax()) {
                 if (!org.mayOverrideBaseMultiplicity()) {
                     if (org.getMultiplicity().getMin() < group.getMultiplicity().getMin()) {
                         if (getGroupInstanceMultiplicity(con, org.getId(), true) < group.getMultiplicity().getMin())
                             throw new FxUpdateException("ex.structure.modification.group.contentExists", group.getId(), group.getMultiplicity().getMin(), group.getMultiplicity().getMax());
                     }
                     if (org.getMultiplicity().getMax() > group.getMultiplicity().getMax()) {
                         if (getGroupInstanceMultiplicity(con, org.getId(), false) > group.getMultiplicity().getMax())
                             throw new FxUpdateException("ex.structure.modification.group.contentExists", group.getId(), group.getMultiplicity().getMin(), group.getMultiplicity().getMax());
                     }
                 }
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_GROUPS + " SET DEFMINMULT=? ,DEFMAXMULT=? WHERE ID=?");
                 ps.setInt(1, group.getMultiplicity().getMin());
                 ps.setInt(2, group.getMultiplicity().getMax());
                 ps.setLong(3, group.getId());
                 ps.executeUpdate();
                 ps.close();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("multiplicity=").append(group.getMultiplicity());
                 changes = true;
             }
             // change the group's label
             if (org.getLabel() != null && !org.getLabel().equals(group.getLabel()) ||
                     org.getLabel() == null && group.getLabel() != null ||
                     org.getHint() != null && !org.getHint().equals(group.getHint()) ||
                     org.getHint() == null && group.getHint() != null) {
                 Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()}, con,
                         TBL_STRUCT_GROUPS, new String[]{"DESCRIPTION", "HINT"}, "ID", group.getId());
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("label=").append(group.getLabel()).append(',');
                 changesDesc.append("hint=").append(group.getHint()).append(',');
                 changes = true;
             }
             // change the group's name
             if (!org.getName().equals(group.getName())) {
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_GROUPS + " SET NAME=? WHERE ID=?");
                 ps.setString(1, group.getName());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 ps.close();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("name=").append(group.getName());
                 changes = true;
             }
             // change the group options
             if (updateGroupOptions(con, group)) {
                 changesDesc.append(",options:");
                 List<FxStructureOption> options = group.getOptions();
                 for (FxStructureOption option : options) {
                     changesDesc.append(option.getKey()).append("=").append(option.getValue()).append(" overridable=").
                             append(option.isOverridable()).append(" isSet=").append(option.isSet());
                 }
                 changes = true;
             }
 
             if (changes) {
                 htracker.track("history.group.update.groupProperties", group.getName(), group.getId(), changesDesc.toString());
             }
             success = true;
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
             if (!success) {
                 EJBUtils.rollback(ctx);
             }
         }
         return changes;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createGroup(FxGroupEdit group, String parentXPath) throws FxApplicationException {
         return createGroup(FxType.ROOT_ID, group, parentXPath);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createGroup(long typeId, FxGroupEdit group, String parentXPath) throws FxApplicationException {
         return createGroup(typeId, group, parentXPath, group.getName());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createGroup(long typeId, FxGroupEdit group, String parentXPath, String assignmentAlias) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.StructureManagement);
         Connection con = null;
         PreparedStatement ps = null;
         StringBuilder sql = new StringBuilder(2000);
         long newGroupId;
         long newAssignmentId;
         try {
             parentXPath = parentXPath.toUpperCase();
             assignmentAlias = assignmentAlias.toUpperCase();
             FxType type = CacheAdmin.getEnvironment().getType(typeId);
             FxAssignment tmp = type.getAssignment(parentXPath);
             if (tmp != null && tmp instanceof FxPropertyAssignment)
                 throw new FxInvalidParameterException("ex.structure.assignment.noGroup", parentXPath);
             //parentXPath is valid, create the group, then assign it to root
             newGroupId = seq.getId(FxSystemSequencer.TYPEGROUP);
             con = Database.getDbConnection();
             ContentStorage storage = StorageManager.getContentStorage(type.getStorageMode());
             // do not allow to add mandatory groups (i.e. min multiplicity > 0) to types for which content exists
             if (storage.getTypeInstanceCount(con, typeId) > 0 && group.getMultiplicity().getMin() > 0) {
                 throw new FxCreateException("ex.structure.group.creation.exisitingContentMultiplicityError", group.getName(), group.getMultiplicity().getMin());
             }
 
             //create group
             sql.append("INSERT INTO ").append(TBL_STRUCT_GROUPS).
                     append("(ID,NAME,DEFMINMULT,DEFMAXMULT,MAYOVERRIDEMULT)VALUES(?,?,?,?,?)");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, newGroupId);
             ps.setString(2, group.getName());
             ps.setInt(3, group.getMultiplicity().getMin());
             ps.setInt(4, group.getMultiplicity().getMax());
             ps.setBoolean(5, group.mayOverrideBaseMultiplicity());
             ps.executeUpdate();
             ps.close();
             sql.setLength(0);
             Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()},
                     con, TBL_STRUCT_GROUPS, new String[]{"DESCRIPTION", "HINT"}, "ID", newGroupId);
             //calc new position
             sql.append("SELECT COALESCE(MAX(POS)+1,0) FROM ").append(TBL_STRUCT_ASSIGNMENTS).
                     append(" WHERE PARENTGROUP=? AND TYPEDEF=?");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, (tmp == null ? FxAssignment.NO_PARENT : tmp.getId()));
             ps.setLong(2, typeId);
             ResultSet rs = ps.executeQuery();
             long pos = 0;
             if (rs != null && rs.next())
                 pos = rs.getLong(1);
             ps.close();
             storeOptions(con, TBL_STRUCT_GROUP_OPTIONS, "ID", newGroupId, null, group.getOptions());
             sql.setLength(0);
             //create root assignment
             sql.append("INSERT INTO ").append(TBL_STRUCT_ASSIGNMENTS).
                     //               1  2     3       4       5       6       7       8   9     10     11   12          13     14
                             append("(ID,ATYPE,ENABLED,TYPEDEF,MINMULT,MAXMULT,DEFMULT,POS,XPATH,XALIAS,BASE,PARENTGROUP,AGROUP,GROUPMODE)" +
                             "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
             ps = con.prepareStatement(sql.toString());
             newAssignmentId = seq.getId(FxSystemSequencer.ASSIGNMENT);
             ps.setLong(1, newAssignmentId);
             ps.setInt(2, FxAssignment.TYPE_GROUP);
             ps.setBoolean(3, true);
             ps.setLong(4, typeId);
             ps.setInt(5, group.getMultiplicity().getMin());
             ps.setInt(6, group.getMultiplicity().getMax());
             if (group.getMultiplicity().isValid(group.getAssignmentDefaultMultiplicity())) {
                 ps.setInt(7, group.getAssignmentDefaultMultiplicity());
             } else {
                 //default is min(min,1).
                 ps.setInt(7, group.getMultiplicity().getMin() > 1 ? group.getMultiplicity().getMin() : 1);
             }
             ps.setLong(8, pos);
             if (parentXPath == null || "/".equals(parentXPath))
                 parentXPath = "";
             ps.setString(9, type.getName() + XPathElement.stripType(parentXPath) + "/" + assignmentAlias);
             ps.setString(10, assignmentAlias);
             ps.setNull(11, java.sql.Types.NUMERIC);
             ps.setLong(12, (tmp == null ? FxAssignment.NO_PARENT : tmp.getId()));
             ps.setLong(13, newGroupId);
             ps.setInt(14, group.getAssignmentGroupMode().getId());
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()}, con,
                     TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", newAssignmentId);
             StructureLoader.reloadAssignments(FxContext.get().getDivisionId());
             htracker.track(type, "history.assignment.createGroup", group.getName(), type.getId(), type.getName());
             if (type.getId() != FxType.ROOT_ID)
                 createInheritedAssignments(CacheAdmin.getEnvironment().getAssignment(newAssignmentId), con, sql, type.getDerivedTypes());
         } catch (FxNotFoundException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } catch (FxLoadException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(e);
             EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.group.exists", group.getName(), (parentXPath.length() == 0 ? "/" : parentXPath));
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, ps);
         }
         return newAssignmentId;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeGroup(long groupId) throws FxApplicationException {
         List<FxGroupAssignment> assignments = CacheAdmin.getEnvironment().getGroupAssignments(groupId, true);
         if (assignments.size() == 0)
             throw new FxNotFoundException("ex.structure.assignment.notFound.id", groupId);
 
         for (FxGroupAssignment a : assignments) {
             if (!a.isDerivedAssignment()) {
                 removeAssignment(a.getId(), true, true, false, true);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxAssignment assignment, boolean createSubAssignments) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.StructureManagement);
         long returnId;
         boolean reload = false;
         Connection con = null;
         try {
             con = Database.getDbConnection();
             if (assignment instanceof FxPropertyAssignmentEdit) {
                 if (((FxPropertyAssignmentEdit) assignment).isNew()) {
                     returnId = createPropertyAssignment(con, null, (FxPropertyAssignmentEdit) assignment);
                 } else {
                     returnId = assignment.getId();
                     try {
                         reload = updatePropertyAssignment(con, null, (FxPropertyAssignmentEdit) assignment);
                     } catch (FxLoadException e) {
                         EJBUtils.rollback(ctx);
                         throw new FxUpdateException(e);
                     } catch (FxNotFoundException e) {
                         EJBUtils.rollback(ctx);
                         throw new FxUpdateException(e);
                     }
                 }
 
             } else if (assignment instanceof FxGroupAssignmentEdit) {
                 if (((FxGroupAssignmentEdit) assignment).isNew()) {
                     returnId = createGroupAssignment(con, null, (FxGroupAssignmentEdit) assignment, createSubAssignments);
                 } else {
                     returnId = assignment.getId();
                     try {
                         reload = updateGroupAssignment(con, (FxGroupAssignmentEdit) assignment);
                     } catch (FxLoadException e) {
                         EJBUtils.rollback(ctx);
                         throw new FxUpdateException(e);
                     } catch (FxNotFoundException e) {
                         EJBUtils.rollback(ctx);
                         throw new FxUpdateException(e);
                     }
                 }
             } else
                 throw new FxInvalidParameterException("ASSIGNMENT", "ex.structure.assignment.noEditAssignment");
             try {
                 if (reload) {
                     StructureLoader.reload(con);
                     //clear instance cache
                     CacheAdmin.expireCachedContents();
                     //check for possible side effects on the flat storage
                     if (divisionConfig.isFlatStorageEnabled()) {
                         //check if flattened assignments now are required to be unflattened
                         final FxFlatStorage fs = FxFlatStorageManager.getInstance();
                         assignment = CacheAdmin.getEnvironment().getAssignment(returnId); //make sure we have the updated version
                         if (assignment instanceof FxPropertyAssignment) {
                             if (((FxPropertyAssignment) assignment).isFlatStorageEntry() &&
                                     !fs.isFlattenable((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(returnId))) {
                                 fs.unflatten(con, (FxPropertyAssignment) assignment);
                                 StructureLoader.reload(con);
                             }
                         } else if (assignment instanceof FxGroupAssignment) {
                             boolean needReload = false;
                             for (FxAssignment as : ((FxGroupAssignment) assignment).getAllChildAssignments()) {
                                 if (!(as instanceof FxPropertyAssignment))
                                     continue;
                                 if (((FxPropertyAssignment) as).isFlatStorageEntry() &&
                                         !fs.isFlattenable((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(as.getId()))) {
                                     fs.unflatten(con, (FxPropertyAssignment) as);
                                     needReload = true;
                                 }
                             }
                             if (needReload)
                                 StructureLoader.reload(con);
                         }
                         if (divisionConfig.get(SystemParameters.FLATSTORAGE_AUTO)) {
                             //check if some assignments can now be flattened
                             FxFlatStorageManager.getInstance().flattenType(con, fs.getDefaultStorage(), assignment.getAssignedType());
                             StructureLoader.reload(con);
                         }
                     }
                 }
             } catch (FxCacheException e) {
                 EJBUtils.rollback(ctx);
                 throw new FxCreateException(e, "ex.cache", e.getMessage());
             } catch (FxLoadException e) {
                 EJBUtils.rollback(ctx);
                 throw new FxCreateException(e);
             }
             return returnId;
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e);
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, null);
         }
     }
 
     /**
      * Check if a property assignments minum multiplicity may be changed and throw an exception if not
      *
      * @param con      an open and valid connection
      * @param original original assignment
      * @param modified modified assignment
      * @throws SQLException      db error
      * @throws FxUpdateException change is not allowed
      */
     private void checkChangeGroupAssignmentMinMultiplicity(Connection con, FxGroupAssignment original, FxGroupAssignmentEdit modified) throws SQLException, FxUpdateException {
         final long minMult = getGroupInstanceMultiplicity(con, original.getGroup().getId(), true);
         boolean changeOk = false;
         if (minMult == 0) {
             //check if the assignment has a parentgroup with a min. multiplicity of 0
             if (original.hasParentGroupAssignment() && original.getParentGroupAssignment().getMultiplicity().getMin() == 0) {
                 changeOk = getGroupInstanceMultiplicity(con, original.getParentGroupAssignment().getGroup().getId(), true) == 0;
             } else if (original.getMultiplicity().getMin() == 0) {
                 //check if sub assignments are required
                 changeOk = !original.hasMandatorySubAssignments();
             } else
                 changeOk = true;
         }
         if (!changeOk && minMult < modified.getMultiplicity().getMin())
             throw new FxUpdateException("ex.structure.modification.contentExists", "minimumMultiplicity");
     }
 
     /**
      * Updates a group assignment
      *
      * @param con   a valid and open connection
      * @param group the FxGroupAssignment to be changed
      * @return returns true if changes were made to the group assignment
      * @throws FxApplicationException on errors
      */
     private boolean updateGroupAssignment(Connection con, FxGroupAssignmentEdit group) throws FxApplicationException {
         if (group.isNew())
             throw new FxInvalidParameterException("ex.structure.assignment.update.new", group.getXPath());
         final StringBuilder sql = new StringBuilder(1000);
         boolean changes = false;
         boolean success = false;
         StringBuilder changesDesc = new StringBuilder(200);
         FxGroupAssignment org = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(group.getId());
         PreparedStatement ps = null;
         try {
             sql.setLength(0);
             // enable / disable an assignment
             if (org.isEnabled() != group.isEnabled()) {
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("enabled=").append(group.isEnabled());
                 //apply for all child groups and properties as well!
                 if (org.getAssignments().size() > 0)
                     changesDesc.append(", ").append(group.isEnabled() ? "en" : "dis").append("abled child assignments: ");
                 for (FxAssignment as : org.getAssignments()) {
                     changesDesc.append(as.getXPath()).append(',');
                 }
                 if (changesDesc.charAt(changesDesc.length() - 1) == ',')
                     changesDesc.deleteCharAt(changesDesc.length() - 1);
                 if (!group.isEnabled())
                     removeAssignment(org.getId(), true, false, true, false);
                 else {
                     StringBuilder affectedAssignment = new StringBuilder(500);
                     affectedAssignment.append(org.getId());
                     for (FxAssignment as : org.getAllChildAssignments())
                         affectedAssignment.append(",").append(as.getId());
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET ENABLED=? WHERE ID IN (" + affectedAssignment + ")");
                     ps.setBoolean(1, true);
                     ps.executeUpdate();
                     ps.close();
                 }
                 changes = true;
             }
             // set the assignment's position
             if (org.getPosition() != group.getPosition()) {
                 int finalPos = setAssignmentPosition(con, group.getId(), group.getPosition());
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("position=").append(finalPos);
                 changes = true;
             }
             // change the assignment's default multiplicity (will be auto-adjusted to a valid value in FxGroupAssignmentEdit)
             if (org.getDefaultMultiplicity() != group.getDefaultMultiplicity()) {
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET DEFMULT=? WHERE ID=?");
                 ps.setInt(1, group.getDefaultMultiplicity());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 ps.close();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("defaultMultiplicity=").append(group.getDefaultMultiplicity());
                 changes = true;
             }
             // change the assignment's multiplicity
             final boolean needMinChange = org.getMultiplicity().getMin() != group.getMultiplicity().getMin();
             final boolean needMaxChange = org.getMultiplicity().getMax() != group.getMultiplicity().getMax();
             if (needMinChange || needMaxChange) {
                 if (org.getGroup().mayOverrideBaseMultiplicity()) {
                     if (needMinChange)
                         checkChangeGroupAssignmentMinMultiplicity(con, org, group);
                     if (needMaxChange && getGroupInstanceMultiplicity(con, org.getGroup().getId(), false) > group.getMultiplicity().getMax())
                         throw new FxUpdateException("ex.structure.modification.contentExists", "maximumMultiplicity");
                 } else {
                     throw new FxUpdateException("ex.structure.group.assignment.overrideBaseMultiplicityNotEnabled", org.getGroup().getId());
                 }
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET MINMULT=? ,MAXMULT=? WHERE ID=?");
                 ps.setInt(1, group.getMultiplicity().getMin());
                 ps.setInt(2, group.getMultiplicity().getMax());
                 ps.setLong(3, group.getId());
                 ps.executeUpdate();
                 ps.close();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("multiplicity=").append(group.getMultiplicity());
                 changes = true;
             }
             // set the assignment's position
             if (org.getPosition() != group.getPosition()) {
                 int finalPos = setAssignmentPosition(con, group.getId(), group.getPosition());
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("position=").append(finalPos);
                 changes = true;
             }
             // set the XPath (and the alias) of a group assignment
             if (!org.getXPath().equals(group.getXPath()) || !org.getAlias().equals(group.getAlias())) {
                 if (!XPathElement.isValidXPath(XPathElement.stripType(group.getXPath())) ||
                         group.getAlias().equals(XPathElement.lastElement(XPathElement.stripType(org.getXPath())).getAlias()))
                     throw new FxUpdateException("ex.structure.assignment.noXPath");
                 // generate correct XPATH
                 if (!group.getXPath().startsWith(group.getAssignedType().getName()))
                     group.setXPath(group.getAssignedType().getName() + group.getXPath());
                 //avoid duplicates
                 if (org.getAssignedType().isXPathValid(group.getXPath(), true))
                     throw new FxUpdateException("ex.structure.assignment.exists", group.getXPath(), group.getAssignedType().getName());
                 // update db entries
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET XPATH=?, XALIAS=? WHERE ID=?");
                 ps.setString(1, group.getXPath());
                 ps.setString(2, group.getAlias());
                 ps.setLong(3, group.getId());
                 ps.executeUpdate();
                 ps.close();
                 // update the relevant content instances
                 ContentStorage storage = StorageManager.getContentStorage(TypeStorageMode.Hierarchical);
                 storage.updateXPath(con, group.getId(), XPathElement.stripType(org.getXPath()),
                         XPathElement.stripType(group.getXPath()));
                 //update all child assignments
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET XPATH=? WHERE ID=?");
                 for (FxAssignment child : org.getAllChildAssignments()) {
                     ps.setString(1, group.getXPath() + child.getXPath().substring(org.getXPath().length()));
                     ps.setLong(2, child.getId());
                     ps.executeUpdate();
                     storage.updateXPath(con, child.getId(), XPathElement.stripType(child.getXPath()),
                             XPathElement.stripType(group.getXPath() + child.getXPath().substring(org.getXPath().length())));
                 }
                 ps.close();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("xPath=").append(group.getXPath()).append(",alias=").append(group.getAlias());
                 changes = true;
             }
             // update label
             if (org.getLabel() != null && !org.getLabel().equals(group.getLabel()) ||
                     org.getLabel() == null && group.getLabel() != null ||
                     org.getHint() != null && !org.getHint().equals(group.getHint()) ||
                     org.getHint() == null && group.getHint() != null) {
                 Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()}, con,
                         TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", group.getId());
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("label=").append(group.getLabel()).append(',');
                 changesDesc.append("hint=").append(group.getHint()).append(',');
                 changes = true;
             }
             //update SystemInternal flag, this is a one way function, so it can only be set, but not reset!!
             if (!org.isSystemInternal() && group.isSystemInternal() && FxContext.getUserTicket().isGlobalSupervisor()) {
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET SYSINTERNAL=? WHERE ID=?");
                 ps.setBoolean(1, group.isSystemInternal());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 ps.close();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("systemInternal=").append(group.isSystemInternal());
                 changes = true;
             }
             // change GroupMode
             // OneOf --> AnyOf always allowed, AnyOf --> OneOf not allowed if content exists
             if (org.getMode().getId() != group.getMode().getId()) {
                 if (org.getMode().equals(GroupMode.AnyOf) && group.getMode().equals(GroupMode.OneOf)) {
                     if (getGroupInstanceMultiplicity(con, org.getGroup().getId(), true) > 0) {
                         throw new FxUpdateException(LOG, "ex.structure.group.assignment.modeChangeError");
                     }
                 }
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET GROUPMODE=? WHERE ID=?");
                 ps.setLong(1, group.getMode().getId());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 ps.close();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("groupMode=").append(group.getMode().getId());
                 changes = true;
             }
             // change the parentgroupassignment
             // TODO: change the parent group assignment & failure conditions
 //            if (org.getParentGroupAssignment().getId() != group.getParentGroupAssignment().getId()) {
 //            }
             // change the group assignment options
             if (updateGroupAssignmentOptions(con, group)) {
                 changesDesc.append(",options:");
                 List<FxStructureOption> options = group.getOptions();
                 for (FxStructureOption option : options) {
                     changesDesc.append(option.getKey()).append("=").append(option.getValue()).append(" overridable=").
                             append(option.isOverridable()).append(" isSet=").append(option.isSet()).append(" isInherited=").
                             append(option.getIsInherited());
                 }
                 changes = true;
             }
 
             if (changes)
                 htracker.track(group.getAssignedType(), "history.assignment.updateGroupAssignment", group.getXPath(), group.getAssignedType().getId(), group.getAssignedType().getName(),
                         group.getGroup().getId(), group.getGroup().getName(), changesDesc.toString());
 
             success = true;
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(e);
             EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.assignment.group.exists", group.getAlias(), group.getXPath());
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
             if (!success) {
                 EJBUtils.rollback(ctx);
             }
         }
         return changes;
     }
 
     /**
      * Set an assignments position, updating positions of all assignments in the same hierarchy level
      *
      * @param con          an open and valid connection
      * @param assignmentId the id of the assignment with the desired position
      * @param position     desired position
      * @return the position that "really" was assigned
      * @throws FxUpdateException           on errors
      * @throws FxInvalidParameterException if the position is too high
      */
     private int setAssignmentPosition(Connection con, long assignmentId, int position) throws FxUpdateException, FxInvalidParameterException {
         if (position < 0)
             position = 0;
         if (position > FxAssignment.POSITION_BOTTOM)
             throw new FxInvalidParameterException("position", "ex.structure.assignment.pos.tooHigh", position, FxAssignment.POSITION_BOTTOM);
         PreparedStatement ps = null, ps2 = null;
         int retPosition = position;
         try {
             ps = con.prepareStatement("SELECT TYPEDEF, PARENTGROUP, POS, SYSINTERNAL FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE ID=?");
             ps.setLong(1, assignmentId);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 return position; //no record exists
             long typeId = rs.getLong(1);
             long parentGroupId = rs.getLong(2);
             int orgPos = rs.getInt(3);
             boolean sysinternal = rs.getBoolean(4);
             if (orgPos == position)
                 return retPosition; //no need to change anything
 
             if (!sysinternal && parentGroupId == FxAssignment.NO_PARENT &&
                     position < CacheAdmin.getEnvironment().getSystemInternalRootPropertyAssignments().size()) {
                 //adjust position to be above the sysinternal properties if connected to the root group
                 position += CacheAdmin.getEnvironment().getSystemInternalRootPropertyAssignments().size();
             }
 
             //move all positions in a range of 10000+ without gaps
             ps.close();
             ps = con.prepareStatement("SELECT ID FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE TYPEDEF=? AND PARENTGROUP=? ORDER BY POS");
             ps.setLong(1, typeId);
             ps.setLong(2, parentGroupId);
             rs = ps.executeQuery();
             ps2 = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET POS=? WHERE ID=?");
             int counter = 10000;
             while (rs != null && rs.next()) {
                 ps2.setInt(1, counter++);
                 ps2.setLong(2, rs.getLong(1));
                 ps2.addBatch();
             }
             ps2.executeBatch();
 
             ps.close();
             ps = con.prepareStatement("SELECT ID FROM " + TBL_STRUCT_ASSIGNMENTS +
                     " WHERE TYPEDEF=? AND PARENTGROUP=? AND POS>=? AND ID<>? ORDER BY POS");
             ps.setLong(1, typeId);
             ps.setLong(2, parentGroupId);
             ps.setInt(3, 10000);
             ps.setLong(4, assignmentId);
             rs = ps.executeQuery();
             int currPos = 0;
             boolean written = false;
             while (rs != null && rs.next()) {
                 ps2.setInt(1, currPos);
                 if (!written && currPos == position) {
                     written = true;
                     retPosition = currPos;
                     ps2.setLong(2, assignmentId);
                     ps2.addBatch();
                     ps2.setInt(1, ++currPos);
                 }
                 ps2.setLong(2, rs.getLong(1));
                 ps2.addBatch();
                 currPos++;
             }
             if (!written) {
                 //last element
                 retPosition = currPos;
                 ps2.setInt(1, currPos);
                 ps2.setLong(2, assignmentId);
                 ps2.addBatch();
             }
             ps2.executeBatch();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //ignore
             }
             try {
                 if (ps2 != null)
                     ps2.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
         return retPosition;
     }
 
     /**
      * Creates a group assignment
      *
      * @param con                  a valid and open connection
      * @param sql                  an instance of StringBuilder
      * @param group                an instance of the FxGroupAssignment to be persisted
      * @param createSubAssignments if true calls createGroupAssignment is called recursively to create sub assignments
      * @return returns the assignmentId
      * @throws FxApplicationException on errors
      */
     private long createGroupAssignment(Connection con, StringBuilder sql, FxGroupAssignmentEdit group, boolean createSubAssignments) throws FxApplicationException {
         if (!group.isNew())
             throw new FxInvalidParameterException("ex.structure.assignment.create.existing", group.getXPath());
         if (sql == null) {
             sql = new StringBuilder(1000);
         }
         PreparedStatement ps = null;
         long newAssignmentId;
         try {
             FxGroupAssignment thisGroupAssignment;
             String XPath;
             if (!group.getXPath().startsWith(group.getAssignedType().getName())) {
                 if (group.getAlias() != null)
                     XPath = XPathElement.buildXPath(false, group.getAssignedType().getName(), XPathElement.stripType(group.getXPath()));
                 else
                     XPath = "/";
             } else
                 XPath = group.getXPath();
             if (group.getAlias() != null) {
                 sql.setLength(0);
                 sql.append("INSERT INTO ").append(TBL_STRUCT_ASSIGNMENTS).
                         //               1  2     3       4       5       6       7       8   9     10     11   12          13     14          15
                                 append("(ID,ATYPE,ENABLED,TYPEDEF,MINMULT,MAXMULT,DEFMULT,POS,XPATH,XALIAS,BASE,PARENTGROUP,AGROUP,SYSINTERNAL,GROUPMODE)" +
                                 "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                 ps = con.prepareStatement(sql.toString());
                 newAssignmentId = seq.getId(FxSystemSequencer.ASSIGNMENT);
                 ps.setLong(1, newAssignmentId);
                 ps.setInt(2, FxAssignment.TYPE_GROUP);
                 ps.setBoolean(3, group.isEnabled());
                 ps.setLong(4, group.getAssignedType().getId());
                 ps.setInt(5, group.getMultiplicity().getMin());
                 ps.setInt(6, group.getMultiplicity().getMax());
                 ps.setInt(7, group.getDefaultMultiplicity());
                 int position = getValidPosition(con, sql, group.getPosition(), group.getAssignedType().getId(), group.getParentGroupAssignment());
                 ps.setInt(8, position);
                 ps.setString(9, XPath);
                 ps.setString(10, group.getAlias());
                 if (group.getBaseAssignmentId() == FxAssignment.NO_BASE)
                     ps.setNull(11, java.sql.Types.NUMERIC);
                 else
                     ps.setLong(11, group.getBaseAssignmentId());
                 ps.setLong(12, group.getParentGroupAssignment() == null ? FxAssignment.NO_PARENT : group.getParentGroupAssignment().getId());
                 ps.setLong(13, group.getGroup().getId());
                 ps.setBoolean(14, group.isSystemInternal());
                 ps.setInt(15, group.getMode().getId());
                 ps.executeUpdate();
                 ps.close();
                 Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()}, con,
                         TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", newAssignmentId);
                 thisGroupAssignment = new FxGroupAssignment(newAssignmentId, true, group.getAssignedType(),
                         group.getAlias(), XPath, position, group.getMultiplicity(), group.getDefaultMultiplicity(),
                         group.getParentGroupAssignment(), group.getBaseAssignmentId(),
                         group.getLabel(), group.getHint(), group.getGroup(), group.getMode(), null);
                 setAssignmentPosition(con, newAssignmentId, group.getPosition());
             } else {
                 thisGroupAssignment = null;
                 newAssignmentId = FxAssignment.NO_PARENT;
             }
             htracker.track(group.getAssignedType(), "history.assignment.createGroupAssignment", XPath, group.getAssignedType().getId(), group.getAssignedType().getName(),
                     group.getGroup().getId(), group.getGroup().getName());
 
 
             // FxStructureOption inheritance
             boolean isInheritedAssignment = FxSharedUtils.checkAssignmentInherited(group);
             if (isInheritedAssignment) {
                 // FxStructureOptions - retrieve only those with an activated "isInherited" flag
                 final List<FxStructureOption> inheritedOpts = FxStructureOption.cloneOptions(group.getOptions(), true);
                 if (inheritedOpts.size() > 0) {
                     storeOptions(con, TBL_STRUCT_GROUP_OPTIONS, "ID", group.getGroup().getId(), newAssignmentId, inheritedOpts);
                 }
             } else {
                 storeOptions(con, TBL_STRUCT_GROUP_OPTIONS, "ID", group.getGroup().getId(), newAssignmentId, group.getOptions());
             }
 
             if (group.getBaseAssignmentId() > 0 && createSubAssignments) {
                 FxGroupAssignment baseGroup = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(group.getBaseAssignmentId());
                 for (FxGroupAssignment ga : baseGroup.getAssignedGroups()) {
                     FxGroupAssignmentEdit gae = new FxGroupAssignmentEdit(ga);
                     gae.setEnabled(group.isEnabled());
                     createGroupAssignment(con, sql, FxGroupAssignmentEdit.createNew(gae, group.getAssignedType(), ga.getAlias(), XPath, thisGroupAssignment), createSubAssignments);
                 }
                 for (FxPropertyAssignment pa : baseGroup.getAssignedProperties()) {
                     FxPropertyAssignmentEdit pae = new FxPropertyAssignmentEdit(pa);
                     pae.setEnabled(group.isEnabled());
                     createPropertyAssignment(con, sql, FxPropertyAssignmentEdit.createNew(pae, group.getAssignedType(), pa.getAlias(), XPath, thisGroupAssignment));
                 }
             }
             try {
                 StructureLoader.reloadAssignments(FxContext.get().getDivisionId());
             } catch (FxApplicationException e) {
                 EJBUtils.rollback(ctx);
                 throw new FxCreateException(e, "ex.cache", e.getMessage());
             }
             if (group.getAssignedType().getId() != FxType.ROOT_ID)
                 createInheritedAssignments(CacheAdmin.getEnvironment().getAssignment(newAssignmentId), con, sql,
                         group.getAssignedType().getDerivedTypes());
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(e);
             EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.assignment.group.exists", group.getAlias(), group.getAssignedType().getName() + group.getXPath());
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxCreateException(e);
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
         }
         return newAssignmentId;
     }
 
     /**
      * Updates a property
      *
      * @param con  a valid and open connection
      * @param prop the instance of FxPropertyEdit to be changed
      * @return returns true if changes were made to the property
      * @throws FxApplicationException on errors
      */
     private boolean updateProperty(Connection con, FxPropertyEdit prop) throws FxApplicationException {
         if (prop.isNew())
             throw new FxInvalidParameterException("ex.structure.property.update.new", prop.getName());
         boolean changes = false;
         boolean success = false;
         StringBuilder changesDesc = new StringBuilder(200);
         final FxEnvironment env = CacheAdmin.getEnvironment();
         FxProperty org = env.getProperty(prop.getId());
         PreparedStatement ps = null;
 
         try {
             if (!org.isSystemInternal() || FxContext.getUserTicket().isGlobalSupervisor()) {
                 // change the multiplicity override prop
                 if (org.mayOverrideBaseMultiplicity() != prop.mayOverrideBaseMultiplicity()) {
                     if (!prop.mayOverrideBaseMultiplicity() && getPropertyInstanceCount(prop.getId()) > 0) {
                         final long minMult = getPropertyInstanceMultiplicity(con, org.getId(), true);
                         if (minMult > 0 && minMult < prop.getMultiplicity().getMin())
                             throw new FxUpdateException("ex.structure.modification.contentExists", "minimumMultiplicity");
                         if (getPropertyInstanceMultiplicity(con, org.getId(), false) > prop.getMultiplicity().getMax())
                             throw new FxUpdateException("ex.structure.modification.contentExists", "maximumMultiplicity");
                     }
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET MAYOVERRIDEMULT=? WHERE ID=?");
                     ps.setBoolean(1, prop.mayOverrideBaseMultiplicity());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("mayOverrideMultiplicity=").append(prop.mayOverrideBaseMultiplicity());
                     changes = true;
                 }
                 // change the props multiplicity
                 if (org.getMultiplicity().getMin() != prop.getMultiplicity().getMin() ||
                         org.getMultiplicity().getMax() != prop.getMultiplicity().getMax()) {
                     if (!prop.mayOverrideBaseMultiplicity()) {
                         if (org.getMultiplicity().getMin() < prop.getMultiplicity().getMin()) {
                             for(FxPropertyAssignment pa: CacheAdmin.getEnvironment().getPropertyAssignments(prop.getId(), true))
                                 checkChangePropertyAssignmentMinMultiplicity(con, pa, prop.getMultiplicity());
                         }
                         if (org.getMultiplicity().getMax() > prop.getMultiplicity().getMax()) {
                             if (getPropertyInstanceMultiplicity(con, org.getId(), false) > prop.getMultiplicity().getMax())
                                 throw new FxUpdateException("ex.structure.modification.contentExists", "maximumMultiplicity");
                         }
                     }
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET DEFMINMULT=? ,DEFMAXMULT=? WHERE ID=?");
                     ps.setInt(1, prop.getMultiplicity().getMin());
                     ps.setInt(2, prop.getMultiplicity().getMax());
                     ps.setLong(3, prop.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("multiplicity=").append(prop.getMultiplicity());
                     changes = true;
                 }
                 //not supported yet
                 if (!org.getName().equals(prop.getName())) {
                     throw new FxUpdateException("ex.structure.modification.notSupported", "name");
                     /*
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET NAME=? WHERE ID=?");
                     ps.setString(1, prop.getName());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("name=").append(prop.getName());
                     changes = true;
                     */
                 }
                 //may only change if there are no existing content instances that use this property already
                 if (org.getDataType().getId() != prop.getDataType().getId()) {
                     if (getPropertyInstanceCount(org.getId()) == 0) {
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET DATATYPE=? WHERE ID=?");
                         ps.setLong(1, prop.getDataType().getId());
                         ps.setLong(2, prop.getId());
                         ps.executeUpdate();
                         ps.close();
                         //FX-858: get all assignments for this property and re-flatten if possible to reflect data type changes
                         final List<FxPropertyAssignment> refAssignments = env.getReferencingPropertyAssignments(prop.getId());
                         final FxFlatStorage fs = FxFlatStorageManager.getInstance();
                         List<FxPropertyAssignment> flattened = new ArrayList<FxPropertyAssignment>(refAssignments.size());
                         for (FxPropertyAssignment refAssignment : refAssignments) {
                             if (refAssignment.isFlatStorageEntry()) {
                                 fs.unflatten(refAssignment);
                                 flattened.add(refAssignment);
                             }
                         }
                         if (flattened.size() > 0) {
                             try {
                                 StructureLoader.reload(con);
                                 final FxEnvironment envNew = CacheAdmin.getEnvironment();
                                 boolean needReload = false;
                                 for (FxPropertyAssignment ref : flattened) {
                                     final FxPropertyAssignment paNew = (FxPropertyAssignment) envNew.getAssignment(ref.getId());
                                     if (fs.isFlattenable(paNew)) {
                                         fs.flatten(fs.getDefaultStorage(), paNew);
                                         needReload = true;
                                     }
                                 }
                                 if (needReload)
                                     StructureLoader.reload(con);
                             } catch (FxCacheException e) {
                                 EJBUtils.rollback(ctx);
                                 throw new FxCreateException(e, "ex.cache", e.getMessage());
                             }
                         }
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("dataType=").append(prop.getDataType().getName());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.contentExists", "dataType");
                 }
                 //may only change if there are no existing content instances that use this property already
                 if (org.getReferencedType() != null && prop.getReferencedType() != null &&
                         org.getReferencedType().getId() != prop.getReferencedType().getId() ||
                         org.hasReferencedType() != prop.hasReferencedType()) {
                     if (getPropertyInstanceCount(org.getId()) == 0) {
                         if (prop.isDefaultValueSet() && (prop.getDefaultValue() instanceof FxReference)) {
                             //check if the type matches the instance
                             checkReferencedType(con, (FxReference) prop.getDefaultValue(), prop.getReferencedType());
                             //check for referencing assignments
                             final List<FxPropertyAssignment> refAssignments = env.getReferencingPropertyAssignments(prop.getId());
                             for (FxPropertyAssignment refAssignment : refAssignments) {
                                 if (refAssignment.hasAssignmentDefaultValue() && refAssignment.getDefaultValue() instanceof FxReference)
                                     checkReferencedType(con, (FxReference) refAssignment.getDefaultValue(), prop.getReferencedType());
                             }
                         }
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET REFTYPE=? WHERE ID=?");
                         ps.setLong(2, prop.getId());
                         if (prop.hasReferencedType()) {
                             ps.setLong(1, prop.getReferencedType().getId());
                         } else
                             ps.setNull(1, java.sql.Types.NUMERIC);
                         ps.executeUpdate();
                         ps.close();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("referencedType=").append(prop.getReferencedType());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.contentExists", "referencedType");
                 }
                 // set fulltext indexing for the property
                 if (org.isFulltextIndexed() != prop.isFulltextIndexed()) {
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET ISFULLTEXTINDEXED=? WHERE ID=?");
                     ps.setBoolean(1, prop.isFulltextIndexed());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("isFulltextIndexed=").append(prop.isFulltextIndexed());
                     FulltextIndexer ft = StorageManager.getFulltextIndexer(con);
                     if (prop.isFulltextIndexed())
                         ft.rebuildIndexForProperty(prop.getId());
                     else
                         ft.removeIndexForProperty(prop.getId());
                     changes = true;
                 }
                 // set ACL override flag
                 if (org.mayOverrideACL() != prop.mayOverrideACL()) {
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET MAYOVERRIDEACL=? WHERE ID=?");
                     ps.setBoolean(1, prop.mayOverrideACL());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("mayOverrideACL=").append(prop.mayOverrideACL());
                     changes = true;
                 }
                 //may only change if there are no existing content instances that use this property already
                 if (org.getReferencedList() != null && prop.getReferencedList() != null &&
                         org.getReferencedList().getId() != prop.getReferencedList().getId() ||
                         org.hasReferencedList() != prop.hasReferencedList()) {
                     if (getPropertyInstanceCount(org.getId()) == 0) {
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET REFLIST=? WHERE ID=?");
                         ps.setLong(2, prop.getId());
                         if (prop.hasReferencedList()) {
                             ps.setLong(1, prop.getReferencedList().getId());
                         } else
                             ps.setNull(1, java.sql.Types.NUMERIC);
                         ps.executeUpdate();
                         ps.close();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("referencedList=").append(prop.getReferencedList());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.contentExists", "referencedList");
                 }
                 // set the unique mode
                 if (org.getUniqueMode() != prop.getUniqueMode()) {
                     boolean allowChange = getPropertyInstanceCount(org.getId()) == 0 || prop.getUniqueMode().equals(UniqueMode.None);
                     if (!allowChange) {
                         boolean hasFlat = false;
                         for (FxPropertyAssignment pa : env.getPropertyAssignments(prop.getId(), true)) {
                             if (pa.isFlatStorageEntry()) {
                                 hasFlat = true;
                                 break;
                             }
                         }
                         if (!hasFlat) {
                             boolean check = true;
                             for (FxType type : env.getTypesForProperty(prop.getId())) {
                                 check = StorageManager.getContentStorage(TypeStorageMode.Hierarchical).
                                         uniqueConditionValid(con, prop.getUniqueMode(), prop, type.getId(), null);
                                 if (!check)
                                     break;
                             }
                             allowChange = check;
                         }
                     }
                     if (allowChange) {
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET UNIQUEMODE=? WHERE ID=?");
                         ps.setLong(1, prop.getUniqueMode().getId());
                         ps.setLong(2, prop.getId());
                         ps.executeUpdate();
                         ps.close();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("uniqueMode=").append(prop.getUniqueMode().getId());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.contentExists", "uniqueMode");
                 }
                 // change the property's ACL
                 if (org.getACL().getId() != prop.getACL().getId()) {
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET ACL=? WHERE ID=?");
                     ps.setLong(1, prop.getACL().getId());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("acl=").append(prop.getACL().getId());
                     changes = true;
                 }
                 // change the prop's label
                 if (org.getLabel() != null && !org.getLabel().equals(prop.getLabel()) ||
                         org.getLabel() == null && prop.getLabel() != null ||
                         org.getHint() != null && !org.getHint().equals(prop.getHint()) ||
                         org.getHint() == null && prop.getHint() != null) {
                     Database.storeFxString(new FxString[]{prop.getLabel(), prop.getHint()}, con,
                             TBL_STRUCT_PROPERTIES, new String[]{"DESCRIPTION", "HINT"}, "ID", prop.getId());
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("label=").append(prop.getLabel()).append(',');
                     changesDesc.append("hint=").append(prop.getHint()).append(',');
                     changes = true;
                 }
                 // change the default value
                 if (org.getDefaultValue() != null && !org.getDefaultValue().equals(prop.getDefaultValue()) ||
                         org.getDefaultValue() == null && prop.getDefaultValue() != null) {
                     if (changes)
                         changesDesc.append(',');
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET DEFAULT_VALUE=? WHERE ID=?");
                     FxValue defValue = prop.getDefaultValue();
                     if (defValue instanceof FxBinary) {
                         ContentStorage storage = StorageManager.getContentStorage(TypeStorageMode.Hierarchical);
                         storage.prepareBinary(con, (FxBinary) defValue);
                     }
                     if (prop.isDefaultValueSet() && (defValue instanceof FxReference)) {
                         //check if the type matches the instance
                         checkReferencedType(con, (FxReference) defValue, prop.getReferencedType());
                         //check for referencing assignments
                         final List<FxPropertyAssignment> refAssignments = env.getReferencingPropertyAssignments(prop.getId());
                         for (FxPropertyAssignment refAssignment : refAssignments) {
                             if (refAssignment.hasAssignmentDefaultValue() && refAssignment.getDefaultValue() instanceof FxReference)
                                 checkReferencedType(con, (FxReference) refAssignment.getDefaultValue(), prop.getReferencedType());
                         }
                     }
                     final String _def = defValue == null || defValue.isEmpty() ? null : ConversionEngine.getXStream().toXML(defValue);
                     if (_def == null)
                         ps.setNull(1, java.sql.Types.VARCHAR);
                     else
                         ps.setString(1, _def);
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     ps.close();
                     changesDesc.append("defaultValue=").append(prop.getDefaultValue());
                     changes = true;
                 }
                 //update SystemInternal flag, this is a one way function, so it can only be set, but not reset!!
                 if (!org.isSystemInternal() && prop.isSystemInternal()) {
                     if (FxContext.getUserTicket().isGlobalSupervisor()) {
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET SYSINTERNAL=? WHERE ID=?");
                         ps.setBoolean(1, prop.isSystemInternal());
                         ps.setLong(2, prop.getId());
                         ps.executeUpdate();
                         ps.close();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("systemInternal=").append(prop.isSystemInternal());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.systemInternal.notGlobalSupervisor", prop.getName());
                 }
                 if (org.isMultiLang() != prop.isMultiLang()) {
                     if (getPropertyInstanceCount(org.getId()) > 0)
                         throw new FxUpdateException("ex.structure.modification.contentExists", "multiLang");
                 }
             }
             if (updatePropertyOptions(con, prop)) {
                 changesDesc.append(",options:");
                 List<FxStructureOption> options = prop.getOptions();
                 for (FxStructureOption option : options) {
                     changesDesc.append(option.getKey()).append("=").append(option.getValue()).append(" overridable=").
                             append(option.isOverridable()).append(" isSet=").append(option.isSet());
                 }
                 changes = true;
             }
 
             if (changes)
                 htracker.track("history.assignment.updateProperty", prop.getName(), prop.getId(), changesDesc.toString());
             success = true;
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             /*TODO: Determine if this must be checked
             if (Database.isUniqueConstraintViolation(e))
                 throw new FxEntryExistsException("ex.structure.assignment.property.exists", prop.getAlias(), prop.getXPath());
             */
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
             if (!success) {
                 EJBUtils.rollback(ctx);
             }
         }
         return changes;
     }
 
     /**
      * Check if the type of the given references matches the given type
      *
      * @param con   an open and valid connection
      * @param value the value containing references
      * @param type  the type the references should match
      * @throws FxNotFoundException on errors
      * @throws FxLoadException     on errors
      * @throws FxUpdateException   if type does not match
      */
     private void checkReferencedType(Connection con, FxReference value, FxType type) throws FxNotFoundException, FxLoadException, FxUpdateException {
         ContentStorage storage = StorageManager.getContentStorage(TypeStorageMode.Hierarchical);
         for (long lang : value.getTranslatedLanguages()) {
             FxPK pk = value.getTranslation(lang);
             if (pk.isNew())
                 continue;
             final long pkRefType = storage.getContentTypeId(con, pk);
             if (pkRefType != type.getId())
                 throw new FxUpdateException(LOG, "ex.content.value.invalid.reftype", type,
                         CacheAdmin.getEnvironment().getType(pkRefType));
         }
     }
 
     /**
      * @param con      an existing connection
      * @param original the original property assignment to compare changes
      *                 against and update. if==null, the original will be fetched from the cache
      * @param modified the modified property assignment   @return if any changes were found
      * @return true if the original assignment was modified
      * @throws FxApplicationException on errors
      */
     private boolean updatePropertyAssignment(Connection con, FxPropertyAssignment original,
                                              FxPropertyAssignmentEdit modified) throws FxApplicationException {
         if (modified.isNew())
             throw new FxInvalidParameterException("ex.structure.assignment.update.new", modified.getXPath());
         final StringBuilder sql = new StringBuilder(1000);
         boolean changes = false;
         boolean success = false;
         StringBuilder changesDesc = new StringBuilder(200);
         if (original == null)
             original = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(modified.getId());
         PreparedStatement ps = null;
         try {
             if (con == null)
                 con = Database.getDbConnection();
             sql.setLength(0);
 
             if (!original.isSystemInternal() || FxContext.getUserTicket().isGlobalSupervisor()) {
                 // enable or disable a property assignment, remove the assignment if set to false
                 if (original.isEnabled() != modified.isEnabled()) {
                     if (!modified.isEnabled())
                         removeAssignment(original.getId(), true, false, true, false);
                     else {
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET ENABLED=? WHERE ID=?");
                         ps.setBoolean(1, modified.isEnabled());
                         ps.setLong(2, original.getId());
                         ps.executeUpdate();
                         ps.close();
                     }
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("enabled=").append(modified.isEnabled());
                     changes = true;
                 }
                 // change the property assignment's default multiplicity
                 if (original.getDefaultMultiplicity() != modified.getDefaultMultiplicity()) {
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET DEFMULT=? WHERE ID=?");
                     ps.setInt(1, modified.getDefaultMultiplicity());
                     ps.setLong(2, original.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("defaultMultiplicity=").append(modified.getDefaultMultiplicity());
                     changes = true;
                 }
                 boolean needMin = original.getMultiplicity().getMin() != modified.getMultiplicity().getMin();
                 boolean needMax = original.getMultiplicity().getMax() != modified.getMultiplicity().getMax();
                 // change the property assignment's multiplicity
                 if (needMin || needMax) {
                     if (original.getProperty().mayOverrideBaseMultiplicity()) {
                         //only check if instances exist
                         if (EJBLookup.getTypeEngine().getInstanceCount(original.getAssignedType().getId()) > 0) {
                             if (needMin)
                                 checkChangePropertyAssignmentMinMultiplicity(con, original, modified.getMultiplicity());
                             if (needMax && getPropertyInstanceMultiplicity(con, original.getProperty().getId(), false) > modified.getMultiplicity().getMax())
                                 throw new FxUpdateException("ex.structure.modification.contentExists", "maximumMultiplicity");
                         }
                     } else {
                         throw new FxUpdateException("ex.structure.property.assignment.overrideBaseMultiplicityNotEnabled", original.getProperty().getId());
                     }
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET MINMULT=? ,MAXMULT=? WHERE ID=?");
                     ps.setInt(1, modified.getMultiplicity().getMin());
                     ps.setInt(2, modified.getMultiplicity().getMax());
                     ps.setLong(3, original.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("multiplicity=").append(modified.getMultiplicity());
                     changes = true;
                 }
                 // set the assignment's position
                 if (original.getPosition() != modified.getPosition()) {
                     int finalPos = setAssignmentPosition(con, modified.getId(), modified.getPosition());
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("position=").append(finalPos);
                     changes = true;
                 }
                 // alias / xpath change
                 if (!original.getXPath().equals(modified.getXPath()) || !original.getAlias().equals(modified.getAlias())) {
                     if (!XPathElement.isValidXPath(XPathElement.stripType(modified.getXPath())) ||
                             modified.getAlias().equals(XPathElement.lastElement(XPathElement.stripType(original.getXPath())).getAlias()))
                         throw new FxUpdateException("ex.structure.assignment.noXPath");
                     // generate correct XPATH
                     if (!modified.getXPath().startsWith(modified.getAssignedType().getName()))
                         modified.setXPath(modified.getAssignedType().getName() + modified.getXPath());
                     //avoid duplicates
                     if (original.getAssignedType().isXPathValid(modified.getXPath(), true))
                         throw new FxUpdateException("ex.structure.assignment.exists", modified.getXPath(), modified.getAssignedType().getName());
                     // update db entries
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET XPATH=?, XALIAS=? WHERE ID=?");
                     ps.setString(1, modified.getXPath());
                     ps.setString(2, modified.getAlias());
                     ps.setLong(3, modified.getId());
                     ps.executeUpdate();
                     ps.close();
                     // update the relevant content instances
                     ContentStorage storage = StorageManager.getContentStorage(TypeStorageMode.Hierarchical);
                     storage.updateXPath(con, modified.getId(), XPathElement.stripType(original.getXPath()),
                             XPathElement.stripType(modified.getXPath()));
 
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("xPath=").append(modified.getXPath()).append(",alias=").append(modified.getAlias());
                     changes = true;
                 }
                 // change the assignment's ACL
                 if (original.getACL().getId() != modified.getACL().getId()) {
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET ACL=? WHERE ID=?");
                     ps.setLong(1, modified.getACL().getId());
                     ps.setLong(2, original.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("acl=").append(modified.getACL().getId());
                     changes = true;
                 }
                 // options are stored via storeOption method
                 if (original.isMultiLang() != modified.isMultiLang()) {
                     //Multi->Single: lang=system, values of the def. lang. are used, other are discarded
                     //Single->Multi: lang=default language
                     if (!original.getProperty().mayOverrideMultiLang())
                         //noinspection ThrowableInstanceNeverThrown
                         throw new FxUpdateException("ex.structure.assignment.overrideNotAllowed.multiLang", original.getXPath(),
                                 original.getProperty().getName()).setAffectedXPath(original.getXPath(), FxContentExceptionCause.MultiLangOverride);
                     if (modified.isFlatStorageEntry() && getAssignmentInstanceCount(modified.getId()) > 0)
                         //noinspection ThrowableInstanceNeverThrown
                         throw new FxUpdateException("ex.structure.assignment.overrideNotSupported.multiLang", original.getXPath(),
                                 original.getProperty().getName()).setAffectedXPath(original.getXPath(), FxContentExceptionCause.MultiLangOverride);
                     StorageManager.getContentStorage(TypeStorageMode.Hierarchical).
                             updateMultilanguageSettings(con, original.getId(), original.isMultiLang(), modified.isMultiLang(), modified.getDefaultLanguage());
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("multiLang=").append(modified.isMultiLang());
                     changes = true;
                 }
                 // change the assignment's label
                 if (original.getLabel() != null && !original.getLabel().equals(modified.getLabel()) ||
                         original.getLabel() == null && modified.getLabel() != null ||
                         original.getHint() != null && !original.getHint().equals(modified.getHint()) ||
                         original.getHint() == null && modified.getHint() != null) {
                     Database.storeFxString(new FxString[]{modified.getLabel(), modified.getHint()}, con,
                             TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", original.getId());
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("label=").append(modified.getLabel()).append(',');
                     changesDesc.append("hint=").append(modified.getHint()).append(',');
                     changes = true;
                 }
                 // change the assigment's default value
                 if (original.getDefaultValue() != null && !original.getDefaultValue().equals(modified.getDefaultValue()) ||
                         original.getDefaultValue() == null && modified.getDefaultValue() != null ||
                         original.hasAssignmentDefaultValue() != modified.hasAssignmentDefaultValue()) {
                     if (changes)
                         changesDesc.append(',');
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET DEFAULT_VALUE=? WHERE ID=?");
                     FxValue defValue = modified.getDefaultValue();
                     if (defValue instanceof FxBinary && modified.hasAssignmentDefaultValue()) {
                         ContentStorage storage = StorageManager.getContentStorage(modified.getAssignedType().getStorageMode());
                         storage.prepareBinary(con, (FxBinary) defValue);
                     }
                     final String _def = defValue == null || defValue.isEmpty() ? null : ConversionEngine.getXStream().toXML(defValue);
                     if (_def != null && modified.hasAssignmentDefaultValue() && (modified.getDefaultValue() instanceof FxReference)) {
                         //check if the type matches the instance
                         checkReferencedType(con, (FxReference) modified.getDefaultValue(), modified.getProperty().getReferencedType());
                     }
                     if (_def == null || !modified.hasAssignmentDefaultValue())
                         ps.setNull(1, java.sql.Types.VARCHAR);
                     else
                         ps.setString(1, _def);
                     ps.setLong(2, original.getId());
                     ps.executeUpdate();
                     ps.close();
                     changesDesc.append("defaultValue=").append(original.getDefaultValue());
                     changes = true;
                 }
                 // change the default language
                 if (original.getDefaultLanguage() != modified.getDefaultLanguage()) {
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET DEFLANG=? WHERE ID=?");
                     ps.setInt(1, (int) modified.getDefaultLanguage());
                     ps.setLong(2, original.getId());
                     ps.executeUpdate();
                     ps.close();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("defaultLanguage=").append(modified.getDefaultLanguage());
                     changes = true;
                 }
                 //update SystemInternal flag, this is a one way function, so it can only be set, but not reset!!
                 if (!original.isSystemInternal() && modified.isSystemInternal()) {
                     if (FxContext.getUserTicket().isGlobalSupervisor()) {
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET SYSINTERNAL=? WHERE ID=?");
                         ps.setBoolean(1, modified.isSystemInternal());
                         ps.setLong(2, original.getId());
                         ps.executeUpdate();
                         ps.close();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("systemInternal=").append(modified.isSystemInternal());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.systemInternal.notGlobalSupervisor", modified.getLabel());
                 }
                 // change the parentgroupassignment
 //                if (original.getParentGroupAssignment().getId() != modified.getParentGroupAssignment().getId()) {
 //                }
                 /*
                 if (changes) {
                     //propagate changes to derived assignments
                     List<FxAssignment> children = CacheAdmin.getEnvironment().getDerivedAssignments(modified.getId());
                     for (FxAssignment as : children) {
                         if (as instanceof FxPropertyAssignment) {
                             updatePropertyAssignment(null, null, null, (FxPropertyAssignment) as, modified);
                         }
                     }
                     //if there are changes AND the assignment is a child,
                     // break the inheritance and make it a "ROOT_BASE" assignment
                     if(original.isDerivedAssignment()) {
                         if (ps!=null)
                             ps.close();
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET BASE=? WHERE ID=?");
                         ps.setNull(1, Types.NUMERIC);
                         ps.setLong(2, original.getId());
                         ps.executeUpdate();
                            changesDesc.append(",baseAssignment=null");
                     }
                }
                */
             } else
                 throw new FxUpdateException("ex.structure.systemInternal.forbidden", modified.getLabel());
 
             if (updatePropertyAssignmentOptions(con, original, modified)) {
                 changesDesc.append(",options:");
                 List<FxStructureOption> options = modified.getOptions();
                 for (FxStructureOption option : options) {
                     changesDesc.append(option.getKey()).append("=").append(option.getValue()).append(" overridable=").
                             append(option.isOverridable()).append(" isSet=").append(option.isSet()).append("isInherited").
                             append(option.getIsInherited());
                 }
                 changes = true;
             }
 
             //TODO: compare all possible modifications
             if (changes) {
                 htracker.track(modified.getAssignedType(), "history.assignment.updatePropertyAssignment", original.getXPath(), modified.getAssignedType().getId(), modified.getAssignedType().getName(),
                         modified.getProperty().getId(), modified.getProperty().getName(), changesDesc.toString());
             }
             success = true;
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(e);
             EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.assignment.property.exists", original.getAlias(), original.getXPath());
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
             if (!success) {
                 EJBUtils.rollback(ctx);
             }
         }
         return changes;
     }
 
     /**
      * Check if a property assignments minum multiplicity may be changed and throw an exception if not
      *
      * @param con          an open and valid connection
      * @param original     original assignment
      * @param modifiedMult modified multiplicity
      * @throws SQLException      db error
      * @throws FxUpdateException change is not allowed
      */
     private void checkChangePropertyAssignmentMinMultiplicity(Connection con, FxPropertyAssignment original, FxMultiplicity modifiedMult) throws SQLException, FxUpdateException {
         final long minMult = getPropertyInstanceMultiplicity(con, original.getProperty().getId(), true);
         boolean changeOk = false;
         //check if the assignment has a parentgroup with a min. multiplicity of 0 and
         if (minMult == 0 && original.hasParentGroupAssignment() && original.getParentGroupAssignment().getMultiplicity().getMin() == 0) {
             changeOk = getGroupInstanceMultiplicity(con, original.getParentGroupAssignment().getGroup().getId(), true) == 0;
         }
         if (!changeOk && minMult < modifiedMult.getMin())
             throw new FxUpdateException("ex.structure.modification.contentExists", "minimumMultiplicity");
     }
 
     /**
      * Creates a property assignment
      *
      * @param con a valid and open connection
      * @param sql an instance of StringBuilder
      * @param pa  an instance of FxPropertyAssignmentEdit to be persisted
      * @return the property assignmentId
      * @throws FxApplicationException on errors
      */
     private long createPropertyAssignment(Connection con, StringBuilder sql, FxPropertyAssignmentEdit pa) throws FxApplicationException {
         if (!pa.isNew())
             throw new FxInvalidParameterException("ex.structure.assignment.create.existing", pa.getXPath());
         if (sql == null) {
             sql = new StringBuilder(1000);
         }
         PreparedStatement ps = null;
         long newAssignmentId;
         try {
             sql.setLength(0);
             sql.append("INSERT INTO ").append(TBL_STRUCT_ASSIGNMENTS).
                     //               1  2     3       4       5       6       7       8   9     10     11   12          13
                             append("(ID,ATYPE,ENABLED,TYPEDEF,MINMULT,MAXMULT,DEFMULT,POS,XPATH,XALIAS,BASE,PARENTGROUP,APROPERTY," +
                             //14 15      16          17
                             "ACL,DEFLANG,SYSINTERNAL,DEFAULT_VALUE)" +
                             "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
             ps = con.prepareStatement(sql.toString());
             newAssignmentId = seq.getId(FxSystemSequencer.ASSIGNMENT);
             ps.setLong(1, newAssignmentId);
             ps.setInt(2, FxAssignment.TYPE_PROPERTY);
             ps.setBoolean(3, pa.isEnabled());
             ps.setLong(4, pa.getAssignedType().getId());
             ps.setInt(5, pa.getMultiplicity().getMin());
             ps.setInt(6, pa.getMultiplicity().getMax());
             ps.setInt(7, pa.getDefaultMultiplicity());
             int position = getValidPosition(con, sql, pa.getPosition(), pa.getAssignedType().getId(), pa.getParentGroupAssignment());
             ps.setInt(8, position);
             String XPath;
             if (!pa.getXPath().startsWith(pa.getAssignedType().getName()))
                 XPath = XPathElement.buildXPath(false, pa.getAssignedType().getName(), pa.getXPath());
             else
                 XPath = pa.getXPath();
             ps.setString(9, XPath);
             ps.setString(10, pa.getAlias());
             if (pa.getBaseAssignmentId() == FxAssignment.NO_BASE)
                 ps.setNull(11, Types.NUMERIC);
             else
                 ps.setLong(11, pa.getBaseAssignmentId());
             ps.setLong(12, pa.getParentGroupAssignment() == null ? FxAssignment.NO_PARENT : pa.getParentGroupAssignment().getId());
             ps.setLong(13, pa.getProperty().getId());
             ps.setLong(14, pa.getACL().getId());
             ps.setInt(15, pa.hasDefaultLanguage() ? (int) pa.getDefaultLanguage() : (int) FxLanguage.SYSTEM_ID);
             ps.setBoolean(16, pa.isSystemInternal());
             FxValue defValue = pa.getDefaultValue();
             if (defValue instanceof FxBinary) {
                 ContentStorage storage = StorageManager.getContentStorage(pa.getAssignedType().getStorageMode());
                 storage.prepareBinary(con, (FxBinary) defValue);
             }
             final String _def = defValue == null || defValue.isEmpty() ? null : ConversionEngine.getXStream().toXML(defValue);
             if (_def == null)
                 ps.setNull(17, java.sql.Types.VARCHAR);
             else
                 ps.setString(17, _def);
             ps.executeUpdate();
             ps.close();
             Database.storeFxString(new FxString[]{pa.getLabel(), pa.getHint()}, con,
                     TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", newAssignmentId);
             htracker.track(pa.getAssignedType(), "history.assignment.createPropertyAssignment", XPath, pa.getAssignedType().getId(), pa.getAssignedType().getName(),
                     pa.getProperty().getId(), pa.getProperty().getName());
 
             // FxStructureOption inheritance
             boolean isInheritedAssignment = FxSharedUtils.checkAssignmentInherited(pa);
             if (isInheritedAssignment) {
                 // FxStructureOptions - retrieve only those with an activated "isInherited" flag
                 final List<FxStructureOption> inheritedOpts = FxStructureOption.cloneOptions(pa.getOptions(), true);
                 if (inheritedOpts.size() > 0) {
                     storeOptions(con, TBL_STRUCT_PROPERTY_OPTIONS, "ID", pa.getProperty().getId(), newAssignmentId, inheritedOpts);
                 }
             } else {
                 storeOptions(con, TBL_STRUCT_PROPERTY_OPTIONS, "ID", pa.getProperty().getId(), newAssignmentId, pa.getOptions());
             }
 
             setAssignmentPosition(con, newAssignmentId, pa.getPosition());
 
             if (!pa.isSystemInternal()) {
                 if (divisionConfig.isFlatStorageEnabled() && divisionConfig.get(SystemParameters.FLATSTORAGE_AUTO)
                         && !FxEnvironmentUtils.isNoImmediateFlattening()) {
                     final FxFlatStorage fs = FxFlatStorageManager.getInstance();
                     if (fs.isFlattenable(pa)) {
                         try {
                             StructureLoader.reload(con);
                         } catch (FxCacheException e) {
                             EJBUtils.rollback(ctx);
                             throw new FxCreateException(e, "ex.cache", e.getMessage());
                         }
                         fs.flatten(con, fs.getDefaultStorage(), (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(newAssignmentId));
                     }
                 }
 
                 //only need a reload and inheritance handling if the property is not system internal
                 //since system internal properties are only created from the type engine we don't have to care
                 try {
                     StructureLoader.reloadAssignments(FxContext.get().getDivisionId());
                 } catch (FxApplicationException e) {
                     EJBUtils.rollback(ctx);
                     throw new FxCreateException(e, "ex.cache", e.getMessage());
                 }
                 if (pa.getAssignedType().getId() != FxType.ROOT_ID)
                     createInheritedAssignments(CacheAdmin.getEnvironment().getAssignment(newAssignmentId), con, sql,
                             pa.getAssignedType().getDerivedTypes());
             }
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = StorageManager.isUniqueConstraintViolation(e);
             if (!ctx.getRollbackOnly())
                 EJBUtils.rollback(ctx);
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.assignment.property.exists", pa.getAlias(), pa.getAssignedType().getName() + pa.getXPath());
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
         }
         return newAssignmentId;
     }
 
     /**
      * Get a valid position for the assignment within the same hierarchy.
      * Probes for the desired position first and if taken returns the next available
      *
      * @param con                   connection ( has to be valid and open!)
      * @param sql                   StringBuilder for the statement
      * @param desiredPos            desired position
      * @param typeId                FxType id
      * @param parentGroupAssignment the parent gorup assignment or <code>null</code> if assigned to the root
      * @return a valid position for the assignment
      * @throws SQLException      on errors
      * @throws FxCreateException if no result could be retrieved
      */
     private int getValidPosition(Connection con, StringBuilder sql, int desiredPos, long typeId, FxGroupAssignment parentGroupAssignment) throws SQLException, FxCreateException {
         PreparedStatement ps = null;
         sql.setLength(0);
         if (desiredPos >= FxAssignment.POSITION_BOTTOM) {
             sql.append("SELECT MAX(POS+1) FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE TYPEDEF=? AND PARENTGROUP=?");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, typeId);
             ps.setLong(2, parentGroupAssignment == null ? FxAssignment.NO_PARENT : parentGroupAssignment.getId());
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next())
                 return rs.getInt(1);
             throw new FxCreateException("ex.structure.position.failed", typeId, parentGroupAssignment == null ? FxAssignment.NO_PARENT : parentGroupAssignment.getId(), desiredPos);
         }
         sql.append("SELECT ").append(StorageManager.getIfFunction(
                 //                                                                   1                 2         3
                 "(SELECT COUNT(ID) FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE TYPEDEF=? AND PARENTGROUP=? AND POS=?)>0",
                 //                                                                                4                 5
                 "(SELECT COALESCE(MAX(POS)+1,0) FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE TYPEDEF=? AND PARENTGROUP=?)",
                 //6
                 "?")).append(StorageManager.getFromDual());
         try {
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, typeId);
             ps.setLong(2, parentGroupAssignment == null ? FxAssignment.NO_PARENT : parentGroupAssignment.getId());
             ps.setInt(3, desiredPos);
             ps.setLong(4, typeId);
             ps.setLong(5, parentGroupAssignment == null ? FxAssignment.NO_PARENT : parentGroupAssignment.getId());
             ps.setInt(6, desiredPos);
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next())
                 return rs.getInt(1);
             throw new FxCreateException("ex.structure.position.failed", typeId, parentGroupAssignment == null ? FxAssignment.NO_PARENT : parentGroupAssignment.getId(), desiredPos);
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeAssignment(long assignmentId, boolean removeSubAssignments, boolean removeDerivedAssignments)
             throws FxApplicationException {
         removeAssignment(assignmentId, removeSubAssignments, removeDerivedAssignments, false, false);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeAssignment(long assignmentId) throws FxApplicationException {
         removeAssignment(assignmentId, true, false, false, true);
     }
 
     /**
      * Remove an assignment
      *
      * @param assignmentId             assignment to remove
      * @param removeSubAssignments     if assignment is a group, remove all attached properties and groups?
      * @param removeDerivedAssignments if derivates of this assignment in derived types exist, remove them as well?
      * @param disableAssignment        if false, find all derived assignments, flag them as 'regular' assignments and set them as new base
      * @param allowDerivedRemoval      allow removal of derived assignments
      * @throws FxApplicationException on errors
      */
     private void removeAssignment(long assignmentId, boolean removeSubAssignments, boolean removeDerivedAssignments,
                                   boolean disableAssignment, boolean allowDerivedRemoval) throws FxApplicationException {
         final UserTicket ticket = FxContext.getUserTicket();
         FxPermissionUtils.checkRole(ticket, Role.StructureManagement);
         FxAssignment assignment;
         assignment = CacheAdmin.getEnvironment().getAssignment(assignmentId);
         assert assignment != null : "Assignment retrieved was null";
         if (!disableAssignment) {
             //if removal, check if its a derived assignment which may not be removed
             if (!allowDerivedRemoval && assignment.isDerivedAssignment())
                 throw new FxRemoveException("ex.structure.assignment.delete.derived", assignment.getXPath());
         }
 
         Connection con = null;
         PreparedStatement ps = null;
         StringBuilder sql = new StringBuilder(500);
         try {
             con = Database.getDbConnection();
 
             List<FxAssignment> affectedAssignments = new ArrayList<FxAssignment>(10);
             affectedAssignments.add(assignment);
 
             if (assignment instanceof FxGroupAssignment && removeSubAssignments) {
                 FxGroupAssignment ga = (FxGroupAssignment) assignment;
                 _addSubAssignments(affectedAssignments, ga);
             }
 
 
             if (removeDerivedAssignments) {
                 //find all derived assignments
                 sql.append("SELECT ID FROM ").append(TBL_STRUCT_ASSIGNMENTS).append(" WHERE BASE=?");
                 ps = con.prepareStatement(sql.toString());
                 long prevSize = 0;
                 while (prevSize != affectedAssignments.size()) { //run until no derived assignments are found
                     prevSize = affectedAssignments.size();
                     List<FxAssignment> adds = new ArrayList<FxAssignment>(5);
                     for (FxAssignment check : affectedAssignments) {
                         ps.setLong(1, check.getId());
                         ResultSet rs = ps.executeQuery();
                         if (rs != null && rs.next()) {
                             FxAssignment derived = CacheAdmin.getEnvironment().getAssignment(rs.getLong(1));
                             if (!adds.contains(derived) && !affectedAssignments.contains(derived))
                                 adds.add(derived);
                         }
                     }
                     affectedAssignments.addAll(adds);
                 }
                 ps.close();
                 sql.setLength(0);
             } else if (!disableAssignment) {
                 //find all (directly) derived assignments, flag them as 'regular' assignments and set them as new base
                 breakAssignmentInheritance(con, sql, affectedAssignments.toArray(new FxAssignment[affectedAssignments.size()]));
             }
 
             //security checks
             if (!ticket.isGlobalSupervisor()) {
                 //assignment permission
                 StringBuilder assignmentList = new StringBuilder(200);
                 for (FxAssignment check : affectedAssignments) {
                     assignmentList.append(",").append(check.getId());
                     if (check instanceof FxPropertyAssignment && check.getAssignedType().isUsePropertyPermissions()) {
                         FxPropertyAssignment pa = (FxPropertyAssignment) check;
                         if (!ticket.mayDeleteACL(pa.getACL().getId(), 0/*owner is irrelevant here*/))
                             throw new FxNoAccessException("ex.acl.noAccess.delete", pa.getACL().getName());
                     }
                 }
                 //affected content permission
                 sql.append("SELECT DISTINCT O.ACL FROM ").append(TBL_CONTENT).
                         append(" O WHERE O.ID IN(SELECT D.ID FROM ").append(TBL_CONTENT_DATA).
                         append(" D WHERE D.ASSIGN IN(").append(assignmentList.substring(1)).append("))");
                 java.lang.System.out.println("SQL==" + sql.toString());
                 ps = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 ResultSet rs = ps.executeQuery();
                 while (rs != null && rs.next()) {
                     if (!ticket.mayDeleteACL(rs.getInt(1), 0/*owner is irrelevant here*/))
                         throw new FxNoAccessException("ex.acl.noAccess.delete", CacheAdmin.getEnvironment().getACL(rs.getInt(1)));
                 }
                 ps.close();
             }
 
             if (disableAssignment)
                 sql.append("UPDATE ").append(TBL_STRUCT_ASSIGNMENTS).append(" SET ENABLED=? WHERE ID=?");
             else
                 sql.append("DELETE FROM ").append(TBL_STRUCT_ASSIGNMENTS).append(" WHERE ID=?");
             ps = con.prepareStatement(sql.toString());
 
             //batch remove all multi language entries and content datas
             PreparedStatement psML = null;
             PreparedStatement psData = null;
             PreparedStatement psDataFT = null;
             PreparedStatement psBinaryGet = null;
             PreparedStatement psBinaryRemove = null;
             PreparedStatement psPropertyOptionRemove = null;
             PreparedStatement psGroupOptionRemove = null;
             try {
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_STRUCT_ASSIGNMENTS).append(ML).append(" WHERE ID=?");
                 psML = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_STRUCT_PROPERTY_OPTIONS).append(" WHERE ASSID=?");
                 psPropertyOptionRemove = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_STRUCT_GROUP_OPTIONS).append(" WHERE ASSID=?");
                 psGroupOptionRemove = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_CONTENT_DATA).append(" WHERE ASSIGN=?");
                 psData = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_CONTENT_DATA_FT).append(" WHERE ASSIGN=?");
                 psDataFT = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("SELECT DISTINCT FBLOB FROM ").
                         append(TBL_CONTENT_DATA).append(" WHERE ASSIGN=? AND FBLOB IS NOT NULL");
                 psBinaryGet = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_CONTENT_BINARY).append(" WHERE ID=?");
                 psBinaryRemove = con.prepareStatement(sql.toString());
                 for (FxAssignment ml : affectedAssignments) {
                     if (!disableAssignment) {
                         psML.setLong(1, ml.getId());
                         psML.addBatch();
                     }
                     psData.setLong(1, ml.getId());
                     psData.addBatch();
                     if (ml instanceof FxPropertyAssignment) {
                         if (!disableAssignment) {
                             if (((FxPropertyAssignment) ml).isFlatStorageEntry())
                                 FxFlatStorageManager.getInstance().removeAssignmentMappings(con, ml.getId());
                         }
                         psDataFT.setLong(1, ml.getId());
                         psDataFT.addBatch();
                         psPropertyOptionRemove.setLong(1, ml.getId());
                         psPropertyOptionRemove.addBatch();
                         //only need to remove binaries if its a binary type...
                         switch (((FxPropertyAssignment) ml).getProperty().getDataType()) {
                             case Binary:
                                 psBinaryGet.setLong(1, ml.getId());
                                 ResultSet rs = psBinaryGet.executeQuery();
                                 while (rs != null && rs.next()) {
                                     psBinaryRemove.setLong(1, rs.getLong(1));
                                     psBinaryRemove.addBatch();
                                 }
                         }
                     } else if (ml instanceof FxGroupAssignment) {
                         psGroupOptionRemove.setLong(1, ml.getId());
                         psGroupOptionRemove.addBatch();
                     }
                 }
                 if (!disableAssignment) {
                     psML.executeBatch();
                     psPropertyOptionRemove.executeBatch();
                     psGroupOptionRemove.executeBatch();
                     psBinaryRemove.executeBatch();
                     psDataFT.executeBatch();
                     psData.executeBatch();
                 }
             } finally {
                 Database.closeObjects(AssignmentEngineBean.class, null, psML);
                 Database.closeObjects(AssignmentEngineBean.class, null, psData);
                 Database.closeObjects(AssignmentEngineBean.class, null, psDataFT);
                 Database.closeObjects(AssignmentEngineBean.class, null, psBinaryGet);
                 Database.closeObjects(AssignmentEngineBean.class, null, psBinaryRemove);
                 Database.closeObjects(AssignmentEngineBean.class, null, psGroupOptionRemove);
                 Database.closeObjects(AssignmentEngineBean.class, null, psPropertyOptionRemove);
             }
 
             if (disableAssignment)
                 ps.setBoolean(1, false);
 
             if (affectedAssignments.size() > 1)
                 affectedAssignments = FxStructureUtils.resolveRemoveDependencies(affectedAssignments);
             for (FxAssignment rm : affectedAssignments) {
                 ps.setLong(disableAssignment ? 2 : 1, rm.getId());
                 ps.executeUpdate();
             }
 
             FxStructureUtils.removeOrphanedProperties(con);
             FxStructureUtils.removeOrphanedGroups(con);
             StructureLoader.reload(con);
             htracker.track(assignment.getAssignedType(),
                     disableAssignment ? "history.assignment.remove" : "history.assignment.disable",
                     assignment.getXPath(), assignmentId, removeSubAssignments, removeDerivedAssignments);
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(LOG, e, "ex.cache", e.getMessage());
         } catch (FxLoadException e) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(e);
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
 
     }
 
     /**
      * Find all (directly) derived assignments and flag them as 'regular' assignments and set them as new base
      *
      * @param con         an open and valid connection
      * @param sql         string builder
      * @param assignments the assignments to 'break'
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      * @throws java.sql.SQLException       on errors
      */
     private void breakAssignmentInheritance(Connection con,
                                             StringBuilder sql, FxAssignment... assignments) throws SQLException, FxNotFoundException, FxInvalidParameterException {
         sql.setLength(0);
         sql.append("UPDATE ").append(TBL_STRUCT_ASSIGNMENTS).append(" SET BASE=? WHERE BASE=?"); // AND TYPEDEF=?");
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(sql.toString());
             ps.setNull(1, Types.NUMERIC);
             for (FxAssignment as : assignments) {
                 ps.setLong(2, as.getId());
                 int count = 0;
                 //'toplevel' fix
                 //        for(FxType types: assignment.getAssignedType().getDerivedTypes() ) {
                 //            ps.setLong(3, types.getId());
                 count += ps.executeUpdate();
                 //        }
                 if (count > 0)
                     LOG.info("Updated " + count + " assignments to become the new base assignment");
                 /* sql.setLength(0);
                 //now fix 'deeper' inherited assignments
                 for(FxType types: assignment.getAssignedType().getDerivedTypes() ) {
                     for( FxType subderived: types.getDerivedTypes())
                         _fixSubInheritance(ps, subderived, types.getAssignment(assignment.getXPath()).getId(), assignment.getId());
                 }*/
             }
             ps.close();
             sql.setLength(0);
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
         }
     }
 
     /*private void _fixSubInheritance(PreparedStatement ps, FxType type, long newBase, long assignmentId) throws SQLException, FxInvalidParameterException, FxNotFoundException {
         ps.setLong(1, newBase);
         ps.setLong(2, assignmentId);
         ps.setLong(3, type.getId());
         ps.executeUpdate();
         for( FxType derived: type.getDerivedTypes())
             _fixSubInheritance(ps, derived, newBase, assignmentId);
     }*/
 
     /**
      * Recursively gather all sub assignments of the requested group assignment and add it to the given list
      *
      * @param affectedAssignments list where all sub assignments and the group itself are being put
      * @param ga                  the group assignment to start at
      */
     private void _addSubAssignments(List<FxAssignment> affectedAssignments, FxGroupAssignment ga) {
         affectedAssignments.addAll(ga.getAssignedProperties());
         for (FxGroupAssignment subga : ga.getAssignedGroups()) {
             affectedAssignments.add(subga);
             _addSubAssignments(affectedAssignments, subga);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxPropertyEdit property) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.StructureManagement);
         long returnId = property.getId();
         boolean reload;
         Connection con = null;
         try {
             con = Database.getDbConnection();
             reload = updateProperty(con, property);
             if (reload)
                 StructureLoader.reload(con);
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e, "ex.cache", e.getMessage());
         } catch (FxLoadException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, null);
         }
         return returnId;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxGroupEdit group) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.StructureManagement);
         long returnId = group.getId();
         boolean reload;
         Connection con = null;
         try {
             con = Database.getDbConnection();
             reload = updateGroup(con, group);
             if (reload)
                 StructureLoader.reload(con);
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e, "ex.cache", e.getMessage());
         } catch (FxLoadException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, null);
         }
         return returnId;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long getPropertyInstanceCount(long propertyId) throws FxDbException {
         Connection con = null;
         PreparedStatement ps = null;
         long count = 0;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE TPROP=?");
             ps.setLong(1, propertyId);
             ResultSet rs = ps.executeQuery();
             rs.next();
             count = rs.getLong(1);
             ps.close();
             if (EJBLookup.getDivisionConfigurationEngine().isFlatStorageEnabled()) {
                 //also examine flat storage entries
                 count += FxFlatStorageManager.getInstance().getPropertyInstanceCount(con, propertyId);
             }
         }
         catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         }
         finally {
             Database.closeObjects(AssignmentEngineBean.class, con, ps);
         }
         return count;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long getAssignmentInstanceCount(long assignmentId) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         long count = 0;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE ASSIGN=?");
             ps.setLong(1, assignmentId);
             ResultSet rs = ps.executeQuery();
             rs.next();
             count = rs.getLong(1);
             ps.close();
             if (EJBLookup.getDivisionConfigurationEngine().isFlatStorageEnabled()) {
                 //also examine flat storage entries
                 count += FxFlatStorageManager.getInstance().getAssignmentInstanceCount(con, assignmentId);
             }
         }
         catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         }
         finally {
             Database.closeObjects(AssignmentEngineBean.class, con, ps);
         }
         return count;
     }
 
     /**
      * Get minimum or maximum multiplicity of properties in content instances for a given property
      *
      * @param con        an open and valid Connection
      * @param propertyId requested property
      * @param minimum    true for minimum, false for maximum
      * @return minimum or maximum multiplicity of properties of instances
      * @throws SQLException on errors
      */
     private long getPropertyInstanceMultiplicity(Connection con, long propertyId, boolean minimum) throws SQLException {
         PreparedStatement ps = null;
         long mult = 0;
         try {
             if (minimum)
                ps = con.prepareStatement("SELECT MIN(s.MAXIDX) FROM (SELECT MAX(d.XINDEX) AS \"MAXIDX\",d.ID,d.VER FROM " + TBL_CONTENT_DATA + " d WHERE d.TPROP=? GROUP BY d.ID, d.VER) s");
             else
                 ps = con.prepareStatement("SELECT MAX(XINDEX) FROM " + TBL_CONTENT_DATA + " WHERE TPROP=?");
             ps.setLong(1, propertyId);
             ResultSet rs = ps.executeQuery();
             rs.next();
             mult = rs.getLong(1);
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
         }
         return mult;
     }
 
     /**
      * Get the minimum or maximum multiplicity of groups in content instances for a given group
      *
      * @param con     an open and valid connection
      * @param groupId the requested groupId
      * @param minimum true for minimum, false for maximum
      * @return minium or maximum multiplicity of group instances
      * @throws SQLException on errors
      */
     private long getGroupInstanceMultiplicity(Connection con, long groupId, boolean minimum) throws SQLException {
         PreparedStatement ps = null;
         long mult = 0;
         List<Long> assignmentIds = new ArrayList<Long>();
         try { // retrieve the assignment ids, then the max / min xindex from the content table for the given ids
             ps = con.prepareStatement("SELECT ID FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE AGROUP=?");
             ps.setLong(1, groupId);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 assignmentIds.add(rs.getLong(1));
             }
             ps.close();
 
             if (assignmentIds.size() > 0) {
                 StringBuilder query;
                 if (minimum) {
                    query = new StringBuilder("SELECT MIN(s.MAXIDX) FROM (SELECT MAX(d.XINDEX) AS \"MAXIDX\",d.ID,d.VER FROM " + TBL_CONTENT_DATA + " d WHERE d.ASSIGN=");
                 } else
                     query = new StringBuilder("SELECT MAX(d.XINDEX) FROM " + TBL_CONTENT_DATA + " d WHERE d.ASSIGN=");
                 for (int i = 0; i < assignmentIds.size(); i++) { // build the complete query
                     query.append(assignmentIds.get(i).toString());
                     if (i < assignmentIds.size() - 1) {
                         query.append(" OR d.ASSIGN=");
                     }
                 }
                 if (minimum) {
                     query.append(" GROUP BY d.ID, d.VER) s");
                 }
                 ps = con.prepareStatement(query.toString());
                 rs = ps.executeQuery();
                 rs.next();
                 mult = rs.getLong(1);
                 ps.close();
             }
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, null, ps);
         }
         return mult;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public Map<String, List<FxPropertyAssignment>> getPotentialFlatAssignments(FxType type) {
         return FxFlatStorageManager.getInstance().getPotentialFlatAssignments(type);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public boolean isFlattenable(FxPropertyAssignment pa) {
         return FxFlatStorageManager.getInstance().isFlattenable(pa);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void flattenAssignment(FxPropertyAssignment assignment) throws FxApplicationException {
         try {
             final FxFlatStorage fs = FxFlatStorageManager.getInstance();
             fs.flatten(fs.getDefaultStorage(), assignment);
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw e;
         }
         try {
             StructureLoader.reload(null);
         } catch (FxCacheException e) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(e, "ex.cache", e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void flattenAssignment(String storage, FxPropertyAssignment assignment) throws FxApplicationException {
         try {
             FxFlatStorageManager.getInstance().flatten(storage, assignment);
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw e;
         }
         try {
             StructureLoader.reload(null);
         } catch (FxCacheException e) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(e, "ex.cache", e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void unflattenAssignment(FxPropertyAssignment assignment) throws FxApplicationException {
         try {
             FxFlatStorageManager.getInstance().unflatten(assignment);
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw e;
         }
         try {
             StructureLoader.reload(null);
         } catch (FxCacheException e) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(e, "ex.cache", e.getMessage());
         }
     }
 }
