 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 
 import tcwi.xml.*;
 import tcwi.enumFiles.ErrorState;
 import tcwi.exception.Exceptions;
 import tcwi.fileHandler.Check;
 import tcwi.TCWIFile.ErrorCompareFile;
 import tcwi.TCWIFile.ErrorFile;
 
 public class Web_TreeViewInitializator {
 
	private static final String VERSION = "0.5.1.1";
 	private static final String AUTHORS = "EifX & hulllemann";
 	private static ArrayList<String> javascript = new ArrayList<String>();
 	private static String folderSeparator = Check.folderSeparator();
 	private static MasterFile pFile;
 	private static ArrayList<FolderElem> errFailFolders;
 	private static ProjectType projectType = ProjectType.PROJECT;
 	
 	/**
 	 * Check a given folder has files with errors in it
 	 * @param path
 	 * @return
 	 */
 	private static boolean isAFailFolder(String path){
 		for(int i=0;i<errFailFolders.size();i++){
 			if(errFailFolders.get(i).getPath().equals(path)){
 				return errFailFolders.get(i).isFail();
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Returns position number from an element in errFailFolders
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
 	 * Helper method for analyseFailFolders
 	 * @param elem
 	 */
 	private static void addInFailFolders(FolderElem elem){
 		String[] strArr = elem.getPath().split(folderSeparator);
 		String str = "";
 		for(int i=0;i<strArr.length;i++){
 			if(i==0)
 				str = strArr[0];
 			else
 				str = str+folderSeparator+strArr[i];
 			
 			int failNr = isExistInErrFailFolders(str);
 			if(failNr==-1){
 				FolderElem e = new FolderElem(elem.isFail(),str);
 				errFailFolders.add(e);
 			}else{
 				if(elem.isFail()){
 					errFailFolders.get(failNr).setFail(true);
 				}
 			}
 			
 		}
 	}
 	
 	/**
 	 * Analyse all folders and check, if it is a fail-folder or not
 	 */
 	private static void analyseFailFolders(){
 		errFailFolders = new ArrayList<FolderElem>();
 		
 		if(projectType==ProjectType.PROJECT){
 			for(int i=0;i<((ProjectFile) pFile).getFiles().size();i++){
 				ErrorFile eFile = ((ProjectFile) pFile).getFiles().get(i);
 				String str = "";
 				if(eFile.getPath().lastIndexOf(folderSeparator)>0){
 					str = eFile.getPath().substring(0,eFile.getPath().lastIndexOf(folderSeparator));
 				}else{
 					str = eFile.getPath();
 				}
 				
 				FolderElem e = new FolderElem(eFile.haveErrors()||eFile.isCompileError(),str);
 				addInFailFolders(e);
 			}
 		}
 		if(projectType==ProjectType.COMPARE){
 			for(int i=0;i<((CompareFile) pFile).getFiles().size();i++){
 				ErrorCompareFile eFile = ((CompareFile) pFile).getFiles().get(i);
 				String str = "";
 				if(eFile.getPath().lastIndexOf(folderSeparator)>0){
 					str = eFile.getPath().substring(0,eFile.getPath().lastIndexOf(folderSeparator));
 				}else{
 					str = eFile.getPath();
 				}
 				
				FolderElem e = new FolderElem(eFile.haveErrors()||eFile.getCompileError()==ErrorState.NOWCOMPILEERROR||eFile.getCompileError()==ErrorState.COMPILEERROR,str);
 				addInFailFolders(e);
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
 	private static String getIcon(String dirType, boolean isFail){
 		if(!isFail){
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
 	private static String getIcon(ErrorCompareFile file){
 		if(file.getFilestate().equals(ErrorState.DELETED)){
 			return "filedelete";
 		}
 		if(file.getFilestate().equals(ErrorState.CREATED)&&file.haveErrors()){
 			return "filenewerror";
 		}
 		if(file.getFilestate().equals(ErrorState.CREATED)&&!file.haveErrors()){
 			return "filenew";
 		}
 		if(file.isParserErrorCountChanged() || file.isTypeErrorCountChanged()){
 			return "filedifference";
 		}
 		
 		return "fileidentical";
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
 		if(projectType == ProjectType.PROJECT){
 			for(int i=0;i<((ProjectFile)pFile).getFiles().size();i++){
 				relativeFiles.add(((ProjectFile)pFile).getFiles().get(i).getPath());
 			}
 		}
 		if(projectType == ProjectType.COMPARE){
 			for(int i=0;i<((CompareFile) pFile).getFiles().size();i++){
 				relativeFiles.add(((CompareFile) pFile).getFiles().get(i).getPath());
 			}
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
 					if(p.startsWith(folderSeparator)){
 						p = p.substring(folderSeparator.length());
 					}
 					
 					boolean isFail = isAFailFolder(p);
 					javascript.add(cleanStr(pathArr[j])+".iconSrc = ICONPATH + \""+getIcon("open",isFail)+".gif\"");
 					javascript.add(cleanStr(pathArr[j])+".iconSrcClosed = ICONPATH + \""+getIcon("closed",isFail)+".gif\"");
 					p="";
 				}
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
 			
 			if(projectType==ProjectType.PROJECT){
 				javascript.add("doc"+i+".iconSrc = ICONPATH + \""+getIcon(((ProjectFile) pFile).getFiles().get(i))+".gif\"");
 			}
 			if(projectType==ProjectType.COMPARE){
 				javascript.add("doc"+i+".iconSrc = ICONPATH + \""+getIcon(((CompareFile) pFile).getFiles().get(i))+".gif\"");
 			}
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
 		if(projectType == ProjectType.PROJECT){
 			javascript.add("MAX_LENGTH = "+((ProjectFile) pFile).getFiles().size());
 			javascript.add("foldersTree = gFld(\"<i>"+pFile.getFullname()+" "+((ProjectFile) pFile).getVersion()+"</i>\", \"\")");
 		}
 		if(projectType == ProjectType.COMPARE){
 			javascript.add("MAX_LENGTH = "+((CompareFile) pFile).getFiles().size());
 			javascript.add("foldersTree = gFld(\"<i>"+pFile.getFullname()+"</i>\", \"\")");
 		}
 		javascript.add("foldersTree.treeID = \"Frameset\"");
 		
 		if(projectType == ProjectType.PROJECT){
 			if(((ProjectFile) pFile).haveErrors()){
 				javascript.add("foldersTree.iconSrc = ICONPATH + \"folderopenfail.gif\"");
 				javascript.add("foldersTree.iconSrcClosed = ICONPATH + \"folderclosedfail.gif\"");
 			}else{
 				javascript.add("foldersTree.iconSrc = ICONPATH + \"folderopenok.gif\"");
 				javascript.add("foldersTree.iconSrcClosed = ICONPATH + \"folderclosedok.gif\"");
 			}
 		}
 		if(projectType == ProjectType.COMPARE){
 			if(((CompareFile) pFile).haveErrors()){
 				javascript.add("foldersTree.iconSrc = ICONPATH + \"folderopenfail.gif\"");
 				javascript.add("foldersTree.iconSrcClosed = ICONPATH + \"folderclosedfail.gif\"");
 			}else{
 				javascript.add("foldersTree.iconSrc = ICONPATH + \"folderopenok.gif\"");
 				javascript.add("foldersTree.iconSrcClosed = ICONPATH + \"folderclosedok.gif\"");
 			}
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
 			System.out.println("Read needed variables...");
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
 			File f = new File( WebIntProjectsPath+folderSeparator+projectName+".project.xml");
 			String project_settings_xml_path = "";
 			if(f.exists()){
 				project_settings_xml_path = WebIntProjectsPath+folderSeparator+projectName+".project.xml";
 				projectType = ProjectType.PROJECT;
 			}else{
 				project_settings_xml_path = WebIntProjectsPath+folderSeparator+projectName+".compare.xml";
 				projectType = ProjectType.COMPARE;
 			}
 
 			//Do the work
 			System.out.println("Read folder tree...");
 			try {
 				if(projectType == ProjectType.PROJECT){
 					pFile = Parser.getProject(project_settings_xml_path);
 				}else{
 					pFile = Parser.getCompare(project_settings_xml_path);
 				}
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
