 import java.io.*;
 import java.util.ArrayList;
import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 
 public class Preprocesser {
 
 	public Preprocesser(){
 
 	}
 	/**
 	 * Makes a copy of the file
 	 * @arg takes in a String with the filepath 
 	 */
 	public File makeCopy(String filePath) throws IOException
 	{
 		File original = new File(filePath);
 		File copy = new File("cyborg_cpy");
 		File targetDir = new File(System.getProperty("java.io.tmpdir"));
 
 		System.out.println("Copying " + original);
 		FileUtils.copyFile(original, copy);
 		FileUtils.copyFileToDirectory(copy, targetDir);
 
 		return copy;
 	}
 
 	/**
 	 * grabs the text from a file and puts it into a string 
 	 * @args takes in a file to copy the text from
 	 */
 	public String getCode(File cyFile) throws IOException
 	{
 		FileReader fr = new FileReader(cyFile);
 		StringBuffer fileData = new StringBuffer(1000);
 		BufferedReader reader = new BufferedReader(fr);
 		char[] buf = new char[1024];
 		int numRead=0;
 		while((numRead=reader.read(buf))!=-1)
 		{
 			fileData.append(buf, 0, numRead);
 		}
 		reader.close();
 		return fileData.toString();
 	}
 
 	
 
 	/**
 	 * Finds the id in scope and the id. 
 	 * Prepends the scope id to the id.
 	 * @args takes a String with the code to be processed 
 	 */
 	public String prependScope(String code)
 	{
 		Pattern pattern = Pattern.compile("<.*?[\n?].*?scope=.*?[\n?]*?>");
 		Matcher matcher = pattern.matcher(code);
 		String finalCode=code;
 
 		while (matcher.find())
 		{
 			//barg, regex could be more robust
 			Pattern scopePattern = Pattern.compile("scope=\".*?\"");
 			Pattern idPattern = Pattern.compile("id=\".*?\"");
 
 			Matcher scopeMatcher;
 			Matcher idMatcher;
 
 			scopeMatcher = scopePattern.matcher(matcher.group());
 			scopeMatcher.find();
 			String scopeID=scopeMatcher.group();
 			String tempCode = finalCode.replaceAll(scopeID, "");
 			finalCode=tempCode;
 			scopeID=scopeID.substring(7, scopeID.length()-1);
 			System.out.println(scopeID);
 			idMatcher = idPattern.matcher(matcher.group());
 			idMatcher.find();
 			String id= idMatcher.group();
 			String idString = id.substring(4, id.length()-1);
 			String newId = "id=\""+scopeID+"_"+idString+"\"";
 			System.out.println(newId);
 			tempCode = finalCode.replaceAll(id, newId);
 			finalCode=tempCode;
 		}		
 		System.out.println("FINAL CODE");
 		System.out.println(finalCode);
 		return finalCode;
 	}
 
	public String seperateVarInit(String code)
 	{
 		//find the string between the <variables> </variables> tag
 		Pattern varTags = Pattern.compile("<event.*>.*?</event>");
 		Matcher matcher = varTags.matcher(code);
 		if (matcher.find())
 		{
 			
 			//lop off the variables tags
 			String insideTags = matcher.group();
 			insideTags=insideTags.substring(11, insideTags.length()-12);
 			String[] vars=insideTags.split(";");
 			String preVar="";
 			String separatedVar = "";
 
 			for (int i=0;i<vars.length;i++)
 			{
 				String[] varParts = vars[i].split("[ =]+");
 				separatedVar=separatedVar+varParts[0]+varParts[1]+";"+varParts[1]+"="+varParts[2]+";";
 			}
 			code=code.replaceAll(matcher.group(), separatedVar);
 			
 		}
 		return code;
 	}
 	
 	public String addIncludes(String code) throws IOException
 	{
 		String withIncludes="";
 		Pattern includeTags = Pattern.compile("[^>][ \t\n\r]*?<includes>.*?</includes>");
 		Matcher matcher = includeTags.matcher(code);
 		if (matcher.find())
 		{
 			String includes="";
 			String [] noOpenTag = matcher.group().split("<includes>");
 			String noTags = noOpenTag[1].substring(0, noOpenTag[1].length()-12);
 			String[] includedFiles = noTags.split(";");
 			for (int i=0;i<includedFiles.length;i++)
 			{
 				String nextFile=includedFiles[i].trim();
 				File file = new File(nextFile);
 				String fileText = getCode(file);
 				fileText=fileText.replaceAll("\n", "  ");
 				includes=includes+fileText;
 			}
 			code=code.replace(matcher.group(), includes);
 			matcher = includeTags.matcher(code);
 			if (!matcher.find())
 			{
 				return code;
 			}else
 			{
 				addIncludes(code);
 			}
 		}
 		return code;
 	}
 	public void doVariableInitializations(String cyborgFile) {
 
 		/* don't know why, but somehow the variables tag needed the tabs regex */
 		String var = "[ \t]*?<variables>[ \t]*?";
 		String var2 = "</variables>";
 		Pattern startVar = Pattern.compile(var);
 		Pattern endVar = Pattern.compile(var2);
 
 		/* regular expression for "datatype identifier = value;" */
 		Pattern id1 = Pattern.compile("[a-zA-Z$_][a-zA-Z0-9$_.]*[ \t\f\r]*[a-zA-Z$_][a-zA-Z0-9$_.]*"
 				+ "[ \t\f\r]*=[ \t\f\r]*\"?[a-zA-Z0-9$_.]*\"?;");
 		try {
 			FileReader fr = new FileReader(cyborgFile);
 			BufferedReader br = new BufferedReader(fr);
 			String line = null;
 			StringBuffer program = new StringBuffer();
 			while((line = br.readLine()) != null) {
 				Matcher varStart = startVar.matcher(line);
 				if(varStart.matches()) {
 					program.append("\n<variables>\n ");
 					Matcher varEnd = endVar.matcher(line);
 					Matcher mId = id1.matcher(line);
 
 					/* This loop separates the declaration and initialization of variables
 					 * It will keep looping until it finds the </variables> tag.
 					 */
 					while(!varEnd.find()) {
 						while(mId.find()) {
 
 							/* this transforms "int x = 5;" to "int x; x = 5;" */
 							String[] identifiers = (mId.group()).split("[ =]+");
 							StringBuffer ids = new StringBuffer();
 							ids.append(identifiers[0]);
 							ids.append(" ");
 							ids.append(identifiers[1]);
 							ids.append("; ");
 							ids.append(identifiers[1]);
 							ids.append("=");
 							ids.append(identifiers[2]);
 							ids.append(" ");
 							program.append(ids.toString());
 							program.append(" ");
 						}
 						line = br.readLine();
 						mId = id1.matcher(line);
 						varEnd = endVar.matcher(line);
 					}
 					program.append("\n</variables>");
 				} else {
 					program.append("\n");
 					program.append(line);
 				}
 			}
 			br.close();
 			fr.close();
 			PrintWriter pr = new PrintWriter(cyborgFile);
 			pr.print(program.toString());
 			pr.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 
 
 }
