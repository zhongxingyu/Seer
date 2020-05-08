 package zed;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.*;
 
 public class Game extends StateBasedGame {
 	public static final String gamename = "zed";
 	public static final int menu = 0;
 	public static final int zed = 1;
 	public static final int gameover = 2;
 	public static final int victory = 3;
 	
 	public Game() {
 		super("Zed");
 		this.addState(new Menu(menu));
 		this.addState(new Zed(zed));
 		this.addState(new GameOver(gameover));
 		this.addState(new Victory(victory));
 	}
 	
 	public Game(String gamename) {
 		super(gamename);
 		this.addState(new Menu(menu));
 		this.addState(new Zed(zed));
 		this.addState(new GameOver(gameover));
 		this.addState(new Victory(victory));
 	}
 	
 	public void initStatesList(GameContainer gc) throws SlickException {
		this.getState(zed).init(gc, this);
 		this.getState(menu).init(gc,  this);
 		this.getState(gameover).init(gc, this);
 		this.getState(victory).init(gc,this);

 		this.enterState(menu);
 	}
 	
 	public static void main(String[] args){
 		AppGameContainer appgc;
 		try{appgc = new AppGameContainer(new Game(gamename));
 		//gc.setDisplayMode(640, 360, false);
 			appgc.start();
 		}catch(SlickException e){
 			e.printStackTrace();
 			Logger.getLogger(Zed.class.getName()).log(Level.SEVERE,null, e);			
 		}
 		
 	}
 }
