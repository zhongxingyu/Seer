 package org.amm.dp.budai.behavior.interpreter;
 
 //Terminal expression
 public class Bed extends Goods {
 	
 	@Override
 	public int Interpret(CurrentPricesContext context) {
 		int price = context.GetPrice("Bed");
		System.out.printf("Bed: {0}", price);
 		return price;
 	}
 }
