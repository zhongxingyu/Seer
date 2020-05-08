 package bmr;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import util.Util;
 
 public class BlockReducer extends Reducer<Text, Text, Text, Text> {
 
 	public BlockReducer() {}
 
 	/**The value in the BlockReducer has the nodeID appended to the beginning
 	 * This is because, now that the mapper maps to blocks, you need the nodeID information
 	 * somewhere else (not in the key, but the value)
 	 */
 
 	protected void reduce(Text key, java.lang.Iterable<Text> values, 
 			org.apache.hadoop.mapreduce.Reducer<Text, Text, Text, Text>.Context context)
 					throws IOException, InterruptedException {
 
 		Long blockID = Long.valueOf(key.toString());
 		Long beginningNodeID = new Long(blockID == 0 ? 0 : Util.blocks[blockID.intValue() - 1] + 1);
 		Long endingNodeID = new Long(Util.blocks[blockID.intValue()]);
 		int size = endingNodeID.intValue() - beginningNodeID.intValue() + 1;
 
 		//Array of max block size to map nodes to
 		double[] beginningPR = new double[size];
 		double[] PR = new double[size];
 		double[] NPR = new double[size];
 
 		// Array of the original mapper values for this node except the pagerank
 		/*
 		 * NumOuts
 		 * List of Outs
 		 */
 		String[] originalValues = new String[size];
 
 		// Array of the total incoming pagerank from outside node
 		double[] boundaryPR = new double[size];
 
 		// Array of incoming edges
 		String[] originNodes = new String[size];
 
 		/**Need first iteration to set everything up from the reducer**/
 
 		// Iterate through all the values
 		for (Text t : values) {
 			String v = t.toString();
 
 			String[] type = v.split(" ", 4);				
 			Long nodeID = Long.valueOf(type[1]);
 			int offset = nodeID.intValue() - beginningNodeID.intValue();
 			Double rank = Double.valueOf(type[2]);
 
 			// If this value is just the type setting value
 			if (type[0].equals("-1")) {
 				beginningPR[offset] = rank;
 				NPR[offset] = rank;
 				originalValues[offset] = type[3];
 			} else if (type.equals("0")) {
 				// If this value is from another node in the block
				originNodes[offset] += type[3] + " ";
 			} else {
 				// Then it's a boundary PR
 				boundaryPR[offset] += rank;
 			}
 		}
 		double residual = 0;
 
 		/*
 		 * Calculate the pageranks until convergence or until the residual is under a threshold
 		 */
 		do {
 			PR = NPR;
 			NPR = new double[size];
 			residual = IterateBlockOnce(beginningPR, PR, NPR, boundaryPR, originalValues, originNodes, beginningNodeID);
 			
 			// TODO: find a better metric to stop by
 		} while (residual > 0.001);
 
 		WriteKeyValue(context, beginningNodeID, NPR, originalValues);
 	}
 
 	private double IterateBlockOnce(double[] beginningPR, double[] PR, double[] NPR, 
 			double[] boundaryPR,	String[] originalValues, String[] originNodes, 
 			Long beginningNodeID) {
 		double residual = 0;
 		
 		// Iterate through all the nodes in the block
 		for (int i = 0 ; i < NPR.length ; i++) {
 
 			// Always add the boundary flow into this block
 			NPR[i] += boundaryPR[i];
 
 			StringTokenizer itr = new StringTokenizer(originNodes[i]);
 			while (itr.hasMoreTokens()) {
 
 				// Get the source of the edge
 				Long edgeFromID = Long.valueOf(itr.nextToken());
 				int offset = edgeFromID.intValue() - beginningNodeID.intValue();
 				
 				// Get the previous pagerank of that source
 				double edgeFromPageRank = PR[offset];
 				
 				// Get the numOuts of that source
 				long numOuts = Long.valueOf(originalValues[offset].split(" ")[0]);
 				
 				// If this origin doesn't go out, then it all goes to me/itself
 				if (numOuts == 0) {
 					numOuts = 1;
 				}
 				// Add the flow to me
 				NPR[offset] += edgeFromPageRank / ((double) numOuts);
 			}
 			
 			// Damping
 			NPR[i] = Util.dis + Util.damping * NPR[i];
 			residual += Math.abs(beginningPR[i] - NPR[i]) / NPR[i];
 		}
 		
 		return residual / (double) NPR.length;
 	}
 
 	/**
 	 * 
 	 * @param context
 	 * @param beginningNodeID
 	 * @param NPR
 	 * @param originalValues
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * 
 	 * Write out each key and value pair
 	 */
 	private void WriteKeyValue(org.apache.hadoop.mapreduce.Reducer<Text, Text, Text, Text>.Context context, 
 			long beginningNodeID, double[] NPR, String[] originalValues) 
 					throws IOException, InterruptedException {
 		for (int i = 0 ; i < NPR.length ; i++) {
 
 			long nodeID = i + beginningNodeID;
 			context.write(new Text("" + nodeID), new Text("" + NPR[i] + " " + originalValues[i]));
 		}
 	}
 }
