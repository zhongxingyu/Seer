 import java.util.Queue;
 
 
 public class Person
 {
 	private int currentFloor;
 	private Queue<Integer> nextFloors;
 	private Building myBuilding;
 	private int passNo;
 	
 	public Person(int currentFloor,Queue<Integer> nextFloors, Building building, int passNo)
 	{
 		this.currentFloor=currentFloor;
 		this.nextFloors=nextFloors;
 		myBuilding= building;
 		this.passNo=passNo;
 	}
 	
 	public void run()
 	{
 		System.out.println("New Person: "+passNo);
 		while(!nextFloors.isEmpty())
 		{
 			System.out.println("Passenger: " + passNo + " got to elevator");
 			int nextFloor=nextFloors.poll();
 			boolean down=nextFloor<currentFloor;
 			Elevator e = null;
 			System.out.println("Passenger: " + passNo + " called elevator");
 			if (down)
 			{
 				e = myBuilding.callDown(currentFloor);
 			}
 			else
 			{
 				e = myBuilding.callUp(currentFloor);
 			}
 			getOnElevator(e);
 			System.out.println("Passenger: " + passNo + " got on elevator");
 			e.requestFloor(nextFloor);
 			System.out.println("Passenger: " + passNo + " requested floor");
 			if(down)
 			{
 				myBuilding.awaitDown(nextFloor);
 			}
 			else
 			{
 				myBuilding.awaitUp(nextFloor);
 			}
 			getOffElevator(e);
 			System.out.println("Passenger: " + passNo + " got off elevator");
 			currentFloor=nextFloor;
 		}
 		
 	}
 	
 
	private void setNextFloor(int floor)
 	{
 		nextFloors.add(floor);
 	}
 	
	public void getOnElevator(Elevator e)
 	{
 		if (!e.enter())
 		{
 			try
 			{
 				Thread.sleep(3000);
 			} catch (InterruptedException e1)
 			{
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 	
 	private void getOffElevator(Elevator e)
 	{
 		e.exit();
 	}
 
 }
