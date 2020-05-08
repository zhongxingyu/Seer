 public class Subset {
 
 	public static void main(String[] args) {
 		int K = Integer.parseInt(args[0]);
 		RandomizedQueue<String> queue = new RandomizedQueue<String>();
 		for (String s : StdIn.readAllStrings())
 			queue.enqueue(s);
 		for (String s : queue) {
 			StdOut.println(s);
 		}
 	}
 }
