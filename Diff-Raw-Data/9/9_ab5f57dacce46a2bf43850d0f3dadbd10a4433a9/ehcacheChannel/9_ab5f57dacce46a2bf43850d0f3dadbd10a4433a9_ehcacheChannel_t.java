 package org.daum.library.ehcache;
 
 
 import org.kevoree.ContainerNode;
 import org.kevoree.ContainerRoot;
 import org.kevoree.Group;
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractChannelFragment;
 import org.kevoree.framework.ChannelFragmentSender;
 
 import org.kevoree.framework.KevoreePlatformHelper;
 import org.kevoree.framework.message.Message;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by jed
  * User: jedartois@gmail.com
  * Date: 11/05/12
  * Time: 14:46
  */
 
 @Library(name = "JavaSE", names = {"Android"})
 @ChannelTypeFragment
 @DictionaryType({
         @DictionaryAttribute(name = "cacheName", defaultValue = "kevoreeCluster", optional = false)
 })
 public class ehcacheChannel extends AbstractChannelFragment implements Runnable {
 
     private Thread handler=null;
     private  boolean alive=true;
     private Logger logger = LoggerFactory.getLogger(ehcacheChannel.class);
     @Start
     public void startChannel() {
         alive = true;
         handler = new Thread(this);
         handler.start();
     }
 
     @Stop
     public void stopChannel() {
         try
         {
             alive = false;
             handler.interrupt();
         } catch (Exception e) {
             //ignore
         }
 
 
 
     }
 
     @Update
     public void updateChannel() {
 
 
     }
 
     @Override
     public Object dispatch(Message message) {
         return null;
     }
 
     @Override
     public ChannelFragmentSender createSender(String s, String s1) {
         return null;
     }
 
     @Override
     public void run() {
 
 
 
 
     }
 
 
 
 
     public String getAddress(String remoteNodeName) {
         String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                 org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
         if (ip == null || ip.equals("")) {
             ip = "127.0.0.1";
         }
         return ip;
     }
 
 
     public String getAddressModel(String remoteNodeName) {
         String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                 org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
         if (ip == null || ip.equals("")) {
             ip = "127.0.0.1";
         }
         return ip;
     }
 
     public List<String> getAllNodes () {
         ContainerRoot model = this.getModelService().getLastModel();
        for (Group g : model.getGroups()) {
             List<String> peers = new ArrayList<String>(g.getSubNodes().size());
             for (ContainerNode node : g.getSubNodes()) {
                 peers.add(node.getName());
             }
             return peers;
         }
         return new ArrayList<String>();
     }
 
 
 
 }
