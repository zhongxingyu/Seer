 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.connection;
 
 import Sirius.navigator.exception.ConnectionException;
 import de.cismet.reconnector.Reconnector;
 import de.cismet.reconnector.rmi.RmiReconnector;
 import Sirius.navigator.tools.CloneHelper;
 
 import Sirius.server.localserver.attribute.ClassAttribute;
 import Sirius.server.localserver.method.MethodMap;
 import Sirius.server.middleware.interfaces.proxy.CatalogueService;
 import Sirius.server.middleware.interfaces.proxy.MetaService;
 import Sirius.server.middleware.interfaces.proxy.QueryStore;
 import Sirius.server.middleware.interfaces.proxy.SearchService;
 import Sirius.server.middleware.interfaces.proxy.SystemService;
 import Sirius.server.middleware.interfaces.proxy.UserService;
 import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
 import Sirius.server.middleware.types.LightweightMetaObject;
 import Sirius.server.middleware.types.Link;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.User;
 import Sirius.server.newuser.UserException;
 import Sirius.server.newuser.UserGroup;
 import Sirius.server.search.SearchOption;
 import Sirius.server.search.SearchResult;
 import Sirius.server.search.store.Info;
 import Sirius.server.search.store.QueryData;
 
 import Sirius.util.image.ImageHashMap;
 
 import org.apache.log4j.Logger;
 
 import java.io.File;
 
 import java.net.MalformedURLException;
 
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.Icon;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 import de.cismet.cids.server.CallServerService;
 
 import de.cismet.security.Proxy;
 
 /**
  * A singleton factory class that creates and manages connections.
  *
  * @author   Pascal
  * @version  1.0 12/22/2002
  */
 public final class RMIConnection implements Connection, Reconnectable<CallServerService> {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(RMIConnection.class);
     private static final boolean IS_LEIGHTWEIGHT_MO_CODE_ENABLED;
     private static final String DISABLE_MO_FILENAME = "cids_disable_lwmo";  // NOI18N
 
     static {
         final String uHome = System.getProperty("user.home");  // NOI18N
         if (uHome != null) {
             final File homeDir = new File(uHome);
             final File disableIndicator = new File(homeDir, DISABLE_MO_FILENAME);
             IS_LEIGHTWEIGHT_MO_CODE_ENABLED = !disableIndicator.isFile();
             if (!IS_LEIGHTWEIGHT_MO_CODE_ENABLED) {
                 LOG.warn("LIGHTWIGHTMETAOBJECT CODE IS DISABLED! FOUND FILE: " + disableIndicator);
             }
         } else {
             IS_LEIGHTWEIGHT_MO_CODE_ENABLED = true;
         }
     }
 
     //~ Instance fields --------------------------------------------------------
 
     protected String callserverURL = null;
     protected boolean connected = false;
     protected java.lang.Object callserver;
     protected Reconnector<CallServerService> reconnector;
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Reconnector<CallServerService> getReconnector() {
         return reconnector;
     }
 
    private Reconnector<CallServerService> createReconnector() {
         reconnector = new RmiReconnector<CallServerService>(CallServerService.class, callserverURL);
         reconnector.useDialog(true, null);
         return reconnector;
     }
 
     @Override
     public boolean connect(final String callserverURL) throws ConnectionException {
         this.callserverURL = null;
         this.connected = false;
 
 //        try {
             LOG.info("creating network connection to callserver '" + callserverURL + "'");
            callserver = createReconnector().getProxy();
             //callserver = Naming.lookup(callserverURL);
 //        } catch (MalformedURLException mue) {
 //            LOG.fatal("'" + callserverURL + "' is not a valid URL", mue);
 //            throw new ConnectionException("'" + callserverURL + "' is not a valid URL", mue);
 //        } catch (NotBoundException nbe) {
 //            LOG.fatal("[NetworkError] could not connect to '" + callserverURL + "'", nbe);
 //            throw new ConnectionException("[NetworkError] could not connect to '" + callserverURL + "'", nbe);
 //        } catch (RemoteException re) {
 //            LOG.fatal("[ServerError] could not connect to '" + callserverURL + "'", re);
 //            throw new ConnectionException("[ServerError] could not connect to '" + callserverURL + "'", re);
 //        }
 
         if (LOG.isDebugEnabled()) {
             final StringBuffer buffer = new StringBuffer("remote interfaces of '").append(callserver.getClass()
                             .getName())
                         .append("': ");
             final Class[] interfaces = callserver.getClass().getInterfaces();
 
             for (int i = 0; i < interfaces.length; i++) {
                 buffer.append('\n');
                 buffer.append(interfaces[i].getName());
             }
 
             LOG.debug(buffer);
         }
 
         this.callserverURL = callserverURL;
         this.connected = true;
 
         return this.connected;
     }
 
     @Override
     public boolean connect(final String callserverURL, final Proxy proxy) throws ConnectionException {
         if (proxy != null) {
             LOG.warn("RMI over proxy not supported yet"); // NOI18N
         }
 
         return connect(callserverURL);
     }
 
     @Override
     public boolean reconnect() throws ConnectionException {
         if (callserverURL != null) {
             return connect(callserverURL);
         } else {
             LOG.error("can't reconnect - no connection informations from previous connection found");
             throw new ConnectionException(
                 "can't reconnect - no connection informations from previous connection found",
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public void disconnect() {
         this.connected = false;
         this.callserverURL = null;
         this.callserver = null;
     }
 
     @Override
     public boolean isConnected() {
         return this.connected;
     }
 
     // Default -----------------------------------------------------------------
     @Override
     public String[] getDomains() throws ConnectionException {
         try {
             return ((MetaService)callserver).getDomains();
         } catch (RemoteException re) {
             LOG.fatal("[ServerError] could not retrieve the local server names", re);
             throw new ConnectionException("[ServerError] could not retrieve the local server names: " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public ImageHashMap getDefaultIcons() throws ConnectionException {
         try {
             return new ImageHashMap(((SystemService)callserver).getDefaultIcons());
         } catch (RemoteException re) {
             LOG.fatal("[ServerError] could not retrieve the default icons", re);
             throw new ConnectionException("[ServerError] could not retrieve the default icons: " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public Icon getDefaultIcon(final String name) throws ConnectionException {
         try {
             // proxy should implement caching here
             return this.getDefaultIcons().get(name);
         } catch (ConnectionException ce) {
             LOG.fatal("[ServerError] could not retrieve the default icon for '" + name + "'");
             throw ce;
         }
     }
 
     // User ---------------------------------------------------------
     @Override
     public User getUser(final String usergroupLocalserver,
             final String usergroup,
             final String userLocalserver,
             final String username,
             final String password) throws ConnectionException, UserException {
         try {
             return ((UserService)callserver).getUser(
                     usergroupLocalserver,
                     usergroup,
                     userLocalserver,
                     username,
                     password);
         } catch (UserException ue) {
             LOG.warn("can't login: wrong user informations", ue);
             throw ue;
         } catch (RemoteException re) {
             LOG.fatal("[ServerError] can't login", re);
             throw new ConnectionException("[ServerError] can't login: " + re.getMessage(),
                 ConnectionException.FATAL,
                 re);
         }
     }
 
     @Override
     public Vector getUserGroupNames() throws ConnectionException {
         try {
             return ((UserService)callserver).getUserGroupNames();
         } catch (RemoteException re) {
             LOG.fatal("[ServerError] could not retrieve the usergroup names", re);
             throw new ConnectionException("[ServerError] could not retrieve the usergroup names: " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public Vector getUserGroupNames(final String username, final String domain) throws ConnectionException,
         UserException {
         try {
             return ((UserService)callserver).getUserGroupNames(username, domain);
         } catch (RemoteException re) {
             if (re.getMessage().indexOf("UserGroupException") != -1) {
                 LOG.warn("[ServerError] could not retrieve the usergroup names for user '" + username + "'", re);
                 throw new UserException(re.getMessage());
             } else {
                 LOG.fatal("[ServerError] could not retrieve the usergroup names for user '" + username
                             + "' on localserver '" + domain + "'",
                     re);
                 throw new ConnectionException("[ServerError] could not retrieve the usergroup names for user '"
                             + username + "' on localserver '" + domain + "': " + re.getMessage(),
                     ConnectionException.FATAL);
             }
         }
     }
 
     @Override
     public boolean changePassword(final User user, final String oldPassword, final String newPassword)
             throws ConnectionException, UserException {
         try {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("changing user password");
             }
             return ((UserService)callserver).changePassword(user, oldPassword, newPassword);
         } catch (UserException ue) {
             LOG.warn("could not change password");
             throw ue;
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not change user password", re);
             throw new ConnectionException("[ServerError] could not change user password: " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     // Node ---------------------------------------------------------
     @Override
     public Node[] getRoots(final User user, final String domain) throws ConnectionException {
         try {
             return ((CatalogueService)callserver).getRoots(user, domain);
         } catch (RemoteException re) {
             LOG.fatal("[ServerError] could not retrieve the top nodes of domain '" + domain + "'", re);
             throw new ConnectionException("[ServerError] could not  retrieve the top nodes of domain '" + domain + "': "
                         + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public Node[] getRoots(final User user) throws ConnectionException {
         try {
             return ((CatalogueService)callserver).getRoots(user);
         } catch (RemoteException re) {
             LOG.fatal("[CatalogueService] could not retrieve the top nodes", re);
             throw new ConnectionException("[CatalogueService] could not retrieve the top nodes: " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public Node[] getChildren(final Node node, final User user) throws ConnectionException {
         try {
             final Node[] n = ((CatalogueService)callserver).getChildren(node, user);
 
             if (node.isDynamic() && node.isSqlSort()) {
                 return n;
             }
 
             return Sirius.navigator.tools.NodeSorter.sortNodes(n);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not retrieve children of node '" + node, re);
             throw new ConnectionException("[ServerError] could not retrieve children of node '" + node
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public Node getNode(final User user, final int nodeID, final String domain) throws ConnectionException {
         try {
             return ((MetaService)callserver).getMetaObjectNode(user, nodeID, domain);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not retrieve node '" + nodeID + "' of domain '" + domain + "'", re);
             throw new ConnectionException("[ServerError] could not retrieve node '" + nodeID + "' of domain '" + domain
                         + "': " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public Node addNode(final Node node, final Link parent, final User user) throws ConnectionException {
         try {
             return ((CatalogueService)callserver).addNode(node, parent, user);
         } catch (RemoteException re) {
             LOG.error("[ServerError] addNode() could not add node '" + node + "'", re);
             throw new ConnectionException("[ServerError] addNode() could not add node '" + node + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public boolean deleteNode(final Node node, final User user) throws ConnectionException {
         try {
             return ((CatalogueService)callserver).deleteNode(node, user);
         } catch (RemoteException re) {
             LOG.error("[ServerError] deleteNode() could not delete node '" + node + "'", re);
             throw new ConnectionException("[ServerError] deleteNode() could not delete node '" + node + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public boolean addLink(final Node from, final Node to, final User user) throws ConnectionException {
         try {
             return ((CatalogueService)callserver).addLink(from, to, user);
         } catch (RemoteException re) {
             LOG.error("[ServerError] addLink() could not add Link", re);
             throw new ConnectionException("[ServerError] addLink() could not add Link: " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public boolean deleteLink(final Node from, final Node to, final User user) throws ConnectionException {
         try {
             return ((CatalogueService)callserver).deleteLink(from, to, user);
         } catch (RemoteException re) {
             LOG.error("[ServerError] deleteLink() could not delete Link", re);
             throw new ConnectionException("[ServerError] deleteLink() could not delete Link: " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public Node[] getClassTreeNodes(final User user) throws ConnectionException {
         try {
             return ((MetaService)callserver).getClassTreeNodes(user);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not retrieve the class tree nodes", re);
             throw new ConnectionException("[ServerError] could not retrieve the class tree nodes: " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     // Classes and Objects -----------------------------------------------------
     @Override
     public MetaClass getMetaClass(final User user, final int classID, final String domain) throws ConnectionException {
         try {
             return ((MetaService)callserver).getClass(user, classID, domain);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not retrieve meta class '" + classID + "' from domain '" + domain + "'",
                 re);
             throw new ConnectionException("[ServerError] could not retrieve meta class '" + classID + "' from domain '"
                         + domain + "': " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public MetaClass[] getClasses(final User user, final String domain) throws ConnectionException {
         try {
             return ((MetaService)callserver).getClasses(user, domain);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not retrieve the classes from domain '" + domain + "'", re);
             throw new ConnectionException("[ServerError] could not retrieve the classes from domain '" + domain + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public MetaObject[] getMetaObjectByQuery(final User user, final String query) throws ConnectionException {
         try {
             return ((MetaService)callserver).getMetaObject(user, query);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not retrieve MetaObject", re);
             throw new ConnectionException("[ServerError] could not retrieve MetaObject: " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public MetaObject[] getMetaObject(final User user, final Sirius.server.search.Query query)
             throws ConnectionException {
         try {
             return ((MetaService)callserver).getMetaObject(user, query);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not retrieve MetaObject", re);
             throw new ConnectionException("[ServerError] could not retrieve MetaObject: " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public MetaObject getMetaObject(final User user, final int objectID, final int classID, final String domain)
             throws ConnectionException {
         try {
             return ((MetaService)callserver).getMetaObject(user, objectID, classID, domain);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not retrieve MetaObject '" + objectID + '@' + classID + '@' + domain
                         + '\'',
                 re);
             throw new ConnectionException("[ServerError] could not retrieve the classes from domain '" + domain + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public int insertMetaObject(final User user, final Sirius.server.search.Query query, final String domain)
             throws ConnectionException {
         try {
             return ((MetaService)callserver).insertMetaObject(user, query, domain);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not insert / update MetaObject '" + query + "'", re);
             throw new ConnectionException("[[ServerError] could not insert / update MetaObject '" + query + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public MetaObject insertMetaObject(final User user, final MetaObject MetaObject, final String domain)
             throws ConnectionException {
         try {
             return ((MetaService)callserver).insertMetaObject(user, MetaObject, domain);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not insert MetaObject '" + MetaObject + "'", re);
             throw new ConnectionException("[[ServerError] could not insert MetaObject '" + MetaObject + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public int updateMetaObject(final User user, final MetaObject MetaObject, final String domain)
             throws ConnectionException {
         try {
             return ((MetaService)callserver).updateMetaObject(user, MetaObject, domain);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not update MetaObject '" + MetaObject + "'", re);
             throw new ConnectionException("[[ServerError] could not update MetaObject '" + MetaObject + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public int deleteMetaObject(final User user, final MetaObject MetaObject, final String domain)
             throws ConnectionException {
         try {
             return ((MetaService)callserver).deleteMetaObject(user, MetaObject, domain);
         } catch (RemoteException re) {
             LOG.error("[ServerError] deleteMetaObject(): could not delete MetaObject '" + MetaObject + "'", re);
             throw new ConnectionException("[[ServerError] deleteMetaObject(): could not delete MetaObject '"
                         + MetaObject + "': " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public MetaObject getInstance(final User user, final MetaClass c) throws ConnectionException {
         try {
             try {
                 final MetaObject mo = (MetaObject)CloneHelper.clone(((MetaService)callserver).getInstance(user, c));
                 return mo;
             } catch (CloneNotSupportedException ce) {
                 LOG.warn("could not clone MetaObject", ce);
                 return ((MetaService)callserver).getInstance(user, c);
             }
         } catch (RemoteException re) {
             LOG.error("[ServerError] getInstance(): could not get instance of class '" + c + "'", re);
             throw new ConnectionException("[[ServerError] getInstance(): could not get instance of class '" + c + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     // Dynmaic Search ----------------------------------------------------------
     @Override
     public HashMap getSearchOptions(final User user) throws ConnectionException {
         try {
             return ((Sirius.server.middleware.interfaces.proxy.SearchService)callserver).getSearchOptions(user);
         } catch (RemoteException re) {
             LOG.fatal("[SearchService] getSearchOptions() failed ...", re);
             throw new ConnectionException("[SearchService] getSearchOptions() failed: " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public HashMap getSearchOptions(final User user, final String domain) throws ConnectionException {
         try {
             return ((Sirius.server.middleware.interfaces.proxy.SearchService)callserver).getSearchOptions(user, domain);
         } catch (RemoteException re) {
             LOG.fatal("[SearchService] getSearchOptions() failed ...", re);
             throw new ConnectionException("[SearchService] getSearchOptions() failed: " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public SearchResult search(final User user, final String[] classIds, final SearchOption[] searchOptions)
             throws ConnectionException {
         try {
             return ((Sirius.server.middleware.interfaces.proxy.SearchService)callserver).search(
                     user,
                     classIds,
                     searchOptions);
         } catch (RemoteException re) {
             LOG.fatal("[SearchService] search failed ...", re);
             throw new ConnectionException("[SearchService] search failed: " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     // QueryData ---------------------------------------------------------------
     @Override
     public Info[] getUserQueryInfos(final User user) throws ConnectionException {
         try {
             return ((QueryStore)callserver).getQueryInfos(user);
         } catch (RemoteException re) {
             LOG.fatal("[ServerError] getUserGroupQueryInfos(UserGroup userGroup)", re);
             throw new ConnectionException("[ServerError] getUserGroupQueryInfos(UserGroup userGroup): "
                         + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public Info[] getUserGroupQueryInfos(final UserGroup userGroup) throws ConnectionException {
         try {
             return ((QueryStore)callserver).getQueryInfos(userGroup);
         } catch (RemoteException re) {
             LOG.fatal("[QueryStore] getUserGroupQueryInfos(UserGroup userGroup)", re);
             throw new ConnectionException("[QueryStore] getUserGroupQueryInfos(UserGroup userGroup): "
                         + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public QueryData getQueryData(final int id, final String domain) throws ConnectionException {
         try {
             return ((QueryStore)callserver).getQuery(id, domain);
         } catch (RemoteException re) {
             LOG.fatal("[QueryStore] getQuery(QueryInfo queryInfo)", re);
             throw new ConnectionException("[QueryStore] getQuery(QueryInfo queryInfo): " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public boolean storeQueryData(final User user, final QueryData data) throws ConnectionException {
         try {
             return ((QueryStore)callserver).storeQuery(user, data);
         } catch (RemoteException re) {
             LOG.fatal("[ueryStore] storeUserQuery(User user, Query query)", re);
             throw new ConnectionException("[ueryStore] storeUserQuery(User user, Query query): " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public boolean deleteQueryData(final int queryDataId, final String domain) throws ConnectionException {
         try {
             return ((QueryStore)callserver).delete(queryDataId, domain);
         } catch (RemoteException re) {
             LOG.fatal("[QueryStore] deleteQuery(QueryInfo queryInfo)", re);
             throw new ConnectionException("[QueryStore] deleteQuery(QueryInfo queryInfo): " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     // Methods -----------------------------------------------------------------
     @Override
     public MethodMap getMethods(final User user) throws ConnectionException {
         try {
             return ((MetaService)callserver).getMethods(user);
         } catch (RemoteException re) {
             LOG.fatal("[ServerError] could not retrieve methods", re);
             throw new ConnectionException("[ServerError] could not retrieve methods: " + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public MethodMap getMethods(final User user, final String domain) throws ConnectionException {
         try {
             return ((MetaService)callserver).getMethods(user, domain);
         } catch (RemoteException re) {
             LOG.fatal("[ServerError] could not retrieve methods from domain " + domain + "", re);
             throw new ConnectionException("[ServerError] could not retrieve methods from domain " + domain + ": "
                         + re.getMessage(),
                 ConnectionException.FATAL);
         }
     }
 
     @Override
     public int addQuery(final User user, final String name, final String description, final String statement)
             throws ConnectionException {
         try {
             return ((SearchService)callserver).addQuery(user, name, description, statement);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not add query '" + name + "'", re);
             throw new ConnectionException("[ServerError] could not add query '" + name + "': " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public int addQuery(final User user,
             final String name,
             final String description,
             final String statement,
             final int resultType,
             final char isUpdate,
             final char isRoot,
             final char isUnion,
             final char isBatch) throws ConnectionException {
         try {
             return ((SearchService)callserver).addQuery(
                     user,
                     name,
                     description,
                     statement,
                     resultType,
                     isUpdate,
                     isRoot,
                     isUnion,
                     isBatch);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not add query '" + name + "'", re);
             throw new ConnectionException("[ServerError] could not add query '" + name + "': " + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public boolean addQueryParameter(final User user,
             final int queryId,
             final String paramkey,
             final String description) throws ConnectionException {
         try {
             return ((SearchService)callserver).addQueryParameter(user, queryId, paramkey, description);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not add query parameter '" + queryId + "'", re);
             throw new ConnectionException("[ServerError] could not add query parameter '" + queryId + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public boolean addQueryParameter(final User user,
             final int queryId,
             final int typeId,
             final String paramkey,
             final String description,
             final char isQueryResult,
             final int queryPosition) throws ConnectionException {
         try {
             return ((SearchService)callserver).addQueryParameter(user, queryId, paramkey, description);
         } catch (RemoteException re) {
             LOG.error("[ServerError] could not add query parameter '" + queryId + "'", re);
             throw new ConnectionException("[ServerError] could not add query parameter '" + queryId + "': "
                         + re.getMessage(),
                 ConnectionException.ERROR);
         }
     }
 
     @Override
     public MetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
             final User user,
             final String[] representationFields,
             final String representationPattern) throws ConnectionException {
         try {
             if (IS_LEIGHTWEIGHT_MO_CODE_ENABLED) {
                 final LightweightMetaObject[] lwmos = ((MetaService)callserver).getAllLightweightMetaObjectsForClass(
                         classId,
                         user,
                         representationFields,
                         representationPattern);
                 return initLightweightMetaObjectsWithMetaService(lwmos);
             } else {
                 return getLightweightMetaObjectsFallback(classId, user);
             }
         } catch (RemoteException ex) {
             throw new ConnectionException("[ServerError] could not get all LightweightMetaObjects for class " + classId,
                 ex);
         }
     }
 
     @Override
     public MetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
             final User user,
             final String[] representationFields,
             final AbstractAttributeRepresentationFormater formater) throws ConnectionException {
         try {
             if (IS_LEIGHTWEIGHT_MO_CODE_ENABLED) {
                 final LightweightMetaObject[] lwmos = ((MetaService)callserver).getAllLightweightMetaObjectsForClass(
                         classId,
                         user,
                         representationFields);
                 return initLightweightMetaObjectsWithMetaServiceAndFormater(lwmos, formater);
             } else {
                 return getLightweightMetaObjectsFallback(classId, user);
             }
         } catch (RemoteException ex) {
             throw new ConnectionException("[ServerError] could not get all LightweightMetaObjects for class " + classId,
                 ex);
         }
     }
 
     @Override
     public MetaObject[] getLightweightMetaObjectsByQuery(final int classId,
             final User user,
             final String query,
             final String[] representationFields,
             final String representationPattern) throws ConnectionException {
         try {
             if (IS_LEIGHTWEIGHT_MO_CODE_ENABLED) {
                 final LightweightMetaObject[] lwmos = ((MetaService)callserver).getLightweightMetaObjectsByQuery(
                         classId,
                         user,
                         query,
                         representationFields,
                         representationPattern);
                 return initLightweightMetaObjectsWithMetaService(lwmos);
             } else {
                 return getLightweightMetaObjectsFallback(classId, user);
             }
         } catch (RemoteException ex) {
             throw new ConnectionException("[ServerError] could not get all LightweightMetaObjects for class " + classId,
                 ex);
         }
     }
 
     @Override
     public MetaObject[] getLightweightMetaObjectsByQuery(final int classId,
             final User user,
             final String query,
             final String[] representationFields,
             final AbstractAttributeRepresentationFormater formater) throws ConnectionException {
         try {
             if (IS_LEIGHTWEIGHT_MO_CODE_ENABLED) {
                 final LightweightMetaObject[] lwmos = ((MetaService)callserver).getLightweightMetaObjectsByQuery(
                         classId,
                         user,
                         query,
                         representationFields);
                 return initLightweightMetaObjectsWithMetaServiceAndFormater(lwmos, formater);
             } else {
                 return getLightweightMetaObjectsFallback(classId, user);
             }
         } catch (RemoteException ex) {
             throw new ConnectionException("[ServerError] could not get all LightweightMetaObjects for class " + classId,
                 ex);
         }
     }
 
     /**
      * !For debugging purpose only. Do not use!
      *
      * @param   classId  DOCUMENT ME!
      * @param   user     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     private MetaObject[] getLightweightMetaObjectsFallback(final int classId, final User user)
             throws ConnectionException {
         final MetaClass mc = ClassCacheMultiple.getMetaClass(user.getDomain(), classId);
         final ClassAttribute ca = mc.getClassAttribute("sortingColumn");  // NOI18N
         String orderBy = "";  // NOI18N
         if (ca != null) {
             final String value = ca.getValue().toString();
             orderBy = " order by " + value;  // NOI18N
         }
         final String query = "select " + mc.getID() + "," + mc.getPrimaryKey() + " from " + mc.getTableName() + orderBy;  // NOI18N
 
         return getMetaObjectByQuery(user, query);
     }
 
     /**
      * Initializes LWMOs with the appropriate metaservice.
      *
      * @param   lwmos  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private MetaObject[] initLightweightMetaObjectsWithMetaService(final LightweightMetaObject[] lwmos) {
         if (lwmos != null) {
             final MetaService msServer = (MetaService)callserver;
             for (final LightweightMetaObject lwmo : lwmos) {
                 if (lwmo != null) {
                     lwmo.setMetaService(msServer);
                 }
             }
         }
         return lwmos;
     }
 
     /**
      * Initializes LWMOs with the appropriate metaservice and string formatter.
      *
      * @param   lwmos     DOCUMENT ME!
      * @param   formater  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private MetaObject[] initLightweightMetaObjectsWithMetaServiceAndFormater(final LightweightMetaObject[] lwmos,
             final AbstractAttributeRepresentationFormater formater) {
         if (lwmos != null) {
             final MetaService msServer = (MetaService)callserver;
             for (final LightweightMetaObject lwmo : lwmos) {
                 if (lwmo != null) {
                     lwmo.setMetaService(msServer);
                     lwmo.setFormater(formater);
                 }
             }
         }
         return lwmos;
     }
 }
