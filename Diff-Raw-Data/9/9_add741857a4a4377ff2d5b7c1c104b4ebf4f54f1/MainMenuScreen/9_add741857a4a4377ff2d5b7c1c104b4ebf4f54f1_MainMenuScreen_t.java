 package pt.up.fe.lpoo.bombermen.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
 
 import java.io.IOException;
 
 import pt.up.fe.lpoo.bombermen.Assets;
 import pt.up.fe.lpoo.bombermen.Bombermen;
 import pt.up.fe.lpoo.bombermen.Constants;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 
 public class MainMenuScreen implements Screen
 {
     private Bombermen _game;
 
     // Actors
     private Image _backgroundImage;
     private Group _uiGroup;
 
     public MainMenuScreen(Bombermen game)
     {
         _game = game;
     }
 
     @Override
     public void render(float delta)
     {
         Gdx.gl.glClearColor(0, 0, 0, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
         _game.GetStage().act(delta);
         _game.GetStage().draw();
     }
 
     @Override
     public void resize(int width, int height)
     {
         _game.GetStage().setViewport(Constants.DEFAULT_WIDTH, Constants.DEFAULT_HEIGHT, true);
         _game.GetStage().getCamera().translate(-_game.GetStage().getGutterWidth(), -_game.GetStage().getGutterHeight(), 0);
     }
 
     @Override
     public void show()
     {
         _uiGroup = new Group();
 
         _backgroundImage = new Image(Assets.GetTexture("background"));
         _backgroundImage.setBounds(0, 0, _game.GetStage().getWidth(), _game.GetStage().getHeight());
         _backgroundImage.setTouchable(Touchable.disabled);
         _game.GetStage().addActor(_backgroundImage);
 
         TextButton playButton = new TextButton("Play", _game.GetSkin());
         TextButton createServerButton = new TextButton("Create Server", _game.GetSkin());
         TextButton settingsButton = new TextButton("Settings", _game.GetSkin());
         TextButton exitButton = new TextButton("Exit", _game.GetSkin());
 
         Table buttonsTable = new Table(_game.GetSkin());
         buttonsTable.add(playButton).left();
         buttonsTable.row();
         buttonsTable.add().height(10);
         buttonsTable.row();
         buttonsTable.add(createServerButton).left();
         buttonsTable.row();
         buttonsTable.add().height(10);
         buttonsTable.row();
         buttonsTable.add(settingsButton).left();
         buttonsTable.row();
         buttonsTable.add().height(10);
         buttonsTable.row();
         buttonsTable.add(exitButton).left();
         buttonsTable.setPosition(80, 100);
         _uiGroup.addActor(buttonsTable);
 
         Label feupLabel = new Label("FEUP 2013/2013 - MIEIC", _game.GetSkin());
         feupLabel.setPosition(620, 5);
         _uiGroup.addActor(feupLabel);
 
         playButton.addListener(new ChangeListener()
         {
             @Override
             public void changed(ChangeEvent event, Actor actor)
             {
                 Assets.PlaySound("menu_select");
 
                 _game.setScreen(_game.GetSelectServerScreen());
             }
         });
 
         createServerButton.addListener(new ChangeListener()
         {
             @Override
             public void changed(ChangeEvent event, Actor actor)
             {
                 Assets.PlaySound("menu_select");
 
                 try
                 {
                    Runtime.getRuntime().exec("java -classpath ..\\bombermen-server\\bin;..\\bombermen\\bin;..\\bombermen-server\\forms-1.3.0.jar pt.up.fe.lpoo.bombermen.gui.BombermenServerGui");
                 }
                 catch (IOException e)
                 {
                     Dialog dialog = new Dialog("Error", _game.GetSkin());
                     dialog.text("Could not launch server process.\nDetails printed to console.");
                     dialog.button("Okay");
                     dialog.pack();
                     dialog.setPosition(800/2 - dialog.getWidth() / 2, 480/2 - dialog.getHeight() / 2);
                     _game.GetStage().addActor(dialog);
 
                     e.printStackTrace();
                 }
             }
         });
 
         settingsButton.addListener(new ChangeListener()
         {
             @Override
             public void changed(ChangeEvent event, Actor actor)
             {
                 Assets.PlaySound("menu_select");
 
                 _game.setScreen(_game.GetSettingsScreen());
             }
         });
 
         exitButton.addListener(new ChangeListener()
         {
             @Override
             public void changed(ChangeEvent event, Actor actor)
             {
                 Assets.PlaySound("menu_select");
 
                 Gdx.app.exit();
             }
         });
 
         if (Gdx.app.getType() != ApplicationType.Desktop)
             createServerButton.setDisabled(true);
 
         _uiGroup.addAction(sequence(alpha(0), fadeIn(1)));
         _game.GetStage().addActor(_uiGroup);
     }
 
     @Override
     public void hide()
     {
         _uiGroup.addAction(sequence(fadeOut(1), removeActor()));
         _backgroundImage.remove();
     }
 
     @Override
     public void pause()
     {
     }
 
     @Override
     public void resume()
     {
     }
 
     @Override
     public void dispose()
     {
     }
 }
