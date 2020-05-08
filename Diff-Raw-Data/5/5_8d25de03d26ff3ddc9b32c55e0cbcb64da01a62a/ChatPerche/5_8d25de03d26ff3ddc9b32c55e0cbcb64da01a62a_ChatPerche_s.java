 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package chatperche_serveur;
 import Commun.Chaterface;
 import Commun.Requete;
 import java.net.InetAddress;
 import java.rmi.*;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  *
  * @author Sebastien
  */
 public class ChatPerche extends UnicastRemoteObject implements Chaterface {
 
     private LinkedList<Integer> personnesConnectees;
     private LinkedList<Message> messages;
     
     // Implémentation du constructeur
     public ChatPerche() throws java.rmi.RemoteException {
         this.messages = new LinkedList<Message>();
         this.personnesConnectees = new LinkedList<Integer>();
         System.out.println("Serveur lancé");
     }
     
     // Implémentation de la méthode distante
     public String requeteClient(Requete req) throws java.rmi.RemoteException {
         String message = req.getMessage();
         
         if (message.startsWith("connect")) 
            return this.connect();
         if (req.getClientID()==-1)
            return "Vous n'êtes pas connecté";
         else if (message.startsWith("send"))
             return this.send(message.substring(5), req.getClientID());
         else if (message.startsWith("bye"))
             return this.bye(req.getClientID());
          else if (message.startsWith("who"))
             return this.who();
         else {
              return  "Commande inconnue";
         }
 
     }
     /**
      * Methode de connexion : on renvoie le premier id de connexion disponible
      * @param id
      * @throws java.rmi.RemoteException 
      */
     public String connect() {
         int id = 0;
         while (personnesConnectees.contains(id)) {
             id++;
         } 
         personnesConnectees.add(id);
         System.out.println("L'utilisateur " + id + " s'est connecté");
         return "Connecté : "+id;
     }
     
     /** 
      * Methode de déconnexion
      * @param id
      * @throws java.rmi.RemoteException 
      */
     public String bye(int id) throws java.rmi.RemoteException {
         if (personnesConnectees.contains(id)) {
             personnesConnectees.remove(id);
         } 
         String aAfficher = "L'utilisateur " + id + " s'est déconnecté";
         System.out.println(aAfficher);
         return "Good bye";
         
     }
     
     
     /**
      * Affiche les utilisateurs connectés
      * @return String, contenant les id des utilisateurs connectés
      * @throws java.rmi.RemoteException 
      */
     public String who() throws java.rmi.RemoteException {
         String result = "";
         Iterator<Integer> itr = this.personnesConnectees.iterator();
         while (itr.hasNext()){
                 Integer i = itr.next();
                 if (result == "") {
                     result += "Liste des personnes connectées : " + i.toString();
                 }
                 else {
                     result += ", " + i.toString();
                 }
         }
         return result;
     }
     
      /**
      * Affiche tous les messages envoyés
      * @return String
      * @throws java.rmi.RemoteException 
      */
     public String displayAll() throws java.rmi.RemoteException {
         String result = "";
         Iterator<Message> itr = this.messages.iterator();
         while (itr.hasNext()){
             Message m = itr.next();
             result += "Client " + m.getClientID() + " a dit : " + m.getMessage() + "\n";
         }
         return result;
     }
     
     /**
      * Fonction d'envoi d'un message texte (mot clef "send")
      * @param message
      * @throws java.rmi.RemoteException 
      */
     public String send(String message, int idUtilisateur) throws java.rmi.RemoteException {
         Message m = new Message(message, idUtilisateur);
         messages.add(m);
         return ("Vous avez écrit '" + message + "'");
     }
     
     
 
     
     public static void main(String args[]) {
             
         int port=8888;
         String URL;
         
 //        try { // transformation d ’une chaîne de caractères en entier
 //            Integer I = new Integer(args[0]);
 //            port = I.intValue();
 //        } catch (Exception ex) {
 //            System.out.println(" Please enter: Server <port>");
 //            return;
 //        }
         try {
             // Création du serveur de nom - rmiregistry
             Registry registry = LocateRegistry.createRegistry(port);
             // Création d ’une instance de l’objet serveur
             Chaterface obj = new ChatPerche();
             // Calcul de l’URL du serveur
             URL = "//" + InetAddress.getLocalHost().getHostName() + ":"
                     + port + "/mon_serveur";
             Naming.rebind(URL, obj);
         } catch (Exception exc) {
              exc.printStackTrace();
         }
     }
 
 }
