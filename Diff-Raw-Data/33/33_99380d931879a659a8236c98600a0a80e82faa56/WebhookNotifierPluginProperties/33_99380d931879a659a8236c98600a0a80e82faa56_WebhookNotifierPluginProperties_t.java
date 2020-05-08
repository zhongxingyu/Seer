 package org.wickedsource.hooked.plugins.webhook;
 
 import java.util.*;
 
 /**
  * @author Tom Hombergs <tom.hombergs@gmail.com>
  */
 public class WebhookNotifierPluginProperties {
 
     private static final String PROPERTY_URLS = "urlsToNotify";
 
     private static final String PROPERTY_PARAMETER_NAMES = "payloadParameterNames";
 
     private Properties properties;
 
    public WebhookNotifierPluginProperties(Properties p) {
         this.properties = p;
     }
 
    public List<WebHookParameters> getWebHookParameters() {
        List<String> urls = getUrlsFromProperties();
        List<String> parameters = getParameterNamedFromProperties();
 
         Iterator<String> urlIterator = urls.iterator();
         Iterator<String> paramIterator = parameters.iterator();
 
         List<WebHookParameters> resultList = new ArrayList<>();
         while (urlIterator.hasNext()) {
             String url = urlIterator.next();
             String paramName = "payload";
             if (paramIterator.hasNext()) {
                 paramName = paramIterator.next();
             }
             WebHookParameters params = new WebHookParameters(url, paramName);
             resultList.add(params);
         }
         return resultList;
     }

    private List<String> getParameterNamedFromProperties() {
        String parametersString = properties.getProperty(PROPERTY_PARAMETER_NAMES);
        List<String> parameters;
        if (parametersString != null) {
            parameters = Arrays.asList(parametersString.split(","));
        } else {
            parameters = new ArrayList<>();
        } return parameters;
    }

    private List<String> getUrlsFromProperties() {
        String urlsString = properties.getProperty(PROPERTY_URLS);
        List<String> urls;
        if (urlsString != null) {
            urls = Arrays.asList(urlsString.split(","));
        } else {
            urls = new ArrayList<>();
        } return urls;
    }
 }
