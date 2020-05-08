 package artlife;
 
 import java.util.ArrayList;
 
 import behaviors.*;
 
 public class DNA implements Cloneable{
 
 	private ArrayList<Behavior> dna;
 	private Behavior current;
 	public static final double MUT_GENE_RATE=.05, MUT_DNA_RATE=.3, CROSS_RATE=.9, CROSS_EQUAL=.7;
 	
 	public DNA() {
 		dna = new ArrayList<Behavior>();
 	}
 	
 	public static DNA makeDefault() {
 		DNA temp = new DNA();
 		temp.dna.add(new GO_FORWARD(3));
 		temp.dna.add(new TURN(3));
 		temp.dna.add(new EAT(3));
 		temp.current = temp.dna.get(0);
 		return temp;
 	}
 	
 	public void performNextBehavior(Grid grid,Organism self) {
 		int next = current.perform(grid, self);
 		current = dna.get(next);
 	}
 	
 	public DNA clone() {
 		DNA temp = new DNA();
 		for(int i=0;i<dna.size();i++) {
 			temp.dna.add(dna.get(i).clone());
 		}
 		temp.current = temp.dna.get(0);
 		return temp;
 	}
 	
 	public DNA mutate() {
 		DNA temp = new DNA();
 		for(int i=0;i<dna.size();i++) {
 			if(Math.random()<MUT_GENE_RATE)
 				temp.dna.add(dna.get(i).mutate());
 			else
 				temp.dna.add(dna.get(i).clone());
 		}
 		temp.current = temp.dna.get(0);
 		return temp;
 	}
 	
 	public static DNA reproduce(DNA mom,DNA dad) {
 		DNA ma = (Math.random()<MUT_DNA_RATE)?mom.mutate():mom.clone();
 		DNA pa = (Math.random()<MUT_DNA_RATE)?dad.mutate():dad.clone();
 		
 		if(Math.random()>CROSS_RATE)
 			return ma;
 		
 		// Do crossover part here...
 		int mapoint = (int)(Math.min(ma.dna.size(),pa.dna.size())*Math.random());
 		int papoint = Math.random()>CROSS_EQUAL?mapoint:(int)(pa.dna.size()*Math.random());
 		DNA result = new DNA();
		result.dna.addAll(ma.dna.sublist(0,mapoint));
		result.dna.addAll(pa.dna.sublist(papoint, pa.dna.size()-1));
 		return result;
 	}
 	
 }
