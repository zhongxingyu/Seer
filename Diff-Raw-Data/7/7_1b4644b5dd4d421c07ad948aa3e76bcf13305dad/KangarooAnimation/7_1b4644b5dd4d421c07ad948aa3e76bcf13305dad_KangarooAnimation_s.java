 package se.chalmers.kangaroo.view;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import se.chalmers.kangaroo.model.kangaroo.Kangaroo;
 import se.chalmers.kangaroo.model.utils.Direction;
 /**
  * A class representing the animation of the Kangaroo.
  * @author simonal
  *
  */
 public class KangarooAnimation implements Animation {
 
 	private Image rightSheet;
 	private Image leftSheet;
 	private String lastSheet;
 	private int widthPerFrame;
 	private int height;
 	private int tick;
 	private int currentFrame;
 	private Kangaroo kangaroo;
 	/**
 	 * Constructor taking the pathname and width and height of the animation.
 	 * @param pathName
 	 * @param width
 	 * @param height
 	 */
 	public KangarooAnimation(Kangaroo k,int width, int height) {
 		rightSheet = Toolkit.getDefaultToolkit().getImage("resources/gfx/sheets/kangaroo_58x64_right.png");
 		leftSheet = Toolkit.getDefaultToolkit().getImage("resources/gfx/sheets/kangaroo_58x64_left.png");
 		lastSheet = "rightSheet";
 		currentFrame = 0;
 		this.widthPerFrame = width;
 		this.height = height;
 		this.kangaroo = k;
 
 		tick = 0;
 	}
 	/**
 	 * Draws the animation once every half second if the game is running in 60 ups.
 	 */
 	@Override
 	public void drawSprite(Graphics g, int x, int y) {
 		if (tick == 9) {
 			tick = 0;
 			currentFrame++;
 			currentFrame = (currentFrame % 3);
 		}
 		if (kangaroo.getVerticalSpeed() != 0) {
 			if(kangaroo.getDirection() == Direction.DIRECTION_EAST) {
 				g.drawImage(rightSheet, (x-widthPerFrame/2)+10, y-height, (x+widthPerFrame/2)+10, y,
 						widthPerFrame*2, 1, widthPerFrame*3, height, null, null);
 				this.lastSheet = "rightSheet";
 			} else if (kangaroo.getDirection() == Direction.DIRECTION_WEST) {
 				g.drawImage(leftSheet, x-5, y-height, x+widthPerFrame-5, y,
 						widthPerFrame*2, 1, widthPerFrame*3, height, null, null);
 				this.lastSheet = "leftSheet";
 			} else if (kangaroo.getDirection() == Direction.DIRECTION_NONE) {
 				if(lastSheet == "leftSheet") {
					g.drawImage(leftSheet, x-5, y-height, x+widthPerFrame-5, y,
							1, 1, widthPerFrame, height, null, null);
 				}
 				if(lastSheet == "rightSheet") {
 					g.drawImage(rightSheet, (x-widthPerFrame/2)+10, y-height, (x+widthPerFrame/2)+10, y,
							1, 1, widthPerFrame, height, null, null);
 				}
 			}
 		} else if(kangaroo.getDirection() == Direction.DIRECTION_EAST) {
 			g.drawImage(rightSheet, (x-widthPerFrame/2)+10, y-height, (x+widthPerFrame/2)+10, y,
 					(currentFrame * widthPerFrame), 1,
 					(currentFrame * widthPerFrame) + widthPerFrame,
 					height, null, null);
 			this.lastSheet = "rightSheet";
 		} else if (kangaroo.getDirection() == Direction.DIRECTION_WEST) {
 			g.drawImage(leftSheet, x-5, y-height, x+widthPerFrame-5, y,
 					(currentFrame * widthPerFrame), 1,
 					(currentFrame * widthPerFrame) + widthPerFrame,
 					height, null, null);
 			this.lastSheet = "leftSheet";
 		} else if (kangaroo.getDirection() == Direction.DIRECTION_NONE) {
 			if(lastSheet == "leftSheet") {
 				g.drawImage(leftSheet, x-5, y-height, x+widthPerFrame-5, y,
 						1, 1, widthPerFrame, height, null, null);
 			}
 			if(lastSheet == "rightSheet") {
 				g.drawImage(rightSheet, (x-widthPerFrame/2)+10, y-height, (x+widthPerFrame/2)+10, y,
 						1, 1, widthPerFrame, height, null, null);
 			}
 		}
 		tick++;
 
 	}
 
 }
