 package com.github.joakimpersson.tda367.controller.utils;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 import org.newdawn.slick.state.transition.Transition;
 
 /**
 * A simple class for util methods in the controller package
  * 
  * @author joakimpersson
  * 
  */
 public class ControllerUtils {
 	private ControllerUtils() {
 	}

 	/**
 	 * Responsible for changing into an new state
 	 * 
 	 * @param game
 	 *            The Bomberman game container
 	 * @param newState
 	 */
 	public static void changeState(StateBasedGame game, int newState) {
 		Transition fadeIn = new FadeInTransition(Color.cyan, 500);
 		Transition fadeOut = new FadeOutTransition(Color.cyan, 500);
 		game.enterState(newState, fadeOut, fadeIn);
 	}
 
 	/**
 	 * Clear everything in the input queue from previous states
 	 * 
 	 * @param input
 	 *            The input method used by the slick framework that contains the
 	 *            latest action
 	 */
 	public static void clearInputQueue(Input input) {
 		input.clearControlPressedRecord();
 		input.clearKeyPressedRecord();
 		input.clearMousePressedRecord();
 	}
 }
