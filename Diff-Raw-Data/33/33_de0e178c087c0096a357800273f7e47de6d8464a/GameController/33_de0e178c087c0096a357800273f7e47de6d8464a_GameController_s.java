 package edu.chl.codenameg.controller;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.Direction;
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.GameModel;
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 import edu.chl.codenameg.view.GameView;
 
 public class GameController implements KeyListener, Runnable{
 	
 	private GameModel model;
 	private GameView view;
 	private List<PlayerCharacter> listOfPC;
 	
 	
 	private boolean isRunning = true;
 	
 	public GameController() {
 		this.model = new GameModel();
 		this.view = new GameView(this.model, this);
 		
 		listOfPC = new ArrayList<PlayerCharacter>();		
 		getPlayerCharacters(this.model);
 		
 		Thread gameThread = new Thread(this);
 		
 		gameThread.start();
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent evt) {
 		// TODO Auto-generated method stub
 		if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
 			model.startGame();
 		}
 		else if(evt.getKeyCode() == KeyEvent.VK_LEFT) {
 			listOfPC.get(0).move(Direction.LEFT);
 		}
 		else if(evt.getKeyCode() == KeyEvent.VK_RIGHT) {
 			listOfPC.get(0).move(Direction.RIGHT);
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
 			listOfPC.get(0).stopMove();
 		}
		else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
 			listOfPC.get(0).stopMove();
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent evt) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		while (this.isRunning) {
 				model.update(10);
 				view.repaint();
 				try {
 					Thread.sleep(10);
 				} catch (InterruptedException e) {
 					// if we get this exception, we're asked to terminate ourselves
 					this.isRunning = false;
 				}
 		}
 	}
 	
 	private void getPlayerCharacters(GameModel model) {
 		List<Entity> list = model.getWorld().getEntities();
 		for (int i = 0; i < list.size(); i++) {
 			if (list.get(i) instanceof PlayerCharacter) {
 				listOfPC.add((PlayerCharacter) list.get(i));
 			}
 		}
 	}
 }
