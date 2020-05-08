 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 
 import javax.swing.JPanel;
 //TODO: fix diverter arm being blocked by diverter platform
 
 @SuppressWarnings("serial")
 public class LaneDemo extends JPanel
 {
 	WholeLane lane1;
 	Part p1;
 	Part p2;
 	GUILane gl;
 	GUIPart gp1;
 	GUIPart gp2;
 	GUIFeeder gf;
 	GUIDiverter gd;
 	GUIDiverterArm gda;
 	GUIBin gpb;
 	GUIPart gp3;
 	GUIPart gp4;
 	GUIPart lastPart;
 	
 	int paintCount = 0;
 	
 	public LaneDemo()
 	{
 		//panel size
 		this.setPreferredSize(new Dimension(800,400));	
 		
 		//load images
 		Painter.loadImages();
 		
 		//initialize
 		lane1 = new WholeLane();
 		gl = new GUILane( lane1.getLane(), true, 5, 90, 110 );
 		gl.turnOn(System.currentTimeMillis());
 		gf = new GUIFeeder( lane1.getFeeder(), 470, 110 );
 		gd = new GUIDiverter( 390, 112 );
 		gda = new GUIDiverterArm( 390, 155 );
 		gda.movement = new Movement(new Point2D.Double(390,155), 0, System.currentTimeMillis(), new Point2D.Double(390,155), 0.7, System.currentTimeMillis()+1);
 		gpb = null;
 		p1 = new Part("p1", "a random part", 5, Painter.ImageEnum.CORNFLAKE);
 		p2 = new Part("p2", "another random part", 6, Painter.ImageEnum.NUT);
 		
 		//parts that are not shown
 		gp3 = new GUIPart(p1, 460, 150);
 		gp4 = new GUIPart(p2, 460, 150);
 		lastPart = null;
 	}
 	
 	public void paint(Graphics gfx)
 	{
 		Graphics2D g = (Graphics2D)gfx;
 		
 		if (gl.shouldReset(System.currentTimeMillis())) gl.reset(System.currentTimeMillis());
 		
 		if( lane1.getActiveLane() == 1 ){ //if top lane
 			//top lane moving parts
 			if( lastPart != null && lastPart == gp3 ){ //box had cornflakes
 				if (paintCount % 200 == 20 || paintCount % 200 == 120 ) { //from diverter moving to lane
 					gp1 = new GUIPart(p1, 460, 150);
 					gp1.movement = Movement.fromSpeed(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(390,120), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 40 || paintCount % 200 == 140 ) { //moving on lane
 					gp1 = new GUIPart(p1, 390, 120);
 					gp1.movement = Movement.fromSpeed(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(70,120), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 70 || paintCount % 200 == 170 ) { //from diverter moving to lane
 					gp2 = new GUIPart(p1, 460, 150);
 					gp2.movement = Movement.fromSpeed(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(390,120), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 90 || paintCount % 200 == 190 ) { //moving on lane
 					gp2 = new GUIPart(p1, 390, 120);
 					gp2.movement = Movement.fromSpeed(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(70,120), 0, lane1.getSpeed());
 				}
 			} else if (lastPart != null && lastPart == gp4) { //box had nuts
 				if (paintCount % 200 == 20 || paintCount % 200 == 120 ) { //from diverter moving to lane
 					gp1 = new GUIPart(p2, 460, 150);
 					gp1.movement = Movement.fromSpeed(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(390,120), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 40 || paintCount % 200 == 140 ) { //moving on lane
 					gp1 = new GUIPart(p2, 390, 120);
 					gp1.movement = Movement.fromSpeed(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(70,120), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 70 || paintCount % 200 == 170 ) { //from diverter moving to lane
 					gp2 = new GUIPart(p2, 460, 150);
 					gp2.movement = Movement.fromSpeed(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(390,120), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 90 || paintCount % 200 == 190 ) { //moving on lane
 					gp2 = new GUIPart(p2, 390, 120);
 					gp2.movement = Movement.fromSpeed(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(70,120), 0, lane1.getSpeed());
 				}
 			} else { //no parts yet
 				gp1 = null;
 				gp2 = null;
 			}
 			
 		} else { //bottom lane
 			//bot lane moving parts
 			if( lastPart != null && lastPart == gp3 ){ //box had cornflakes
 				if (paintCount % 200 == 20 || paintCount % 200 == 120 ) { //from diverter moving to lane
 					gp1 = new GUIPart(p1, 460, 150);
 					gp1.movement = Movement.fromSpeed(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(390,180), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 40 || paintCount % 200 == 140 ) { //moving on lane
 					gp1 = new GUIPart(p1, 390, 180);
 					gp1.movement = Movement.fromSpeed(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(70,180), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 70 || paintCount % 200 == 170 ) { //from diverter moving to lane
 					gp2 = new GUIPart(p1, 460, 150);
 					gp2.movement = Movement.fromSpeed(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(390,180), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 90 || paintCount % 200 == 190 ) { //moving on lane
 					gp2 = new GUIPart(p1, 390, 180);
 					gp2.movement = Movement.fromSpeed(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(70,180), 0, lane1.getSpeed());
 				}
 			} else if( lastPart != null && lastPart == gp4 ){ //box had nuts
 				if (paintCount % 200 == 20 || paintCount % 200 == 120 ) { //from diverter moving to lane
 					gp1 = new GUIPart(p2, 460, 150);
 					gp1.movement = Movement.fromSpeed(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(390,180), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 40 || paintCount % 200 == 140 ) { //moving on lane
 					gp1 = new GUIPart(p2, 390, 180);
 					gp1.movement = Movement.fromSpeed(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(70,180), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 70 || paintCount % 200 == 170 ) { //from diverter moving to lane
 					gp2 = new GUIPart(p2, 460, 150);
 					gp2.movement = Movement.fromSpeed(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(390,180), 0, lane1.getSpeed());
 				}
 				
 				if (paintCount % 200 == 90 || paintCount % 200 == 190 ) { //moving on lane
 					gp2 = new GUIPart(p2, 390, 180);
 					gp2.movement = Movement.fromSpeed(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(70,180), 0, lane1.getSpeed());
 				}
 			} else { //no parts yet
 				gp1 = null;
 				gp2 = null;
 			}
 		}
 		
 		//change lane every 5, 105
 		if( paintCount % 200 == 5 || paintCount % 200 == 105){
 			lane1.divert();
 			if(lane1.getActiveLane() == 1) { //arm rotates from top to bot
 				gda.movement = new Movement(new Point2D.Double(390,155), -0.7, System.currentTimeMillis(), new Point2D.Double(390,155), 0.7, System.currentTimeMillis()+500);
 			} else { //arm rotates from bot to top
 				gda.movement = new Movement(new Point2D.Double(390,155), 0.7, System.currentTimeMillis(), new Point2D.Double(390,155), -0.7, System.currentTimeMillis()+500);
 			}
 		}
 		
 		gl.draw(g, System.currentTimeMillis());
 		
 		gf.draw(g, System.currentTimeMillis());
 		
 		gd.draw(g, System.currentTimeMillis());
 		
 		gda.draw(g, System.currentTimeMillis());
 		
 		//make box appear
 		if( paintCount % 200 == 0 ) { //cornflakes
			gpb = new GUIBin( /*gp3,*/ new Bin(gp3.part, 10), 512, 110 );
 			lastPart = gp3;
 		}
 		
 		if( paintCount % 200 == 100 ) { //nuts
			gpb = new GUIBin( /*gp4,*/ new Bin(gp3.part, 10), 512, 110 );
 			lastPart = gp4;
 		}
 		
 		//make box disappear
 		if( paintCount % 200 == 20 || paintCount % 200 == 120)
 			gpb = null;
 		
 		if( gpb != null )
 			gpb.draw(g, System.currentTimeMillis(), false);
 		
 		if( gp1 != null )
 			gp1.draw(g, System.currentTimeMillis());
 		
 		if( gp2 != null )
 			gp2.draw(g, System.currentTimeMillis());
 		
 //		if (paintCount % 100 == 75)
 //			if ( lane1.areLanesOn() )
 //				lane1.turnOffLane();
 //			else
 //				lane1.turnOnLane();
 		
 		
 		paintCount++;
 	}
 	
 }
