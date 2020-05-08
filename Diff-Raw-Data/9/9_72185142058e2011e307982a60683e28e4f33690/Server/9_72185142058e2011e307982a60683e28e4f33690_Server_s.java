 package sd.server;
 
import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.rmi.server.RemoteServer;
 import java.rmi.server.ServerNotActiveException;
 import java.util.Hashtable;
 import java.util.TreeSet;
 
 import sd.exceptions.NenhumServidorDisponivelException;
 import sd.exceptions.ObjetoNaoEncontradoException;
 import sd.interfaces.InterfaceAcesso;
 import sd.interfaces.InterfaceReplicacao;
 import sd.types.Box;
 
 /**
  * @author Anderson de França Queiroz <contato (at) andersonq (dot) eti (dot) br
  * @author André Xavier Martinez
  * @author Tiago de França Queiroz <contato (at) tiago (dot) eti (dot) br
  *
  */
 @SuppressWarnings("serial")
public class Server implements InterfaceAcesso, InterfaceReplicacao, Serializable
 {
     Hashtable<Integer, Box> h;
     TreeSet<Integer> bst;
 
    public Server()
     {
         h = new Hashtable<Integer, Box>();
         bst = new TreeSet<Integer>();
     }
 
     public void replica(Integer id, Box obj) throws RemoteException,
     NenhumServidorDisponivelException
     {
         h.put(id, obj);
         bst.add(id);
 
         try
         {
             System.out.printf("Received %s\n", obj.toString());
             System.out.printf("Received %s from %s\n", obj.toString(), RemoteServer.getClientHost());
         }
         catch (ServerNotActiveException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public void apaga(Integer id) throws RemoteException,
     NenhumServidorDisponivelException, ObjetoNaoEncontradoException
     {
         try
         {
             System.out.printf("%s asked to delete %s\n", RemoteServer.getClientHost(), id);
         }
         catch (ServerNotActiveException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         if(! bst.contains(id))
             throw new ObjetoNaoEncontradoException(id.toString());
         else
         {
             bst.remove(id);
             h.remove(id);
         }
     }
 
     public Box recupera(String link) throws RemoteException,
     ObjetoNaoEncontradoException
     {
         String id;
         Integer i;
 
         /*
          * It splits the string link and get the part before "#"
          * in other words, it gets the object ID.
          */
         id = link.split("#")[0];
 
         i = new Integer(id);
 
         if(bst.contains(i))
             return (Box) h.get(i);
         else
             throw new ObjetoNaoEncontradoException(id);
     }
 }
