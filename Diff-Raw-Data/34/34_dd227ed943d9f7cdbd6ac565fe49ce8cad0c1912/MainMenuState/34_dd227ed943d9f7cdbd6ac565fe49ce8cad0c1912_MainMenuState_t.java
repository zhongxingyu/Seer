 package aigilas.states;
 
 import aigilas.net.Client;
 import aigilas.net.LocalClient;
 import aigilas.net.Server;
 import aigilas.ui.SelectableButton;
 import aigilas.ui.UiAssets;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import org.apache.commons.io.FileUtils;
 import sps.bridge.Contexts;
 import sps.bridge.Sps;
 import sps.core.Loader;
 import sps.core.Logger;
 import sps.graphics.Assets;
 import sps.graphics.Renderer;
 import sps.io.Input;
 import sps.states.StateManager;
 
 import java.io.File;
 
 public class MainMenuState extends MenuState {
     public MainMenuState() {
         super();
         Server.shutdown();
         Client.reset(new LocalClient());
        Input.get().setup(Client.get());
 
         Label.LabelStyle lblStyle = new Label.LabelStyle(Assets.get().font(), Color.WHITE);
         Label title = new Label("Aigilas", lblStyle);
 
         final SelectableButton startGameBtn = new SelectableButton("Start Local", UiAssets.getButtonStyle());
         startGameBtn.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 Logger.info("Starting the game");
                 Client.get().startGame();
             }
         });
 
         SelectableButton startServerBtn = new SelectableButton("Host LAN", UiAssets.getButtonStyle());
         startServerBtn.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 StateManager.loadState(new StartHostServerState());
             }
         });
 
         SelectableButton connectToServerBtn = new SelectableButton("Join LAN", UiAssets.getButtonStyle());
         connectToServerBtn.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 StateManager.loadState(new JoinServerAsGuestState());
             }
         });
 
         SelectableButton helpBtn = new SelectableButton("Help", UiAssets.getButtonStyle());
         helpBtn.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 StateManager.loadState(new HelpMenuState());
             }
         });
 
         SelectableButton optionsBtn = new SelectableButton("Options", UiAssets.getButtonStyle());
         optionsBtn.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 StateManager.loadState(new OptionsState());
             }
         });
 
         SelectableButton exitBtn = new SelectableButton("Exit", UiAssets.getButtonStyle());
         exitBtn.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 Gdx.app.exit();
             }
         });
 
         add(startGameBtn);
         add(startServerBtn);
         add(connectToServerBtn);
         table.row();
         add(helpBtn);
         add(optionsBtn);
         add(exitBtn);
 
         title.setY((int) (Renderer.get().VirtualHeight * .8));
         title.setX((int) (Renderer.get().VirtualWidth * .4));
         stage.addActor(title);
 
         File versionDat = Loader.get().data("version.dat");
         if (versionDat.exists()) {
             try {
                 String versionText = FileUtils.readFileToString(versionDat);
                 Label version = new Label("Version: " + versionText, lblStyle);
                 stage.addActor(version);
             }
             catch (Exception e) {
                 Logger.info("WARNING: No version information found at " + versionDat.getAbsolutePath());
             }
         }
         startGameBtn.setSelected(true);
     }
 
     @Override
     public void update() {
         super.update();
         if (Client.get().isGameStarting()) {
             for (int ii = 0; ii < Client.get().getPlayerCount(); ii++) {
                 Input.get().setContext(Contexts.get(Sps.Contexts.Free), ii);
             }
             StateManager.loadState(new LoadingState());
         }
     }
 
     @Override
     public String getName() {
         return "MainMenuState";
     }
 }
