 package util;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class RandomList<A extends Scoreable> {
 	
 	private List<A> l;
 	private double sum;
 	
 	public RandomList(){
 		l = new LinkedList<A>();
 		sum = 0;
 	}
 	
 	public void add(A item){
 		l.add(item);
 		sum += item.getScore();
 	}
 	
 	public A getRandomItem(){
 		double rand = Math.random() * sum;
		//System.out.println("GetRandomItem : rand = " + rand);
 		double val = 0;
 		for (int i = 0; i < l.size(); i++){
 			val += l.get(i).getScore();
			//System.out.println("\tValue = " + val);
			if (rand <= val)
 				return l.get(i);
 		}
 		return null;
 	}
	
	public int nbElements(){
		return l.size();
	}
 }
