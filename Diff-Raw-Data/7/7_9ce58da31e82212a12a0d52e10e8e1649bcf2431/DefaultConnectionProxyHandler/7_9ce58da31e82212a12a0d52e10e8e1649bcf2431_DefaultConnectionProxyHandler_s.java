 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.connection.proxy;
 
 import Sirius.navigator.connection.ConnectionSession;
 import Sirius.navigator.exception.ConnectionException;
 
 import Sirius.server.localserver.method.MethodMap;
 import Sirius.server.middleware.types.Link;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.User;
 import Sirius.server.search.CidsServerSearch;
 import Sirius.server.search.Query;
 import Sirius.server.search.SearchOption;
 import Sirius.server.search.SearchResult;
 
 import Sirius.util.image.ImageHashMap;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * Default implementation of the connection proxy interface.
  *
  * @author   Pascal
  * @version  1.0 12/22/2002
  */
 public class DefaultConnectionProxyHandler extends ConnectionProxyHandler {
 
     //~ Instance fields --------------------------------------------------------
 
     protected final ProxyInterface proxyHandler;
     protected ImageHashMap iconCache = null;
     protected ClassAndMethodCache classAndMethodCache = null;
     protected HashMap objectCache = new HashMap();
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DefaultConnectionProxyHandler object.
      *
      * @param  connectionSession  DOCUMENT ME!
      */
     public DefaultConnectionProxyHandler(final ConnectionSession connectionSession) {
         super(connectionSession);
         proxyHandler = new DefaultConnectionProxyHandler.DefaultConnectionProxy();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public HashMap getClassHash() {
         return classAndMethodCache.getClassHash();
     }
 
     @Override
     public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
         try {
             if (method.getDeclaringClass().equals(Sirius.navigator.connection.Connection.class)) {
                 if (method.getName().equals("getDefaultIcons")) {             // NOI18N
                     if (iconCache == null) {
                         if (log.isInfoEnabled()) {
                             log.info("[ConnectionProxy] filling icon cache"); // NOI18N
                         }
                         iconCache = (ImageHashMap)method.invoke(connection, args);
                     }
 
                     return iconCache;
                 } else {
                     return method.invoke(connection, args);
                 }
             } else if (method.getDeclaringClass().equals(Sirius.navigator.connection.proxy.ProxyInterface.class)) {
                 return method.invoke(proxyHandler, args);
             } else {
                 log.error("[ConnectionProxy] undeclared method '" + method.getName() + "'");                  // NOI18N
                 throw new RuntimeException("[ConnectionProxy] undeclared method '" + method.getName() + "'"); // NOI18N
             }
         } catch (final InvocationTargetException itex) {
             // ok, no need to worry about
             throw itex.getTargetException();
         } catch (final Exception ex) {
             log.error("[ConnectionProxy] unexpected invocation exception' " + ex.getMessage() + "'", ex);              // NOI18N
             throw new RuntimeException("[ConnectionProxy] unexpected invocation exception' " + ex.getMessage() + "'"); // NOI18N
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class ClassAndMethodCache {
 
         //~ Instance fields ----------------------------------------------------
 
         private HashMap classHash = null;
         private HashMap methodHash = null;
         private List lsNames = null;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Konstruiert einen neuen leeren ClassCache. Der ClassCache wird beim erstmaligen Ladens einer
          * Sirius.Middleware.Types.Class gefuellt.
          */
         public ClassAndMethodCache()    // MetaService metaService)
         {
             classHash = new HashMap(25, 0.5f);
             methodHash = new HashMap(25, 0.5f);
             lsNames = new ArrayList(5);
             // metaServiceRef = metaService;
         }
 
         /**
          * Konstruiert einen neuen ClassCache, der mit den Classes eines bestimmten Lokalservers gefuellt wird.
          *
          * @param  user              User
          * @param  localServerNames  DOCUMENT ME!
          */
         public ClassAndMethodCache(final User user, final String[] localServerNames) {
             classHash = new HashMap(50, 0.5f);
             methodHash = new HashMap(25, 0.5f);
             lsNames = new ArrayList((localServerNames.length + 1));
             // metaServiceRef = metaService;
             try {
                 final MethodMap methodMap = connection.getMethods(user);
                 if (methodMap != null) {
                     this.putMethods(methodMap);
                 }
             } catch (final Exception e) {
                 log.fatal("Ausnahme im ClassAndMethodCache beim Aufruf von remoteNodeRef.getMethods(...): ", e); // NOI18N
             }
             for (int i = 0; i < localServerNames.length; i++) {
                 try {
                     final MetaClass[] tmpClasses = connection.getClasses(user, localServerNames[i]);             // .getClasses(user, localServerNames[i]);
 
                     if (tmpClasses != null) {
                         putClasses(tmpClasses, localServerNames[i]);
                     }
                 } catch (final Exception e) {
                     log.fatal("Ausnahme im ClassAndMethodCache beim Aufruf von remoteNodeRef.getClasses(...): ", e); // NOI18N
                 }
             }
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public HashMap getClassHash() {
             return classHash;
         }
 
         /**
          * Laedt eine Class aus dem Cache bzw. vom Server.<br>
          * Ist die Class noch nicht im Cache enthalten wird sie vom Server geladen, wurden von diesem LocalServer noch
          * keine Classes geladen, so werden alle Classes dieses LocalServers gecacht.
          *
          * @param   user             User.
          * @param   classID          Die ID der zu ladenden Class.
          * @param   localServerName  Der LocalServer des Users.
          *
          * @return  DOCUMENT ME!
          *
          * @throws  ConnectionException  DOCUMENT ME!
          */
         public MetaClass getCachedClass(final User user, final int classID, final String localServerName)
                 throws ConnectionException {
             final String key = new String(localServerName + classID);
             // Falls noch keine Class von diesem LocalServer geladen wurde,
             // -> alle Classes des LocalServer cachen
             if (!lsNames.contains(localServerName)) {
                 final MetaClass[] tmpClasses = connection.getClasses(user, localServerName);
                 this.putClasses(tmpClasses, localServerName);
                 this.putMethods(connection.getMethods(user, localServerName));
                 if (log.isDebugEnabled()) {
                     log.debug("<CC> Classes von neuem LocalServer " + localServerName + " gecacht");
                 }
             }
 
             // Falls die Class nicht im Cache enthalten ist
             // -> Class vom Server laden
             if (!classHash.containsKey(key)) {
                 final MetaClass tmpClass = connection.getMetaClass(user, classID, localServerName);
                 this.putClass(tmpClass, localServerName);
                 this.putMethods(connection.getMethods(user, localServerName));
                 return tmpClass;
             } else {
                 return (MetaClass)classHash.get(key);
             }
         }
 
         /**
          * Liefert alle Classes, die sich im Cache befinden.
          *
          * @return  Ein Array von Type Sirius.Middleware.Types.Class oder null.
          */
         public MetaClass[] getAllCachedClasses() {
             final List classVector = new ArrayList(classHash.values());
 
             if (classVector == null) {
                 return null;
             }
 
             return (MetaClass[])classVector.toArray(new MetaClass[classVector.size()]);
         }
 
         /**
          * Fuegt eine Class zum ClassCache hinzu.
          *
          * @param  cls   Die zu cachende Class.
          * @param  lsID  LocalServer ID, bildet zusammen mit der Class ID einen einduetigen Schluessel fuer die
          *               Hashtable
          */
         protected void putClass(final MetaClass cls, final String lsID) {
             final String key = String.valueOf(lsID + cls.getID());
             if (!classHash.containsKey(key)) {
                 classHash.put(key, cls);
             }
         }
 
         /**
          * Fuegt ein Array von Classes zum ClassCache hinzu.
          *
          * @param  classes          Ein Array von cachenden Classes.
          * @param  localServerName  DOCUMENT ME!
          */
         protected void putClasses(final MetaClass[] classes, final String localServerName) {
             lsNames.add(localServerName);
             for (int i = 0; i < classes.length; i++) {
                 final String key = localServerName + classes[i].getID();
                 if (!classHash.containsKey(key)) {
                     classHash.put(key, classes[i]);
                 }
 
                 if (log.isDebugEnabled()) {
                     log.debug("<CMC> Class gecacht: " + classes[i].getName() + " " + classes[i].getID() + " "
                                 + classes[i].getDomain());
                 }
             }
             if (log.isDebugEnabled()) {
                 log.debug("<CMC> " + classes.length + " Classes von LocalServer " + localServerName + " gecacht.");
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   methodKey  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Sirius.server.localserver.method.Method getCachedMethod(final String methodKey) {
             return (Sirius.server.localserver.method.Method)methodHash.get(methodKey);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  method           DOCUMENT ME!
          * @param  localServerName  DOCUMENT ME!
          */
         protected void putMethod(final Sirius.server.localserver.method.Method method, final String localServerName) {
             final String key = String.valueOf(localServerName + method.getID());
             if (!methodHash.containsKey(key)) {
                 methodHash.put(key, method);
                 if (log.isDebugEnabled()) {
                     log.debug("<CMC> method '" + key + "' gecacht.");
                 }
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  methodMap  DOCUMENT ME!
          */
         protected void putMethods(final MethodMap methodMap) {
             if (methodMap != null) {
                 methodHash.putAll(methodMap);
                 if (log.isDebugEnabled()) {
                     final Iterator iterator = methodMap.keySet().iterator();
                     if (iterator.hasNext()) {
                         if (log.isDebugEnabled()) {
                             log.debug("<CMC> method '" + iterator.next() + " gecacht."); // NOI18N
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class DefaultConnectionProxy implements ProxyInterface {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void setProperty(final String name, final String value) {
             if (log.isDebugEnabled()) {
                 log.debug("[ProxyInterface] setting propety '" + name + "' to '" + value + "'");
             }
         }
 
         @Override
         public ConnectionSession getSession() {
             return session;
         }
 
         @Override
         public Node[] getRoots() throws ConnectionException {
             return connection.getRoots(session.getUser());
         }
 
         @Override
         public Node[] getRoots(final String domain) throws ConnectionException {
             return connection.getRoots(session.getUser(), domain);
         }
 
         @Override
         public Node[] getChildren(final Node node) throws ConnectionException {
             final Node[] c = connection.getChildren(node, session.getUser());
 
             if (node.isDynamic() && node.isSqlSort()) {
                 return c;
             }
 
             return Sirius.navigator.tools.NodeSorter.sortNodes(c);
         }
 
         @Override
         public Node getNode(final int nodeID, final String domain) throws ConnectionException {
             return connection.getNode(session.getUser(), nodeID, domain);
         }
 
         @Override
         public Node addNode(final Node node, final Link parent) throws ConnectionException {
             return connection.addNode(node, parent, session.getUser());
         }
 
         @Override
         public boolean deleteNode(final Node node) throws ConnectionException {
             return connection.deleteNode(node, session.getUser());
         }
 
         @Override
         public boolean addLink(final Node from, final Node to) throws ConnectionException {
             return connection.addLink(from, to, session.getUser());
         }
 
         @Override
         public boolean deleteLink(final Node from, final Node to) throws ConnectionException {
             return connection.deleteLink(from, to, session.getUser());
         }
 
         @Override
         public Node[] getClassTreeNodes() throws ConnectionException {
             return connection.getClassTreeNodes(session.getUser());
         }
 
         @Override
         public HashMap getSearchOptions() throws ConnectionException {
             if (log.isDebugEnabled()) {
                 final HashMap searchOptions = connection.getSearchOptions(session.getUser());
                 log.info(searchOptions.size() + " search options loaded"); // NOI18N
 
                 return searchOptions;
             } else {
                 return connection.getSearchOptions(session.getUser());
             }
         }
 
         @Override
         public SearchResult search(final Collection classIds, final Collection searchOptions)
                 throws ConnectionException {
             if (log.isDebugEnabled()) {
                 log.debug(classIds);
             }
             for (final Object so : searchOptions) {
                 final SearchOption sopt = (SearchOption)so;
                 if (log.isDebugEnabled()) {
                     log.debug(sopt);
                 }
             }
 
             return connection.search(session.getUser(),
                     (String[])classIds.toArray(new String[classIds.size()]),
                     (SearchOption[])searchOptions.toArray(new SearchOption[searchOptions.size()]));
         }
 
         @Override
         public SearchResult search(final Collection searchOptions) throws ConnectionException {
             return this.search(new LinkedList(), searchOptions);
         }
 
         @Override
         public int addQuery(final String name, final String description, final String statement)
                 throws ConnectionException {
             return connection.addQuery(session.getUser(), name, description, statement);
         }
 
         @Override
         public int addQuery(final String name,
                 final String description,
                 final String statement,
                 final int resultType,
                 final char isUpdate,
                 final char isRoot,
                 final char isUnion,
                 final char isBatch) throws ConnectionException {
             return connection.addQuery(session.getUser(),
                     name,
                     description,
                     statement,
                     resultType,
                     isUpdate,
                     isRoot,
                     isUnion,
                     isBatch);
         }
 
         @Override
         public boolean addQueryParameter(final int queryId, final String paramkey, final String description)
                 throws ConnectionException {
             return connection.addQueryParameter(session.getUser(), queryId, paramkey, description);
         }
 
         @Override
         public boolean addQueryParameter(final int queryId,
                 final int typeId,
                 final String paramkey,
                 final String description,
                 final char isQueryResult,
                 final int queryPosition) throws ConnectionException {
             return connection.addQueryParameter(session.getUser(),
                     queryId,
                     typeId,
                     paramkey,
                     description,
                     isQueryResult,
                     queryPosition);
         }
 
         @Override
         public MetaClass[] getClasses() throws ConnectionException {
             final String[] domains = connection.getDomains();
             final ArrayList classes = new ArrayList();
 
             for (int i = 0; i < domains.length; i++) {
                 MetaClass[] classArray = new MetaClass[0];
                 try {
                     classArray = this.getClasses(domains[i]);
                 } catch (final Exception t) {
                     log.error("Fehler im DefaultConnectionProxyHandler bei getClasses", t);
                 }
 
                 for (int j = 0; j < classArray.length; j++) {
                     classes.add(classArray[j]);
                 }
             }
 
             return (MetaClass[])classes.toArray(new MetaClass[classes.size()]);
         }
 
         @Override
         public MetaClass[] getClasses(final String domain) throws ConnectionException {
             return connection.getClasses(session.getUser(), domain);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws  ConnectionException  DOCUMENT ME!
          */
         public void initClassAndMethodCache() throws ConnectionException {
             classAndMethodCache = new ClassAndMethodCache(session.getUser(), connection.getDomains());
         }
 
         @Override
         public MetaClass getMetaClass(final int classID, final String domain) throws ConnectionException {
             if (classAndMethodCache == null) {
                 if (log.isInfoEnabled()) {
                     log.info("[ConnectionProxy] filling meta class cache"); // NOI18N
                 }
                 classAndMethodCache = new ClassAndMethodCache(session.getUser(), connection.getDomains());
             }
 
             final MetaClass metaClass = classAndMethodCache.getCachedClass(session.getUser(), classID, domain);
             if (log.isDebugEnabled()) {
                 log.debug("getgetMetaClass(): classID=" + classID + ", domain=" + domain);
                 log.debug("MetaClass: " + metaClass + "\nMetaClass.getName(): " + metaClass.getName()
                             + "\nMetaClass.getEditor(): " + metaClass.getEditor() + "\nMetaClass.getComplexEditor(): "
                             + metaClass.getComplexEditor());
             }
 
             return metaClass;
         }
 
         @Override
         public MetaClass getMetaClass(final String classKey) throws ConnectionException {
             try {
                 final StringTokenizer tokenizer = new StringTokenizer(classKey, "@"); // NOI18N
                 final int classID = Integer.valueOf(tokenizer.nextToken()).intValue();
                 final String domain = tokenizer.nextToken();
 
                 return this.getMetaClass(classID, domain);
             } catch (final ConnectionException cexp) {
                 throw cexp;
             } catch (final Exception t) {
                 log.error("malformed classKey: '" + classKey + "' (classId@domain expected)"); // NOI18N
                 throw new ConnectionException("malformed class key: '" + classKey + "' (classId@domain expected)",
                     ConnectionException.ERROR,
                     t);                                                                        // NOI18N
             }
         }
 
         @Override
         public Sirius.server.localserver.method.Method getMethod(final String methodKey) throws ConnectionException {
             if (classAndMethodCache == null) {
                 if (log.isInfoEnabled()) {
                     log.info("[ConnectionProxy] filling meta class cache"); // NOI18N
                 }
                 classAndMethodCache = new ClassAndMethodCache(session.getUser(), connection.getDomains());
             }
 
             return classAndMethodCache.getCachedMethod(methodKey);
         }
 
         @Override
         public MetaObject getMetaObject(final int objectID, final int classID, final String domain)
                 throws ConnectionException {
             if (classAndMethodCache == null) {
                 initClassAndMethodCache();
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("getMetaObject(): objectID=" + objectID + ", classID=" + classID + ", domain=" + domain); // NOI18N
             }
 
             final MetaObject metaObject = connection.getMetaObject(session.getUser(), objectID, classID, domain);
             if (metaObject != null) {
                 if (log.isDebugEnabled()) {
                     log.debug(" MetaObject: " + metaObject + " MetaObject.getName(): " + metaObject.getName()
                                 + " MetaObject.getEditor(): " + metaObject.getEditor()
                                 + " MetaObject.getComplexEditor(): " + metaObject.getComplexEditor());
                 }
            }
 
            // set Classes in SubObjects as well
            metaObject.setAllClasses(classAndMethodCache.getClassHash());
 
             return metaObject;
         }
 
         @Override
         public MetaObject getMetaObject(final String objectId) throws ConnectionException {
             try {
                 final StringTokenizer tokenizer = new StringTokenizer(objectId, "@"); // NOI18N
                 final int objectID = Integer.valueOf(tokenizer.nextToken()).intValue();
                 final int classID = Integer.valueOf(tokenizer.nextToken()).intValue();
                 final String domain = tokenizer.nextToken();
 
                 return this.getMetaObject(objectID, classID, domain);
             } catch (final ConnectionException cexp) {
                 throw cexp;
             } catch (final Exception t) {
                 log.error("malformed object id: '" + objectId + "' (objectID@classID@domain expected)"); // NOI18N
                 throw new ConnectionException("malformed object id: '" + objectId
                             + "' (objectID@classID@domain expected)",
                     ConnectionException.ERROR,
                     t);                                                                                  // NOI18N
             }
         }
 
         @Override
         public MetaObject[] getMetaObject(final Query query) throws ConnectionException {
             final MetaObject[] obs = connection.getMetaObject(session.getUser(), query);
 
             for (int i = 0; i < obs.length; i++) {
                 if (obs[i] != null) {
                     obs[i].setAllClasses(classAndMethodCache.getClassHash());
                 }
             }
 
             return obs;
         }
 
         @Override
         public MetaObject[] getMetaObjectByQuery(final String query, final int sig) throws ConnectionException {
             if (classAndMethodCache == null) {
                 initClassAndMethodCache();
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("getMetaObjectByQuery"); // NOI18N
             }
 
             try {
                 final MetaObject[] obs = connection.getMetaObjectByQuery(session.getUser(), query);
 
                 for (int i = 0; i < obs.length; i++) {
                     if (obs[i] != null) {
                         obs[i].setAllClasses(classAndMethodCache.getClassHash());
                     }
                 }
 
                 return obs;
             } catch (Exception t) {
                 log.warn("Fehler in getMetaObjectByQuery", t);
             }
 
             return null;
         }
 
         @Override
         public MetaObject insertMetaObject(final MetaObject MetaObject, final String domain)
                 throws ConnectionException {
             return connection.insertMetaObject(session.getUser(), MetaObject, domain);
         }
 
         @Override
         public int insertMetaObject(final Query query, final String domain) throws ConnectionException {
             return connection.insertMetaObject(session.getUser(), query, domain);
         }
 
         @Override
         public int updateMetaObject(final MetaObject MetaObject, final String domain) throws ConnectionException {
             return connection.updateMetaObject(session.getUser(), MetaObject, domain);
         }
 
         @Override
         public int deleteMetaObject(final MetaObject MetaObject, final String domain) throws ConnectionException {
             return connection.deleteMetaObject(session.getUser(), MetaObject, domain);
         }
 
         @Override
         public MetaObject getInstance(final MetaClass c) throws ConnectionException {
             MetaObject MetaObject = null;
 
             MetaObject = connection.getInstance(session.getUser(), c);
             MetaObject.setAllClasses(classAndMethodCache.getClassHash());
 
             return MetaObject;
         }
 
         @Override
         public Collection customServerSearch(final CidsServerSearch serverSearch) throws ConnectionException {
             return connection.customServerSearch(session.getUser(), serverSearch);
         }
     }
 }
