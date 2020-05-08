 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ne.ui;
 
 import render.AreaRenderer;
 
 /**
  *
  * @author Administrator
  */
 public class NE_GUI_SpriteArea extends NE_GUI_Element{
     //public String sprite_name = "gfx/items/undefined.png";
     AreaRenderer area_renderer;
     public NE_GUI_SpriteArea(){
         area_renderer = new AreaRenderer();
     }
 
     public void set_rect(int _x, int _y, int _w, int _h){
         area_renderer.set_rect(_x, _y, _w, _h);
     }
 
     @Override
     public void render(){
         area_renderer.render(get_x(), get_y(), w, h);
        super.render();
     }
 }
