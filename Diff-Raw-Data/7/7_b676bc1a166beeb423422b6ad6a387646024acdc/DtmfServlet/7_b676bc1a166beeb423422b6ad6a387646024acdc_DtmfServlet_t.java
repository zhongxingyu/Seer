 // ========================================================================
 // Copyright 2008-2010 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 
 package org.cipango.example;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.sip.SipApplicationSession;
 import javax.servlet.sip.SipServlet;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 
 import org.mortbay.log.Log;
 
 public class DtmfServlet extends SipServlet
 {
 
     public static final int DTMF_PAYLOAD_TYPE = 101;
 
     private static final long serialVersionUID = 1L;
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         log("init");
         SdpParser sdpParser = new SdpParser();
         ServletContext servletContext = getServletContext();
         servletContext.setAttribute(SdpParser.class.getName(), sdpParser);
     }
 
     @Override
     protected void doRegister(SipServletRequest register)
             throws ServletException, IOException {
         log("doRegister");
         register.createResponse(SipServletResponse.SC_OK).send();
     }
 
     @Override
     protected void doInvite(SipServletRequest invite) throws ServletException,
             IOException {
         log("doInvite");
         invite.createResponse(SipServletResponse.SC_RINGING).send();
         ServletContext servletContext = getServletContext();
         SdpParser sdpParser = (SdpParser)servletContext.getAttribute(
                 SdpParser.class.getName());
         Charset charset = Charset.forName("UTF-8");
 
         Object contentObject = invite.getContent();
         String sdp;
         if (contentObject instanceof String)
             sdp = (String)contentObject;
         else
         {
             byte[] content = (byte[])contentObject;
             sdp = new String(content, charset);
         }
         RtpConnection rtpConnection = sdpParser.getRtpConnection(sdp);
         if (rtpConnection == null)
         {
             invite.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE).send();
             return;
         }
         int audioPayloadType;
         List<Integer> payloadTypes = rtpConnection.getPayloadTypes();
         if (payloadTypes.contains(0))
             audioPayloadType = 0;
         else if (payloadTypes.contains(8))
             audioPayloadType = 8;
         else
         {
             invite.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE).send();
             return;
         }
         if (!payloadTypes.contains(DTMF_PAYLOAD_TYPE))
         {
             invite.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE).send();
             return;
         }
         int port = rtpConnection.getPort();
         DtmfSession dtmfSession = new DtmfSession(DTMF_PAYLOAD_TYPE,
                 rtpConnection.getHost(), port, audioPayloadType);
         try
         {
             dtmfSession.init();
         }
         catch (Exception e)
         {
             Log.warn("cannot initialize dtmf session on port " + port, e);
             SipServletResponse resp = invite.createResponse(
                     SipServletResponse.SC_SERVER_INTERNAL_ERROR);
             resp.send();
             return;
         }
         invite.getApplicationSession(true).setAttribute(
                 DtmfSession.class.getName(), dtmfSession);
 
         SipServletResponse resp =
             invite.createResponse(SipServletResponse.SC_OK);
         String sdpAnswer = "v=0\r\n"
            + "o=user1 123 456 IN IP4 127.0.0.1\r\n"
             + "s=-\r\n"
            + "c=IN IP4 127.0.0.1\r\n"
             + "t=0 0\r\n"
             + "m=audio " + dtmfSession.getLocalPort() + " RTP/AVP "
                 + audioPayloadType + " " + DTMF_PAYLOAD_TYPE + "\r\n"
             + "a=rtpmap:0 PCMU/8000\r\n"
             + "a=rtpmap:" + DTMF_PAYLOAD_TYPE + " telephone-event/8000\r\n"
             + "a=fmtp:" + DTMF_PAYLOAD_TYPE + " 0-15\r\n";
         
         resp.setContent(sdpAnswer.getBytes(charset), "application/sdp");
         resp.send();
     }
 
     @Override
     protected void doAck(SipServletRequest ack) throws ServletException,
             IOException {
         log("doAck");
         SipApplicationSession sipApplicationSession =
             ack.getApplicationSession();
         DtmfSession dtmfSession = (DtmfSession)
             sipApplicationSession.getAttribute(DtmfSession.class.getName());
         dtmfSession.start();
     }
 
     @Override
     protected void doBye(SipServletRequest bye) throws ServletException,
             IOException {
         log("doBye");
         bye.createResponse(SipServletResponse.SC_OK).send();
         SipApplicationSession sipApplicationSession =
             bye.getApplicationSession();
         DtmfSession dtmfSession = (DtmfSession)
             sipApplicationSession.getAttribute(DtmfSession.class.getName());
         dtmfSession.stop();
         bye.getSession().invalidate();
         sipApplicationSession.invalidate();
     }
 
 }
