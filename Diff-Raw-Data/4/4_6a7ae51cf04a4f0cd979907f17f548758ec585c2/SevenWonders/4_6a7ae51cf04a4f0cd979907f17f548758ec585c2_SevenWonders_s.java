 package com.steamedpears.comp3004;
 
 import com.steamedpears.comp3004.routing.*;
 import com.steamedpears.comp3004.views.*;
 import org.apache.log4j.BasicConfigurator;
 
 import org.apache.log4j.*;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class SevenWonders {
    public static final String PATH_RESOURCE = "src/main/resources/";
     public static final String PATH_DATA = PATH_RESOURCE + "data/";
     public static final String PATH_IMG = PATH_RESOURCE + "img/";
     public static final String PATH_IMG_CARDS = PATH_IMG + "cards/";
     public static final String PATH_IMG_WONDERS = PATH_IMG + "wonders/";
     public static final String PATH_CARDS = PATH_DATA + "cards.json";
     public static final String PATH_WONDERS = PATH_DATA + "wonderlist.json";
 
     public static final int MAX_PLAYERS = 7;
     public static final int MIN_PLAYERS = 3;
 
     static Logger logger = Logger.getLogger(SevenWonders.class);
 
     public static void main(String[] args){
         BasicConfigurator.configure();
         new SevenWonders();
     }
 
     ViewFrame view;
     NewGameDialog newGameDialog;
     JDialog dialog;
     Router router;
     PlayerView playerView;
 
     public static enum View { PLAYER_VIEW, HIGHLEVEL_VIEW };
     private View currentView;
 
     // TODO: build this
     HighLevelView highLevelView;
 
     public SevenWonders() {
         view = new ViewFrame();
         openNewGameDialog();
     }
 
     public void createGame(boolean isHost, String ipAddress, int port, int players) {
         logger.info("Creating game");
         closeNewGameDialog();
         if(isHost){
             router = Router.getHostRouter(port, players);
             dialog = new HostGameDialog(view,this);
         }else{
             router = Router.getClientRouter(ipAddress, port);
             dialog = new JoinGameDialog(view,this);
         }
         router.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent changeEvent) {
                 try {
                     updateView();
                 } catch(Exception e) {
                     e.printStackTrace();
                     System.exit(-1);
                 }
             }
         });
         dialog.setVisible(true);
     }
 
     public void startGame() {
         closeDialog();
         selectPlayerView();
         router.beginGame();
     }
 
     private void selectPlayerView() {
         currentView = View.PLAYER_VIEW;
     }
 
     private void updateView() throws Exception {
         switch(currentView) {
             case PLAYER_VIEW:
                 playerView = new PlayerView((router.getLocalGame()).getPlayerById(router.getLocalPlayerId()));
                 view.setView(playerView);
                 break;
             case HIGHLEVEL_VIEW:
                 // do stuff
                 break;
             default:
                 throw new Exception("Unknown view selected");
         }
     }
 
     public void closeDialog() {
         if(dialog == null) return;
         dialog.setVisible(false);
         dialog.dispose();
         dialog = null;
     }
 
     public void openNewGameDialog() {
         closeDialog();
         newGameDialog = new NewGameDialog(view,this);
         newGameDialog.setVisible(true);
     }
 
     public void closeNewGameDialog() {
         if(newGameDialog == null) return;
         newGameDialog.setVisible(false);
         newGameDialog.dispose();
         newGameDialog = null;
     }
 
     public void exit() {
         System.exit(0);
     }
 }
