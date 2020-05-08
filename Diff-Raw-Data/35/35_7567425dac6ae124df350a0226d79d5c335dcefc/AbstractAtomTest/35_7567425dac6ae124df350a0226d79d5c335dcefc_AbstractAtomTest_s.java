 package org.mule.galaxy.test;
 
 
 import java.io.IOException;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Repository;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 import javax.servlet.http.HttpServletRequest;
 
 import junit.framework.TestCase;
 
 import org.apache.abdera.Abdera;
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.model.Base;
 import org.apache.abdera.protocol.server.ServiceContext;
 import org.apache.abdera.spring.SpringAbderaServlet;
 import org.apache.abdera.writer.Writer;
 import org.apache.abdera.writer.WriterFactory;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.FilterHolder;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.jetty.webapp.WebAppContext;
 import org.mule.galaxy.Registry;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springmodules.jcr.SessionFactory;
 import org.springmodules.jcr.support.OpenSessionInViewFilter;
 
 public class AbstractAtomTest extends TestCase {
     
     protected Registry registry;
     protected ServiceContext abderaServiceContext;
     protected Abdera abdera = new Abdera();
     protected Factory factory = abdera.getFactory();
     private Server server;
     private WebAppContext context;
    
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         initializeJetty();
     }
 
     @Override
     protected void tearDown() throws Exception {
         clearJcrRepository();
         server.stop();
         
         super.tearDown();
     } 
     
     private void clearJcrRepository() {
         try {
             WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context.getServletContext());
             Repository repository = (Repository) wac.getBean("repository");
             Session session = repository.login(new SimpleCredentials("username", "password".toCharArray()));
 
             Node node = session.getRootNode();
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
 
     protected void prettyPrint(Base doc) throws IOException {
         WriterFactory writerFactory = abdera.getWriterFactory();
         Writer writer = writerFactory.getWriter("prettyxml");
         writer.writeTo(doc, System.out);
         System.out.println();
     }
 
     private void initializeJetty() throws Exception {
         server = new Server(9002); 
         
         context = new WebAppContext();
         context.setContextPath("/");
         context.setWar(getWebappDirectory());
         server.setHandler(context);
         server.setStopAtShutdown(true);
         
         server.start();
     }
 
     protected String getWebappDirectory() {
         return "./src/main/webapp";
     }
     
 }
