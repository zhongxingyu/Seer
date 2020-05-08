 package models.network;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import models.network.Game;
 
 /**
  * Een klasse voor het onderhouden van een 
  * socketverbinding tussen een Client en een Server.
  */
 public class ClientHandler extends Communicator implements Protocol, Runnable {
 
   private Server server;
   private Socket sock;
   private String name;
   private Game game;
 
   /**
    * Construeert een ClientHandler object.
    * Initialiseert de beide datastreams.
    * @require server != null && sock != null
    */
   public ClientHandler(Server server, Socket sock) throws IOException {
     this.server = server;
     this.sock = sock;        
     // Initialiseert beide datastreams
     this.in   = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
     this.out  = new BufferedWriter(new OutputStreamWriter(this.sock.getOutputStream()));
   }
 
   /**
    * De berichten die binnen komen op de inputstream
    * van de socket worden hier afgehandeld.
    */
   public void run() {
     try {
       String line;
       while ((line = in.readLine()) != null) {
         try {
           processCommand(line);          
         } catch (IllegalArgumentException e) {
           // Laat de client weten dat dit command 'm niet gaat worden.
           sendCommand(Protocol.ERROR, Protocol.ERROR_MALFORMED, e.getMessage());
         }
       }
      // De inputstream is gestopt (zonder foutmelding), de client
      // heeft de verbinding dus verbroken.
      shutdown();
     } catch (IOException e) {
       shutdown();
     }
   }
   
   /**
    * Verwerkt binnenkomende commando's van een client
    * naar de server.
    * @throws IllegalArgumentException als het commando niet
    *         ondersteund wordt
    */
   public void processCommand(String msg) {
     String command;
     Scanner scanner = new Scanner(msg);
     
     try {
       command = scanner.next();      
     } catch (NoSuchElementException e) {
       throw new IllegalArgumentException("Het commando is leeg!");
     }
     
     try {      
       if (Protocol.JOIN.equals(command)) {
         // We kennen deze speler toe aan de client.
         // Op dit moment is nog niet duidelijk welke mark
         // deze speler gaat gebruiken, of dat erg is komen
         // we later wel achter.
         this.name = scanner.next();
         // Successvol gejoined.
         sendCommand(Protocol.SUCCESS, Protocol.FALSE, Protocol.FALSE);
       }
 
       else if (Protocol.REQUEST_GAME.equals(command)) {
         int numPlayers = scanner.nextInt();
         this.broadcast(
           String.format("Ik wil graag een spel spelen met %d spelers.", numPlayers)
         );
         // Nieuw spel aanvragen met numPlayers.
         // Sla de aanvraag op zodat we ons ook weer kunnen terugtrekken
         // als we voortijdig de verbinding verbreken.
         Game.request(numPlayers, this);
       }
 
       else if (Protocol.MARK.equals(command)) {
       }
 
       else if (Protocol.TURN.equals(command)) {
       }
      
       else if (Protocol.CHAT.equals(command)) {
       }
       
       else if (Protocol.LIST_PLAYERS.equals(command)) {
         if (game != null) {
           // In-game
           String playerList = "";
           for (ClientHandler player : this.game.getPlayers()) {
             playerList += player.getName() + " ";
           }
           // Stuur de lijst terug
           sendCommand(Protocol.PLAYER_LIST, playerList);
           
         } else {
           // In de challenge room
           // this.challengeRoom.getPlayers()
           // ..
         }
       }
       
       else if (Protocol.ACCEPT.equals(command)) {
       }
       
       else {
         throw new IllegalArgumentException(
           String.format("Het commando: '%s' wordt niet ondersteund!", command)
         );
       }
             
     } catch (NoSuchElementException e) {
       // We hadden verwacht dat er nog een
       // argument kwam (scanner.next()), maar blijkbaar is die 
       // niet meegegeven of van het juiste type, dus een exceptie. 
       throw new IllegalArgumentException(
         String.format("Het commando: '%s' is onjuist geformatteerd!", command)
       );
     }
   }
 
   /**
    * Koppelt een Game aan een ClientHandler
    * @param game Het spel waaraan de ClientHandler
    *        deelneemt
    */
   public void setGame(Game game) {
     this.game = game;
   }
   
   /**
    * Geeft de naam van van de ClientHandler
    * @return naam van de ClientHandler
    */
   public String getName() {
     return this.name;
   }
 
   /**
    * Stuur een chat bericht naar ALLE andere spelers op de server
    */
   public void broadcast(String msg) {
     this.server.broadcast(this.getName(), msg);
   }
   
   /**
    * De ClientHandler meldt zich af bij de Server en stuurt
    * vervolgens een laatste broadcast naar de Server om te melden
    * dat de Client niet langer deelneemt aan de chatbox.
    */
  private void shutdown() {  
     // Schrijf de ClientHandler uit bij de Server
     server.removeHandler(this);
     // Verwijder de Game-request als deze nog openstond
     Game.revoke(this);
 
     this.broadcast(
       String.format("<%s> heeft de verbinding verbroken", this.getName())
     );
   }
 
   @Override
   public String toString() {
     return this.getName();
   }
 
 }
