 package de.noonex.moddropplugin.amount;
 
 import java.util.Random;
 
 public class RangeAmount extends Amount
 {
 	int min, max;
 	int differenceminmax;
 	Random rnd;
 	
 	public RangeAmount(int min, int max)
 	{
 		this.min = min;
 		this.max = max;
 		this.rnd = new Random();
 		
 		this.differenceminmax = this.max - this.min;
 	}
 
 	@Override
 	public void Do(IAmountable amountable)
 	{
		int amount = this.rnd.nextInt(differenceminmax) + this.min;
 		for(int i = 0; i < amount; i++)
 		{
 			amountable.Do(amount);
 		}
 	}
 
 }
