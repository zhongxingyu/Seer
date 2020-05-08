 package game.entities;
 
 import game.components.Message;
 import game.components.interfaces.IAnimatedComponent;
 import game.pods.GameTime;
 import game.world.World;
 import math.Vector2;
 
 import org.newdawn.slick.Graphics;
 
 import util.Node;
 
 public class Animation implements IEntity {
   private final IAnimatedComponent anim;
   private final Graphics graphics;
 
   private boolean active;
   private World world;
   private Vector2 position;
 
   public Animation(int x, int y, IAnimatedComponent anim, Graphics graphics) {
     this.position = new Vector2(x, y);
    this.anim     = anim;
     this.graphics = graphics;
 
    active = true;
   }
 
   @Override
   public World getWorld() {
     return world;
   }
 
   @Override
   public boolean isActive() {
     return active;
   }
 
   @Override
   public void remove() {
     active = false;
   }
 
   @Override
   public void render(Graphics g) {
     g.pushTransform();
     g.translate(position.x, position.y);
 
     anim.render(g);
 
     g.popTransform();
   }
 
   @Override
   public void sendMessage(Message message, Object args) {
     anim.reciveMessage(message, args);
   }
 
   public void setPosition(Vector2 position) {
     this.position = position;
   }
 
   @Override
   public void setWorld(World world) {
     this.world = world;
   }
 
   @Override
   public void update(GameTime time) {
     anim.update(time);
 
     if (anim.getCurrentTile().isEqual(anim.getLastTile())) {
       active = false;
 
       graphics.pushTransform();
       graphics.translate(position.x, position.y);
       anim.render(graphics);
       graphics.popTransform();
       graphics.flush();
     }
   }
 
   @Override
   public String debugString() {
     return "Animation";
   }
 
   @Override
   public Node<String> debugTree() {
     Node<String> parent = new Node<>(debugString());
 
     parent.nodes.add(new Node<>("Animation = " + anim.toString()));
     parent.nodes.add(new Node<>("Active = " + Boolean.toString(isActive())));
 
     return parent;
   }
 }
