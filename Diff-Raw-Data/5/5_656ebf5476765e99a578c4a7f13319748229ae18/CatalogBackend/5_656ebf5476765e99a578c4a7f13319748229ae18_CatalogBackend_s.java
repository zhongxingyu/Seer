 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.jpa.backend.service.impl;
 
 import org.apache.log4j.Logger;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import de.cismet.cids.jpa.backend.core.PersistenceProvider;
 import de.cismet.cids.jpa.backend.service.CatalogService;
 import de.cismet.cids.jpa.entity.catalog.CatLink;
 import de.cismet.cids.jpa.entity.catalog.CatNode;
 import de.cismet.cids.jpa.entity.cidsclass.Attribute;
 import de.cismet.cids.jpa.entity.common.CommonEntity;
 import de.cismet.cids.jpa.entity.common.Domain;
 import de.cismet.cids.jpa.entity.permission.NodePermission;
 
 import de.cismet.diff.db.DatabaseConnection;
 
 /**
  * DOCUMENT ME!
  *
  * @author   Martin Scholl
  * @version  1.0
  */
 public class CatalogBackend implements CatalogService {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(CatalogBackend.class);
 
     private static final String Q_PARAM_IDFROM = "idFrom";                                                    // NOI18N
     private static final String Q_PARAM_IDTO = "idTo";                                                        // NOI18N
     private static final String Q_FROM_CATLINK_IDFROM_IDTO = "FROM CatLink WHERE idFrom = :" + Q_PARAM_IDFROM // NOI18N
                 + " AND idTo = :" + Q_PARAM_IDTO;                                                             // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient PersistenceProvider provider;
     private transient Set<Integer> nonLeafCache;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of <code>CatalogBackend.</code>
      *
      * @param  provider  DOCUMENT ME!
      */
     public CatalogBackend(final PersistenceProvider provider) {
         this.provider = provider;
         nonLeafCache = getNonLeafNodes();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @SuppressWarnings({ "PMD.AvoidCatchingGenericException" })
     @Override
     public Map<String, String> getSimpleObjectInformation(final CatNode node) {
         if (!node.getNodeType().equals(CatNode.Type.OBJECT.getType())) {
             return null;
         }
 
         final Map<String, String> objInfo = new HashMap<String, String>();
         final Map<String, String> tmp = new HashMap<String, String>();
         try {
             for (final Iterator<Attribute> it = node.getCidsClass().getAttributes().iterator(); it.hasNext();) {
                 final Attribute a = it.next();
                 tmp.put(a.getName(), a.getFieldName());
             }
         } catch (final Exception e) {
             LOG.warn("could not retrieve simple object information", e); // NOI18N
             return null;
         }
 
         Connection con = null;
         Statement stmt = null;
         ResultSet set = null;
         try {
             con = DatabaseConnection.getConnection(provider.getRuntimeProperties());
             con.setAutoCommit(false);
             stmt = con.createStatement();
             set = stmt.executeQuery(
                     "SELECT * "                                  // NOI18N
                             + "FROM "
                             + node.getCidsClass().getTableName() // NOI18N
                             + " WHERE id = "
                             + node.getObjectId());               // NOI18N
             while (set.next()) {
                 final Iterator<Entry<String, String>> entries = tmp.entrySet().iterator();
                 while (entries.hasNext()) {
                     final Entry<String, String> e = entries.next();
                     objInfo.put(e.getKey(), set.getString(e.getValue()));
                 }
 
                 break;
             }
             if (set.next()) {
                 throw new SQLException("query shall return just one row"); // NOI18N
             }
 
             set.close();
             con.commit();
 
             return objInfo;
         } catch (final SQLException ex) {
             LOG.error("error during object information fetching", ex); // NOI18N
             if (con != null) {
                 try {
                     con.rollback();
                 } catch (final SQLException sqle) {
                     LOG.error("could not roll back", sqle);            // NOI18N
                 }
             }
         } finally {
             DatabaseConnection.closeResultSet(set);
             DatabaseConnection.closeStatement(stmt);
             DatabaseConnection.closeConnection(con);
         }
 
         return null;
     }
 
     @Override
     public List<CatNode> getNodeParents(final CatNode node) {
         final EntityManager em = provider.getEntityManager();
         final Query q = em.createQuery(
                 "FROM CatNode node WHERE node.id in (SELECT idFrom FROM CatLink WHERE idTo = :id)"); // NOI18N
         q.setParameter("id", node.getId());                                                          // NOI18N
         final List<CatNode> nodeList = q.getResultList();
         final ListIterator<CatNode> it = nodeList.listIterator();
         while (it.hasNext()) {
             final CatNode n = it.next();
             n.setIsLeaf(false);
         }
         return nodeList;
     }
 
     @Override
     public List<CatNode> getNodeChildren(final CatNode node) {
         final EntityManager em = provider.getEntityManager();
         final Query q = em.createQuery(
                 "FROM CatNode node WHERE node.id in (SELECT idTo FROM CatLink WHERE idFrom = :id)"); // NOI18N
         q.setParameter("id", node.getId());                                                          // NOI18N
         final List<CatNode> nodeList = q.getResultList();
         final ListIterator<CatNode> it = nodeList.listIterator();
         while (it.hasNext()) {
             final CatNode n = it.next();
             n.setIsLeaf(isLeaf(n, true));
         }
 
         return nodeList;
     }
 
     @Override
     public List<CatNode> getRootNodes(final CatNode.Type type) {
         final EntityManager em = provider.getEntityManager();
         final Query q;
         if (type == null) {
             q = em.createQuery("FROM CatNode node WHERE node.isRoot = true ");                          // NOI18N
         } else {
             q = em.createQuery("FROM CatNode node WHERE node.isRoot = true AND node.nodeType = :type"); // NOI18N
             q.setParameter("type", type.getType());                                                     // NOI18N
         }
         final List<CatNode> nodeList = q.getResultList();
         final ListIterator<CatNode> it = nodeList.listIterator();
         while (it.hasNext()) {
             final CatNode n = it.next();
             n.setIsLeaf(isLeaf(n, true));
         }
         return nodeList;
     }
 
     @Override
     public List<CatNode> getRootNodes() {
         return getRootNodes(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   parent  DOCUMENT ME!
      * @param   node    DOCUMENT ME!
      *
      * @return  true if the node has been deleted from the database, false if only the link has been deleted
      */
     @Override
     public boolean deleteNode(final CatNode parent, final CatNode node) {
         final EntityManager em = provider.getEntityManager();
         // if parent == null then link will not exist
        Query q = null;
         if (parent != null) {
             q = em.createQuery("FROM CatLink c WHERE c.idTo = " + node.getId() + " AND c.idFrom = " + parent.getId()); // NOI18N
             provider.delete((CommonEntity)q.getSingleResult());
             if (isLeaf(parent, false)) {
                 parent.setIsLeaf(true);
                 nonLeafCache.remove(parent.getId());
             }
         }
         q = em.createQuery("FROM CatLink c WHERE c.idTo = " + node.getId());                                           // NOI18N
         if (q.getResultList().isEmpty()) {
             q = em.createQuery("FROM CatLink c WHERE c.idFrom = " + node.getId());                                     // NOI18N
             final Iterator<CatLink> lit = q.getResultList().iterator();
             while (lit.hasNext()) {
                 provider.delete(lit.next());
             }
             provider.delete(node);
             return true;
         } else {
             // revoke root status if node has not been deleted
             if (parent == null) {
                 node.setIsRoot(false);
                 provider.store(node);
             }
             return false;
         }
     }
 
     @Override
     public void deleteRootNode(final CatNode node) {
         deleteNode(null, node);
     }
 
     @Override
     public void moveNode(final CatNode oldParent, final CatNode newParent, final CatNode node) {
         final EntityManager em = provider.getEntityManager();
         final CatLink link;
         if (oldParent == null) {
             final Query q = em.createQuery("FROM Domain WHERE name = 'LOCAL'");
             final Domain domain = (Domain)q.getSingleResult();
             link = new CatLink();
             link.setIdTo(node.getId());
             link.setDomainTo(domain);
         } else {
             final Query q = em.createQuery(Q_FROM_CATLINK_IDFROM_IDTO);
             q.setParameter(Q_PARAM_IDFROM, oldParent.getId());
             q.setParameter(Q_PARAM_IDTO, node.getId());
 
             link = (CatLink)q.getSingleResult();
             if (isLeaf(oldParent, false)) {
                 oldParent.setIsLeaf(true);
                 nonLeafCache.remove(oldParent.getId());
             }
         }
         link.setIdFrom(newParent.getId());
         newParent.setIsLeaf(false);
         nonLeafCache.add(newParent.getId());
         provider.store(link);
     }
 
     @Override
     public void copyNode(final CatNode oldParent, final CatNode newParent, final CatNode node) {
         final EntityManager em = provider.getEntityManager();
         final Domain domainTo;
         // there is no domain for dynamic nodes
         if ((oldParent == null) || (node.getId() == -1)) {
             final Query q = em.createQuery("FROM Domain WHERE name = 'LOCAL'"); // NOI18N
             domainTo = (Domain)q.getSingleResult();
         } else {
             final Query q = em.createQuery(
                     "FROM CatLink WHERE idFrom = "
                             + oldParent.getId()
                             + "AND idTo = "
                             + node.getId());                                    // NOI18N
             domainTo = ((CatLink)q.getSingleResult()).getDomainTo();
         }
         // TODO: will the child nodes be copied also or linked or just left out
         CatNode newNode = new CatNode();
         newNode.setCidsClass(node.getCidsClass());
         newNode.setDerivePermFromClass(node.getDerivePermFromClass());
         newNode.setDynamicChildren(node.getDynamicChildren());
         newNode.setIcon(node.getIcon());
         newNode.setIconFactory(node.getIconFactory());
         newNode.setIsLeaf(node.isLeaf());
         newNode.setIsRoot(newParent == null);
         newNode.setName(node.getName());
         newNode.setNodePermissions((node.getId() == null) ? null : copyPermissions(node.getNodePermissions(), newNode));
         newNode.setNodeType(node.getNodeType());
         newNode.setObjectId(node.getObjectId());
         newNode.setPolicy(node.getPolicy());
         newNode.setSqlSort(node.getSqlSort());
        newNode.setUrl((node.getUrl().getId() == null) ? null : node.getUrl());
         newNode = addNode(newParent, newNode, domainTo);
         copyLinks(node, newNode);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   perms    DOCUMENT ME!
      * @param   newNode  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Set<NodePermission> copyPermissions(final Set<NodePermission> perms, final CatNode newNode) {
         final Set<NodePermission> ret = new HashSet<NodePermission>();
         for (final NodePermission perm : perms) {
             final NodePermission newPerm = new NodePermission();
             newPerm.setNode(newNode);
             newPerm.setPermission(perm.getPermission());
             newPerm.setUserGroup(perm.getUserGroup());
             ret.add(newPerm);
         }
         return ret;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  oldNode  DOCUMENT ME!
      * @param  newNode  DOCUMENT ME!
      */
     private void copyLinks(final CatNode oldNode, final CatNode newNode) {
         final EntityManager em = provider.getEntityManager();
         final Query q = em.createQuery("FROM CatLink WHERE idFrom = " + oldNode.getId()); // NOI18N
         final List<CatLink> links = q.getResultList();
         for (final CatLink link : links) {
             final CatLink newLink = new CatLink();
             newLink.setIdFrom(newNode.getId());
             newLink.setIdTo(link.getIdTo());
             newLink.setDomainTo(link.getDomainTo());
             provider.store(newLink);
         }
     }
 
     @Override
     public void linkNode(final CatNode oldParent, final CatNode newParent, final CatNode node) {
         final EntityManager em = provider.getEntityManager();
         final CatLink clone;
         if (oldParent == null) {
             final Query q = em.createQuery("FROM Domain WHERE name = 'LOCAL'");    // NOI18N
             final Domain domain = (Domain)q.getSingleResult();
             clone = new CatLink();
             clone.setDomainTo(domain);
             clone.setIdTo(node.getId());
         } else {
             final Query q = em.createQuery(
                     "FROM CatLink WHERE idFrom = "
                             + oldParent.getId()
                             + "AND idTo = "
                             + node.getId());                                       // NOI18N
             final CatLink link = (CatLink)q.getSingleResult();
             try {
                 clone = link.clone();
             } catch (final CloneNotSupportedException ex) {
                 throw new IllegalStateException("cannot clone link: " + link, ex); // NOI18N
             }
         }
 
         clone.setIdFrom(newParent.getId());
         newParent.setIsLeaf(false);
         nonLeafCache.add(newParent.getId());
         provider.store(clone);
     }
 
     @Override
     public CatNode addNode(final CatNode parent, final CatNode newNode, final Domain domainTo) {
         if (newNode == null) {
             throw new IllegalArgumentException("new node must not be null");                 // NOI18N
         }
         if (domainTo == null) {
             throw new IllegalArgumentException("domainTo must not be null");                 // NOI18N
         }
         if ((parent == null) && !newNode.getIsRoot()) {
             throw new IllegalStateException("if parent == null new node must be root node"); // NOI18N
         }
         final CatNode node = provider.store(newNode);
         if (parent != null) {
             final CatLink link = new CatLink();
             link.setIdFrom(parent.getId());
             link.setIdTo(node.getId());
             link.setDomainTo(domainTo);
             provider.store(link);
             parent.setIsLeaf(false);
             nonLeafCache.add(parent.getId());
         }
         return node;
     }
 
     @Override
     public boolean isLeaf(final CatNode node, final boolean useCache) {
         if (useCache && (nonLeafCache != null)) {
             {
                 return !nonLeafCache.contains(node.getId());
             }
         }
         final EntityManager em = provider.getEntityManager();
         final Integer id = node.getId();
         final Query q = em.createQuery("select id from CatLink link where link.idFrom = :id"); // NOI18N
         q.setParameter("id", id);                                                              // NOI18N
         q.setMaxResults(1);
         return q.getResultList().isEmpty();
     }
 
     @Override
     public void reloadNonLeafNodeCache() {
         final Thread t = new Thread(new Runnable() {
 
                     @Override
                     public void run() {
                         getNonLeafNodes();
                     }
                 });
         t.start();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @SuppressWarnings({ "PMD.AvoidCatchingGenericException" })
     private synchronized Set<Integer> getNonLeafNodes() {
         Connection con = null;
         Statement stmt = null;
         ResultSet set = null;
         try {
             con = DatabaseConnection.getConnection(provider.getRuntimeProperties());
             stmt = con.createStatement();
             set = stmt.executeQuery("SELECT DISTINCT id_from FROM cs_cat_link"); // NOI18N
         } catch (final Exception ex) {
             LOG.error("could not fetch nonLeafCache", ex);                       // NOI18N
             DatabaseConnection.closeResultSet(set);
             DatabaseConnection.closeStatement(stmt);
             DatabaseConnection.closeConnection(con);
             return null;
         }
 
         final Set<Integer> ret = new HashSet<Integer>();
         try {
             while (set.next()) {
                 ret.add(set.getInt(1));
             }
         } catch (final Exception ex) {
             LOG.error("could not build non leaf node id cache", ex); // NOI18N
             return null;
         } finally {
             DatabaseConnection.closeResultSet(set);
             DatabaseConnection.closeStatement(stmt);
             DatabaseConnection.closeConnection(con);
         }
 
         return nonLeafCache = ret;
     }
 
     @Override
     public Domain getLinkDomain(final CatNode from, final CatNode to) {
         if ((from == null) || (to == null)) {
             return null;
         }
         final EntityManager em = provider.getEntityManager();
         final Query q = em.createQuery("FROM CatLink link WHERE link.idFrom = :idFrom AND link.idTo = :idTo"); // NOI18N
         q.setParameter("idFrom", from.getId());                                                                // NOI18N
         q.setParameter("idTo", to.getId());                                                                    // NOI18N
         final CatLink link = (CatLink)q.getSingleResult();
         return provider.getEntity(Domain.class, link.getDomainTo().getId());
     }
 
     @Override
     public void setLinkDomain(final CatNode from, final CatNode to, final Domain domainTo) {
         if (from == null) {
             throw new IllegalArgumentException("from node must not be null"); // NOI18N
         }
         if (to == null) {
             throw new IllegalArgumentException("to node must not be null");   // NOI18N
         }
         if (domainTo == null) {
             throw new IllegalArgumentException("domainTo must not be null");  // NOI18N
         }
 
         final EntityManager em = provider.getEntityManager();
         final Query q = em.createQuery("FROM CatLink link WHERE link.idFrom = :idFrom AND link.idTo = :idTo"); // NOI18N
         q.setParameter("idFrom", from.getId());                                                                // NOI18N
         q.setParameter("idTo", to.getId());                                                                    // NOI18N
         final CatLink link = (CatLink)q.getSingleResult();
         link.setDomainTo(domainTo);
         provider.store(link);
     }
 }
