 package entity;
 
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 import component.RectangleComponent;
 
 public class Menu extends Container {
     int active = -1;
     private List<MenuItem> items;
 
     @Override
     protected List<MenuItem> getChildren() {
         return this.items;
     }
 
     public Menu(List<MenuItem> items, GameContainer container) {
         this.addComponent(new RectangleComponent());
         for (MenuItem item : items) {
             item.setContainer(container);
         }
         this.items = items;
         this.setLayout(new CenteredLayout(container, this));
         this.getLayout().setContainer(this);
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) {
         g.setColor(Color.lightGray);
         super.render(container, game, g);
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) {
         Input input = container.getInput();
         int mouseX = input.getMouseX();
         int mouseY = input.getMouseY();
         int counter = 0;
         boolean over = false;
         for (Rectangle r : this.getLayout().getRectangles()) {
             if (r.contains(mouseX, mouseY)) {
                 this.active = counter;
                this.items.get(counter).setColor(Color.blue);
                 over = true;
            } else {
                this.items.get(counter).setColor(Color.red);
             }
             counter++;
         }
         if (!over && this.active != -1) {
             this.active = -1;
         }
         if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON) && over) {
             ActionListener listener = this.items.get(this.active).getListener();
             listener.actionPerformed(null);
         }
     }
 }
