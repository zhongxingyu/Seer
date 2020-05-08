 package org.train.state;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.train.app.Game;
 import org.train.entity.Level;
 import org.train.entity.Menu;
 import org.train.entity.MenuItem;
 import org.train.entity.MessageBox;
 import org.train.factory.EffectFactory;
 import org.train.helper.LevelHelper;
 import org.train.other.LevelController;
 import org.train.other.ResourceManager;
 import org.train.other.Translator;
 
 public class GameState extends BasicGameState {
 
     private int stateId;
     private Level level = null;
     private Menu menu = null;
     private Menu gameOverMenu = null;
     private Translator translator;
     private LevelController levelController;
     private MessageBox messageBox;
     private boolean wasFinished = false;
 
     public GameState(int stateId) {
         this.stateId = stateId;
     }
 
     @Override
     public void init(final GameContainer container, final StateBasedGame game)
             throws SlickException {
         this.levelController = this.container.getComponent(LevelController.class);
         this.translator = this.container.getComponent(Translator.class);
         this.messageBox = this.container.getComponent(MessageBox.class);
         this.messageBox.setBackgroundColor(Color.lightGray);
         this.initMenuItems(container, game);
         this.initGameOverMenuItems(container, game);
         this.initLevel(container, game);
         this.menu.close();
         this.gameOverMenu.close();
     }
 
     private void initMenuItems(final GameContainer container, final StateBasedGame game) {
         MenuItem continueItem = new MenuItem(this.translator.translate("Game.Menu.Continue"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent arg0) {
                         GameState.this.menu.close();
                         GameState.this.gameOverMenu.close();
                     }
                 });
         MenuItem repeatLevel = new MenuItem(this.translator.translate("Game.Menu.RepeatLevel"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.initLevel(container, game);
                         GameState.this.menu.close();
                         GameState.this.gameOverMenu.close();
                     }
                 });
         MenuItem subMenu = new MenuItem(this.translator.translate("Game.Menu.Menu"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.menu.close();
                         GameState.this.gameOverMenu.close();
                         game.enterState(Game.MENU_FOR_GAME_STATE);
                     }
                 });
         MenuItem mainMenu = new MenuItem(this.translator.translate("Game.Menu.MainMenu"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.menu.close();
                         GameState.this.gameOverMenu.close();
                         game.enterState(Game.MENU_STATE);
                     }
                 });
         List<MenuItem> menuItems = new ArrayList<MenuItem>();
         menuItems.add(continueItem);
         menuItems.add(repeatLevel);
         menuItems.add(subMenu);
         menuItems.add(mainMenu);
         for (MenuItem item : menuItems) {
             item.setMargin(30);
         }
         this.menu = new Menu(menuItems, container,
                 this.container.getComponent(ResourceManager.class),
                 this.container.getComponent(EffectFactory.class));
         this.menu.setBackgroundColor(Color.lightGray);
     }
 
     private void initGameOverMenuItems(final GameContainer container, final StateBasedGame game) {
         MenuItem repeatLevel = new MenuItem(this.translator.translate("Game.Menu.RepeatLevel"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.initLevel(container, game);
                         GameState.this.menu.close();
                         GameState.this.gameOverMenu.close();
                     }
                 });
         MenuItem subMenu = new MenuItem(this.translator.translate("Game.Menu.Menu"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.menu.close();
                         GameState.this.gameOverMenu.close();
                         game.enterState(Game.MENU_FOR_GAME_STATE);
                     }
                 });
         MenuItem mainMenu = new MenuItem(this.translator.translate("Game.Menu.MainMenu"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.menu.close();
                         GameState.this.gameOverMenu.close();
                         game.enterState(Game.MENU_STATE);
                     }
                 });
 
         List<MenuItem> gameOverMenuItems = new ArrayList<MenuItem>();
         gameOverMenuItems.add(repeatLevel);
         gameOverMenuItems.add(subMenu);
         gameOverMenuItems.add(mainMenu);
         for (MenuItem item : gameOverMenuItems) {
             item.setMargin(30);
         }
         this.gameOverMenu = new Menu(gameOverMenuItems, container,
                 this.container.getComponent(ResourceManager.class),
                 this.container.getComponent(EffectFactory.class));
         this.gameOverMenu.setBackgroundColor(Color.lightGray);
     }
 
     private void initLevel(GameContainer container, final StateBasedGame game) {
         try {
             this.wasFinished = false;
             this.level = this.levelController.getCurrentLevel();
             int itemSize = this.level.getOriginalImageSize();
             LevelHelper levelHelper = this.container.getComponent(LevelHelper.class);
             float scale = levelHelper.computeScale(container, this.level.getOriginalImageSize(),
                     new Dimension(this.level.getWidth(), this.level.getHeight()));
             this.level.setScale(scale);
             int width = this.level.getWidth() * (int) (itemSize * scale);
             int height = this.level.getHeight() * (int) (itemSize * scale);
             this.level.setMarginLeft((container.getWidth() - width) / 2);
             this.level.setMarginTop((container.getHeight() - height) / 2);
             if (!this.level.isValid()) {
                 this.messageBox.showConfirm(this.translator.translate("Game.LevelIsInvalid"),
                         new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent arg0) {
                                 game.enterState(Game.EDITOR_STATE);
                             }
                         }, new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 game.enterState(Game.MENU_FOR_GAME_STATE);
                             }
                         });
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g)
             throws SlickException {
         this.level.render(container, game, g);
         this.menu.render(container, game, g);
         this.gameOverMenu.render(container, game, g);
         this.messageBox.render(container, game, g);
     }
 
     @Override
     public void update(final GameContainer container, final StateBasedGame game, int delta)
             throws SlickException {
         Input input = container.getInput();
         if (!this.menu.isShowed() && input.isKeyPressed(Input.KEY_ESCAPE)
                 && !this.level.isFinished() && !this.level.isOver()) {
             this.menu.show();
         }
         if (this.level.isOver()) {
             this.gameOverMenu.show();
         }
         if (this.level.isFinished()) {
             if (this.levelController.nextLevelExist()) {
                 if (!this.wasFinished) {
                     this.wasFinished = true;
                     this.levelController.updateProgress();
                 }
                 this.messageBox.showConfirm(this.translator.translate("Game.LevelFinished"),
                         new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 GameState.this.levelController.loadNextLevel();
                                 GameState.this.initLevel(container, game);
                             }
                         }, new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 game.enterState(Game.MENU_STATE);
                             }
                         });
                 if (input.isKeyPressed(Input.KEY_ENTER)
                         || input.isKeyPressed(Input.KEY_NUMPADENTER)) {
                     this.levelController.loadNextLevel();
                     this.initLevel(container, game);
                     this.messageBox.close();
                 }
             } else {
                 this.messageBox.showConfirm(this.translator.translate("Game.Congratulation"),
                         new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 game.enterState(Game.MENU_FOR_GAME_STATE);
                             }
                         }, new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 game.enterState(Game.MENU_STATE);
                             }
                         });
             }
         }
         this.menu.update(container, game, delta);
         this.gameOverMenu.update(container, game, delta);
         if (this.menu.isShowed()) {
             if (input.isKeyPressed(Input.KEY_ESCAPE)) {
                 this.menu.close();
             }
         } else if (!this.gameOverMenu.isShowed()) {
             this.level.update(container, game, delta);
         }
         this.messageBox.update(container, game, delta);
         input.clearKeyPressedRecord();
     }
 
     @Override
     public int getID() {
         return this.stateId;
     }
 
 }
