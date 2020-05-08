 package src.main;
 
 public class Measurement{
 	private Bag<Node> measurements;
 
 	public Measurement(){
 		measurements = new Bag<Node>();
 	}
 	private class Node{
 		int n;
 		int space;
 		int numOps;
 		int diameter;
 		Node(int num, int s, int no, int d){
 			n = num;
 			space = s;
 			numOps = no;
 			diameter = d;
 		}
 	}
 
 	public void addData(int n, int s, int no, int d){
 		measurements.add(new Node(n, s, no, d));
 	}
 
 	public void averageAllAddToMeasurement(Measurement avg){
 		int spaceTotal = 0, numOpsTotal = 0, diameterTotal = 0, numTotal = 0;
 		for(Node n : measurements){
 			numTotal += n.n;
 			spaceTotal += n.space;
 			numOpsTotal += n.numOps;
 			diameterTotal += n.diameter;
 		}
 		int numAvg = numTotal/measurements.size();
 		int spaceAvg = spaceTotal/measurements.size();
 		int numOpsAvg = numOpsTotal/measurements.size();
		int diameterAvg = diameterTotal/measurements.size();
 		avg.addData(numAvg,spaceAvg, numOpsAvg, diameterAvg);
 	}
 
 	public String diameterDataString(){
 		StringBuilder s = new StringBuilder();
 		s.append("\\addplot coordinates {\n");
 		for(Node n: measurements){
 			s.append("("+n.n+", "+n.diameter+")\n");
 		}
 		s.append("};");
 		return s.toString();
 	}
 
 	public String spaceDataString(){
 		StringBuilder s = new StringBuilder();
 		s.append("\\addplot coordinates {\n");
 		for(Node n: measurements){
 			s.append("("+n.n+", "+n.space+")\n");
 		}
 		s.append("};");
 		return s.toString();
 	}	
 
 	public String numOpsDataString(){
 		StringBuilder s = new StringBuilder();
 		s.append("\\addplot coordinates {\n");
 		for(Node n: measurements){
 			s.append("("+n.n+", "+n.numOps+")\n");
 		}
 		s.append("};");
 		return s.toString();
 	}
 }
