 package de.tu_darmstadt.gdi1.samegame;
 
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import static java.awt.event.KeyEvent.*;
 
 import java.util.Locale;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.InternalFailureException;
 import de.tu_darmstadt.gdi1.samegame.gameframes.MainPanel;
 import de.tu_darmstadt.gdi1.samegame.gameframes.LoadGameFrame;
 
 public class GameController extends KeyAdapter implements ActionListener{
 
 	private Level level;
 	private SameGameViewer viewer;
 
 
 	// the list of entities
 	private Vector<JButton> entities = null;
 
 	public GameController(Level level){
 		this.level = level;
 	}
 
 	public GameController(Level level, SameGameViewer viewer){
 		this.level = level;
 		this.viewer = viewer;
 	}
 
 	public void setLevel(Level level){
 		this.level = level;
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		if(e.getSource() instanceof JMenuItem){
 			menuClick((JMenuItem) e.getSource());
 		}else if(e.getSource() instanceof JButton){
 			fieldClick(e, (JButton) e.getSource());
 		}else if (e.getSource() instanceof JFileChooser){
 			String command = e.getActionCommand();
 	        if (command.equals(JFileChooser.APPROVE_OPTION)) {
 	        	//just for testing a system.out
 	        	System.out.println(LoadGameFrame.getLoadPath());
 	        }
 			
 	        }
 	}
 
 	public void menuClick(JMenuItem menuItem){
 		String menuName = menuItem.getName();
 		if (menuName.equals("GameMenu_RestartLvl") )
 			level.restartLevel();
 		if (menuName.equals("FileMenu_GenerateLevel")){
 			viewer.closeMainFrame();
 			viewer.setLevel(level);
 			level.generateLevel(10, 10, 5, 3);
 		}
 		if (menuName.equals("Skin_Default"))
 			viewer.setSkin("defaultskin", Color.white, Color.black);
 		if (menuName.equals("Skin_Tuskin"))
 			viewer.setSkin("tuskin", Color.black, Color.white);
		if (menuName.equals("Skin_Squareskin"))
			viewer.setSkin("squareskin", Color.black, Color.white);
 		if (menuName.equals("Skin_Ballskin"))
			viewer.setSkin("ballskin", Color.white, Color.black);
 		if (menuName.equals("FileMenu_SaveLevel"))
 			viewer.showMainFrame();
 		if (menuName.equals("FileMenu_LoadLevel"))
 			viewer.showLoadGameFrame();
 		if (menuName.equals("FileMenu_Exit"))
 			viewer.closeMainFrame();
 		if (menuName.equals("GameMenu_Undo"))
 			level.undo();
 		if (menuName.equals("GameMenu_Redo"))
 			level.redo();
 		if (menuName.equals("German"))
 			viewer.setLanguage(new Locale("de", "DE"));
 		if (menuName.equals("English"))
 			viewer.setLanguage(new Locale("en", "US"));
 	}
 	
 	public void fieldClick(ActionEvent e, JButton b){
 		// retrieve first button
 		JButton btn = b;
 		
 			if (e.getSource() == btn) {
 				// determine x and y position
 				int posX =  (int)btn.getLocation().getX();
 				
 				int posY =  (int)btn.getLocation().getY();
 
 				// pass message along
 				MainPanel mainPanel = viewer.getMainPanel();
 				mainPanel.entityClicked(posX / btn.getWidth(), 
 										posY / btn.getHeight());
 			}
 		}
 
 	
 	@Override
 	public void keyPressed(KeyEvent e){
 		int key = e.getKeyCode();
 		int markedRow = 0;
 		int markedCol = 0;
 		if(viewer!=null){
 			markedRow = viewer.getMarkedFieldRow();
 			markedCol = viewer.getMarkedFieldCol();
 			viewer.markField(markedRow, markedCol);
 		}
 
 		switch(key){
 			case VK_N:
 				level.restartLevel();
 				break;
 			case VK_BACK_SPACE:
 				if (viewer.duringAnimation() != true)
 					try{
 						level.undo();
 					}catch(CannotUndoException ignored){}
 				break;
 			case VK_ENTER:
 				if (viewer.duringAnimation() != true)
 					try{
 						level.redo();
 					}catch(CannotRedoException ignored){}
 				break;
 			case VK_SPACE:
 				if (viewer.duringAnimation() != true)
 					viewer.getMainPanel().entityClicked(markedCol, markedRow);
 				break;
 			case VK_LEFT:
 				if (markedCol >0){
 					markedCol -=1;
 					viewer.markField(markedRow,markedCol);
 					try{
 						viewer.getMainPanel().redraw();
 					}catch(InternalFailureException ex){
 						ex.printStackTrace();
 					}
 				}
 				else System.out.println("else");
 				break;
 			case VK_RIGHT:
 				if (markedCol < level.getFieldWidth()-1){
 					markedCol +=1;
 					viewer.markField(markedRow,markedCol);
 					try{
 						viewer.getMainPanel().redraw();
 					}catch(InternalFailureException ex){
 						ex.printStackTrace();
 					}
 				}
 				break;
 			case VK_UP:
 				if (markedRow >0){
 					markedRow -=1;
 					viewer.markField(markedRow, markedCol);
 					try{
 						viewer.getMainPanel().redraw();
 					}catch(InternalFailureException ex){
 						ex.printStackTrace();
 					}
 				}
 				break;
 			case VK_DOWN:
 				if (markedRow < level.getFieldHeight()-1){
 					markedRow +=1;
 					viewer.markField(markedRow, markedCol);
 					try{
 						viewer.getMainPanel().redraw();
 					}catch(InternalFailureException ex){
 						ex.printStackTrace();
 					}
 				}
 				break;
 			default:;
 		}
 	}
 }
