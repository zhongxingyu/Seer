 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 
 public class Utils {
 
 	public static<E> HashMap<String,E> atom2ObjectMapE(Iterable<E> iterable){
 		E e = null;
 		
 		HashMap<String, E> e_Map = new HashMap<String, E>();
 		Iterator<E> it = iterable.iterator();
 		while(it.hasNext()){
 			e = it.next();
 			e_Map.put(e.toString(), e);
 		}
 		return e_Map;
 	}
 	
 	
 	
 	/**
 	 * Returns an object from a Iterable object that has the same name. 
 	 * The iterable argument must be of Iterable Class, and the name a String
 	 * @param  		iterable  object
 	 * @param		String name
 	 * @return      Object that has the same name
 	 * @see         getEFromIterable
 	 */
 	public static <E> E getEFromIterable(Iterable<E> iterable, String name)
 	{
 		E res = null;
 		E i = null;
 		boolean found = false;
 		Iterator<E> it = iterable.iterator();
 		while(it.hasNext() && !found)
 		{
 			i=it.next();
 			if(i.toString().equals(name))
 				found = true;
 		}
 		if(found)
 			res = i;
 		return res;
 	}
 	
 public static String diffPosPre(String pathindex){
 		
 		String line=null;
 		
 		StringBuilder lines = null;
 		
 		try{
 			
 			String newpath = "output/"+pathindex;
 			 
 			File path = new File(newpath);
 			
 			ProcessBuilder pb;
 				
 				ArrayList<String> cmds = new ArrayList<String>();
 				cmds.add("diff");
 				cmds.add("-r");
 				cmds.add("--exclude=index");
 				cmds.add("pre");
 				cmds.add("pos");
 				
 				pb = new ProcessBuilder(cmds);
 			
 			pb.directory(path);	
 			
 			Process pr = pb.start();
 			
 			OutputStream out = pr.getOutputStream();
 			InputStream in = pr.getInputStream();
 			InputStream err = pr.getErrorStream();
 
 			InputStreamReader isr = new InputStreamReader(in);
 			OutputStreamWriter osr = new OutputStreamWriter(out);
 			
 			
 			BufferedReader br = new BufferedReader(isr);
 			BufferedWriter bw = new BufferedWriter(osr);
 			
 			bw.flush();
 			bw.close();
 		
 			
 			line = br.readLine();
 			
 			if(line != null) lines = new StringBuilder();
 			
 			while(line != null){
 				lines.append(line+"\n");
 				
 				line = br.readLine();
 				}
 			
 			System.out.println(lines);
 			
 			if(lines != null){
 				
 				BufferedWriter writer = null;
 				
 				try
 				{
 					writer = new BufferedWriter( new FileWriter(newpath +"/diff.txt"));
 					writer.write(lines.toString());
 
 				}
 				catch (IOException e){}
 				
 				finally
 				{
 				try
 					{
 						if ( writer != null)
 							writer.close( );
 					}
 					catch ( IOException e){}
 			     }
 				}
 			
 			br.close();
 			
			pr.waitFor();
 		
 		
 		}catch(Exception exc){
 			exc.printStackTrace();
 		}
 		
 	
 		
 		return line;
 		
 	}
 
 public static void diffIndex(String pathindex){
 	printindex(pathindex+"/pre");
 	printindex(pathindex+"/pos");
 }
 
 public static String printindex(String pathindex){
 	
 	String line=null;
 	
 	StringBuilder lines = null;
 	
 	try{
 		
 		String newpath = "output/"+pathindex;
 		 
 		File path = new File(newpath);
 		
 		ProcessBuilder pb;
 			
 			ArrayList<String> cmds = new ArrayList<String>();
 			cmds.add("git");
 			cmds.add("ls-files");
 			cmds.add("--stage");
 			
 			pb = new ProcessBuilder(cmds);
 		
 		pb.directory(path);	
 		
 		Process pr = pb.start();
 		
 		OutputStream out = pr.getOutputStream();
 		InputStream in = pr.getInputStream();
 		InputStream err = pr.getErrorStream();
 
 		InputStreamReader isr = new InputStreamReader(in);
 		OutputStreamWriter osr = new OutputStreamWriter(out);
 		
 		
 		BufferedReader br = new BufferedReader(isr);
 		BufferedWriter bw = new BufferedWriter(osr);
 		
 		bw.flush();
 		bw.close();
 	
 		
 		line = br.readLine();
 		
 		if(line != null) lines = new StringBuilder();
 		
 		while(line != null){
 			lines.append(line+"\n");
 			
 			line = br.readLine();
 			}
 		
 		System.out.println(lines);
 		
 		if(lines != null){
 			
 			BufferedWriter writer = null;
 			
 			try
 			{
 				writer = new BufferedWriter( new FileWriter(newpath +"/diff_index.txt"));
 				writer.write(lines.toString());
 
 			}
 			catch (IOException e){}
 			
 			finally
 			{
 			try
 				{
 					if ( writer != null)
 						writer.close( );
 				}
 				catch ( IOException e){}
 		     }
 			}
 		
 		br.close();
 		
		pr.waitFor();
 	
 	
 	}catch(Exception exc){
 		exc.printStackTrace();
 	}
 	
 
 	
 	return line;
 	
 }
 }
