 package net.qmat.qmhh;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 public class CreaturesModel extends ProcessingObject {
 	
 	private ArrayList<Creature> creatures;
 	
 	public CreaturesModel() {
 		creatures = new ArrayList<Creature>();
 	}
 	
 	public void addCreature() {
 		creatures.add(new Creature());
 	}
 	
 	public void update() {
 		for(Creature creature : creatures) {
 			creature.update(creatures);
 		}
 	}
 	
 	@SuppressWarnings("static-access")
 	public void draw() {
 		p.rectMode(p.CENTER);
 		p.noFill();
 		p.stroke(200);
 		for(Creature creature : creatures) {
 			creature.draw();
 		}
 	}
 	
 	public ArrayList<Creature> getTargeters(final float angle) {
 		ArrayList<Creature> orderedCreatures = new ArrayList<Creature>(creatures);
 		Collections.sort(orderedCreatures, new Comparator<Creature>() {
 			public int compare(Creature c1, Creature c2) {
 				PPoint2 c1p = c1.getPPosition();
 				PPoint2 c2p = c2.getPPosition();
 				// if one of the creatures has a target and the other doesn't, no need to check angles
 				if(c1.hasTargetP() && !c2.hasTargetP())
 					return 1;
				if(c2.hasTargetP() && !c1.hasTargetP())
					return -1;
 				// so they both don't have targets, or they both do, check angles
 				// TODO: check whether the ones with targets have companion hunters
 				if(calculateAngularDistance(angle, c1p.t) < calculateAngularDistance(angle, c1p.t))
 					return -1;
 				else if(calculateAngularDistance(angle, c2p.t) > calculateAngularDistance(angle, c1p.t))
 					return 1;
 				else
 					return 0;
 			}
 		});
 		return orderedCreatures;
 	}
 	
 	private float calculateAngularDistance(float a1, float a2) {
 		float d = Math.abs(a1 - a2);
 		return d < Main.PI ? d : Main.TWO_PI - d;
 	}
 	
 
 }
