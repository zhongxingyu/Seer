 package eu.play_project.dcep.distributedetalis.configurations.helpers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
 
 /**
  * Load prolog code form files in class path and add it to engine.
  * @author Stefan Obermeier
  *
  */
 public class LoadPrologCode implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 100L;
 
 	public LoadPrologCode(){}
 	
 	public void loadCode(String fliename, PlayJplEngineWrapper engine) throws IOException{
 		List<String> methods= getPrologMethods(fliename);
 		
 		//Add methods to engine.
 		for (String code : methods) {
 			engine.execute("assert((" + code + "))");
			System.out.println(code);
 		}
 	}
 	
 	// Get code from file and seperate different methods.
 	private  List<String> getPrologMethods(String methodFile) throws IOException{
 		List<String> result = new LinkedList<String>();
 
 			InputStream is = this.getClass().getClassLoader().getResourceAsStream(methodFile);
 			BufferedReader br =new BufferedReader(new InputStreamReader(is));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			
 			while (null != (line = br.readLine())) {
 				if (!(line.equals(" "))) {
 					if (!line.startsWith("%")) { // Ignore comments
 						sb.append(line.split("%")[0]); //Ignore rest of the line if comment starts.
 					}
 				}
 			}
 
 			br.close();
 			is.close();
 			
 			String[] methods = sb.toString().split(Pattern.quote( "." ) ); // Dot defines a new method.
 			for (String m : methods) {
 				if(!m.trim().equals("")){ // Do not add empty lines.
 					result.add(m);
 				}
 			}
 			return result;
 	}
 
 
 }
