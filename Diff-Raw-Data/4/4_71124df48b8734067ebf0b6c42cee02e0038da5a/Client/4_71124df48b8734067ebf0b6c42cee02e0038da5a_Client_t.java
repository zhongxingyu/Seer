 package client;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.Card;
 import model.FriendCards;
 import model.Game;
 import model.GameProperties;
 import model.Play;
 import model.Player;
 import view.View;
 
 public class Client
 {
     private Socket socket;
     private List<Player> players;
     private ObjectOutputStream out;
 
     private View view;
     private Game game;
 
     public Client(View view)
     {
         this.players = new ArrayList<Player>();
         this.view = view;
     }
 
     /**
      * Connects to the server at the specified port and address.
      * 
      * @throws IOException
      */
     public void connect(int port, byte[] address) throws IOException
     {
         socket = new Socket();
         socket.connect(new InetSocketAddress(InetAddress.getByAddress(address),
                 port), 30000);
 
         new Thread()
         {
             public void run()
             {
                 try
                 {
                     out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(
                             socket.getInputStream());
                     request("HELLO", view.name);
 
                     while (true)
                     {
                         Object obj = in.readObject();
                         synchronized (view)
                         {
                             processMessage((Object[]) obj);
                         }
                     }
                 }
                catch (IOException e)
                 {
                     System.out.println("client has closed input stream");
                 }
                 catch (Exception e)
                 {
                     e.printStackTrace();
                 }
                 finally
                 {
                     close();
                 }
             }
         }.start();
         System.out.println("client has connected input stream");
 
         view.joinRoom();
     }
 
     public void close()
     {
         try
         {
             players.clear();
             if (socket != null)
                 socket.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         view.leaveRoom();
     }
 
     public int numPlayers()
     {
         return players.size();
     }
 
     /* Methods called by controller */
 
     public synchronized void requestStartGame(GameProperties properties)
     {
         request("STARTGAME", properties);
         view.requestStartGame();
     }
 
     public synchronized void requestStartRound()
     {
         request("STARTROUND");
         view.requestStartRound();
     }
 
     public synchronized void requestShowCards(List<Card> cards)
     {
         Play play = new Play(view.getPlayerID(), cards);
         if (game.canShowCards(play))
             request("SHOW", play);
         else
             view.notify("Invalid show.");
     }
 
     public synchronized void requestFriendCards(FriendCards friendCards)
     {
         if (game.canSelectFriendCards(view.getPlayerID(), friendCards))
             request("SELECTFRIEND", view.getPlayerID(), friendCards);
     }
 
     public synchronized void requestMakeKitty(List<Card> cards)
     {
         Play play = new Play(view.getPlayerID(), cards);
         if (game.canMakeKitty(play))
             request("MAKEKITTY", play);
         else
             view.notify("Incorrect number of cards.");
     }
 
     public synchronized void requestPlayCards(List<Card> cards)
     {
         Play play = new Play(view.getPlayerID(), cards);
         if (game.canPlay(play))
             request("PLAY", play);
         else
             view.notify("Invalid play.");
     }
 
     private synchronized void request(Object... args)
     {
         try
         {
             out.writeObject(args);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     /* Called after a response from the server */
 
     private synchronized void processMessage(Object... data)
     {
         String command = (String) data[0];
 
         if (command.equals("ADDPLAYER"))
         {
             /* ADDPLAYER [player] */
             Player player = (Player) data[1];
             players.add(player);
         }
         else if (command.equals("YOU"))
         {
             /* YOU [playerID] */
             view.setPlayerID((Integer) data[1]);
         }
         else if (command.equals("REMOVEPLAYER"))
         {
             // TODO the following code is incorrect.
             /* REMOVEPLAYER [playerID] */
             Player removedPlayer = null;
             for (Player player : players)
                 if (players.remove(removedPlayer = player))
                     break;
             if (game != null)
                 game.removePlayer(removedPlayer);
         }
         else if (command.equals("STARTGAME"))
         {
             /* STARTGAME [properties] */
             game = new Game((GameProperties) data[1]);
             game.setView(view);
             game.addPlayers(players);
         }
         else if (command.equals("GAMESTATE"))
         {
             /* GAMESTATE [game] */
             game = (Game) data[1];
             game.setView(view);
         }
         else if (command.equals("STARTROUND"))
         {
             /* STARTROUND [random seed] */
             game.startRound((Long) data[1]);
         }
         else if (command.equals("NOTIFICATION"))
         {
             // TODO notify the view.
         }
         else if (command.equals("DRAW"))
         {
             /* DRAW [player ID] */
             game.drawFromDeck((Integer) data[1]);
         }
 
         else if (command.equals("TAKEKITTY"))
         {
             /* TAKEKITTY */
             game.takeKittyCards();
         }
         else if (command.equals("SHOW"))
         {
             /* SHOW [cards] */
             game.showCards((Play) data[1]);
         }
         else if (command.equals("SELECTFRIEND"))
         {
             /* SELECTFRIEND [player ID] [friend cards] */
             game.selectFriendCards((Integer) data[1], (FriendCards) data[2]);
         }
         else if (command.equals("MAKEKITTY"))
         {
             /* MAKEKITTY [cards] */
             game.makeKitty((Play) data[1]);
         }
         else if (command.equals("PLAY"))
         {
             /* PLAY [cards] */
             game.play((Play) data[1]);
         }
     }
 }
