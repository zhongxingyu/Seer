 package org.usfirst.frc1318.components;
 
 import org.usfirst.frc1318.reference.*;
 import org.usfirst.frc1318.reference.PS2Controller.PS2ButtonMap;
 import org.usfirst.frc1318.shared.ButtonData;
 import org.usfirst.frc1318.shared.KinematicData;
 import org.usfirst.frc1318.shared.ReferenceData;
 import org.usfirst.frc1318.shared.TBGamepadData;
 import org.usfirst.frc1318.shared.constants.PortRef;
 import org.usfirst.frc1318.shared.constants.TBButtonRef;
 
 import com.sun.squawk.util.MathUtils;
 
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 
 public class TBGamePadReader {
 	public static final int NUM_GAME_PADS = 1;
 	private ButtonData[] buttons = new ButtonData[NUM_GAME_PADS + 1];
 	
 	public interface ButtonMap {
 		int COMPRESSOR_TOGGLE = PS2Controller.PS2ButtonMap.CIRCLE;
 		int KICK_BUTTON = PS2ButtonMap.X;
 		int MODE_TOGGLE = PS2ButtonMap.TRIANGLE;
 	}
 
 	public void init() {
 		setToggles();
 	}
 	
 	/**
 	 * Buttons default to be press and hold. This method sets buttons that are toggles as such.
 	 */
 	private void setToggles() {
		buttons[ButtonMap.COMPRESSOR_TOGGLE].setButtonType(TBButtonRef.COMPRESSOR_TOGGLE, TBButtonRef.TOGGLE);
		buttons[ButtonMap.MODE_TOGGLE].setButtonType(TBButtonRef.MODE_TOGGLE, TBButtonRef.TOGGLE);
 	}
 	
 	public void read() {
 		synchronized (Sensors.getInstance().getPilotGamePad()) {
 			GamePad pilot = Sensors.getInstance().getPilotGamePad();
 			axisVals[PILOT_JS][GamePad.AXIS_X1] = scale.scale(accountForDeadZone(pilot.getXLeft()));
 			axisVals[PILOT_JS][GamePad.AXIS_X2] = scale.scale(accountForDeadZone(pilot.getXRight()));
 			axisVals[PILOT_JS][GamePad.AXIS_Y1] = scale.scale(accountForDeadZone(pilot.getYLeft()));
 			axisVals[PILOT_JS][GamePad.AXIS_Y2] = scale.scale(accountForDeadZone(pilot.getYRight()));
 			if(previousPilotDPad != pilotDPad) {
 				previousPilotDPad = pilotDPad;
 			}
 			pilotDPad = pilot.getDPad();
 			for(int i = 1; i <= GamePad.NUM_BUTTONS; i++) {
 				buttons[PILOT_JS].isPressed(i, pilot.getRawButton(i));
 			}
 		}
 		synchronized (Sensors.getInstance().getCopilotGamePad()) {
 			GamePad copilot = Sensors.getInstance().getCopilotGamePad();
 			axisVals[COPILOT_JS][GamePad.AXIS_X1] = scale.scale(accountForDeadZone(copilot.getXLeft()));
 			axisVals[COPILOT_JS][GamePad.AXIS_X2] = cubic.scale(copilot.getXRight());//turret
 			axisVals[COPILOT_JS][GamePad.AXIS_Y1] = scale.scale(accountForDeadZone(copilot.getYLeft()));
 			axisVals[COPILOT_JS][GamePad.AXIS_Y2] = scale.scale(accountForDeadZone(copilot.getYRight()));
 			
 			if(previousCopilotDPad != copilotDPad) {
 				previousCopilotDPad = copilotDPad;
 			}
 			copilotDPad = copilot.getDPad();
 
 
 			for(int i = 1; i <= GamePad.NUM_BUTTONS; i++) {
 				buttons[COPILOT_JS].isPressed(i, copilot.getRawButton(i));
 			}
 		}		
 		writeVals();
 	}
 
 	private void writeVals() {
 		synchronized (ReferenceData.getInstance()) {
 			JoystickSetData joystickSetData = ReferenceData.getInstance().getJoystickSetData();
 			copyVals(joystickSetData);
 			writeConveyors(joystickSetData);
 			writeShooter(joystickSetData, ReferenceData.getInstance().getAimingInputs());	
 			writeTurret(joystickSetData, ReferenceData.getInstance().getAimingInputs());
 			writeGyro(joystickSetData);
 			writeMecanumDrive(joystickSetData.getJoystickData(ButtonRef.MECANUM_V_JS));
 			writeShotHistory(ReferenceData.getInstance().getShotHistory());
 			writeRangeOverride(ReferenceData.getInstance());
 		}
 		synchronized (KinematicData.getInstance()) {
 			writeLowerConveyors(KinematicData.getInstance().getLoaderBeltSpeeds());
 			writeTipperSpeed(KinematicData.getInstance());
 		}
 	
 	}
 	private void copyVals(JoystickSetData joystickSetData) {
 			buttons[PILOT_JS].copyTo(joystickSetData.getJoystickData(PILOT_JS).getButtons());
 			buttons[COPILOT_JS].copyTo(joystickSetData.getJoystickData(COPILOT_JS).getButtons());		
 	}
 	
 	private void writeMode(TBGamepadData gpData) {
 		
 	}
 	
 	private void writeKick(TBGamepadData gpData) {
 		
 	}
 	
 	private void writeCompressor(TBGamepadData gpData) {
 		
 	}
 }
