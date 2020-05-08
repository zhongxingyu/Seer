 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.frontend;
 
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory;
 
 
 
 
 import java.util.logging.Handler;
 
 /**
  * Master controller of analysis routines.  Sets up critical default parameters if they have not been set up and runs
  * the analysis method.
  * 
  * @author Colin J. Fuller
  */
 
 public class AnalysisController {
 
 
     public static final String DATA_OUTPUT_DIR="output";
     public static final String IMAGE_OUTPUT_DIR="output_mask";
     public static final String PARAMETER_OUTPUT_DIR="parameters";
     public static final String SERIALIZED_DATA_SUFFIX="serialized";
     static final String PARAMETER_EXTENSION=".xml";
 
     /**
      * Run analysis on the local machine.
      * @param params    The {@link ParameterDictionary} specifying the options for this analysis run.
      */
 	public void runLocal(ParameterDictionary params) {
 	
 
 		AnalysisMetadata am = new AnalysisMetadata();
 		am.setInputParameters(params);
 		am.setOutputParameters(new ParameterDictionary(params));
 		addLocalParameters(am.getOutputParameters());
 		LocalAnalysis.run(am);	
 		
 	}
 	
 	public void runLocal(String parametersFilename) {
 		AnalysisMetadata am = loadMetadataFromFile(parametersFilename);
 		addLocalParameters(am.getOutputParameters());
 		LocalAnalysis.run(am);
 	}
 	
 	private void addLocalParameters(ParameterDictionary params) {
 		params.addIfNotSet("temp_dir", System.getProperty("user.dir") + java.io.File.separator + "temp");
 		params.addIfNotSet("image_extension", "");
 		params.addIfNotSet("DEBUG", "false");
 	}
 
 
     public void addAnalysisLoggingHandler(Handler h) {
         LoggingUtilities.addHandler(h);
     }
 
 
 	private AnalysisMetadata loadMetadataFromFile(String parametersFilename) {
 		
 		return AnalysisMetadataParserFactory.createParserForFile(parametersFilename).parseFileToAnalysisMetadata(parametersFilename);
 		
 	}
 
 
 }
