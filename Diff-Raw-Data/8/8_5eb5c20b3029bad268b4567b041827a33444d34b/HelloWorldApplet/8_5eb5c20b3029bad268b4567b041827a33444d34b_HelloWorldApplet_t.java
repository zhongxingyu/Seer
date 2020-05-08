 package net.cruciblesoftware.example;
 
 import javax.swing.JApplet;
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
 
 public class HelloWorldApplet extends JApplet {
     static Logger logger = Logger.getLogger(HelloWorldApplet.class);
 
     /* init() is called by the browser as the entry point to the
      * program.
     */
     public void init() { 
         // Get some parameters and write them to the log.
        BasicConfigurator.resetConfiguration();
         BasicConfigurator.configure();
 
         int appWidth = Integer.parseInt(this.getParameter("width"));
         int appHeight = Integer.parseInt(this.getParameter("height")); 
        logger.info("Initializing applet to " + appWidth + "x" + appHeight);
        
         // add content jpanels here...
         // this.add(new JPanel(appWidth, appHeight));
     }
 
     public void start() {
         // called after init() and each time the page is visited
         logger.info("Applet is supposed to START now.");
     }
 
     public void stop() {
         // called after start() and each time the page is left
         logger.info("Applet is supposed to STOP now.");
     }
 
     public void destroy() {
         // called after stop() and just before all resources are freed
         // make sure to close out threads here
         logger.info("Applet is supposed be DESTROYED now.");
     }
 }
