 import java.util.Random;
 import java.util.Vector;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 public class Level {
 	public Vector<Ball> balls;
 	public Vector<Brick> bricks;
 	public Pad pad;
 	public Vector<Bonus> bonuses;
 	private int lifes = 5;
 	private Input input;
 	private long timerText = -1;
 	private final int delay = 4;
 	private final int fps = Main.getFPS();
 	private TrueTypeFont dramatic;
 	
 	public Level ( GameContainer gc ) {
 		java.awt.Font f = new java.awt.Font( "Times New Roman", java.awt.Font.PLAIN, 50 );
 		dramatic = new TrueTypeFont( f, true ); 
 		input = gc.getInput();
 		Ball.setMaxVelocity(7.3f);
 		Bonus.setVelocity(5.0f);
 		bonuses = new Vector<Bonus>();
 		generateBalls();
 		generateBricks();
 		generatePad();
 	}
 	
 	public void update(GameContainer gc, int delta) throws SlickException {
 		if ( timerText == 0 && lifes < 0) {
 			Main.exitToMenu();
 		}
 		if ( timerText != -1 ) {
 			timerText--;
 			return;
 		}
 		pad.update(gc,delta);
 		for ( int i = 0, size = balls.size(); i < balls.size(); i++ ) {
 			balls.get(i).update( gc, delta );
 			if ( size != balls.size() )
 				i--;
 			size = balls.size();
 		}
 		if ( balls.size() == 0 )
 			die();
 		for ( Brick d : bricks ) {
 			d.update(gc, delta);
 		}
 		if ( bricks.size() == 0 )
 			timerText = fps*delay;
 		for ( int i = 0; i < bonuses.size(); i++ ) {
 			if ( !bonuses.get(i).update(gc, delta) )
 				i--;
 		}
 			
 	}
 
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		if ( lifes < 0 ) {
 			drawDramaticText( gc, g, "You are dead!!!", Color.red, new Vector2f( gc.getWidth()/4, 3*gc.getHeight()/7));
 			return;
 		}
 		if ( bricks.size() == 0 ) {
 			drawDramaticText( gc, g, "You won!!!", Color.green, new Vector2f( gc.getWidth()/3, 3*gc.getHeight()/7));
 			return;
 		}
 		if ( timerText >= 0 ) {
 			drawDramaticText( gc, g, "Game starts in : " + timerText/fps, Color.white, new Vector2f( gc.getWidth()/5, gc.getHeight()/2 - 20));
 		}
 		for ( Bonus b : bonuses )
 			b.render(gc, g);
 		for ( Ball b : balls )
 			b.render( gc, g );
 		for ( Brick d : bricks )
 			d.render(gc, g);
 		g.setColor( Color.white );
 		g.drawString("Lifes : " + lifes, 700, 20);
 		pad.render(gc, g);
 	}
 	
 	public void die() {
 		lifes--;
 		timerText = fps*delay;
 		if ( lifes < 0 ) {
 			return;
 		}
 		generateBalls();
 		generatePad();
 		
 	}
 	
 	public int getLowestBrick( float x ) {
 		float max = Integer.MIN_VALUE;
 		for ( Brick br : bricks ) {
 			Rectangle r = br.getRectangle();
 			if ( r.getMaxY() > max && r.contains( x, r.getCenterY()))
 				max = br.getRectangle().getMaxY();
 		}
 		return (int) max;
 	}
 	
 	private void generateBricks() {
 		bricks = new Vector<Brick>();
 		for(int i=0;i<6;i++) {
 			for(int j=0;j<8;j++) {
 				Random rand=new Random();
 				float appear=rand.nextFloat();
 				if(appear>0.5) {
 					float col1=rand.nextFloat();
 					float col2=rand.nextFloat();
 					float col3=rand.nextFloat();
 					bricks.add(new Brick(new Vector2f(40+i*60,j*20+20),new Vector2f(58,18),new Color(col1,col2,col3)));
 					bricks.add(new Brick(new Vector2f(740-40-i*60,j*20+20),new Vector2f(58,18),new Color(col1,col2,col3)));
 					bricks.add(new Brick(new Vector2f(40+i*60,280-j*20-20),new Vector2f(58,18),new Color(col1,col2,col3)));
 					bricks.add(new Brick(new Vector2f(740-40-i*60,280-j*20-20),new Vector2f(58,18),new Color(col1,col2,col3)));
 				}
 			}
 		}
 	}
 	
 	private void generateBalls() {
 		balls = new Vector<Ball>();
		balls.add(new Ball( this , new Vector2f ( input.getMouseX(), 500 ), 20, new Color ( 1.0f , 0.0f, 0.0f ) ));
 		for( Ball b : balls )
 			b.setVellocity( new Vector2f ( 0.9f, -4.3f ) );
 	}
 	
 	private void generatePad ( ) {
 		pad = new Pad(
 				new Vector2f( input.getMouseX(), 550 ),
 				new Vector2f( 70, 20 ));
 	}
 	
 	private void drawDramaticText( GameContainer gc, Graphics g, String str, Color c, Vector2f pos ){
 		Font f = g.getFont();
 		g.setColor( c );
 		g.setFont( dramatic );
 		g.drawString( str, pos.x, pos.y );
 		g.setFont(f);
 	}
 }
