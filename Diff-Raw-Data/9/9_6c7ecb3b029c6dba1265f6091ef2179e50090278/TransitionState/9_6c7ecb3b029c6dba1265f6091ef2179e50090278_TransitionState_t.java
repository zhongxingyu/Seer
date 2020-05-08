 package com.vulcastudios.states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.vulcastudios.TestGame;
 
 public class TransitionState extends BasicGameState {
 	
 	private int numOfZombies;
 	private long finalTime;
 	private boolean newLevel = true;
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 
 		double seconds = finalTime / 1000.0;
 		g.drawString("Final Time: " + seconds + " seconds", 50, 50);
 		String par = ((TestGame)game).getCurrentLevel().getPar();
 		g.drawString("Zombies Used: " + numOfZombies + " Par: " + par, 50, 100);
 		if(!((TestGame)game).isOnLastLevel()){
 			g.drawString("Press Enter to continue to the next level", 50, 150);
 		}
 		else{
 			g.drawString("The Game Is Done!", 50, 150);
 		}
 		
 		((TestGame)game).getCurrentLevel().render(container, game, g);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		
 		if(newLevel){
 			numOfZombies = ((TestGame)game).getCurrentLevel().getNumberOfZombies();
 			((TestGame)game).getCurrentLevel().initLevelReplay();
 			finalTime = ((TestGame)game).getCurrentLevel().getFinalTime();
 			newLevel = false;
 		}
 		
 		if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 			game.enterState(TestGame.MAIN_MENU_STATE_ID);
		} else if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
 			if (((TestGame)game).isOnLastLevel()) {
 				game.enterState(TestGame.CREDITS_STATE);
 			} else {
 				((TestGame)game).goToNextLevel();
 				newLevel = true;
 				game.enterState(TestGame.IN_GAME_STATE);
 				numOfZombies = -1;
 				finalTime = -1;
 			}
		} else {
			((TestGame)game).getCurrentLevel().update(container, game, delta);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return TestGame.TRANSITION_STATE;
 	}
 
 }
