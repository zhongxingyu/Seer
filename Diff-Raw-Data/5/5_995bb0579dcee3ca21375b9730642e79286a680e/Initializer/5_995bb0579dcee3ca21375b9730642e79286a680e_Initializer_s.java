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
 
 package edu.stanford.cfuller.colocalization3d;
 
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParser;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory;
 
 
 import java.util.logging.Handler;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
 * Initializes the parameters needed for the colocalization analysis from a file, and sets up the
 * logging for the analysis.
 * @author Colin J. Fuller
 */
 public class Initializer {
 	
 	/**
 	 * Optional parameters
 	 */
 	
 	static final String LOGFILE_PARAM = "log_to_file";
 	static final String DETAILED_LOG_PARAM = "log_detailed_messages";
 	
 	boolean initialized;
 	ParameterDictionary params;
 	
 	public Initializer() {
 		this.initialized = false;
 		params = null;
 	}
 	
 	/**
 	 * Sets up a parameter dictionary from any appropriate default values.
 	 * @return An initialized {@link ParameterDictionary}
 	 */
 	public ParameterDictionary initializeParameters() {
 
 		if (this.initialized) return this.params;
 
		this.setUpLogHandler();
		
 		this.params = ParameterDictionary.emptyDictionary();
 
 		this.initialized = true;
 
 		return this.params;
 	}
 	
 	/**
 	 * Sets up a parameter dictionary from options specified on the command line.
 	 * @param cmdLineArgs The command line arguments to the program, as passed by the VM to the main function.
 	 * @return a {@link ParameterDictionary} containing the values of parameters initialized based on the arguments
 	 */
 	public ParameterDictionary initializeParameters(String[] cmdLineArgs) {
 
 		if (this.initialized) return this.params;
 
 
 		if (cmdLineArgs != null && cmdLineArgs.length > 0) {
 			
 			AnalysisMetadataParser amp = AnalysisMetadataParserFactory.createParserForFile(cmdLineArgs[0]);
 			
 			this.params = amp.parseFileToParameterDictionary(cmdLineArgs[0]);
 			
 			//this.params = ParameterDictionary.readParametersFromFile(cmdLineArgs[0]);
 		} else {
 			return initializeParameters();
 		}
 		
 		this.setUpLogHandler();
 		
 		this.initialized = true;
 		
 		return this.params;		
 	}
 	
 	private void setUpLogHandler() {
 
 		Handler h = null;
 
 		if (params.hasKey(LOGFILE_PARAM)) {
 			try {
 				h = new FileHandler(params.getValueForKey(LOGFILE_PARAM));
 			} catch (java.io.IOException e) {
 				System.err.println("Unable to log to file: " + params.getValueForKey(LOGFILE_PARAM) + ".  Logging to console.");
 				h = new ConsoleHandler();
 			}
 
 		} else {
 
 			h = new ConsoleHandler();
 		}
 
 		if (params.hasKeyAndTrue(DETAILED_LOG_PARAM)) {
 			h.setLevel(Level.ALL);
 			Logger.getLogger(Colocalization3DMain.LOGGER_NAME).setLevel(Level.ALL);
 		} else {
 			h.setLevel(Level.INFO);
 			Logger.getLogger(Colocalization3DMain.LOGGER_NAME).setLevel(Level.INFO);
 		}
 		
 		Logger lgr = Logger.getLogger(Colocalization3DMain.LOGGER_NAME);
 
 		Handler[] hs = lgr.getHandlers();
 
 		for (Handler hOrig : hs) {
 		    lgr.removeHandler(hOrig);
 		}
 
 		Logger.getLogger(Colocalization3DMain.LOGGER_NAME).addHandler(h);
 		Logger.getLogger(Colocalization3DMain.LOGGER_NAME).setUseParentHandlers(false);
 	}
 	
 }
 
