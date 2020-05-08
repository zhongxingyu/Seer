 package scene1;
 import java.awt.*;
 import java.awt.geom.*;
 
 /**
    A shape that manages its selection state.
 */
 public abstract class SelectableShape implements SceneShape
 {
    public void setSelected(boolean b)
    {
       selected = b;
    }
 
    public boolean isSelected()
    {
       return selected;
    }
 
    private boolean selected;
 }
