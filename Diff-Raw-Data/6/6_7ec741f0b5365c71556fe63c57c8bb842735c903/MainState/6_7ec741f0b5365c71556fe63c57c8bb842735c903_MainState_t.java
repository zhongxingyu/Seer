 package jam.ld24.game;
  
 import infinitedog.frisky.events.InputEvent;
 import infinitedog.frisky.game.ManagedGameState;
 import jam.ld24.entities.Avatar;
 import jam.ld24.entities.Zombie;
 import jam.ld24.tiles.TileSet;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class MainState extends ManagedGameState {
     private boolean paused = false;
     Avatar ivan;
     Zombie david;
     
     public MainState(int stateID)
     {
         super(stateID);
         em.setGameState(C.States.MAIN_STATE.name);
     }
 
     @Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
         em.setGameState(C.States.MAIN_STATE.name);
         
         evm.addEvent(C.Events.CLOSE_WINDOW.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_ESCAPE));
         
         //Player movement
         evm.addEvent(C.Events.MOVE_LEFT.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_A));
         evm.addEvent(C.Events.MOVE_RIGHT.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_D));
         evm.addEvent(C.Events.MOVE_UP.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_W));
         evm.addEvent(C.Events.MOVE_DOWN.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_S));
                 
         tm.getInstance().addTexture(C.Textures.ZOMBIE.name, C.Textures.ZOMBIE.path);
         tm.getInstance().addTexture(C.Textures.AVATAR.name, C.Textures.AVATAR.path);
         
         // Init zombies
         ivan = new Avatar();
         david = new Zombie();
         
         ivan.setPosition(new Vector2f(50, 50));
         david.setPosition(new Vector2f(100, 100));
         
         em.addEntity(ivan.getName(), ivan);
         em.addEntity(david.getName(), david);
     }
     
     @Override
     public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
         em.render(gc, g);

         TileSet test = new TileSet("test", "resources/textures/tileset_test.png", 32);
         test.render(0, 0, 32);
         test.render(1, 32, 32);
         test.render(2, 64, 32);
         test.render(3, 96, 32);
         
         test.render(0, 0, 128, 2, 1);
         test.render(0, 0, 192, 1, 2);
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
         em.setGameState(C.States.MAIN_STATE.name);
         em.update(gc, delta);
         if(evm.isHappening(C.Events.CLOSE_WINDOW.name, gc)) {
             gc.exit();
         }
     }
 }
