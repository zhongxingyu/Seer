 public class Subset {
 
 	public static void main(String[] args) {
 		int K = Integer.parseInt(args[0]);
 		RandomizedQueue<String> queue = new RandomizedQueue<String>();
 		for (String s : StdIn.readAllStrings())
 			queue.enqueue(s);
		int i = 0;
 		for (String s : queue) {
			if (i == K)
				break;
 			StdOut.println(s);
			i += 1;
 		}
 	}
 }
