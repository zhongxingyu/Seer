 package pcc.ui;
 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import pcc.analysis.ChangeCounterUtils;
 import pcc.io.IOUtils;
 import pcc.vercon.Project;
 import pcc.vercon.ProjectVersion;
 
 
 public class Main{
 	public static Project project;
 	public static String projectName;
 	public static PCCFrame frame;
 	private static Scanner in;
 	public static void main(String[] args){
 		//init resources
 		in = new Scanner(System.in);
 		frame = new PCCFrame();
 		frame.setVisible(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	public static void openProject(){
 		projectName	=	JOptionPane.showInputDialog(frame, "Project name: ");
 
 		if (null != projectName)
 		{
 			try {
 				project = IOUtils.openProject(projectName+File.separator+"project.dat");
				project.setProjectName(projectName);
 				JOptionPane.showMessageDialog(frame, "Opened "+ projectName);
 			} catch (Throwable t){
 				JOptionPane.showMessageDialog(null,"Error opening project file.");
 			}
 		}
 	}
 	private static void saveProject(){
 		if(project==null)
 			return;
 		try {
 			File dir = new File(projectName);
 			if(!dir.exists())
 				IOUtils.createFolder(projectName);
 			IOUtils.saveProject(project, projectName+File.separator+"project.dat");
 			//JOptionPane.showMessageDialog(frame, "Saved "+ projectName);
 		} catch (Throwable t) {
 			t.printStackTrace();
 			JOptionPane.showMessageDialog(null,"Error updating project file.");
 		}
 	}
 	public static void explicitSave(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		saveProject();
 	}
 	public static void newProject(){
 		
 		projectName	=	JOptionPane.showInputDialog(frame, "Project name: ");
 		
 		if (null != projectName)
 		{
 			if(!projectName.matches("[a-zA-Z0-9_]+"))
 				System.out.println("Invalid name entered: letters and underscores only.");
 			else{
 				if((new File(projectName)).exists())
 					System.out.println("A folder/file with the given name already exists.");
 				else{
 					project = new Project();
					project.setProjectName(projectName);
 					saveProject();
 					JOptionPane.showMessageDialog(frame, "Created project "+ projectName);
 				}
 			}
 		}
 	}
 	public static void addFile(){
 		if(project==null){
 			JOptionPane.showMessageDialog(frame, "Please open or create a project first.");
 			return;
 		}
 		
 		String name = JOptionPane.showInputDialog(frame, "File name: "); 
 		
 		if (null != name)
 		{
 			try{
				File file = new File(projectName+File.separator+name);
 				if(!file.exists())
 				{
 					throw new RuntimeException();
 				}
 			}
 			catch(Throwable t){
				JOptionPane.showMessageDialog(frame, "Error adding file.. check if file exists");
 				return;
 			}
 			for(String fn:project.getFiles())
 				if(fn.equalsIgnoreCase(name)){
 					JOptionPane.showMessageDialog(frame, "That file is already being monitored: see \"files\"");
 					return;
 				}
 			project.addFile(name);
 			saveProject();
 		}
 	}
 	
 	public static void removeFile(String name){
 		if(!project.removeFile(name))
 			JOptionPane.showMessageDialog(frame, "File Not Found!");
 		saveProject();
 	}
 	
 	public static void commitNewVersion(){
 		if(project==null){
 			JOptionPane.showMessageDialog(frame, "Please open or create a project first.");
 			return;
 		}
 		
 		String number = JOptionPane.showInputDialog(frame, "Version number: ");
 		
 		if (null != number)
 		{
 			if(project.getVersion(number)!=null){
 				JOptionPane.showMessageDialog(frame, "That version already exists: see \"versions\"");
 				return;
 			}
 			String author = JOptionPane.showInputDialog(frame, "Author name: ");
 			if (null != author)
 			{
 				String reason = JOptionPane.showInputDialog(frame, "Reason for commit: ");
 				if (null != reason)
 				{
 					try {
 						project.commit(number, author, reason);
 					} catch (IOException e) {
 						JOptionPane.showMessageDialog(frame, "Error commiting source files.");
 					}
 					saveProject();
 				}
 			}
 		}
 	}
 	public static void exportChangeLables(){
 		if(project==null){
 			JOptionPane.showMessageDialog(frame, "Please open or create a project first.");
 			return;
 		}
 		String version1 = 	JOptionPane.showInputDialog(frame, "First version: ");
 		
 		if (null != version1)
 		{
 	
 			String version2 = 	JOptionPane.showInputDialog(frame, "Last version: ");
 			if (null != version2)
 			{
 				String dir = JOptionPane.showInputDialog(frame, "Output directory: ");;
 				if (null != dir)
 				{
 					try {
 						if(IOUtils.fileExists(dir)){
 							if (JOptionPane.YES_OPTION != (JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete all files in this directory? ")))
 								return;
 							IOUtils.deleteFolder(dir);
 						}
 						ChangeCounterUtils.exportChangeLabels(dir,
 								project.getVersion(version1), project.getVersion(version2));
 					} catch (IOException e) {
 						e.printStackTrace();
 						JOptionPane.showMessageDialog(frame, "Error exporting change labels.");
 					}
 				}
 			}
 		}
 	}
 	public static void displayAllFiles(){
 		if(project==null){
 			JOptionPane.showMessageDialog(frame, "Please open or create a project first.");
 			return;
 		}
 		
 		String ret	=	"<html>";
 		for(String fn:project.getFiles())
 			ret += fn;
 		ret +="</br></html>";
 		
 		frame.setReport(ret);
 		
 	}
 	public static void displayCurrentVersion(){
 		if(project==null){
 			JOptionPane.showMessageDialog(frame, "Please open or create a project first.");
 			return;
 		}
 		//System.out.println(project.getCurrentVersion());
 		
 		frame.setReport(project.getCurrentVersion());
 
 	}
 	public static void displayAllVersions(){
 		if(project==null){
 			JOptionPane.showMessageDialog(frame, "Please open or create a project first.");
 			return;
 		}
 		
 		String ret	=	"<html>";
 		for(String v:project.getVersionList())
 			//System.out.println(v);
 			ret += v;
 		
 		ret += "<br></html>";
 	}
 	
 	public static String displayVersionData(String number){
 		if(project==null){
 			JOptionPane.showMessageDialog(frame, "Please open or create a project first.");
 			return "";
 		}
 		ProjectVersion version = project.getVersion(number);
 		String ret = "";
 		if(version==null)
 			JOptionPane.showMessageDialog(frame, "Invalid version number: see \"versions\"");
 		else{
 			ret+="<html>"+version.getMetaData();
 			ret+="<br>Total LLOC: "+ChangeCounterUtils.getLLOC(version);
 			ret+="</br></html>";
 		}
 		return ret;
 	}
 	public static void displayVersionChanges(boolean shortReport){
 		if(project==null){
 			JOptionPane.showMessageDialog(frame, "Please open or create a project first.");
 			return;
 		}
 		
 		String version1 = JOptionPane.showInputDialog(frame, "First version: ");
 		ProjectVersion v1 = project.getVersion(version1);
 		if(v1==null){
 			JOptionPane.showMessageDialog(frame, "Invalid version number.");
 			return;
 		}
 		
 		String version2 = JOptionPane.showInputDialog(frame, "Last version: ");
 		ProjectVersion v2 = project.getVersion(version2);
 		if(v2==null){
 			JOptionPane.showMessageDialog(frame, "Invalid version number.");
 			return;
 		}
 		
 		String report = "";
 		if(shortReport)
 			report = ChangeCounterUtils.getLLOCChanges(v1, v2);
 		else
 			report = ChangeCounterUtils.getLineChangesReport(v1, v2);
 		//System.out.println(report);
 		
 		frame.setReport(report);
 	}
 }
