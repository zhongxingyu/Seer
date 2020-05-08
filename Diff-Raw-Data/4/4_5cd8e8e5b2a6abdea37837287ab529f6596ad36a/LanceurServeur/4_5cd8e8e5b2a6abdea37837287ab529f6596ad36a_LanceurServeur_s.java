 package fr.mercredymurderparty.serveur;
 
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.net.InetAddress;
 import fr.mercredymurderparty.client.CoeurClient;
 import fr.mercredymurderparty.ihm.fenetres.FenetreAdmin;
 import fr.mercredymurderparty.outil.FichierXML;
 import fr.mercredymurderparty.outil.Theme;
 
 public class LanceurServeur
 {
 	/**
 	 * @param _args : un tableau d'argument qu'on peut passer  l'appel de l'application
 	 */
 	public static void main(String[] _args)
 	{
 		// lancement du serveur
 		new LanceurServeur();
 	}
 	
 	/**
 	 * Le constructeur du lanceur
 	 */
 	public LanceurServeur()
 	{
 		// Charger fichier de config
 		FichierXML xml = new FichierXML("config.xml");
 		
 		// Au premier lancement de l'appli, on prend une ip de base
 		try 
 		{
			if (xml.valeurNoeud("application", "premierLancement") == "oui")
 			{
 				// Recuperer l'adresse ip de la machine
 				InetAddress in = InetAddress.getLocalHost();
 				InetAddress[] all = InetAddress.getAllByName(in.getHostName());
 				
 				// Sauvegarder l'inet adresse dans le fichier config
 				xml.modifierNoeud("serveur", "ip", all[1].getHostAddress());
 			}
 		} 
 		catch (Exception e) 
 		{
 			// Si ca se passe mal, on laisse en localhost
 			xml.modifierNoeud("serveur", "ip", "localhost");
 		}
 		finally
 		{
 			xml.modifierNoeud("application", "premierLancement", "non");
 		}
 
 		// Lancer le serveur
 		CoeurServeur server = new CoeurServeur(Integer.parseInt(xml.valeurNoeud("serveur", "port")));
 		
 		// Dfinir le thme de l'interface
 		Theme theme = new Theme();
 		theme.charger();
 		
 		// Dfinir l'environnement graphique
 		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice[] devices = env.getScreenDevices();
 			
 		// Crer une nouvelle connexion client
 		CoeurClient coeurClient = new CoeurClient(xml.valeurNoeud("serveur", "ip"), Integer.parseInt(xml.valeurNoeud("serveur", "port")));
 			
 		// Lancer la fenetre
 		FenetreAdmin fenetreAdmin = new FenetreAdmin(devices[0], coeurClient, server);
 		fenetreAdmin.setVisible(true);
 	}
 }
