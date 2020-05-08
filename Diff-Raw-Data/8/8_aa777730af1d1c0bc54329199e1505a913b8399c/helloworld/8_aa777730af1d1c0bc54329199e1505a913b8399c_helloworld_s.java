 import lejos.nxt.*;
 
 public class helloworld 
 {
 	
 	public static void main(String[] args) throws InterruptedException
 	{
 		System.out.println("Hello world!");
 		Button.waitForPress();
 		Motor.A.setSpeed(150);
 		Motor.B.setSpeed(400);
 		for(int i = 0; i < 15; i++)
 		{
 			if(i%2 == 0)
 			{
 				Motor.A.forward();
 				Motor.B.backward();
 			}
 			else
 			{
 				Motor.B.forward();
 				Motor.A.backward();
 			}
 			Thread.sleep(1000);
 		}
 		
		/*
		for(int i = 2; i < 400; i = i*i)
 		{
 			Motor.A.setSpeed(i);
 			Motor.B.setSpeed(i);
			Thread.sleep(30);
 		}
		*/
 		
 		Button.waitForPress();
 		Motor.A.backward();
 		Motor.B.backward();
 		Button.waitForPress();
 		Motor.A.stop();
 		Motor.B.stop();
 		System.out.println("Finished!");
 		Button.waitForPress();
 	}
 
 }
