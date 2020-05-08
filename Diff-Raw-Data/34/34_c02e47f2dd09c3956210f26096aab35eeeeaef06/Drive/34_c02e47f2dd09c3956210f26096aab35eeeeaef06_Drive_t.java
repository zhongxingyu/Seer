 package edu.first3729.frc2012;
 
 import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.Jaguar;
 
 public class Drive
 {
 	
 	private Jaguar fl;
 	private Jaguar fr;
 	private Jaguar bl;
 	private Jaguar br;
 	
 	private double _x_prev;
 	private double _y_prev;
 	private double _z_prev;
 	private double fl_out, fr_out, br_out, bl_out;
 	
 	private double ramp(double desired_output, double current_output, double increment)
 	{
 		if (desired_output  <= .1 && desired_output >= -.1)
                     increment /= 2;
 		if (desired_output < current_output)
                     return current_output - increment; 
 		else if (desired_output > current_output)
                     return increment + current_output;
 		else
                     return current_output;
 	}
 	
 	public Drive()
 	{
 		_y_prev = _x_prev = _z_prev = 0.0;
                 fl = new Jaguar(Params.fl_port);
 		fr = new Jaguar(Params.fr_port);
 		bl = new Jaguar(Params.bl_port);
 		br = new Jaguar(Params.br_port);
 		
 	}
 	
 	public void drive_tank(double left, double right)
 	{
             left = ramp(left, _x_prev, Params.x_ramp_increment);
             right = ramp(right, _y_prev, Params.y_ramp_increment);
             
             fl.set(left);
             bl.set(left);
             fr.set(-right);
             br.set(-right);
             
             _x_prev = left;
             _y_prev = right;
 	}
 	
 	public void drive_tank_noramp(double left, double right)
 	{
             fl.set(left);
             bl.set(left);
             fr.set(-right);
             br.set(-right);
 	}
 	
 	// Input from x and y axes on joystick, mapped to y = speed, x = turn
 	public void drive_arcade(double x, double y)
 	{
             x = ramp(x, _x_prev,Params.x_ramp_increment);
             y = ramp(y, _y_prev,Params.y_ramp_increment);        
                     
             // If not pushing forward much, switch to tank mode to turn in place
 		if ((y <= 0.1 && y > 0) || (y >= -0.1 && y < 0)) {
 			this.drive_tank(x * 0.75, -x * 0.75);
 		} 
                 else {
 			double left, right;
 			// If turning left:
 			if (x < 0) {
 				double mag = MathUtils.log(-y);
 				double ratio = (mag - 0.5) / (mag + 0.5);
 				if (ratio == 0) ratio = .0000000001;
 				left = y / ratio;
 				right = y;
 			}
 			// If turning right:
 			else if (x > 0) {
 				double mag = MathUtils.log(y);
 				double ratio = (mag - 0.5) / (mag + 0.5);
 				if (ratio == 0) ratio = .0000000001;
 				left = y;
 				right = y / ratio;
 			}
                         else {
 				left = y;
 				right = y;
 			}
 			// Keep everything within the confines of [-1.0, 1.0]
 			left = ( (left > 1.0) ? 1.0 : left);
 			left = ( (left < -1.0) ? 1.0 : left);
 			right = ( (right > 1.0) ? 1.0 : right);
 			right = ( (right < -1.0) ? 1.0 : right);
 			fl.set(-left);
 			fr.set(right);
 			bl.set(-left);
 			br.set(-right);
 		}
                 _x_prev = x;
 		_y_prev = y;
 
 	}
 	
 	// Mecanum drive!
 	public void drive_mecanum(double x, double y, double z)
 	{
 		x = ramp(x, _x_prev, Params.x_ramp_increment);
 		y = ramp(y, _y_prev, Params.y_ramp_increment);
 		z = ramp(z, _z_prev, Params.z_ramp_increment);
 
                 /* 
                  * Reasons behind this next bit of craziness:
                  * 
                  * X is the right joystick X-value, representing sideways
                  *   mecanum strafing motion.
                  * Y is the right joystick Y-value, representing forward motion.
                  * Z is the left joystick X-value, representing arcade-style
                  *   turning motion.
                  * 
                  * Let's say we're only pushing Y forward.  We want the robot to
                  *   just move forward.  To do this, we simply drive all 4
                  *   wheels forward.  So we get this:
                  *   
                  *   fl_out = y;
                  *   fr_out = y;
                  *   bl_out = y;
                  *   br_out = y;
                  * 
                  * Now, let's say we're only moving X to the right.  We want the
                  *   robot to strafe to the right.  To do this, we drive the
                  *   wheels in the corners at opposite speeds, like this:
                  * 
                  *   fl_out = -x;
                  *   fr_out = x;
                  *   bl_out = x;
                  *   br_out = -x;
                  * 
                  * Finally, we move Z to the right.  We want the robot to turn
                  *   in place.  To do this, we drive the right wheels backwards
                  *   and the left wheels forwards like so:
                  * 
                  *   fl_out = z;
                  *   fr_out = -z
                  *   bl_out = z;
                  *   br_out = -z;
                  * 
                  * Combining all of these statements, we get this:
                  * 
                  *   fl_out = y - x + z;
                  *   fr_out = y + x - z;
                  *   bl_out = y + x + z;
                  *   br_out = y - x - z;
                  * 
                  * However, in doing this we could have inputs as high as 3 or
                  *   as low as -2.  As such, we need to normalize the outputs to
                  *   be a fraction of the maximum output value.  That way, if we
                  *   push X, Y, and Z all full forward, we don't get this:
                  *   
                  *   fl_out = 1;
                  *   fr_out = 1;
                  *   bl_out = 3;
                  *   br_out = -1;
                  * 
                  * But instead, we get this:
                  * 
                  *   fl_out = 0.333333;
                  *   fr_out = 0.333333;
                  *   bl_out = 1;
                  *   br_out = -0.333333;
                  * 
                  * This has the same net motion vector, but all the output
                  *   values are within the output range of -1.0 to 1.0.
                  * 
                  * Incidentally, the result of pushing Y full forward, X full
                  *   right, and Z full right should be having the robot move
                  *   forward, strafe right, and turn right all at once.
                  * 
                  */
                 
                 fl_out = y - x + z;
                 fr_out = y + x - z;
                 bl_out = y + x + z;
                 br_out = y - x - z;
 
                 // Maximum absolute value of all output speeds - this next bit normalizes the outputs to no more than 1.0 and no less than -1.0
 		double max;
                 max = Math.max(Math.max(Math.abs(fl_out), Math.abs(fr_out)), Math.max(Math.abs(br_out), Math.abs(bl_out)));
 		if (max > 1.0) {
 			fl_out /= max;
 			fr_out /= max;
 			br_out /= max;
 			bl_out /= max;
 		}
                 else if (max < .11) {
                     fr_out = fl_out = br_out = bl_out = 0;
                 }
                 
                 if (Math.abs(y) <= .1 && Math.abs(x) <= .1) {
                     z *= .75;
                     fl_out = -z;
                     bl_out = -z;
                     br_out = z;
                     fr_out = z;
                 }
                 
                 System.out.println("fr_out: " + fr_out + " fl_out: " + fl_out + " br_out: " + br_out + " bl_out: " + br_out);
                 
 		fl.set(fl_out);
 		br.set(-br_out);
 		fr.set(-fr_out);
 		bl.set(bl_out);
                 
 		_x_prev = x;
 		_y_prev = y;
 		_z_prev = z;
 	}
 	
         public void lock_motors()
         {
             fl_out = fr_out = br_out = bl_out = 0.0;
             fl.set(fl_out);
             bl.set(bl_out);
             fr.set(fr_out);
             br.set(br_out);
         }
 }
