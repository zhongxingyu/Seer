 package team.black.fruitswirl;
 
 import java.util.Set;
 
 import org.flixel.FlxButton;
 import org.flixel.FlxG;
 import org.flixel.FlxGroup;
 import org.flixel.FlxPoint;
 import org.flixel.FlxSprite;
 import org.flixel.FlxState;
 import org.flixel.FlxText;
 import org.flixel.FlxU;
 import org.flixel.event.IFlxButton;
 import com.badlogic.gdx.graphics.Color;
 
 public class PlayState extends FlxState
 {
 	private Grid g;
 	private FlxSprite bg, lvlComplete;
 	private FlxText t, scoreText, lvlText, lvlcompletetexta, lvlcompletetextb;
 	private boolean justTapped = false;
 	private FlxPoint mpos = new FlxPoint(0,0);
 	private FlxButton lvl_ok, lvl_cancel;
 	private FlxGroup pauseStuff = new FlxGroup(), gameStuff = new FlxGroup(),
 						guideGridPoints = new FlxGroup();
 	
 	public boolean[] pauseCards = new boolean[10];
 	public int[] levelThres = new int[11];
 	
 	
 	@Override
 	public void create()
 	{
 		
 		//level score thresholds
 		levelThres[0] = 50;
 		for ( int i = 1; i < 10; i++ )
 			levelThres[i] += levelThres[i-1] + 50 + ( i * 25 );
 		//try and score this much!
 		levelThres[10] = Integer.MAX_VALUE - Rg.rng.nextInt(9999);
 		
 		//prep
 		bg = new FlxSprite(0,0);
 		bg.makeGraphic(FlxG.width, FlxG.height, 0xFF000000);
 		
 		t = new FlxText(0, 0, 600, "FruitSwirl Alpha 1.1");
 		
 		lvlComplete = new FlxSprite();
 		lvlComplete.loadGraphic("lvl_complete.png");
 		lvlComplete.x = (FlxG.width/2) - (lvlComplete.width/2) + 15;
 		lvlComplete.y = (FlxG.height/2) - (lvlComplete.height/2) - 40;
 		
 		lvlcompletetexta = new FlxText(lvlComplete.x - 75, lvlComplete.y+95, 400);
 		lvlcompletetexta.setText("Nice work! This game is still only in " +
 				"Alpha, so we'll add more soon (hopefully)!");
 		
 		lvlcompletetextb = new FlxText(lvlcompletetexta.x, lvlcompletetexta.y+20, 400);
 		lvlcompletetextb.setText("To help us out, would you mind answering a" +
 				" few questions about the app? Please?");
 		
 		lvl_ok = new FlxButton((FlxG.width/2) - 75, (FlxG.height/2)+58,
 				"Take Survey", okIBtn);
 		lvl_cancel = new FlxButton((FlxG.width/2) + 15, (FlxG.height/2)+58,
 				"Keep Playing", cancelIBtn);
 		
 		pauseStuff.add(lvlComplete);
 		pauseStuff.add(lvlcompletetexta);
 		pauseStuff.add(lvlcompletetextb);
 		pauseStuff.add(lvl_cancel);
 		pauseStuff.add(lvl_ok);
 		
 		g = new Grid();
 		g.makeFirstBoard();
 		g.initGravityPoints();
 		
 		lvlText = new FlxText(g.minPoint.x, g.maxPoint.y + Fruit.SIZE_Y + 3, 50, "Level: 1");
 		scoreText = new FlxText(lvlText.x + 50, lvlText.y, 200, "Score: 0");
 		
 		Rg.spinner.alignToGridMember(g.getFirstVisible());
 		
 		//gridPoints for debugging
 		for (int i = 0; i < g.gravityPoints.size(); i++){
 			FlxSprite s = new FlxSprite(g.gravityPoints.get(i).x,
 										g.gravityPoints.get(i).y);
 			s.makeGraphic(2, 2, Color.GRAY.toIntBits());
 			guideGridPoints.add(s);
 		}
 		
 		gameStuff.add(g.bg);
 		gameStuff.add(Rg.spinner);
 		gameStuff.add(g.fruits);
 		gameStuff.add(guideGridPoints);
 		gameStuff.add(scoreText);
 		gameStuff.add(lvlText);
 		
 		//render
 		add(bg);
 		add(gameStuff);
 		add(pauseStuff);
 		add(t);
 		
 		pauseStuff.setAll("visible", false);
 		
 	}
 	
 	@Override
 	public void update(){
 		super.update();
 		
 		Set<Point> lineups = g.findLineups();
 		if ( lineups.size() > 0 ){
 			g.clearLineups(lineups);
 			g.genNewFruits();
 		}
 		
 		//update score text
 		scoreText.setText("Score: " + Rg.curScore);
 		
 		if ( Rg.curScore > levelThres[Rg.curLevel] ){
 			levelUp();
 			pauseGame();
 		}
 		
 		if ( !Rg.paused ){
 			if ( Rg.animLock ){
 				Rg.animLockTimer += FlxG.elapsed;
 				if ( Rg.animLockTimer > Rg.animLockoutTime )
 					Rg.animationUnlock();
 			} else g.updateDrawables();
 			
 			int fingers_down = 0;		
 			justTapped = FlxG.mouse.justPressed();
 			if ( FlxG.mouse.pressed() ){
 				mpos = FlxG.mouse.getWorldPosition();
 				justTapped = false;
 			}
 			
 			for(int i = 0; i < 10; i++){
 				if(FlxG.mouse.justReleased(i)){
 					fingers_down++;
 				}
 			}
 			
 			if ( fingers_down == 1 ){
 				FlxPoint moveHere = g.checkOverlap(mpos);
 				if ( moveHere != null ){
 					boolean moved = Rg.spinner.move(moveHere, 
 							                    g.getCollidePosFromPoint(moveHere));
 					if (!moved){
 						g.rotateFruits();
 						Rg.curRotations++;
 					}
 				}
 				justTapped = false;
 			}
 			
 			if (fingers_down == 3){
 				g.makeFirstBoard();
 				Rg.spinner.alignToGridMember(g.getFirstVisible());
 				justTapped = false;
 			}
 			
 		}
 	}
 	
 	public void levelUp(){
 		Rg.curLevel++;
 		if ( Rg.curLevel == 10 ){
 			FlxG.log("You're my hero! How long did this take you to do?");
 		}
 	}
 	
 	public void pauseGame(){
 		Rg.paused = true;
 		gameStuff.setAll("visible", false);
 		pauseStuff.setAll("visible", true);
 	}
 	
 	public void unpauseGame(){
 		Rg.paused = false;
 		gameStuff.setAll("visible", true);
 		pauseStuff.setAll("visible", false);
 		g.setOffscreenVisible(false);
 	}
 	
 	IFlxButton okIBtn = new IFlxButton() {
 		
 		@Override
 		public void callback() {
 			FlxU.openURL("http://goo.gl/nbjopi");
 		}
 	};
 	
 	IFlxButton cancelIBtn = new IFlxButton() {
 		@Override
 		public void callback() {
 			FlxG.log("Unpausing Game...");
 			unpauseGame();
 		}
 	};
 	
 
 }
