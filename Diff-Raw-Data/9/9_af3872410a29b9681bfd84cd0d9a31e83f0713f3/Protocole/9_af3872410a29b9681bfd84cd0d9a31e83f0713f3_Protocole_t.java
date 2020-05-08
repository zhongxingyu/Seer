 package fr.pc2ron.protocole;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import fr.pc2ron.interfaces.IChaine;
 import fr.pc2ron.interfaces.IDonnee;
 import fr.pc2ron.interfaces.IEntierNonSigne2;
 import fr.pc2ron.interfaces.IProtocole;
 import fr.pc2ron.interfaces.ITrame;
 import fr.pc2ron.interfaces.ITrameBuilder;
 import fr.pc2ron.interfaces.ITrameFactory;
 import fr.pc2ron.trame.ReceptionTrame;
 import fr.pc2ron.trame.Trame;
 import fr.pc2ron.trame.TrameFactory;
 import fr.pc2ron.trame.VisiteurEnvoiTrame;
 import fr.pc2ron.trame.donnee.Chaine;
 import fr.pc2ron.trame.donnee.DonneeFactory;
 import fr.pc2ron.trame.donnee.EntierNonSigne2;
 
 import fr.pc2ron.protocole.ETypeTrame;
 
 public class Protocole implements IProtocole {
 	private Socket sock;
 	private DataOutputStream out;
 	private DataInputStream in;
 	
 	private static IProtocole instance;
 	
 	private Protocole() {
        }
 	
 	public static IProtocole getInstance() {
 		if (null == instance) { // Premier appel
 			instance = new Protocole();
 	    }
 	    return instance;
 	}
 
 	@Override
 	public void commencerPartie() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	// { 0x49, string "PC2RON?", string "PC2RON2011" } 
 	public void connexion(String host, int port) throws Exception {
 		// Initialisation de la socket
 		sock = new Socket(host, port);
 		in = new DataInputStream(sock.getInputStream());
 		out = new DataOutputStream(sock.getOutputStream());
 		
 		// Envoi de la trame Init
 		ITrameBuilder trameBuilder = TrameBuilder.getInstance();
 		ITrame trameInit = trameBuilder.creerTrameInit();
 		
 		VisiteurEnvoiTrame envoi = new VisiteurEnvoiTrame();
 		envoi.visit(trameInit, out);
 		
 		// Reception de l'acquittement
 		ReceptionTrame reception = new ReceptionTrame();
 		ITrame ack = reception.recevoirTrame(in);
 		
                 
                 
                 ETypeTrame eTypeTrame = ETypeTrame.getTypeTrame(ack.getId());
 		
 		IDonnee message = ack.getDonnees().get(0);
 		
 		// ? { 0x41, string "OK", string "PC2R2011" }
 		if(eTypeTrame == ETypeTrame.TrameAck &&
 		   message instanceof Chaine) {
 			IChaine chaine = (IChaine) message;
 				
 			if(! chaine.getChaine().equals("OK")) {
                             throw new Exception("La version du protocole n'est pas supportee par le serveur !");
 			}
 		} else {
                     throw new Exception("Impossible de lire la reponse du serveur !");
 		}
 	}
 
 	@Override
 	public void deconnexion() {
             //try {
                 ITrameFactory trameFactory = TrameFactory.getInstance();
                 ITrame trameFin = trameFactory.getTrameEnd();
                 VisiteurEnvoiTrame envoi = new VisiteurEnvoiTrame();
                 
                 envoi.visit(trameFin, out);
                 /*
                 out.flush();
                 out.close();
                 sock.close();
                 
                  
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }*/
 	}
 
 	@Override
 	public void envoyerOrdre(EOrdre ordre) {
             // Envoi de la trame Order
             ITrameBuilder trameBuilder = TrameBuilder.getInstance();
             ITrame trameOrder = trameBuilder.creerTrameOrder(ordre);
             
             //debug
             System.out.println(trameOrder.toString());
             //debug
             
             VisiteurEnvoiTrame envoi = new VisiteurEnvoiTrame();
             envoi.visit(trameOrder, out);
 	}
 
 	@Override
         //@todo implementer
 	public HashMap<String, Object> getDonneesUser(ITrame trameUser) throws Exception {
             HashMap<String, Object> hashmapUser = new HashMap<String, Object>();
  
             return hashmapUser;
 	}
 		
 	@Override
 	public int getGagnant(ITrame trameWin) throws Exception {
             //IDonnee donnee = trameWin.getDonnees().get(0);
             int idGagnant = -1;
             
             if(trameWin.getNbDonnees() != 1) {
                 throw new Exception("La trame Start recue n'est pas correcte !");
             } else {
                 IDonnee donnee = trameWin.getDonnees().get(0);
 
                 if(donnee instanceof EntierNonSigne2) {
                     IEntierNonSigne2 entierNonSigne2 = (IEntierNonSigne2) donnee;
                     idGagnant = entierNonSigne2.getEntier();
                 } else {
                     throw new Exception("La trame Start recue n'est pas correcte !");
                 }
             }
             
             return idGagnant;
 	}
 
 
 	@Override
 	public ArrayList getContenuTrame() throws Exception {
             ReceptionTrame reception = new ReceptionTrame();
             ITrame trameRecue = reception.recevoirTrame(in);
             ETypeTrame typeTrame = ETypeTrame.getTypeTrame(trameRecue.getId());
 
             ArrayList contenuTrame = new ArrayList();
             contenuTrame.add(typeTrame);
 
             switch(typeTrame) {
                 // @todo implementer
                 case TrameUser:
                     do {
                         contenuTrame.add(getDonneesUser(trameRecue));
                         //debug
                         System.out.println(trameRecue.toString());
                         //debug
                         
                         trameRecue = reception.recevoirTrame(in);
                         typeTrame = ETypeTrame.getTypeTrame(trameRecue.getId());
                     } while (typeTrame != ETypeTrame.TrameEnd);
                     
                     break;
 
                 case TrameStart:
                     contenuTrame.add(getMessageStart(trameRecue));
                     break;
 
                 case TramePause:
                     contenuTrame.add(getMessagePause(trameRecue));
                     break;
 
                 case TrameTurn:
                     contenuTrame.add(getPositions(trameRecue));
                     break;
 
                 case TrameDeath:
                     contenuTrame.add(getPerdants(trameRecue));
                     break;
 
                 case TrameWin:
                     contenuTrame.add(getGagnant(trameRecue));
                     break;
 
                 default:
                     throw new Exception("Type de trame inconnu !");
             }
             
             //DEBUG
             System.out.println(trameRecue.toString());
             //DEBUG
             
             return contenuTrame;
 	}
 
 	@Override
 	public int inscription(short r, short v, short b, String nom)
 			throws Exception {
                 System.out.println("Inscription...");
                 
 		// Envoi de la trame Connect
 		ITrameBuilder trameBuilder = TrameBuilder.getInstance();
 		ITrame trameConnect = trameBuilder.creerTrameConnect(r, v, b, nom);
 		
 		VisiteurEnvoiTrame envoi = new VisiteurEnvoiTrame();
 		envoi.visit(trameConnect, out);
 		
 		// Reception de l'acquittement
 		ReceptionTrame reception = new ReceptionTrame();
 		ITrame ack = reception.recevoirTrame(in);
 		
 		ETypeTrame typeTrame = ETypeTrame.getTypeTrame(ack.getId());
 		
 		IDonnee message = ack.getDonnees().get(0);
 		IDonnee donnee = ack.getDonnees().get(1);
 		int id = -1;
 		
                 //DEBUG
                 System.out.println(ack.toString());
                 //DEBUG
                 
 		// ? { 0x52, string "OK", uint16 id }
 		if(typeTrame == ETypeTrame.TrameRegistered &&
 		   message instanceof Chaine) {
 			IChaine chaine = (IChaine) message;
 			
 			if(! chaine.getChaine().equals("OK")) {
 				IChaine messageErreur = (IChaine) donnee;
 				throw new Exception("Inscription impossible ! : " + messageErreur.getChaine());
 			} else {
 				IEntierNonSigne2 entierNonSigne2 = (IEntierNonSigne2) donnee;
 				id = entierNonSigne2.getEntier();
 			}
 		} else {
 			throw new Exception("Impossible de lire la reponse du serveur !");
 		}
 		
 		return id;
 	}
 
 	@Override
 	public String getMessageStart(ITrame trameStart) throws Exception {
 		IDonnee donnee = trameStart.getDonnees().get(0);
 		String message = "";
 		
 		if(donnee != null && donnee instanceof Chaine) {
 			IChaine chaine = (IChaine) donnee;
 			message = chaine.getChaine();
 		} else {
 			throw new Exception("La trame Start recue n'est pas correcte !");
 		}
 		
 		return message;
 	}
 
 	@Override
 	public String getMessagePause(ITrame tramePause) throws Exception {
 		IDonnee donnee = tramePause.getDonnees().get(0);
 		String message = "";
 		
 		if(donnee != null && donnee instanceof Chaine) {
 			IChaine chaine = (IChaine) donnee;
 			message = chaine.getChaine();
 		} else {
 			throw new Exception("La trame Pause recue n'est pas correcte !");
 		}
 		
 		return message;
 	}
 
 	@Override
 	public int[] getPerdants(ITrame trameDeath) throws Exception {
             int[] perdants = null;
             int nbDonnees = trameDeath.getNbDonnees();
             
             if(nbDonnees == 1 || nbDonnees == 2) {
                 perdants = new int[nbDonnees];
                 IDonnee donnee;
                 
                 for(int i = 0; i < nbDonnees; i++) {
                     donnee = trameDeath.getDonnees().get(i);
                     
                     if(donnee instanceof EntierNonSigne2) {
                         IEntierNonSigne2 entier = (IEntierNonSigne2) donnee; 
                         perdants[i] = entier.getEntier();
                     }  else {
                         throw new Exception("La trame Death recue n'est pas correcte !");
                     }
                 }
                 
             } 
             
             return perdants;
 	}
 
 	@Override
         //@todo implementer
 	public HashMap<String, Object> getPositions(ITrame trameTurn) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
