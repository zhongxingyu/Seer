 package de.fhkoeln.gm.wba2.phase2.xmpp_client;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jivesoftware.smack.*;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smackx.ServiceDiscoveryManager;
 import org.jivesoftware.smackx.packet.DiscoverInfo;
 import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
 import org.jivesoftware.smackx.packet.DiscoverItems;
 import org.jivesoftware.smackx.pubsub.AccessModel;
 import org.jivesoftware.smackx.pubsub.ConfigureForm;
 import org.jivesoftware.smackx.pubsub.FormType;
 import org.jivesoftware.smackx.pubsub.Item;
 import org.jivesoftware.smackx.pubsub.LeafNode;
 import org.jivesoftware.smackx.pubsub.PayloadItem;
 import org.jivesoftware.smackx.pubsub.PubSubManager;
 import org.jivesoftware.smackx.pubsub.PublishModel;
 import org.jivesoftware.smackx.pubsub.SimplePayload;
 import org.jivesoftware.smackx.pubsub.Subscription;
 import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
 
 public class ConnectionHandler {
 
     private XMPPConnection xmpp_conn;
     private AccountManager ac;
     private PubSubManager pubsub_man;
     private String username;
     private String hostname;
     private ItemEventListener<Item> listener;
 
     public ConnectionHandler() {
     }
 
     /**
      * Establish a connection to the xmpp server
      * 
      * @param hostname server address
      * @param port server port
      * @return Successful or failed
      */
     public boolean connect(String hostname, int port) {
 
         if (xmpp_conn != null && xmpp_conn.isConnected()) {
             return true;
         }
 
         ConnectionConfiguration config = new ConnectionConfiguration(hostname,
                 port);
         xmpp_conn = new XMPPConnection(config);
         ac = new AccountManager(xmpp_conn);
 
         try {
             xmpp_conn.connect();
             pubsub_man = new PubSubManager(xmpp_conn, "pubsub."
                     + xmpp_conn.getHost());
         } catch (XMPPException e) {
             return false;
         }
         
         this.hostname = hostname;
         
         return true;
     }
 
     /**
      * Login to the xmpp server
      * 
      * @param username username of the user
      * @param password password of the user
      * @return Successful or failed
      */
     public boolean login(String username, String password) {
 
         try {
             xmpp_conn.login(username, password);
         } catch (XMPPException e) {
             return false;
         }
         
         this.username = username;
         
         return true;
     }
 
     /**
      * Register a user
      * 
      * @param username username of the user
      * @param password password of the user
      * @return Successful or failed
      */
     public boolean register(String username, String password) {
 
         try {
             ac.createAccount(username, password);
         } catch (XMPPException e) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Get all nodes known to the xmpp server
      * 
      * @return List with all NodeIds
      */
     public List<String> getAllNodes() {
 
         List<String> entries = new ArrayList<String>();
 
         try {
             DiscoverItems itms = pubsub_man.discoverNodes(null);
 
             Iterator<DiscoverItems.Item> it = itms.getItems();
 
             for (; it.hasNext();) {
                 entries.add(it.next().getNode());
             }
 
         } catch (XMPPException e) {
             e.printStackTrace();
         }
 
         return entries;
     }
 
     /**
      * Publish an item through a node
      * 
      * @param node_id id of the node
      * @param payload_data payload to publish
      * @return Successful or failed
      */
     public boolean publishWithPayload(String node_id, String payload_data) {
 
         LeafNode node = null;
         
         if(payload_data.length() == 0) {
             System.err.println("No payload given!");
             return false;
         }
         
         try {
             node = pubsub_man.getNode(node_id);
         } catch (XMPPException e) {
 
             // Node was not found
             System.err.println("Node was not found!");
 
             if (e.getXMPPError().getCode() == 404) {
 
                 // Node not found
                 try {
                     node = pubsub_man.createNode(node_id);
                     node.sendConfigurationForm(createForm(FormType.submit,
                             false, true, PublishModel.open, AccessModel.open));
 
                 } catch (XMPPException e1) {
                     // Node could not be created
                     System.err.println("Node could not be created!");
                     return false;
                 }
             }
         }
 
         if (node != null) {
 
             SimplePayload payload = new SimplePayload("palette", "",
                     payload_data);
             PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(
                     null, payload);
 
             try {
                 node.send(item);
             } catch (XMPPException e) {
                 // Item could not be send
                 e.printStackTrace();
                 System.err.println("Item could not be sent!");
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Subscribe to a leafnode/topic
      * 
      * @param node_id id of the leafnode
      * @return Successful or failed
      */
     public boolean subscribeToNode(String node_id) {
         LeafNode node = null;
 
         try {
             node = pubsub_man.getNode(node_id);
             node.subscribe(this.username + "@" + this.hostname);
             node.addItemEventListener(listener);
         } catch (XMPPException e) {
 
             // Node was not found
             System.err.println("Node was not found! I am gonna create one now.");
 
             if (e.getXMPPError().getCode() == 404) {
 
                 // Node not found
                 try {
                     // subscribes automatically
                     node = pubsub_man.createNode(node_id);
                     node.sendConfigurationForm(createForm(FormType.submit,
                             false, true, PublishModel.open, AccessModel.open));
                     node.addItemEventListener(listener);
                     // a user is automatically subscribed to a node he creates
                     return true;
                 } catch (XMPPException e1) {
                     // Node could not be created
                     System.err.println("Node could not be created!");
                     return false;
                 }
             }
             else {
                 System.err.println("Unknown errorcode: " + e.getXMPPError().getCode());
                 return false;
             }
         }
         
         return true;
     }
 
     /**
      * Unsubscribe from a leafnode
      * 
      * @param node_id id of a node
      * @return Successful or failed
      */
     public boolean unsubscribeToNode(String node_id) {
         LeafNode node = null;
 
         try {
             node = pubsub_man.getNode(node_id);
             node.unsubscribe(this.username + "@" + this.hostname);
             node.removeItemEventListener(listener);
 
             System.out.println("Unsubscribing succeded!");
 
         } catch (XMPPException e) {
             System.err.println("Unsubscribing failed!");
             return false;
         }
         
         return true;
     }
 
     /**
      * Delete a node
      * 
      * @param node_id id of the node
      * @return Successful or failed
      */
     public boolean deleteNode(String node_id) {
         
         try {
             pubsub_man.deleteNode(node_id);
         } catch (XMPPException e) {
             System.err.println("Couldn't delete the node with the ID \""
                     + node_id + "\"");
             return false;
         }
 
         return true;
     }
 
     /**
      * Return a list with ids of all the nodes the user is subscribed to
      * 
      * @return list of node ids
      */
     public List<String> getSubscribedNodes() {
 
         List<String> entries = new ArrayList<String>();
 
         try {
             List<Subscription> subs = pubsub_man.getSubscriptions();
 
             for (Subscription curr : subs) {
                 entries.add(curr.getNode());
             }
 
         } catch (XMPPException e) {
             e.printStackTrace();
         }
 
         return entries;
 
     }
 
     /**
      * 
      * @param node_id id of the node
      * @return Node information as a string
      */
     public String getNodeInformation(String node_id) {
 
         String info = "";
 
         ServiceDiscoveryManager discoManager = ServiceDiscoveryManager
                 .getInstanceFor(xmpp_conn);
 
         DiscoverInfo discoInfo;
         try {
             discoInfo = discoManager.discoverInfo(
                     "pubsub." + xmpp_conn.getHost(), node_id);
 
             Iterator<Identity> it = discoInfo.getIdentities();
 
             while (it.hasNext()) {
                 DiscoverInfo.Identity identity = (DiscoverInfo.Identity) it
                         .next();
                 info += "Name:\t" + identity.getName() + "\n" + "Type:\t"
                         + identity.getType() + "\n" + "Category:\t"
                         + identity.getCategory() + "\n";
 
                 LeafNode node = pubsub_man.getNode(node_id);
 
                 List<Subscription> subs = node.getSubscriptions();
 
                 if (subs.size() > 0) {
                     info += "Subscriptions:\n";
 
                     for (Subscription curr : subs) {
                         info += "    " + curr.toXML() + "\n";
                     }
 
                     info += "\n";
                 }
             }
 
         } catch (XMPPException e) {
             e.printStackTrace();
         }
 
         return info;
 
     }
 
     /**
      * Attach a listener to all nodes a user is subscribed to
      * 
      */
     private void attachListenerToSubscribedNodes() {
 
         List<Subscription> subs;
         try {
             subs = pubsub_man.getSubscriptions();
         } catch (XMPPException e1) {
             System.err.println("Could not get Subscriptions!");
             e1.printStackTrace();
             return;
         }
         
         for (Subscription curr : subs) {
             try {
                 pubsub_man.getNode(curr.getNode()).addItemEventListener(
                         listener);
             } catch (XMPPException e) {
                 System.err
                         .println("Couldn't get Node for attaching Listener");
             }
         }
     }
 
     /**
      * Add a listener which will be used to output all incoming Messages
      * 
      * @param listener listener given
      */
     public void addItemListener(ItemEventListener<Item> listener) {
         this.listener = listener;
         attachListenerToSubscribedNodes();
     }
 
     /**
      * Return the username of the user currently logged in
      * 
      * @return username
      */
     public String getUsername() {
        return this.username;
     }
     
     /**
      * Return the hostname
      * 
      * @return hostname
      */
     public String getHost() {
        return this.hostname;
     }
     
     /**
      * Disconnect from the xmpp server
      * 
      */
     public void disconnect() {
         Presence offline = new Presence(Presence.Type.unavailable, "", 1, Presence.Mode.away);
         xmpp_conn.sendPacket(offline);
         xmpp_conn.disconnect();
     }
 
     /**
      * Create a ConfigureForm
      * 
      * @return configureform
      */
     private ConfigureForm createForm(FormType type, boolean pers,
             boolean payload, PublishModel pm, AccessModel am) {
         ConfigureForm form = new ConfigureForm(type);
         form.setPersistentItems(pers);
         form.setDeliverPayloads(payload);
         form.setPublishModel(pm);
         form.setAccessModel(am);
 
         return form;
     }
 
     public void finalize() {
         disconnect();
     }
 }
