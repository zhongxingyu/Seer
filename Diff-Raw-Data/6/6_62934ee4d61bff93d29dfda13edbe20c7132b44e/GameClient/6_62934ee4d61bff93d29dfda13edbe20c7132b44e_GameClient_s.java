 package mygame;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.font.BitmapText;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.network.Client;
 import com.jme3.network.Message;
 import com.jme3.network.MessageListener;
 import com.jme3.network.Network;
 import com.jme3.renderer.RenderManager;
 import com.jme3.system.JmeContext;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mygame.util.NetworkMessages;
 
 /**
  * test
  *
  * @author normenhansen
  */
 public class GameClient extends SimpleApplication {
 
     private static final String SHOOT_BUTTON = "shoot";
     private static final String NEXT_BUTTON = "next";
     private static final Float CAM_HEIGHT = 8f;
     com.jme3.network.Client client;
     private BulletAppState bulletAppState;
     private Map map;
     private Team team1;
     private Team team2;
     private Team currentTeam;
     private int round = 1;
     // GUI
     private BitmapText scoreText;
     private BitmapText playerText;
     private BitmapText timerText;
     private BitmapText roundText;
     
     private Game game;
 
     public static void main(String[] args) {
         GameClient app = new GameClient();
         app.start(JmeContext.Type.Display);
         app.pauseOnFocus = false;
     }
 
     @Override
     public void simpleInitApp() {
         client = null;
         initializeClient();
         instantiateObjects();
         initializeCamera();
         initCrossHairs();
         try {
             client = Network.connectToServer("localhost", AngryPidgeServerMain.PORT);
             client.start();
             NetworkMessages.initializeSerializables();
             client.addMessageListener(new ClientListener());
             client.send(new NetworkMessages.ConnectionMessage());
         } catch (IOException ex) {
             Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         game = new Game(map, timerText, assetManager, rootNode);
     }
 
     @Override
     public void simpleUpdate(float tpf) {
         scoreText.setText(getScoreText());
         playerText.setText(getPlayerText());
         game.update(getTimer(), currentTeam);
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
 
     private void initializeCamera() {
         Vector3f positon = map.getShootingSpot(0).getPosition();
         cam.setLocation(new Vector3f(positon.x, 8f, positon.z));
         cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);
     }
 
     private void initializeClient() {
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         inputManager.addMapping(SHOOT_BUTTON, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         inputManager.addListener(actionListener, SHOOT_BUTTON);
 
         inputManager.addMapping(NEXT_BUTTON, new KeyTrigger(KeyInput.KEY_SPACE));
         inputManager.addListener(actionListener, NEXT_BUTTON);
     }
 
     private void instantiateObjects() {
         map = new Map(assetManager, rootNode, bulletAppState);
         team1 = new Team(1, bulletAppState, assetManager, rootNode);
         team2 = new Team(2, bulletAppState, assetManager, rootNode);
         currentTeam = team1;
         initializeGui();
     }
     private ActionListener actionListener = new ActionListener() {
         public void onAction(String name, boolean isPressed, float tpf) {
             Player currentPlayer = currentTeam.getCurrentPlayer();
             if (name.equals(SHOOT_BUTTON) && !isPressed && currentPlayer.canShoot()) {
                 currentTeam.getCurrentPlayer().shoot(cam.getDirection(), cam.getLocation());
                 roundText.setText("LA RONDE " + round +
                         " EST TERMINÉE\nPATIENTEZ JUSQU'À CE QUE LES AUTRES JOUEURS TERMINENT\nLEUR RONDE.");
             } else if (name.equals(NEXT_BUTTON) && !isPressed) {
                 movePlayerToNextRound();
             }
 
         }
     };
 
     public class ClientListener implements MessageListener<Client> {
 
         public void messageReceived(Client source, Message message) {
             if (message instanceof NetworkMessages.NetworkMessage) {
                 NetworkMessages.NetworkMessage networkMessage = (NetworkMessages.NetworkMessage) message;
                 if (scoreText != null) {
                     scoreText.setText("Client received: '" + networkMessage.getMessage() + "'");
                 }
             }
             if (message instanceof NetworkMessages.GameUpdateMessage) {
                 NetworkMessages.GameUpdateMessage updateMessage = (NetworkMessages.GameUpdateMessage) message;
                 if (scoreText != null) {
                     scoreText.setText("Ronde en cours : " + updateMessage.getPosition() + " || Score équipe 1 = " + updateMessage.getTeam1Score() + " | Score équipe 2 = " + updateMessage.getTeam2Score());
                    movePlayerToNextRound();
                 }
                 client.send(new NetworkMessages.NetworkMessage("message received"));
             }
         }
     }
 
     public void setCameraLocation(Vector3f position) {
         this.cam.setLocation(position);
     }
 
     private void initializeGui() {
         guiNode.detachAllChildren();
         guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
         scoreText = new BitmapText(guiFont, false);
         scoreText.setSize(guiFont.getCharSet().getRenderedSize());
         scoreText.setText(getScoreText());
         scoreText.setLocalTranslation(200, scoreText.getLineHeight(), 0);
         playerText = new BitmapText(guiFont, false);
         playerText.setSize(guiFont.getCharSet().getRenderedSize());
         playerText.setText(getPlayerText());
         playerText.setLocalTranslation(475, 475, 0);
 
         timerText = new BitmapText(guiFont, false);
         timerText.setSize(guiFont.getCharSet().getRenderedSize() * 2);
         timerText.setColor(ColorRGBA.Red);
         timerText.setText("");
         timerText.setLocalTranslation(
                 settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                 settings.getHeight() / 2 + timerText.getLineHeight() / 2 + 200, 0);
 
         roundText = new BitmapText(guiFont, false);
         roundText.setSize(guiFont.getCharSet().getRenderedSize());
         roundText.setLocalTranslation(
                 settings.getWidth() / 2 - 300,
                 settings.getHeight() / 2, 0);
         roundText.setText("");
 
         guiNode.attachChild(scoreText);
         guiNode.attachChild(playerText);
         guiNode.attachChild(timerText);
         guiNode.attachChild(roundText);
 
     }
 
     private void initCrossHairs() {
         BitmapText ch = new BitmapText(guiFont, false);
         ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
         ch.setText("+");
         ch.setLocalTranslation(
                 settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                 settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
         guiNode.attachChild(ch);
     }
 
     private String getPlayerText() {
         return "Équipe : " + currentTeam.getTeamNumber() + " || Joueur : " + currentTeam.getCurrentPlayer().getPlayerNumber();
     }
 
     private String getScoreText() {
         return "Ronde en cours : " + round + " || Score équipe 1 = " + team1.getTeamScore() + " | Score équipe 2 = " + team2.getTeamScore();
     }
 
     private void movePlayerToNextRound() {
         round++;
         Vector3f nextSpotPosition = map.getShootingSpot(round-1).getPosition();
         Vector3f nextCamPosition = new Vector3f(nextSpotPosition.x, CAM_HEIGHT, nextSpotPosition.z);
         cam.setLocation(nextCamPosition);
         roundText.setText("");
        game.setIsCountdownStarted(true);
     }
 }
