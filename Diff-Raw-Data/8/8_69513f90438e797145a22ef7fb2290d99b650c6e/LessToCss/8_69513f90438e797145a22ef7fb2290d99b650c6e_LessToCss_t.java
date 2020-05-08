 package com.teamstudio;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.util.Vector;
 
 import javax.faces.context.FacesContext;
 
 import lotus.domino.*;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 public class LessToCss implements Serializable {
 	public static final String BEAN_NAME = "LessToCssBean";
 	private static final long serialVersionUID = 5801795019973564444L;
 	public String generatedFilePath;
 
 	public LessToCss() {
 
 	}
 
 	public static LessToCss get(FacesContext context) {
 		return (LessToCss) context.getApplication().getVariableResolver().resolveVariable(context, LessToCss.BEAN_NAME);
 	}
 
 	@SuppressWarnings("unchecked")
 	public String createCSSFileFromLessText(Document doc, String Less, String CSSfileName) {
 		String cSSFileLocation = "";
 
 		String lessFile = "temp.less";
 		String cssFile = CSSfileName;
 		String jarFile = "jcruncherEx.jar";
 
 		Session session = JSFUtil.getSessionAsSigner();
 		String delim = System.getProperty("file.separator");
 		generatedFilePath = "";
 		try {
 			generatedFilePath = getGeneratedFilePath(session, "templess");
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NotesException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		String FulllessFile = generatedFilePath + delim + lessFile;
 		String FullcssFile = generatedFilePath + delim + cssFile;
 		String FulljarFile = generatedFilePath + delim + jarFile;
 
 		File inputLessFile = new File(FulllessFile);
 
 		try {
 			inputLessFile.createNewFile();
 			FileWriter fw = new FileWriter(FulllessFile, true); // the true will
 																// append the
 																// new data
 			fw.write(Less);// appends the string to the file
 			fw.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		if (!inputLessFile.exists()) {
 			System.out.println("not got file:" + FulllessFile);
 		} else {
 			try{
 				//Now output other LESS files out to dir
 				RichTextItem rtitem = (RichTextItem)doc.getFirstItem("lessfiles");
 				Vector objects = rtitem.getEmbeddedObjects();
 				for (int i=0; i<objects.size(); i++){
 					EmbeddedObject object = (EmbeddedObject)objects.elementAt(i);
 					object.extractFile(generatedFilePath + delim + object.getName());
 				}
 			}catch(NotesException ne){
 				ne.printStackTrace();
 			}
 			
 			String commandline = "";
 			commandline = commandline + "\"" + getJavaDirectory(session) + "/java\"";
 			commandline = commandline + " -jar \"" + FulljarFile + "\"";
 			commandline = commandline + " --less \"" + FulllessFile + "\" \"" + FullcssFile + "\"";
 			try {
 				copyResourceToFs("jcruncherEx.jar", FulljarFile);
 				runprogram(commandline);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			//cleanupfiles(FulllessFile);
 			cleanupfiles(FulljarFile);
 
 			cSSFileLocation = FullcssFile;
 		}
 		return cSSFileLocation;
 	}
 
 	public static boolean cleanupfiles(String filename) {
 		try {
 			File file = new File(filename);
 
 			if (file.delete()) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	public boolean cleanupDir(){
 		try {
 			FileUtils.deleteDirectory(new File(generatedFilePath));
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	public static void runprogram(String commandline) throws InterruptedException, IOException {
 		try {
 
 			ProcessBuilder pb = new ProcessBuilder(commandline);
 			pb.redirectErrorStream(true); // merge stdout, stderr of process
 
 			Process p = pb.start();
 			InputStreamReader isr = new InputStreamReader(p.getInputStream());
 			BufferedReader br = new BufferedReader(isr);
 
 			String lineRead;
 			StringBuffer out = new StringBuffer();
 			while ((lineRead = br.readLine()) != null) {
 				System.out.println(lineRead);
 				out.append(lineRead);
 			}
 
 			int rc = p.waitFor();
 			if (rc != 0){
 				throw new Exception("Non zero return from command line:" + out.toString());
 			}
 		} catch (IOException e) {
 			e.printStackTrace(); // or log it, or otherwise handle it
 		} catch (InterruptedException ie) {
 			ie.printStackTrace(); // or log it, or otherwise handle it
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public static String getGeneratedFilePath(Session session, String DocumentsDir) throws IOException, SecurityException, NotesException {
 		String delim = System.getProperty("file.separator");
 		String userDir = System.getProperty("user.home");
 		String tmp = session.evaluate("@Unique").get(0).toString();
 		String fullPath = null;
 		if (isWindows()) {
 			fullPath = session.getEnvironmentString("Directory", true) + delim + DocumentsDir + delim + tmp;
 			fullPath = fullPath.replaceAll("Program Files", "Progra~1");
 		} else {
 			fullPath = userDir + delim + DocumentsDir + delim + tmp;
 		}
 		createDir(fullPath);
 		return fullPath;
 	}
 
 	public static String getJavaDirectory(Session session) {
 		String userDir = System.getProperty("user.home");
 		String fullPath = "";
 		if (isWindows()) {
 			try {
 				fullPath = session.getEnvironmentString("Directory", true);
 			} catch (NotesException e) {
 				e.printStackTrace();
 			}
 			fullPath = fullPath.replaceAll("Program Files", "Progra~1");
			fullPath = fullPath.replaceAll("data", "jvm/bin");
 		} else {
			//fullPath = userDir;
			//fullPath = fullPath.replaceAll("data", "linux/jvm/bin");
			fullPath = "/opt/ibm/lotus/notes/latest/linux";
 		}
 
 		return fullPath;
 
 	}
 
 	public static void createDir(String fullPath) throws IOException, SecurityException {
 		if (fullPath != null && fullPath.length() > 0) {
 			File theDir = new File(fullPath);
 			if (!theDir.exists()) {
 				if (!theDir.mkdirs())
 					throw new IOException("System couldn't create directory at '" + fullPath + "'. This is probably an OS security issue");
 			}
 		}
 	}
 
 	public static boolean isWindows() {
 		String os = System.getProperty("os.name").toLowerCase();
 		return (os.indexOf("win") > -1);
 	}
 
 	public static boolean isMac() {
 		String os = System.getProperty("os.name").toLowerCase();
 		return (os.indexOf("mac") > -1);
 	}
 
 	public static boolean isUnix() {
 		String os = System.getProperty("os.name").toLowerCase();
 		return (os.indexOf("nix") > -1 || os.indexOf("nux") > -1);
 	}
 
 	public static boolean isSolaris() {
 		String os = System.getProperty("os.name").toLowerCase();
 		return (os.indexOf("sunos") > -1);
 	}
 
 	// *************************Copy binary file stuff***************
 
 	public static boolean copyResourceToFs(String resource_Name, String output_File_Name) {
 		try {
 			InputStream inStream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(resource_Name);
 			byte[] fileContents = IOUtils.toByteArray(inStream);
 			writeBinaryFile(fileContents, output_File_Name);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	/**
 	 * Write a byte array to the given file. Writing binary data is
 	 * significantly simpler than reading it.
 	 */
 	static void writeBinaryFile(byte[] aInput, String aOutputFileName) {
 		try {
 			OutputStream output = null;
 			try {
 				output = new BufferedOutputStream(new FileOutputStream(aOutputFileName));
 				output.write(aInput);
 			} finally {
 				output.close();
 			}
 		} catch (FileNotFoundException ex) {
 			// "File not found."
 		} catch (IOException ex) {
 		}
 	}
 	
 }
