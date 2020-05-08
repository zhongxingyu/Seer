 
 public class randmst {
 	public static void main (String[] args) {
 		String usage = "Usage: <program> <run version> <numpoints> <numtrials> <dimension>";
         if (args.length != 4) {
             System.out.println("Wrong number of arguments.");
             System.out.println(usage);
             return;
         }
         
         int version = Integer.parseInt(args[0]);
         int numpoints = Integer.parseInt(args[1]);
         int numtrials = Integer.parseInt(args[2]);
         int dimension = Integer.parseInt(args[3]);
         
         switch (version) {
         	case 0:
         		Graph g0;
         		double N = 5;
         		double totalWeights = 0;
         		for (int i = 0; i < numtrials; i++) {
         			g0 = new AdjListGraph(dimension, numpoints, N);
         			totalWeights += g0.prim();
         		}
         		double averageWeight = totalWeights / numtrials;
         		System.out.println(averageWeight + "  " + numpoints + "  " + numtrials + "  " + dimension);
         		break;
         	case 1:
         		Graph g1 = new AdjListGraph(dimension, numpoints, 0.1);
         		//g1.print();
         		System.out.println("done");
         		break;
         	case 2:
         		Graph g2 = new AdjListGraph(dimension, numpoints, 5);
         		g2.print();
         		g2.prim();
         		break;
         	case 3:
         		int[] array = {1,2,10,8,3,9,7,4,6,5};
         		MinHeap h = new MinHeap();
         		
         		for(int i = 0; i < 10; i++) {
         			h.insert(new AdjListNode(i,array[i]));
         		}
         		
         		while (!h.isEmpty()) {
         			System.out.println(h.deleteMin().toString() + "\n");
         		}
         		//h.print();
         		break;
         		
         	default:
         		System.exit(1);
         }
 	}
 }
