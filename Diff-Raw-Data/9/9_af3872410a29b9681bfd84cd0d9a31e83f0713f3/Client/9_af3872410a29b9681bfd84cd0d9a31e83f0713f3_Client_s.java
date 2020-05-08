 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.pc2ron;
 
 import fr.pc2ron.interfaces.*;
 import fr.pc2ron.protocole.EOrdre;
 import fr.pc2ron.protocole.ETypeTrame;
 import fr.pc2ron.protocole.Protocole;
 import fr.pc2ron.protocole.TrameBuilder;
 import fr.pc2ron.trame.ReceptionTrame;
 import fr.pc2ron.trame.TrameFactory;
 import fr.pc2ron.trame.VisiteurEnvoiTrame;
 import fr.pc2ron.trame.donnee.DonneeFactory;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 /**
  *
  * @author mehdi
  */
 public class Client {
     public static void main(String[] argv) {       
         //@fail
         // Creation de la trame
         IDonneeFactory donneeFactory = DonneeFactory.getInstance();
         ITrameFactory trameFactory = TrameFactory.getInstance();
         IProtocole protocole = Protocole.getInstance();
         
         //@fail
         VisiteurEnvoiTrame envoi = new VisiteurEnvoiTrame();
         IReceptionTrame recept = new ReceptionTrame();
         ITrameBuilder tBuilder = TrameBuilder.getInstance();
         
         try {          
             protocole.connexion("127.0.0.1", 5555);
             //protocole.inscription((short)56, (short)67, (short)350, "Mehdi");
             
            //protocole.deconnexion();
           
             // Trame User
             //protocole.getContenuTrame();
             
             // Decompte
             //protocole.getContenuTrame();
             //protocole.getContenuTrame();
             //protocole.getContenuTrame();
             //protocole.getContenuTrame();
             
             //protocole.envoyerOrdre(EOrdre.DROIT);
         } catch (Exception ex) {
             System.out.println("Connexion impossible");
             ex.printStackTrace();
         }
     }
 }
