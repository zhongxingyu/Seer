 package se.samuelandersson.clicky.screen;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import se.samuelandersson.clicky.Clicky;
 import se.samuelandersson.clicky.game.ClickyGame;
 import se.samuelandersson.clicky.game.GameListener;
 import se.samuelandersson.clicky.game.Highscores;
 import se.samuelandersson.clicky.manager.MusicManager.Song;
 
 public class GameScreen extends AbstractScreen implements GameListener
 
 {
     private ClickyGame game;
 
     private boolean running;
     private boolean enteringHighscore;
 
     private Table table;
     private Label accuracyLabel;
     private Label scoreLabel;
     private Label startGameLabel;
 
     public GameScreen(Clicky app)
     {
         super(app);
     }
 
     @Override
     public InputMultiplexer getInputMultiplexer()
     {
         if (inputMultiplexer == null) {
             inputMultiplexer = new InputMultiplexer();
             inputMultiplexer.addProcessor(stage);
             inputMultiplexer.addProcessor(this);
         }
         return inputMultiplexer;
     }
 
     @Override
     public void resize(int width, int height)
     {
         super.resize(width, height);
         table.invalidate();
         startGameLabel.setX(width / 2 - startGameLabel.getWidth() / 2);
     }
 
     @Override
     public void show()
     {
         super.show();
         running = true;
         app.getMusicManager().play(Song.GAME);
     }
 
     public void reset()
     {
        game.reset();
        stage.clear();
        initialize();
     }
 
     @Override
     public void initialize()
     {
         table = new Table(getSkin());
         accuracyLabel = new Label("", getSkin());
         scoreLabel = new Label("", getSkin());
         startGameLabel = new Label("Press space to start", getSkin(), "burning");
 
         table.setFillParent(true);
         table.defaults().width(200);
         table.align(Align.top);
         table.add(accuracyLabel);
         table.add(scoreLabel);
         stage.addActor(table);
         stage.addActor(startGameLabel);
         if (game != null)
             game.dispose();
         game = new ClickyGame(app, getStage());
         game.initialize();
         game.addListener(this);
     }
 
     @Override
     public void update(float delta)
     {
         if (!game.isPaused()) {
             stage.act(delta);
             game.update(delta);
         }
         updateLabels();
     }
 
     private void updateLabels()
     {
         float acc = game.getClicks() > 0 ? game.getHits()
                 / (float) game.getClicks() : 0;
         accuracyLabel.setText("Acc: " + (int) (acc * 100) + "%");
        scoreLabel.setText("Score: " + game.getScore());
     }
 
     @Override
     public void gameOver()
     {
         table.align(Align.center);
         table.clear();
         scoreLabel.setStyle(getSkin().get("burning", LabelStyle.class));
         table.add(scoreLabel);
         table.row();
         table.add(accuracyLabel);
         table.invalidate();
         if (!enteringHighscore) {
             startGameLabel.setText("Press space to play again");
             centerX(startGameLabel);
             startGameLabel.setVisible(true);
         }
     }
 
     @Override
     public void gameStarted()
     {
         table.align(Align.top);
         table.clear();
         scoreLabel.setStyle(getSkin().get("default", LabelStyle.class));
         table.add(accuracyLabel);
         table.add(scoreLabel);
         table.invalidate();
         startGameLabel.setVisible(false);
     }
 
     private void centerX(Actor l)
     {
         l.setX(Gdx.graphics.getWidth() / 2 - l.getWidth() / 2);
     }
 
     @Override
     public void newHighscore(final int score)
     {
         enteringHighscore = true;
         final Label l = new Label("You qualified for the highscore!",
                 getSkin(), "burning");
         final Label l2 = new Label("Enter your name", getSkin());
         final TextField field = new TextField("", getSkin());
         field.setWidth(300);
         centerX(field);
         centerX(l);
         centerX(l2);
         field.setY(Gdx.graphics.getHeight() * 2 / 3);
         l2.setY(field.getY() + field.getHeight() + 5);
         l.setY(l2.getY() + l2.getHeight() + 5);
         stage.addActor(field);
         stage.addActor(l);
         stage.addActor(l2);
         field.toFront();
         field.addListener(new InputListener()
         {
             @Override
             public boolean keyDown(InputEvent e, int key)
             {
                 if (key == Keys.ENTER) {
                     Highscores.addHighscore(field.getText(), score);
                     field.removeListener(this);
                     field.remove();
                     l.remove();
                    reset();
                     running = false;
                     enteringHighscore = false;
                    app.setScreen(app.getHighscoreScreen());
                     return true;
                 }
                 return false;
             }
         });
     }
 
     @Override
     public void render()
     {
         super.render();
         stage.draw();
         game.render(getBatch());
         Table.drawDebug(stage);
     }
 
     public boolean isRunning()
     {
         return running;
     }
 
     @Override
     public boolean keyUp(int key)
     {
         if (key == Keys.F3) {
             Clicky.DEBUGDRAW = !Clicky.DEBUGDRAW;
             return true;
         }
 
         if (key == Keys.SPACE) {
             if (!game.isStarted() && !enteringHighscore) {
                 game.reset();
                 game.start();
             }
         }
 
         if (key == Keys.ESCAPE) {
             app.setScreen(app.getMenuScreen());
             return true;
         }
 
         if (key == Keys.P) {
             game.setPaused(!game.isPaused());
             return true;
         }
         return false;
     }
 
 }
