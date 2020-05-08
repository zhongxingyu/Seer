 package vooga.rts.gui.menus;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import vooga.rts.gui.Menu;
 import vooga.rts.gui.Window;
 import vooga.rts.networking.client.ClientModel;
 import vooga.rts.networking.communications.ExpandedLobbyInfo;
 import vooga.rts.networking.server.MatchmakerServer;
 
 
 public class MultiMenu extends Menu implements Observer {
 
     private JFrame myFrame;
     private ClientModel myClientModel;
 
     public MultiMenu (JFrame f) {
         myFrame = f;
        MatchmakerServer server = new MatchmakerServer();
        server.startAcceptingConnections();
         List<String> factions = new ArrayList<String>();
         factions.add("protoss");
         factions.add("zerg");
         List<String> maps = new ArrayList<String>();
         maps.add("map1");
         maps.add("map2");
         List<Integer> maxPlayers = new ArrayList<Integer>();
         maxPlayers.add(4);
         maxPlayers.add(6);
        // myClientModel = new ClientModel(null, "Test Game", "User 1", factions, maps, maxPlayers);
 
     }
 
     public void setFrame () {        
         myFrame.setContentPane(myClientModel.getView());
         myFrame.setVisible(true);
     }
     
     public void unsetFrame() {
         myFrame.remove(myClientModel.getView());
     }
     
     @Override
     public void paint (Graphics2D pen) {
 
 
     }
 
     public void handleMouseDown (int x, int y) {
         myFrame.remove(myClientModel.getView());
         setChanged();
         notifyObservers();
     }
 
     @Override
     public void update (Observable o, Object a) {
         if (o instanceof ClientModel) {
             ClientModel c = (ClientModel) o;
             ExpandedLobbyInfo e = (ExpandedLobbyInfo) a;
         }
         setChanged();
         notifyObservers();
     }
 }
