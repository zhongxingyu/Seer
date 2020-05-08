 import java.util.Arrays;
 
 import javax.swing.*;        
 
 public class RouterNode {
 	private boolean poisonedReverse = true;
 
 	private int myID;
 	private GuiTextArea myGUI;
 	private RouterSimulator sim;
 	private int[] costs = new int[RouterSimulator.NUM_NODES];
 	private int[][] distanceTable = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
 	private int[] minRoute = new int[RouterSimulator.NUM_NODES];
 
 	//--------------------------------------------------
 	public RouterNode(int ID, RouterSimulator sim, int[] costs) {
 		myID = ID;
 		this.sim = sim;
 		myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
 
 		System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);
 		
 		// Initialize all distance vectors values to infinity
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 			for (int j = 0; j < RouterSimulator.NUM_NODES; j++)
 				distanceTable[i][j] = RouterSimulator.INFINITY;
 		
 		// Set this node's distance vector to the direct link costs.
 		System.arraycopy(costs, 0, distanceTable[myID], 0, RouterSimulator.NUM_NODES);
 		
 		// Initialize the minimal routes to the direct links if they exist.
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 		{
 			if (costs[i] != RouterSimulator.INFINITY)
 				minRoute[i] = i;
 			else
 				minRoute[i] = RouterSimulator.INFINITY;
 		}
 		
 		sendDistanceVector();
 	}
 
 	//--------------------------------------------------
 	public void recvUpdate(RouterPacket pkt) {
 		
 		System.arraycopy(pkt.mincost, 0, distanceTable[pkt.sourceid], 0, RouterSimulator.NUM_NODES);
 		recalculateDistanceVector();
 	}
 
 	/**
 	 * Recalculate the distance vector of this node.
 	 */
 	private void recalculateDistanceVector() {
 		int[] newDistanceVector = new int[RouterSimulator.NUM_NODES];
 
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 		{
 			int path = minRoute[i] = findShortestPath(i);
			newDistanceVector[i] = costs[path] + distanceTable[path][i];
 		}
 
 		if (!Arrays.equals(newDistanceVector, distanceTable[myID])) {
 			distanceTable[myID] = newDistanceVector;
 			sendDistanceVector();
 		}
 	}
 	
 	/**
 	 * Finds the shortest path to the router with ID dest.
 	 * @param dest The ID of the router that we should find the shortest
 	 * path to.
 	 * @return The ID of the first router in the path to dest.
 	 */
 	private int findShortestPath(int dest) {
 		
 		int distance = costs[dest];
 		int path;
 		
 		if (distance != RouterSimulator.INFINITY)
 			path = dest;
 		else
 			path = RouterSimulator.INFINITY;
 		
 		
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 		{
 			if (i == myID || i == dest)
 				continue;
 			
 			/* Check if the path through i is shorter than the currently shortest
 			 * path found. */
 			if (costs[i] != RouterSimulator.INFINITY &&
 					distanceTable[i][dest] != RouterSimulator.INFINITY &&
 					costs[i] + distanceTable[i][dest] < distance) {
 				distance = costs[i] + distanceTable[i][dest];
 				path = i;
 			}
 		}
 		return path;
 	}
 	
 	private void sendDistanceVector() {
 		
 		// Send distance vector to all adjacent routers
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 		{
 			if (i == myID || costs[i] == RouterSimulator.INFINITY)
 				continue;
 			
 			// Create a poisoned distance vector
 			int[] distVector = new int[RouterSimulator.NUM_NODES];
 			for (int k = 0; k < RouterSimulator.NUM_NODES; k++)
 			{
 				if (poisonedReverse && i == minRoute[k])
 					distVector[k] = RouterSimulator.INFINITY;
 				else
 					distVector[k] = distanceTable[myID][k];
 			}
 			
 			// Send the poisoned distance vector
 			RouterPacket pkt = new RouterPacket(myID, i, distVector);
 			sendUpdate(pkt);
 		}
 	}
 	
 	//--------------------------------------------------
 	private void sendUpdate(RouterPacket pkt) {
 		sim.toLayer2(pkt);
 	}
 
 
 	//--------------------------------------------------
 	public void printDistanceTable() {
 		StringBuilder b;
 		
 		myGUI.println("Current table for " + myID +
 				"  at time " + sim.getClocktime());
 		
 		myGUI.println("Distancetable:");
 		b = new StringBuilder(F.format("dst", 7) + " | ");
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 			b.append(F.format(i, 5));
 		myGUI.println(b.toString());
 		
 		for (int i = 0; i < b.length(); i++)
 			myGUI.print("-");
 		myGUI.println();
 		
 		for (int source = 0; source < RouterSimulator.NUM_NODES; source++)
 		{
 			if (source == myID)
 				continue;
 			
 			b = new StringBuilder("nbr" + F.format(source, 3) + " | ");
 			for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 				b.append(F.format(distanceTable[source][i], 5));
 			myGUI.println(b.toString());
 		}
 		
 		myGUI.println("\nOur distance vector and routes:");
 		
 		b = new StringBuilder(F.format("dst", 7) + " | ");
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 			b.append(F.format(i, 5));	
 		myGUI.println(b.toString());
 		
 		for (int i = 0; i < b.length(); i++)
 			myGUI.print("-");
 		myGUI.println();
 		
 		b = new StringBuilder(F.format("cost", 7) + " | ");
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 			b.append(F.format(distanceTable[myID][i], 5));
 		myGUI.println(b.toString());
 		
 		b = new StringBuilder(F.format("route", 7) + " | ");
 		for (int i = 0; i < RouterSimulator.NUM_NODES; i++)
 		{
 			if (minRoute[i] != RouterSimulator.INFINITY)
 				b.append(F.format(minRoute[i], 5));
 			else
 				b.append(F.format("-", 5));
 		}
 		myGUI.println(b.toString());
 		myGUI.println();
 	}
 
 	//--------------------------------------------------
 	public void updateLinkCost(int dest, int newcost) {
 		costs[dest] = newcost;
 		recalculateDistanceVector();
 	}
 
 }
