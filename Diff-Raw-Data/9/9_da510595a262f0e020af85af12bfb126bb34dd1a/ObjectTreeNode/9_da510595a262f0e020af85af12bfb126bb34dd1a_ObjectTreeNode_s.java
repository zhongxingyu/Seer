 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.types.treenode;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.resource.ResourceManager;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 
 import org.apache.log4j.Logger;
 
 import javax.swing.ImageIcon;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class ObjectTreeNode extends DefaultMetaTreeNode {
 
     //~ Static fields/initializers ---------------------------------------------
 
    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2948703976584112759L;
 
     private static final transient Logger LOG = Logger.getLogger(ObjectTreeNode.class);
 
     //~ Instance fields --------------------------------------------------------
 
     protected ImageIcon nodeIcon;
     private MetaClass metaClass;
     private static final ResourceManager resource = ResourceManager.getManager();
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ObjectTreeNode object.
      *
      * @param  MetaObjectNode  DOCUMENT ME!
      */
     public ObjectTreeNode(final MetaObjectNode MetaObjectNode) {
         super(MetaObjectNode);
 
         try {
             final MetaClass metaClass = this.getMetaClass();
             if ((metaClass != null) && (metaClass.getObjectIconData().length > 0)) {
                 this.nodeIcon = new ImageIcon(metaClass.getObjectIconData());
             } else {
                 this.nodeIcon = resource.getIcon("ObjectNodeIcon.gif");//NOI18N
             }
         } catch (Exception exp) {
             this.nodeIcon = resource.getIcon("ObjectNodeIcon.gif");//NOI18N
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public MetaObjectNode getMetaObjectNode() {
         return (MetaObjectNode)this.userObject;
     }
 
     @Override
     public TreeNodeLoader getTreeNodeLoader() {
         return ((DefaultMetaTreeNode)this.getParent()).getTreeNodeLoader();
     }
 
     // --------------------------------------------------------------------------
     @Override
     public final synchronized void explore() throws Exception {
         if (LOG.isDebugEnabled()) {
             LOG.debug("[ObjectNode] Begin explore()"); // NOI18N
         }
 
         if (!isExplored() && !this.getMetaObjectNode().isLeaf()) {
             this.explored = this.getTreeNodeLoader().addChildren(this);
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("[ObjectNode] End explore()"); // NOI18N
         }
     }
 
     @Override
     public final boolean isRootNode() {
         return false;
     }
 
     @Override
     public final boolean isWaitNode() {
         return false;
     }
 
     @Override
     public final boolean isPureNode() {
         return false;
     }
 
     @Override
     public final boolean isObjectNode() {
         return true;
     }
 
     @Override
     public final boolean isClassNode() {
         return false;
     }
 
     @Override
     public final String toString() {
         String toString = getMetaObjectNode().getName();
         if (toString == null) {
             final MetaObject mo = getMetaObjectNode().getObject();
             if (mo != null) {
                 toString = mo.toString();
                 getNode().setName(toString);
             } else {
                 // Implicitly stores toString in getNode().setName(...) for future use.
                 // See implementation of getMetaObject().
                 toString = getMetaObject().toString();
             }
         }
         return toString;
     }
 
     @Override
     public final String getDescription() {
         return this.getMetaObjectNode().getDescription();
     }
 
     @Override
     public final ImageIcon getOpenIcon() {
         return this.nodeIcon;
     }
 
     @Override
     public final ImageIcon getClosedIcon() {
         return this.nodeIcon;
     }
 
     @Override
     public final ImageIcon getLeafIcon() {
         return this.nodeIcon;
     }
 
     @Override
     public final boolean equals(final DefaultMetaTreeNode node) {
         if (node.isObjectNode() && (this.getID() == node.getID()) && this.getDomain().equals(node.getDomain())) {
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public final boolean equalsNode(final Node node) {
         if ((node instanceof MetaObjectNode) && this.getMetaObjectNode().getDomain().equals(node.getDomain())
                     && (this.getMetaObjectNode().getId() == node.getId())) {
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public final int getID() {
         return this.getMetaObjectNode().getId();
     }
 
     @Override
     public final String getDomain() {
         return this.getMetaObjectNode().getDomain();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public final synchronized MetaObject getMetaObject() {
         if (this.getMetaObjectNode().getObject() == null) {
             try {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("caching object node"); // NOI18N
                 }
                 final MetaObject metaObject = SessionManager.getProxy()
                             .getMetaObject(this.getMetaObjectNode().getObjectId(),
                                 this.getMetaObjectNode().getClassId(),
                                 this.getMetaObjectNode().getDomain());
                 this.getMetaObjectNode().setObject(metaObject);
                 if ((getNode().getName() == null) || getNode().getName().equals("NameWirdGeladen")) {
                     getNode().setName(metaObject.toString());
                 }
             } catch (final Throwable t) {
                 LOG.error("could not retrieve meta object of node '" + getMetaObjectNode() + "'", t);
             }
         }
         return this.getMetaObjectNode().getObject();
     }
 
     /**
      * Setzt ein neues MetaObject, bzw. die ver\u00E4nderte Kopie des alten MetaObjects dieser Node.
      *
      * @param  metaObject  DOCUMENT ME!
      */
     public final void setMetaObject(final MetaObject metaObject) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("setting mo from " + getMetaObject() + " to " + metaObject); // NOI18N
         }
         this.getMetaObjectNode().setObject(metaObject);
         this.setChanged(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public final MetaClass getMetaClass() throws Exception {
         if (metaClass == null) {
             metaClass = SessionManager.getProxy().getMetaClass(this.getMetaObjectNode().getClassId(), this.getDomain());
         }
         return metaClass;
     }
 
     @Override
     public String getKey() throws Exception {
         if (LOG.isDebugEnabled()) {
             LOG.debug("getkey"); // NOI18N
         }
         return this.getMetaObject().getKey().toString();
     }
 
     @Override
     public int getClassID() {
         return this.getMetaObjectNode().getClassId();
     }
 }
