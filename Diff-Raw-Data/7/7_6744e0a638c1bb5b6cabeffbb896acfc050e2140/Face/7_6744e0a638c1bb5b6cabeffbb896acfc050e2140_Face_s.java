 package simpleclock;
 
 import java.awt.Color;
 
 public class Face extends Sprite {
 
 	private CenterPeg centerPeg;
 	private OuterRim outerRim;
 	private Numbers numbers;
 	private TickMarkRing minuteTickMarks;
 	private TickMarkRing hourTickMarks;
 
 	public Face(MyPApplet app) {
 		super(app);
 		centerPeg = new CenterPeg(app, Color.BLACK, 3);
 		outerRim = new OuterRim(app, Color.BLACK, 300, 4);
 		numbers = new Numbers(app, Color.BLACK);
 		minuteTickMarks = new TickMarkRing(app, Color.BLACK, 5, 150, 1, 60);
 		hourTickMarks = new TickMarkRing(app, Color.BLACK, 10, 150, 3, 12);
 	}
 
 	public void render() {
 		app.pushMatrix();
 
 		outerRim.draw();
 		minuteTickMarks.draw();
 		hourTickMarks.draw();
 		centerPeg.draw();
 		numbers.draw();
 
 		app.popMatrix();
 	}

 }
