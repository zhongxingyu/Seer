 /**
  * 
  */
 
 package org.itea.neotiq.xmpp;
 
 import org.itea.neotiq.devicemodel.Device;
 import org.itea.neotiq.devicemodel.DeviceDelegate;
 import org.itea.neotiq.devicemodel.DeviceEvent;
 import org.jivesoftware.smack.AccountManager;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.SmackConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.provider.ProviderManager;
 import org.jivesoftware.smackx.pubsub.AccessModel;
 import org.jivesoftware.smackx.pubsub.ConfigureForm;
 import org.jivesoftware.smackx.pubsub.FormType;
 import org.jivesoftware.smackx.pubsub.LeafNode;
 import org.jivesoftware.smackx.pubsub.NodeType;
 import org.jivesoftware.smackx.pubsub.PayloadItem;
 import org.jivesoftware.smackx.pubsub.PubSubManager;
 import org.jivesoftware.smackx.pubsub.PublishModel;
 import org.jivesoftware.smackx.pubsub.SimplePayload;
 
 import android.util.Log;
 
 /**
  * @author hbriand
  */
 public class XMPPComm implements DeviceDelegate, PacketListener, PacketFilter {
 
     private static final String TAG = "XMPPComm";
 
     protected static final String PREFIX = "VDFNeotiq_";
 
    protected static final String XMPP_HOST = "webmow.ensiie.fr";//"192.168.0.78";
 
//    protected static final String XMPP_HOST = "192.168.0.78";
 
     protected static final int XMPP_PORT = 5222;
 
    private static final String XMPP_SERVICE_NAME = "webmow.ensiie.fr";//"iMac.local";
 
//    private static final String XMPP_SERVICE_NAME = "iMac.local";
 
     private static final String XMPP_SERVICE_LOGIN = PREFIX + "Services";
 
     private static final String XMPP_SERVICE_PASSWORD = PREFIX + "Services";
 
     private static final String XMPP_PS_NS = "pubsub:event:"; // Namespace
                                                               // constant for
                                                               // Pub/Sub items
 
     private static final Object DEMO_NAME = "DemoBtn";
 
     private static XMPPComm mInstance;
 
     private static ConnectionConfiguration XMPP_CONFIG;
 
     private String XMPP_LOGIN;
 
     protected String XMPP_JID;
 
     protected String XMPP_NODE;
 
     private String XMPP_PASSWORD;
 
     private Device mDevice;
 
     private XMPPConnection mXMPPConn;
 
     private PubSubManager mPSMgr; // For device Pub/Sub
 
     private XMPPComm(Device d) {
         this.mDevice = d;
         XMPPConnection.DEBUG_ENABLED = true;
         SmackConfiguration.setPacketReplyTimeout(10000);
         if (this.mDevice == null) {
             this.XMPP_LOGIN = XMPP_SERVICE_LOGIN;
             this.XMPP_PASSWORD = XMPP_SERVICE_PASSWORD;
         } else {
             this.XMPP_LOGIN = PREFIX + d.getName().replace(" ", "");
             this.XMPP_NODE = PREFIX + d.getName().replace(" ", "");
             this.XMPP_PASSWORD = PREFIX + d.getName().replace(" ", "");
         }
         this.XMPP_JID = XMPP_LOGIN + "@" + XMPP_SERVICE_NAME;
         XMPP_CONFIG = new ConnectionConfiguration(XMPP_HOST, XMPP_PORT, XMPP_SERVICE_NAME);
         // XMPP_CONFIG.setSASLAuthenticationEnabled(false);
         try {
             this.initiateConnection();
         } catch (XMPPException e) {
             e.printStackTrace();
         }
     }
 
     public static XMPPComm getInstance() {
         if (mInstance == null) {
             ProviderManager.getInstance().addIQProvider("query", XmppRest.NAMESPACE,
                     new XmppRest.Provider());
             ProviderManager.getInstance().addIQProvider("result", XmppRest.NAMESPACE,
                     new XmppRest.Provider());
             mInstance = new XMPPComm(null);
         }
         return mInstance;
     }
 
     private void initiateConnection() throws XMPPException {
         // Connecting to the server...
         if (mXMPPConn == null || !mXMPPConn.isConnected()) {
             mXMPPConn = new XMPPConnection(XMPP_CONFIG);
             Log.d(TAG, "Connecting to the server...");
             mXMPPConn.connect();
         }
         // Logging in...
         Log.d(TAG, "Login : " + XMPP_LOGIN + " and password : " + XMPP_PASSWORD);
         try {
             mXMPPConn.login(XMPP_LOGIN, XMPP_PASSWORD);
         } catch (XMPPException e) {
             // Try to register the account...
             Log.d(TAG, "Loggin failed, trying to register the account");
             AccountManager am = new AccountManager(mXMPPConn);
             am.createAccount(XMPP_LOGIN, XMPP_PASSWORD);
             Log.d(TAG, "Account registered now logging in");
             mXMPPConn.login(XMPP_JID, XMPP_PASSWORD);
         }
         Log.d(TAG, "User loggedin : " + mXMPPConn.getUser());
 
         // Sending presence
         Log.d(TAG, "Sending presence");
         Presence presence = new Presence(Presence.Type.available);
         mXMPPConn.sendPacket(presence);
 
         if (this.mDevice != null) {
             // Creating & publishing
             mPSMgr = new PubSubManager(mXMPPConn, "pubsub." + XMPP_SERVICE_NAME);
             // Node configuration
             ConfigureForm f = new ConfigureForm(FormType.submit);
             f.setPersistentItems(true);
             f.setDeliverPayloads(true);
             f.setAccessModel(AccessModel.open);
             f.setPublishModel(PublishModel.open);
             f.setSubscribe(true);
             f.setNodeType(NodeType.leaf);
             Log.d(TAG, "Trying to create node " + XMPP_NODE);
             try {
                 mPSMgr.createNode(XMPP_NODE, f);
             } catch (XMPPException e) {
                 mPSMgr.deleteNode(XMPP_NODE);
                 mPSMgr.createNode(XMPP_NODE, f);
             }
             Log.d(TAG, "Node " + XMPP_NODE + " created.");
         } else {
             // Launch iq
             Log.d(TAG, "Launching iq");
             mXMPPConn.addPacketListener(this, this);
             //XmlDescription.sendToGlobalRepo();
         }
     }
 
     public void eventOccured(DeviceEvent de) {
         Log.d(TAG, "DeviceEvent occured : " + de.toString());
         if (mDevice != null) {
             try {
                 LeafNode node = (LeafNode)mPSMgr.getNode(XMPP_NODE);
                 node.send(getItemForEvent(de));
             } catch (XMPPException e) {
                 e.printStackTrace();
             }
             if (mDevice.getName().equals(DEMO_NAME)) {
                 // Do specific action for demo
             }
         }
     }
 
     private PayloadItem<SimplePayload> getItemForEvent(DeviceEvent de) {
         String deviceName = ((Device)de.getSource()).getName();
         PayloadItem<SimplePayload> retItem = new PayloadItem<SimplePayload>(deviceName
                 + System.currentTimeMillis(), new SimplePayload(deviceName,
                 XMPP_PS_NS + deviceName, de.asXML()));
         return retItem;
     }
 
     public void deviceCreated(Device d) {
         Log.d(TAG, "Device created : " + d);
         if (mDevice == null) {
             d.setDelegate(new XMPPComm(d));
             XmlDescription.sendToGlobalRepo();
             return;
         }
     }
 
     @Override
     protected void finalize() throws Throwable {
         mXMPPConn.disconnect();
         super.finalize();
     }
 
     public boolean accept(Packet packet) {
         return (packet instanceof XmppRest);
     }
 
     public void processPacket(Packet packet) {
         // On parse les infos du paquet : il faut caster packet vers XmppRest
         try {
             XmppRest respIq = RestDevices.treatResponse((XmppRest)packet);
             mXMPPConn.sendPacket(respIq);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 }
