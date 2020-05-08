 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import tcwi.TCWIFile.ErrorFile;
 import tcwi.exception.Exceptions;
 import tcwi.fileHandler.Check;
 import tcwi.tools.Tools;
 import tcwi.xml.Parser;
 
 public class Web_ProjectInitializator {
	private static final String VERSION = "0.2.4.4";
 	private static final String AUTHORS = "EifX & hulllemann";
 	private static ArrayList<ErrorFile> files = new ArrayList<ErrorFile>();
 	private static Check check = new Check();
 	private static Parser parser;
 
 	/**
 	 * Recursive method for get all files in a given path ends with ".c"
 	 * It saves this files into <tt>files</tt>
 	 * @param path
 	 * @return An error-string
 	 */
 	private static String getAllFiles(String path){
 		File file = new File(path);
 		File[] fileList = file.listFiles();
 		try{
 			for(int i=0;i<fileList.length;i++){
 				if(fileList[i].isDirectory()){
 					getAllFiles(fileList[i].getAbsolutePath());
 				}else{
 					if(fileList[i].toString().endsWith(".c")){
 						try{
 							String str = fileList[i].getAbsolutePath().substring(0,fileList[i].getAbsolutePath().length()-2);
 							ErrorFile errFile = Parser.getAllErrors(str+".c.xml");
 							files.add(errFile);
 						}catch (IOException e){
 							return fileList[i].getAbsolutePath();
 						}
 					}
 				}
 			}
 		}catch (Exception e){
 			return path;
 		}
 		return "";
 	}
 	
 	private static String escapeString(String str){
 		str = str.replace("&", "AND");
 		str = str.replace("|", "OR");
 		str = str.replace("<", "GREATER");
 		str = str.replace(">", "LOWER");
 		return str;
 	}
 	
 	/**
 	 * Write the .project.xml-file
 	 * @param projectPath
 	 * @param projectName
 	 * @param projectFullName
 	 * @param projectVersion
 	 * @param projectAuthor
 	 * @param path
 	 * @param projectHasDeltas
 	 * @param projectDeltaMain
 	 */
 	private static void writeProjectFile(String projectPath, String projectName, String projectFullName, String projectVersion, String projectAuthor, String path, String projectHasDeltas, String projectDeltaMain) {
 		try{
 			Calendar c = new GregorianCalendar();
 			String month = Tools.correctCalendarForm(c.get(GregorianCalendar.MONTH)+1);
 			String day = Tools.correctCalendarForm(c.get(GregorianCalendar.DAY_OF_MONTH));
 			String hour = Tools.correctCalendarForm(c.get(GregorianCalendar.HOUR_OF_DAY));
 			String minute = Tools.correctCalendarForm(c.get(GregorianCalendar.MINUTE));
 			String second = Tools.correctCalendarForm(c.get(GregorianCalendar.SECOND));
 
 			RandomAccessFile file = new RandomAccessFile(projectPath+Check.folderSeparator()+projectName+".project.xml","rw");
 			file.writeBytes("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");
 			file.writeBytes("<project>\r\n");
 			file.writeBytes("	<header>\r\n");
 			file.writeBytes("		<project>\r\n");
 			file.writeBytes("			<idname>"+projectName+"</idname>\r\n");
 			file.writeBytes("			<fullname>"+escapeString(projectFullName)+"</fullname>\r\n");
 			file.writeBytes("			<version>"+escapeString(projectVersion)+"</version>\r\n");
 			file.writeBytes("			<path>"+path+"</path>\r\n");
 			file.writeBytes("		</project>\r\n");
 			file.writeBytes("		<delta>\r\n");
 			if(projectHasDeltas.equals("true")){
 				file.writeBytes("			<hasdeltas>true</hasdeltas>\r\n");
 				file.writeBytes("			<mainproject>"+projectDeltaMain+"</mainproject>\r\n");
 			}else{
 				file.writeBytes("			<hasdeltas>false</hasdeltas>\r\n");
 				file.writeBytes("			<mainproject></mainproject>\r\n");
 			}
 			file.writeBytes("		</delta>\r\n");
 			file.writeBytes("		<build>\r\n");
 			file.writeBytes("			<builder>"+escapeString(projectAuthor)+"</builder>\r\n");
 			file.writeBytes("			<date>"+c.get(GregorianCalendar.YEAR)+"-"+month+"-"+day+"</date>\r\n");
 			file.writeBytes("			<time>"+hour+":"+minute+":"+second+"</time>\r\n");
 			file.writeBytes("		</build>\r\n");
 			file.writeBytes("		<stats>\r\n");
 			int pErrs = 0, tErrs = 0, excluded = 0, compileErr = 0;
 			for(int i=0;i<files.size();i++){
 				pErrs += files.get(i).getParserError().size();
 				tErrs += files.get(i).getTypeError().size();
 				if(files.get(i).isExcluded()){
 					excluded++;
 				}
 				if(files.get(i).isCompileError()){
 					compileErr++;
 				}
 			}
 			file.writeBytes("			<parsererrors>"+pErrs+"</parsererrors>\r\n");
 			file.writeBytes("			<typeerrors>"+tErrs+"</typeerrors>\r\n");
 			file.writeBytes("			<excludedfiles>"+excluded+"</excludedfiles>\r\n");
 			file.writeBytes("			<compileerrors>"+compileErr+"</compileerrors>\r\n");
 			file.writeBytes("		</stats>\r\n");
 			file.writeBytes("	</header>\r\n");
 			file.writeBytes("	<errors>\r\n");
 			
 			for(int i=0;i<files.size();i++){
 				file.writeBytes("		<file>\r\n");
 				file.writeBytes("			<path>"+files.get(i).getPath().substring(path.length()+1)+"</path>\r\n");
 				file.writeBytes("			<excluded>"+files.get(i).isExcluded()+"</excluded>\r\n");
 				file.writeBytes("			<compileerror>"+files.get(i).isCompileError()+"</compileerror>\r\n");
 				file.writeBytes("			<summary>\r\n");
 				file.writeBytes("				<parsererrors>"+files.get(i).getParserError().size()+"</parsererrors>\r\n");
 				file.writeBytes("				<typeerrors>"+files.get(i).getTypeError().size()+"</typeerrors>\r\n");
 				file.writeBytes("			</summary>\r\n");
 				file.writeBytes("			<errorlist>\r\n");
 				for(int j=0;j<files.get(i).getParserError().size();j++){
 					file.writeBytes("				<parsererror>\r\n");
 					file.writeBytes("					<featurestr>"+escapeString(files.get(i).getParserError().get(j).getFeaturestr())+"</featurestr>\r\n");
 					file.writeBytes("					<msg>"+escapeString(files.get(i).getParserError().get(j).getMsg())+"</msg>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+files.get(i).getParserError().get(j).getFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+files.get(i).getParserError().get(j).getLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+files.get(i).getParserError().get(j).getCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("				</parsererror>\r\n");
 				}
 				for(int j=0;j<files.get(i).getTypeError().size();j++){
 					file.writeBytes("				<typeerror>\r\n");
 					file.writeBytes("					<featurestr>"+escapeString(files.get(i).getTypeError().get(j).getFeaturestr())+"</featurestr>\r\n");
 					file.writeBytes("					<severity>"+files.get(i).getTypeError().get(j).getSeverity()+"</severity>\r\n");
 					file.writeBytes("					<msg>"+escapeString(files.get(i).getTypeError().get(j).getMsg())+"</msg>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+files.get(i).getTypeError().get(j).getFromFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+files.get(i).getTypeError().get(j).getFromLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+files.get(i).getTypeError().get(j).getFromCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+files.get(i).getTypeError().get(j).getToFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+files.get(i).getTypeError().get(j).getToLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+files.get(i).getTypeError().get(j).getToCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("				</typeerror>\r\n");
 				}
 				file.writeBytes("			</errorlist>\r\n");
 				file.writeBytes("		</file>\r\n");
 			}
 			
 
 			file.writeBytes("	</errors>\r\n");
 			file.writeBytes("</project>\r\n");
 			file.close();
 		
 		}catch (IOException e){
 			Exceptions.throwException(4, e, true, path);
 		}
 	}
 	
 	/**
 	 * Main class
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length!=7){
 			System.out.println("Help - Web_ProjectInitializator "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: Web_ProjectInitializator [PROJECTPATH] [PROJECTNAME] [PROJECTFULLNAME] ");
 			System.out.println("                                [PROJECTVERSION] [PROJECTAUTHOR] [DELTAPROJECT]");
 			System.out.println("                                [DELTAMAIN] [GLOBAL_SETTINGS]");
 			System.out.println("\n[PROJECTPATH]");
 			System.out.println("     Absolute Path for scan for TypeChef-Files\n");
 			System.out.println("[PROJECTNAME]");
 			System.out.println("     Project shortcut-name. It must be unique!\n");
 			System.out.println("[PROJECTFULLNAME]");
 			System.out.println("     Project name\n");
 			System.out.println("[PROJECTVERSION]");
 			System.out.println("     Project version\n");
 			System.out.println("[PROJECTAUTHOR]");
 			System.out.println("     Project author\n");
 			System.out.println("[DELTAPROJECT]");
 			System.out.println("     true or false, if the project has deltas\n");
 			System.out.println("[GLOBAL_SETTINGS]");
 			System.out.println("     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			String path = args[0];
 			String projectName = args[1];
 			String projectFullName = args[2];
 			String projectVersion = args[3];
 			String projectAuthor = args[4];
 			String projectHasDeltas = args[5];
 			String settingFile = args[6];
 			
 			System.out.println("Starting initialization from project "+projectName+"...");
 			
 			long time = System.currentTimeMillis();
 
 			parser = new Parser(settingFile);
 			String projectPath = parser.getSetting_ProjectPath();
 			
			if(!check.uniqueCheck(projectName, projectPath)||projectHasDeltas.equals("true")){
 				System.out.println("Project name is OK!");
 			}else{
 				Exceptions.throwException(5, null, true, "");
 			}
 			
 			//Removes an additional folder separator from an path end
 			//ex. /app/home/foo/bar/ --> /app/home/foo/bar
 			if(path.endsWith(Check.folderSeparator()+"")){
 				path = path.substring(0,path.length()-1);
 			}
 			
 			File file = new File(path);
 			if(!file.exists()){
 				Exceptions.throwException(7, null, true, path);
 			}
 			
 			String str = getAllFiles(path);
 			if(str.equals("")){
 				System.out.println("Initialization DONE!");
 			}else{
 				Exceptions.throwException(6, null, true, str);
 			}
 			
 			System.out.println("Sort files...");
 			
 			ErrorFile[] errFiles = new ErrorFile[files.size()];
 			for(int i=0;i<files.size();i++){
 				errFiles[i] = files.get(i);
 			}
 
 			Arrays.sort(errFiles);
 			files.clear();
 			for(int i=0;i<errFiles.length;i++){
 				files.add(errFiles[i]);
 			}
 
 			System.out.println("Sorting DONE!");
 			System.out.println("Writing down the project file...");
 			
 			if(projectHasDeltas.equals("true")){
 				projectName = Tools.findAFreeProjectName(projectName, path, true);
 			}
 			
 			writeProjectFile(projectPath,projectName,projectFullName,projectVersion,projectAuthor,path,projectHasDeltas,args[1]);
 			
 			System.out.println("Writing DONE!\nScript DONE!");
 			System.out.printf("Duration: %.2f sec\n",(System.currentTimeMillis()-time)/1000.0);
 		}
 	}
 }
