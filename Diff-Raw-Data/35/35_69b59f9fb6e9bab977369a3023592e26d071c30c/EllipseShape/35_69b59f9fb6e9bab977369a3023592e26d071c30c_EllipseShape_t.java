 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphview.shapes;
 
 import geometry.Intersect;
 import geometry.Rect;
 import geometry.Vec2;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.geom.Ellipse2D;
 
 /**
  *
  * @author Kirill
  */
 public class EllipseShape extends NodeAspect{
     Ellipse2D.Float ell=null;
     
     public EllipseShape(Rect rect)
     {
         setRectangle(rect);
         ell=new Ellipse2D.Float(rect.left,rect.top,rect.getSize().x,rect.getSize().y);
         this.aspectType=eNodeAspectType.ELLIPSE;
     };
     
     public EllipseShape(float posX,float posY, float sizeX, float sizeY)
     {
         setRectangle(new Rect(posX,posY,posX+sizeX,posY+sizeY));
         ell=new Ellipse2D.Float(posX,posY,sizeX,sizeY);
         this.aspectType=eNodeAspectType.ELLIPSE;
     };
     
     public EllipseShape(Vec2 position, float radius)
     {
         Rect r=new Rect(position.x-radius,position.y-radius,position.x+radius,position.y+radius);
         setRectangle(r);
         ell=new Ellipse2D.Float(r.left,r.top,r.getSize().x,r.getSize().y);
         this.aspectType=eNodeAspectType.ELLIPSE;
         
     };
     
     @Override
     public void draw(Graphics2D g) {
         Rect globalPlace=getGlobalRectangle();
         g.setColor(color.getProp());
         g.fillOval((int)globalPlace.left, (int)globalPlace.top, (int)globalPlace.getSize().x, (int)globalPlace.getSize().y);
         g.setColor(Color.black);
         
         g.drawOval((int)globalPlace.left, (int)globalPlace.top, (int)globalPlace.getSize().x, (int)globalPlace.getSize().y);
 
         super.draw(g);
     }
     
     @Override
     public boolean isIntersects(Vec2 pt) {
         ell.x=getPosition().x;
         ell.y=getPosition().y;
         ell.width=getSize().x;
         ell.height=getSize().y;
         return ell.contains(pt.toPoint());
     }
 
     @Override
     public boolean isIntersects(Rect r) {
         return Intersect.rectangle_rectangle(getGlobalRectangle(), r)==Intersect.INCLUSION;
     }
 
     @Override
     public Vec2 getPortPoint(Vec2 from) {
         Vec2 v1=new Vec2();
         Vec2 v2=new Vec2();
         
         Intersect.line_ellipsecenter(from,getGlobalRectangle(),v1,v2);
         
         if(from.getDistance(v1)<from.getDistance(v2))
             return v1;
         return v2;
     }
     
     @Override
     public Rect getContainRect() {
        
        Vec2 axes=new Vec2((float)(getRectangle().getSize().x*Math.sqrt(2.0f)/2),
                (float)(getRectangle().getSize().y*Math.sqrt(2.0f)/2));
        Vec2 center=getRectangle().getSize().divide(2);
        
        Rect r=new Rect(center.x-axes.x/2,center.y-axes.y/2,center.x+axes.x/2,center.y+axes.y/2);
        return r;
     }
 }
