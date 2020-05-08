 package org.amm.dp.budai.behavior.interpreter;
 
 //Terminal expression
 public class TV extends Goods {
 	@Override
 	public int Interpret(CurrentPricesContext context) {
 		int price = context.GetPrice("TV");
		System.out.printf("TV: %d", price);
 		return price;
 	}
 }
