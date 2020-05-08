 package org.malai.wiimote.interaction;
 
 import org.malai.interaction.InitState;
 import org.malai.interaction.Interaction;
 import org.malai.stateMachine.ITransition;
 
 import wiiusej.wiiusejevents.physicalevents.ButtonsEvent;
 import wiiusej.wiiusejevents.physicalevents.IREvent;
 import wiiusej.wiiusejevents.physicalevents.JoystickEvent;
 import wiiusej.wiiusejevents.physicalevents.MotionSensingEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.DisconnectionEvent;
 
 public class WiimoteInteraction extends Interaction implements WiimoteEventHandler {
 
 	public WiimoteInteraction() {
 		super();
 	}
 	
 	public WiimoteInteraction(final InitState initState) {
 		super(initState);
 	}
 	
 	@Override
 	protected void initStateMachine() {
 		// TODO Auto-generated method stub
 	}
 	
 	public void onButtonPressed(ButtonsEvent button) {
 		if(!activated) return ;
 
 		boolean again = true;
 		ITransition t;
 
 		for(int i=0, j=currentState.getTransitions().size(); i<j && again; i++) {
 			t = currentState.getTransition(i);
 
 			if(t instanceof ButtonPressedTransition) {
 				((ButtonPressedTransition)t).setButton(button);
 				again = !checkTransition(t);
 			}
 		}
 		
 	}
 
 	public void onDisconnection(DisconnectionEvent disconnection) {
		if(!activated) return ;		
 	}
 
 	public void onMotionSensing(MotionSensingEvent motion) {
 		if(!activated) return ;
 
 		boolean again = true;
 		ITransition t;
 
 		for(int i=0, j=currentState.getTransitions().size(); i<j && again; i++) {
 			t = currentState.getTransition(i);
 
 			if(t instanceof MotionSensingTransition) {
 				((MotionSensingTransition)t).setMotion(motion);
 				again = !checkTransition(t);
 			}
 		}
 	}
 
 	public void onJoystickMove(JoystickEvent joystick) {
 		if(!activated) return ;
 
 		boolean again = true;
 		ITransition t;
 
 		for(int i=0, j=currentState.getTransitions().size(); i<j && again; i++) {
 			t = currentState.getTransition(i);
 
 			if(t instanceof JoystickMoveTransition) {
 				((JoystickMoveTransition)t).setJoystick(joystick);
 				again = !checkTransition(t);
 			}
 		}
 	}
 
 	public void onIrEvent(IREvent ir) {
 		if(!activated) return ;
 
 		boolean again = true;
 		ITransition t;
 
 		for(int i=0, j=currentState.getTransitions().size(); i<j && again; i++) {
 			t = currentState.getTransition(i);
 
 			if(t instanceof IRTransition) {
 				((IRTransition)t).setIR(ir);
 				again = !checkTransition(t);
 			}
 		}
 	}
 
 }
