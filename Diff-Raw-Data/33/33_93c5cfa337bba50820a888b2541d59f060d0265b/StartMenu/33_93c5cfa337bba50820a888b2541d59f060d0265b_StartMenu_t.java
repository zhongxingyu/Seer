 package dungeon.ui.screens;
 
 import dungeon.messages.LifecycleEvent;
 import dungeon.client.Client;
 import dungeon.ui.messages.ShowLobby;
 
 import javax.swing.*;
 import javax.swing.event.MouseInputAdapter;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 
 /**
  * The start menu that shows things like a start and quit button.
  */
 public class StartMenu extends JPanel {
   private final JTextField nameField = new JTextField("Link");
 
   private final JButton startButton = new JButton("Lokales Spiel starten");
 
   private final JButton startNetworkButton = new JButton("Lobby erstellen");
 
   private final JButton joinNetworkButton = new JButton("Lobby beitreten");
 
   private final JButton quitButton = new JButton("Beenden");
 
   private final Client client;
 
   public StartMenu (Client client) {
     super(new GridLayout(5, 1));
 
     this.client = client;
 
     this.add(this.nameField);
     this.add(this.startButton);
     this.add(this.startNetworkButton);
     this.add(this.joinNetworkButton);
     this.add(this.quitButton);
 
     this.startButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         StartMenu.this.client.setPlayerName(StartMenu.this.nameField.getText());
 
         try {
           StartMenu.this.client.startServer(6077);
 
           StartMenu.this.client.sendReady();
         } catch (Client.ServerStartException ex) {
           JOptionPane.showMessageDialog(null, "Spiel konnte nicht gestartet werden");
         } catch (Client.ConnectException ex) {
           JOptionPane.showMessageDialog(null, "Spiel konnte nicht gestartet werden");
         }
       }
     });
 
     this.startNetworkButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         StartMenu.this.client.setPlayerName(StartMenu.this.nameField.getText());
 
         try {
           StartMenu.this.client.startServer(6077);
 
           StartMenu.this.client.send(new ShowLobby(StartMenu.this.client.getPlayerId()));
         } catch (Client.ServerStartException ex) {
           JOptionPane.showMessageDialog(null, "Lobby konnte nicht erstellt werden");
         } catch (Client.ConnectException ex) {
           JOptionPane.showMessageDialog(null, "Lobby konnte nicht erstellt werden");
         }
       }
     });
 
     this.joinNetworkButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         StartMenu.this.client.setPlayerName(StartMenu.this.nameField.getText());
 
         String server = "localhost";
 
         server = JOptionPane.showInputDialog(null, "Adresse des Servers", server);
 
         if (server == null) {
           return;
         }
 
         try {
           StartMenu.this.client.connect(server, 6077);
 
           StartMenu.this.client.send(new ShowLobby(StartMenu.this.client.getPlayerId()));
        } catch (Client.ConnectException ex) {
           JOptionPane.showMessageDialog(null, "Es konnte keine Verbindung hergestellt werden");
         }
       }
     });
 
     this.quitButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         StartMenu.this.client.send(LifecycleEvent.SHUTDOWN);
       }
     });
   }
 }
