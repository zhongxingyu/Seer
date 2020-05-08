 package io.seqware.queryengine.sandbox.testing;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.broadinstitute.variant.variantcontext.VariantContext;
 import org.json.JSONException;
 import org.junit.Test;
 
 import io.seqware.queryengine.sandbox.testing.plugins.*;
 import io.seqware.queryengine.sandbox.testing.ReturnValue;
 import io.seqware.queryengine.sandbox.testing.model.VCFReader;
 
 public class FeaturePluginRunnerTest {
 	@Test
 	//This is basically and external implementation of the runPlugin function in BackendTestInterface, for ease of testing.
 	//Will be added into GATK_Picard_Backend test.
 	public ReturnValue runPlugin() throws IOException, JSONException{
 		FeaturePluginRunner runMe = new FeaturePluginRunner();
 		ReturnValue rv = new ReturnValue();
 		VCFReader vcfReader;
 		Iterator<VariantContext> vIter;
 		
 		//Move these to some external global variable later, will not be called in the runPlugin params
 		String outputDir = System.getProperty("user.home") + "/Filtered";
 		String inputDir = "src/main/resources/PluginData/";
 		
 		File jsonQuery = new File("src/main/resources/testdata/query.json");
 		InputStream is = new FileInputStream(jsonQuery);
 		String jsonTxt = IOUtils.toString(is);
 		
 		/**Filtering commences**/
 		runMe.getFilteredFiles(inputDir, jsonTxt, outputDir);
 		
 		//TODO: run map method from pluginClass for every genome position in every filtered file
 		FeatureCountPlugin countInstance = new FeatureCountPlugin();
 		
		
		File filtered;
		String InputFilePath;
		filtered = new File(outputDir);
		for (File child : filtered.listFiles()){
			InputFilePath = child.getAbsolutePath();
			String featureSetID = child
					.getName()
					.substring(0, child.getName().indexOf("."));
			if (FilenameUtils.getExtension(InputFilePath).equals("vcf")){ 
				vcfReader = new VCFReader(InputFilePath);
				vIter = vcfReader.getVCFIterator();
				while (vIter.hasNext()){
					//Apply the plugin.
				}
				
			}
 		}
 		
 		rv.getKv().put("pluginResultFile", "resultFilePath");
 		return rv;
 	}
 	
 	public class FeatureCountPlugin implements FeaturePluginInterface{
 
 		@Override
 		public void map(long position,
 				Map<FeatureSet, Collection<Feature>> features,
 				Map<String, String> output) {
 			
 			
 		}
 
 		@Override
 		public void reduce(String key, Iterable<String> values,
 				Map<String, String> output) {
 			
 			
 		}
 		
 	}
 	
 	
 }
