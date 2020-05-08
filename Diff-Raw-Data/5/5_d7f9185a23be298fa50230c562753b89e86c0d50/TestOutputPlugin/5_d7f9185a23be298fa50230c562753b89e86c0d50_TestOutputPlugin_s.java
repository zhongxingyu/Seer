 /*
  * Copyright (C) 2012 SeqWare
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.seqware.queryengine.plugins.contribs;
 
 import com.github.seqware.queryengine.model.Feature;
 import com.github.seqware.queryengine.model.FeatureSet;
 import com.github.seqware.queryengine.plugins.runners.MapperInterface;
 import com.github.seqware.queryengine.plugins.runners.ReducerInterface;
 import com.github.seqware.queryengine.plugins.recipes.FilteredFileOutputPlugin;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.hadoop.io.Text;
 
 /**
  * This plug-in implements a quick and dirty export using Map/Reduce
  *
  *
  * @author bso
  * @version $Id: $Id
  */
 public class TestOutputPlugin extends FilteredFileOutputPlugin{
    	private Text text = new Text();
     private Text textKey = new Text();
     Set<Feature> featuresAtCurrentLocation = new HashSet<Feature>();
     @Override
 	public void map(long position, Map<FeatureSet, Collection<Feature>> atoms, MapperInterface<Text, Text> mapperInterface) {
     	System.out.println("Mapping.........");
 		for (FeatureSet fs : atoms.keySet()){
 			for (Feature f : atoms.get(fs)){
 				if (f.getStart() == position){
 					System.out.println("Feature at valid position: " +  f.getDisplayName());
 					featuresAtCurrentLocation.add(f);
 				} else {
 					System.out.println ("f.getStart: " + f.getStart() + ", " + "position:" + position);
 				}
 			}
 		}
 		
 		for (FeatureSet fs : atoms.keySet()){
 			System.out.println(fs.getReference().getDisplayName());
 			for (Feature f : atoms.get(fs)){
 				System.out.println(f.getStart());
 				System.out.println("Size of added features...: "+  featuresAtCurrentLocation.size());
 				for (Feature positionFeature : featuresAtCurrentLocation){
 					System.out.println("In the loop.. getting start pos: " +positionFeature.getStart());
 					String indelRange = convertToIndelRange(positionFeature.getStart(), positionFeature.getStop());
 					System.out.println("indelRange..: " + indelRange);
 					String indelStart = convertLongToString(positionFeature.getStart());
 					System.out.println("indelStart..: " + indelStart);
 					text.set(indelRange);
 					System.out.println("ran text.set.. do you see me?");
 					textKey.set(indelStart);
 					System.out.println("ran textKey.set.. do you see me?");
 					System.out.println("Running mapperInterface");
 					mapperInterface.write(textKey, text);
 					System.out.println(indelRange);
 				}
 			}
 		}
 		
 	}
 	@Override
 	public void reduce(Text reduceKey, Iterable<Text> reduceValues, ReducerInterface<Text, Text> reducerInterface) {
 		System.out.println("Reducing.......");
 		Set<Text> seenSet = new HashSet<Text>();
 		String newFeatStr = "";
 		for (Text val : reduceValues){
 			System.out.println(val.toString());
 			seenSet.add(val);
 			String[] fsArr = val.toString().split(",");
 			for (String curr : fsArr){
				newFeatStr += curr + ", ";
 			}
 			System.out.println("REDUCED: "+ newFeatStr);
 		}
		text.set(reduceKey.toString() + "\t" + newFeatStr);
 		System.out.println("Running reducerInterface");
 		reducerInterface.write(text,null);
 		
 	}
 	
 	public static String convertToIndelRange(long start, long stop){
 		String startPos = String.valueOf(start);
 		String endPos = String.valueOf(stop);
 //		String converted = startPos.substring(0, startPos.indexOf(".")) + 
 //				endPos.substring(0, endPos.indexOf("."));
 		System.out.println("Converted IndelRange..");
 		return(startPos + "-" + endPos);
 	}
 	
 	public static String convertLongToString(long start){
 		String startPos = String.valueOf(start);
 //		return(startPos.substring(0, startPos.indexOf(".")));
 		return (startPos);
 	}
 }
