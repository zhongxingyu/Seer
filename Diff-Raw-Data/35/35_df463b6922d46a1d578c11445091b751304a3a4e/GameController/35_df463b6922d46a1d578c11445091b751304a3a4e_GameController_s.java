 package de.tu_darmstadt.gdi1.samegame.controller;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 
 import static java.awt.event.KeyEvent.*;
 
 import java.io.File;
 
 import java.util.Locale;
 
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.ParameterOutOfRangeException;
 
 import de.tu_darmstadt.gdi1.samegame.model.Level;
 import de.tu_darmstadt.gdi1.samegame.view.SameGameViewer;
 
 import de.tu_darmstadt.gdi1.samegame.controller.AbstractController;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.InternalFailureException;
 import de.tu_darmstadt.gdi1.samegame.view.gameframes.CustomizeFrame;
 import de.tu_darmstadt.gdi1.samegame.view.gameframes.MainPanel;
 
 public class GameController extends AbstractController{
 
 	////////////////////////Class/Attributes//////////////////////////
 	private SameGameViewer viewer;
 	private int markedRow;
 	private int markedCol;
 	private CustomizeFrame cframe;
 
 	////////////////////////Class/Constructors////////////////////////
 	public GameController(Level level){
 		this.level = level;
 		this.markedRow = 0;
 		this.markedCol = 0;
 	}
 
 	public GameController(Level level, SameGameViewer viewer){
 		this.level = level;
 		this.viewer = viewer;
 		this.markedRow = 0;
 		this.markedCol = 0;
 	}
 
 	////////////////////////Getters//&//Setters///////////////////////
 	public void setLevel(Level level){
 		this.level = level;
 	}
 	
 
 	////////////////////////Class/Operations//////////////////////////
 	
 
 	/**
 	 * Handle the clicks on a menu entry
 	 * 
 	 * 
 	 * @param menuItem
 	 * 				The menu entry this was clicked
 	 */
 	@Override
 	public void menuClick(JMenuItem menuItem){
 		String menuName = menuItem.getName();
 		// Which menu item has been clicked? Release the appropriate method
 		if (menuName.equals("GameMenu_RestartLvl") )
 			level.restartLevel();
 		if (menuName.equals("FileMenu_GenerateLevel")){
 			viewer.closeMainFrame();
 			viewer.setLevel(level);
 			level.generateLevel(10, 10, 5, 3);
 			viewer.showMainFrame();
 			viewer.notifyLevelLoaded();
 		}
 		if (menuName.equals("Skin_Default"))
 			viewer.setSkin("defaultskin", Color.black, Color.white, Color.LIGHT_GRAY);
 		if (menuName.equals("Skin_Tuskin"))
 			viewer.setSkin("tuskin", Color.white, Color.black, Color.black);
 		if (menuName.equals("Skin_Squareskin"))
 			viewer.setSkin("squareskin", Color.black, Color.white, Color.white);
 		if (menuName.equals("Skin_Ballskin"))
 			viewer.setSkin("ballskin", Color.white, Color.black, Color.black);
 		if (menuName.equals("Skin_Jewelskin"))
 			viewer.setSkin("jewelskin", Color.black, Color.white, Color.white);
 		if (menuName.equals("FileMenu_Exit"))
 			viewer.closeMainFrame();
 		
 		if (menuName.equals("GameMenu_Undo")){
 			try	{
 				level.undo();
 				}
 			catch(CannotUndoException ex){
 				SameGameViewer.showAlertFrame(ex.getMessage(), "There is no step to undo!");
 			}
 		}
 			
 		if (menuName.equals("GameMenu_Redo")){
 			try	{
 				level.redo();
 				}
 			catch(CannotRedoException ex){
 				SameGameViewer.showAlertFrame(ex.getMessage(), "There is no step to redo!");
 			}
 		}
 		
 
 		if (menuName.equals("German"))
 			viewer.setLanguage(new Locale("de", "DE"));
 		if (menuName.equals("English"))
 			viewer.setLanguage(new Locale("en", "US"));
 		if (menuName.equals("Polish"))
 			viewer.setLanguage(new Locale("pl", "PL"));
 		if (menuName.equals("About"))
 			viewer.showAboutFrame();
 		if (menuName.equals("FileMenu_GenerateCustomLevel")){
 			viewer.showCustomizeFrame();
		if (menuName.equals("CustomizeFrame")){
			viewer.closeMainFrame();
			viewer.setLevel(level);
			level.generateLevel(
						cframe.getCustomWidth(),
						cframe.getCustomHeight(),
						cframe.getCustomStoneNumber(),
						cframe.getCustomMinStones());
			viewer.showMainFrame();
			viewer.notifyLevelLoaded();
			
		}
 		}
 
 		File f;
 		String source;
 		if (menuName.equals("FileMenu_SaveLevel")){
 			source = "SaveLevel";
 			f = viewer.showFileChooseDialog(source);
 		}else if (menuName.equals("FileMenu_LoadLevel")){
 			source = "LoadLevel";
 			f = viewer.showFileChooseDialog(source);
 		} else if (menuName.equals("FileMenu_SaveGameState")){
 			source = "SaveGameState";
 			f = viewer.showFileChooseDialog(source);
 		} else if (menuName.equals("FileMenu_LoadGameState")){
 			source = "LoadGameState";
 			f = viewer.showFileChooseDialog(source);
 		}else{
 			f = null;
 			source = null;
 		}
 		
 		if(f != null)
 			fileChoosed(source, f);
 	}
 
 	/**
 	 * Handle the clicks on the Gamepanel
 	 * 
 	 * @param ActionEvent
 	 * 				The Actionevent this was released
 	 * @param JButton
 	 * 				The button this was clicked
 	 */
 	@Override
 	public void fieldClick(ActionEvent e, JButton b){
 		JButton btn = b;
 		
 		if (e.getSource() == btn) {
 			// determine x and y position
 			int posX =  (int)btn.getLocation().getX();
 			int posY =  (int)btn.getLocation().getY();
 
 			// entityClicked() on the bottun that was clicked
 			entityClicked(posX / btn.getWidth(), 
 								posY / btn.getHeight());
 			
 		}
 	}
 
 	/**
 	 * Handle which save/load menu entry was clicked
 	 * 
 	 * @param source
 	 * 				which save/load menu entry was choosed
 	 * @param f
 	 * 				which file should be saved
 	 */
 	public void fileChoosed(String source, File f){
 		try{
 			if(source.equals("LoadLevel")){
 				Level newLevel = new Level(viewer);
 				newLevel.restoreLevel(f);
 
 				viewer.setLevel(newLevel);
 				this.level = newLevel;
 
 				viewer.closeMainFrame();
 				viewer.showMainFrame();
 				viewer.notifyLevelLoaded();
 			}else if(source.equals("SaveLevel")){
 				level.storeLevel(f, true);
 			}else if(source.equals("LoadGameState")){
 				Level newLevel = new Level(viewer);
 				newLevel.restoreLevelState(f);
 
 				viewer.setLevel(newLevel);
 				this.level = newLevel;
 
 				viewer.closeMainFrame();
 				viewer.showMainFrame();
 				viewer.notifyLevelLoaded();
 			}else if(source.equals("SaveGameState")){
 				level.storeLevelState(f, true);
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Handle the click on a stone
 	 * 
 	 * @param positionX
 	 * 				position of a stone on the x axis
 	 * @param positionY
 	 * 				position of a stone on the y axis
 	 */
 	public void entityClicked(int positionX, int positionY){
 		MainPanel panel = viewer.getMainPanel();
 		panel.getParentWindow().requestFocus();
 
 		// removeStone() is only possible, if no animation in process 
 		// and if the stone removeable
 		if(!viewer.duringAnimation() && level.removeable(positionY, positionX)){
 			try{
 				// check if the game unfinished
 				// start the animation, remove the stone and redraw the gamefield
 				if(!level.isFinished()){
 					viewer.startAnimation(positionY, positionX, 500);
 					level.removeStone(positionY, positionX);
 					panel.redraw();
 				}
 			}catch(ParameterOutOfRangeException e){
 				SameGameViewer.showAlertFrame(e.getParameterName(), "Parameter out of Range");
 			}catch(InternalFailureException e){
 				SameGameViewer.showAlertFrame(e.getMessage(), "Internal Failure");
 			}
 		}
 	}
 
 	/**
 	 * Handle the keyactions
 	 * 
 	 * @param e
 	 * 			the key this was pressed
 	 */
 	@Override
 	public void keyPressed(KeyEvent e){
 		MainPanel panel = viewer.getMainPanel();
 
 		int key = e.getKeyCode();
 		// marked the field on upper left corner as the "start stone"
 		if(viewer!=null)
 			viewer.markField(markedRow, markedCol);
 
 		// which key was pressed? Release the appropriate method
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
 				if (panel != null){
 						if(viewer.duringAnimation() != true)
 							entityClicked(markedCol, markedRow);
 				}else
 					try{
 						level.removeStone(markedRow, markedCol);
 					}catch(ParameterOutOfRangeException ex){
 						SameGameViewer.showAlertFrame(ex.getMessage(), "Parameter out of range");
 					}
 				break;
 			case VK_LEFT:
 				if (markedCol >0){
 					markedCol -=1;
 					viewer.markField(markedRow,markedCol);
 					try{
 						if(panel != null)
 							panel.redraw();
 					}catch(InternalFailureException ex){
 						SameGameViewer.showAlertFrame(ex.getMessage(), "Internal Failure");
 					}
 				}
 				break;
 			case VK_RIGHT:
 				if (markedCol < level.getFieldWidth()-1){
 					markedCol +=1;
 					viewer.markField(markedRow,markedCol);
 					try{
 						if(panel != null)
 							panel.redraw();
 					}catch(InternalFailureException ex){
 						SameGameViewer.showAlertFrame(ex.getMessage(), "Internal Failure");
 					}
 				}
 				break;
 			case VK_UP:
 				if (markedRow >0){
 					markedRow -=1;
 					viewer.markField(markedRow, markedCol);
 					try{
 						if(panel != null)
 							panel.redraw();
 					}catch(InternalFailureException ex){
 						SameGameViewer.showAlertFrame(ex.getMessage(), "Internal Failure");
 					}
 				}
 				break;
 			case VK_DOWN:
 				if (markedRow < level.getFieldHeight()-1){
 					markedRow +=1;
 					viewer.markField(markedRow, markedCol);
 					try{
 						if(panel != null)
 							panel.redraw();
 					}catch(InternalFailureException ex){
 						SameGameViewer.showAlertFrame(ex.getMessage(), "Internal Failure");
 					}
 				}
 				break;
 			case VK_T:
 				viewer.showHighscoreFrame();
 				break;
 			case VK_G:
 				viewer.closeMainFrame();
 				viewer.setLevel(level);
 				level.generateLevel(10, 10, 5, 3);
 				viewer.showMainFrame();
 				viewer.notifyLevelLoaded();
 				break;
 			case VK_C:
 				viewer.showCustomizeFrame();
 			default:;
 		}
 	}
 }
