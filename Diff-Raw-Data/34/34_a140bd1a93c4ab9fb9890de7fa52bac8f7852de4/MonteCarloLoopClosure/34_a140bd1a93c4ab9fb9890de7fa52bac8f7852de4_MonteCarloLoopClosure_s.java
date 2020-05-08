 package experiment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.vecmath.Tuple2i;
 
 import tool.BackboneSegmentAnalyser;
 import tool.ChainTreeScene;
 import tool.Tuple2;
 import dataStructure.AdjustableChainTree;
 import dataStructure.ChainTree;
 import energyFunction.AtomDistance;
 import energyFunction.EndAtomDistance;
 import energyFunction.EnergyFunction;
 
 public class MonteCarloLoopClosure {
 	public static void main(String[] args) throws InterruptedException {
 		/*
 		 * Configuration.
 		 */
		String pdbId = "1FUS"; // 1PUX, 1RKI, 1T0G, 1F3U, 1XJH, 1JN1, 1X6J, 2B7T
		int segmentNo = 3;
 		
 		/*
 		 * Setup.
 		 */
 		AdjustableChainTree cTree = new AdjustableChainTree(pdbId);
 		
 		Tuple2<Integer, Integer> segment = BackboneSegmentAnalyser.extractIntermediateSegments(cTree).get(segmentNo);
 		int start = segment.e1;
 		int end = segment.e2;
 
 		// create subtrees
 		AdjustableChainTree t0 = cTree.getSubchain(0, start+1);
 		AdjustableChainTree t1 = cTree.getSubchain(0, end);
 		AdjustableChainTree t2 = cTree.getSubchain(end+1, cTree.length());
 		
 		ChainTree[] cTrees = {t1, t2}; 
 		ChainTreeScene scene = new ChainTreeScene(cTrees);
 		
 		// define energy function for the last atom
 		EnergyFunction energyFunction = new EndAtomDistance(t1, new AdjustableChainTree(t1));
 		
 		// find rotateable bonds in the segment
 		List<Integer> rotateableBonds = new ArrayList<Integer>();
 		
 		for (int i : t1.rotatableBonds()) {
 			if (start <= i && i <= end) {
 				rotateableBonds.add(i);
 			}
 		}
 		
 		// unfold segment
 		for (int i : rotateableBonds) {
 			do {
 				t1.changeRotationAngle(i, Math.random()*180);
 			} while(t1.isClashing() || t1.areClashing(t2));
 		}
 		
 		scene.repaint(t1);
 		
 		// make ready simulation
 		double energy = energyFunction.compute();
 		double startTime = System.currentTimeMillis();
 		
 		// simulate
 		int count = 0;
 		while(count < 100) {
 			int bond = rotateableBonds.get((int) (Math.random() * rotateableBonds.size()));
 			double angle = (Math.random()-0.5)*15*(Math.PI/180);
 			
 			t1.changeRotationAngle(bond, angle);
 			
 			if (!t1.isClashing() && !t1.areClashing(t2)) {
 			
 				double tmpEnergy = energyFunction.compute();
 				double iterationTime = System.currentTimeMillis() - startTime;
 			
 				if (tmpEnergy < energy || iterationTime > 5000) {
 					energy = tmpEnergy;
 					
 					// new conformation
 					if (energy < 1 || iterationTime > 5000) {
 						if (iterationTime < 5000) {
 							scene.add(t1.getSubchain(start-1, t1.length()), 1);
 							System.out.println(count + " computed in: " + iterationTime / 1000 + " sec.");
 							count++;
 						}
 						
 						// unfold segment
 						for (int i : rotateableBonds) {
 							do {
 								t1.changeRotationAngle(i, Math.random()*180);
 							} while(t1.isClashing() || t1.areClashing(t2));
 						}
						
 						energy = energyFunction.compute();
 						startTime = System.currentTimeMillis();
 					}
 					
 					scene.repaint(t1);
 					continue;
 				}
 				
 				if (tmpEnergy < energy * 1.01) {
 					continue;
 				}
 			}
 			
 			t1.changeRotationAngle(bond, -angle);
 		}
 	}
 }
