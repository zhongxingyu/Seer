 package org.amm.dp.budai.behavior.interpreter;
 
 import java.util.Arrays;
 
 public class InterpreterDemo {
 
 	public static void main(String[] args) {
 		new InterpreterDemo().RunInterpreterDemo();
 	}
 
 	public void RunInterpreterDemo() {
 		
 		// create syntax tree that represents sentence
 		GoodsPackage truckWithGoods = PrepareTruckWithGoods();
 		// get latest context
 		CurrentPricesContext pricesContext = GetRecentPricesContext();
 		// invoke Interpret
 		int totalPriceForGoods = truckWithGoods.Interpret(pricesContext);
 
		System.out.printf("Total: %d", totalPriceForGoods);
 	}
 
 	private CurrentPricesContext GetRecentPricesContext() {
 		CurrentPricesContext pricesContext = new CurrentPricesContext();
 		pricesContext.SetPrice("Bed", 400);
 		pricesContext.SetPrice("TV", 100);
 		pricesContext.SetPrice("Laptop", 500);
 		return pricesContext;
 	}
 
 	public GoodsPackage PrepareTruckWithGoods() {
 		GoodsPackage truck = new GoodsPackage();
 
 		Bed bed = new Bed();
		GoodsPackage packedBed = new GoodsPackage(
				Arrays.asList((Goods)bed));
 
		truck.GoodsInside.add(packedBed);
 		truck.GoodsInside.add(new TV());
 		truck.GoodsInside.add(new TV());
 
		truck.GoodsInside.add(new GoodsPackage(Arrays.asList((Goods)new Laptop(),
 				new Laptop(), new Laptop())));
 
 		return truck;
 	}
 }
