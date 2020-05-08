 package de.noonex.moddropplugin.amount;
 
 import java.text.ParseException;
 
 public final class AmountParser
 {
 	public static Amount ParseAmount(String amountstr) throws ParseException
 	{		
 		if(amountstr.contains("-"))
 		{
 			return parseRangeAmount(amountstr);
 		}
 		else if(amountstr.contains(","))
 		{
 			return parseMultipleAmount(amountstr);
 		}
 		else
 		{
 			int amount;
 			
 			try
 			{
 				amount = Integer.parseInt(amountstr);
 			}
 			catch(NumberFormatException ex)
 			{
 				throw new ParseException("The amount is invalid.",0);
 			}
 			
 			return new NormalAmount(amount, 1);
 		}	
 	}
 
 	private static Amount parseMultipleAmount(String amountstr)
 			throws ParseException
 	{
		String[] multipleAmountString = amountstr.split("-");
 		int[] amounts = new int[multipleAmountString.length];
 		
 		for(int i = 0; i < amounts.length; i++)
 		{
 			try
 			{
 				amounts[i] = Integer.parseInt(multipleAmountString[i]);
 			}
 			catch(NumberFormatException ex)
 			{
 				throw new ParseException("The amount is invalid.",0);
 			}
 		}
 		
 		return new MultipleAmount(amounts);
 	}
 
 	private static Amount parseRangeAmount(String amountstr)
 			throws ParseException
 	{
 		String[] minmax = amountstr.split("-");
 		int min, max;
 		
 		if(minmax.length != 2)
 		{
 			throw new ParseException("The amount is invalid.",0);
 		}
 		
 		try
 		{
 			min = Integer.parseInt(minmax[0]);
 			max = Integer.parseInt(minmax[1]);
 		}
 		catch(NumberFormatException ex)
 		{
 			throw new ParseException("The amount is invalid.",0);
 		}
 		
 		return new RangeAmount(min, max);
 	}
 }
