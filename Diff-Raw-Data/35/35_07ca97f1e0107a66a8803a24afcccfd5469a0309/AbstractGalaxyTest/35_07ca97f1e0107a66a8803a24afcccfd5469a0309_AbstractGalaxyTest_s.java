 package org.mule.galaxy.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collection;
 
 import javax.activation.MimeTypeParseException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.jackrabbit.api.JackrabbitRepository;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactPolicyException;
 import org.mule.galaxy.ArtifactResult;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.Settings;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.impl.jcr.JcrUtil;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.security.UserManager;
 import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
 import org.springframework.transaction.support.TransactionSynchronizationManager;
 import org.springmodules.jcr.SessionFactory;
 import org.springmodules.jcr.SessionFactoryUtils;
 import org.springmodules.jcr.jackrabbit.support.UserTxSessionHolder;
 
 public class AbstractGalaxyTest extends AbstractDependencyInjectionSpringContextTests {
 
     protected static final Log log = LogFactory.getLog(AbstractGalaxyTest.class);
 
     protected JackrabbitRepository repository;
     protected Registry registry;
     protected Settings settings;
     protected SessionFactory sessionFactory;
     protected LifecycleManager lifecycleManager;
     protected UserManager userManager;
     protected Session session;
     protected PolicyManager policyManager;
 
     public AbstractGalaxyTest() {
         super();
         setPopulateProtectedVariables(true);
     }
 
     public URL getResource(String name) {
         return getClass().getResource(name);
     }
 
     public InputStream getResourceAsStream(String name) {
         return getClass().getResourceAsStream(name);
     }
 
     protected User getAdmin() {
         return userManager.authenticate("admin", "admin");
     }
     
     protected Artifact importHelloWsdl() 
         throws RegistryException, ArtifactPolicyException, IOException, MimeTypeParseException {
         InputStream helloWsdl = getResourceAsStream("/wsdl/hello.wsdl");
         
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
         
         ArtifactResult ar = registry.createArtifact(workspace, 
                                                     "application/xml", 
                                                     "hello_world.wsdl", 
                                                     "0.1", 
                                                     helloWsdl, 
                                                     getAdmin());
         return ar.getArtifact();
     }
 
     private void clearJcrRepository() {
         try {
             Session session = repository.login(new SimpleCredentials("username", "password".toCharArray()));
 
             Node node = session.getRootNode();
             //JcrUtil.dump(node.getNode("workspaces"));
             for (NodeIterator itr = node.getNodes(); itr.hasNext();) {
                 Node child = itr.nextNode();
                 if (!child.getName().equals("jcr:system")) {
                     child.remove();
                 }
             }
             session.save();
             session.logout();
         } catch (PathNotFoundException t) {
         } catch (Throwable t) {
             t.printStackTrace();
         }
     }
 
     @Override
     protected String[] getConfigLocations() {
         return new String[] {
             "/META-INF/applicationContext-core.xml"
         };
     }
 
    @Override
     protected void onSetUp() throws Exception {
         super.onSetUp();
         session = SessionFactoryUtils.getSession(sessionFactory, true);
         TransactionSynchronizationManager.bindResource(sessionFactory, new UserTxSessionHolder(session));
     }
 
     @Override
     protected void onTearDown() throws Exception {
         if (repository != null) {
             clearJcrRepository();
             setDirty();
         }
 
         TransactionSynchronizationManager.unbindResource(sessionFactory);
         SessionFactoryUtils.releaseSession(session, sessionFactory);
         super.onTearDown();
     }
 
 
 }
