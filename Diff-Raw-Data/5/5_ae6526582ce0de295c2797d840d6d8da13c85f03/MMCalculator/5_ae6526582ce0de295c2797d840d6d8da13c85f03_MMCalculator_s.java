 package org.usfirst.frc1318.minimike.calculators;
 
 import org.usfirst.frc1318.components.RobotComponentBase;
import org.usfirst.frc1318.components.minimike.MMGamePadReader;
import org.usfirst.frc1318.shared.minimike.MMGamePadData;
import org.usfirst.frc1318.shared.minimike.MMLimitSwitchData;
 import org.usfirst.frc1318.shared.minimike.MMTurretData;
 import org.usfirst.frc1318.utils.DriverStationPrint;
 
 public class MMCalculator extends RobotComponentBase {
 	
 	DriverStationPrint driverStationPrint;
 	boolean useGamePad;
 	long timmer = 0;
 	long distanceTime = timmer / 2;
 	long movementTimmer = 0;
 	int lastSwitchHit = 0; // left = 1; right = 2; none = 0
 	
 	public MMCalculator(){
 	}
 	
 	public void robotInit(){
 		driverStationPrint = new DriverStationPrint();
 	}
 	
 	public void teleopPeriodic(){
 		
 	}
 	
 	public void setUseGamePad(boolean newValue){
 		this.useGamePad = newValue;
 	}
 	
 	public boolean getUseGamePad(){
 		return useGamePad;
 	}
 	
 	public boolean canMove(){
 		if(MMGamePadData.getLeftButton()&& MMGamePadData.getRightButton()){
 			return false;
 		}else if(!MMGamePadData.getLeftButton() && !MMGamePadData.getRightButton()){
 			return false;
 		}else if(MMLimitSwitchData.getData() == MMLimitSwitchData.ERROR){
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean canMoveRight(){
 		if(MMLimitSwitchData.getData() == MMLimitSwitchData.HIT_RIGHT && this.canMove()){
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean canMoveLeft(){
 		if(MMLimitSwitchData.getData() == MMLimitSwitchData.HIT_LEFT && this.canMove()){
 			return false;
 		}
 		return true;
 	}
 	
 }
