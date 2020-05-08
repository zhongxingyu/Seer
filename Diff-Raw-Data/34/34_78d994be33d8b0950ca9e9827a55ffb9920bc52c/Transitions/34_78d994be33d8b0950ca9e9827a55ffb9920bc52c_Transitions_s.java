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
		return new FadeOutTransition(Color.white, 400);
 	}
 
 	public static Transition fadeIn() {
		return new FadeInTransition(Color.white, 100);
 	}
 }
