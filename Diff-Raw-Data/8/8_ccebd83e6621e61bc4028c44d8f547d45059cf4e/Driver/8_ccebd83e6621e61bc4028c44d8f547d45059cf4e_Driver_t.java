 package thesaurus.parser;
 
 import java.io.File;
 import java.util.LinkedList;
 
 public class Driver 
 {
 
 	public static void main(String[] args)
 	{
 		File test = new File("/home/james/GIT/myWorkspace/TP3/data.graphml");
 		System.out.println(test.getAbsolutePath());
 		InternalRepresentation driver = new InternalRepresentation(test);
 		
 		String[] synomyns = new String[3];
 		synomyns[0] = "happy";
 		synomyns[1] = "content";
 		synomyns[2] = "joyful";
 		
 		//driver.addVertex("friday", synomyns);
 		
 		
 	
 		
 		//driver.removeVertex("happy");
 		
		System.out.println(driver.getOneSynomyn(""));
 		
 	}
 }
