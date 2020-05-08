 /******************************************************
 Cours:  LOG121
 Projet: Squelette du laboratoire #1
 Nom du fichier: CommBase.java
 Date créé: 2013-05-03
 *******************************************************
 Historique des modifications
 *******************************************************
 *@author Patrice Boucher
 2013-05-03 Version initiale
 *******************************************************/  
 
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 
 
 /**
  * Base d'une communication via un fil d'exécution parallèle.
  */
 public class CommBase {
 	
 	private final int DELAI = 1000;
 	private SwingWorker threadComm =null;
 	private PropertyChangeListener listener = null;
 	private boolean isActif = false;
 	int port = 0;
 	Socket socket;
 	BufferedReader in;
     PrintWriter out;
 	String host = "localhost";
 	
 	/**
 	 * Constructeur
 	 */
 	public CommBase(){
 	}
 	
 	/**
	 * Dfinir le rcepteur de l'information reue dans la communication avec le serveur
 	 * @param listener sera alerté lors de l'appel de "firePropertyChanger" par le SwingWorker
 	 */
 	public void setPropertyChangeListener(PropertyChangeListener listener){
 		this.listener = listener;
 	}
 	
 	/**
 	 * Démarre la communication
 	 */
 	public void start(){
		String hostbrute = JOptionPane.showInputDialog("Quel est le nom d'hte et le port du serveur de formes?", "localhost:10000");
 		String str[]=hostbrute.split(":");
 		port = Integer.parseInt(str[1]);
 		host = str[0];
 		System.out.println(host);
 		System.out.println(port);
 		creerCommunication();
 	}
 	
 	/**
 	 * Arrête la communication
 	 */
 	public void stop(){
 		try {
 		 out.println("END");
          out.flush();
          socket.close();
 		}catch (UnknownHostException e) {
             
             e.printStackTrace();
         }catch (IOException e) {
              
             e.printStackTrace();
         }
 		if(threadComm!=null)
 			threadComm.cancel(true); 
 		isActif = false;
 		
 	}
 	
 	/**
 	 * Créer le nécessaire pour la communication avec le serveur
 	 */
 	protected void creerCommunication(){		
 		// Crée un fil d'exécusion parallèle au fil courant,
 		threadComm = new SwingWorker(){
 			@Override
 			protected Object doInBackground() throws Exception {
 				System.out.println("Le fils d'execution parallele est lance");
 				
 				try {
 		            socket = new Socket(host,port); 
 		            in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
 		            
 		                out = new PrintWriter(socket.getOutputStream());
 		                
 		                //System.out.println(in.readLine());
 		                
 		        }catch (UnknownHostException e) {
 		             
 		            e.printStackTrace();
 		        }catch (IOException e) {
 		             
 		            e.printStackTrace();
 		        }				
 				while(true){
 					Thread.sleep(DELAI);
 					out.println("GET");
 	                out.flush();
 	                String message_distant = in.readLine();
 	                message_distant = in.readLine();
 	                if(listener!=null)
 						   firePropertyChange("ENVOIE-TEST", null, message_distant);
 					
 					// C'EST DANS CETTE BOUCLE QU'ON COMMUNIQUE AVEC LE SERVEUR
 					
  					//La méthode suivante alerte l'observateur 
 					
 				}
 				//return null;
 			}
 		};
 		if(listener!=null)
 		   threadComm.addPropertyChangeListener(listener); // La méthode "propertyChange" de ApplicationFormes sera donc appelée lorsque le SwinkWorker invoquera la méthode "firePropertyChanger" 		
 		threadComm.execute(); // Lance le fil d'exécution parallèle.
 		isActif = true;
 	}
 	
 	/**
 	 * @return si le fil d'exécution parallèle est actif
 	 */
 	public boolean isActif(){
 		return isActif;
 	}
 }
