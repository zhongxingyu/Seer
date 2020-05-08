 package factory;
 
 import inventory.fruit.Apple;
 import inventory.fruit.Banana;
 import inventory.fruit.Pear;
 
 import java.util.*;
 
 import factory.machine.BufferMachine;
 import factory.machine.Machine;
 import factory.plant.*;
 import factory.dimension.ConveyorBeltDimension;
 import factory.dimension.PointXY;
 import factory.dimension.SorterDimension;
 
 import draw.StdDraw;
 
import buffer.ProductionBuffer;

 /**
  * Factory.java
  * 
  * @author:			Devin Barry
  * @date:			01.10.2012
  * @lastModified: 	23.10.2012
  *
  * This class draws conveyors and can be manipulated by SystemJ to
  * animate the conveyors.
  * 
  * Some new test methods have been added recently 22.10.2012
  */
 
 /*************************************************************************
  * Dependecies: StdDraw.java
  * 
  *************************************************************************/
 
 public class Factory {
 	
 	public static final int WINDOW_LENGTH = 600;
 	public static final int WINDOW_HEIGHT = 1200;
 	public static final double SCALE = 2.0; //how the factory dimensions relate to the window dimensions
 	
 	private static final double FACTORY_LENGTH = WINDOW_LENGTH * SCALE;
 	private static final double FACTORY_HEIGHT = WINDOW_HEIGHT * SCALE; 
 	
 	
 	public static List<Machine> productionLine = new ArrayList<Machine>(20); //initial capacity of 20
 	static final PointXY base = new PointXY(0, 0);
 	
 	public Factory() {
 		StdDraw.setCanvasSize(WINDOW_HEIGHT, WINDOW_LENGTH); //pixel size of window
 		StdDraw.setXscale(0, FACTORY_HEIGHT);
 		StdDraw.setYscale(0, FACTORY_LENGTH);
 		StdDraw.show(0);
 	}
 	
 	public void paint() {
 		paint(30);
 	}
 	
 	/**
 	 * This should probably be made to be thread safe
 	 * TODO
 	 * @param delay
 	 */
 	public void paint(int delay) {
 		// clear the background
 		//StdDraw.clear(StdDraw.GRAY);
 		StdDraw.clear();
 		
 		//Draw all machines in the factory
 		Iterator<Machine> i = productionLine.iterator();
 		while (i.hasNext()) {
 			i.next().draw(base);
 		}
 		
 		//Show everything that has been drawn
 		StdDraw.show(delay);
 	}
 
 	/**
 	 * a test client for the factory
 	 */
 	public static void main(String[] args) {
 		final Factory c = new Factory();
 		c.testFactory();
 		c.addTestFruits();
 		c.paint();
 		
 		try { Thread.sleep(3000); } catch (Exception e) {}
 		c.advanceConveyors();
 		c.paint();
 		try { Thread.sleep(3000); } catch (Exception e) {}
 		c.advanceConveyors();
 		c.paint();
 		try { Thread.sleep(3000); } catch (Exception e) {}
 		c.advanceConveyors();
 		c.paint();
 		try { Thread.sleep(3000); } catch (Exception e) {}
 		c.advanceConveyors();
 		c.paint();
 	}
 	
 	/**
 	 * Tests a mock factory setup
 	 */
 	private void testFactory() {
 		double conveyorLine1Y = 150;
 		PointXY hbl1_1Pos = new PointXY(100,conveyorLine1Y);
 		HoldingBay hbl1_1 = new HoldingBay(hbl1_1Pos);
 		productionLine.add(hbl1_1);
 		
 		PointXY sl1_1Pos = new PointXY(hbl1_1.nextMachineStartPoint().getX(), hbl1_1.nextMachineStartPoint().getY() + SorterDimension.RADIUS);
 		Sorter sl1_1 = new Sorter(sl1_1Pos);
 		productionLine.add(sl1_1);
		ProductionBuffer s1pb = sl1_1.getProductionBuffer();
		System.out.println("test " + s1pb.isFull());
 		
 		PointXY cbl1_1Pos = new PointXY(sl1_1.nextMachineStartPoint().getX(), sl1_1.nextMachineStartPoint().getY() + (ConveyorBeltDimension.WIDTH / 2));
 		ConveyorBelt cbl1_1 = new ConveyorBelt(cbl1_1Pos, 6, 0); //size 8, 0 angle
 		PointXY cbl1_2Pos = new PointXY(cbl1_1.nextMachineStartPoint());
 		ConveyorBelt cbl1_2 = new ConveyorBelt(cbl1_2Pos, 12, 15); //size 8, 15 degree angle
 		PointXY cbl1_3Pos = new PointXY(cbl1_2.nextMachineStartPoint());
 		ConveyorBelt cbl1_3 = new ConveyorBelt(cbl1_3Pos, 8, 0); //size 8, 0 angle
 		productionLine.add(cbl1_1);
 		productionLine.add(cbl1_2);
 		productionLine.add(cbl1_3);
 		
 		double conveyorLine2Y = 300;
 		PointXY cbl2_1Pos = new PointXY(100,conveyorLine2Y);
 		ConveyorBelt cbl2_1 = new ConveyorBelt(cbl2_1Pos, 8, 0); //size 8, 0 angle
 		PointXY cbl2_2Pos = new PointXY(cbl2_1.nextMachineStartPoint());
 		ConveyorBelt cbl2_2 = new ConveyorBelt(cbl2_2Pos, 8, -35); //size 8, 30 angle
 		productionLine.add(cbl2_1);
 		productionLine.add(cbl2_2);
 		
 		PointXY el2_1Pos = new PointXY(cbl2_2.nextMachineStartPoint());
 		ExtendedPlatform el2_1 = new ExtendedPlatform(el2_1Pos);
 		productionLine.add(el2_1);
 		
 		PointXY cbl2_3Pos = new PointXY(el2_1.nextMachineStartPoint());
 		ConveyorBelt cbl2_3 = new ConveyorBelt(cbl2_3Pos, 8, 15); //size 8, 0 angle
 		productionLine.add(cbl2_3);
 		PointXY hbl2_1Pos = new PointXY(cbl2_3.nextMachineStartPoint());
 		HoldingBay hbl2_1 = new HoldingBay(hbl2_1Pos);
 		productionLine.add(hbl2_1);
 		
 		
 		ConveyorBelt test1 = new ConveyorBelt(new PointXY(600, 500), 19, 160);
 		productionLine.add(test1);
 		ConveyorBelt test2 = new ConveyorBelt(new PointXY(600, 500), 3, 0);
 		productionLine.add(test2);
 		PointXY sl3_1Pos = new PointXY(test2.nextMachineStartPoint());
 		Sorter sl3_1 = new Sorter(sl3_1Pos);
 		productionLine.add(sl3_1);
 		
		System.out.println("test " + s1pb.isFull());
 	}
 	
 	/**
 	 * Add various test fruits to the machines for testing
 	 */
 	private void addTestFruits() {
 		int maxCount;
 		double random;
 		Machine m;
 		BufferMachine bm;
 		Iterator<Machine> i = productionLine.iterator();
 		while (i.hasNext()) {
 			m = i.next();
 			if (m instanceof BufferMachine) bm = (BufferMachine)m;
 			else continue;
 			
 			if (m instanceof HoldingBay) maxCount = 49;
 			else if (m instanceof ConveyorBelt) maxCount = 6;
 			else maxCount = 1;
 			
 			for (int j = 0; j < maxCount; j++) {
 				random = Math.random();
 				if (random <= 0.333) bm.addInventory(new Apple());
 				else if (random <= 0.666) bm.addInventory(new Banana());
 				else bm.addInventory(new Pear());
 			}
 		}
 	}
 	
 	/**
 	 * Tests advancing of conveyors
 	 */
 	private void advanceConveyors() {
 		Machine m;
 		BufferMachine bm;
 		Iterator<Machine> i = productionLine.iterator();
 		while (i.hasNext()) {
 			m = i.next();
 			if (m instanceof BufferMachine) bm = (BufferMachine)m;
 			else continue;
 			
 			if (bm instanceof ConveyorBelt) {
 				bm.AdvanceBuffer();
 			}
 		}
 	}
 
 }
