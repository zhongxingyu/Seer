 package confdb.gui;
 
 import java.util.ArrayList;
 
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import confdb.data.Configuration;
 import confdb.data.Template;
 import confdb.data.EDSourceTemplate;
 import confdb.data.EDSourceInstance;
 import confdb.data.Parameter;
 import confdb.data.VStringParameter;
 import confdb.data.DataException;
 
 import confdb.db.CfgDatabase;
 
 import confdb.converter.*;
 
 
 /**
  * ConverterService
  * ----------------
  * @author Philipp Schieferdecker
  *
  * Create text representations of the current configuration in various
  * formats, taking the parameters entered in the ConfigurationPanel
  * into account.
  */
 public class ConverterService
 {
     //
     // member data
     //
     
     /** reference to the db interface */
     private CfgDatabase database = null;
 
     /** format to convert to */
     private String format = "ASCII";
     
     /** input file(s) for PoolSource */
     private String input = "";
     
 
     //
     // construction
     //
 
     /** standard constructor */
     public ConverterService(CfgDatabase database)
     {
 	this.database = database;
     }
 
     
     //
     // member functions
     //
     
     /** set the format */
     public boolean setFormat(String format)
     {
 	if (format.toLowerCase().equals("ascii")||
 	    format.toLowerCase().equals("python")||
 	    format.toLowerCase().equals("html")) {
 	    this.format = format.toLowerCase();
 	    return true;
 	}
 	return false;
     }
     
     /** set input: either a root file, or a text file with a list of root files */
     public void setInput(String input) { this.input = input; }
     
     /** convert configuration */
     public String convertConfiguration(Configuration config)
     {
 	String result = new String();
 	try {
 	    ConverterFactory converterFactory=ConverterFactory.getFactory("default");
 	    Converter        converter       =converterFactory.getConverter(format);
 	    
 	    if (input.length()>0) {
 		EDSourceInstance poolSource = getPoolSource(config.releaseTag());
 		converter.overrideEDSource(poolSource);
 	    }
 
 	    result = converter.convert(config);
 	}
 	catch (Exception e) {
 	    String msg = "FAILED to convert configuration: " + e.getMessage();
 	    System.out.println(msg);
 	}
 	return result;
     }
     
     /** make a PoolSource */
     public EDSourceInstance getPoolSource(String releaseTag)
     {
 	// set 'fileNames' parameter
 	ArrayList<String> fileNames = new ArrayList<String>();
 	if (input.endsWith(".root")) {
	    fileNames.add(input);
 	}
 	else {
 	    BufferedReader inputStream = null;
 	    try {
 		inputStream=new BufferedReader(new FileReader(input));
 		String fileName;
 		while ((fileName = inputStream.readLine()) != null) {
		    if (fileName.endsWith(".root")) fileNames.add(fileName);
 		    else System.out.println("ERROR parsing filelist '"+input+"'.");
 		}
 	    }
 	    catch (IOException e) {
 		System.out.println("Error parsing filelist '"+input+"':"
 				   +e.getMessage());
 	    }
 	    finally {
 		if (inputStream != null) 
 		    try { inputStream.close(); } catch (IOException e) {}
 	    }
 	}
 	
 	if (fileNames.isEmpty()) {
 	    System.out.println("ERROR: Failed to parse any fileNames.");
 	    return null;
 	}
 
 	Parameter pFileNames=new VStringParameter("fileNames",fileNames,false,false);
 
 	// create PoolSource instance
 	EDSourceTemplate template =
 	    database.loadEDSourceTemplate(releaseTag,"PoolSource");
 	EDSourceInstance poolSource = null;
 	try {
 	    poolSource = (EDSourceInstance)template.instance();
 	    Parameter p = poolSource.parameter("fileNames");
 	    int i = poolSource.indexOfParameter(p);
 	    poolSource.updateParameter(i,pFileNames.valueAsString());
 	}
 	catch (DataException e) {
 	    System.out.println("FAILED to create instance of PoolSource:"+
 		 	       e.getMessage());
 	}
 	return poolSource;
     }
 }
