 package org.sappho.jira.rpc.soap.client;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.rpc.ServiceException;
 
 import org.sappho.jira.rpc.soap.common.FieldChange;
 
 import com.thoughtworks.xstream.XStream;
 
 public class SapphoJiraRpcSoapServiceWrapper {
 
     private final SapphoJiraRpcSoap service;
     private final String token;
 
     public SapphoJiraRpcSoapServiceWrapper(String url, String username, String password) throws MalformedURLException,
             ServiceException, RemoteException {
 
         service = new SapphoJiraRpcSoapServiceLocator().getSapphoRpcSoapV1(new URL(url
                + "/rpc/soap/sappho-getparent-v1"));
         token = service.login(username, password);
     }
 
     public String getParent(String issueKey) throws RemoteException {
 
         return service.getParentKey(token, issueKey);
     }
 
     public String getMovedIssueKey(String issueKey) throws RemoteException {
 
         return service.getMovedIssueKey(token, issueKey);
     }
 
     @SuppressWarnings("unchecked")
     public Map<String, List<FieldChange>> getFieldChangeHistory(String issueKey, String[] fieldNames)
             throws RemoteException {
 
         String xml = service.getFieldChangeHistory(token, issueKey, fieldNames);
         return (Map<String, List<FieldChange>>) new XStream().fromXML(xml);
     }
 }
