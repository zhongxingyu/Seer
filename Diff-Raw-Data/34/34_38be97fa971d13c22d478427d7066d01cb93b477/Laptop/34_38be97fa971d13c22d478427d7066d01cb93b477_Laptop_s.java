 package org.amm.dp.budai.behavior.interpreter;
 
 //Terminal expression
 public class Laptop extends Goods {
 	
 	@Override
 	public int Interpret(CurrentPricesContext context) {
 		int price = context.GetPrice("Laptop");
		System.out.printf("Laptop: {0}", price);
 		return price;
 	}
 }
