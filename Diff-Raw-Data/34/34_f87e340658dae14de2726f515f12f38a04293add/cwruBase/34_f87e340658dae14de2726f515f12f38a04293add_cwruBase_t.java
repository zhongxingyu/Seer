 package cwru;
 
 import robocode.AdvancedRobot;
 import robocode.ScannedRobotEvent;
 
 public class cwruBase extends AdvancedRobot
 {
 	LifeBox fileSystem;
 	Sonar sally;
 	Gunner biggles;
 	Legs larson;
 	Projector lcd;
 	long processTime;
 	long totalTime;
 	public void run()
 	{
 		//================SETUP================
 		fileSystem = new LifeBox(this);
 		sally = new Sonar(fileSystem);
 		biggles = new Gunner(fileSystem);
 		larson = new Legs(fileSystem);
 		lcd = new Projector(fileSystem);
 		this.setAdjustGunForRobotTurn(true);
 		this.setAdjustRadarForGunTurn(true);
 		this.setAdjustRadarForRobotTurn(true);
 		//==========REPEATING ACTIONS==========
 		while(true)
 		{
 			//perform calculations
 			processTime = System.currentTimeMillis();
			System.out.println("pre error sonar");
 			sally.process(); //radar movement/data add
			System.out.println("pre error Projector");
 			lcd.process(); //projection of movement
			System.out.println("pre error Gunner?");
 			biggles.process(); //gun movement/data add
			System.out.println("pre error Legs");
 			larson.process(); //move movement/data add
 			totalTime = System.currentTimeMillis()-processTime;
 			System.out.println("TOTAL calc time (millis):"+totalTime);
 			
 			//set the movement based on calculations
 			processTime = System.currentTimeMillis();
 			sally.set();
 			biggles.set();
 			larson.set();
 			totalTime = System.currentTimeMillis()-processTime;
 			System.out.println("set Action time (millis):"+totalTime);
 			
 			//execute all movement
 			processTime = System.currentTimeMillis();
 			execute();
 			totalTime = System.currentTimeMillis()-processTime;
 			System.out.println("execute Time (millis):"+totalTime);
 		}
 	}
 	public void onScannedRobotEvent(ScannedRobotEvent sre)
 	{
 		sally.inputScan(sre);
 	}
 }
