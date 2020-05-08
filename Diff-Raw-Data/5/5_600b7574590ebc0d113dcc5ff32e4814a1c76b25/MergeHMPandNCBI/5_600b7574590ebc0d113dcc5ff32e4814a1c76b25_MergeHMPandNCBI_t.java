 /** 
  * Author:  anthony.fodor@gmail.com    
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 
 package scripts.bigHMPBlast;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.zip.GZIPInputStream;
 
 import utils.ConfigReader;
 
 public class MergeHMPandNCBI
 {
 	private static class Holder
 	{
 		int numNCBI=0;
 		int numHMP=0;
 		int numMerged=0;
 		
 		@Override
 		public String toString()
 		{
 			return "" + numNCBI + " " + " " + numHMP + " " + numMerged;
 		}
 	}
 	
 	public static void main(String[] args) throws Exception
 	{
 		HashMap<Float, Holder> map = new HashMap<Float, Holder>();
 		
		for( int x=1; x <=20; x++)
 			addToMap(x, map);
 		
 		writeResults(map);
 	}
 	
 	private static void writeResults(HashMap<Float, Holder> map) throws Exception
 	{
 		double cumulativeNCBI=0;
 		double cumulativeHMP=0;
 		double cumulativeMerged=0;
 		
 		long sumNCBI=0;
 		long sumHMP=0;
 		long sumMerged =0;
 		
 		List<Float> keys = new ArrayList<Float>(map.keySet());
 		Collections.sort(keys);
 		Collections.reverse(keys);
 		
 		for( Float f : keys)
 		{
 			Holder h = map.get(f);
 			sumNCBI+= h.numNCBI;
 			sumHMP += h.numHMP;
 			sumMerged += h.numMerged;
 		}
 		
 		if(sumNCBI != sumHMP || sumNCBI != sumMerged ) 
 			throw new Exception("No " + sumNCBI + " " + sumHMP + " " + sumMerged);
 		
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ConfigReader.getBigBlastDir() + 
 				File.separator + "compareNCBI_HMP.txt")));
 		
 		writer.write("bitScore\tcumulativeNCBI\tcumulativeHMP\tcumulativeMerged\n");
 		
 		for( Float f : keys)
 		{
 			Holder h = map.get(f);
 			
 			cumulativeNCBI += ((double)h.numNCBI) / sumNCBI;
 			cumulativeHMP+= ((double)h.numHMP) / sumHMP;
 			cumulativeMerged+= ((double)h.numMerged) / sumMerged;
 		
 			writer.write( f + "\t" + cumulativeNCBI + "\t" + cumulativeHMP + "\t" + cumulativeMerged + "\n");
 		}
 		
 		writer.flush();  writer.close();
 	}
 	
 	private static void addToMap(int x, HashMap<Float, Holder> map) throws Exception
 	{
 		System.out.println("Trying " + x);
 		int numDone=0;
 		BufferedReader hmpReader = 
 				new BufferedReader(new InputStreamReader( 
 						new GZIPInputStream( new FileInputStream((new File(ConfigReader.getBigBlastDir() + 
 				File.separator + "SRR061115.fasta_FILE_"+ x +"_TO_HMP.txt.gz"))))));
 		
 		BufferedReader ncbiReader = new BufferedReader(new InputStreamReader( 
 				new GZIPInputStream( new FileInputStream((new File(ConfigReader.getBigBlastDir()+ 
				File.separator + "SRR061115.fasta_FILE_" + x + "_TO_NCBI.txt.gz"))))));
 		
 		while(true)
 		{
 			QueryToBitScore hmpScore = walkToNextLine(hmpReader);
 			QueryToBitScore ncbiScore = walkToNextLine(ncbiReader);
 			
 			if( hmpScore== null )
 			{
 				if( ncbiScore != null)
 					throw new Exception("Mismatched");
 
 				hmpReader.close();
 				ncbiReader.close();
 				return;
 			}
 			
 			Holder h = getOrAdd(map, hmpScore.bitScore);
 			h.numHMP++;
 			
 			h = getOrAdd(map, ncbiScore.bitScore);
 			h.numNCBI++;
 			
 			h = getOrAdd(map, Math.max(ncbiScore.bitScore,hmpScore.bitScore));
 			h.numMerged++;
 			
 			numDone++;
 			
 			if(numDone % 100000 == 0 )
 				System.out.println(numDone);
 		}
 	}
 	
 	private static Holder getOrAdd(HashMap<Float,Holder> map, float key)
 	{
 		Holder h = map.get(key);
 		
 		if( h == null)
 		{
 			h = new Holder();
 			map.put(key,h);
 		}
 		
 		return h;
 	}
 	
 	private static class QueryToBitScore
 	{
 		String query;
 		float bitScore;
 	}
 	
 	private static QueryToBitScore walkToNextLine(BufferedReader reader) throws Exception
 	{
 		String s= reader.readLine();
 		QueryToBitScore queryToScore = null;
 		
 		while( s != null)
 		{
 			s = s.trim();
 			if(s.startsWith("# Query"))
 			{
 				StringTokenizer sToken = new StringTokenizer(s);
 				sToken.nextToken(); sToken.nextToken();
 				queryToScore = new QueryToBitScore();
 				queryToScore.query = sToken.nextToken();
 				
 				if(sToken.hasMoreTokens())
 					throw new Exception("Unexpected token " + sToken.nextToken());
 			}
 			else if( s.endsWith("0 hits found"))
 			{
 				queryToScore.bitScore =0;
 				return queryToScore;
 			}
 			else if( s.endsWith("hits found"))
 			{
 				s = reader.readLine();
 				StringTokenizer sToken = new StringTokenizer(s);
 				
 				if( ! sToken.nextToken().equals(queryToScore.query))
 					throw new Exception("Expecting query " + queryToScore.query + " " +s);
 				
 				for( int x=0; x < 10; x++)
 					sToken.nextToken();
 				
 				queryToScore.bitScore = Float.parseFloat(sToken.nextToken());
 				
 				if( sToken.hasMoreTokens())
 					throw new Exception("Unexpected token " + sToken.nextToken());
 				
 				return queryToScore;
 			}
 			
 			s = reader.readLine();
 		}
 		
 		return queryToScore;
 	}
 }
