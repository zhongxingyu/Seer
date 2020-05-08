 import java.util.ArrayList;
 import java.util.Random;
 
 public class RandomStupid implements Algorithm {
 
 	int size;
 	boolean[] visited;
 
 	public RandomStupid(World w) {
 		size = w.getSize();
 		visited = new boolean[size];
 	}
 
 	@Override
 	public int[] solve() {
 		
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		int[] answer = new int[size];
 		
 		for (int i = 0; i < size; i++) {
 			list.add(i);
 		}
 		
 		for (int i = 0; i < size; i++) {
			Random rn = new Random();
 			
			int random = rn.nextInt() % list.size(); 
 			int value = list.get(random);
 			list.remove(random);
 			answer[i] = value;
 		}
 		
 		return answer;
 	}
 }
