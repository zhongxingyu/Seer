 package org.zaproxy.zap.extension.saml;
 
 import org.apache.log4j.Logger;
 import org.parosproxy.paros.core.proxy.ProxyListener;
 import org.parosproxy.paros.network.HtmlParameter;
 import org.parosproxy.paros.network.HttpMessage;
 
 import java.util.Map;
 import java.util.Properties;
 
public class SAMLProxyListener implements ProxyListener{
 
     private boolean active;
     private Properties autoChangeAttribs;
 
     protected static Logger log = Logger.getLogger(SAMLProxyListener.class.getName());
 
     public SAMLProxyListener() {
         setActive(true);
     }
 
    public void setActive(boolean value){
         active = value;
        if(active && autoChangeAttribs == null){
             loadAutoChangeAttributes();
         }
     }
 
     @Override
     public boolean onHttpRequestSend(HttpMessage message) {
        if(active && SAMLUtils.hasSAMLMessage(message)){
             String samlParameter = null;
             SAMLMessage samlMessage = null;
             Binding binding = null;
 
             //search the url parameters for saml message
             for (HtmlParameter parameter : message.getUrlParams()) {
                if("SAMLRequest".equals(parameter.getName())||"SAMLResponse".equals(parameter.getName())){
                     samlParameter = parameter.getName();
                     binding = Binding.HTTPRedirect;
                    samlMessage = new SAMLMessage(SAMLUtils.extractSAMLMessage(parameter.getValue(),binding));
                     break;
                 }
             }
 
             //search the post parameters for saml message
             for (HtmlParameter parameter : message.getFormParams()) {
                if("SAMLRequest".equals(parameter.getName())||"SAMLResponse".equals(parameter.getName())){
                     samlParameter = parameter.getName();
                     binding = Binding.HTTPPost;
                     samlMessage = new SAMLMessage(SAMLUtils.extractSAMLMessage(parameter.getValue(), binding));
                     break;
                 }
             }
 
             //change the parameters
             for (Map.Entry<Object, Object> entry : autoChangeAttribs.entrySet()) {
                 //todo:for now, only the first value is taken into consideration, need to fix this
                 String value = entry.getValue().toString().split(",")[0];
                 try {
                    samlMessage.setValueTo(entry.getKey().toString(),value);
                 } catch (SAMLException e) {
                     //Exception can be ignored, as the message may not contain the attribute
                    log.debug("Value "+value+" could not be set for "+entry.getKey());
                 }
             }
 
             //rebuild the message
             try {
                SAMLResender.buildSAMLRequest(message,samlParameter,samlMessage.getPrettyFormattedMessage(),binding);
             } catch (SAMLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
         return true;
     }
 
     @Override
     public boolean onHttpResponseReceive(HttpMessage message) {
         return true;
     }
 
     @Override
     public int getArrangeableListenerOrder() {
         return 0;
     }
 
    public void loadAutoChangeAttributes(){
         autoChangeAttribs = SAMLUtils.loadConfigurations();
     }
 }
