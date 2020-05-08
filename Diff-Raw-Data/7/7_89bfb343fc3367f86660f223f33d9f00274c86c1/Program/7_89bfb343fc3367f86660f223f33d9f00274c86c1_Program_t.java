 package yapl;
 /**** YAPL Compiler
 *
 *	Copyright 2010 Daniel Hlbling, Klagenfurt University
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *       
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 ***/
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 
 import yapl.impl.BackendMJ;
 import yapl.interfaces.CompilerError;
 import yapl.lib.CompilerMessage;
 import yapl.lib.YAPLException;
 
 public class Program {
 	public static final String PREDEFINED_PROCEDURES = 
 			"Procedure writeint(i: Integer);" +
 			"Procedure writebool(b: Boolean);" +
 			"Procedure writeln();" +
 			"Procedure readint(): Integer;";
 
 	private static yapl parser = new yapl(new StringReader(PREDEFINED_PROCEDURES));
 	public static void main(String[] args) {
		if (args.length < 1) {
 			writeUsage();
 			return;
 		}
 		String programName = args[0];
		String outputName = args[1];
 		try {
 		    FileInputStream fis = new FileInputStream(programName);
 		    BackendMJ backend = new BackendMJ();
 			parser.init(backend);
 		    parser.ReInit(new StringReader(PREDEFINED_PROCEDURES));
 		    parser.PredefinedProcedures();
 		    parser.ReInit(fis);
 		    
 		    parser.Start();
 
 		    CompilerMessage.printOK(programName);
		    backend.writeObjectFile(new FileOutputStream(outputName));
 	    } catch (YAPLException ex) {
 	    	writeError(ex, programName);
 		} catch (ParseException e) {
 			writeError(e, programName);
 		} catch (TokenMgrError e){
 			writeError(e, programName);
 		}
 		catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	private static void writeError(CompilerError error, String programName){
 		CompilerMessage.printError(error, programName);
 	}
 
 	private static void writeUsage() {
 		System.out.println("YAPL-Compiler: No Input file specified.\n" +
 		  "Usage: compilerbau <inputfile.yapl >");
 	}
 }
