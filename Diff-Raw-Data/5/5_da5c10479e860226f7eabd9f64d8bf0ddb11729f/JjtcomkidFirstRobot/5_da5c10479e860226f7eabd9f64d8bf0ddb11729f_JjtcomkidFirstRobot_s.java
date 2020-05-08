 package Jjtcomkid;
 
 import robocode.*;
 
 import java.awt.Color;
 
 public class JjtcomkidFirstRobot extends AdvancedRobot {
 	public boolean shoot = false;
 
 	public void run() {
 		setColors(Color.black, Color.green, Color.red);
 		setAdjustGunForRobotTurn(true);
 		// setAdjustRadarForGunTurn(true);
 		while (true) {
 			// setTurnRadarLeft(30);
 			if (getEnergy() > 25) {
 				ahead(200);
 				back(200);
 			} else {
 				setTurnRight(10000);
 				ahead(10000);
 			}
 		}
 	}
 
 	public void onScannedRobot(ScannedRobotEvent event) {
 		fire(3);
 		// setTurnGunLeft(event.getHeading()- this.getGunHeading());
 	}
 
 	public void onHitByBullet(HitByBulletEvent event) {
 		double gunHeading = getGunHeading();
 		double heading = event.getHeading();
 		if (heading - 180 < 0) {
			setTurnGunLeft(gunHeading - (heading + 180));
 		} else
			setTurnGunLeft(gunHeading - (heading - 180));
 		setTurnLeft(getHeading() - (heading - 90));
 		System.out.println("Hit from " + heading + " and current gun heading "
 				+ gunHeading);
 	}
 
 	public void onHitWall(HitWallEvent event) {
 		double bearing = event.getBearing();
 		setTurnLeft(bearing);
 		// back(20);
 	}
 
 	public void onHitRobot(HitRobotEvent event) {
 		double gunBearing = getGunHeading();
 		double bearing = event.getBearing();
 		if (bearing - 180 < 0) {
 			setTurnGunLeft(gunBearing - (bearing + 180));
 		} else
 			setTurnGunLeft(gunBearing - (bearing - 180));
 		setTurnLeft(getHeading() - (bearing - 90));
 		System.out.println("Hit robot from " + bearing
 				+ " and current gun bearing " + gunBearing);
 	}
 
 	public void onWin(WinEvent event) {
 		setTurnLeft(720);
 		setTurnGunRight(720);
 		setTurnRadarLeft(3600);
 	}
 }
