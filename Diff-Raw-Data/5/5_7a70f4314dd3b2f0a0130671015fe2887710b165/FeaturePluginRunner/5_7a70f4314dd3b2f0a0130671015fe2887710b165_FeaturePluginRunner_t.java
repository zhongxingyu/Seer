 package io.seqware.queryengine.sandbox.testing.plugins;
 
 import io.seqware.queryengine.sandbox.testing.ReturnValue;
 import io.seqware.queryengine.sandbox.testing.utils.VCFReader;
 import io.seqware.queryengine.sandbox.testing.utils.JSONQueryParser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.broadinstitute.variant.variantcontext.VariantContext;
 import org.json.JSONException;
 
 public class FeaturePluginRunner {
 	//TODO: 1. Filter files
 	//		2. Run plugin on the filtered files
 	  public Map<String,String> fileMap = 
 			  new HashMap<String,String>();
 	public Map<String,String> applyMap(String InputFilePath, Map<FeatureSet, Collection<Feature>> features) throws IOException{
 		VCFReader inputVCF = 
 				new VCFReader(InputFilePath);
 			
 			Map<String,String> output = 
 				new HashMap<String,String>();
 			
 		Iterator<VariantContext> vcfIter = inputVCF.getVCFIterator();
 		String position;
 
 		 
 		
 		while(vcfIter.hasNext()){
 			VariantContext variant = vcfIter.next();
 			position = Integer.toString(variant.getStart());
 			Iterator it = variant.getAttributes().entrySet().iterator();
 			
 			while( it.hasNext()){
 				Map.Entry pair = (Map.Entry)it.next();
 			}
 
 		}
 		
 		
 		return null;
 	}
 
 	//Apply filter as determined by the JSON query
 	public void getFilteredFiles(String queryJSON, String OutputFilePath) throws IOException, JSONException{
 		String InputFilePath;
 		String inputDir; 
 		String[] inputDirs = fileMap.keySet().toArray(new String[fileMap.keySet().size()]);
 		inputDir = inputDirs[0].toString();
 		inputDir = inputDir.substring(0, inputDir.lastIndexOf("/"));
 		
 		File filedir = 
 				new File(inputDir);
 		
 		Feature FeatureID = 
 				new Feature();
 		
 		File makefile = new File(OutputFilePath);
 		
 		
 		if (!makefile.exists()){
 			boolean success = makefile.mkdirs();
 		} else if (makefile.exists()){
 			FileUtils.cleanDirectory(makefile);
 		}
 		
 		JSONQueryParser JParse = new JSONQueryParser(queryJSON);
 		HashMap<String, String> fsmapq = JParse.getFeatureSetQuery();
 
 		//Generate Complete Map of FeatureSetId and INFO
 		for (File child : filedir.listFiles()){
 			InputFilePath = child.getAbsolutePath();
 			String filename = child
 					.getName()
 					.substring(0, child.getName().indexOf("."));
 			if (FilenameUtils.getExtension(InputFilePath).equals("vcf")
 					&& ((fsmapq.keySet().contains(filename)) || (fsmapq.size() ==0))){ 
 				//Write this to temp file output
 				FeatureID.readVCFinfo(InputFilePath, queryJSON, OutputFilePath, filename); //Read INFO fields in VCF only
 
 			}
 			
 		}
 	}
 	
 	public ReturnValue runPlugin(String queryJSON, Class pluginClass){
 		
 		return null;
 	}
 	
 	//TODO Create function to go through input vcf file and implement map function
 	public Map<FeatureSet, Collection<Feature>> makeMapInput(String Directory) throws IOException{
 		File filedir = 
 				new File(Directory);
 		
 		Map<FeatureSet,Collection<Feature>> MapInput = 
 				new HashMap<FeatureSet,Collection<Feature>>();
 		
 		ArrayList<Feature> Features = 
 				new ArrayList<Feature>();
 		
 		String line;
 		for (File child : filedir.listFiles()){
 			String AbsolutePath = child.getAbsolutePath();
 			if (FilenameUtils.getExtension(AbsolutePath).equals("txt")){
 				FeatureSet featureset = 
 						new FeatureSet(child);
 				
 				BufferedReader in = 
 						new BufferedReader(
 								new FileReader(AbsolutePath));
 				
 				while((line = in.readLine()) != null){
 					Feature feature = 
 							new Feature(line);
 						Features.add(feature);
 				}
 				MapInput.put(featureset, Features);	
 			}
 		}
 		return MapInput;
 	}
 	
 	public class SimpleReadsCountPlugin extends AbstractPlugin<Reads, ReadSet> implements ReadPluginInterface{
     }
     
     public class SimpleFeaturesCountPlugin extends AbstractPlugin<Feature, FeatureSet> implements FeaturePluginInterface{
     }
     
     public abstract class AbstractPlugin <UNIT, SET>{
         public final String count = "COUNT";
         
         public void map(long position, Map<SET, Collection<UNIT>> reads, Map<String, String> output) {
             if (!output.containsKey(count)){
                 output.put(count, String.valueOf(0));
             }
             for(Collection<UNIT> readCollection  :reads.values()){
                 Integer currentCount = Integer.valueOf(output.get(count));
                 int nextCount = currentCount += readCollection.size();
                 output.put(count, String.valueOf(nextCount));
             }
         }
 
         public void reduce(String key, Iterable<String> values, Map<String, String> output) {
                 Integer currentCount = Integer.valueOf(output.get(count));
                 for(String value : values){
                     currentCount = currentCount += 1;
                 }
                 output.put(count, String.valueOf(currentCount));
         }
     }
 }
