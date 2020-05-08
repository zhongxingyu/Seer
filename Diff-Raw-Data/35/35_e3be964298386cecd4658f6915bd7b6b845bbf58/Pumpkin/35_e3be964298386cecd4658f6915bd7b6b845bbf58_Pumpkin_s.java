 /**
  * @author Jonas Raditschnig (1021251)
  */
 
 public class Pumpkin implements Growable
 {
 	
 	private double 	weight;
 	private int 	age;
 	
 	/**
	 * Liefert den Prozentsatz des K�rbisses der von Schecken zerfressen wird.
	 * @param d Aktueller Tag
	 * @param c Wettkampf der alle vorherigen Tage gespeichert hat
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
		catch( CompException e ) {} // Overflow von getAvg durch Exception kontrolliert
 		
 		return snailrate;
 	}
 
 	/**
	 * Generiert einen K�rbis mit Gewicht und Alter.
 	 */
 	public Pumpkin()
 	{
 		this.weight 	= 1.0;
 		this.age 	= 0;
 	}
 
 	/**
	 * L�sst den K�rbiss je nach Wetter wachsen (und altern).
	 * 0,05% pro Prozent Sonne am Tag und zus�tzlich ist relevant ob eine Trockenperiode vorherrscht.
	 * Durch feuchtes Wetter k�nnen Schnecken den K�rbis zerfressen.
	 * @param d Aktueller Tag
	 * @param c Wettkampf der alle vorherigen Tage gespeichert hat
 	 */
 	@Override
 	public void grow(IntDay d, IntCompetition c)
 	{
		double growthrate = d.getPerSun() * 0.05;
 
 		try
 		{
 			growthrate /= c.getAvgRain(5)<10 ? 2 : 1;
 			growthrate = c.getAvgRain(10)<10 ? 0 : growthrate;
 		}
		catch( CompException e ) {} // Overflow von getAvg durch Exception kontrolliert
 		
 		this.weight *= (growthrate + 1);
 		this.weight -= this.snails(d, c)*this.weight;
 		this.age++;
 	}
 
 	/**
	 * Liefert das Gewicht
 	 */
 	@Override
 	public double getWeight()
 	{
 		return this.weight;
 	}
 
 	/**
	 * Liefert das Alter
 	 */
 	@Override
 	public int getAge()
 	{
 		return this.age;
 	}
 
 }
