 /*
  * Copyright (C) 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.waveprotocol.wave.examples.fedone.federation.xmpp;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.protobuf.RpcCallback;
 
 import org.dom4j.Attribute;
 import org.dom4j.Element;
 import org.xmpp.packet.IQ;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Implementation of XMPP Discovery.
  *
  *
  */
 @Singleton
 public class XmppDisco {
 
   private static final Logger logger =
       Logger.getLogger(XmppDisco.class.getCanonicalName());
   private WaveXmppComponent component;
 
   Map<String, RpcCallback<String>> pendingDiscoMap;
   Map<String, String> domainToJidMap;
   private Map<String, RpcCallback<IQ>> inProgressDiscoMap;
 
   static final String DISCO_INFO_CATEGORY = "collaboration";
   static final String DISCO_INFO_TYPE = "google-wave";
 
   /**
    * Constructor.
    */
   @Inject
   public XmppDisco() {
     pendingDiscoMap = new HashMap<String, RpcCallback<String>>();
     domainToJidMap = new HashMap<String, String>();
     inProgressDiscoMap = new HashMap<String, RpcCallback<IQ>>();
   }
 
   /**
    * Sets the parent component for this instance. Must be called before any
    * other methods will work.
    *
    * @param component the component
    */
   public void setComponent(WaveXmppComponent component) {
     this.component = component;
   }
 
   /**
    * Handles a disco info get from a foreign source. A remote server is trying
    * to ask us what we support. Send back a message identifying as a wave
    * component.
    *
    * @param iqGet the IQ packet.
    */
   void processDiscoInfoGet(IQ iqGet) {
     IQ response = new IQ();
     WaveXmppComponent.copyRequestPacketFields(iqGet, response);
     response.setType(IQ.Type.result);
     Element query =
         response
             .setChildElement("query", WaveXmppComponent.NAMESPACE_DISCO_INFO);
     query.addElement("identity")
         .addAttribute("category", DISCO_INFO_CATEGORY)
         .addAttribute("type", DISCO_INFO_TYPE)
         .addAttribute("name", component.getDescription());
     query.addElement("feature")
         .addAttribute("var", WaveXmppComponent.NAMESPACE_WAVE_SERVER);
     component
         .sendPacket(response, /* no retry */ false, /* no callback */ null);
   }
 
 
   /**
    * Handles a disco items get from a foreign xmpp agent. no useful responses.
    *
    * @param iq the IQ packet.
    */
   void processDiscoItemsGet(IQ iq) {
     IQ response = new IQ(IQ.Type.result);
     WaveXmppComponent.copyRequestPacketFields(iq, response);
     response.setChildElement("query", WaveXmppComponent.NAMESPACE_DISCO_ITEMS);
     component
         .sendPacket(response, false, /* no retry */ null /* no callback */);
 
   }
 
   /**
    * Look up the Wave Server JID of the remote host. If it's not one we know
    * already, start the disco discovery process.
    *
    * @param remoteServer the FQDN of the remote server
    * @param callback     a callback that will be invoked with either the target
    *                     JID or null if remote server doesn't support wave.
    */
   void discoverRemoteJid(String remoteServer,
                          RpcCallback<String> callback) {
     if (domainToJidMap.containsKey(remoteServer)) {
       callback.run(domainToJidMap.get(remoteServer));
     } else {
       sendDiscoItemsGet(remoteServer, callback);
     }
 
   }
 
   /**
    * Sends a disco info get to a remote server. Starts the discovery process for
    * a wave server.
    *
    * @param remoteServer the name of the remote server.
    * @param callback     the callback to trigger with the Wave JID on the remote
    *                     server
    */
   private void sendDiscoItemsGet(String remoteServer,
                                  RpcCallback<String> callback) {
     logger.info("Trying to discover remote server: " + remoteServer);
     IQ request = new IQ();
     request.setType(IQ.Type.get);
     request.setID(component.generateId());
     request.setChildElement("query", WaveXmppComponent.NAMESPACE_DISCO_ITEMS);
     request.setTo(remoteServer);
     request.setFrom(component.componentJID);
     pendingDiscoMap.put(request.getID(), callback);
 
     component.sendPacket(request, /* retry */  true, null);
   }
 
   /**
    * Handles a disco items response from a foreign XMPP agent. Then calls info
    * on each item, looking for a wave server.
    *
    * @param iq the IQ packet.
    */
   void processDiscoItemsResult(IQ iq) {
     RpcCallback<String> callback = pendingDiscoMap.remove(iq.getID());
     if (callback != null) {
       DiscoItemIterator discoIter =
           new DiscoItemIterator(iq, this.component, callback);
       discoIter.run(null);
     } else {
       logger.fine("got unexpected iq items result " + iq);
     }
   }
 
   /**
    * Handles a disco items error from a foreign XMPP agent. We switch to a
    * fallback mode where we just try wave.servername instead.
    *
    * @param iq the IQ packet.
    */
   void processDiscoItemsError(IQ iq) {
     RpcCallback<String> callback = pendingDiscoMap.remove(iq.getID());
     if (callback != null) {
       DiscoItemIterator discoIter =
           new DiscoItemIterator(iq.getFrom().toString(), this.component, callback);
       discoIter.run(null);
     } else {
       logger.fine("got unexpected iq items result " + iq);
     }
   }
 
   /**
    * Handles a disco info result for a remote JID, triggers the callback. When
    * we're walking through remote JIDs, we'll see these.
    *
    * @param iq the IQ packet.
    */
   void processDiscoInfoResult(IQ iq) {
     String id = iq.getID();
 
     if (inProgressDiscoMap.containsKey(id)) {
       RpcCallback<IQ> next = inProgressDiscoMap.remove(id);
       next.run(iq);
     } else {
       logger.fine("got unexpected iq info response " + iq);
     }
   }
 
 
   /**
    * This class takes a IQ disco#items result, and calls disco#info on each JID
    * in the result. When it finds one supporting wave, it triggers the
    * callback.
    */
   private class DiscoItemIterator implements RpcCallback<IQ> {
 
     private final List<String> candidateJids = new ArrayList<String>();
     private final String serverName;
     private final WaveXmppComponent waveComponent;
     private final RpcCallback<String> callback;
 
 
     /**
      * Constructor. Extracts the JIDs from the disco#items response.
      *
      * @param itemsIQ   the disco#items response
      * @param component the parent wave component
      * @param callback  the callback to invoke with the discovered JID or null
      */
     @SuppressWarnings("unchecked")
     DiscoItemIterator(IQ itemsIQ, WaveXmppComponent component,
                       RpcCallback<String> callback) {
       this.waveComponent = component;
       this.callback = callback;
       this.serverName = itemsIQ.getFrom().toString();
       // dom4j documentation says this should return List<Element>
       //noinspection unchecked
       List<Element> items = itemsIQ.getChildElement().elements("item");
       for (Element item : items) {
         Attribute jid = item.attribute("jid");
         if (jid != null) {
           candidateJids.add(jid.getValue());
         }
       }
     }
 
     /**
      * Constructor. The server doesn't support disco. Try to check wave.server
      * instead.
      *
      * @param forceCheck   the bare JID of the server.
      * @param component the parent wave component
      * @param callback  the callback to invoke with the discovered JID or null
      */
     @SuppressWarnings("unchecked")
     DiscoItemIterator(String forceCheck, WaveXmppComponent component,
                       RpcCallback<String> callback) {
       this.waveComponent = component;
       this.callback = callback;
      this.serverName = forceCheck;
 
      candidateJids.add("wave." + forceCheck);
     }
 
     /**
      * Process a result (if supplied) and check for a matching feature. If
      * found, trigger the callback JID. If not found, send a request for the
      * next JID. If none remain, trigger the callback with the value null.
      */
     @SuppressWarnings("unchecked")
     public void run(IQ infoResult) {
       if (infoResult != null) { // first time, no result.
         // check the result to see if it matches wave.
         //noinspection unchecked
         List<Element> features =
             infoResult.getChildElement().elements("feature");
         for (Element feature : features) {
           Attribute var = feature.attribute("var");
           if (var != null && var.getValue()
               .equals(WaveXmppComponent.NAMESPACE_WAVE_SERVER)) {
             String targetJID = infoResult.getFrom().toString();
             domainToJidMap.put(serverName, targetJID);
             logger.info("Discovered remote JID: " + targetJID + " for " + serverName);
             this.callback.run(targetJID);
             return;
           }
         }
 
       }
       // take the next candidateJID, run a disco info against it.
       if (candidateJids.isEmpty()) {
         // ran out of candidates
         logger.info("Couldn't find wave on " + serverName);
         this.callback.run(null);
         return;
       }
       String candidate = candidateJids.remove(0);
       IQ request = new IQ();
       request.setType(IQ.Type.get);
       request.setID(component.generateId());
       request.setChildElement("query", WaveXmppComponent.NAMESPACE_DISCO_INFO);
       request.setTo(candidate);
       request.setFrom(waveComponent.componentJID);
       inProgressDiscoMap.put(request.getID(), this);
       waveComponent.sendPacket(request, true, /* retry */null);
     }
   }
 }
