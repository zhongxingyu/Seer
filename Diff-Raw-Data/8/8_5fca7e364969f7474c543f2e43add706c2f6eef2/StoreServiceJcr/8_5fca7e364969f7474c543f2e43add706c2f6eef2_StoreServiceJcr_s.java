 package com.digt.jcr.spi;
 
 import com.digt.jcr.StoreItemJcr;
 import com.digt.model.StoreItem;
 import com.digt.spi.StoreService;
 import java.util.concurrent.Future;
 import java.util.logging.Logger;
 
 import javax.jcr.Node;
 import javax.jcr.Repository;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 
 import org.apache.jackrabbit.commons.JcrUtils;
 import org.apache.shindig.auth.SecurityToken;
 import org.apache.shindig.common.util.ImmediateFuture;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 import javax.jcr.Credentials;
 import javax.jcr.LoginException;
 import javax.jcr.NodeIterator;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Workspace;
 import javax.jcr.nodetype.NodeType;
 import javax.jcr.nodetype.NodeTypeManager;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryResult;
 import javax.jcr.security.AccessControlEntry;
 import javax.jcr.security.AccessControlList;
 import javax.jcr.security.AccessControlManager;
 import javax.jcr.security.Privilege;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.jackrabbit.commons.cnd.CndImporter;
 import org.apache.jackrabbit.core.security.principal.PrincipalImpl;
 import org.apache.jackrabbit.value.BinaryValue;
 import org.apache.shindig.protocol.ProtocolException;
 import org.apache.shindig.protocol.RestfulCollection;
 import org.apache.shindig.social.opensocial.spi.CollectionOptions;
 import org.apache.shindig.social.opensocial.spi.UserId;
 
 public class StoreServiceJcr implements StoreService {
 	
 	private Provider<Repository> repository;
 
 	private static final Logger LOG = Logger.getLogger(
 			StoreServiceJcr.class.getName());
 
     @Inject
     public StoreServiceJcr(Provider<Repository> repository) {
         Session sess = null;
         try {
             this.repository = repository;
             Repository repo = repository.get();
             sess = repo.login(getCredentials("admin"));
             
             Workspace ws = sess.getWorkspace();
             NodeTypeManager manager = ws.getNodeTypeManager();
             NodeType nt = null;
             try {
                 nt = manager.getNodeType("dt:docsign");
             } catch (Exception e) {}
             if (nt == null) {
                 NodeType[] nodeTypes = CndImporter.registerNodeTypes(
                         new InputStreamReader(this.getClass()
                             .getResourceAsStream("/nodetype.cnd")), sess);
                 manager.registerNodeTypes(nodeTypes, true);
             }
             List<String> wsExisting = Arrays.asList(ws.getAccessibleWorkspaceNames());
             String[] wsBuiltin = new String[]{"user", "public", "org"};
             for (String wsName : wsBuiltin) {
                 if (!wsExisting.contains(wsName))
                     ws.createWorkspace(wsName);
             }
             sess.save();
         } catch (Exception ex) {
             LOG.log(Level.SEVERE, null, ex);
         } finally {
             close(sess);
         }
     }   
     
     @Override
 	public Future<String> getUserStorePath(UserId userId, SecurityToken token, boolean create) {
 		
 		String uid = userId.getUserId(token);
 		Session sess = null;
         String userHomePath = null;
 		try {
             sess = getSession(uid, "user");
             try {
                 userHomePath = JcrUtils.getNodeIfExists(
                     "/" + uid.substring(0, 2) + "/" + uid, sess).getPath();
             } catch (Exception e) {
                 LOG.log(Level.FINE, null, e);
             }
             
             if (create) {
                 if (userHomePath == null) {
                     LOG.log(Level.FINE, "User home for {0} doesn''t exist. Creating.", uid);
                     userHomePath = createUserHome(uid);
                 }
                 // Rebind with user credentials
                 Node userHome = JcrUtils.getOrCreateByPath(
                         userHomePath, NodeType.NT_FOLDER, sess);
                 buildUserHome(userHome, sess);
                 sess.save();
             }
             
 		} catch(Exception e) {
 			LOG.log(Level.SEVERE, null, e);
 		} finally {
             close(sess);
         }
         
         if (userHomePath == null) {
             throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                     "User home doesn't exist and cannot be created for " + uid);
         }
 
         return ImmediateFuture.newInstance(userHomePath);
 	}
     
     @Override
     public Future<Void> createItem(UserId userId, String workspace, String path, 
                     InputStream is, String mimeType, SecurityToken token) {
         
         Session sess = null;
         try {
             sess = getSession(userId.getUserId(token), workspace);
 
             String nodeType = (is == null)?NodeType.NT_FOLDER:NodeType.NT_FILE;
             Node n = JcrUtils.getOrCreateByPath(
                     path, NodeType.NT_FOLDER, nodeType, sess, false);
             
             if (!n.isNew()) {
                 throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Node already exists: "
                         + path);
             }
             // If file
             if (is != null) {
                 Node data = n.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);
                 data.setProperty(Property.JCR_DATA, new BinaryValue(is));
                 data.setProperty(Property.JCR_MIMETYPE, mimeType);
                 Calendar lastModified = Calendar.getInstance();
                data.setProperty(path, lastModified);
             }
             
             sess.save();
         } catch (ProtocolException e) { 
 			LOG.log(Level.INFO, null, e);
             throw e;
         } catch (Exception e) { 
 			LOG.log(Level.SEVERE, null, e);
             throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 		} finally {
             close(sess);
         }
       
         return null;
     }
     
     @Override
     public Future<RestfulCollection<StoreItem>> getItems(
             UserId userId, String workspace, String path, CollectionOptions opts, SecurityToken token) {
         
         Session sess = null;
         try {
             if (path.isEmpty() || path.equals("/.")) {
                 path = getRootPath(userId, workspace, token) + path;
             }
             
             sess = getSession(userId.getUserId(token), workspace);
             
             Node node = JcrUtils.getNodeIfExists(path, sess);
             if (node == null) 
                 throw new ProtocolException(HttpServletResponse.SC_NOT_FOUND, "Not found " + path);
             
             ArrayList<StoreItem> res = new ArrayList<StoreItem>();
             if (opts == null) {
                 opts = new CollectionOptions();
                 opts.setMax(20);
             }
             
             Long total = 0L;
             if (node.getPrimaryNodeType().isNodeType(NodeType.NT_FOLDER)) {
                 // Return node childs
                 if (!path.endsWith("/.")) {
                     Query q = sess.getWorkspace().getQueryManager().createQuery(
                             "SELECT * FROM [nt:base] as item WHERE ISCHILDNODE('"
                             + node.getPath() +"') AND (item.[jcr:primaryType]='nt:folder'"
                             + " OR item.[jcr:primaryType]='nt:file')"
                             + " ORDER BY item.[jcr:primaryType] DESC, NAME(item)", 
                             Query.JCR_SQL2);
 
                     QueryResult qr = q.execute();
 
                     NodeIterator ni = qr.getNodes();
                     total = ni.getSize();
                     ni.skip(opts.getFirst());
                     while(ni.hasNext() && ni.getPosition() - opts.getFirst() <= opts.getMax()) {
                         Node n = (Node) ni.next();
                         //LogUtils.LogNodeProps(LOG, Level.FINE, n);
                         StoreItemJcr item = new StoreItemJcr(n);
                         res.add(item);
                     }
                 // Return folder node itself
                 } else {
                     StoreItemJcr item = new StoreItemJcr(node);
                     res.add(item);
                     total = 1L;
                 }
             } else if (node.getPrimaryNodeType().isNodeType(NodeType.NT_FILE)) {
                 StoreItemJcr item = new StoreItemJcr(node);
                 res.add(item);
                 total = 1L;
             }
             
             return ImmediateFuture.newInstance(
                     new RestfulCollection<StoreItem>(res, opts.getFirst(), total.intValue()));
         } catch (ProtocolException e) { 
 			LOG.log(Level.INFO, null, e);
             throw e;
         } catch (Exception e) { 
 			LOG.log(Level.SEVERE, null, e);
             throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 		} finally {
             close(sess);
         }
     }
     
     @Override
     public Future<Void> getItemData(UserId userId, String workspace, String path, 
                                     OutputStream os, SecurityToken token) {
         Session sess = null;
         InputStream is = null;
         try {
             sess = getSession(userId.getUserId(token), workspace);
             
             Node node = JcrUtils.getNodeIfExists(path, sess);
             if (node == null) 
                 throw new ProtocolException(HttpServletResponse.SC_NOT_FOUND, "Not found " + path);
             
             node = node.getNode(Property.JCR_CONTENT);
             if (node == null) 
                 throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Not a file " + path);
             
             is = node
                     .getProperty(Property.JCR_DATA)
                     .getBinary()
                     .getStream();
             
             byte[] buf = new byte[65535];
             int count;
             while ((count = is.read(buf)) > 0) {
                 os.write(buf, 0, count);
             }
         } catch (ProtocolException e) { 
 			LOG.log(Level.INFO, null, e);
             throw e;
         } catch (Exception e) { 
 			LOG.log(Level.SEVERE, null, e);
             throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 		} finally {
             close(sess);
             try {
                 if (is != null) is.close();
             } catch (IOException e) {}
         }
         
         return null;
     }
     
     @Override
     public Future<Void> moveItem(UserId userId, String workspace, 
             String oldPath, String newPath, SecurityToken token) {
         
         Session sess = null;
         try {
             sess = getSession(userId.getUserId(token), workspace);
             sess.move(oldPath, newPath);
             sess.save();
         } catch (ProtocolException e) { 
 			LOG.log(Level.INFO, null, e);
             throw e;
         } catch (Exception e) { 
 			LOG.log(Level.SEVERE, null, e);
             throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 		} finally {
             close(sess);
         }
 
         return null;
     }
     
     @Override
     public Future<Void> deleteItem(UserId userId, String workspace, String path, SecurityToken token) {
         
         Session sess = null;
         try {
             sess = getSession(userId.getUserId(token), workspace);
             sess.removeItem(path);
             sess.save();
         } catch (ProtocolException e) { 
 			LOG.log(Level.INFO, null, e);
             throw e;
         } catch (Exception e) { 
 			LOG.log(Level.SEVERE, null, e);
             throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 		} finally {
             close(sess);
         }
         
         return null;
     }
     
     @Override
     public Future<Void> setAcl(UserId userId, Set<String> ids, Set<String> newAcl,
             String workspace, String path, SecurityToken token) {
 
         Session sess = null;
         try {
             sess = getSession(userId.getUserId(token), workspace);
             Node node = JcrUtils.getNodeIfExists(path, sess);
             if (node == null) {
                 throw new ProtocolException(HttpServletResponse.SC_NOT_FOUND, "Path not found: " + path);
             }
             AccessControlManager aMgr = sess.getAccessControlManager();
             AccessControlList acl;
             try {
                 // get first applicable policy (for nodes w/o a policy)
                 acl = (AccessControlList) aMgr.getApplicablePolicies(path).nextAccessControlPolicy();
             } catch (NoSuchElementException e) {
                 // else node already has a policy, get that one
                 acl = (AccessControlList) aMgr.getPolicies(path)[0];
             }
             // remove all existing entries
             for (AccessControlEntry e : acl.getAccessControlEntries()) {
                 acl.removeAccessControlEntry(e);
             }
             
             Privilege[] newPriv = new Privilege[newAcl.size()];
             int i = 0;
             for (String a: newAcl) {
                 newPriv[i] = aMgr.privilegeFromName(a);
                 i ++;
             }
             
             for (String prId : ids) {
                 acl.addAccessControlEntry(
                         new PrincipalImpl(prId), newPriv);
             }
 
             // the policy must be re-set
             aMgr.setPolicy(path, acl);
             sess.save();
         } catch (ProtocolException e) { 
 			LOG.log(Level.INFO, null, e);
             throw e;
         } catch (Exception e) { 
 			LOG.log(Level.SEVERE, null, e);
             throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 		} finally {
             close(sess);
         }
 
         return null;
     }
     
     private String createUserHome(String uid) {
 	    Session sess = null;
 		try {
 			sess = getSession("admin", "user");
 			Node userHome = JcrUtils.getOrCreateByPath("/" + uid.substring(0, 2) + "/" + uid, NodeType.NT_FOLDER, sess);
             AccessControlManager aMgr = sess.getAccessControlManager();
             
             // create a privilege set with jcr:all
             Privilege[] privileges = new Privilege[] { aMgr.privilegeFromName(Privilege.JCR_ALL) };
             AccessControlList acl;
             try {
                 // get first applicable policy (for nodes w/o a policy)
                 acl = (AccessControlList) aMgr.getApplicablePolicies(userHome.getPath()).nextAccessControlPolicy();
             } catch (NoSuchElementException e) {
                 // else node already has a policy, get that one
                 acl = (AccessControlList) aMgr.getPolicies(userHome.getPath())[0];
             }
             // remove all existing entries
             for (AccessControlEntry e : acl.getAccessControlEntries()) {
                 acl.removeAccessControlEntry(e);
             }
             
             // add a new one for a principal
             acl.addAccessControlEntry(new PrincipalImpl(uid), privileges);
 
             // the policy must be re-set
             aMgr.setPolicy(userHome.getPath(), acl);
 
             // and the session must be saved for the changes to be applied
 			sess.save();
             
             return userHome.getPath();
 		} catch(Exception e) {
 			LOG.log(Level.SEVERE, null, e);
 		} finally {
             close(sess);
         }
         
         return null;
     }
     
     private void buildUserHome(Node userHome, Session sess) throws RepositoryException {
             String builtinFolders[] = {
                 "Inbox", "Certificates", "Settings", "Documents", "Trash",
                 "Certificate Requests", "Containers", "Certificate Trusted Lists"
             };
             for (String f : builtinFolders) {
                 if (!userHome.hasNode(f)) { 
                      userHome.addNode(f, NodeType.NT_FOLDER);
                 }
             }
             
             /* Node welcome = JcrUtils.getOrCreateByPath(userHome.getPath()+"/Inbox/welcome", NodeType.NT_FILE, sess);
             
             if (welcome.isNew()) {
                 Node content = welcome.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);
                 content.setProperty(Property.JCR_DATA, "Welcome");
             } */
     }
     
     private Session getSession(String userId, String workspace) throws LoginException, RepositoryException {
 		Repository repo = repository.get();
 		if (repo == null) return null;
 		return repo.login(getCredentials(userId), workspace);
     }
     
     private void close(Session sess) {
         if (sess != null && sess.isLive()) sess.logout();
     }
     
     private Credentials getCredentials(String uid) {
         
 		SimpleCredentials creds = new SimpleCredentials(uid, new char[0]);
 		creds.setAttribute("trust_credentials_attribute", "");
         
         return creds;
     }
     
     private String getRootPath(UserId userId, String workspace, SecurityToken token) 
             throws InterruptedException, ExecutionException {
         
         if (workspace.equals("user")) {
                 return getUserStorePath(userId, token, false).get();
         } else if (workspace.equals("public")) {
                 return "";
         } else if (workspace.equals("org")) {
                 // TODO: need to calculate organization root here
                 return "";
         }
     
         throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Invalid workspace '" + workspace + "'");
     }
 }
