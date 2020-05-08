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
 
 package org.jinglenodes.relay;
 
 import org.jinglenodes.session.CallSession;
 import org.jinglenodes.session.CallSessionMapper;
 import org.xmpp.component.AbstractServiceProcessor;
 import org.xmpp.component.IqRequest;
 import org.xmpp.packet.IQ;
 import org.xmpp.tinder.JingleIQ;
 
 /**
  * Created by IntelliJ IDEA.
  * User: thiago
  * Date: 2/20/12
  * Time: 8:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RelayServiceProcessor extends AbstractServiceProcessor {
 
     private String relayService;
     private final String namespace = RelayIQ.NAMESPACE;
     private CallSessionMapper callSessionMapper;
 
     @Override
     public IQ createServiceRequest(Object object, final String fromNode, final String toNode) {
         final RelayIQ relayIQ = new RelayIQ(true);
         relayIQ.setTo(toNode != null ? toNode + "@" + relayService : relayService);
        relayIQ.setFrom(fromNode != null ? fromNode + "@" + this.getComponentJID().getDomain() : null);
         return relayIQ;
     }
 
     @Override
     protected String getRequestId(Object obj) {
         if (obj instanceof JingleIQ) {
             final JingleIQ iq = (JingleIQ) obj;
             return iq.getJingle().getSid();
         }
         return null;
     }
 
     @Override
     protected void handleResult(IqRequest iq) {
         if (iq.getOriginalPacket() instanceof JingleIQ) {
             final JingleIQ jingleIQ = (JingleIQ) iq.getOriginalPacket();
             final CallSession callSession = callSessionMapper.getSession(jingleIQ);
             if (callSession != null) {
                 final RelayIQ relayIQ = RelayIQ.parseRelayIq(iq.getResult());
                 callSession.setRelayIQ(relayIQ);
             }
         }
     }
 
     @Override
     protected void handleError(IqRequest iq) {
 
     }
 
     @Override
     protected void handleTimeout(IqRequest request) {
 
     }
 
     @Override
     public String getNamespace() {
         return namespace;
     }
 
     public String getRelayService() {
         return relayService;
     }
 
     public void setRelayService(String relayService) {
         this.relayService = relayService;
     }
 
     public CallSessionMapper getCallSessionMapper() {
         return callSessionMapper;
     }
 
     public void setCallSessionMapper(CallSessionMapper callSessionMapper) {
         this.callSessionMapper = callSessionMapper;
     }
 }
