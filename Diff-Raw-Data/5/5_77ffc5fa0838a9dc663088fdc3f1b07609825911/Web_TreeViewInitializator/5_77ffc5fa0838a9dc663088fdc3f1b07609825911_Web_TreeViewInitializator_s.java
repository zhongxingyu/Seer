 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 
 import tcwi.xml.*;
 import tcwi.exception.Exceptions;
 import tcwi.fileHandler.Check;
 import tcwi.TCWIFile.ErrorFile;
 
 public class Web_TreeViewInitializator {
 
	private static final String VERSION = "0.4.1.2";
 	private static final String AUTHORS = "EifX & hulllemann";
 	private static ArrayList<String> javascript = new ArrayList<String>();
 	private static String folderSeparator = Check.folderSeparator();
 	private static ProjectFile pFile;
 	private static ArrayList<FolderElem> errFailFolders;
 	
 	/**
 	 * Check a given folder has files with errors in it
 	 * @param path
 	 * @return
 	 */
 	private static boolean isAFailFolder(String path){
 		String str = "";
 		if(path.lastIndexOf(folderSeparator)>0){
 			str = path.substring(0,path.lastIndexOf(folderSeparator));
 		}else{
 			str = path;
 		}
 		for(int i=0;i<errFailFolders.size();i++){
 			if(errFailFolders.get(i).getPath().equals(str)){
 				return errFailFolders.get(i).isFail();
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Helper-Method for analyseFailFolders
 	 * @param path
 	 * @return
 	 */
 	private static int isExistInErrFailFolders(String path){
 		for(int i=0;i<errFailFolders.size();i++){
 			if(errFailFolders.get(i).getPath().equals(path)){
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Analyse all folders and check, if it is a fail-folder or not
 	 */
 	private static void analyseFailFolders(){
 		errFailFolders = new ArrayList<FolderElem>();
 		for(int i=0;i<pFile.getFiles().size();i++){
 			ErrorFile eFile = pFile.getFiles().get(i);
 			String str = "";
 			if(eFile.getPath().lastIndexOf(folderSeparator)>0){
 				str = eFile.getPath().substring(0,eFile.getPath().lastIndexOf(folderSeparator));
 			}else{
 				str = eFile.getPath();
 			}
 			FolderElem e = new FolderElem(eFile.haveErrors(),str);
 			
 			int errNr = isExistInErrFailFolders(str);
 			if(errNr==-1){
 				errFailFolders.add(e);
 			}else{
				if(eFile.haveErrors()){
 					errFailFolders.get(errNr).setFail(true);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Write the output file
 	 * @param path
 	 * @param prettyOutput
 	 */
 	private static void writeTxt(String path, boolean noChk){
 		try{
 			File f = new File(path);
 			f.delete();
 			RandomAccessFile file = new RandomAccessFile(path,"rw");
 			for(int i=0;i<javascript.size();i++){
 				if(noChk){
 					if(!javascript.get(i).contains(".prependHTML =")){
 						file.writeBytes(javascript.get(i)+"\r\n");
 					}
 				}else{
 					file.writeBytes(javascript.get(i)+"\r\n");
 				}
 			}
 			file.close();
 		}catch (IOException e){
 			Exceptions.throwException(3, e, true, path);
 		}
 	}
 
 	/**
 	 * Get the right dir-icon for the treeview
 	 * @param path
 	 * @param projectType
 	 * @return
 	 */
 	private static String getIcon(String path, String dirType){
 		if(!isAFailFolder(path)){
 			return "folder"+dirType+"ok";
 		}else{
 			return "folder"+dirType+"fail";
 		}
 	}
 	
 	/**
 	 * Get the right file-icon for the treeview
 	 * @param file
 	 * @param projectType
 	 * @return
 	 */
 	private static String getIcon(ErrorFile file){
 		if(!file.haveErrors()){
 			if(file.isCompileError()){
 				return "filecompilefail";
 			}else if(file.isExcluded()){
 				return "fileempty";
 			}else{
 				return "fileok";
 			}
 		}else{
 			return "filefail";
 		}
 	}
 
 	/**
 	 * Checks the difference between the old and the new path and
 	 * deletes or creates folders<br>
 	 * The method returns an int-array. The first number in the array counts
 	 * how many folders must be go back. The second number in the array counts
 	 * how many folder must be go up (or must be created).
 	 * @param oldArr
 	 * @param newArr
 	 * @return
 	 */
 	private static int[] dirDecision(String[] oldArr, String[] newArr){
 		int oldLen = oldArr.length-1;
 		int newLen = newArr.length-1;
 		int[] output = {0,0};
 		
 		if(oldLen>newLen){
 			for(int i=0;i<oldLen;i++){
 				if(i<newLen){
 					if(!oldArr[i].equals(newArr[i])){
 						output[0] = newLen-i;
 						output[1] = i;
 						return output;
 					}
 				}
 			}
 			return output;
 		}else if(oldLen==newLen){
 			for(int i=0;i<oldLen;i++){
 				if(i<newLen){
 					if(!oldArr[i].equals(newArr[i])){
 						output[0] = oldLen-i;
 						output[1] = i;
 						return output;
 					}
 				}
 			}
 			return output;
 		}else{
 			for(int i=0;i<newLen;i++){
 				if(i<oldLen){
 					if(!newArr[i].equals(oldArr[i])){
 						output[0] = newLen-i;
 						output[1] = i;
 						return output;
 					}
 				}
 			}
 			output[0] = newLen-oldLen;
 			output[1] = oldLen;
 			return output;
 		}
 	}
 	
 	/**
 	* This method cleans a string. Only letters and numbers are excepted in a string, all other
 	* values are replaced by an underscore.
 	* @param str
 	* @return a clean String
 	*/
 	private static String cleanStr(String str){
 		String newStr = "";
 		for(int i=0;i<str.length();i++){
 			if(((str.charAt(i)>=65)&&(str.charAt(i)<=90))||((str.charAt(i)>=97)&&(str.charAt(i)<=122))||((str.charAt(i)>=48)&&(str.charAt(i)<=57))){
 				newStr += str.charAt(i);
 			}else{
 				newStr += "_";
 			}
 		}
 		return "dir"+newStr;
 	}
 	
 	/**
 	 * Main Method to generate the JS-Tree
 	 * @param projectName
 	 * @param projectType
 	 * @param projectPath
 	 */
 	private static void generateJSTree(){
 		ArrayList<String> relativeFiles = new ArrayList<String>();
 		for(int i=0;i<pFile.getFiles().size();i++){
 			relativeFiles.add(pFile.getFiles().get(i).getPath());
 		}
 		
 		String[] oldArr = {""};
 
 		for(int i=0;i<relativeFiles.size();i++){
 			String newPath = relativeFiles.get(i);
 			
 			String[] pathArr;
 			pathArr = relativeFiles.get(i).split(folderSeparator);
 
 			//If the path has changed, draw the missing parts
 			String p = "";
 			int[] dir = dirDecision(oldArr, pathArr);
 			//Write folders
 			if(dir[0]>0){
 				for(int j=dir[1];j<(dir[0]+dir[1]);j++){
 					if(j==0){
 						javascript.add(cleanStr(pathArr[0])+" = insFld(foldersTree, gFld(\""+pathArr[0]+"\", \"\"))");
 					}else{
 						javascript.add(cleanStr(pathArr[j])+" = insFld("+cleanStr(pathArr[j-1])+", gFld(\""+pathArr[j]+"\", \"\"))");
 					}
 					
 					
 					for(int k=0;k<=j;k++){
 						p+=folderSeparator+pathArr[k];
 					}
 					p = p.substring(1);
 					
 					javascript.add(cleanStr(pathArr[j])+".iconSrc = ICONPATH + \""+getIcon(p,"open")+".gif\"");
 					javascript.add(cleanStr(pathArr[j])+".iconSrcClosed = ICONPATH + \""+getIcon(p,"closed")+".gif\"");
 				}
 			}else{
 				//p = oldP;
 			}
 			
 			//Write files
 			newPath = newPath.replace("\\", "/");
 			newPath = newPath.replace(" ", "_");
 			
 			//Print the files!
 			if(pathArr.length==1){
 				javascript.add("doc"+i+" = insDoc(foldersTree, gLnk(\"S\", \""+pathArr[pathArr.length-1]+".c\", P1+\""+newPath+"\"+P2))");
 			}else{
 				javascript.add("doc"+i+" = insDoc("+cleanStr(pathArr[pathArr.length-2])+", gLnk(\"S\", \""+pathArr[pathArr.length-1]+".c\", P1+\""+newPath+"\"+P2))");
 			}
 			
 			javascript.add("doc"+i+".iconSrc = ICONPATH + \""+getIcon(pFile.getFiles().get(i))+".gif\"");
 			javascript.add("doc"+i+".prependHTML = C1+\""+i+"\"+C2");
 			
 			oldArr = pathArr;
 		}
 	}
 	
 	/**
 	 * This Method draws the javascript-header
 	 * @param projectFullNameAndVersion
 	 * @param iconPath
 	 * @param prePath
 	 * @param postPath
 	 * @param projectType
 	 * @param failureProject
 	 */
 	private static void generateJSHeader(String iconPath, String WebsiteDefaultURL){
 		javascript.add("USETEXTLINKS = 1");
 		javascript.add("STARTALLOPEN = 0");
 		javascript.add("USEICONS = 1");
 		javascript.add("BUILDALL = 0");
 		javascript.add("USEFRAMES = 0");
 		javascript.add("PRESERVESTATE = 1");
 		javascript.add("HIGHLIGHT = 0");
 		javascript.add("P1 = \""+WebsiteDefaultURL+"/project?choice=view&files=\"");
 		javascript.add("P2 = \"&project="+pFile.getIdname()+"\"");
 		javascript.add("C1 = \"<td valign=middle><input type=checkbox id=\\\"chkbox\"");
 		javascript.add("C2 = \"\\\"></td>\"");
 		javascript.add("ICONPATH = '"+iconPath+"'");
 		javascript.add("MAX_LENGTH = "+pFile.getFiles().size());
 		
 		javascript.add("foldersTree = gFld(\"<i>"+pFile.getFullname()+" "+pFile.getVersion()+"</i>\", \"\")");
 		javascript.add("foldersTree.treeID = \"Frameset\"");
 		
 		if(!pFile.haveErrors()){
 			javascript.add("foldersTree.iconSrc = ICONPATH + \"folderopenok.gif\"");
 			javascript.add("foldersTree.iconSrcClosed = ICONPATH + \"folderclosedok.gif\"");
 		}else{
 			javascript.add("foldersTree.iconSrc = ICONPATH + \"folderopenfail.gif\"");
 			javascript.add("foldersTree.iconSrcClosed = ICONPATH + \"folderclosedfail.gif\"");
 		}
 	}
 
 	/**
 	 * Main function
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length!=2){
 			System.out.println("Help - web_TreeViewInitializator "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: web_TreeViewInitializator [PROJECTNAME] [GLOBAL_SETTINGS]");
 			System.out.println("\n[PROJECTNAME]");
 			System.out.println("\n     Project name\n");
 			System.out.println("\n[GLOBAL_SETTINGS]");
 			System.out.println("\n     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			System.out.println("\nRead needed variables...");
 			String projectName = args[0];
 			String globalSettings = args[1];
 			
 			long time = System.currentTimeMillis();
 			
 			//Init the XMLParser
 			Parser settingsParser = new Parser(globalSettings);
 			String WebIntProjectsPath = settingsParser.getSetting_ProjectPath();
 			String TreeViewPath = settingsParser.getSetting_TreeviewPath();
 			String TreeViewIconsPath = settingsParser.getSetting_TreeviewIconsPath();
 			String WebsiteDefaultURL = settingsParser.getSetting_WebsiteDefaultURI();
 			
 			//Read the project dir
 			String project_settings_xml_path = WebIntProjectsPath+folderSeparator+projectName+".project.xml";
 
 			//Do the work
 			System.out.println("Read folder tree...");
 			try {
 				pFile = Parser.getProject(project_settings_xml_path);
 			} catch (IOException e) {
 				Exceptions.throwException(1, e, true, project_settings_xml_path);
 			} catch (Exception e) {
 				Exceptions.throwException(2, e, true, project_settings_xml_path);
 			}
 			
 			System.out.println("Analyse fail-folders...");
 
 			analyseFailFolders();
 			
 			System.out.println("Build JavaScript-File...");
 			
 			generateJSHeader(TreeViewIconsPath, WebsiteDefaultURL);
 			generateJSTree();
 			
 			System.out.println("Save folder tree...");
 			
 			//Build the path for the JSON-path
 			writeTxt(TreeViewPath+folderSeparator+projectName+".js",false);
 			writeTxt(TreeViewPath+folderSeparator+projectName+".nochk.js",true);
 			System.out.println("DONE!");
 			System.out.printf("Duration: %.2f sec\n",(System.currentTimeMillis()-time)/1000.0);
 		}
 	}
 
 }
