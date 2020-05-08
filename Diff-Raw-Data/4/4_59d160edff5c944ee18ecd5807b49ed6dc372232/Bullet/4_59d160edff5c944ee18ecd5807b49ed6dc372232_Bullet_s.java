 import java.awt.*;
 
 
 public class Bullet extends Entity
 {
     private float damage, lastX, lastY, xVel, yVel;
     private int  width, height, alliance;
     private Image bullet;
     
     public Bullet(EntityManager manager, int xpos, int ypos, int dmg, int alnc)
     {
         super(manager);
         x=xpos;lastX=x;
         y=ypos;lastY=y;
        width=1;
        height=1;
         damage=dmg;
         yVel=0;
         xVel=0;
         alliance=alnc;
         bullet=Toolkit.getDefaultToolkit().getImage("Pics/BlueSquaretrans.png");
         
         shape = new RectShape(x, y, width, height);
     }
     
     
     public void update(float delta)
     {
         
         lastX = x;
         lastY = y;
         
         x+= xVel * delta;
         y+= yVel * delta;
         
         
         RectShape rect = (RectShape)shape;
         rect.xpos = x;
         rect.ypos = y;
         
         if( x<0    && xVel<=0 ||
             x>1280 && xVel>=0 ||
             y<0    && yVel<=0 ||
             y>780  && yVel>=0){
                 Destroy();
             }
     }
     
     
     public void draw(Graphics g, float interp)
     {
         
         
         Graphics2D g2d=(Graphics2D)g; // Create a Java2D version of g.
         
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
         
         int drawX = (int) ((x - lastX) * interp + lastX - width/2);
         int drawY = (int) ((y - lastY) * interp + lastY - height/2);
         
         
         g2d.drawImage(bullet,drawX,drawY,width,height,null);
         g2d.setColor(Color.WHITE);
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
         
     }
     
     public void setSpeed(float a, float b)
     {
         xVel=a;
         yVel=b;
     }
     
     public void Collide(Entity other)
     {
         if(other instanceof Enemy)
         {
             Enemy enemy = (Enemy)other;
             if(alliance!=enemy.getAlliance())
             {
                 enemy.Hurt(damage);
                 Destroy();
             }
             
         }
         
         else if(other instanceof Player)
         {
             Player player = (Player)other;
             if(alliance!=player.getAlliance())
             {
                 player.Hurt(damage);
                 Destroy();
             }
             
         }
     }
 }
