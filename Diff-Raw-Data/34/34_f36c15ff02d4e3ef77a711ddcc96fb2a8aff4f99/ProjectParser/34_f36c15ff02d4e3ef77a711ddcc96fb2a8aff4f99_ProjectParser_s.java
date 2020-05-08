 package styling.parser;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 
 import styling.entities.ClassResult;
 import styling.entities.ProjectResult;
 
 public abstract class ProjectParser {
 
 	public static ProjectResult parse(String pathToProject) {
 		
 		File directory = new File(pathToProject);
 		String[] extensions = { "java" };
 		
 		Collection<File> files = FileUtils.listFiles(directory, extensions, true);
 		List<ClassResult> classResults = new LinkedList<ClassResult>();
 		
 		for(File file : files) {
 			try {
				classResults.add(ClassParser.Parse(file));
 			} catch (Exception e) {System.out.println("Error");}
 		}
 		
 		return new ProjectResult(classResults);
 	}	
 }
