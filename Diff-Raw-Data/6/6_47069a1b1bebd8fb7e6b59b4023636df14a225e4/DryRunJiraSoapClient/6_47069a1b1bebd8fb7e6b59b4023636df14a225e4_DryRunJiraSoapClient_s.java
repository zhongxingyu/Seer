 package jiraldapsyncer;
 
 import _soapclient.beans.RemoteGroup;
 import _soapclient.beans.RemoteUser;
 
 import javax.xml.rpc.ServiceException;
 import java.rmi.RemoteException;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class DryRunJiraSoapClient extends JiraSoapClient {
    public DryRunJiraSoapClient(String adminLogin, String adminPassword) throws ServiceException, RemoteException {
        super(adminLogin, adminPassword);
     }
 
     @Override
     public boolean createUser(RemoteUser user) throws RemoteException {
         System.out.println("Would have created "+user.getName());
         return true;
 //        return super.createUser(user);
     }
 
     @Override
     public void addUserToGroup(RemoteUser user, RemoteGroup group) throws RemoteException {
         System.out.println("Would have added "+user.getName()+" to "+group.getName());
     }
 
     @Override
     public void removeUserFromGroup(RemoteUser user, RemoteGroup group) throws RemoteException {
         System.out.println("Would have removed " + user.getName() + " from " + group.getName());
     }
 }
