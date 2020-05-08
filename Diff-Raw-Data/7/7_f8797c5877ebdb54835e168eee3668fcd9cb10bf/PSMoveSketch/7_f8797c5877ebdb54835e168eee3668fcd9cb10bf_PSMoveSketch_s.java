 package de.t045t.hackery;
 
 import processing.core.*;
 import io.thp.psmove.*;
 import java.util.ArrayList;
 
 public class PSMoveSketch extends PApplet{
 
 	private ArrayList<PSMove> moves;
 	private ArrayList<MahoneyParameters> paramList;
 
 	public void setup() {
 		// Initialize MAGIC WANDS
 		moves = new ArrayList<PSMove>();
 		paramList = new ArrayList<MahoneyParameters>();
 		for (int i = 0; i < psmoveapi.psmove_count_connected(); i++) {
 			moves.add(new PSMove(i));
 			paramList.add(new MahoneyParameters());
 		}
 
 		// Set up Processing canvas
 		size(800, 600, P3D);
 		noStroke();
 		colorMode(RGB, 1);
 		fill(0.4f);
 	}
 
 	private float currentAngle;
 
 	public void draw() {
 		background(125,125,125);
 		stroke(255,0,0);
 		for (int i = 0; i < moves.size(); i++) {
 			PSMove move = moves.get(i);
 			if (move.poll() > 0) {
 				getAccelAngles(move.getAx(), move.getAy(), move.getAz());
 				MahonyAHRSupdateIMU(paramList.get(i), 
 						move.getGx(), move.getGy(), move.getGz(), 
 						move.getAx(), move.getAy(), move.getAz());//,
 					//	move.getMx(), move.getMy(), move.getMz());
 			}
 
 			float[] tmpQ = paramList.get(i).q;
 			double heading, attitude, bank;
 			{
 				double test = tmpQ[1]*tmpQ[2] + tmpQ[3]*tmpQ[0];
 				if (test > 0.499) { // singularity at north pole
 					heading = 2 * atan2(tmpQ[1],tmpQ[0]);
 					attitude = Math.PI/2;
 					bank = 0;
 					return;
 				}
 				if (test < -0.499) { // singularity at south pole
 					heading = -2 * atan2(tmpQ[1],tmpQ[0]);
 					attitude = - Math.PI/2;
 					bank = 0;
 					return;
 				}
 			    double sqx = tmpQ[1]*tmpQ[1];
 			    double sqy = tmpQ[2]*tmpQ[2];
 			    double sqz = tmpQ[3]*tmpQ[3];
 			    heading = atan2(2*tmpQ[2]*tmpQ[0]-2*tmpQ[1]*tmpQ[3] , (float) (1 - 2*sqy - 2*sqz));
 				attitude = asin((float) (2*test));
 				bank = atan2(2*tmpQ[1]*tmpQ[0]-2*tmpQ[2]*tmpQ[3] , (float) (1 - 2*sqx - 2*sqz));
 			}
 			rect(100, 300, 100, (int) (200 * tmpQ[0]));
 			rect(200, 300, 100, (int) (200 * tmpQ[1]));
 			rect(300, 300, 100, (int) (200 * tmpQ[2]));
 			rect(400, 300, 100, (int) (200 * tmpQ[3]));
 
 		}
 		updateRadialDial(mouseX, mouseY);
 		if (moves.size() > 0) {
 
 			int trigger = moves.get(0).get_trigger();
 			currentAngle = (float) (trigger / 255.0) * TWO_PI;
 		} else {
 			currentAngle = (float) (mouseX / 800.0) * TWO_PI;
 		}
		drawSegment(400, 300, 200, (int) (currentAngle / (PI / 36)), 150, 0.5f, 0.5f+currentAngle);
 	}
 
 	void updateRadialDial(int x, int y) {
 
 	}
 
 	private void drawSegment(int x, int y, int segHeight, int segments, int innerRadius, int startAngle, int endAngle) {
 		drawSegment(x, y, segHeight, segments, innerRadius, (float) (startAngle / 360.0) * TWO_PI, (float) (endAngle / 360.0) * TWO_PI);
 	}
 	private void drawSegment(int x, int y, int segHeight, int segments, int innerRadius, float startAngle, float endAngle) {
 		fill(0,0,0);
 		beginShape(QUAD_STRIP);
 		float tmp = (endAngle-startAngle) / segments;
 		for(int i = 0; i < segments+1; i++) {
			vertex(x+segHeight*cos((startAngle+tmp)*i), y+segHeight*sin((startAngle+tmp)*i));
			vertex(x+innerRadius*cos((startAngle+tmp)*i), y+innerRadius*sin((startAngle+tmp)*i));
 		}
 		endShape(CLOSE);
 	}
 
 	private void getAccelAngles(float ax, float ay, float az) {
 		float[] ret = new float[4];
 		float recipNorm;
 		
 		// Normalise gyroscope measurement
 		recipNorm = invSqrt(ax * ax + ay * ay + az * az);
 		ax *= recipNorm;
 		ay *= recipNorm;
 		az *= recipNorm;
 
 		rect(500, 300, 100, (int) (200 * ax));
 		rect(600, 300, 100, (int) (200 * ay));
 		rect(700, 300, 100, (int) (200 * az));
 	}
 	
 	private void Mahoney(MahoneyParameters params, float gx, float gy, float gz, float ax, float ay, float az, float mx, float my, float mz) {
 
 		float recipNorm;
 		float q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;  
 		float hx, hy, bx, bz;
 		float halfvx, halfvy, halfvz, halfwx, halfwy, halfwz;
 		float halfex, halfey, halfez;
 		float qa, qb, qc;
 
 		// Use IMU algorithm if magnetometer measurement invalid (avoids NaN in magnetometer normalisation)
 		if((mx == 0.0f) && (my == 0.0f) && (mz == 0.0f)) {
 			MahonyAHRSupdateIMU(params, gx, gy, gz, ax, ay, az);
 			return;
 		}
 
 		// Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
 		if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {
 
 			// Normalise accelerometer measurement
 			recipNorm = invSqrt(ax * ax + ay * ay + az * az);
 			ax *= recipNorm;
 			ay *= recipNorm;
 			az *= recipNorm;     
 
 			// Normalise magnetometer measurement
 			recipNorm = invSqrt(mx * mx + my * my + mz * mz);
 			mx *= recipNorm;
 			my *= recipNorm;
 			mz *= recipNorm;
 
 			/*
 			// Normalise gyroscope measurement
 			recipNorm = invSqrt(gx * gx + gy * gy + gz * gz);
 			gx *= recipNorm;
 			gy *= recipNorm;
 			gz *= recipNorm;
 			*/
 
 			System.out.println(gx + "\t" + gy + "\t" + gz + "\t" + ax + "\t" + ay + "\t" + az + "\t" + mx + "\t" + my + "\t" + mz + "\t");
 
 
 			// Auxiliary variables to avoid repeated arithmetic
 			q0q0 = params.q[0] * params.q[0];
 			q0q1 = params.q[0] * params.q[1];
 			q0q2 = params.q[0] * params.q[2];
 			q0q3 = params.q[0] * params.q[3];
 			q1q1 = params.q[1] * params.q[1];
 			q1q2 = params.q[1] * params.q[2];
 			q1q3 = params.q[1] * params.q[3];
 			q2q2 = params.q[2] * params.q[2];
 			q2q3 = params.q[2] * params.q[3];
 			q3q3 = params.q[3] * params.q[3];   
 
 			// Reference direction of Earth's magnetic field
 			hx = 2.0f * (mx * (0.5f - q2q2 - q3q3) + my * (q1q2 - q0q3) + mz * (q1q3 + q0q2));
 			hy = 2.0f * (mx * (q1q2 + q0q3) + my * (0.5f - q1q1 - q3q3) + mz * (q2q3 - q0q1));
 			bx = sqrt(hx * hx + hy * hy);
 			bz = 2.0f * (mx * (q1q3 - q0q2) + my * (q2q3 + q0q1) + mz * (0.5f - q1q1 - q2q2));
 
 			// Estimated direction of gravity and magnetic field
 			halfvx = q1q3 - q0q2;
 			halfvy = q0q1 + q2q3;
 			halfvz = q0q0 - 0.5f + q3q3;
 			halfwx = bx * (0.5f - q2q2 - q3q3) + bz * (q1q3 - q0q2);
 			halfwy = bx * (q1q2 - q0q3) + bz * (q0q1 + q2q3);
 			halfwz = bx * (q0q2 + q1q3) + bz * (0.5f - q1q1 - q2q2);  
 
 			// Error is sum of cross product between estimated direction and measured direction of field vectors
 			halfex = (ay * halfvz - az * halfvy) + (my * halfwz - mz * halfwy);
 			halfey = (az * halfvx - ax * halfvz) + (mz * halfwx - mx * halfwz);
 			halfez = (ax * halfvy - ay * halfvx) + (mx * halfwy - my * halfwx);
 
 			// Compute and apply integral feedback if enabled
 			if(params.twoKi > 0.0f) {
 				params.integralFBx += params.twoKi * halfex * (1.0f / params.sampleFreq);	// integral error scaled by Ki
 				params.integralFBy += params.twoKi * halfey * (1.0f / params.sampleFreq);
 				params.integralFBz += params.twoKi * halfez * (1.0f / params.sampleFreq);
 				gx += params.integralFBx;	// apply integral feedback
 				gy += params.integralFBy;
 				gz += params.integralFBz;
 			}
 			else {
 				params.integralFBx = 0.0f;	// prevent integral windup
 				params.integralFBy = 0.0f;
 				params.integralFBz = 0.0f;
 			}
 
 			// Apply proportional feedback
 			gx += params.twoKp * halfex;
 			gy += params.twoKp * halfey;
 			gz += params.twoKp * halfez;
 		}
 
 		// Integrate rate of change of quaternion
 		gx *= (0.5f * (1.0f / params.sampleFreq));		// pre-multiply common factors
 		gy *= (0.5f * (1.0f / params.sampleFreq));
 		gz *= (0.5f * (1.0f / params.sampleFreq));
 		qa = params.q[0];
 		qb = params.q[1];
 		qc = params.q[2];
 		params.q[0] += (-qb * gx - qc * gy - params.q[3] * gz);
 		params.q[1] += (qa * gx + qc * gz - params.q[3] * gy);
 		params.q[2] += (qa * gy - qb * gz + params.q[3] * gx);
 		params.q[3] += (qa * gz + qb * gy - qc * gx); 
 
 		// Normalise quaternion
 		recipNorm = invSqrt(params.q[0] * params.q[0] + params.q[1] * params.q[1] + params.q[2] * params.q[2] + params.q[3] * params.q[3]);
 		params.q[0] *= recipNorm;
 		params.q[1] *= recipNorm;
 		params.q[2] *= recipNorm;
 		params.q[3] *= recipNorm;
 	}
 
 	private void MahonyAHRSupdateIMU(MahoneyParameters params, float gx, float gy, float gz, float ax, float ay, float az) {
 		float recipNorm;
 		float halfvx, halfvy, halfvz;
 		float halfex, halfey, halfez;
 		float qa, qb, qc;
 
 		// Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
 		if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {
 
 			// Normalise accelerometer measurement
 			recipNorm = invSqrt(ax * ax + ay * ay + az * az);
 			ax *= recipNorm;
 			ay *= recipNorm;
 			az *= recipNorm;        
 
 			/*
 			// Normalise gyroscope measurement
 			recipNorm = invSqrt(gx * gx + gy * gy + gz * gz);
 			gx *= recipNorm;
 			gy *= recipNorm;
 			gz *= recipNorm;
 			*/
 
 			// Estimated direction of gravity and vector perpendicular to magnetic flux
 			halfvx = params.q[1] * params.q[3] - params.q[0] * params.q[2];
 			halfvy = params.q[0] * params.q[1] + params.q[2] * params.q[3];
 			halfvz = params.q[0] * params.q[0] - 0.5f + params.q[3] * params.q[3];
 
 			// Error is sum of cross product between estimated and measured direction of gravity
 			halfex = (ay * halfvz - az * halfvy);
 			halfey = (az * halfvx - ax * halfvz);
 			halfez = (ax * halfvy - ay * halfvx);
 
 			// Compute and apply integral feedback if enabled
 			if(params.twoKi > 0.0f) {
 				params.integralFBx += params.twoKi * halfex * (1.0f / params.sampleFreq);	// integral error scaled by Ki
 				params.integralFBy += params.twoKi * halfey * (1.0f / params.sampleFreq);
 				params.integralFBz += params.twoKi * halfez * (1.0f / params.sampleFreq);
 				gx += params.integralFBx;	// apply integral feedback
 				gy += params.integralFBy;
 				gz += params.integralFBz;
 			}
 			else {
 				params.integralFBx = 0.0f;	// prevent integral windup
 				params.integralFBy = 0.0f;
 				params.integralFBz = 0.0f;
 			}
 
 			// Apply proportional feedback
 			gx += params.twoKp * halfex;
 			gy += params.twoKp * halfey;
 			gz += params.twoKp * halfez;
 		}
 
 		// Integrate rate of change of quaternion
 		gx *= (0.5f * (1.0f / params.sampleFreq));		// pre-multiply common factors
 		gy *= (0.5f * (1.0f / params.sampleFreq));
 		gz *= (0.5f * (1.0f / params.sampleFreq));
 		qa = params.q[0];
 		qb = params.q[1];
 		qc = params.q[2];
 		params.q[0] += (-qb * gx - qc * gy - params.q[3] * gz);
 		params.q[1] += (qa * gx + qc * gz - params.q[3] * gy);
 		params.q[2] += (qa * gy - qb * gz + params.q[3] * gx);
 		params.q[3] += (qa * gz + qb * gy - qc * gx); 
 
 		// Normalise quaternion
 		recipNorm = invSqrt(params.q[0] * params.q[0] + params.q[1] * params.q[1] + params.q[2] * params.q[2] + params.q[3] * params.q[3]);
 		params.q[0] *= recipNorm;
 		params.q[1] *= recipNorm;
 		params.q[2] *= recipNorm;
 		params.q[3] *= recipNorm;
 	}
 
 	private float invSqrt(float x) {
 		return (float) (1.0 / Math.sqrt((double) x));
 	}
 }
