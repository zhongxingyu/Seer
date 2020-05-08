 /** 
  * Author:  anthony.fodor@gmail.com
  * 
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 package bottomUpTree;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 import java.util.zip.GZIPOutputStream;
 
 import probabilisticNW.ProbSequence;
 import dereplicate.DereplicateBySample;
 
 public class RunOne
 {
 	public static final float INITIAL_THRESHOLD = 0.03f;
 	public static final float EXCEED_THRESHOLD = 0.09f;
 	
 	public static void main(String[] args) throws Exception
 	{
 		System.out.println("STARTING");
 		if( args.length != 2)
 		{
 			System.out.println("Usage RunOne inputFasta outputClusterFile");
 			System.exit(1);
 		}
 		
		String sampleName = args[0].replace(DereplicateBySample.DEREP_PREFIX, "");
 		File file = new File(args[0]);
 		List<ProbSequence> initialSeqs= 
 				ClusterAtLevel.getInitialSequencesFromFasta(
 						file.getAbsolutePath(), sampleName,INITIAL_THRESHOLD, 1.0f,sampleName);
 		
 		System.out.println("GOT SEQS");
 		int numAttempts = 1;
 		int newClusterSize = initialSeqs.size()+1;
 		
 		/*
 		//while( numAttempts <=10 && newClusterSize > initialSeqs.size())
 		{
 			System.out.println("Got " + initialSeqs.size() + " trying attempt " + numAttempts );
 			newClusterSize = initialSeqs.size();
 			List<ProbSequence> newCluster = new ArrayList<ProbSequence>();
 			//ClusterAtLevel.clusterAtLevel(newCluster, initialSeqs, INITIAL_THRESHOLD, 1.0f);
 			LogMultiple.clusterAtLevel(newCluster, initialSeqs, INITIAL_THRESHOLD, 1.0f);
 			initialSeqs = newCluster;
 			numAttempts++;
 		}
 		*/
 		
 		System.out.println("Finished with " + initialSeqs.size() + " in " + numAttempts + " attempts");
 		
 		ObjectOutputStream out =new ObjectOutputStream( new GZIPOutputStream(
 				new FileOutputStream(new File(args[1]))));
 		
 		out.writeObject(initialSeqs);
 		
 		out.flush(); out.close();
 	}
 }
