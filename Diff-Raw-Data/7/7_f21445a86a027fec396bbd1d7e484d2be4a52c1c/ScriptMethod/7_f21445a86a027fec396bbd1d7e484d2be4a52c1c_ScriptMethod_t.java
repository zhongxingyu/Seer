 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2012 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the Software), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.method;
 
 import org.jruby.embed.ScriptingContainer;
 
 
 /**
  * A method that runs a ruby script using jruby, which can be used to script
  * custom analysis methods at runtime.
  * <p>
  * The method gets the name of the script file from the parameter dictionary, and sets
  * the local variables "parameters", "imageset" and "method" to be the parameter dictionary
  * in use for the analysis, the ImageSet object containing the images being analyzed, and
  * this method object.
  * <p>
  * 
  * @author Colin J. Fuller
  */
 
 public class ScriptMethod extends Method {
 	
 	public static final String SCRIPT_FILENAME_PARAM = edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataRubyParser.SCRIPT_FILENAME_PARAM;
 	
 	final static String SCRIPT_FUNCTIONS_FILE = "edu/stanford/cfuller/imageanalysistools/resources/script_methods.rb";
 	
	final static String PARAM_METHOD_DISPLAY_NAME = "method_display_name";
	
 	public void go() {
 		
 		String scriptFilename = this.parameters.getValueForKey(SCRIPT_FILENAME_PARAM);
 		
 		ScriptingContainer sc = new ScriptingContainer(org.jruby.embed.LocalContextScope.SINGLETHREAD, org.jruby.embed.LocalVariableBehavior.PERSISTENT);
 		
 		sc.setClassLoader(ij.IJ.getClassLoader());
 		
 		sc.put("parameters", this.parameters);
 		sc.put("imageset", this.imageSet);
 		sc.put("method", this);
 				
 		sc.setCompatVersion(org.jruby.CompatVersion.RUBY1_9);
 		
 		sc.runScriptlet(this.getClass().getClassLoader().getResourceAsStream(SCRIPT_FUNCTIONS_FILE), SCRIPT_FUNCTIONS_FILE);
 		sc.runScriptlet(org.jruby.embed.PathType.ABSOLUTE, scriptFilename);
 		
		if (this.parameters.hasKey(PARAM_METHOD_DISPLAY_NAME)) {
			this.setDisplayName(this.parameters.getValueForKey(PARAM_METHOD_DISPLAY_NAME));
		}
		
 		
 		
 	}
 	
 }
 
