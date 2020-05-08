 package org.esbhive.node.mgt.ui;
 
 import java.rmi.RemoteException;
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.client.Options;
 import org.apache.axis2.client.ServiceClient;
 import org.apache.axis2.context.ConfigurationContext;
 import org.esbhive.node.mgt.client.ESBNode;
 import org.esbhive.node.mgt.client.EsbNodeManagerStub;
 



 /**
  * Hello world!
  *
  */
public class NodeManagerClient
{
 
  private  EsbNodeManagerStub stub;
 
  public NodeManagerClient(ConfigurationContext configCtx, String backendServerURL, String cookie) throws AxisFault{
     String serviceURL = backendServerURL + "EsbNodeManager";
    
    
     stub = new EsbNodeManagerStub(configCtx, serviceURL);
     ServiceClient client = stub._getServiceClient();
     Options options = client.getOptions();
     options.setManageSession(true);
     options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
   }
 
  public ESBNode[] addNodeToHive(ESBNode me,ESBNode addto) throws java.lang.Exception{
    try {
      return stub.addNode(me,addto);
    } catch (RemoteException ex) {
      String message = "Error  when adding node to hive";
      throw new java.lang.Exception(message,ex);
    }
  }

  public ESBNode[] getNodes() throws java.lang.Exception{
     ESBNode[] nodes;
     try {
       nodes = stub.getNodes();
       return nodes;
     } catch (RemoteException ex) {
       String message = "Error  when getting nodes";
      throw new java.lang.Exception(message,ex);
     }
   }
 }
