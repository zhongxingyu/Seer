 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JPopupMenu;
 import student.grid.Critter;
 import student.grid.HexGrid.Reference;
 import student.grid.Tile;
 import student.world.World;
 
 /**
  *
  * @author Panda^H^H^H^H^Hhwh48
  */
 public class MouseInteractionHandler extends MouseAdapter {
     private World model;
     private WorldFrame view;
     private Reference<Tile> rclxtar = null;
     
     private JPopupMenu men;
     
     private Action rock, unrock;
    private Action crit, critMenIts[] = new Action[8];
     
     public MouseInteractionHandler(final World _model, final WorldFrame _view) {
         model = _model;
         view = _view;
        rock = new LocAxn("put rock") {
             @Override
             public void act() {
                 rclxtar.setContents(new Tile.Rock());
             }
         };
         unrock = new LocAxn("derock") {
             @Override
             public void act() {
                 rclxtar.setContents(new Tile(false, 0));
             }
         };
        crit = new LocAxn("add critter") {
             @Override
             protected void act() {
                 Tile t = new Tile(false, 0);
                 t.putCritter(new Critter(model, rclxtar, 9));
                 rclxtar.setContents(t);
             }
         };
         critMenIts[0] = new LocAxn("forward") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().forward();
             }
         };
         critMenIts[1] = new LocAxn("backward") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().backward();
             }
         };
         critMenIts[2] = new LocAxn("left") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().left();
             }
         };
         critMenIts[3] = new LocAxn("right") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().right();
             }
         };
         critMenIts[4] = new LocAxn("eat") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().eat();
             }
         };
         critMenIts[5] = new LocAxn("attack") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().attack();
             }
         };
         critMenIts[6] = new LocAxn("grow") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().grow();
             }
         };
        critMenIts[7] = new LocAxn("remove") {
            @Override
            public void act() {
                if(rclxtar.contents().critter())
                    rclxtar.contents().removeCritter();
            }
        };
         /*criMenIts[0] = new LocAxn("bud") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().bud();
             }
         };*/
         /*criMenIts[0] = new LocAxn("mate") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().mate();
             }
         };*/
         /*criMenIts[0] = new LocAxn("tag") {
             @Override
             public void act() {
                 if(rclxtar.contents().critter())
                     rclxtar.contents().getCritter().tag();
             }
         };*/
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         switch(e.getButton()) {
             case 1:
                 leftClick(e);
                 break;
             case 3:
                 rightClick(e);
                 break;
         }
     }
 
     private void leftClick(MouseEvent e) {
         Reference<Tile> at = lookup(e);
         view.display().setCurrentLocation(at);
         view.repaint();
     }
 
     private void rightClick(MouseEvent e) {
         if(men != null)
             men.setVisible(false);
         rclxtar = lookup(e);
         if(rclxtar.contents() == null)
             rclxtar.setContents(new Tile(false,0));
         men = new JPopupMenu();
         if(rclxtar.contents().rock())
             men.add(unrock);
         else {
             men.add(rock);
            if(rclxtar.contents().critter())
                for(Action a : critMenIts)
                    men.add(a);
            else
                men.add(crit);
         }
         men.setLocation(e.getLocationOnScreen());
         men.setVisible(true);
     }
     
     private Reference<Tile> lookup(MouseEvent e) {
         int x = e.getX();
         int y = e.getY();
         int ret[] = view.display().grid().hexAt(x, y);
         int r = ret[0];
         int c = ret[1];
         return model.at(r, c);
     }
     
     private abstract class LocAxn extends AbstractAction {
         @Override
         public final void actionPerformed(ActionEvent e) {
             act();
             men.setVisible(false);
             view.repaint();
         }
         protected abstract void act();
         public LocAxn(String s) {
             super(s);
         }
     }
 }
