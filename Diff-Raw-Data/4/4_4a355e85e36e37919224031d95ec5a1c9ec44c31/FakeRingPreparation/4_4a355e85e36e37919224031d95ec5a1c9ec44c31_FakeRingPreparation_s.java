 package org.jinglenodes.custom;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.jingle.Info;
 import org.jinglenodes.jingle.processor.JingleProcessor;
 import org.jinglenodes.jingle.processor.JingleSipException;
 import org.jinglenodes.prepare.CallPreparation;
 import org.jinglenodes.session.CallSession;
 import org.xmpp.packet.JID;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.message.Message;
 import org.zoolu.sip.message.SipChannel;
 
 public class FakeRingPreparation extends CallPreparation {
     private static final Logger log = Logger.getLogger(FakeRingPreparation.class);
 
     private int sleepTime = 5;
     private JingleProcessor jingleProcessor;
 
     @Override
     public boolean prepareInitiate(JingleIQ iq, CallSession session) {
         return true;
     }
 
     @Override
     public boolean proceedInitiate(JingleIQ iq, CallSession session) {
         return true;
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
     public void proceedInfo(JingleIQ iq, CallSession session) {
     }
 
     @Override
     public boolean prepareInitiate(Message msg, CallSession session, SipChannel channel) {
         return true;
     }
 
     @Override
     public JingleIQ proceedSIPInitiate(JingleIQ iq, CallSession session, SipChannel channel) {
 
         try {
             log.debug("Fake Ring on IQ:" + iq.toXML());
            final JingleIQ ring = JingleProcessor.createJingleSessionInfo(new JID(iq.getJingle().getSid()), new JID(iq.getJingle().getInitiator()), iq.getFrom() != null ? iq.getFrom().toString() : null, iq.getJingle().getSid(), Info.Type.ringing);
             ring.setFrom(iq.getTo());
             jingleProcessor.processIQ(ring);
         } catch (JingleSipException e) {
             log.warn("Failed to create Ringing", e);
         }
 
         return iq;
     }
 
     @Override
     public void proceedSIPInfo(JingleIQ iq, CallSession session, SipChannel channel) {
     }
 
     @Override
     public JingleIQ proceedSIPEarlyMedia(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     @Override
     public JingleIQ proceedSIPTerminate(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     @Override
     public JingleIQ proceedSIPAccept(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     public int getSleepTime() {
         return sleepTime;
     }
 
     public void setSleepTime(int sleepTime) {
         this.sleepTime = sleepTime;
     }
 
     public JingleProcessor getJingleProcessor() {
         return jingleProcessor;
     }
 
     public void setJingleProcessor(JingleProcessor jingleProcessor) {
         this.jingleProcessor = jingleProcessor;
     }
 }
