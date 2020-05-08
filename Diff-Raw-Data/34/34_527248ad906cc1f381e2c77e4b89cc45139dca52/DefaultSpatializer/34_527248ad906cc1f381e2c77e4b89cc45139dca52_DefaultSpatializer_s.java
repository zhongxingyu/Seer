 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package com.sun.mpk20.voicelib.app;
 
 import java.util.logging.Logger;
 
 public class DefaultSpatializer implements Spatializer {
 
     private boolean debug;
 
     /** a logger */
     protected static final Logger logger =
             Logger.getLogger(DefaultSpatializer.class.getName());
 
     private FallOffFunction fallOffFunction;
 
     private static final String MAXIMUM_VOLUME =
         "com.sun.server.impl.app.VoiceManager.MAXIMUM_VOLUME";
 
     private static final double DEFAULT_MAXIMUM_VOLUME = 1;
 
     private static final String FALLOFF =
         "com.sun.server.impl.app.VoiceManager.FALLOFF";
 
     private static final double DEFAULT_FALLOFF = .92;
 
     private static final double DEFAULT_FULL_VOLUME_RADIUS = 0;
 
     private static final double DEFAULT_ZERO_VOLUME_RADIUS = .26;
 
     private static final double PiOver2 = Math.PI / 2;
 
     private static double[] sinTable = new double[360];
 
     static {
 	for (int i = 0; i < 360; i++) {
 	    sinTable[i] = Math.sin(Math.toRadians(i));
 	}
     }
 
     public DefaultSpatializer() {
 	fallOffFunction = new InverseFallOff();
 
 	String s = System.getProperty(MAXIMUM_VOLUME);
 
 	double maximumVolume = DEFAULT_MAXIMUM_VOLUME;
 
 	if (s != null) {
 	    try {
 	        maximumVolume = Double.parseDouble(s);
 	    } catch (NumberFormatException e) {
 	        logger.warning("Invalid maximum volume:  " + s);
 	    }
 	}
 
 	logger.fine("Maximum volume " + maximumVolume);
 
 	s = System.getProperty(FALLOFF);
 
 	double fallOff = DEFAULT_FALLOFF;
 
 	if (s != null) {
 	    try {
 	        fallOff = Double.parseDouble(s);
 	    } catch (NumberFormatException e) {
 	        logger.warning("Invalid maximum volume:  " + s);
 	    }
 	}
 	
 	/*
 	 * Initialize fallOffFunction
 	 */
 	setFallOff(fallOff);
 	setMaximumVolume(maximumVolume);
         setFullVolumeRadius(DEFAULT_FULL_VOLUME_RADIUS);
 	setZeroVolumeRadius(DEFAULT_ZERO_VOLUME_RADIUS);
     }
 
     public void setFallOffFunction(FallOffFunction fallOffFunction) {
 	this.fallOffFunction = fallOffFunction;
     }
 
     public FallOffFunction getFallOffFunction() {
 	return fallOffFunction;
     }
 
     public void setFallOff(double fallOff) {
         fallOffFunction.setFallOff(fallOff);
 
         logger.fine("Set fallOff to " + round(fallOff));
 
         fallOffFunction.setFallOff(fallOff);
     }
 
     public double getFallOff() {
 	return fallOffFunction.getFallOff();
     }
 
     public void setFullVolumeRadius(double fullVolumeRadius) {
 	logger.fine("Set full volume radius to " 
 	    + round(fullVolumeRadius));
 	fallOffFunction.setFullVolumeRadius(fullVolumeRadius);
     }
 
     public double getFullVolumeRadius() {
 	return fallOffFunction.getFullVolumeRadius();
     }
 
     public void setZeroVolumeRadius(double zeroVolumeRadius) {
 	logger.fine("Set zero volume radius to " 
 	    + round(zeroVolumeRadius));
 	fallOffFunction.setZeroVolumeRadius(zeroVolumeRadius);
     }
 
     public double getZeroVolumeRadius() {
 	return fallOffFunction.getZeroVolumeRadius();
     }
 
     public void setMaximumVolume(double maximumVolume) {
 	logger.fine("Set maximum volume to " + round(maximumVolume));
 	fallOffFunction.setMaximumVolume(maximumVolume);
     }
 
     public double getMaximumVolume() {
 	return fallOffFunction.getMaximumVolume();
     }
 
     class Position {
 	public double x;
 	public double y;
 	public double z;
 	public double orientation;
 
 	public Position(double x, double y, double z, double orientation) {
 	    this.x = x;
 	    this.y = y;
 	    this.z = z;
 	    this.orientation = orientation;
 	}
 
 	public String toString() {
 	    return "(" + round(x) + ", " + round(y) + ", " 
 		+ round(z) + ")" + ":" + toDegrees(orientation);
 	}
 
     }
 
     public double[] spatialize(double sourceX, double sourceY, 
 	    double sourceZ, double sourceOrientation,
 	    double destX, double destY, double destZ, 
 	    double destOrientation) {
 
 	Position p2 = new Position(sourceX, sourceY, sourceZ,
 	    sourceOrientation);
 
 	Position p1 = new Position(destX, destY, destZ, destOrientation);
 
 	logger.finest("spatialize:  " + p1 + " to " + p2);
 
 	/*
 	 * Here are the assumptions about what happens to the sound volume
 	 * when someone speaks:
 	 *
  	 * - the volume is decreases with distance.
 	 * - The angle of the listener determines how much volume goes to
 	 *   each ear of the listener.  If the angle of the listener is 0
 	 *   relative to the speaker, the volume goes to both ears equally.
 	 * 
 	 * The problem can be simplified by noticing that if the distance
 	 * between p1 and p2 is non-zero, then there is always
 	 * a straight line between p1 and p2 in 3-space.
 	 * This straight line can be rotated so that it is parallel 
 	 * to the x-axis by subtracting out the angle between the two points.
 	 * In doing so, the angles of each point must also have the angle
 	 * between the points subtracted out.
 	 * 
 	 * Now it must be determined which point is in front of the other.
 	 * If the angle at which sounds hits p1 is between 90 and 270 degrees,
 	 * p2 is behind p1.  Otherwise p2 is in front of p1.
 	 */
 
 	/*
 	 * This is the distance between the two calls in 3-space.
 	 * It is a value between 0 and 1 which represents a fraction
 	 * of the window size.
 	 *
 	 * It is used to attenuate the volume.
 	 */
 	double distance = getDistance(p1, p2);
 
 	double volume = fallOffFunction.distanceToVolumeLevel(distance);
 
 	logger.finest("distance " + round(distance) + " volume " 
 	    + round(volume));
 
         /*
          * p1ReceiveAngle is the angle at call 1 at which the sound from call 2 
 	 * hits call 1.
          */
 	debug = false;
 
 	if (volume > .1) {
 	    //debug = true;
 	}
 
 	double p1ReceiveAngle = getAngle(p1, p2);
 
 	double p1ReceiveDegrees = toDegrees(p1ReceiveAngle);
 
 	if (debug) {
 	    logger.info("receive angle " + p1ReceiveDegrees);
 	}
 
 	/*
 	 * parameter 0 is front/back with 1 being front and -1 back
 	 * parameter 1 is left/right with -1 being left and 1 right
 	 * parameter 2 is up/down with -1 being down and 1 up
 	 * parameter 3 is the volume 0 to maxVolume (usually 1)
 	 */
 	double[] privateMixParameters = new double[4];
 
 	privateMixParameters[2] = 0;	// up/down
 
 	if (p1ReceiveDegrees <= 90) {
             privateMixParameters[0] = 1 - (p1ReceiveDegrees / 90);
 	    privateMixParameters[1] = -p1ReceiveDegrees / 90;
 	} else if (p1ReceiveDegrees <= 180) {
 	    double d = p1ReceiveDegrees - 90;
             privateMixParameters[0] = -d / 90;
 	    privateMixParameters[1] = -(1 - (d / 90));
 
 	    logger.finest("d " + d + " p0 " + round(privateMixParameters[0]));
 	} else if (p1ReceiveDegrees <= 270) {
 	    double d = p1ReceiveDegrees - 180;
             privateMixParameters[0] = -(1 - (d / 90));
 	    privateMixParameters[1] = d / 90;
 	} else {
 	    double d = p1ReceiveDegrees - 270;
             privateMixParameters[0] = d / 90;
 	    privateMixParameters[1] = 1 - (d / 90);
 	}
 
 	// Try this for smoother transitions.
         privateMixParameters[0] = Math.cos(p1ReceiveAngle);
 	privateMixParameters[1] = -Math.sin(p1ReceiveAngle);
 	//privateMixParameters[1] = -sinTable[(int) p1ReceiveDegrees];
 
         privateMixParameters[3] = volume;    // volume
 
 	privateMixParameters[0] = round(privateMixParameters[0]);
 	privateMixParameters[1] = round(privateMixParameters[1]);
 	privateMixParameters[2] = round(privateMixParameters[2]);
 	privateMixParameters[3] = round(privateMixParameters[3]);
 
 	logger.finest("p1Rec " + p1ReceiveDegrees
 	    + " privateMixParameters " + privateMixParameters[0] + ", " 
 	    + privateMixParameters[1] + ", " + privateMixParameters[2] + ", "
 	    +  privateMixParameters[3]);
 
 	return privateMixParameters;
     }
 
     public static double getDistance(double sourceX, double sourceY,
                                      double sourceZ, double destX, 
 				     double destY, double destZ) {
 
 	double xd = sourceY - sourceX;
         double yd = destY - destX;
 
         double d = Math.sqrt(xd * xd + yd * yd);
 
         double zd = destZ - sourceZ;
 
         double distance = d;
 
         if (zd != 0) {
             distance = Math.sqrt(d * d + zd * zd);
         }
 
         return distance;
     }
 
     public static double getDistance(Position p1, Position p2) {
 	double xd = p2.x - p1.x;
 	double yd = p2.y - p1.y;
 
 	double d = Math.sqrt(xd * xd + yd * yd);
 
 	double zd = p2.z - p1.z;
 
 	double distance = d;
 
 	if (zd != 0) {
 	    distance = Math.sqrt(d * d + zd * zd);
 	}
 
 	logger.finest(
 	    "P1 (" + round(p1.x) + "," + round(p1.y) + ")"
 	    + " P2 (" + round(p2.x) + "," + round(p2.y) + ")"
 	    + " distance " + round(distance));
 
 	return distance;
     }
 
     /*
      * Get angle at which sound from p2 hits p1
      */
     private double getAngle(Position p1, Position p2) {
 	if (p1.x == p2.x && p1.y == p2.y) {
 	    /*
 	     * p1 and p2 are at the same place
 	     * Their rotations don't matter.
 	     * Treat them like they are facing each other.
 	     */
 	    return 0;
 	}
 
 	double p1ReceiveAngle;
 
 	if (p1.x == p2.x) {
 	    /*
 	     * p1 and p2 are along the same vertical line.
 	     */
             if (p1.y < p2.y) {
 		/*
 		 * p1 is below p2, the angle is correct as is.
 		 */
                 p1ReceiveAngle = PiOver2;         // 90 degrees
             } else {
 		/*
 		 * p1 is above p2, p1 is receiving audio 
 		 * on the opposite size.
 		 */
                 p1ReceiveAngle = -PiOver2;        // -90 degrees
 	    }
 	} else {
 	    p1ReceiveAngle = Math.atan((p2.y - p1.y) / (p2.x - p1.x));
 	}
 
 	if (p1.x > p2.x) {
 	    logger.finest("p1RecAngle needs PI added " + toDegrees(p1ReceiveAngle));
 	    p1ReceiveAngle += Math.PI;
 	} 
 
 	if (debug) {
           logger.info("p1=" + p1 + " p2=" + p2
             + " p1RecAng " + toDegrees(p1ReceiveAngle) + " p1 orient " 
 	    + toDegrees(p1.orientation)
 	    + " p1RecAng adj " + toDegrees(p1ReceiveAngle -  p1.orientation));
 	}
 
 	p1ReceiveAngle -= p1.orientation;
 
 	return p1ReceiveAngle;
     }
 
     private int toDegrees(double radians) {
         int degrees = ((int) Math.toDegrees(radians)) % 360;
 
         if (degrees < 0) {
 	    logger.finest("degrees < 0, adding 360 " + degrees);
             degrees += 360;
         }
 
         return degrees;
     }
 
     public static double round(double v) {
         return Math.round(v * 100) / (double) 100;
     }
 
     public static void main(String[] args) {
 	DefaultSpatializer spatializer = new DefaultSpatializer();
 
 	int n = 40000;
 
 	doit(spatializer, n);
 
 	long start = System.currentTimeMillis();
 
 	doit(spatializer, n);
 
 	long elapsed = System.currentTimeMillis() - start;
 
 	System.out.println("elapsed " + elapsed + " avg " 
 	    + ((double) elapsed / n));
     }
  
     private static void doit(DefaultSpatializer spatializer, int n) {
 	for (int i = 0; i < n; i++) {
 	    double sourceX = i;
 	    double sourceY = i + 1;
 	    double sourceOrientation = i;
 
 	    double destX = i + 2;
 	    double destY = i + 3;
 	    double destOrientation = i + 1;
 
     	    spatializer.spatialize(sourceX, sourceY, 
 	        0, sourceOrientation, destX, destY, 
 		0, destOrientation);
 	}
     }
 
 }
