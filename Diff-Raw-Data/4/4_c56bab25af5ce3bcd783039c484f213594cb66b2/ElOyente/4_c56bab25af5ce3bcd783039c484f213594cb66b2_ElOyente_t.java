 package com.technicolor.eloyente;
 
 import hudson.Extension;
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.model.Item;
 import hudson.model.Items;
 import hudson.model.Project;
 import hudson.triggers.Trigger;
 import hudson.triggers.TriggerDescriptor;
 import hudson.util.FormValidation;
 import hudson.util.ListBoxModel;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jenkins.model.Jenkins;
 import net.sf.json.JSONObject;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.packet.DiscoverItems;
 import org.jivesoftware.smackx.pubsub.LeafNode;
 import org.jivesoftware.smackx.pubsub.Node;
 import org.jivesoftware.smackx.pubsub.PubSubManager;
 import org.jivesoftware.smackx.pubsub.Subscription;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.Stapler;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
 * @author Juan Luis Pardo Gonz&aacute;lez
 * @author Isabel Fern&aacute;ndez D&iacute;az
  */
 public class ElOyente extends Trigger<Project> {
 
     private String server;
     private String user;
     private String password;
     private Project project;
     private final static Map<String, Connection> connections = new HashMap<String, Connection>();
 
     /**
      * Constructor for elOyente.
      *
      * It uses the Descriptor to set the fields required later on. The
      * Descriptor will bring the information set in the main configuration to
      * the particular job configuration.
      */
     @DataBoundConstructor
     public ElOyente(Project project, String server, String password, String user) {
 
         this.server = server;
         this.password = password;
         this.user = user;
     }
 
     /**
      * Method used for starting a job.
      *
      * This method is called when the Save or Apply button are pressed in a job
      * configuration in case this plugin is activated.
      *
      * It checks if there is all the information required for an XMPP connection
      * in the main configuration and creates the connection.
      *
      * It is also called when restarting a connection because of changes in the
      * main configuration.
      *
      *
      * @param project
      * @param newInstance
      */
     @Override
     public void start(Project project, boolean newInstance) {
 
         server = this.getDescriptor().server;
         user = this.getDescriptor().user;
         password = this.getDescriptor().password;
         this.project = project;
         ArrayList nodes = new ArrayList();
 
         System.out.println("JOB: " + project.getName());
         System.out.println("Con datos: " + !connections.isEmpty());
         System.out.println("Conexion existente: " + connections.containsKey(project.getName()));
         System.out.println("Reloading: " + getDescriptor().reloading);
 
         try {
             /* If there are old connections, there is a connection for this job and it's reloading then 
              we stop the connection, delete the old subscription and store the node name in order to recreate the connection and subcriptions later
              with the new parameters.
              */
 
             if (!connections.isEmpty() && connections.containsKey(project.getName()) && getDescriptor().reloading) {
                 nodes = deleteSubscriptions(connections.get(project.getName()), this.getDescriptor().olduser, project.getName());
                 connections.get(project.getName()).disconnect();
             }
 
             /* If parameters are not empty it will create a connection and add it to the Map that stores the connections for later uses.
              * It will also recreate the subscription based on the saved nodes and the new parameters.
              */
 
             if (server != null && !server.isEmpty() && user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
 //                if (connections.containsKey(project.getName()) && connections.get(project.getName()).getUser().split("@")[0].equals(user)) {
 //                    System.err.println("\n\n\nproject.getName() = " + project.getName());
 //                    System.err.println("\n\n\nuser = " + user);
 //                    System.err.println(connections.get(project.getName()).getUser().split("@")[0]);
 //                    connections.get(project.getName()).disconnect();
 //                }
                 ConnectionConfiguration config = new ConnectionConfiguration(server);
                 Connection con = new XMPPConnection(config);
                 con.connect();
                 if (con.isConnected()) {
                     try {
                         con.login(user, password, project.getName());
                         connections.put(project.getName(), con);
 
                         if (getDescriptor().reloading) {
                             recreateSubscription(connections.get(project.getName()), user, nodes, project.getName());
                         }
                         addListeners(connections.get(project.getName()), project);
                     } catch (XMPPException ex) {
                         System.err.println("Login error");
                         ex.printStackTrace(System.err);
                     }
                 }
             }
         } catch (XMPPException ex) {
             System.err.println("Couldn't establish the connection, or already connected");
             ex.printStackTrace(System.err);
         }
     }
 
     /**
      * Add listeners to the connections.
      *
      * This method is in charge of adding listeners to a node to which a job is
      * subscribed to. This way it will receive events that will trigger it.
      *
      * @param con
      * @param project
      * @throws XMPPException
      */
     public void addListeners(Connection con, Project project) throws XMPPException {
         PubSubManager mgr = new PubSubManager(con);
         Iterator it = mgr.getSubscriptions().iterator();
 
         while (it.hasNext()) {
             Subscription sub = (Subscription) it.next();
             String JID = sub.getJid();
             String JIDuser = JID.split("@")[0];
             String JIDresource = JID.split("@")[1].split("/")[1];
             System.out.println("this.job.getName() = " + project.getName());
             if (JIDuser.equals(user) && JIDresource.equals(project.getName())) {
                 LeafNode node = (LeafNode) mgr.getNode(sub.getNode());
                 ItemEventCoordinator itemEventCoordinator = new ItemEventCoordinator(sub.getNode(), this);
                 node.addItemEventListener(itemEventCoordinator);
             }
         }
     }
 
     /**
      *
      * Deletes the subscriptions that a job has when the parameters are changed
      * in the main configuration.
      *
      * This method deletes the subscriptions to a node that a user has and saves
      * the name of those nodes in order to recreate new subscriptions for the
      * new modified user.
      *
      * @param con
      * @param olduser
      * @param resource
      * @return nodes
      * @throws XMPPException
      */
     public ArrayList deleteSubscriptions(Connection con, String olduser, String resource) throws XMPPException {
         ArrayList nodes = new ArrayList();
         PubSubManager mgr = new PubSubManager(con);
         Iterator it = mgr.getSubscriptions().iterator();
 
         while (it.hasNext()) {
             Subscription sub = (Subscription) it.next();
             String JID = sub.getJid();
             String JIDuser = JID.split("@")[0];
             String JIDresource = JID.split("@")[1].split("/")[1];
 
             if (JIDuser.equals(olduser) && JIDresource.equals(resource)) {
                 Node node = mgr.getNode(sub.getNode());
                 node.unsubscribe(JID);
                 nodes.add(node.getId());
             }
         }
         return nodes;
     }
 
     /**
      * Recreates the subscriptions with the new parameters.
      *
      * This method creates the subscriptions again but now with the new
      * parameters introduced in the main config.
      *
      * @param con
      * @param newuser
      * @param nodes
      * @param resource
      * @throws XMPPException
      */
     public void recreateSubscription(Connection con, String newuser, ArrayList nodes, String resource) throws XMPPException {
         PubSubManager mgr = new PubSubManager(con);
         Iterator it = nodes.iterator();
         while (it.hasNext()) {
             Node node = mgr.getNode((String) it.next());
             String JID = con.getUser();
             node.subscribe(JID);
         }
     }
 
     /**
      * Run a job.
      *
      * This method will be called by the ItemEventCoordinator
      * when an XMPP event is received and a job must be triggered.
      * 
      */
     @Override
     public void run() {
         if (!project.getAllJobs().isEmpty()) {
             Iterator iterator = project.getAllJobs().iterator();
             while (iterator.hasNext()) {
                 ((Project) iterator.next()).scheduleBuild(null);
             }
         }
     }
 
     /**
      * Retrieves the descriptor for the plugin elOyente.
      *
      * Used by the plugin to set and work with the main configuration.
      *
      * @return DescriptorImpl
      */
     @Override
     public final DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     /**
      * Descriptor for {@link ElOyente}. Used as a singleton. The class is marked
      * as public so that it can be accessed from views.
      *
      * <p> See
      * <tt>src/main/resources/hudson/plugins/eloyente/elOyente/*.jelly</tt> for
      * the actual HTML fragment for the configuration screen.
      */
     @Extension // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DescriptorImpl extends TriggerDescriptor {
 
         /**
          * To persist global configuration information, simply store it in a
          * field and call save().
          *
          * <p> If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private String server, oldserver;
         private String user, olduser;
         private String password, oldpassword;
         private boolean reloading = false;
 
         /**
          * Brings the persisted configuration in the main configuration.
          *
          * Brings the persisted configuration in the main configuration.
          */
         public DescriptorImpl() {
             load();
             oldserver = this.getServer();
             olduser = this.getUser();
             oldpassword = this.getPassword();
         }
 
         /**
          * Returns true if this task is applicable to the given project.
          *
          * True to allow user to configure this post-promotion task for the given project.
          * 
          * @return boolean
          * @param item
          */
         @Override
         public boolean isApplicable(Item item) {
             return true;
         }
 
         
         /**
          * Human readable name of this kind of configurable object.
          *
          *
          * @return String
          */
         @Override
         public String getDisplayName() {
             return "XMPP triggered plugin";
         }
 
         
         /**
          * Invoked when the global configuration page is submitted..
          * 
          * Overriden in order to persist the main configuration, reporting and
          * and call the method reloadJobs, that we'll decide if it's necessary to
          * reload or not.
          * 
          * @param req
          * @param formData// public void listen(Node node, Trigger trigger) { //
          * ItemEventCoordinator itemEventCoordinator = new
          * ItemEventCoordinator(node.getId(), trigger); //
          * node.addItemEventListener(itemEventCoordinator); // }
          * @return boolean
          * @throws Descriptor.FormException
          */
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
             // To persist global configuration information,
             // set that to properties and call save().
             server = formData.getString("server");
             user = formData.getString("user");
             password = formData.getString("password");
 
             report();
             save();
             reloadJobs();
 
 
             oldserver = server;
             olduser = user;
             oldpassword = password;
 
             return super.configure(req, formData);
         }
 
         /**
          * This method reloads the jobs that are using ElOyente applying the new
          * main configuration.
          *
          * Checks if the parameters username, password and server of the main
          * configuration have changed, if so it calls the method start() of all
          * those jobs that are using ElOyente in order to connect to the server
          * with the new credentials and reset the subscriptions.
          */
         public void reloadJobs() {
             if (!server.equals(oldserver) || !user.equals(olduser) || !password.equals(oldpassword)) {
                 Iterator it2 = (Jenkins.getInstance().getItems()).iterator();
                 while (it2.hasNext()) {
                     AbstractProject job = (AbstractProject) it2.next();
                     Object instance = (ElOyente) job.getTriggers().get(this);
                     if (instance != null) {
                         System.out.println(job.getName() + ": Yo tengo el plugin");
                         reloading = true;
                         File directoryConfigXml = job.getConfigFile().getFile().getParentFile();
                         try {
                             Items.load(job.getParent(), directoryConfigXml);
 
                         } catch (IOException ex) {
                             Logger.getLogger(ElOyente.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     } else {
                         System.out.println(job.getName() + ": Yo no tengo el plugin");
                     }
                     reloading = false;
                 }
             }
         }
 
         /**
          * Used for logging to the log file.
          *
          * This method reports information related to XMPPadmin events like
          * "Available Nodes", connection information, etc. It creates a
          * connection to take the required data for reporting and it closes it
          * after. It is used in the main configuration every time the Save or
          * Apply buttons are pressed.
          *
          */
         public void report() {
             Logger logger = Logger.getLogger("com.technicolor.eloyente");
             //Logger loger =  Logger.getLogger(ElOyente.class.getName());
 
             if (server != null && !server.isEmpty() && user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
                 try {
                     //Connection
                     ConnectionConfiguration config = new ConnectionConfiguration(server);
                     PubSubManager mgr;
                     Connection con = new XMPPConnection(config);
                     con.connect();
                     logger.log(Level.INFO, "Connection established");
                     if (con.isConnected()) {
 
                         //Login
                         con.login(user, password, "Global");
                         logger.log(Level.INFO, "JID: {0}", con.getUser());
                         logger.log(Level.INFO, "{0} has been logged to openfire!", user);
 
                         //Log the availables nodes
                         mgr = new PubSubManager(con);
                         DiscoverItems items = mgr.discoverNodes(null);
                         Iterator<DiscoverItems.Item> iter = items.getItems();
 
                         logger.log(Level.INFO, "NODES: ---------------------------------");
                         while (iter.hasNext()) {
                             DiscoverItems.Item i = iter.next();
                             logger.log(Level.INFO, "Node: {0}", i.getNode());
                             System.out.println("Node: " + i.toXML());
                             System.out.println("NodeName: " + i.getNode());
                         }
                         logger.log(Level.INFO, "END NODES: -----------------------------");
 
                         //Disconnection
                         con.disconnect();
                     }
                 } catch (XMPPException ex) {
                     System.err.println(ex.getXMPPError().getMessage());
                 }
             }
         }
 
         /**
          * This method returns the URL of the XMPP server.
          *
          *
          * global.jelly calls this method to obtain the value of field server.
          *
          * @return server
          */
         public String getServer() {
             return server;
         }
 
         /**
          * This method returns the username for the XMPP connection.
          *
          *
          * global.jelly calls this method to obtain the value of field user.
          *
          * @return user
          */
         public String getUser() {
             return user;
         }
 //         private String getName() {
 //             return name;
 //        }
 
         /**
          * This method returns the password for the XMPP connection.
          *
          *
          * global.jelly calls this method to obtain the value of field password.
          *
          * @return password
          */
         public String getPassword() {
             return password;
         }
 
         /**
          * Used for knowing if a job is using the start() method because it is
          * loading or the configuration has changed and it is being reloaded.
          * 
          * @return reloading
          */
         public boolean getReloading() {
             return reloading;
         }
 
         /**
          * It stores the server address previous to the configuration change.
          * 
          * @return oldserver
          */
         public String getOldServer() {
             return oldserver;
         }
 
         /**
          *It stores the user previous to the configuration change.
          * 
          * @return olduser 
          */
         public String getOldUser() {
             return olduser;
         }
 
         /**
          *It stores the password previous to the configuration change.
          * 
          * @return oldpassword
          */
         public String getOldPassword() {
             return oldpassword;
         }
 
         /**
          * Performs on-the-fly validation of the form field 'server'.
          *
          * This method checks if the connection to the XMPP server specified is
          * available. It shows a notification describing the status of the
          * server connection.
          *
          * @param server
          */
         public FormValidation doCheckServer(@QueryParameter String server) {
 
             try {
                 ConnectionConfiguration config = new ConnectionConfiguration(server);
                 Connection con = new XMPPConnection(config);
 
                 if (server.isEmpty()) {
                     return FormValidation.warningWithMarkup("No server specified");
                 }
                 con.connect();
                 if (con.isConnected()) {
                     con.disconnect();
                     return FormValidation.okWithMarkup("Connection available");
                 }
                 return FormValidation.errorWithMarkup("Couldn't connect");
             } catch (XMPPException ex) {
                 return FormValidation.errorWithMarkup("Couldn't connect");
             }
         }
 
         /**
          * Performs on-the-fly validation of the form fields 'user' and
          * 'password'.
          *
          * This method checks if the user and password of the XMPP server
          * specified are correct and valid. It shows a notification describing
          * the status of the login.
          *
          * @param user
          * @param password
          */
         public FormValidation doCheckPassword(@QueryParameter String user, @QueryParameter String password, @QueryParameter String server) {
             ConnectionConfiguration config = new ConnectionConfiguration(server);
             Connection con = new XMPPConnection(config);
             if ((user.isEmpty() || password.isEmpty()) || server.isEmpty()) {
                 return FormValidation.warningWithMarkup("Not authenticated");
             }
             try {
                 con.connect();
                 con.login(user, password);
                 if (con.isAuthenticated()) {
                     con.disconnect();
                     return FormValidation.okWithMarkup("Authentication succed");
                 }
                 return FormValidation.warningWithMarkup("Not authenticated");
             } catch (XMPPException ex) {
                 return FormValidation.errorWithMarkup("Authentication failed");
             }
         }
 
         public ListBoxModel doFillNodesAvailableItems(@QueryParameter("name") String name) throws XMPPException, InterruptedException {
             ListBoxModel items = new ListBoxModel();
             ArrayList nodesSubsArray = new ArrayList();
             String nodeName;
             String pjName = name;
             Project pj;
             pj = (Project) Jenkins.getInstance().getItem(name);
             Object instance = (ElOyente) pj.getTriggers().get(this);
 
             System.out.println("pjName:" + pjName);
             if (instance != null) {
 
                 Connection con = connections.get(pjName);
                 PubSubManager mgr = new PubSubManager(con);
 
                 DiscoverItems it = mgr.discoverNodes(null);
                 Iterator<DiscoverItems.Item> iter = it.getItems();
 
                 HashMap<String, Subscription> prueba = new HashMap<String, Subscription>();
                 List<Subscription> listSubs = mgr.getSubscriptions();
 
                 for (int i = 0; i < listSubs.size(); i++) {
                     if (listSubs.get(i).getJid().equals(con.getUser())) {
                         System.out.println("User: " + listSubs.get(i).getJid() + " es igual a: " + con.getUser());
                         System.out.println("Suscrito a: " + listSubs.get(i).getNode());
                         nodeName = listSubs.get(i).getNode();
                         nodesSubsArray.add(nodeName);
                     }
                 }
 
                 if (con.isAuthenticated()) {
                     while (iter.hasNext()) {
                         DiscoverItems.Item i = iter.next();
                         if (!nodesSubsArray.contains(i.getNode())) {
                             items.add(i.getNode());
                             System.out.println("Node shown: " + i.getNode());
                         } else {
                             System.out.println("Node no shown: " + i.getNode());
                         }
                     }
                     //con.disconnect();
                 } else {
                     items.add("No esta conectado el amigo");
                     System.out.println("No Logeado");
                 }
             }
             return items;
 
         }
 
         
         public ListBoxModel doFillNodesSubItems() throws XMPPException, InterruptedException {
             ListBoxModel items = new ListBoxModel();
 ////            String nodeName;
 ////            String pj;
 ////
 ////            if (semaforo == true) {
 ////                wait();
 ////            } else {
 ////                semaforo = true;
 ////                pj = ElOyente.DescriptorImpl.getCurrentDescriptorByNameUrl();
 ////                System.out.println("pj " + pj);
 ////                Connection con = connections.get(pj);
 ////                //PubSubManager mgr=getPubSubManager();
 ////                PubSubManager mgr = new PubSubManager(con);
 ////
 ////                List<Subscription> listSubs = mgr.getSubscriptions();
 ////
 ////                for (int i = 0; i < listSubs.size(); i++) {
 ////                    if (listSubs.get(i).getJid().equals(con.getUser())) {
 ////                        nodeName = listSubs.get(i).getNode();
 ////                        items.add(nodeName);
 ////                    }
 ////                }
 ////                 //con.disconnect();
 ////                semaforo = false;
 ////            }
 
             return items;
         }
 
         /**
          * In charge of subscribing the job to a node.
          * 
          * It subscribes the job being configured to the node selected, and also creates the listeners
          * required for triggering jobs when XMPP events are received.
          *
          * @param nodesAvailable
          * @param name
          * @return
          */
         public FormValidation doSubscribe(@QueryParameter("nodesAvailable") String nodesAvailable) {
             String projectName = Stapler.getCurrentRequest().findAncestorObject(AbstractProject.class).getName();
             Connection con = connections.get(projectName);
             PubSubManager mgr = new PubSubManager(con);
             Trigger trigger = null;
 
             try {
                 Iterator it2 = (Jenkins.getInstance().getItems()).iterator();
                 while (it2.hasNext()) {
                     AbstractProject job = (AbstractProject) it2.next();
                     trigger = (ElOyente) job.getTriggers().get(this);
                 }
                 LeafNode node = (LeafNode) mgr.getNode(nodesAvailable);
                 ItemEventCoordinator itemEventCoordinator = new ItemEventCoordinator(nodesAvailable, trigger);
                 node.addItemEventListener(itemEventCoordinator);
                 String JID = con.getUser();
                 node.subscribe(JID);
                 return FormValidation.ok(con.getUser() + " Subscribed to " + nodesAvailable + " with resource " + projectName);
             } catch (Exception e) {
                 return FormValidation.error("Couldn't subscribe to " + nodesAvailable);
             }
         }
     }
 }
