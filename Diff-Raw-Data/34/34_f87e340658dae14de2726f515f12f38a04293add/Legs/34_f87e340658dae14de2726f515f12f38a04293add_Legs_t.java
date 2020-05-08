 package cwru;
 
 public class Legs extends Brain
 {
 	double moveEndTheta;
 	double moveEndDistance;
 	double randomW;
 	double randomH;
 	public Legs(LifeBox source) 
 	{
 		super(source);
		System.out.println("pre error source exists");
 	}
 	//Legs is the brain that controls movement of the robot
 	public void process()
 	{
 		long processTime = System.currentTimeMillis();
 		if (robot_near_wall())
 		{
 			double width = source.battlefield_width;
 			double heigh = source.battlefield_height;
			randomW = (width/3)+Math.random()*(width/3);
			randomH = (heigh/3)+Math.random()*(heigh/3);
 		}
 		moveEndTheta = choose_radians_to_turn_left(); //calculate desired theta
 		moveEndDistance = choose_distance_to_move(); //calculate desired distance
 		long totalTime = System.currentTimeMillis()-processTime;
 		System.out.println("MOV calc time (millis):"+totalTime);
 	}
 	public final void set()
 	{
 		source.mainRobot.setTurnLeftRadians(moveEndTheta);
 		source.mainRobot.setAhead(moveEndDistance);
 	}
 	public double choose_radians_to_turn_left()
 	{
 		double radian_result = 0;
 		//calculate the radians to turn left
 		if (robot_near_wall())//if the robot is near a wall
 		{
 			//move to center
 			radian_result = direction_to_point(source.mainRobot.getX(), 
 					source.mainRobot.getY(), randomW, randomH);
 		}
 		else if (source.getRobot().getTurnRemainingRadians()>.349) //greater than one move turn
 		{
 			//if it was turning, let it keep turning
 			radian_result = source.getRobot().getTurnRemainingRadians();
 		}
 		else if (source.getRobot().getDistanceRemaining()>8) //greater than one move turn
 		{
 			//if it is still moving from a prior setAhead()
 			radian_result = 0;
 		}
 		else
 		{
 			//do the normal thing
 			//if the robot isn't near an edge, 
 			//	it isn't already turning, 
 			//	and it isn't moving forward
 			double random_radian = Math.random()*2*Math.PI-Math.PI;
 			radian_result = random_radian;
 		}
 		return radian_result;
 	}
 	public double choose_distance_to_move()
 	{
 		double distance_result = 0;
 		//calculate the radians to turn left
 		if (robot_near_wall())//if the robot is near a wall
 		{
 			//move to center
 			distance_result = distance_to_point(source.mainRobot.getX(), source.mainRobot.getY(),
 					randomW, randomH);
 		}
 		else if (source.getRobot().getDistanceRemaining()>8) //greater than one move turn
 		{
 			//if it is still moving from a prior setAhead()
 			distance_result = source.getRobot().getDistanceRemaining();
 		}
 		else if (source.getRobot().getTurnRemainingRadians()>.349) //greater than one move turn
 		{
 			//if it was turning, let it keep turning
 			distance_result = 0;
 		}
 		else
 		{
 			//do the normal thing
 			//if the robot isn't near an edge, 
 			//	it isn't already turning, 
 			//	and it isn't moving forward
 			double random_radian = Math.random()*2*Math.PI-Math.PI;
 			distance_result = random_radian;
 		}
 		return distance_result;
 	}
 	public boolean robot_near_wall()
 	{
 		boolean near_wall = false;
		System.out.println("pre error get Robot call");
		cwruBase source_bot = source.getRobot();
		System.out.println("pre error getX call");
		double xCoord = source_bot.getX();
		double yCoord = source_bot.getY();
		System.out.println("post getX call");
		if (xCoord<50 ||  //if the robot is close to the right or left
				xCoord>(source.battlefield_width-50))
 		{
 			near_wall =  true;
 		}
		if (yCoord<50 ||  //if the robot is close to the top or bottom
				yCoord>(source.battlefield_height-50))
 		{
 			near_wall =  true;
 		}
 		return near_wall;
 	}
 	public double direction_to_point(double px,double py, double mx, double my)
 	{
 		double radians_return = 0;
 		//adjust for rotated coordinate system
 		radians_return = (-Math.atan2(py-my, px-mx)+Math.PI/2)%(2*Math.PI);
 		return radians_return;
 	}
 	public double distance_to_point(double px, double py, double mx, double my)
 	{
 		return Brain.distance(px, py, mx, my);
 	}
 }
