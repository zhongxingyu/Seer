 package org.jinglenodes.detour;
 
 import org.apache.log4j.Logger;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.dom4j.QName;
 import org.xmpp.component.AbstractServiceProcessor;
 import org.xmpp.component.IqRequest;
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.JID;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.message.JIDFactory;
 
 /**
  * Created by IntelliJ IDEA.
  * User: thiago
  * Date: 2/21/12
  * Time: 5:52 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DetourServiceProcessor extends AbstractServiceProcessor {
     private final Logger log = Logger.getLogger(DetourServiceProcessor.class);
     private final Element requestElement;
     private final String xmlns;
     private String accountService;
 
     public DetourServiceProcessor(final String elementName, final String xmlns) {
         this.xmlns = xmlns;
         this.requestElement = DocumentHelper.createElement(new QName(elementName, new Namespace("", xmlns)));
     }
 
     @Override
    public IQ createServiceRequest(Object object,  String fromNode, String toNode) {
         final IQ request = new IQ(IQ.Type.get);
         if (toNode.indexOf("00") == 0) {
             toNode = "+" + toNode.substring(2);
         }
         final JID toService = JIDFactory.getInstance().getJID(toNode + "@" + accountService);
         request.setTo(toService);
         request.setChildElement(requestElement.createCopy());
         if (log.isDebugEnabled()) {
             log.debug("createServiceRequest: " + request.toXML());
         }
         return request;
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
     }
 
     @Override
     protected void handleError(IqRequest iq) {
         log.error("Failed to Retrieve Account: " + iq.getResult().toXML());
     }
 
     @Override
     protected void handleTimeout(IqRequest request) {
 
     }
 
     @Override
     public String getNamespace() {
         return xmlns;
     }
 
     public String getAccountService() {
         return accountService;
     }
 
     public void setAccountService(String accountService) {
         this.accountService = accountService;
     }
 }
