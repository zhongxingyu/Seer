 package edu.bismarckstate.shortestpathhelper.movement;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import lejos.nxt.Button;
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.robotics.navigation.DifferentialPilot;
 import edu.bismarckstate.shortestpathhelper.util.MovementInstruction;
 
 public class RobotMovement {
 
	private double facing;
 	private DifferentialPilot diffPilot;
 	private boolean diffPilotSetup = false;
 	
 	/**
 	 * Default Constructor uses the default file name of robotsetup.rbt
 	 */
 	public RobotMovement(){
 		setDifferentialPilotFromFile("robotsetup.rbt");
 	}
 	
 	/**
 	 * Used for custom robots.
 	 * @param wheel The name of the file holding the wheel diameter. Just put the wheel diameter in the file, nothing else.
 	 * @param trackW The name of the file holding the track Width. Just put the track width in the file, nothing else.
 	 */
 	public RobotMovement(String fileName){
 		setDifferentialPilotFromFile(fileName);
 	}
 	
 	/**
 	 * sets the base diff pilot up and also sets the boolean value to tell if the pilot correctly set up or not
 	 * @param fileName the string filename of the pilot setup file
 	 */
 	private void setDifferentialPilotFromFile(String fileName)
 	{
 		double trackwidth = 0;
 		double wheelwidth = 0;
 		NXTRegulatedMotor motor1 = Motor.B;
 		NXTRegulatedMotor motor2 = Motor.A;
 		boolean reversed = false;
 		boolean fileReadok = true;
 		File data = new File(fileName);
 		if (data.canRead())
 		{
 			try{
 				InputStream is = new FileInputStream(data);
 				DataInputStream din = new DataInputStream(is);
 				BufferedReader br = new BufferedReader(new InputStreamReader(din));
 				motor1 = getMotorFromString(br.readLine());
 				motor2 = getMotorFromString(br.readLine());
 				trackwidth = Double.parseDouble(br.readLine().trim());
 				wheelwidth = Double.parseDouble(br.readLine().trim());
 				reversed = getMotorReversed(br.readLine().trim());
 				din.close();
 			}catch(IOException ioe){
 				fileReadok = false;
 				System.out.println("error:" + ioe.getMessage());
 				Button.ENTER.waitForPressAndRelease();
 			}catch(Exception ex){
 				fileReadok = false;
 				System.out.println("error:" + ex.getMessage());
 				Button.ENTER.waitForPressAndRelease();
 			}
 		}else{
 			fileReadok = false;
 		}
 		
 		if (fileReadok){
 			this.diffPilotSetup = true;
 			this.diffPilot = new DifferentialPilot(wheelwidth, trackwidth, motor1, motor2,reversed);
 		} else {
 			this.diffPilotSetup = false;
 		}
 	}
 	
 	private NXTRegulatedMotor getMotorFromString(String motorLetter) throws Exception{
 		if (motorLetter.toLowerCase().equals("a")){
 			return Motor.A;
 		} else if (motorLetter.toLowerCase().equals("b")){
 			return Motor.B;
 		} else if (motorLetter.toLowerCase().equals("c")){
 			return Motor.C;
 		}else{
 			throw new Exception("Error finding a motor");
 		}
 	}
 	
 	private boolean getMotorReversed(String reversed){
 		if (reversed.equals("1"))
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean isDiffPilotOk()
 	{
 		return diffPilot != null && diffPilotSetup;
 	}
 	
 	/**
 	 * Moves the robot according to MovementInstruction
 	 * @param move The instruction you want to send. Should have a direction and distance.
 	 * 				Will face the direction (north south east or west, or 0 to 360) and move the distance you send.
 	 */
 	public void move(MovementInstruction move){
 		
 		double dist = move.getDistance();
 		double rot = move.getNormalizedTurn(facing);
 		this.diffPilot.rotate(rot);
 		this.diffPilot.travel(dist);
		this.facing = (this.facing + rot) % 360;
		if (this.facing < 0){
			this.facing += 360;
		}
 	}
 
 	/**
 	 * @return The direction (in degrees) the robot is facing. 0 = North, 90 = East, 180 = South, 270 = West
 	 * 		   When the robot is started the direction it's facing is considered north until reset.
 	 */
	public double getFacing() {
 		return facing;
 	}
 
 	/**
 	 * @param facing: The direction you want your robot to 'think' it's facing. 0 = North, 90 = East, 180 = South, 270 = West
 	 *                When the robot is started the direction it's facing is considered north until reset.
 	 *                DALE AND TUCKER WARN: It's best to probably not change it's facing to avoid confusion, but hey you're in charge.
 	 */
	public void setFacing(double facing) {
 		this.facing = facing;
 	}
 	
 	public String toString(){
 		String output = "Facing: ";
 		
		switch((int)facing){
 		case 0: output.concat("NORTH"); break;
 		case 180: output.concat("SOUTH"); break;
 		case 90: output.concat("EAST"); break;
 		case 270: output.concat("WEST"); break;
 		default: output.concat(facing + " degrees"); break;
 		}
 		
 		return output;
 	}
 }
