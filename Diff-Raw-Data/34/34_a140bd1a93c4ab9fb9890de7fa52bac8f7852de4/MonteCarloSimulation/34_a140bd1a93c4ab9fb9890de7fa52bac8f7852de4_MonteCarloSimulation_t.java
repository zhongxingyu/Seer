 package experiment;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.vecmath.Point3d;
 
 import tool.ChainTreeScene;
 import dataStructure.AdjustableChainTree;
 import energyFunction.AtomDistance;	
 import energyFunction.DihedralAngles;
 import energyFunction.EnergyFunction;
 
 public class MonteCarloSimulation {
 	public static void main(String[] args) throws InterruptedException{
 		/*
 		 * Configuration.
 		 */
		String pdbId = "2IFO"; // 1PUX, 1RKI, 1T0G, 1F3U, 1XJH, 1JN1, 1SIS (small)
 		double errorTolerance = 0.01;
 		
 
 		
 		/*
 		 * Setup.
 		 */
 		AdjustableChainTree cTree = new AdjustableChainTree(pdbId);
 		AdjustableChainTree target = new AdjustableChainTree(pdbId);
 		
 		EnergyFunction energyFunction = new AtomDistance(cTree, target);
 		List<Integer> rotateableBonds = cTree.rotatableBonds();
 		
 		errorTolerance++; // simpler computation
 		
 		cTree.unfold();
 
 		// setup scene
 		ChainTreeScene scene = new ChainTreeScene(cTree);
 		//scene.add(target, 5);
 		
 		// ready, set
 		double energy = energyFunction.compute();
 		double energyUpperBound = energy;
 		int iterations = 0;
 		
 		// GO!
 		while(true) {
 			iterations++;
 			
 			int i = rotateableBonds.get((int) (Math.random() * rotateableBonds.size()));
 			double angle = (Math.random()-0.5)*15*(Math.PI/180);
 			
 			cTree.changeRotationAngle(i, angle);
 			
 			if(cTree.isClashing()) {
 				// undo move if tree is clashing
 				cTree.changeRotationAngle(i, -angle);	
 				
 			} else {
 				// if not clashing then test for energy efficiency
 				double tmpEnergy = energyFunction.compute();
 				
 				if (tmpEnergy >= energyUpperBound) {
 					// if energy is higher than the upper bound then discard it
 					cTree.changeRotationAngle(i, -angle);	
 				
 				} else {
 					// try the new conformation
 					energy = tmpEnergy;
 					
 					if (tmpEnergy * errorTolerance < energyUpperBound) {
 						// permanently accept the new conformation
 						energyUpperBound = tmpEnergy * errorTolerance; 
 						
 						System.out.println(iterations + ": " + energy);
 						
 						scene.repaint(cTree);
 					}
 				}
 			}
 		}
 	}
 }
