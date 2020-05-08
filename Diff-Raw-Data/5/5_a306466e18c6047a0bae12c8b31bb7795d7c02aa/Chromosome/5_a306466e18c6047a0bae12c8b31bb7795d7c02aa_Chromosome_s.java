 package geneticalgorithm;
 
 import java.util.Random;
 
 public class Chromosome implements Comparable
 {
 	/** Integer part of chromosome */
 	private int[] integer;
 	/** Float part of chromosome */
 	private int[] decimal;
 	/** Number of bits of each chromosome part */
 	private static int BITS = 16;
 	/** A rank to this chromosome, it means, how good it is to evaluate function*/
 	private double rank;
 
 	public Chromosome()
 	{
 		int i;
 		Random r = new Random();
 
 		this.rank = 0.0;
 		integer = new int[BITS];
 		decimal = new int[BITS];
 
 		for(i = 0; i < BITS; i++)
 		{
 			if(r.nextBoolean())
 				integer[i] = 1;
 			else
 				integer[i] = 0;
 
 			if(r.nextBoolean())
 				decimal[i] = 1;
 			else
 				decimal[i] = 0;
 		}
 	}
 
 	public Chromosome(int bits)
 	{
 		int i;
 		Random r = new Random();
 
 		this.rank = 0.0;
 		this.BITS = bits;
 
 		integer = new int[BITS];
 		decimal = new int[BITS];
 
 		for(i = 0; i < BITS; i++)
 		{
 			if(r.nextBoolean())
 				integer[i] = 1;
 			else
 				integer[i] = 0;
 
 			if(r.nextBoolean())
 				decimal[i] = 1;
 			else
 				decimal[i] = 0;
 		}
 
 	}
 
 	public int[] getInteger()
 	{
 		return integer;
 	}
 
 	public void setInteger(int[] integer)
 	{
 		this.integer = integer;
 	}
 
 	public int[] getDecimal()
 	{
 		return decimal;
 	}
 
 	public void setDecimal(int[] decimal)
 	{
 		this.decimal = decimal;
 	}
 
 	public double getRank() {
 		return rank;
 	}
 
 	public void setRank(double rank) {
 		this.rank = rank;
 	}
 
 	public double getValue()
 	{
 		int i;
 		int pint = 0, pdec = 0;
 
 		for(i = 0; i < BITS; i++)
 		{
 			pint += integer[i]*(Math.pow(2, i));
 			pdec += decimal[i]*(Math.pow(2, i));
 		}
 
 		return Double.parseDouble(String.format("%d.%d", pint, pdec));
 	}
 
 	/**
 	 * This function gets two Chromosomes
 	 * and make a crossover of them in a single
 	 * point.
 	 * 
 	 * The chromosome received will be modified
 	 * to be the result of crossover. 
 	 * 
 	 * @param p1 
 	 * @param p2
 	 */
 	public static void crossover(Chromosome p1, Chromosome p2)
 	{
 		int i, p;
 		int[] tmpint, tmpdec, pint1, pint2, pdec1, pdec2;
 		Random r = new Random();
 
 		tmpint = new int[Chromosome.BITS];
 		tmpdec = new int[Chromosome.BITS];
 
 		/* Get the vectors of bit to work */
 		pint1 = p1.getInteger();
 		pint2 = p2.getInteger();
 		pdec1 = p1.getDecimal();
 		pdec2 = p2.getDecimal();
 
 		/* Get the position to do the crossover */
 		p = r.nextInt(Chromosome.BITS);
 
 		/*
 		 * for 0  to p:
 		 * swap the i-th position of p1 and p2
 		 * 
 		 */
 		for(i = 0; i < p; i++)
 		{
 			/* Store in a temp var */
 			tmpint[i] = pint1[i];
 			tmpdec[i] = pdec1[i];
 
 			/* get pint2/pdec2 and put it in pint1/pdec1 */
 			pint1[i] = pint2[i];
 			pdec1[i] = pdec2[i];
 
 			/* get tmpint/tmpdec and put it in pint1/pdec1 */
 			pint2[i] = tmpint[i];
 			pdec2[i] = tmpdec[i];
 		}
 
 		/* store the new vector in the received objects */
 		p1.setInteger(pint1);
 		p1.setDecimal(pdec1);
 
 		p2.setInteger(pint2);
 		p2.setDecimal(pdec2);
 	}
 	
 	/**
 	 * Mutate a chromosome with a probability
 	 * indicated by tax
 	 * @param tax the probability of mutation
 	 */
 	public void mutation (double tax)
 	{
 	    int[] pint1, pdec1;
 	    int i;
 
 	    /* Get the vectors of bit to work */
 	    pint1 = this.getInteger();
 	    pdec1 = this.getDecimal();
 
 	    /* Select the bit to change */
	    i = (int) Math.ceil(Math.random()*Chromosome.BITS);
 	    /*
 	     * If the generated number is less than or equal
 	     * to tax, mutate the i-th position
 	     */
 	    if(Math.random() <= tax)
 	        pint1[i] = (pint1[i]+1)%2;
 
 	    /* Select the bit to change */
        i = (int) Math.ceil(Math.random()*Chromosome.BITS);
         /*
          * If the generated number is less than or equal
          * to tax, mutate the i-th position
          */
 	    if(Math.random() <= tax)
 	        pdec1[i] = (pdec1[i]+1)%2;
 	}
 	
 	@Override
 	public String toString()
 	{
 		String ret = new String("");
 		int i;
 
 		for(i = Chromosome.BITS - 1; i >= 0; i--)
 		{
 			ret = String.format("%s%d", ret, integer[i]);
 		}
 
 		ret = String.format("%s.", ret);
 
 		for(i = Chromosome.BITS - 1; i >= 0; i--)
 		{
 			ret = String.format("%s%d", ret, decimal[i]);
 		}
 
 		return ret;
 	}
 
 	public int compareTo(Object o)
 	{
 		//Cast to Chromosome
 		Chromosome c = (Chromosome) o;
 
 		//Compare
 		if(this.getRank() == c.getRank())
 		{
 			return 0;
 		}
 		else if(this.getRank() > c.getRank())
 		{
 			return 1;
 		}
 		else
 		{
 			return -1;
 		}
 	}
 }
