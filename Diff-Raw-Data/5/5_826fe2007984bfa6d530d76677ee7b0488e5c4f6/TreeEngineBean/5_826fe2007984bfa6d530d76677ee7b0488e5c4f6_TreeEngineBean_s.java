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
 package com.flexive.ejb.beans;
 
 import com.flexive.core.Database;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.storage.TreeStorage;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxContentSecurityInfo;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.*;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.tree.FxTreeMode;
 import static com.flexive.shared.tree.FxTreeMode.Live;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.tree.FxTreeRemoveOp;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Flexive Tree implementation
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "TreeEngine", mappedName="TreeEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class TreeEngineBean implements TreeEngine, TreeEngineLocal {
     private static final Log LOG = LogFactory.getLog(TreeEngineBean.class);
 
     @Resource
     javax.ejb.SessionContext ctx;
 
     @EJB
     LanguageEngineLocal languageEngine;
     @EJB
     ContentEngineLocal contentEngine;
     @EJB
     SequencerEngineLocal seq;
     @EJB
     private ScriptingEngineLocal scripting;
 
     public TreeEngineBean() {
         // nothing to do
     }
 
     /**
      * Get the caption property id
      *
      * @return caption property id
      * @throws FxApplicationException on errors
      */
     private long getCaptionPropertyId() throws FxApplicationException {
         //TODO: cache me!
         return EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY);
     }
 
     final static boolean PARTIAL_LOADING = true;
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxTreeNode getTree(FxTreeMode mode, long nodeId, int depth) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().getTree(con, contentEngine, mode, nodeId, depth, PARTIAL_LOADING,
                     FxContext.getUserTicket().getLanguage());
         } catch (FxApplicationException fx) {
             throw fx;
         } catch (Throwable t) {
             throw new FxLoadException(LOG, t, "ex.tree.load.failed.node", nodeId, mode, t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public long getIdByPath(FxTreeMode mode, String path) throws FxApplicationException {
         return getIdByFQNPath(mode, FxTreeNode.ROOT_NODE, path);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public long getIdByFQNPath(FxTreeMode mode, long startNode, String path) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().getIdByFQNPath(con, mode, startNode, path);
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxTreeException(LOG, t, "ex.tree.getIdByPath.failed", path, mode);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public long getIdByLabelPath(FxTreeMode mode, long startNode, String path) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().getIdByLabelPath(con, mode, startNode, path);
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxTreeException(LOG, t, "ex.tree.getIdByPath.failed", path, mode);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String getPathById(FxTreeMode mode, long nodeId) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().getPathById(con, mode, nodeId);
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxTreeException(LOG, t, "ex.tree.getPathById.failed", nodeId, mode);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public long[] getIdChain(FxTreeMode mode, long nodeId) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().getIdChain(con, mode, nodeId);
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxTreeException(LOG, t, "ex.tree.getIdChain.failed", nodeId, mode);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * Create a new node
      *
      * @param mode         tree mode
      * @param parentNodeId id of the parent node
      * @param name         name (will only be used if no FQN property is available in the reference)
      * @param label        label for Caption property (only used if new reference is created)
      * @param position     position
      * @param reference    referenced content id
      * @param template     optional template to assign @return id of the created node
      * @return id of the node created
      * @throws FxApplicationException on errors
      */
     private long createNode(long parentNodeId, String name, FxString label,
                             int position, FxPK reference, String template, FxTreeMode mode) throws FxApplicationException {
         Connection con = null;
         if( mode == FxTreeMode.Live)
             throw new FxCreateException("ex.tree.create.live");
         try {
             FxContext.get().setTreeWasModified();
             con = Database.getDbConnection();
             boolean ok = false;
             try {
                 long nodeId =  StorageManager.getTreeStorage().createNode(con, seq, contentEngine, mode, -1,
                         parentNodeId, name, label, position, reference, template);
                 ok = true;
                 return nodeId;
             } finally {
                 if( ok && !ctx.getRollbackOnly() )
                     StorageManager.getTreeStorage().checkTreeIfEnabled(con, mode);
             }
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxCreateException(LOG, t, "ex.tree.create.failed", name);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long[] createNodes(FxTreeMode mode, long parentNodeId, int position, String path) throws FxApplicationException {
         Connection con = null;
         if( mode == FxTreeMode.Live)
             throw new FxCreateException("ex.tree.create.live");
         try {
             FxContext.get().setTreeWasModified();
             con = Database.getDbConnection();
             boolean ok = false;
             try {
                 long[] nodes = StorageManager.getTreeStorage().createNodes(con, seq, contentEngine, mode, parentNodeId, path, position);
                 // call scripts
                 final List<Long> scriptIds = scripting.getByScriptEvent(FxScriptEvent.AfterTreeNodeAdded);
                 if (scriptIds.size() == 0)
                     return nodes;
                 final FxScriptBinding binding = new FxScriptBinding();
                 for (long node : nodes) {
                     binding.setVariable("node", getNode(mode, node));
                     for (long scriptId : scriptIds)
                         scripting.runScript(scriptId, binding);
                 }
                 ok = true;
                 return nodes;
             } finally {
                 if( ok && !ctx.getRollbackOnly() )
                     StorageManager.getTreeStorage().checkTreeIfEnabled(con, mode);
             }
         } catch (FxApplicationException ae) {
             EJBUtils.rollback(ctx);
             throw ae;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, t, "ex.tree.create.failed", path);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void clear(FxTreeMode mode) throws FxApplicationException {
         Connection con = null;
         try {
             FxContext.get().setTreeWasModified();
             con = Database.getDbConnection();
             UserTicket ticket = FxContext.getUserTicket();
             if (FxContext.get().getRunAsSystem() || ticket.isGlobalSupervisor())
                 StorageManager.getTreeStorage().clearTree(con, contentEngine, mode);
             else
                 throw new FxApplicationException("ex.tree.clear.notAllowed", mode.name());
         } catch (FxApplicationException ae) {
             EJBUtils.rollback(ctx);
             throw ae;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, t, "FxTree.err.clear.failed", mode);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * Rename a node
      *
      * @param nodeId id of the node
      * @param mode   tree mode
      * @param name   new name
      * @param label  new label
      * @throws FxApplicationException on errors
      */
     private void renameNode(long nodeId, FxTreeMode mode, String name, FxString label) throws FxApplicationException {
         Connection con = null;
 //        boolean setInContent = false;
         try {
             FxContext.get().setTreeWasModified();
             con = Database.getDbConnection();
             FxTreeNode node = getNode(mode, nodeId);
             boolean nameChanged = false;
             if (!node.getName().equals(name) && !StringUtils.isEmpty(name)) {
                 StorageManager.getTreeStorage().updateName(con, mode, contentEngine, nodeId, name);
                 nameChanged = true;
             }
 
             if (label != null) {
                 // Load the content and its type
                 FxPK pk = new FxPK(node.getReference().getId(), mode == Live ? FxPK.LIVE : FxPK.MAX);
                 FxContent co = contentEngine.load(pk);
                 FxType type = CacheAdmin.getEnvironment().getType(co.getTypeId());
                 //noinspection LoopStatementThatDoesntLoop
                 for (FxPropertyAssignment pa : type.getAssignmentsForProperty(getCaptionPropertyId())) {
                     if (pa.isMultiLang() == label.isMultiLanguage()) {
                         co.setValue(pa.getXPath(), label);
                     } else {
                         if (pa.isMultiLang() && !label.isMultiLanguage()) {
                             FxString org = (FxString) co.getValue(pa.getXPath());
                             org.setDefaultTranslation(label.getDefaultTranslation());
                             co.setValue(pa.getXPath(), org);
                         } else if (!pa.isMultiLang() && label.isMultiLanguage()) {
                             co.setValue(pa.getXPath(), new FxString(false, label.getBestTranslation()));
                         }
                     }
                     contentEngine.save(co);
                     FxContext.get().setTreeWasModified();
                     return; //just change first occurance
                 }
                 //if we reach this, no caption property exists
                 if (!nameChanged && !label.equals(node.getLabel())) {
                     StorageManager.getTreeStorage().updateName(con, mode, contentEngine, nodeId, label.getBestTranslation());
                 }
             }
             StorageManager.getTreeStorage().checkTreeIfEnabled(con, mode);
         } catch (Exception t) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(LOG, t, "ex.tree.rename.failed", nodeId);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
 
     /**
      * Deletes the given node.
      *
      * @param mode                    tree mode to use (Live or Edit tree)
      * @param nodeId                  the node to delete
      * @param deleteReferencedContent delete referenced content
      * @param deleteChildren
      * @throws FxApplicationException on errors
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxTreeMode mode, long nodeId, boolean deleteReferencedContent, boolean deleteChildren) throws FxApplicationException {
         Connection con = null;
         if (nodeId < 0) {
             throw new FxInvalidParameterException("nodeId", "ex.tree.delete.nodeId");
         }
         try {
             FxContext.get().setTreeWasModified();
             con = Database.getDbConnection();
             final FxTreeNode subTree;
             if (deleteReferencedContent) {
                 subTree = deleteChildren ? getTree(mode, nodeId, Integer.MAX_VALUE) : getNode(mode, nodeId);
             } else {
                 subTree = null;
             }
             StorageManager.getTreeStorage().removeNode(con, mode, contentEngine, nodeId, deleteChildren);
             if (deleteReferencedContent) {
                 for (FxTreeNode node: subTree) {
                     if (node.getReference() != null && contentEngine.getReferencedContentCount(node.getReference()) == 0) {
                         // remove only contents that are not referenced elsewhere
                         contentEngine.remove(node.getReference());
                     }
                 }
             }
             StorageManager.getTreeStorage().checkTreeIfEnabled(con, mode);
         } catch (FxNotFoundException nf) {
             // Node does not exist and we wanted to delete it anyway .. so this is no error
         } catch (FxApplicationException ae) {
             EJBUtils.rollback(ctx);
             throw ae;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(LOG, t, "ex.tree.delete.failed", nodeId, t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxTreeNode node, boolean removeReferencedContent, boolean removeChildren) throws FxApplicationException {
         remove(node.getMode(), node.getId(), removeReferencedContent, removeChildren);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxTreeNode node, FxTreeRemoveOp removeOp, boolean removeChildren) throws FxApplicationException {
         remove(node.getMode(), node.getId(), removeOp, removeChildren);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxTreeMode mode, long nodeId, FxTreeRemoveOp removeOp, boolean removeChildren) throws FxApplicationException {
         Connection con = null;
         if (nodeId < 0) {
             throw new FxInvalidParameterException("nodeId", "ex.tree.delete.nodeId");
         }
         try {
             FxContext.get().setTreeWasModified();
             con = Database.getDbConnection();
             final FxTreeNode subTree;
             if (removeOp != FxTreeRemoveOp.Unfile) {
                 subTree = removeChildren ? getTree(mode, nodeId, Integer.MAX_VALUE) : getNode(mode, nodeId);
             } else {
                 subTree = null;
             }
             StorageManager.getTreeStorage().removeNode(con, mode, contentEngine, nodeId, removeChildren);
             if (removeOp != FxTreeRemoveOp.Unfile) {
                 for (FxTreeNode currNode: subTree) {
                     if (currNode.getReference() != null) {
                         long refCount = 0;
                         if (removeOp == FxTreeRemoveOp.RemoveSingleFiled) {
                             //reference count it only relevant if removeOp is RemoveSingleFiled
                             refCount = contentEngine.getReferencedContentCount(currNode.getReference());
                             if (currNode.getId() == nodeId)
                                 refCount++; //include the reference from the already removed node
                         }
                         if( removeOp == FxTreeRemoveOp.Remove || (removeOp == FxTreeRemoveOp.RemoveSingleFiled && refCount == 1) ) {
                             // remove only contents that are not referenced elsewhere
                             contentEngine.remove(currNode.getReference());
                         }
                     }
                 }
             }
             StorageManager.getTreeStorage().checkTreeIfEnabled(con, mode);
         } catch (FxNotFoundException nf) {
             // Node does not exist and we wanted to delete it anyway .. so this is no error
         } catch (FxApplicationException ae) {
             EJBUtils.rollback(ctx);
             throw ae;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(LOG, t, "ex.tree.delete.failed", nodeId, t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * Update a nodes reference
      *
      * @param node node to update
      * @throws FxTreeException on errors
      */
     private void updateReference(FxTreeNodeEdit node) throws FxTreeException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             StorageManager.getTreeStorage().updateReference(con, node.getMode(), node.getId(), node.getReference().getId());
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxTreeException(LOG, t, "ex.tree.update.reference.failed", node.getId(), node.getMode().name(), t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void move(FxTreeMode mode, long nodeId, long destinationId, int newPosition) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             StorageManager.getTreeStorage().move(con, seq, mode, nodeId, destinationId, newPosition);
             StorageManager.getTreeStorage().checkTreeIfEnabled(con, mode);
             FxContext.get().setTreeWasModified();
         } catch (FxApplicationException ae) {
             EJBUtils.rollback(ctx);
             throw ae;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(LOG, t, "ex.tree.move.failed", nodeId, destinationId, newPosition);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long copy(FxTreeMode mode, long source, long destination, int destinationPosition) throws FxApplicationException {
         FxContext.get().setTreeWasModified();
         Connection con = null;
         boolean success = false;
         try {
             con = Database.getDbConnection();
             long nodeId = StorageManager.getTreeStorage().copy(con, seq, mode, source, destination, destinationPosition, false, "CopyOf_");
             // call scripts
             final List<Long> scriptIds = scripting.getByScriptEvent(FxScriptEvent.AfterTreeNodeAdded);
             if (scriptIds.size() == 0)
                 return nodeId;
             FxTreeNode parent = getTree(mode, nodeId, 1000);
             final FxScriptBinding binding = new FxScriptBinding();
             for (long scriptId : scriptIds)
                 executeScript(scriptId, parent, binding);
             success = true;
             return nodeId;
         } catch (FxApplicationException ae) {
             EJBUtils.rollback(ctx);
             throw ae;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(LOG, t, "ex.tree.copy.failed", source, destination, destinationPosition);
         } finally {
             if (success)
                 StorageManager.getTreeStorage().checkTreeIfEnabled(con, mode);
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * Execute a script for a tree node and all its children
      *
      * @param scriptId id of the script to execute
      * @param node     node
      * @param binding  binding
      * @throws FxApplicationException on errors
      */
     private void executeScript(long scriptId, FxTreeNode node, FxScriptBinding binding) throws FxApplicationException {
         binding.setVariable("node", node);
         scripting.runScript(scriptId, binding);
         if (node.getChildren() != null && node.getChildren().size() > 0) {
             for (FxTreeNode child : node.getChildren())
                 executeScript(scriptId, child, binding);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void activate(FxTreeMode mode, long nodeId, boolean includeChildren) throws FxApplicationException {
         Connection con = null;
         try {
             if (mode == FxTreeMode.Live)
                 return;
             con = Database.getDbConnection();
             final FxTreeNode srcNode = StorageManager.getTreeStorage().getNode(con, mode, nodeId);
             if( !contentEngine.getContentVersionInfo(srcNode.getReference()).hasLiveVersion() )
                 throw new FxTreeException("ex.tree.activate.failed.noLiveContent", srcNode.getPath());
             if (!includeChildren) {
                 //if the node is a leaf node, always activate with children to propagate removed subnodes
                 if (srcNode.isLeaf())
                     includeChildren = true;
             }
 
             final List<Long> scriptBeforeIds = scripting.getByScriptEvent(FxScriptEvent.BeforeTreeNodeActivated);
             final List<Long> scriptAfterIds = scripting.getByScriptEvent(FxScriptEvent.AfterTreeNodeActivated);
 
             if (includeChildren) {
                 if (scriptBeforeIds.size() > 0) {
                     final FxScriptBinding binding = new FxScriptBinding();
                     FxTreeNode parent = getTree(FxTreeMode.Edit, nodeId, 1000);
                     for (long scriptId : scriptBeforeIds)
                         executeScript(scriptId, parent, binding);
                 }
                 StorageManager.getTreeStorage().activateSubtree(con, seq, contentEngine, mode, nodeId);
                 if (scriptAfterIds.size() > 0) {
                     final FxScriptBinding binding = new FxScriptBinding();
                     FxTreeNode parent = getTree(FxTreeMode.Live, nodeId, 1000);
                     for (long scriptId : scriptAfterIds)
                         executeScript(scriptId, parent, binding);
                 }
             } else {
                 if (scriptBeforeIds.size() > 0) {
                     final FxScriptBinding binding = new FxScriptBinding();
                     for (long scriptId : scriptBeforeIds) {
                         binding.setVariable("node", getNode(FxTreeMode.Edit, nodeId));
                         scripting.runScript(scriptId, binding);
                     }
                 }
                 StorageManager.getTreeStorage().activateNode(con, seq, contentEngine, mode, nodeId);
                 if (scriptAfterIds.size() > 0) {
                     final FxScriptBinding binding = new FxScriptBinding();
                     for (long scriptId : scriptAfterIds) {
                         binding.setVariable("node", getNode(FxTreeMode.Live, nodeId));
                         scripting.runScript(scriptId, binding);
                     }
                 }
             }
             StorageManager.getTreeStorage().checkTreeIfEnabled(con, mode);
             StorageManager.getTreeStorage().checkTreeIfEnabled(con, FxTreeMode.Live);
             FxContext.get().setTreeWasModified();
         } catch (FxApplicationException ae) {
             EJBUtils.rollback(ctx);
             throw ae;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(LOG, t, "ex.tree.activate.failed", nodeId, includeChildren, t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxTreeNode> getNodesWithReference(FxTreeMode mode, long reference) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().getNodesWithReference(con, mode, reference);
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxUpdateException(LOG, t, "ex.tree.getNodesWithReference.failed", mode, reference);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxTreeNode getNode(FxTreeMode mode, long id) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().getNode(con, mode, id);
         } catch (FxApplicationException fx) {
             throw fx;
         } catch (Throwable t) {
             throw new FxLoadException(LOG, t, "ex.tree.load.failed.node", id, mode, t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<String> getPaths(FxTreeMode mode, long... ids) throws FxApplicationException {
         if (ids == null || ids.length == 0)
             return new ArrayList<String>(0);
         Connection con = null;
         try {
             con = Database.getDbConnection();
             List<String> res = new ArrayList<String>(ids.length);
             TreeStorage tree = StorageManager.getTreeStorage();
             for (long id : ids)
                 res.add(tree.getPathById(con, mode, id));
             return res;
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxTreeException(LOG, t, "ex.tree.getPaths.failed", Arrays.toString(ids), mode);
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<String> getLabels(FxTreeMode mode, long... ids) throws FxApplicationException {
         return getLabels(mode, FxContext.getUserTicket().getLanguage(), ids);
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<String> getLabels(FxTreeMode mode, FxLanguage language, long... ids) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().getLabels(con, mode, getCaptionPropertyId(), language, true, ids);
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxUpdateException(LOG, t, "ex.tree.getLabels.failed", mode, language.getIso2digit(), t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public boolean exist(FxTreeMode mode, long id) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getTreeStorage().exists(con, mode, id);
         } catch (FxApplicationException ae) {
             throw ae;
         } catch (Throwable t) {
             throw new FxLoadException(LOG, t, "ex.tree.exist.failed", id, mode, t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String[] getDatas(FxTreeMode mode, long id) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long[] getReverseIdChain(FxTreeMode mode, long id) throws FxApplicationException {
         long[] chain = getIdChain(mode, id);
         ArrayUtils.reverse(chain);
         return chain;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setData(FxTreeMode mode, long nodeId, String data) {
         throw new UnsupportedOperationException("Not yet implemented");
         // provisional implementation
         /*
         Connection con = null;
         try {
             con = Database.getDbConnection();
             StorageManager.getTreeStorage().setData(con, mode, nodeId, data);
             FxContext.get().setTreeWasModified();
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw e;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxUpdateException(LOG, t, "ex.tree.setData.failed", data, nodeId, t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
         */
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxTreeNode findChild(FxTreeMode mode, long nodeId, String name) throws FxApplicationException {
         for (FxTreeNode node : getTree(mode, nodeId, 1).getChildren()) {
             if (node.getName().equals(name)) {
                 return node;
             }
         }
        throw new FxNotFoundException(LOG, "ex.tree.nodeNotFound.name", name, mode, nodeId);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxTreeNode findChild(FxTreeMode mode, long nodeId, long referenceId) throws FxApplicationException {
         for (FxTreeNode node : getTree(mode, nodeId, 1).getChildren())
             if (node.getReference().getId() == referenceId)
                 return node;
        throw new FxNotFoundException(LOG, "ex.tree.nodeNotFound.reference", referenceId, mode, nodeId);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxTreeNode findChild(FxTreeMode mode, long nodeId, FxPK pk) throws FxApplicationException {
         return findChild(mode, nodeId, pk.getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxTreeNode findChild(FxTreeMode mode, long nodeId, FxReference reference) throws FxApplicationException {
         return findChild(mode, nodeId, reference.getBestTranslation().getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxTreeNodeEdit node) throws FxApplicationException {
         if (node.isPartialLoaded())
             throw new FxTreeException("ex.tree.partialnode.notAllowed", node.getMode(), node.getId());
         if (node.isNew()) {
             long nodeId = createNode(node.getParentNodeId(), node.getName(), node.getLabel(), node.getPosition(),
                     node.getReference(), node.getData(), node.getMode());
             if ((node.getReference() == null || node.getReference().isNew()) &&
                     node.getACLId() != ACLCategory.INSTANCE.getDefaultId()) {
                 //requested a non-default ACL for a folder
                 FxTreeNode created = getNode(node.getMode(), nodeId);
                 FxContent co = contentEngine.load(created.getReference());
                 co.setAclId(node.getACLId());
                 contentEngine.save(co);
             }
             // call scripts
             final List<Long> scriptIds = scripting.getByScriptEvent(FxScriptEvent.AfterTreeNodeAdded);
             if (scriptIds.size() == 0)
                 return nodeId;
             final FxScriptBinding binding = new FxScriptBinding();
             binding.setVariable("node", getNode(node.getMode(), nodeId));
             for (long scriptId : scriptIds)
                 scripting.runScript(scriptId, binding);
             return nodeId;
         } else {
             FxTreeNode old = getNode(node.getOriginalMode(), node.getId());
             if (node.getReference().getId() != old.getReference().getId()) {
                 updateReference(node);
                 FxContentSecurityInfo si = contentEngine.getContentSecurityInfo(old.getReference());
                 if (si.getTypeId() == CacheAdmin.getEnvironment().getType(FxType.FOLDER).getId()) {
                     if (contentEngine.getReferencedContentCount(old.getReference()) == 0)
                         contentEngine.remove(old.getReference());
                 }
                 //need refresh with new reference data
                 old = getNode(node.getOriginalMode(), node.getId());
             }
             if (!old.getName().equals(node.getName()) || !old.getLabel().equals(node.getLabel()))
                 renameNode(node.getId(), node.getOriginalMode(), node.getName(), node.getLabel());
             if (old.getParentNodeId() != node.getParentNodeId() || old.getPosition() != node.getPosition())
                 move(node.getMode(), node.getId(), node.getParentNodeId(), node.getPosition());
             if (node.isActivate() && node.getMode() != FxTreeMode.Live)
                 activate(FxTreeMode.Edit, node.getId(), node.isActivateWithChildren());
             if (node.getACLId() != old.getACLId()) {
                 FxContent co = contentEngine.load(node.getReference());
                 co.setAclId(node.getACLId());
                 contentEngine.save(co);
             }
         }
         return node.getId();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void populate(FxTreeMode mode) throws FxApplicationException {
         final int maxNodeChildren = 3; // "default" of 3 data population nodes
         populate(mode, maxNodeChildren);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void populate(FxTreeMode mode, int maxNodeChildren) throws FxApplicationException {
 
         Connection con = null;
         try {
             con = Database.getDbConnection();
             StorageManager.getTreeStorage().populate(con, seq, contentEngine, mode, maxNodeChildren);
             FxContext.get().setTreeWasModified();
         } catch (FxApplicationException ae) {
             EJBUtils.rollback(ctx);
             throw ae;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             throw new FxLoadException(LOG, t, "ex.tree.populate", mode, t.getMessage());
         } finally {
             Database.closeObjects(TreeEngineBean.class, con, null);
         }
     }
 }
