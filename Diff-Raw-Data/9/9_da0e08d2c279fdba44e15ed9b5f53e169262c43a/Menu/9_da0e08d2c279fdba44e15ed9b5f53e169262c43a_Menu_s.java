 package rekkyn.javagame;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Menu extends BasicGameState {
     static Image title;
     int titleX;
     int titleY;
 
     public static List<Entity> entities = new ArrayList<Entity>();
 
     public Menu(int state) {}
 
     @Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
        title = new Image("res/Title.png");
     }
 
     @Override
     public void enter(GameContainer container, StateBasedGame game) throws SlickException {
         titleX = Game.width / 2 - 150;
         titleY = 0;
 
         for (int i = 0; i < entities.size(); i++) {
             Entity e = entities.get(i);
             if (e instanceof Rekkyn) {
                 ((Rekkyn) e).playerControlled = false;
             }
         }
 
         Rekkyn rekkyn = new Rekkyn(Game.width / 2 - 12, 195);
         add(rekkyn);
 
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
         g.setColor(new Color(0.82F, 0.85F, 0.88F));
         g.fillRect(0, 0, Game.width, Game.height);
         g.drawImage(Game.scaleImage(title, 3), titleX, titleY, titleX + 300, titleY + 240, 0, 0, 300, 240);
         if (System.currentTimeMillis() % 1200 < 600) {
             g.setColor(new Color(0.12F, 0.21F, 0.31F));
             Font.centerText("Press A to begin testing", Game.width / 2 + 1, 251, 3, g);
             g.setColor(new Color(0.49F, 0.58F, 0.68F));
             Font.centerText("Press A to begin testing", Game.width / 2, 250, 3, g);
         }
 
         for (int i = 0; i < entities.size(); i++) {
             Entity e = entities.get(i);
 
             e.render(container, game, g);
         }
 
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
         Input input = container.getInput();
         if (input.isKeyPressed(Input.KEY_ESCAPE)) {
             game.enterState(Game.OPTIONS);
         }
 
         for (int i = 0; i < entities.size(); i++) {
             Entity e = entities.get(i);
 
             e.update(container, game, delta);
 
             if (e.removed) {
                 entities.remove(i--);
             }
         }
         
         if (input.isMousePressed(0)) {
             Rekkyn rekkyn = new Rekkyn(Mouse.getX() - Rekkyn.width / 2, Game.height - Mouse.getY() - Rekkyn.height / 2);
             rekkyn.playerControlled = false;
             rekkyn.isFree = true;
             add(rekkyn);
         }
     }
 
     @Override
     public int getID() {
         return Game.MENU;
     }
 
     public void add(Entity entity) {
         entity.removed = false;
         entities.add(entity);
     }
 
 }
