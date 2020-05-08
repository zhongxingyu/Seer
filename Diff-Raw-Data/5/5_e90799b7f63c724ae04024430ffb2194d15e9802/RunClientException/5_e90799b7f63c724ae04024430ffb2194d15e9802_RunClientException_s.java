 package com.reversemind.glia.test.pojo.client;
 
 import com.reversemind.glia.client.GliaClient;
 import com.reversemind.glia.proxy.ProxyFactory;
 import com.reversemind.glia.test.pojo.shared.ISimplePojo;
 import com.reversemind.glia.test.pojo.shared.Settings;
 import com.reversemind.glia.test.pojo.shared.SimpleException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  */
 public class RunClientException {
 
     private final static Logger LOG = LoggerFactory.getLogger(RunClientException.class);
 
     public static void main(String... args) throws Exception {
         LOG.info("Run EXCEPTION client");
 
         GliaClient client = new GliaClient(Settings.SERVER_HOST, Settings.SERVER_PORT);
         client.start();
 
         ISimplePojo simplePojoProxy = (ISimplePojo) ProxyFactory.getInstance().newProxyInstance(client, ISimplePojo.class);
 
         LOG.info("\n\n=======================");
 
         try {
             String simple = simplePojoProxy.createException("Simple");
         } catch (SimpleException ex) {
            LOG.info("I've got an SimpleException:", ex);
         }
     }
 }
