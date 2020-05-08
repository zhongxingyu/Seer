 package styling.parser;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 
 import styling.entities.ClassResult;
 import styling.entities.MethodResult;
 
 public abstract class ClassParser {
 
	public static ClassResult parse(File file) throws IOException {
 
 		List<String> lineList = FileUtils.readLines(file);
 		int numberOfImports = 0;
 		int numberOfLines = 0;
 		int numberOfAttributes = 0;
 		String className = "";
 		
 		boolean insideClass = false;
 		
		Pattern methodPattern = Pattern.compile("(public|protected|private) (static )?\\w+(<\\w+>)? \\w+\\(.*\\)");
		Pattern attributePattern = Pattern.compile("(public|protected|private) \\w+(<\\w+>)? \\w+");
 		
 		List<MethodResult> methodResultList = new LinkedList<MethodResult>();
 		
 		for (String line : lineList) {
 			
 			if (line.startsWith("import")) {
 				numberOfImports++;
 			}
 			
 			if (insideClass) {
 				numberOfLines++;
 			}
 			
 			if (line.startsWith("public") && line.contains("class") && className.isEmpty()) {
 				insideClass = true;
 				String[] resultado = line.split(" ");
 				
 				className = resultado[resultado.length - 2];
				continue;
 			}
 			
 			boolean isMethod = methodPattern.matcher(line).find();
 			
 			if (isMethod) {
 				methodResultList.add(new MethodResult("", 0));
 			}
 			
 			if (!isMethod && attributePattern.matcher(line).find()) {
 				numberOfAttributes++;
 			}
 		}

 		return new ClassResult(className, numberOfLines - 1, numberOfImports, numberOfAttributes, methodResultList);
 	}
 }
