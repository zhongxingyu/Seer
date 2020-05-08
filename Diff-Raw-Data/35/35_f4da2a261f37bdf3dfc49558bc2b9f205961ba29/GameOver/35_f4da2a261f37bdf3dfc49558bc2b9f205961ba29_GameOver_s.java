 package de.blogspot.hollowspectersgames.frogger.states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import de.blogspot.hollowspectersgames.frogger.other.Constants;
 
 public class GameOver extends BasicGameState {
 	
 	private Sound select;
 	private Sound music;
 	private boolean flash = true;
 	private int flash_timer = 25;
 	private Image pressStartToReturn;
 	private Image background;
 	private Image numbers;
 	private Image[] number = new Image[10];
 	private long posX, posY;
 	private long posX2, posY2;
 	private Image rupee;
 	
 	@Override
 	public void init(GameContainer container, StateBasedGame game) throws SlickException
 	{
 		pressStartToReturn = new Image("img/menu/pressentertoreturn.png");
 		select = new Sound(Constants.SOUND_SELECT);
 		background = new Image("img/menu/gameover_bg.png");
 		music = new Sound(Constants.SOUND_GAMEOVERBG);
 		rupee = new Image("img/rupee1.png");
 		
 		/*
 		 * In Numbers sind alle Ziffern drin, um den Highscore darzustellen.
 		 * Die SubImages sind jeweils die einzelnen Imges
 		 */
 		numbers = new Image("img/menu/numbers.png");
 		number[0] = numbers.getSubImage(0, 0, 32, 32);
 		number[1] = numbers.getSubImage(40, 0, 28, 32);
 		number[2] = numbers.getSubImage(72, 0, 32, 32);
 		number[3] = numbers.getSubImage(108, 0, 32, 32);
 		number[4] = numbers.getSubImage(144, 0, 32, 32);
 		number[5] = numbers.getSubImage(180, 0, 32, 32);
 		number[6] = numbers.getSubImage(216, 0, 32, 32);
 		number[7] = numbers.getSubImage(252, 0, 32, 32);
 		number[8] = numbers.getSubImage(288, 0, 32, 32);
 		number[9] = numbers.getSubImage(324, 0, 33, 32);
 
		posX = 270;
 		posY = 200;
 		posX2 = 270;
 		posY2 = 250;
 	}
 	
 	@Override
 	public void update(GameContainer container, StateBasedGame state, int delta) throws SlickException
 	{	
 		if (music.playing() == false)
 			music.loop();
 		
 		//damit das "press enter to return" flasht
 		flash_timer--;
 		
 		if (flash_timer == 0)
 		{
 			if (flash == true)
 				flash = false;
 			else
 				flash = true;
 			
 			flash_timer = 25;
 		}
 			
 		Input input = container.getInput();
 		
 		if (input.isKeyPressed(Input.KEY_ENTER))
 		{
 			select.play();
 			music.stop();
 			
 			//Highscores gelscht
 			state.enterState(GameStates.MenuState);
 		}	
 	}
 	
 	@Override
 	public void render(GameContainer container, StateBasedGame state, Graphics g) throws SlickException
 	{		
 		background.draw(0,0);
 		rupee.draw((posX2 + 23), posY2 -17, 1.5f);
 		
 		if (flash == true)
 			pressStartToReturn.drawCentered(270,580);
 		
 		//Die 5 Ziffern rendern!
 		//zuerst den rupeeCounter in ein int array umwandeln
 		
 		int[] numbersArr = intToIntArray(GameStatePlaying.highscore);
 		
 		//dann rendern
 		
		for (int i = 0; i <= 4; i++) {
 			int n = numbersArr[i];
			number[n].drawCentered((posX-33*2)+i*33,posY);
			/*
			 * posX setzt sich so zusammen, dass posX die Mitte ist, posX+32
			 * die rechte stelle ist, und dann jeweils um 32 nach links gerckt wird
			 * 33 ist quasi die gap
			 */
 		}
 		
 		/*
 		 * Zweiter Counter nur fr die Rupees.
 		 */
 		int[] numbersArr2 = rupeesToIntArray(GameStatePlaying.getCounter());
 
 		for (int i = 0; i <= 2; i++) {
 			int n = numbersArr2[i];
 			number[n].drawCentered((posX2-33*2)+i*33,posY2);
 		}
 		
 	}
 
 	public int getID() {
 		return GameStates.GameOver;
 	}
 
 	public int[] intToIntArray(int n)
 	{
		int einer;
		int zehner;
		int hunderter;
		int tausender;
		int zehntausender;
 		
		einer = n%10;
		zehner = (n - einer)/10;
		hunderter = ((n - einer) - zehner)/100;
		tausender = (((n - einer) - zehner) - hunderter)/1000;
		zehntausender = ((((n - einer) - zehner) - hunderter) - tausender)/10000;
		
		int[] intArray = {zehntausender, tausender, hunderter, zehner, einer};
 		
 		return intArray;
 	}
 	
 	public int[] rupeesToIntArray(int n)
 	{
 		int einer;
 		int zehner;
 		int hunderter;
 		
 		einer = n%10;
 		zehner = (n - einer)/10;
 		hunderter = ((n - einer) - zehner)/100;
 		
 		int[] intArray = {hunderter, zehner, einer};
 		
 		return intArray;
 	}
 }
