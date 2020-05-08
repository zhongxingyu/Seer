 package view;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.GroupLayout.Group;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import model.Card;
 import model.FriendCards;
 import model.Game;
 import model.Play;
 import model.Trick;
 
 public class HumanView extends View
 {
     private Game game;
     private JFrame frame;
 
     private JTextField notificationField;
     private GamePanel gamePanel;
 
     private JButton createRoomButton, closeRoomButton, joinRoomButton,
             leaveRoomButton, newGameButton, newRoundButton;
     private JButton[] buttons;
 
     private JButton actionButton;
 
     public HumanView(String name)
     {
         super(name);
 
         frame = new JFrame("Tractor");
         frame.getContentPane().setBackground(Color.GREEN);
 
         notificationField = new JTextField("Welcome to Tractor");
         notificationField.setEditable(false);
 
         createRoomButton = new JButton("Create Room");
         createRoomButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 try
                 {
                     server.startServer(3003);
                 }
                 catch (IOException e2)
                 {
                     JOptionPane.showMessageDialog(frame, e2.getMessage());
                 }
             }
         });
         closeRoomButton = new JButton("Close Room");
         closeRoomButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 if (JOptionPane.showConfirmDialog(frame,
                         "Are you sure you want to close the room?") == JOptionPane.YES_OPTION)
                 {
                     server.close();
                 }
             }
         });
         joinRoomButton = new JButton("Join Room");
         joinRoomButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
 //                String input = JOptionPane
 //                        .showInputDialog("Enter IP: e.g. 192.168.0.1");
                 String input = "127.0.0.1";
                 // TODO Set Cancel to go back to original state.
 
                 try
                 {
                     /* Try to parse input IP */
                     String[] addressStr = input.split("\\.");
                     byte[] address = new byte[4];
                     for (int i = 0; i < 4; i++)
                         address[i] = (byte) Short.parseShort(addressStr[i]);
 
                     /* Connect to server */
                     client.connect(3003, address);
                 }
                 catch (Exception e2)
                 {
                     JOptionPane.showMessageDialog(frame, e2.getMessage());
                 }
             }
         });
         leaveRoomButton = new JButton("Leave Room");
         leaveRoomButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 if (JOptionPane.showConfirmDialog(frame,
                         "Are you sure you want to leave the room?") == JOptionPane.YES_OPTION)
                 {
                     client.close();
                 }
             }
         });
         newGameButton = new JButton("New Game");
         newGameButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 new GamePropertiesForm(frame, client).setVisible(true);
             }
         });
         newRoundButton = new JButton("New Round");
         newRoundButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 client.requestStartRound();
             }
         });
         buttons = new JButton[]
         { createRoomButton, closeRoomButton, joinRoomButton, leaveRoomButton,
                 newGameButton, newRoundButton };
         for (JButton button : buttons)
             button.setVisible(false);
         createRoomButton.setVisible(true);
         joinRoomButton.setVisible(true);
 
         actionButton = new JButton();
         actionButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 if (game != null && game.started())
                 {
                     List<Card> cards = gamePanel.resetSelected();
                     if (cards.isEmpty())
                         return;
                     switch (game.getState())
                     {
                         case AWAITING_SHOW:
                             client.requestShowCards(cards);
                             break;
                         case AWAITING_FRIEND_CARDS:
                             client.requestShowCards(cards);
                             break;
                         case AWAITING_KITTY:
                             if (getPlayerID() == game.getMaster().ID)
                             {
                                 client.requestMakeKitty(cards);
                                 break;
                             }
                         case AWAITING_PLAY:
                             if (getPlayerID() == game.getCurrentPlayer().ID)
                             {
 
                                 client.requestPlayCards(cards);
                                 break;
                             }
                         case AWAITING_RESTART:
                             break;
                     }
                 }
             }
         });
         actionButton.setVisible(false);
 
         gamePanel = new GamePanel(this);
 
         frame.setSize(900, 720);
         frame.setResizable(false);
 
         arrange();
     }
 
     private void arrange()
     {
         GroupLayout layout = new GroupLayout(frame.getContentPane());
 
         Group sequentialGroup = layout.createSequentialGroup();
         Group parallelGroup = layout.createParallelGroup(Alignment.BASELINE);
         sequentialGroup.addComponent(notificationField);
         parallelGroup.addComponent(notificationField);
         for (JButton button : buttons)
         {
             sequentialGroup.addComponent(button);
             parallelGroup.addComponent(button);
         }
         layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
                 .addGroup(sequentialGroup).addComponent(gamePanel)
                 .addComponent(actionButton));
         layout.setVerticalGroup(layout.createSequentialGroup()
                 .addGroup(parallelGroup).addComponent(gamePanel)
                 .addComponent(actionButton));
 
         frame.getContentPane().setLayout(layout);
     }
 
     public void start()
     {
         try
         {
             gamePanel.loadImages();
         }
         catch (IOException e)
         {
             JOptionPane.showMessageDialog(frame,
                     "Error: could not load card images.");
         }
         frame.setVisible(true);
         frame.setFocusable(true);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
 
     public void createRoom()
     {
         try
         {
             /* Find external IP */
             String IP = new BufferedReader(new InputStreamReader(new URL(
                     "http://api.exip.org/?call=ip").openStream())).readLine();
             notificationField.setText("Setting up server...");
 
             notificationField.setText("Your IP is " + IP + ". Players:");
 
             createRoomButton.setVisible(false);
             closeRoomButton.setVisible(true);
             frame.repaint();
         }
         catch (Exception e)
         {
             JOptionPane.showMessageDialog(frame, e.getMessage());
         }
     }
 
     public void closeRoom()
     {
         closeRoomButton.setVisible(false);
         createRoomButton.setVisible(true);
         frame.repaint();
     }
 
     public void joinRoom()
     {
         notificationField.setText("Joined room.");
         joinRoomButton.setVisible(false);
         leaveRoomButton.setVisible(true);
         newGameButton.setVisible(true);
         newRoundButton.setVisible(true);
         frame.repaint();
     }
 
     public void leaveRoom()
     {
         notificationField.setText("Left room.");
         leaveRoomButton.setVisible(false);
         joinRoomButton.setVisible(true);
         newGameButton.setVisible(false);
         newRoundButton.setVisible(false);
         frame.repaint();
     }
 
     public void requestStartGame()
     {
         notificationField.setText("Sending new game request...");
         frame.repaint();
     }
 
     public void startGame(Game game)
     {
         notificationField
                 .setText("New game started. Click 'New Round' to begin.");
         actionButton.setVisible(false);
         this.game = game;
         gamePanel.setGame(game);
         frame.repaint();
     }
 
     public void requestStartRound()
     {
         notificationField.setText("Waiting for other players...");
         frame.repaint();
     }
 
     public void startRound()
     {
         notificationField.setText("New round started.");
         actionButton.setText("SHOW");
         actionButton.setVisible(true);
        gamePanel.moveCardsToDeck();
         frame.repaint();
     }
 
     public void requestFriendCards(int numFriends)
     {
         if (getPlayerID() == game.getMaster().ID)
             new FriendCardsForm(frame, client, game.getProperties().numDecks,
                     numFriends).setVisible(true);
         else
             notificationField
                     .setText("Waiting for friend cards to be selected.");
     }
 
     public void notifyCanMakeKitty(int kittySize)
     {
         if (getPlayerID() == game.getMaster().ID)
         {
             notificationField.setText("Select " + kittySize + " cards.");
             actionButton.setText("MAKE KITTY");
             actionButton.setVisible(true);
         }
         else
             actionButton.setVisible(false);
     }
 
     public void drawCard(Card card, int playerID)
     {
         gamePanel.moveCardToHand(card, playerID);
         frame.repaint();
     }
 
     public void showCards(Play play)
     {
         for (Card card : play.getCards())
             gamePanel.moveCardToTable(card, play.getPlayerID());
         frame.repaint();
     }
 
     public void selectFriendCards(FriendCards friendCards)
     {
         // TODO
     }
 
     public void makeKitty(Play play)
     {
         for (Card card : play.getCards())
             gamePanel.moveCardAway(card, play.getPlayerID());
         beforeTurn();
     }
 
     public void playCards(Play play)
     {
         for (Card card : play.getCards())
             gamePanel.moveCardToTable(card, play.getPlayerID());
         beforeTurn();
     }
 
     public void finishTrick(final Trick trick, final int winnerID)
     {
         gamePanel.showPreviousTrick(true);
         /* Delay a while before drawing the trick finish. */
         new Timer().schedule(new TimerTask()
         {
             public void run()
             {
                 for (Play play : trick.getPlays())
                     for (Card card : play.getCards())
                         gamePanel.moveCardAway(card, winnerID);
                 gamePanel.showPreviousTrick(false);
             }
         }, 2000);
     }
 
     public void endRound()
     {
         actionButton.setVisible(false);
         Play kitty = game.getKitty();
         for (Card card : kitty.getCards())
             gamePanel.moveCardToTable(card, kitty.getPlayerID());
     }
 
     public void notify(String notification)
     {
         // TODO make this a temporary message.
         notificationField.setText(notification);
     }
 
     public void repaint()
     {
         frame.repaint();
     }
 
     private void beforeTurn()
     {
         if (game.canStartNewRound())
         {
             notificationField.setText("Click \"Start a New Round\".");
             actionButton.setVisible(false);
         }
         else if (getPlayerID() == game.getCurrentPlayer().ID)
         {
             notificationField.setText("Your turn.");
             actionButton.setText("PLAY");
             actionButton.setVisible(true);
         }
         else
         {
             notificationField
                     .setText(game.getCurrentPlayer().name + "'s turn.");
             actionButton.setVisible(false);
         }
     }
 }
