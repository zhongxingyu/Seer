 package civ;
 
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.JOptionPane;
 import java.awt.Color;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseEvent;
 
 import static civ.State.ActionState.Move;
 import static civ.State.ActionState.None;
 import static civ.State.UnitState.UnitSelected;
 import static civ.State.HoverState.HoverNone;
 import static civ.State.HoverState.HoverTileOnly;
 import static civ.State.HoverState.HoverTileUnit;
 
 public class GameMapView extends JPanel{
     
     private GameMap gm;
     private State state = State.getInstance();
 
     public GameMapView(){
         super();
         gm = new GameMap(this);
         setLayout(null);
        setBounds(-120*(gm.getWidth()-1),0,125*gm.getWidth()*2,170*gm.getHeight());
         setBackground(Color.black);
         GameMapListener gml = new GameMapListener();
         addMouseListener(gml);
         addMouseMotionListener(gml);
     }
 
     private class GameMapListener implements MouseListener, MouseMotionListener{
         private int saveX=0, saveY=0;
         JPanel before, after;
         public void mouseDragged(MouseEvent e){
             if(SwingUtilities.isRightMouseButton(e)){
                 int tempX = getX() + e.getX() - saveX;
                 int tempY = getY() + e.getY() - saveY;
                 if(tempX > 0){
                     tempX = 0;
                 }
                 if(tempX + getWidth() < getParent().getWidth()){
                     tempX = getParent().getWidth() - getWidth();
                 }
                 if(tempY > 0){
                     tempY = 0;
                 }
                 if(tempY + getHeight() < getParent().getHeight()){
                     tempY = getParent().getHeight() - getHeight();
                 }
 
                 setBounds(tempX, tempY, getWidth(), getHeight());
             }
         }
         
         public void mouseReleased(MouseEvent e){
             if(e.getButton() == MouseEvent.BUTTON3){
                 saveX = 0;
                 saveY = 0;
             }
         }
 
         public void mousePressed(MouseEvent e){
             if(e.getButton() == MouseEvent.BUTTON1){
                 if(after != null){
                     GameMapView.this.remove(after);
                     before = null;
                     after = null;
                 }
                 Tile tile = gm.getTileAt(e.getX(), e.getY());
                 if(tile != null){
                     System.out.println("Printing hexagon at x"+ tile.getView().getTilePositionx()+
                             " y"+tile.getView().getTilePositiony());
 
                     switch(state.getActionState()){
                         case Move:
                             civ.Move.makeMove(state.getSelectedTile(), tile);
                             state.setActionState(None);
                             break;
                         case Attack:
                             if(state.getUnitState() == UnitSelected && tile.hasUnit()){
                                 if(!tile.getUnit().isAlly()){
                                     if(Battle.attackRange(state.getSelectedTile(), tile, 
                                                 state.getSelectedUnit().getType().getRange()) > 0){
                                         before = new PopUpBubble(state.getSelectedTile().getView().getX() + 135, 
                                                 state.getSelectedTile().getView().getY() + 35);
                                         GameMapView.this.add(before, 0);
                                         GameMapView.this.repaint();
                                         int choice = showConfirmAttackPane(state.getSelectedUnit(), tile.getUnit(), 
                                             state.getSelectedTile(), tile);
                                         if(choice == 0){
                                             Battle.doBattle(state.getSelectedUnit(), tile.getUnit(), 
                                                 state.getSelectedTile(), tile);
                                         }
                                         after = new PopUpBubble(state.getSelectedTile().getView().getX() + 135, 
                                                 state.getSelectedTile().getView().getY() + 35, 
                                                 Battle.getAttackerLoss(), Battle.getDefenderLoss());
                                         GameMapView.this.remove(before);
                                         GameMapView.this.add(after, 0);
                                         GameMapView.this.repaint();
 
                                     }
                                 }
                                 state.setActionState(None);
                                 tile = state.getSelectedTile();
                             }else{
                                 System.out.println("No unit to attack here.");
                             }
                             break;
                     }
                     tile.select(); 
                 }
             }
             else if(e.getButton() == MouseEvent.BUTTON3){
                 saveX = e.getX();
                 saveY = e.getY();
             }
         }
 
         public void mouseClicked(MouseEvent e){
 
         }
 
         public void mouseMoved(MouseEvent e){
             Tile tile = gm.getTileAt(e.getX(), e.getY());
             if (tile == null){
                 state.setHoverTile(null);
                 state.setHoverState(HoverNone);
             }
             else if (tile != state.getHoverTile() && tile.hasUnit()){
                 state.setHoverTile(tile); 
                 state.setHoverState(HoverTileUnit); 
             } 
             else if (tile != state.getHoverTile() && tile.isExplored() == true){
                 state.setHoverTile(tile); 
                 state.setHoverState(HoverTileOnly); 
             }
         }
 
         public void mouseExited(MouseEvent e){
         
         }
 
         public void mouseEntered(MouseEvent e){
         }	
     } 
 
     private int showConfirmAttackPane(PhysicalUnit att, PhysicalUnit def, Tile tatt, Tile tdef){
         PhysicalUnit tmpAtt = new PhysicalUnit(att);
         PhysicalUnit tmpDef = new PhysicalUnit(def);
         Battle.doAverageBattle(tmpAtt, tmpDef, tatt, tdef);
 
         return JOptionPane.showConfirmDialog(
                 null,
                 "You: " + tmpAtt.getManPower() + "\nDefender: " + tmpDef.getManPower() +
                 "\n Attack?",
                 "Expected outcome",
                 JOptionPane.YES_NO_OPTION);
     }
 }// class
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
