 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author geza
  */
 
 import orc.*;
 
 public class Explorer extends java.lang.Thread {
 	public boolean running = true;
 	public float[] leftMotorAction = null;
 	public float[] leftMotorWeight = null;
 	public float[] rightMotorAction = null;
 	public float[] rightMotorWeight = null;
 	public int idx = 0;
 	public int state = 0;
 	public String[] names = {"forward", "left", "right", "rotateleft", "rotateright"};
 	public float[] weights = {0.5f, 0.97f, 0.97f, 0.97f, 0.97f};
 	public Orc o = null;
 	public Gyroscope g = null;
 	public int angrefi = 0;
 	public int maxheu = 0;
 	public int targang = 0;
 	public int prevtargdiff = Integer.MAX_VALUE;
 
 	public void setState(int newstate) {
 		System.err.println("transition to "+names[newstate]);
 		state = newstate;
 		leftMotorWeight[idx] = weights[newstate];
 		rightMotorWeight[idx] = weights[newstate];
 		prevtargdiff = Integer.MAX_VALUE;
 		angrefi = g.anglei;
		if (newstate != 3 && newstate != 4) {
 		maxheu = 0;
 		targang = angrefi;
 		}
 	}
 
 	public static double bound(double v, double max, double min) {
 		if (v > max) return max;
 		else if (v < min) return min;
 		else return v;
 	}
 
 	public static void shiftleft(double[] a, double v) {
 		int i = 0;
 		for (; i < a.length-1; ++i) {
 			a[i] = a[i+1];
 		}
 		a[i] = v;
 	}
 
 	public static double averageArray(double[] a) {
 		double total = 0.0;
 		for (int i = 0; i < a.length; ++i) {
 			total += a[i];
 		}
 		return total / (double)a.length;
 	}
 
 	public static double median3(double a, double b, double c) {
 		if (a < b) {
 			if (c < b) { // a,c < b
 				if (a < c) { // a < c < b
 					return c;
 				} else { // c < a < b
 					return a;
 				}
 			} else { // a < b < c
 				return b;
 			}
 		} else { // b < a
 			if ( c < a) { // b,c < a
 				if (b < c) { // b < c < a
 					return c;
 				} else { // c < b < a
 					return b;
 				}
 			} else { // b < a < c
 				return a;
 			}
 		}
 	}
 
 	public int circdiff(int ang1, int ang2) {
 		if (ang1 > ang2) {
 			return Math.min(Math.abs(ang2+360-ang1), Math.abs(ang1-ang2));
 		} else {
 			return Math.min(Math.abs(ang1+360-ang2), Math.abs(ang1-ang2));
 		}
 	}
 
 	public void run() {
 		try {
 		//byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		//Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		AnalogInput leftIR = new AnalogInput(o, 7);
 		AnalogInput rightIR = new AnalogInput(o, 0);
 		AnalogInput crossLeftIR = new AnalogInput(o, 5);
 		AnalogInput crossRightIR = new AnalogInput(o, 2);
 		final double desv = 120.0;
 		final double desvCross = 30.0;
 		final double kp = 0.002;
 		final double kd = 0.001;
 		double prevleft = leftIR.getVoltage();
 		double prevright = rightIR.getVoltage();
 		double prevCrossLeft = crossLeftIR.getVoltage();
 		double prevCrossRight = crossRightIR.getVoltage();
 		double[] leftIRreadings = new double[3];
 		double[] rightIRreadings = new double[3];
 		double[] crossLeftIRreadings = new double[3];
 		double[] crossRightIRreadings = new double[3];
 		java.util.Arrays.fill(leftIRreadings, prevleft);
 		java.util.Arrays.fill(rightIRreadings, prevright);
 		java.util.Arrays.fill(crossRightIRreadings, prevCrossLeft);
 		java.util.Arrays.fill(crossLeftIRreadings, prevCrossRight);
 		int rightcooldown = 0;
 		int leftcooldown = 0;
		//setState(3);
 		while (running) {
 			shiftleft(leftIRreadings, 62.5/leftIR.getVoltage());
 			shiftleft(rightIRreadings, 62.5/rightIR.getVoltage());
 			shiftleft(crossLeftIRreadings, 62.5/crossLeftIR.getVoltage());
 			shiftleft(crossRightIRreadings, 62.5/crossRightIR.getVoltage());
 			double left = median3(leftIRreadings[0], leftIRreadings[1], leftIRreadings[2]);//a.getVoltage()
 			//double left = 999999.0;
 			double right = median3(rightIRreadings[0], rightIRreadings[1], rightIRreadings[2]);
 			//double right = averageArray(rightIRreadings);
 
 			double crossLeft = median3(crossLeftIRreadings[0], crossLeftIRreadings[1], crossLeftIRreadings[2]);
 			double crossRight = median3(crossRightIRreadings[0], crossRightIRreadings[1], crossRightIRreadings[2]);
 
 			System.out.println("left is "+left+"right is "+right);
 			//System.out.println(right);
 			double lspeed = 0.0;
 			double rspeed = 0.0;
 			double lspeedCross = 0.0;
 			double rspeedCross = 0.0;
 			boolean sideVote = false;
 			boolean crossVote = false;
 			boolean escape = false;
 			if (leftcooldown > 0) --leftcooldown;
 			if (rightcooldown > 0) --rightcooldown;
 			if (state == 0) { // forwards
 				if (/*crossLeft < 30 ||*/ right < 100) { // rotate left
 					setState(1);
 				} else if (crossRight < 100 || left < 100) { // rotate right
 					setState(2);
 				} else {
 			if (left > right) {
 				//leftMotorWeight[idx] = 0.8f;
 				//rightMotorWeight[idx] = 0.8f;
 				double error = left-desv;
 				if (error > 100.0) error = 100.0;
 				if (error < -100.0) error = -100.0;
 				double basevel = 0.7;
 				//double basevel = bound(1.0-error, 0.7, 0.6);
 				lspeed = -(kp*error-kd*(left-prevleft));//+basevel;
 				rspeed = (kp*error-kd*(left-prevleft));//+basevel;
 				if (lspeed > rspeed) {
 					rspeed += basevel-Math.abs(lspeed);
 					lspeed = basevel;
 				} else {
 					lspeed += basevel-Math.abs(rspeed);
 					rspeed = basevel;
 				}
 				sideVote = true;
 			} else {
 				//leftMotorWeight[idx] = 0.8f;
 				//rightMotorWeight[idx] = 0.8f;
 				double error = right-desv;
 				if (error > 100.0) error = 100.0;
 				if (error < -100.0) error = -100.0;
 				//double basevel = bound(1.0-error, 0.7, 0.6);
 				double basevel = 0.7;
 				lspeed = (kp*error-kd*(right-prevright));//+basevel;
 				rspeed = -(kp*error-kd*(right-prevright));//+basevel;
 				if (lspeed > rspeed) {
 					rspeed += basevel-Math.abs(lspeed);
 					lspeed = basevel;
 				} else {
 					lspeed += basevel-Math.abs(rspeed);
 					rspeed = basevel;
 				}
 			}
 			}
			lspeed = 0.0;
			rspeed = 0.0;
 			} if (state ==  1) { // rotate left
 				rspeed = 0.7;
 				lspeed = -0.7;
 
 				if (((crossRight > 120 && crossLeft > 120) || (crossRight-prevCrossRight) > 0 || (crossLeft-prevCrossLeft) > 0) && ((right > 120 && left > 120) || (left-prevleft) > 0 || (right-prevright) > 0)) {
 					setState(0);
 				}
 			} if (state == 2) { // rotate right
 				rspeed = -0.9;
 				lspeed = 0.3;
 				if (((crossRight > 120 && crossLeft > 120) || (crossRight-prevCrossRight) > 0 || (crossLeft-prevCrossLeft) > 0) && ((right > 120 && left > 120) || (left-prevleft) > 0 || (right-prevright) > 0)) {
 				//if (/*crossLeft > 40 &&*/ crossRight > 120 && left > 50 && right > 50) {
 					setState(0);
 				}
 			} if (state == 3) { // rotate to target angle by the left
 				int targdiff = circdiff(g.anglei,targang);
				if (targdiff < 70 && targdiff > prevtargdiff) { // done rotating
 					rspeed = -0.7;
 					lspeed = 0.7;
 					setState(0);
 				} else {
 					rspeed = 0.7;
 					lspeed = -0.7;
 					prevtargdiff = targdiff;
 				}
 			} if (state == 4) { // rotate to target angle by the right
 				int targdiff = circdiff(g.anglei,targang);
				if (targdiff < 70 && targdiff > prevtargdiff) { // done rotating
 					rspeed = 0.7;
 					lspeed = -0.7;
 					setState(0);
 				} else {
 					rspeed = -0.7;
 					lspeed = 0.7;
 					prevtargdiff = targdiff;
 				}
 			}
 			prevleft = left;
 			prevright = right;
 			prevCrossLeft = crossLeft;
 			prevCrossRight = crossRight;
 			leftMotorAction[idx] = (float)lspeed;
 			rightMotorAction[idx] = (float)rspeed;
 			java.lang.Thread.sleep(20);
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setup(Arbiter a, Gyroscope ng, int ActionWeightIndex) {
 		idx = ActionWeightIndex;
 		leftMotorAction = a.leftMotorAction;
 		leftMotorWeight = a.leftMotorWeight;
 		rightMotorAction = a.rightMotorAction;
 		rightMotorWeight = a.rightMotorWeight;
 		o = a.o;
 		g = ng;
 	}
 
 	public void bye() {
 		running = false;
 	}
 }
