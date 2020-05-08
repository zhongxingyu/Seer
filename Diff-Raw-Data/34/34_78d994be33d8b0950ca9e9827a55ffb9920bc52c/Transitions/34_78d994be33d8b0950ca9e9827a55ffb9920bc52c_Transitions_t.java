 /**
  * 
  */
 package ui;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 import org.newdawn.slick.state.transition.Transition;
 
 /**
  * @author Mullings
  *
  */
 public class Transitions {
 
 	public static Transition fadeOut() {
		return fadeOut(Color.white);
	}
	
	public static Transition fadeOut(Color color) {
		return new FadeOutTransition(color, 400);
 	}
 
 	public static Transition fadeIn() {
		return fadeIn(Color.white);
	}

	public static Transition fadeIn(Color color) {
		return new FadeInTransition(color, 100);
 	}
 }
