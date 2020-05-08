 /**
  * @author Jonas Raditschnig (1021251)
  */
 
 public class Pumpkin implements Growable
 {
 	
 	private double 	weight;
 	private int 	age;
 	
 	/**
	 * Returns the percentage of pumpkin that will be eaten by snails.
	 * @param d current day
	 * @param c Competition that saves all past days
 	 */
 	private double snails(IntDay d, IntCompetition c)
 	{
 		double snailrate = 0.0;
 		
 		snailrate += d.getPerRain()>=50	 ? 0.1 : 0.0;  
 		try
 		{		
 			snailrate += c.getAvgRain(2)>=30 ? 0.1 : 0.0;
 			snailrate += c.getAvgRain(4)>=10 ? 0.1 : 0.0;
 		}
		catch( CompException e ) {} // Overflow of getAvg controlled by Exception
 		
 		return snailrate;
 	}
 
 	/**
	 * Creates a new pumpkin with age and weight
 	 */
 	public Pumpkin()
 	{
 		this.weight 	= 1.0;
 		this.age 	= 0;
 	}
 
 	/**
	 * Lets the Pumpkin age and grow in relation to the current and past weather.
	 * 1% of sun results in 0.05% growth, the amount of rain the last days results in less (or no) growth.
	 * Too much rain results in in the pumpkin getting eaten by snails.
	 * @param d Current day
	 * @param c Competition that saves all past days
 	 */
 	@Override
 	public void grow(IntDay d, IntCompetition c)
 	{
		double growthrate = d.getPerSun() * 0.0005;
 
 		try
 		{
 			growthrate /= c.getAvgRain(5)<10 ? 2 : 1;
 			growthrate = c.getAvgRain(10)<10 ? 0 : growthrate;
 		}
		catch( CompException e ) {} // Overflow of getAvg controlled by Exception
 		
 		this.weight *= (growthrate + 1);
 		this.weight -= this.snails(d, c)*this.weight;
 		this.age++;
 	}
 
 	/**
	 * Returns the weight
 	 */
 	@Override
 	public double getWeight()
 	{
 		return this.weight;
 	}
 
 	/**
	 * Returns the age
 	 */
 	@Override
 	public int getAge()
 	{
 		return this.age;
 	}
 
 }
