 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
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
 package com.flexive.core.storage.genericSQL;
 
 import com.flexive.core.Database;
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.storage.FxTreeNodeInfo;
 import com.flexive.core.storage.TreeStorage;
 import com.flexive.shared.*;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.SequencerEngine;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.UserGroup;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.*;
 import java.util.*;
 
 /**
  * Generic tree storage implementation for nested set model trees
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public abstract class GenericTreeStorage implements TreeStorage {
 
     /**
      * Information to be processed when creating a new node (return value helper)
      */
     protected static class NodeCreateInfo {
         long id;
         String name;
         FxPK reference;
 
         /**
          * Ctor
          *
          * @param id        id
          * @param name      name
          * @param reference reference
          */
         public NodeCreateInfo(long id, String name, FxPK reference) {
             this.id = id;
             this.name = name;
             this.reference = reference;
         }
     }
 
 
     private static final transient Log LOG = LogFactory.getLog(GenericTreeStorage.class);
 
     //                                                         1   2   3      4
     protected static final String TREE_LIVE_NODEINFO = "SELECT LFT,RGT,PARENT,TOTAL_CHILDCOUNT," +
             //                                                                        5
             "(SELECT LFT FROM " + getTable(FxTreeMode.Live) + " WHERE ID=NODE.PARENT) PARENTLEFT, " +
             //                                                                        6
             "(SELECT RGT FROM " + getTable(FxTreeMode.Live) + " WHERE ID=NODE.PARENT) PARENTRIGHT," +
             //7    8          9   10       11          12   13                               14  15   16  17   18         19
             "DEPTH,CHILDCOUNT,REF,TEMPLATE,MODIFIED_AT,NAME,tree_getPosition(TRUE,ID,PARENT),ACL,TDEF,VER,STEP,CREATED_BY,MANDATOR " +
             "FROM (SELECT t.LFT,t.RGT,t.PARENT,t.DEPTH,t.TOTAL_CHILDCOUNT,t.CHILDCOUNT,t.REF,t.TEMPLATE,t.MODIFIED_AT,t.NAME,t.ID,c.ACL,c.TDEF,c.VER,c.STEP,c.CREATED_BY,c.MANDATOR FROM " +
             getTable(FxTreeMode.Live) + " t, " + DatabaseConst.TBL_CONTENT + " c WHERE t.ID=? AND c.ID=t.REF AND c.ISLIVE_VER=1) NODE";
 
     //                                                       1   2   3      4
     protected static final String TREE_EDIT_NODEINFO = "SELECT LFT,RGT,PARENT,TOTAL_CHILDCOUNT," +
             //                                                                        5
             "(SELECT LFT FROM " + getTable(FxTreeMode.Edit) + " WHERE ID=NODE.PARENT) PARENTLEFT, " +
             //                                                                        6
             "(SELECT RGT FROM " + getTable(FxTreeMode.Edit) + " WHERE ID=NODE.PARENT) PARENTRIGHT," +
             //7    8          9   10       11          12   13                                14  15   16  17   18         19
             "DEPTH,CHILDCOUNT,REF,TEMPLATE,MODIFIED_AT,NAME,tree_getPosition(FALSE,ID,PARENT),ACL,TDEF,VER,STEP,CREATED_BY,MANDATOR " +
             "FROM (SELECT t.LFT,t.RGT,t.PARENT,t.DEPTH,t.TOTAL_CHILDCOUNT,t.CHILDCOUNT,t.REF,t.TEMPLATE,t.MODIFIED_AT,t.NAME,t.ID,c.ACL,c.TDEF,c.VER,c.STEP,c.CREATED_BY,c.MANDATOR FROM " +
             getTable(FxTreeMode.Edit) + " t, " + DatabaseConst.TBL_CONTENT + " c WHERE t.ID=? AND c.ID=t.REF AND c.ISMAX_VER=1) NODE";
 
     //                                                        1    2     3       4                  5            6      7        8       9          10          11                                         12    13     14    15     16           17
     private static final String TREE_LIVE_GETNODE = "SELECT t.ID,t.REF,t.DEPTH,t.TOTAL_CHILDCOUNT,t.CHILDCOUNT,t.NAME,t.PARENT,t.DIRTY,t.TEMPLATE,t.MODIFIED_AT,tree_getPosition(TRUE,t.ID,t.PARENT) POS,c.ACL,c.TDEF,c.VER,c.STEP,c.CREATED_BY,c.MANDATOR " +
             "FROM " + getTable(FxTreeMode.Live) + " t, " + DatabaseConst.TBL_CONTENT + " c WHERE t.ID=? AND c.ID=t.REF AND c.ISLIVE_VER=1";
     //                                                        1    2     3       4                  5            6      7        8       9          10          11                                          12    13     14    15     16           17
     private static final String TREE_EDIT_GETNODE = "SELECT t.ID,t.REF,t.DEPTH,t.TOTAL_CHILDCOUNT,t.CHILDCOUNT,t.NAME,t.PARENT,t.DIRTY,t.TEMPLATE,t.MODIFIED_AT,tree_getPosition(FALSE,t.ID,t.PARENT) POS,c.ACL,c.TDEF,c.VER,c.STEP,c.CREATED_BY,c.MANDATOR " +
             "FROM " + getTable(FxTreeMode.Edit) + " t, " + DatabaseConst.TBL_CONTENT + " c WHERE t.ID=? AND c.ID=t.REF AND c.ISMAX_VER=1";
 
     //                                                            1    2     3       4                  5            6      7        8       9          10          11                                         12    13     14    15     16           17
     private static final String TREE_LIVE_GETNODE_REF = "SELECT t.ID,t.REF,t.DEPTH,t.TOTAL_CHILDCOUNT,t.CHILDCOUNT,t.NAME,t.PARENT,t.DIRTY,t.TEMPLATE,t.MODIFIED_AT,tree_getPosition(TRUE,t.ID,t.PARENT) POS,c.ACL,c.TDEF,c.VER,c.STEP,c.CREATED_BY,c.MANDATOR " +
             "FROM " + getTable(FxTreeMode.Live) + " t, " + DatabaseConst.TBL_CONTENT + " c WHERE t.REF=? AND c.ID=t.REF AND c.ISLIVE_VER=1";
     //                                                            1    2     3       4                  5            6      7        8       9          10          11                                          12    13     14    15     16           17
     private static final String TREE_EDIT_GETNODE_REF = "SELECT t.ID,t.REF,t.DEPTH,t.TOTAL_CHILDCOUNT,t.CHILDCOUNT,t.NAME,t.PARENT,t.DIRTY,t.TEMPLATE,t.MODIFIED_AT,tree_getPosition(FALSE,t.ID,t.PARENT) POS,c.ACL,c.TDEF,c.VER,c.STEP,c.CREATED_BY,c.MANDATOR " +
             "FROM " + getTable(FxTreeMode.Edit) + " t, " + DatabaseConst.TBL_CONTENT + " c WHERE t.REF=? AND c.ID=t.REF AND c.ISMAX_VER=1";
 
     //1=id
     private static final String TREE_REF_USAGE_EDIT = "SELECT c.TDEF, t.ID FROM " + DatabaseConst.TBL_CONTENT + " c," + getTable(FxTreeMode.Edit) + " t WHERE c.ID=? AND t.REF=c.ID";
     //1=id
     private static final String TREE_REF_USAGE_LIVE = "SELECT c.TDEF, t.ID FROM " + DatabaseConst.TBL_CONTENT + " c," + getTable(FxTreeMode.Live) + " t WHERE c.ID=? AND t.REF=c.ID";
 
     /**
      * Get the tree table for the requested mode
      *
      * @param mode requested mode
      * @return database table to use
      */
     public static String getTable(FxTreeMode mode) {
         if (mode == FxTreeMode.Live)
             return DatabaseConst.TBL_TREE + "_LIVE";
         else
             return DatabaseConst.TBL_TREE;
     }
 
     /**
      * Check if the template contains valid characters
      *
      * @param template the template to check
      * @throws FxTreeException on errors
      */
     protected void checkTemplateValue(String template) throws FxTreeException {
         if (!StringUtils.isEmpty(template) && template.indexOf(',') >= 0) {
             throw new FxTreeException("Templates may not contain the comma character [,]");
         }
     }
 
     /**
      * Create a new folder instance to be used as reference for nodes
      *
      * @param ce    a reference to the ContentEngine
      * @param name  name
      * @param label label
      * @return FxPK
      * @throws FxApplicationException on errors
      */
     protected FxPK createFolderInstance(ContentEngine ce, String name, FxString label) throws FxApplicationException {
         FxPK reference;
         FxType type = CacheAdmin.getEnvironment().getType(FxType.FOLDER);
         FxContent content = ce.initialize(type.getId());
         List<FxPropertyAssignment> fqns = type.getAssignmentsForProperty(EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_FQN_PROPERTY));
         if (fqns.size() > 0)
             content.setValue(fqns.get(0).getXPath(), new FxString(fqns.get(0).isMultiLang(), name));
         List<FxPropertyAssignment> captions = type.getAssignmentsForProperty(EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY));
         if (captions.size() > 0) {
             FxString captionValue;
             if (label != null) {
                 if (captions.get(0).isMultiLang() && label.isMultiLanguage()) {
                     captionValue = label;
                 } else if (!captions.get(0).isMultiLang() && label.isMultiLanguage()) {
                     captionValue = new FxString(false, label.getBestTranslation());
                 } else if (captions.get(0).isMultiLang() && !label.isMultiLanguage()) {
                     captionValue = new FxString(true, label.getBestTranslation());
                 } else
                     captionValue = new FxString(captions.get(0).isMultiLang(), name); //unreachable fallback
             } else
                 captionValue = new FxString(captions.get(0).isMultiLang(), name);
             content.setValue(captions.get(0).getXPath(), captionValue);
         }
         //make sure we have a live version
         content.setStepId(type.getWorkflow().getLiveStep().getId());
         reference = ce.save(content);
         return reference;
     }
 
     /**
      * Resolve information about a node to be created like creating a folder if no reference is provided or check
      * permissions on existing references and availability of live instance if operated on the live tree
      *
      * @param mode      tree mode
      * @param seq       reference to the sequencer
      * @param ce        reference to the content engine
      * @param nodeId    desired node id
      * @param name      name
      * @param label     label
      * @param reference reference
      * @return NodeCreateInfo
      * @throws FxApplicationException on errors
      */
     protected NodeCreateInfo getNodeCreateInfo(FxTreeMode mode, SequencerEngine seq, ContentEngine ce,
                                                long nodeId, String name, FxString label, FxPK reference) throws FxApplicationException {
         if (nodeId < 0)
             nodeId = seq.getId(mode.getSequencer());
         if (StringUtils.isEmpty(name))
             name = "" + nodeId;
         name = FxFormatUtils.escapeTreePath(name);
         //check or optionally create the reference
         if (reference == null || reference.isNew()) {
             //create a folder instance if no reference is supplied
             reference = createFolderInstance(ce, name, label);
         } else {
             //check read permission
             FxContent co = ce.load(reference);
             if (mode == FxTreeMode.Live) {
                 //if mode==Live(activation), check if a live version exists and if not try to change the step of the max version to live (create a new version?)
                 FxContentVersionInfo versionInfo = ce.getContentVersionInfo(reference);
                 if (versionInfo.hasLiveVersion()) {
                     //check edit permission
                     FxContentSecurityInfo si = ce.getContentSecurityInfo(new FxPK(reference.getId(), versionInfo.getLiveVersion()));
                     FxPermissionUtils.checkPermission(FxContext.get().getTicket(), ACL.Permission.EDIT, si, true);
                 } else {
                     //create a Live version
                     co.setStepId(CacheAdmin.getEnvironment().getType(co.getTypeId()).getWorkflow().getLiveStep().getId());
                     //save will throw an exception if we are not allowed to save a live version
                     reference = ce.createNewVersion(co);
                 }
             }
             List<FxPropertyData> fqns = co.getPropertyData(EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_FQN_PROPERTY), false);
             if (fqns.size() > 0)
                 name = "" + fqns.get(0).getValue().getBestTranslation();
         }
         return new NodeCreateInfo(nodeId, name, reference);
     }
 
     /**
      * {@inheritDoc}
      */
     public long[] createNodes(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode, long parentNodeId, String path, int position) throws FxApplicationException {
         if ("/".equals(path))
             return new long[]{FxTreeNode.ROOT_NODE};
         final List<Long> result = new ArrayList<Long>();
         final Scanner scanner = new Scanner(path);
         long currentParent = parentNodeId;
         scanner.useDelimiter("/");
         while (scanner.hasNext()) {
             String name = scanner.next();
             final FxString label = new FxString(true, name);
             name = FxFormatUtils.escapeTreePath(name);
             if (StringUtils.isEmpty(name))
                 continue;
             long nodeId = getIdByFQNPath(con, mode, currentParent, "/" + name);
             if (nodeId == -1)
                 nodeId = createNode(con, seq, ce, mode, nodeId, currentParent, name, label, position, null, null);
             result.add(nodeId);
             currentParent = nodeId;
         }
         return ArrayUtils.toPrimitive(result.toArray(new Long[result.size()]));
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateName(Connection con, FxTreeMode mode, ContentEngine ce, long nodeId, String name) throws FxApplicationException {
         FxTreeNode node = getNode(con, mode, nodeId);
         name = FxFormatUtils.escapeTreePath(name);
         if (node.getName().equals(name))
             return;
 
         FxContent co = ce.load(node.getReference());
         FxType type = CacheAdmin.getEnvironment().getType(co.getTypeId());
         List<FxPropertyAssignment> fqns = type.getAssignmentsForProperty(EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_FQN_PROPERTY));
         if (fqns.size() > 0) {
             co.setValue(fqns.get(0).getXPath(), new FxString(fqns.get(0).isMultiLang(), name));
             ce.save(co);
             //we're done here since the content engine itself will trigger the tree table update
             return;
         }
         //no fqn - update the name column
         PreparedStatement ps = null;
         try {
             //                                                                1        2          3
             ps = con.prepareStatement("UPDATE " + getTable(mode) + " SET NAME=?, DIRTY=? WHERE ID=?");
             ps.setString(1, name);
             ps.setBoolean(2, mode != FxTreeMode.Live);
             ps.setLong(3, nodeId);
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.tree.update.failed", mode.name(), e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void syncFQNName(Connection con, long referenceId, boolean maxVersion, boolean liveVersion, String name) throws FxApplicationException {
         if (StringUtils.isEmpty(name))
             return;
         if (liveVersion)
             updateName(con, FxTreeMode.Live, referenceId, name);
         if (maxVersion || liveVersion)
             updateName(con, FxTreeMode.Edit, referenceId, name);
     }
 
     /**
      * Update all nodes that reference the given id and set the name
      *
      * @param con         an open and valid connection
      * @param mode        tree mode
      * @param referenceId id of the referenced content
      * @param name        the new name
      * @throws FxApplicationException on errors
      */
     private void updateName(Connection con, FxTreeMode mode, long referenceId, String name) throws FxApplicationException {
         PreparedStatement ps = null;
         try {
             //                                                                1        2           3
             ps = con.prepareStatement("UPDATE " + getTable(mode) + " SET NAME=?, DIRTY=? WHERE REF=?");
             ps.setString(1, FxFormatUtils.escapeTreePath(name));
             ps.setBoolean(2, mode != FxTreeMode.Live); //live tree is never dirty
             ps.setLong(3, referenceId);
             ps.executeUpdate();
             FxContext.get().setTreeWasModified();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.tree.update.failed", mode.name(), e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
     }
 
     /**
      * Clear a tree nodes dirty flag
      *
      * @param con    an open and valid connection
      * @param mode   tree mode
      * @param nodeId node id
      * @throws FxUpdateException on errors
      */
     protected void clearDirtyFlag(Connection con, FxTreeMode mode, long nodeId) throws FxUpdateException {
         PreparedStatement ps = null;
         try {
             //                                                                 1                                 2
             ps = con.prepareStatement("UPDATE " + getTable(mode) + " SET DIRTY=?,MODIFIED_AT=SYSDATE() WHERE ID=?");
             ps.setBoolean(1, false); //live tree is never dirty
             ps.setLong(2, nodeId);
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.tree.update.failed", mode.name(), e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
     }
 
     /**
      * Flag a tree node dirty
      *
      * @param con    an open and valid connection
      * @param mode   tree mode
      * @param nodeId node id
      * @throws FxUpdateException on errors
      */
     protected void flagDirty(Connection con, FxTreeMode mode, long nodeId) throws FxUpdateException {
         PreparedStatement ps = null;
         try {
             //                                                                 1                                 2
             ps = con.prepareStatement("UPDATE " + getTable(mode) + " SET DIRTY=?,MODIFIED_AT=SYSDATE() WHERE ID=?");
             ps.setBoolean(1, mode != FxTreeMode.Live); //live tree is never dirty
             ps.setLong(2, nodeId);
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.tree.update.failed", mode.name(), e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public long getIdByFQNPath(Connection con, FxTreeMode mode, long startNode, String path) throws FxApplicationException {
         path = path.replaceAll("/+", "/");
        if ("/".equals(path))
            return FxTreeNode.ROOT_NODE;
         // Using a statement instead of a prepare statement because it is much faster,
         // so make sure the path will not crash
         if (path.contains("'") || path.contains("\n"))
             return -1;
 
         Statement ps = null;
         try {
             ps = con.createStatement();
             ResultSet rs = ps.executeQuery("SELECT tree_pathToID(" + startNode + ",'" + path + "'," +
                     (mode == FxTreeMode.Live) + ")");
             long result = -1;
             if (rs.next()) {
                 result = rs.getLong(1);
                 if (rs.wasNull())
                     result = -1;
             }
             return result;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Exception e) {
                 //ignore
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public long getIdByLabelPath(Connection con, FxTreeMode mode, long startNode, String path) throws FxApplicationException {
         path = path.replaceAll("/+", "/");
        if ("/".equals(path))
            return FxTreeNode.ROOT_NODE;
         // Using a statement instead of a prepare statement because it is much faster,
         // so make sure the path will not crash
         if (path.contains("'") || path.contains("\n"))
             return -1;
 
         Statement ps = null;
         try {
             ps = con.createStatement();
             ResultSet rs;
             rs = ps.executeQuery("SELECT tree_captionPathToID(" + startNode + ",'" + path + "'," +
                     EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY) + ",1," +
                     (mode == FxTreeMode.Live) + ")");
             long result = -1;
             if (rs.next()) {
                 result = rs.getLong(1);
                 if (rs.wasNull())
                     result = -1;
             }
             return result;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Exception e) {
                 //ignore
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<String> getLabels(Connection con, FxTreeMode mode, long labelPropertyId, FxLanguage language,
                                   boolean stripNodeInfos, long... nodeIds) throws FxApplicationException {
         List<String> ret = new ArrayList<String>(nodeIds.length);
         if (nodeIds.length == 0)
             return ret;
 
         PreparedStatement ps = null;
         ResultSet rs;
         try {
             ps = con.prepareStatement("SELECT tree_FTEXT1024_Chain(?,?,?,?)");
             ps.setInt(2, (int)language.getId());
             ps.setLong(3, labelPropertyId);
             ps.setBoolean(4, mode == FxTreeMode.Live);
             for (long id : nodeIds) {
                 ps.setLong(1, id);
                 rs = ps.executeQuery();
                 if (rs != null && rs.next())
                     ret.add(stripNodeInfos ? stripNodeInfos(rs.getString(1)) : rs.getString(1));
                 else
                     ret.add("/<unknown:" + id + ">");
             }
             return ret;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Exception e) {
                 //ignore
             }
         }
     }
 
     /**
      * Strip any node infos from the path.
      * Node infos have the format path:nodeId:ref:tdef - strip all but path which can not contain ':' as it is replaced
      * with a space in the stored procedure.
      *
      * @param path path to strip
      * @return stripped path
      */
     private String stripNodeInfos(String path) {
         if (StringUtils.isEmpty(path))
             return "";
         StringBuilder sbRet = new StringBuilder(path.length());
         for (String p : path.split("\\/"))
             if (p.indexOf(':') > 0)
                 sbRet.append('/').append(p.substring(0, p.indexOf(':')));
         return sbRet.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     public String getPathById(Connection con, FxTreeMode mode, long id) throws FxTreeException {
         Statement ps = null;
         try {
             ps = con.createStatement();
             ResultSet rs = ps.executeQuery("SELECT tree_idToPath(" + id + "," + (mode == FxTreeMode.Live) + ")");
             String result = null;
             if (rs.next()) {
                 result = rs.getString(1);
                 if (rs.wasNull()) result = "";
             }
             return result != null ? FxFormatUtils.escapeTreePath(result) : "";
         } catch (SQLException e) {
             throw new FxTreeException(LOG, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Exception exc) {
                 //ignore
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public long[] getIdChain(Connection con, FxTreeMode mode, long id) throws FxTreeException {
         Statement ps = null;
         try {
             ps = con.createStatement();
             ResultSet rs = ps.executeQuery("SELECT tree_idchain(" + id + "," + (mode == FxTreeMode.Live) + ")");
             String result = "";
             if (rs.next()) {
                 result = rs.getString(1);
                 if (rs.wasNull()) result = "";
             }
             if (result.length() == 0) {
                 return null;
             }
             String ids[] = result.split("/");
             long[] lres = new long[ids.length - 1];
             int pos = 0;
             for (String sid : ids) {
                 if (pos != 0)
                     lres[pos - 1] = Long.parseLong(sid);
                 pos++;
             }
             return lres;
         } catch (SQLException e) {
             throw new FxTreeException(LOG, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Exception exc) {
                 //ignore
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean exists(Connection con, FxTreeMode mode, long id) throws FxApplicationException {
         try {
             getTreeNodeInfo(con, mode, id);
         } catch (FxNotFoundException e) {
             return false;
         }
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxTreeNode getNode(Connection con, FxTreeMode mode, long nodeId) throws FxApplicationException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(mode == FxTreeMode.Live ? TREE_LIVE_GETNODE : TREE_EDIT_GETNODE);
             ps.setLong(1, nodeId);
             ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 long _id = rs.getLong(1);
                 FxPK _ref = new FxPK(rs.getLong(2), rs.getInt(14));
                 int _depth = rs.getInt(3);
                 int _totalChilds = rs.getInt(4);
                 int _directChilds = rs.getInt(5);
                 String _name = rs.getString(6);
                 long _parent = rs.getLong(7);
                 boolean _dirty = rs.getBoolean(8);
                 String _template = rs.getString(9);
                 if (rs.wasNull())
                     _template = null;
                 long _modified = rs.getTimestamp(10).getTime();
                 int _pos = rs.getInt(11);
                 long _acl = rs.getLong(12);
                 FxType _type = CacheAdmin.getEnvironment().getType(rs.getLong(13));
                 long _stepACL = CacheAdmin.getEnvironment().getStep(rs.getLong(15)).getAclId();
                 long _createdBy = rs.getLong(16);
                 long _mandator = rs.getLong(17);
                 UserTicket ticket = FxContext.get().getTicket();
                 boolean _edit;
                 boolean _create;
                 boolean _delete;
                 boolean _export;
                 boolean _relate;
                 final boolean _system = FxContext.get().getRunAsSystem() || ticket.isGlobalSupervisor();
                 FxPermissionUtils.checkPermission(ticket, ACL.Permission.READ, _type, _stepACL, _acl, true);
                 if (_system || ticket.isMandatorSupervisor() && _mandator == ticket.getMandatorId() ||
                         !_type.usePermissions() || ticket.isInGroup((int) UserGroup.GROUP_OWNER) && _createdBy == ticket.getUserId()) {
                     _edit = _create = _delete = _export = _relate = true;
                 } else {
                     //throw exception if read is forbidden
                     FxPermissionUtils.checkPermission(ticket, ACL.Permission.READ, _type, _stepACL, _acl, true);
                     _edit = FxPermissionUtils.checkPermission(ticket, ACL.Permission.EDIT, _type, _stepACL, _acl, false);
                     _relate = FxPermissionUtils.checkPermission(ticket, ACL.Permission.RELATE, _type, _stepACL, _acl, false);
                     _delete = FxPermissionUtils.checkPermission(ticket, ACL.Permission.DELETE, _type, _stepACL, _acl, false);
                     _export = FxPermissionUtils.checkPermission(ticket, ACL.Permission.EXPORT, _type, _stepACL, _acl, false);
                     _create = FxPermissionUtils.checkPermission(ticket, ACL.Permission.CREATE, _type, _stepACL, _acl, false);
                 }
                 FxString label = Database.loadContentDataFxString(con, "FTEXT1024", "ID=" + _ref.getId() + " AND " +
                         (mode == FxTreeMode.Live ? "ISMAX_VER=1" : "ISLIVE_VER=1") + " AND TPROP=" + EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY));
                 return new FxTreeNode(mode, _id, _parent, _ref, _acl, _name, getPathById(con, mode, _id), label,
                         _pos, new ArrayList<FxTreeNode>(0), new ArrayList<Long>(0), _depth, _totalChilds, _directChilds,
                         _directChilds == 0, _dirty, _modified, _template, _edit, _create, _delete, _relate, _export);
             } else
                 throw new FxLoadException(LOG, "ex.tree.node.notFound", nodeId, mode);
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "ex.tree.load.failed.node", nodeId, mode, exc.getMessage());
         } finally {
             Database.closeObjects(GenericTreeStorage.class, null, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxTreeNode> getNodesWithReference(Connection con, FxTreeMode mode, long referenceId)
             throws FxApplicationException {
         PreparedStatement ps = null;
         List<FxTreeNode> ret = new ArrayList<FxTreeNode>(20);
         long _id = -1;
         try {
             ps = con.prepareStatement(mode == FxTreeMode.Live ? TREE_LIVE_GETNODE_REF : TREE_EDIT_GETNODE_REF);
             ps.setLong(1, referenceId);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 _id = rs.getLong(1);
                 FxPK _ref = new FxPK(rs.getLong(2), rs.getInt(14));
                 int _depth = rs.getInt(3);
                 int _totalChilds = rs.getInt(4);
                 int _directChilds = rs.getInt(5);
                 String _name = rs.getString(6);
                 long _parent = rs.getLong(7);
                 boolean _dirty = rs.getBoolean(8);
                 String _template = rs.getString(9);
                 if (rs.wasNull())
                     _template = null;
                 long _modified = rs.getTimestamp(10).getTime();
                 int _pos = rs.getInt(11);
                 long _acl = rs.getLong(12);
                 FxType _type = CacheAdmin.getEnvironment().getType(rs.getLong(13));
                 long _stepACL = CacheAdmin.getEnvironment().getStep(rs.getLong(15)).getAclId();
                 long _createdBy = rs.getLong(16);
                 long _mandator = rs.getLong(17);
                 UserTicket ticket = FxContext.get().getTicket();
                 boolean _read;
                 boolean _edit;
                 boolean _create;
                 boolean _delete;
                 boolean _export;
                 boolean _relate;
                 final boolean _system = FxContext.get().getRunAsSystem() || ticket.isGlobalSupervisor();
                 FxPermissionUtils.checkPermission(ticket, ACL.Permission.READ, _type, _stepACL, _acl, true);
                 if (_system || ticket.isMandatorSupervisor() && _mandator == ticket.getMandatorId() ||
                         !_type.usePermissions() || ticket.isInGroup((int) UserGroup.GROUP_OWNER) && _createdBy == ticket.getUserId()) {
                     _read = _edit = _create = _delete = _export = _relate = true;
                 } else {
                     //throw exception if read is forbidden
                     _read = FxPermissionUtils.checkPermission(ticket, ACL.Permission.READ, _type, _stepACL, _acl, false);
                     _edit = FxPermissionUtils.checkPermission(ticket, ACL.Permission.EDIT, _type, _stepACL, _acl, false);
                     _relate = FxPermissionUtils.checkPermission(ticket, ACL.Permission.RELATE, _type, _stepACL, _acl, false);
                     _delete = FxPermissionUtils.checkPermission(ticket, ACL.Permission.DELETE, _type, _stepACL, _acl, false);
                     _export = FxPermissionUtils.checkPermission(ticket, ACL.Permission.EXPORT, _type, _stepACL, _acl, false);
                     _create = FxPermissionUtils.checkPermission(ticket, ACL.Permission.CREATE, _type, _stepACL, _acl, false);
                 }
                 if (!_read)
                     continue;
                 FxString label = Database.loadContentDataFxString(con, "FTEXT1024", "ID=" + _ref.getId() + " AND " +
                         (mode == FxTreeMode.Live ? "ISMAX_VER=1" : "ISLIVE_VER=1") + " AND TPROP=" + EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY));
 
                 FxTreeNode node = new FxTreeNode(mode, _id, _parent, _ref, rs.getLong(12), _name, getPathById(con, mode, _id), label,
                         _pos, new ArrayList<FxTreeNode>(0), new ArrayList<Long>(0), _depth, _totalChilds, _directChilds,
                         _directChilds == 0, _dirty, _modified, _template, _edit, _create, _delete, _relate, _export);
                 ret.add(node);
             }
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "ex.tree.load.failed.node", _id, mode, exc.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Exception exc) {/*ignore*/}
         }
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxTreeNode getTree(Connection con, ContentEngine ce, FxTreeMode mode, long nodeId, int depth,
                               boolean loadPartial, FxLanguage partialLoadLanguage) throws FxApplicationException {
         Statement ps = null;
 //        long time = System.currentTimeMillis();
 //        long nodes = 0;
         try {
             long captionProperty = EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY);
             String version = mode == FxTreeMode.Live ? "ISLIVE_VER=1" : "ISMAX_VER=1";
             String label_pos = loadPartial
                     ? "IFNULL(IFNULL(" +
                     "(SELECT f.FTEXT1024 FROM " + DatabaseConst.TBL_CONTENT_DATA + " f WHERE f.TPROP=" +
                     captionProperty + " AND LANG IN(" + partialLoadLanguage.getId() + ",0)AND f." + version + " AND f.ID=n.REF LIMIT 1)," +
                     "(SELECT f.FTEXT1024 from " + DatabaseConst.TBL_CONTENT_DATA + " f where f.tprop=" +
                     captionProperty + " AND ISMLDEF=TRUE AND f." + version + " AND f.ID=n.REF LIMIT 1)" +
                     "),n.NAME)"
                     : "tree_getPosition(TRUE,n.ID,n.PARENT)";
             //                     1    2     3       4                  5            6      7        8       9          10              11              12    13     14    15     16           17
             String sql = "SELECT n.ID,n.REF,n.DEPTH,n.TOTAL_CHILDCOUNT,n.CHILDCOUNT,n.NAME,n.PARENT,n.DIRTY,n.TEMPLATE,n.MODIFIED_AT," + label_pos + ",c.ACL,c.TDEF,c.VER,c.STEP,c.CREATED_BY,c.MANDATOR " +
                     "FROM " + getTable(mode) + " r, " + getTable(mode) + " n, " + DatabaseConst.TBL_CONTENT + " c WHERE r.ID=" + nodeId + " AND n.LFT>=r.LFT AND n.LFT<=r.RGT AND n.DEPTH<=(r.DEPTH+" + depth + ") AND c.ID=n.REF AND c." + version + " ORDER BY n.LFT";
 
             ps = con.createStatement();
             ResultSet rs = ps.executeQuery(sql);
             Map<Long, FxTreeNode> data = new HashMap<Long, FxTreeNode>(100, 2.0f);
             //prepare infos needed to compute permissions
             UserTicket ticket = FxContext.get().getTicket();
             final boolean _system = FxContext.get().getRunAsSystem() || ticket.isGlobalSupervisor();
             //StepId->step ACL Id lookup
             Map<Long, Long> step2stepACLId = new HashMap<Long, Long>(10);
             //typeId->FxType lookup
             Map<Long, FxType> typeId2Type = new HashMap<Long, FxType>(10);
             while (rs.next()) {
 //                nodes++;
                 long _id = rs.getLong(1);
                 FxPK _ref = new FxPK(rs.getLong(2), rs.getInt(14));
                 int _depth = rs.getInt(3);
                 int _totalChilds = rs.getInt(4);
                 int _directChilds = rs.getInt(5);
                 String _name = rs.getString(6);
                 long _parent = rs.getLong(7);
                 boolean _dirty = rs.getBoolean(8);
                 String _template = rs.getString(9);
                 if (rs.wasNull())
                     _template = null;
                 long _modified = rs.getTimestamp(10).getTime();
                 int _pos;
                 FxString label;
                 if (loadPartial) {
                     _pos = FxTreeNode.PARTIAL_LOADED_POS;
                     label = new FxString(true, partialLoadLanguage.getId(), rs.getString(11));
                 } else {
                     _pos = rs.getInt(11);
                     label = Database.loadContentDataFxString(con, "FTEXT1024", "ID=" + _ref.getId() + " AND " +
                             version + " AND TPROP=" + EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY));
                 }
                 if (label.isEmpty())
                     label = new FxString(true, _name); //TODO: check if label should be multilanguage!
                 long _acl = rs.getLong(12);
                 long _tdef = rs.getLong(13);
                 long _step = rs.getLong(15);
                 long _createdBy = rs.getLong(16);
                 long _mandator = rs.getLong(17);
 
                 boolean _read;
                 boolean _edit;
                 boolean _create;
                 boolean _delete;
                 boolean _export;
                 boolean _relate;
 
                 FxType type = typeId2Type.get(_tdef);
                 if (type == null) {
                     type = CacheAdmin.getEnvironment().getType(_tdef);
                     typeId2Type.put(_tdef, type);
                 }
                 Long _stepACL = step2stepACLId.get(_step);
                 if (_stepACL == null) {
                     _stepACL = CacheAdmin.getEnvironment().getStep(_step).getAclId();
                     step2stepACLId.put(_step, _stepACL);
                 }
                 if (_system || ticket.isMandatorSupervisor() && _mandator == ticket.getMandatorId() ||
                         !type.usePermissions() || ticket.isInGroup((int) UserGroup.GROUP_OWNER) && _createdBy == ticket.getUserId()) {
                     _read = _edit = _create = _delete = _export = _relate = true;
                 } else {
                     _read = FxPermissionUtils.checkPermission(ticket, ACL.Permission.READ, type, _stepACL, _acl, false);
                     _edit = FxPermissionUtils.checkPermission(ticket, ACL.Permission.EDIT, type, _stepACL, _acl, false);
                     _relate = FxPermissionUtils.checkPermission(ticket, ACL.Permission.RELATE, type, _stepACL, _acl, false);
                     _delete = FxPermissionUtils.checkPermission(ticket, ACL.Permission.DELETE, type, _stepACL, _acl, false);
                     _export = FxPermissionUtils.checkPermission(ticket, ACL.Permission.EXPORT, type, _stepACL, _acl, false);
                     _create = FxPermissionUtils.checkPermission(ticket, ACL.Permission.CREATE, type, _stepACL, _acl, false);
                 }
                 if (_read) {
                     FxTreeNode node = new FxTreeNode(mode, _id, _parent, _ref, _acl, _name, FxTreeNode.PATH_NOT_LOADED, label,
                             _pos, new ArrayList<FxTreeNode>(10), new ArrayList<Long>(10), _depth, _totalChilds, _directChilds,
                             _directChilds == 0, _dirty, _modified, _template, _edit, _create, _delete, _relate, _export);
                     FxTreeNode parent = data.get(node.getParentNodeId());
                     data.put(_id, node);
                     if (parent != null)
                         parent._addChild(node);
                 }
             }
             final FxTreeNode root = data.get(nodeId);
             if (root == null)
                 throw new FxLoadException(LOG, "ex.tree.node.notFound", nodeId, mode);
             FxTreeNode _root = getNode(con, mode, root.getId());
             root._applyPath(getPathById(con, mode, root.getId()));
             root._applyPosition(_root.getPosition());
             return root;
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "ex.tree.load.failed.tree", nodeId, mode, exc.getMessage(), depth);
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Exception exc) {
                 //ignore
             }
 //            System.out.println(nodes + " nodes loaded in " + (System.currentTimeMillis() - time) + "[ms]. Partial:" + loadPartial);
         }
     }
 
     /*public BigDecimal makeSpace(Connection con, SequencerEngine seq, FxTreeMode mode, long nodeId, int position, final int additional) throws FxApplicationException {
 
 FxTreeNodeInfo nodeInfo = getTreeNodeInfo(con, mode, nodeId);
 //        BigDecimal boundaries[] = getBoundaries(con, nodeInfo, position);
 
 
 //check if there is enough space within the boundaries: ((right-left)-1)-(spacing*3)-2 > 0
 // => right-left-spacing*3 > 3
 
 
 int totalChildCount = nodeInfo.getTotalChildCount() + additional;
 boolean hasSpace = nodeInfo.hasSpaceFor(totalChildCount);
 
 Stack<FxTreeNodeInfo> children = new Stack<FxTreeNodeInfo>();
 children.push(nodeInfo);
 // Determine node to work on
 while (!hasSpace) {
   nodeInfo = getTreeNodeInfo(con, mode, nodeInfo.getParentId());
   totalChildCount += nodeInfo.getDirectChildCount()*//* + 1*//*;
             hasSpace = nodeInfo.hasSpaceFor(totalChildCount);
             if (!hasSpace && nodeInfo.isRoot())
                 throw new FxUpdateException("ex.tree.makeSpace.failed");
 //            if (!hasSpace)
                 children.push(nodeInfo);
         }
 
         // re-allocate all entries in the given nodeInfo with the calculated spacing
         BigDecimal spacing = nodeInfo.getSpacing(totalChildCount);
         BigDecimal left = nodeInfo.getLeft().add(spacing);
         BigDecimal right;
 
         PreparedStatement psSelect = null;
         PreparedStatement psUpdate = null;
         try {
             psSelect = con.prepareStatement("SELECT ID,TOTAL_CHILDCOUNT FROM "+getTable(mode)+" WHERE PARENT=? AND DEPTH=? ORDER BY LFT");
             psUpdate = con.prepareStatement("UPDATE "+getTable(mode)+" SET LFT=?,RGT=? WHERE ID=?");
             ResultSet rs;
             long childCount;
             long id;
             int batches = 0;
             FxTreeNodeInfo current;
             while (!children.isEmpty()) {
                 current = children.pop();
                 psSelect.setLong(1, current.getId());
                 psSelect.setInt(2, current.getDepth()+1);
                 rs = psSelect.executeQuery();
                 while (rs != null && rs.next()) {
                     id = rs.getLong(1);
                     childCount = rs.getLong(2);
                     psUpdate.setBigDecimal(1, left.add(BigDecimal.ONE).add(spacing));
                     // right = left + (3*spacing + 2)*(childcount+additional) + 1
                     // 3... left, middle, right spacing
                     // 2... left and right slot
                     right = left.add(TWO.add(TWO.add(THREE.multiply(spacing)).multiply(new BigDecimal(childCount+additional))));
                     //two is added above instead of one because left was not updated with +1
                     left = right.add(BigDecimal.ONE).add(spacing);
                     psUpdate.setBigDecimal(2, right);
                     psUpdate.setLong(3, id);
                     psUpdate.addBatch();
                     batches++;
                     if( batches % 1000 == 0) {
                         psUpdate.executeBatch();
                         batches = 0;
                     }
                 }
                 if( batches > 0)
                     psUpdate.executeBatch();
             }
         } catch (SQLException e) {
             e.printStackTrace();
             throw new FxTreeException(e, "ex.tree.makeSpace.failed");
         } finally {
             try {
                 if( psSelect != null )
                     psSelect.close();
             } catch (SQLException e) {
                 //ignore
             }
             try {
                 if( psUpdate != null )
                     psUpdate.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
 
 *//*
 
         int spaceCount = (additional * 2) + 1;
         BigDecimal insertSpace = spacing.multiply(new BigDecimal(spaceCount));
         insertSpace = insertSpace.add(new BigDecimal(additional * 2));
 
         reorganizeSpace(con, seq, mode, mode, nodeInfo.getId(), false, spacing, null, nodeInfo, position,
                 insertSpace, boundaries, 0, null, false, false, null);
 *//*
         return spacing;
     }*/
 
     /**
      * {@inheritDoc}
      */
     public void clearTree(Connection con, ContentEngine ce, FxTreeMode mode) throws FxApplicationException {
         Statement stmt = null;
         try {
             FxPK rootPK;
 
             FxTreeMode rootCheckMode = mode == FxTreeMode.Live ? FxTreeMode.Edit : FxTreeMode.Live;
             try {
                 rootPK = getTreeNodeInfo(con, rootCheckMode, FxTreeNode.ROOT_NODE).getReference();
             } catch (FxApplicationException e) {
                 LOG.info("No root node found. Creating a new one.");
 
                 //create the root folder instance
                 FxType type = CacheAdmin.getEnvironment().getType(FxType.FOLDER);
                 FxContent content = ce.initialize(type.getId());
                 List<FxPropertyAssignment> captions =
                         type.getAssignmentsForProperty(EJBLookup.getConfigurationEngine().
                                 get(SystemParameters.TREE_CAPTION_PROPERTY));
                 if (captions.size() > 0) {
                     FxPropertyAssignment paCaption = captions.get(0);
                     FxString label = new FxString(paCaption.isMultiLang(), "Root");
                     content.setValue(paCaption.getXPath(), label);
                 }
                 rootPK = ce.save(content);
             }
 
             stmt = con.createStatement();
             List<Long> folderIds = new ArrayList<Long>(50);
 
             //fetch all referenced folders
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT c.ID FROM " + DatabaseConst.TBL_CONTENT + " c WHERE c.TDEF=" + CacheAdmin.getEnvironment().getType(FxType.FOLDER).getId() + " AND c.ID IN (SELECT DISTINCT t.REF FROM " + getTable(mode) + " t)");
             while (rs != null && rs.next()) {
                 if (rootPK.getId() != rs.getLong(1))
                     folderIds.add(rs.getLong(1));
             }
 
             wipeTree(mode, stmt, rootPK);
 
             //reset sequencer
             stmt.executeUpdate("UPDATE " + DatabaseConst.TBL_SEQUENCE + " SET ID=1 WHERE NAME='" + mode.getSequencer().getSequencerName() + "'");
             if (mode == FxTreeMode.Live) {
                 //flag all edit nodes back to dirty
                 stmt.executeUpdate("UPDATE " + getTable(FxTreeMode.Edit) + " SET DIRTY=TRUE WHERE ID<>" + ROOT_NODE);
             }
 //            stmt.executeBatch();
             if (folderIds.size() > 0) {
                 //remove all referenced folders (or try to at least)
                 int removed = 0;
                 for (long folderId : folderIds) {
                     if (folderId == rootPK.getId())
                         continue; //no need to test reference count for the root node as it IS used
                     FxPK folderPK = new FxPK(folderId);
                     if (ce.getReferencedContentCount(folderPK) == 0) {
                         ce.remove(folderPK);
                         removed++;
                     }
                 }
                 LOG.info("Removed " + removed + "/" + folderIds.size() + " Folder instances");
             }
         } catch (Exception e) {
             throw new FxTreeException(LOG, e, "ex.tree.clear.failed", mode.name(), e.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Throwable t) {
                 /*ignore*/
             }
         }
     }
 
     /**
      * Remove all tree nodes and create a new root node
      *
      * @param mode   tree mode
      * @param stmt   statement
      * @param rootPK primary key of the root content instance
      * @throws SQLException on errors
      */
     protected abstract void wipeTree(FxTreeMode mode, Statement stmt, FxPK rootPK) throws SQLException;
 
     /**
      * {@inheritDoc}
      */
     public void removeNode(Connection con, FxTreeMode mode, ContentEngine ce, long nodeId, boolean removeChildren) throws FxApplicationException {
         if (mode == FxTreeMode.Live)
             removeChildren = true; //always delete child nodes in live mode
         Statement stmt = null;
         if (nodeId == FxTreeNode.ROOT_NODE)
             throw new FxNoAccessException("ex.tree.delete.root");
         FxTreeNodeInfo nodeInfo = getTreeNodeInfo(con, mode, nodeId);
         try {
             stmt = con.createStatement();
 
             final String INSIDE_NODE = " LFT>=" + nodeInfo.getLeft() + " AND RGT<=" + nodeInfo.getRight() + " ";
             long childNodeDelta;
             stmt.addBatch("SET FOREIGN_KEY_CHECKS=0");
             List<FxPK> references = new ArrayList<FxPK>(50);
             UserTicket ticket = FxContext.get().getTicket();
 
             if (removeChildren) {
                 childNodeDelta = nodeInfo.getTotalChildCount() + 1;
                 //FX-102: edit permission checks on references
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT REF FROM " + getTable(mode) + " WHERE " + INSIDE_NODE);
                 while (rs != null && rs.next()) {
                     try {
                         if (ce != null)
                             FxPermissionUtils.checkPermission(ticket, ACL.Permission.EDIT, ce.getContentSecurityInfo(new FxPK(rs.getLong(1))), true);
                         references.add(new FxPK(rs.getLong(1)));
                     } catch (FxLoadException e) {
                         //ignore, might have been removed meanwhile
                     }
                 }
                 stmt.addBatch("DELETE FROM " + getTable(mode) + " WHERE " + INSIDE_NODE);
             } else {
                 childNodeDelta = 1;
                 //FX-102: edit permission checks on references
                 try {
                     if (ce != null)
                         FxPermissionUtils.checkPermission(FxContext.get().getTicket(), ACL.Permission.EDIT, ce.getContentSecurityInfo(nodeInfo.getReference()), true);
                     references.add(nodeInfo.getReference());
                 } catch (FxLoadException e) {
                     //ignore, might have been removed meanwhile
                 }
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET PARENT=" + nodeInfo.getParentId() + " WHERE PARENT=" + nodeId);
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET DEPTH=DEPTH-1,DIRTY=" + (mode != FxTreeMode.Live) + " WHERE " + INSIDE_NODE);
                 stmt.addBatch("DELETE FROM " + getTable(mode) + " WHERE ID=" + nodeId);
             }
 
             // Update the childcount of the parents
             stmt.addBatch("UPDATE " + getTable(mode) + " SET TOTAL_CHILDCOUNT=TOTAL_CHILDCOUNT-" + childNodeDelta +
                     " WHERE (LFT<" + nodeInfo.getLeft() + " AND RGT>" + nodeInfo.getRight() + ")");
             if (removeChildren) {
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT-1 WHERE ID=" + nodeInfo.getParentId());
             } else {
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT+" +
                         (nodeInfo.getDirectChildCount() - 1) + " WHERE ID=" + nodeInfo.getParentId());
             }
 
             // Set the dirty flag for the parent if needed
             if (mode != FxTreeMode.Live) {
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=TRUE WHERE ID=" + nodeInfo.getParentId());
             }
 
             stmt.addBatch("SET FOREIGN_KEY_CHECKS=1");
             if (mode == FxTreeMode.Live && exists(con, FxTreeMode.Edit, nodeId)) {
                 //check if a node with the same id that has been removed in the live tree exists in the edit tree,
                 //the node and all its children will be flagged as dirty in the edit tree
                 FxTreeNodeInfo editNode = getTreeNodeInfo(con, FxTreeMode.Edit, nodeId);
                 stmt.addBatch("UPDATE " + getTable(FxTreeMode.Edit) + " SET DIRTY=TRUE WHERE LFT>=" +
                         editNode.getLeft() + " AND RGT<=" + editNode.getRight());
 //                stmt.addBatch("UPDATE " + getTable(FxTreeMode.Edit) + " SET DIRTY=TRUE WHERE ID=" + editNode.getId());
             }
             stmt.executeBatch();
             if (ce == null)
                 return;
             //if the referenced content is a folder, remove it
             long folderTypeId = CacheAdmin.getEnvironment().getType(FxType.FOLDER).getId();
             for (FxPK ref : references) {
                 FxContent coRef = ce.load(ref);
                 int contentCount = ce.getReferencedContentCount(coRef.getPk());
 //                System.out.println("ContentCount for " + coRef.getPk() + ": " + contentCount);
                 if (coRef.getTypeId() == folderTypeId && contentCount == 0) {
 //                    System.out.println("Removing "+ref);
                     ce.remove(ref);
                 }
             }
             afterNodeRemoved(con, nodeInfo, removeChildren);
         } catch (SQLException exc) {
             throw new FxRemoveException(LOG, "ex.tree.delete.failed", nodeId, exc.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {
                 //ignore
             }
         }
     }
 
     /**
      * Perform needed cleanups like closing gaps after a node has been removed
      *
      * @param con            an open and valid connection
      * @param nodeInfo       info about the removed node
      * @param removeChildren children removed or moved up a level?
      * @throws FxApplicationException on errors
      */
     protected abstract void afterNodeRemoved(Connection con, FxTreeNodeInfo nodeInfo, boolean removeChildren) throws FxApplicationException;
 
     /**
      * {@inheritDoc}
      */
     public void setTemplate(Connection con, FxTreeMode mode, long nodeId, String template) throws FxApplicationException {
         checkTemplateValue(template);
 
         FxTreeNodeInfo node = getTreeNodeInfo(con, mode, nodeId);
         // Any changes at all?
         if (node.hasTemplate(template)) return;
         // Set the template
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("UPDATE " + getTable(mode) + " SET TEMPLATE=?,DIRTY=TRUE,MODIFIED_AT=SYSDATE() WHERE ID=" + nodeId);
             if (template == null)
                 ps.setNull(1, java.sql.Types.VARCHAR);
             else
                 ps.setString(1, template);
             ps.executeUpdate();
         } catch (Throwable t) {
             throw new FxUpdateException(LOG, "ex.tree.setTemplate.failed", template, nodeId, t.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Throwable t) {
                 //ignore
             }
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void activateAll(Connection con, FxTreeMode mode) throws FxTreeException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.addBatch("SET FOREIGN_KEY_CHECKS=0");
             stmt.addBatch("DELETE FROM " + getTable(FxTreeMode.Live));
             stmt.addBatch("INSERT INTO " + getTable(FxTreeMode.Live) + " (SELECT * FROM " + getTable(mode) + ")");
             stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=FALSE");
             stmt.addBatch("UPDATE " + getTable(FxTreeMode.Live) + " SET DIRTY=FALSE");
             stmt.addBatch("SET FOREIGN_KEY_CHECKS=1");
             stmt.executeBatch();
         } catch (Throwable t) {
             throw new FxTreeException(LOG, "ex.tree.activate.all.failed", mode.name(), t.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {/*ignore*/}
         }
     }
 
     /**
      * Gets the next CopyOf___(Id)
      *
      * @param con          an open and valid connection
      * @param mode         tree mode
      * @param copyOfPrefix the prefix to search for
      * @param parentNodeId parent node id
      * @param nodeId       node id
      * @return the CopyOf number
      * @throws FxTreeException on errors
      */
     protected int getCopyOfCount(Connection con, FxTreeMode mode, String copyOfPrefix, long parentNodeId, long nodeId) throws FxTreeException {
         Statement stmt = null;
         try {
             // Update the childcount of the new parents
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + getTable(mode) + " WHERE PARENT=" + parentNodeId + " AND NAME LIKE " +
                     "(SELECT CONCAT(CONCAT('" + copyOfPrefix + "%',NAME),'(%)') FROM " + getTable(mode) + " WHERE ID=" + nodeId + ")");
             int result = 0;
             if (rs.next()) {
                 result = rs.getInt(1);
             }
             return result;
         } catch (SQLException e) {
             throw new FxTreeException(LOG, "ex.tree.copyOf.failed", nodeId, parentNodeId, mode.name(), copyOfPrefix, e.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {
                 //ignore
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void populate(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode) throws FxApplicationException {
         try {
             int maxLevel = 3;
             for (int i = 0; i < maxLevel; i++) {
                 LOG.info("Creating level [" + (i + 1) + "/" + maxLevel + "]");
                 String name = "Level2_[" + i + "]";
                 FxString desc = new FxString(true, "Level2 Node " + i);
                 long newIdLevel1 = createNode(con, seq, ce, mode, -1, ROOT_NODE, name, desc, 0, null, null);
                 for (int y = 0; y < 10; y++) {
                     String name2 = "Level3_[" + i + "|" + y + "]";
                     FxString desc2 = new FxString(true, "Level3 Node " + y);
                     long newIdLevel2 = createNode(con, seq, ce, mode, -1, newIdLevel1, name2, desc2, 0, null, null);
                     for (int t = 0; t < 100; t++) {
                         String name3 = "Level4_[" + i + "|" + y + "|" + t + "]";
                         FxString desc3 = new FxString(true, "Level4 Node " + y);
                         createNode(con, seq, ce, mode, -1, newIdLevel2, name3, desc3, 0, null, null);
                     }
                 }
                 LOG.info("Created level [" + (i + 1) + "/" + maxLevel + "]");
             }
         } catch (Exception e) {
             throw new FxApplicationException(LOG, e, "ex.tree.populate", mode.name(), e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void contentRemoved(Connection con, long contentId, boolean liveVersionRemovedOnly) throws FxApplicationException {
         PreparedStatement ps = null;
         try {
             //  nid
             List<Long> edit = new ArrayList<Long>(10), live = new ArrayList<Long>(10);
             long typeId = -1;
             ps = con.prepareStatement(TREE_REF_USAGE_EDIT);
             ps.setLong(1, contentId);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 edit.add(rs.getLong(2));
                 if (typeId == -1) typeId = rs.getLong(1);
             }
             ps.close();
             ps = con.prepareStatement(TREE_REF_USAGE_LIVE);
             ps.setLong(1, contentId);
             rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 live.add(rs.getLong(2));
                 if (typeId == -1) typeId = rs.getLong(1);
             }
             if (typeId == -1)
                 return; //nothing found
             ContentEngine ce = EJBLookup.getContentEngine();
             FxPK folderPK = null;
             for (long liveNode : live) {
                 boolean inEdit = edit.contains(liveNode);
                 if (liveVersionRemovedOnly || !inEdit)
                     try {
                         removeNode(con, FxTreeMode.Live, ce, liveNode, true);
                     } catch (FxNotFoundException e) {
                         //ok, may be an already removed childnode
                     }
                 else {
                     //if the node is a leaf then remove the node, else replace the referenced content with a new folder instance
                     try {
                         if (inEdit)
                             folderPK = handleContentDeleted(con, ce, folderPK, typeId, getTreeNodeInfo(con, FxTreeMode.Edit, liveNode));
                     } catch (FxNotFoundException e) {
                         //ok, may be an already removed childnode
                     }
                     try {
                         folderPK = handleContentDeleted(con, ce, folderPK, typeId, getTreeNodeInfo(con, FxTreeMode.Live, liveNode));
                     } catch (FxNotFoundException e) {
                         //ok, may be an already removed childnode
                     }
                 }
             }
             edit.removeAll(live);
             //remaining nodes only exist in edit tree
             for (long editNode : edit)
                 try {
                     folderPK = handleContentDeleted(con, ce, folderPK, typeId, getTreeNodeInfo(con, FxTreeMode.Edit, editNode));
                 } catch (FxNotFoundException e) {
                     //ok, may be an already removed childnode
                 }
         } catch (SQLException se) {
             throw new FxTreeException(LOG, "ex.db.sqlError", se.getMessage());
         } finally {
             FxContext.get().setTreeWasModified();
             try {
                 if (ps != null) ps.close();
             } catch (Throwable t) {
                 //ignore
             }
         }
     }
 
     /**
      * Handle removal of a content instance
      *
      * @param con      an open and valid connection
      * @param ce       a reference to the content instance
      * @param folderPK folder pk (if null a new one will be created if needed)
      * @param typeId   type of the content
      * @param nodeInfo node info
      * @return folder PK
      * @throws FxApplicationException on errors
      */
     protected FxPK handleContentDeleted(Connection con, ContentEngine ce, FxPK folderPK, long typeId, FxTreeNodeInfo nodeInfo) throws FxApplicationException {
         if (!nodeInfo.hasChildren()) {
             //leaf node - remove it
             removeNode(con, nodeInfo.getMode(), null, nodeInfo.getId(), false);
             return folderPK;
         }
         //if the referenced content is a foder, remove it without removing its children in edit mode and remove it with children in live mode
         if (FxType.FOLDER.equals(CacheAdmin.getEnvironment().getType(typeId).getName())) {
             removeNode(con, nodeInfo.getMode(), null, nodeInfo.getId(), nodeInfo.getMode() == FxTreeMode.Live);
             return folderPK;
         }
         //No folder and no leaf - replace the content with a new folder instance
         FxTreeNode node = getNode(con, nodeInfo.getMode(), nodeInfo.getId());
         if (folderPK == null)
             folderPK = createFolderInstance(ce, node.getName(), node.getLabel());
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("UPDATE " + getTable(nodeInfo.getMode()) + " SET REF=? WHERE ID=?");
             ps.setLong(1, folderPK.getId());
             ps.setLong(2, node.getId());
             ps.executeUpdate();
         } catch (SQLException se) {
             throw new FxTreeException(LOG, "ex.db.sqlError", se.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
         return folderPK;
     }
 
     /**
      * {@inheritDoc}
      */
     public void checkTreeIfEnabled(Connection con, FxTreeMode mode) throws FxApplicationException {
         if (!EJBLookup.getDivisionConfigurationEngine().get(SystemParameters.TREE_CHECKS_ENABLED))
             return;
         checkTree(con, mode);
     }
 
     /**
      * Check a tree for invalid nodes, will throw a FxTreeException on errors
      *
      * @param con  an open and valid connection
      * @param mode the tree to check
      * @throws com.flexive.shared.exceptions.FxTreeException
      *          on errors
      */
     public abstract void checkTree(Connection con, FxTreeMode mode) throws FxApplicationException;
 }
