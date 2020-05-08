 package rel;
 
 import rel.z.Basics;
 import rel.z.Einstein;
 import rel.z.Lorentz;
 
 public class Special 
 {	
 	
 	/**
 	 * 
 	 * @param A location of point A on a rigid body.
 	 * @param B location of point B on a rigid body.
 	 * @param S length of measuring rod in km
 	 * @return distance A B. Additional measurements to be used later.
 	 */
 	double initialMeasurementWithRod(double A, double B, double S)
 	{
 		int sCount = 0;
 		double distance = B - A;
 		double distanceMeasured = 0;
 		double distanceLeft = B - A;
 		
 		while (S * sCount < distance)
 		{
 			sCount++;
 			
 			distanceMeasured += S;
 			distanceLeft -= S;
 		}		
 		return sCount * S;
 	}
 	
 	
 	/**
 	 * 
 	 * @param v 
 	 * @return velocity W of light outside enclosure
 	 */
 	double propagationOfLightRelativeToEmbankment(double v)
 	{
 		return Basics.speedOfLightInVacuo() - v;
 	}
 	
 	/**
 	 * 
 	 * @param v
 	 * @return velocity W of light within enclosure
 	 */
 	double theoremOfAdditionofVelocities(double v)
 	{
 		return Basics.speedOfLightInVacuo() + v;
 	}
 	
 	/**
 	 * 
 	 * @param m mass of object moving
 	 * @param v velocity at which mass moves
 	 * @return
 	 */
 	public static double kineticEnergyOfMassM(double m, double v)
 	{
 		return Einstein.energy(m) / Lorentz.contractionInDirectionOfMotion(v);
 	}
 	
 	
 	/**
 	 * 
 	 * @param Eo energy
 	 * @param v velocity
 	 * @return The increase in energy of a body moving with velocity c after absorbing an amount of energy Eo
 	 */
 	double increaseInKineticEnergy(double Eo, double v)
 	{
 		return Eo / Lorentz.contractionInDirectionOfMotion(v);
 	}
 	
 	
 	/**
 	 * 
 	 * @param Eo
 	 * @param v
 	 * @param m
 	 * @return the new amount of energy of a body moving with velocity c after absorbing an amount of energy Eo
 	 */
 	double newAmountOfEnergy(double Eo, double v, double m)
 	{
 		return (Einstein.energy(m) + Eo) / Lorentz.contractionInDirectionOfMotion(v);		
 	}	
 	
 	/**
 	 * 
 	 * @param v
 	 * @param w
 	 * @param c
 	 * @return W velocity of light, Fizeau
 	 */
 	double propagationOfLightWithRespectToLiquid(double v, double w, double c)
 	{
 		double numerator = v + w;
 		
 		double denominator = 1 + ((v*w) / Basics.speedOfLightInVacuoSquared());
 		
 		return numerator / denominator;
 	}
 	
 	/**
 	 * 
 	 * @param v
 	 * @return 0
 	 */
 	double beginningOfRod(double v)
 	{
 		return 0 * Lorentz.contractionInDirectionOfMotion(v);
 	}
 	
 	/**
 	 * 
 	 * @param v
 	 * @return
 	 */
 	double endOfRod(double v)
 	{
 		return 1 * Lorentz.contractionInDirectionOfMotion(v);
 	}
 	
 	
 	/**
 	 * test this weekend.. after work, and refine final answer to something between milli and nanoseconds.
 	 * @param B
 	 * @param v
 	 */
 	void simultaneity(double B, double v)
 	{		
 		double A = 0;
 		double t = 0;
 		
		double distanceOfRailLine = B;
		
 		double startingPoint = B/2;
 		double distanceTravelledByObserver = 0;
 		
 		double distanceCoveredAtSpeedOfLightFromA = Basics.millisecond(Basics.speedOfLightInVacuo());
 		double distanceCoveredAtSpeedOfLightFromB = Basics.millisecond(Basics.speedOfLightInVacuo());
 				
 		double positionOfObserver = B/2;
 		
 		
 		while (B > positionOfObserver)
 		{
 			A += distanceCoveredAtSpeedOfLightFromA;
 			B -= distanceCoveredAtSpeedOfLightFromB;
 			distanceTravelledByObserver += v/1000;
 			positionOfObserver += v/1000;
 			t++;
 			
 		}
 		
		System.out.println("Distance of rail line A,B - " + distanceOfRailLine);
 		System.out.println("Milliseconds passed until the observer sees and/or passes the lightning strike from B while.. the mirrors at a 90 degree angle to where he sits have yet to register the lightning strike at A.. discounting the time passed between mirrors, eyes, and cognition - " + t);
 		System.out.println("Distance travelled by observer prior to seeing and/or passing lightning strike B - " + startingPoint + distanceTravelledByObserver);
 		System.out.println("Distance travelled by lightning strike at A - " + A);
 		System.out.println("Distance travelled by lightning strike at B - " + B);		
 	}
 }
