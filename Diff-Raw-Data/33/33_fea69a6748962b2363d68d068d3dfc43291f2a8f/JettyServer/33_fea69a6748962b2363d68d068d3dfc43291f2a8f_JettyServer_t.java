 package ru.aemelin.awt.robot.remote;
 
 
 import com.sun.jersey.api.core.PackagesResourceConfig;
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 import ru.aemelin.awt.robot.remote.rest.RobotService;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dolf
  * Date: 15.02.13
  * Time: 18:35
  */
 
 public class JettyServer {
     private static Server server = new Server(6080);
 
     public static void main(String[] args) throws Exception {
         Context root = new Context(server, "/", Context.SESSIONS);
         root.addServlet(new ServletHolder(new ServletContainer(
                 new PackagesResourceConfig((RobotService.class.getPackage().getName())))),
                 "/robot/*"
         );
         server.start();
     }
 }
