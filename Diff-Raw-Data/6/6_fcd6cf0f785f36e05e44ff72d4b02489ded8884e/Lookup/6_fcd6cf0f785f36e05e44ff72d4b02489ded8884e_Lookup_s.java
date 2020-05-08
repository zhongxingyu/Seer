 package a2;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class Lookup {
 	public static void main(String args[]) throws IOException{
 		Map<Integer,Integer> vals = get();
 		System.out.println(vals.get(24));
 	}
 	
 	public static HashMap<Integer,Integer> get(){
 		HashMap<Integer,Integer> ret = new HashMap<Integer,Integer>();
 		CSVReader reader = null;
 		try{
		reader = new CSVReader(new InputStreamReader(new Lookup().getClass().getClassLoader()
 		        .getResourceAsStream("resources/lookup.csv")));
 	    String [] nextLine;
 	    int i = 0;
 	
 	    
 	    while ((nextLine = reader.readNext()) != null) {
 	    	i++;
 
 	    	String id = nextLine[0];
 	        String count = nextLine[1];
 	        	ret.put(Integer.parseInt(id),Integer.parseInt(count));
 	    }
 		}catch(IOException e){
 			e.printStackTrace(System.err);
 		}finally{
 			IOUtils.closeQuietly(reader);
 		}
 		return ret;
 	}
 }
