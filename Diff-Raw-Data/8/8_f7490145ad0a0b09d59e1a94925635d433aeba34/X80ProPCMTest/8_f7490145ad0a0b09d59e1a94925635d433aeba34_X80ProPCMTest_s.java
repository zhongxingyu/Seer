 package edu.ius.robotics;
 
 import java.io.IOException;
 
 import edu.ius.robotics.robots.X80Pro;
 import edu.ius.robotics.robots.interfaces.*;
 
public class X80PCMTest implements IRobotAudio 
 {
 	private X80Pro robot;
 	
 	public X80Pro getRobot()
 	{
 		return robot;
 	}
 
 	public static void main(String[] args)
 	{
		X80PCMTest pcmtest = new X80PCMTest();
 		pcmtest.getRobot().startAudioRecording((byte) (0xFF & 255));
 		pcmtest.getRobot().stopAudioRecording();
 	}
 	
	public X80PCMTest()
 	{
 		try
 		{
 			robot = new X80Pro("192.168.0.201", this, null);
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	
 	@Override
 	public void audioEvent(String robotIP, int robotPort, short[] audioData)
 	{
 		
 		// Do something with raw pcm waveform?
 	}
 	
 }
