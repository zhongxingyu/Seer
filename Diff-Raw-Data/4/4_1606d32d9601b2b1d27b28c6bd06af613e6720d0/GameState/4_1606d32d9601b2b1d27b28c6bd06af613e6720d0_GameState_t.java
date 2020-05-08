 package no.perry.pong;
 
 
 import sheep.collision.CollisionLayer;
 import sheep.collision.CollisionListener;
 import sheep.game.Sprite;
 import sheep.game.State;
 import sheep.game.World;
 import sheep.graphics.Font;
 import sheep.graphics.Color;
 import sheep.graphics.Image;
 import sheep.input.TouchListener;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 
 
 /**
  * @author Perry
  */
 public class GameState extends State implements TouchListener, CollisionListener {
 
 	private int canvasHeight, canvasWidth, pl1Score, pl2Score, winner = 0;
 	private Sprite paddle1, paddle2;
 	private Ball ball;
 	private Paint paint;
 	private Canvas can;
 	private Display display;
 	private CollisionLayer collisionLayer = new CollisionLayer();
 	private World world = new World();
 	private Image paddle1Image = new Image(R.drawable.paddle1);
 	private Image paddle2Image = new Image(R.drawable.paddle2);
 	private Image ballImage = new Image(R.drawable.ball);
 
 
 	
 	/**
 	 * Constructor, add paddles and a ball to the canvas
 	 * @param display Display data, for adjusting the objects before anything is drawn
 	 */
 	public GameState(Display display) {
 		this.display = display;
 		
 		paddle1 = new Sprite(paddle1Image);
 		paddle2 = new Sprite(paddle2Image);
 		ball = Ball.instance(ballImage);
 
 		collisionLayer.addSprite(paddle1);
 		collisionLayer.addSprite(paddle2);
 		collisionLayer.addSprite(ball);
 
 		
		pl1Score = 0;
		pl2Score = 0;
 		paddle1.setPosition(display.getHeight()/2, display.getHeight()/4);
 		paddle2.setPosition(display.getHeight()/2, display.getHeight()-(display.getHeight()/4));
 
 		ball.setPosition(display.getWidth()/2, display.getHeight()/2);
 		ball.setSpeed(100, 100);
 		paddle1.setXSpeed(200);
 
 		paddle1.addCollisionListener(this);
 		paddle2.addCollisionListener(this);
 		ball.addCollisionListener(this);
 
 		world.addLayer(collisionLayer);
 	}
 
 	
 	public void draw(Canvas canvas){
 		can = canvas;
 
 		Color c = new Color(255,255,255);
 		paint = new Paint(c);
 		paint.setStyle(Style.FILL);
 		can.drawPaint(paint);
 		canvasHeight = can.getHeight();
 		canvasWidth = can.getWidth();
 
 		paddle1.draw(can);
 		paddle2.draw(can);
 
 		Font font1 = new Font(0, 255, 0, 30, Typeface.SERIF, Typeface.NORMAL);
 		Font font2 = new Font(255, 0, 0, 30, Typeface.SERIF, Typeface.NORMAL);
 		font1.setTextAlign(Align.CENTER);
 		font2.setTextAlign(Align.CENTER);
 
 		can.drawText("Score player 1: "+pl1Score, canvasWidth/2, canvasHeight/2-20, font1);
 		can.drawText("Score player 2: "+pl2Score, canvasWidth/2, canvasHeight/2+20, font2);
 
 		ball.draw(can);
 		world.draw(can);
 	}
 
 
 	public void update(float dt) {
 
 		if(can != null){
 			if(ball.getX()>=(canvasWidth-ballImage.getWidth()/2) || ball.getX()<=(ballImage.getWidth()/2)){
 				ball.setSpeed(-ball.getSpeed().getX(), ball.getSpeed().getY());
 			}
 
 			if(pl1Score<21 && pl2Score<21){
 				if(ball.getY()>=(canvasHeight-ballImage.getHeight())){
 					ball.setPosition(display.getWidth()/2, display.getHeight()/2);
 					ball.setYSpeed(-ball.getSpeed().getY());
 					pl1Score++;
 
 
 				}else if(ball.getY()<=0){
 					ball.setPosition(display.getWidth()/2, display.getHeight()/2);
 					ball.setYSpeed(-ball.getSpeed().getY());
 					pl2Score++; 
 				}
 			}else{
 				if(pl1Score == 21){ winner = 2; }
 				if(pl2Score == 21){ winner = 1; }
 				getGame().popState();
 				getGame().pushState(new GameOverState(winner, display));
 			}
 
 			if(paddle1.getX()>=0 && paddle1.getX()<=canvasWidth){
 				if(ball.getX()>paddle1.getX()){
 					paddle1.setXSpeed(200);
 				}else if(ball.getX()<paddle1.getX()){
 					paddle1.setXSpeed(-200);
 				}
 			}else{
 				paddle1.setXSpeed(-paddle1.getSpeed().getX());
 			}
 
 			ball.update(dt);
 			paddle2.update(dt);
 			world.update(dt);
 		}
 	}
 
 	public boolean onTouchMove(MotionEvent event) {
 			if(event.getX()<(display.getWidth()-(paddle2Image.getWidth()/2)) && event.getX()>(paddle2Image.getWidth()/2)){
 				if(event.getY()>(display.getHeight()-(display.getHeight()/4)) && event.getY()<(display.getHeight())){
 					paddle2.setPosition(event.getX(), paddle2.getPosition().getY());
 					return true;
 				}
 			}
 
 		return false;
 	}
 
 	public void collided(Sprite a, Sprite b) {
 		Log.i("collision", "collision!");
 		if(ball.getY()>display.getHeight()-(display.getHeight()/4)-paddle1Image.getHeight()-(ballImage.getHeight()/2)){
 			ball.setYSpeed(-ball.getSpeed().getY());
 			ball.setPosition(ball.getX(), ball.getY()-(ballImage.getHeight()/2));
 		}
 		if(ball.getY()<(display.getHeight()/4)+paddle1Image.getHeight()+(ballImage.getHeight()/2)){
 			ball.setYSpeed(-ball.getSpeed().getY());
 			ball.setPosition(ball.getX(), ball.getY()+(ballImage.getHeight()/2));
 		}
 	}
 
 
 }
