 /*
  * Copyright (C) 2011 - Jingle Nodes - Yuilop - Neppo
  *
  *   This file is part of Switji (http://jinglenodes.org)
  *
  *   Switji is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   Switji is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with MjSip; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *   Author(s):
  *   Benhur Langoni (bhlangonijr@gmail.com)
  *   Thiago Camargo (barata7@gmail.com)
  */
 
 package org.jinglenodes.detour;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Element;
 import org.jinglenodes.jingle.Reason;
 import org.jinglenodes.prepare.CallPreparation;
 import org.jinglenodes.prepare.PrepareStatesManager;
 import org.jinglenodes.session.CallSession;
 import org.jinglenodes.session.CallSessionMapper;
 import org.jinglenodes.sip.SipToJingleBind;
 import org.xmpp.component.ExternalComponent;
 import org.xmpp.component.IqRequest;
 import org.xmpp.component.ResultReceiver;
 import org.xmpp.component.ServiceException;
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.JID;
 import org.xmpp.packet.PacketError;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.message.JIDFactory;
 import org.zoolu.sip.message.Message;
 import org.zoolu.sip.message.SipChannel;
 
 /**
  * Created by IntelliJ IDEA.
  * User: thiago
  * Date: 3/23/12
  * Time: 2:47 PM
  */
 public class DetourPreparation extends CallPreparation implements ResultReceiver {
     final Logger log = Logger.getLogger(DetourPreparation.class);
 
     private SipToJingleBind sipToJingleBind;
     private PrepareStatesManager prepareStatesManager;
     private DetourServiceProcessor detourServiceProcessor;
     private String jinglePhoneType;
     private ExternalComponent externalComponent;
     private CallSessionMapper callSessions;
 
     @Override
     public boolean prepareInitiate(final JingleIQ iq, final CallSession session) {
         JID responder = JIDFactory.getInstance().getJID(iq.getJingle().getResponder());
 
         if(!iq.getFrom().toFullJID().equals(iq.getJingle().getInitiator())){
             prepareStatesManager.cancelCall(iq,session,new Reason(Reason.Type.security_error));
             return false;
         }
 
         try {
             detourServiceProcessor.queryService(iq, null, responder.getNode(), this);
         } catch (ServiceException e) {
             log.error("Failed Querying Account Service.", e);
         }
         return false;
     }
 
     @Override
     public boolean proceedInitiate(JingleIQ iq, final CallSession session) {
         return true;
     }
 
     @Override
     public void receivedResult(IqRequest iqRequest) {
         if (iqRequest.getOriginalPacket() instanceof JingleIQ) {
             final String destination = getJingleDestination(iqRequest.getResult());
             if (destination != null) {
                 detourCall(iqRequest, destination);
                 callSessions.removeSession(callSessions.getSession((JingleIQ) iqRequest.getOriginalPacket()));
             } else {
                 prepareStatesManager.prepareCall((JingleIQ) iqRequest.getOriginalPacket(), null);
             }
         }
     }
 
     private void detourCall(IqRequest iqRequest, final String destinationNode) {
         if (iqRequest.getOriginalPacket() instanceof JingleIQ) {
             final JingleIQ jiq = (JingleIQ) iqRequest.getOriginalPacket();
             IQ error = IQ.createResultIQ(jiq);
             error.setType(IQ.Type.error);
             error.setError(PacketError.Condition.redirect);
             final JID destinationJID = new JID(destinationNode, externalComponent.getServerDomain(), null);
             final Element child = error.getError().getElement();
             if (child != null) {
                 final Element redirect = child.element("redirect").addCDATA("xmpp:" + destinationJID.toBareJID());
                 if (redirect != null) {
                     jiq.setTo(jiq.getJingle().getInitiator());
                     log.warn("Detour Call: " + error.toXML() + " to: " + destinationNode);
                     externalComponent.send(error);
                 }
             }
         }
     }
 
     public String getJingleDestination(final IQ res) {
         boolean isJingle = false;
         String destination = null;
         log.debug("Getting Jingle Destination of: " + res.toString());
         for (Object o : res.getChildElement().elements()) {
             Element e = (Element) o;
             if (e.attributeValue("type").equals(jinglePhoneType)) {
                 isJingle = true;
             }
            destination = e.attributeValue("number");
         }
         return isJingle ? destination : null;
     }
 
     @Override
     public void receivedError(IqRequest iqRequest) {
         log.error("Error Requesting Account");
     }
 
     @Override
     public void timeoutRequest(IqRequest iqRequest) {
         log.error("Timeout Requesting Account");
     }
 
 
     public SipToJingleBind getSipToJingleBind() {
         return sipToJingleBind;
     }
 
     public void setSipToJingleBind(SipToJingleBind sipToJingleBind) {
         this.sipToJingleBind = sipToJingleBind;
     }
 
     public PrepareStatesManager getPrepareStatesManager() {
         return prepareStatesManager;
     }
 
     public void setPrepareStatesManager(PrepareStatesManager prepareStatesManager) {
         this.prepareStatesManager = prepareStatesManager;
     }
 
     public DetourServiceProcessor getDetourServiceProcessor() {
         return detourServiceProcessor;
     }
 
     public void setDetourServiceProcessor(DetourServiceProcessor detourServiceProcessor) {
         this.detourServiceProcessor = detourServiceProcessor;
     }
 
     @Override
     public boolean proceedTerminate(JingleIQ iq, CallSession session) {
         return true;
     }
 
     @Override
     public boolean proceedAccept(JingleIQ iq, CallSession session) {
         return true;
     }
 
     @Override
     public boolean prepareInitiate(Message msg, CallSession session, final SipChannel sipChannel) {
         return true;
     }
 
     @Override
     public boolean proceedSIPInitiate(JingleIQ iq, CallSession session, SipChannel channel) {
         return true;
     }
 
     @Override
     public JingleIQ proceedSIPEarlyMedia(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     @Override
     public boolean proceedSIPTerminate(JingleIQ iq, CallSession session, SipChannel channel) {
         return true;
     }
 
     @Override
     public JingleIQ proceedSIPAccept(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     public String getJinglePhoneType() {
         return jinglePhoneType;
     }
 
     public void setJinglePhoneType(String jinglePhoneType) {
         this.jinglePhoneType = jinglePhoneType;
     }
 
     public ExternalComponent getExternalComponent() {
         return externalComponent;
     }
 
     public void setExternalComponent(ExternalComponent externalComponent) {
         this.externalComponent = externalComponent;
     }
 
     public CallSessionMapper getCallSessions() {
         return callSessions;
     }
 
     public void setCallSessions(CallSessionMapper callSessions) {
         this.callSessions = callSessions;
     }
 }
