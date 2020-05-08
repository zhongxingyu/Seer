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
 
 
 package eTree;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import utils.ConfigReader;
 
 public class PivotToSpreadheet
 {
 	public static void pivotToSpreasheet( double level, ETree etree, File outFile ) throws Exception
 	{
 	
 		// the outerkey is the nodeID; the innerkey is the sampleid
 		HashMap<String, HashMap<String, Integer>> outerMap = new HashMap<String, HashMap<String,Integer>>();
 		System.out.println(outFile);
 		addToMapRecusrively(level, etree.getTopNode(), outerMap);
 		System.out.println(outerMap);
 		writeResults(outFile, outerMap);
 	}
 	
 	private static class Holder implements Comparable<Holder>
 	{
 		String nodeName;
 		int totalNumOfSequences=0;
 		
 		@Override
 		public int compareTo(Holder o)
 		{
 			return o.totalNumOfSequences - this.totalNumOfSequences;
 		}
 	}
 	
 	private static void writeResults( File outFile, HashMap<String, HashMap<String, Integer>> outerMap ) throws Exception
 	{
 		HashSet<String> samples = new HashSet<String>();
 		List<Holder> nodes = new ArrayList<Holder>();
 		
 		for( String s : outerMap.keySet() )
 		{
 			Holder h= new Holder();
 			h.nodeName = s;
 			samples.addAll(outerMap.get(s).keySet());
 			
 			for( int i : outerMap.get(s).values() )
 				h.totalNumOfSequences += i;
 			
			nodes.add(h);
 		}
 		 
 		List<String> sampleList = new ArrayList<String>(samples);
 		Collections.sort(sampleList);
 		Collections.sort(nodes);
 		
 		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
 		
 		writer.write("samples");
 		
 		for( Holder h : nodes)
 			writer.write("\t" + h.nodeName);
 		
 		writer.write("\n");
 		
 		for( String s : sampleList)
 		{
 			writer.write(s);
 			
 			for(Holder h : nodes)
 			{
 				Integer val = null;
 				
 				HashMap<String, Integer> innerMap = outerMap.get(h.nodeName);
 				
 				if(  innerMap != null )
 					val= innerMap.get(s);
 				
 				if( val == null)
 					writer.write("\t0");
 				else
 					writer.write("\t" + val);
 			}
 			
 			writer.write("\n");
 		}
 		
 		writer.flush();  writer.close();
 	} 
 	
 	private static void addToMapRecusrively(double level, ENode node,HashMap<String, HashMap<String, Integer>> outerMap )
 		throws Exception
 	{
 		for( ENode daughter : node.getDaughters() )
 		{
 			if( daughter.getLevel() == level)
 			{
 				if( outerMap.containsKey(daughter.getNodeName() ))
 						throw new Exception("Logic error");
 				
 				outerMap.put(daughter.getNodeName(), daughter.getProbSequence().getSampleCounts());				
 			}
 
 			addToMapRecusrively(level, daughter, outerMap);
 		}
 			
 		
 	}
 	
 	/*
 	 * Run eTree.RunManySingleThread first..
 	 */
 	public static void main(String[] args) throws Exception
 	{
 		ETree etree = ETree.readAsSerializedObject(ConfigReader.getETreeTestDir() + File.separator + "mel74tree.etree");
 		NumberFormat nf = NumberFormat.getInstance();
 		nf.setMaximumFractionDigits(2);
 		
 		for( int x=1; x < ETree.LEVELS.length; x++)
 		{
 			File outFile =new File( ConfigReader.getETreeTestDir() + File.separator + "level" + nf.format(ETree.LEVELS[x]));
 			
 			pivotToSpreasheet(ETree.LEVELS[x], etree, outFile );
 		}
 	}
 }
