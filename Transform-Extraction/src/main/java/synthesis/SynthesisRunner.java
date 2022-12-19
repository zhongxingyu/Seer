package synthesis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import add.entities.PatternInstance;
import add.entities.PropertyPair;
import spoon.Launcher;
import spoon.reflect.declaration.CtElement;
import synthesis.prepare.Config;
import synthesis.prepare.prepareRepository;
import synthesis.processors.SynthesisProcessor;
import synthesis.synthesizer.AbstractSynthesizer;
import synthesis.synthesizer.BinOperatorRepSynthesizer;
import synthesis.synthesizer.ConstantRepSynthesizer;
import synthesis.synthesizer.LogicalExpReduceSynthesizer;
import synthesis.synthesizer.MethodRwMethodSynthesizer;
import synthesis.synthesizer.MethodRwVarSynthesizer;
import synthesis.synthesizer.TargetedRepairTran;
import synthesis.synthesizer.UnwrapMethodSynthesizer;
import synthesis.synthesizer.VarRwMethodSynthesizer;
import synthesis.synthesizer.VarRwVarSynthesizer;

public class SynthesisRunner {
	
	public static void main(String[] args) throws Exception {
	    
		Config.setPath();
		startSynthesis();
	}
	
	public static void startSynthesis() throws Exception {

		   prepareRepository.downloadRepository();

	        String projectName;
	    	int identifyNumber;

		    projectName = "Chart";	   
		    for (int chartID=1; chartID <=26; chartID++)
	   	    {
		    	  if(chartID==1||chartID==8||chartID==20)
		      {
		    	  identifyNumber = chartID;
		    	  ArrayList<String> changedClasses = getChangedClass(projectName,identifyNumber);

		    	  for(int index=0; index<changedClasses.size(); index++) {
		    		  copyFileBTweenDirectories(changedClasses.get(index), projectName, identifyNumber);
		    		  analyzeDiffandSynthesis(changedClasses.get(index), projectName, identifyNumber);
		    	  }
		      }
	   	    }
	   	    
	   	    projectName = "Math";
	   	    for (int MathID=1; MathID <=106; MathID++)
		    {
	   	    	 if(MathID==5||MathID==58||MathID==70||MathID==75||MathID==85)
	   	    	 {
			        identifyNumber= MathID;
			    	 ArrayList<String> changedClasses = getChangedClass(projectName,identifyNumber);
			    	 
			    	 for(int index=0; index<changedClasses.size(); index++) {
			    		  copyFileBTweenDirectories(changedClasses.get(index), projectName, identifyNumber);
			    		  analyzeDiffandSynthesis(changedClasses.get(index), projectName, identifyNumber);
			    	  }
	   	    	 }
		    }
 	      
	   	    projectName = "Time";
	   	    for (int TimeID=1; TimeID <=27; TimeID++)
		    {
	   	    	if(TimeID==19)
	   	    	{
			       identifyNumber= TimeID;
			       ArrayList<String> changedClasses = getChangedClass(projectName,identifyNumber);
			       
			       for(int index=0; index<changedClasses.size(); index++) {
			    		  copyFileBTweenDirectories(changedClasses.get(index), projectName, identifyNumber);
			    		  analyzeDiffandSynthesis(changedClasses.get(index), projectName, identifyNumber);
			    	  }
	   	    	}
		    }   
	   	    
	   	    projectName = "Mockito";
	   	    for (int MockitoID=1; MockitoID <=38; MockitoID++)
		    {
	   	    	if(MockitoID==26)
	   	    	{
			       identifyNumber= MockitoID;
			       ArrayList<String> changedClasses = getChangedClass(projectName,identifyNumber);
			       
			       for(int index=0; index<changedClasses.size(); index++) {
			    		  copyFileBTweenDirectories(changedClasses.get(index), projectName, identifyNumber);
			    		  analyzeDiffandSynthesis(changedClasses.get(index), projectName, identifyNumber);
			    	  }
	   	    	}
		    }  
	   	    
	   	    projectName = "Closure";
	   	    for (int ClosureID=1; ClosureID <=133; ClosureID++)
		    {
	   	    	if(ClosureID==10||ClosureID==14||ClosureID==31||ClosureID==70||ClosureID==86||ClosureID==123)
	   	    	{
			       identifyNumber= ClosureID;
			       ArrayList<String> changedClasses = getChangedClass(projectName,identifyNumber);
			       
			       for(int index=0; index<changedClasses.size(); index++) {
			    		  copyFileBTweenDirectories(changedClasses.get(index), projectName, identifyNumber);
			    		  analyzeDiffandSynthesis(changedClasses.get(index), projectName, identifyNumber);
			    	}
	   	    	}
		    }  
	}
	
	public static void analyzeDiffandSynthesis(String changedclass, String project, int bug_id) throws Exception {
		
		  String[] class_name_before_whole = changedclass.split("\\.");
		  int length = class_name_before_whole.length; 
		  DiffAnalyzer analyzer = new DiffAnalyzer(Config.defects4jresult);
		  List<Pair<CtElement, ArrayList<PatternInstance>>> elementRepairInfo = analyzer.run(Config.defects4jdiffraw+"/"+ project.toLowerCase()+"_"+Integer.toString(bug_id)
                + "/"+class_name_before_whole[length-1]);
		  
		  Pair<CtElement, ArrayList<PatternInstance>> understudy = elementRepairInfo.get(0);
		  ArrayList<PatternInstance> patterninstances = understudy.getRight();  
		  
		  List<Pair<CtElement, ArrayList<String>>> detailedrepairinfo = new ArrayList<Pair<CtElement, ArrayList<String>>>();
		  
		  for(int index=0; index<patterninstances.size();index++) {
			  AbstractSynthesizer synthesizer=null;
			  PatternInstance pattern=patterninstances.get(index);
			  CtElement faultyelement = pattern.getFaulty().get(0);
			  ArrayList<String> newcode = new ArrayList<String>();
			  
			  if(pattern.getPatternName().equals("constChange")) {
				  synthesizer = new ConstantRepSynthesizer(faultyelement, TargetedRepairTran.Constant_Rep);
			  } else if(pattern.getPatternName().equals("binOperatorModif")) {
				  synthesizer = new BinOperatorRepSynthesizer(faultyelement, TargetedRepairTran.BinOper_Rep);
			  } else if(pattern.getPatternName().equals("expLogicReduce")) {
				  synthesizer = new LogicalExpReduceSynthesizer(faultyelement, TargetedRepairTran.LogExp_Red);
			  } else if(pattern.getPatternName().equals("unwrapMethod")) {
				  synthesizer = new UnwrapMethodSynthesizer(faultyelement, TargetedRepairTran.Unwrap_Meth);
			  } else if(pattern.getPatternName().equals("wrongVarRef")) {	  
				  String property= pattern.getMetadata().stream()
							.map(PropertyPair::getValue).collect(Collectors.joining("_"));
				  if(property.equals("Update_VariableRead")||property.equals("Update_FieldRead")||
						  property.equals("Update_TypeAccess")||property.equals("Removed_FieldRead_Added_VariableRead")
						  ||property.equals("Removed_VariableRead_Added_FieldRead")||property.equals("Update_FieldWrite")
						  ||property.equals("Update_VariableWrite")||property.equals("Removed_VariableRead_Added_Literal")
						  ||property.equals("Removed_FieldRead_Added_Literal")||property.equals("Removed_VariableRead_Added_VariableRead")
						  ||property.equals("Removed_VariableWrite_Added_FieldWrite")||property.equals("Removed_TypeAccess_Added_VariableRead")
						  ||property.equals("Removed_FieldRead_Added_TypeAccess")||property.equals("Removed_TypeAccess_Added_FieldRead")
						  ||property.equals("Removed_VariableRead_Added_TypeAccess")||property.equals("Removed_FieldWrite_Added_VariableWrite")
						  ||property.equals("Removed_FieldRead_Added_FieldRead")||property.equals("Removed_TypeAccess_Added_TypeAccess")
						  ||property.equals("Removed_SuperAccess_Added_FieldRead")||property.equals("Removed_VariableWrite_Added_VariableRead")
						  ||property.equals("Removed_VariableRead_Added_VariableWrite")||property.equals("Removed_VariableRead_Added_FieldWrite")
						  ||property.equals("Removed_TypeAccess_Added_Literal")) {
					  synthesizer = new VarRwVarSynthesizer(faultyelement, TargetedRepairTran.Var_RW_Var);
				  } else if(property.equals("Removed_FieldRead_Added_Invocation")||property.equals("Removed_VariableRead_Added_Invocation")||
						  property.equals("Removed_VariableRead_Added_ConstructorCall")||property.equals("Removed_TypeAccess_Added_Invocation")
						  ||property.equals("Removed_FieldRead_Added_ConstructorCall")||property.equals("Removed_FieldWrite_Added_Invocation")
						  ||property.equals("Removed_VariableWrite_Added_Invocation")) {
					  synthesizer = new VarRwMethodSynthesizer(faultyelement, TargetedRepairTran.Var_RW_Meth);
				  }  
			  }	else if(pattern.getPatternName().equals("wrongMethodRef")) {	  
				  String property= pattern.getMetadata().stream()
							.map(PropertyPair::getValue).collect(Collectors.joining("_"));
				  if(property.equals("SameNamedifferentArgument")||property.equals("differentMethodName")||
						  property.equals("Removed_ConstructorCall_Added_Invocation")||property.equals("Removed_Invocation_Added_Invocation")
						  ||property.equals("Removed_Invocation_Added_ConstructorCall")||property.equals("Removed_ConstructorCall_Added_ConstructorCall")
						  ||property.equals("Removed_NewClass_Added_Invocation")) {
					  synthesizer = new MethodRwMethodSynthesizer(faultyelement, TargetedRepairTran.Meth_RW_Meth);
				  } else if(property.equals("Removed_Invocation_Added_VariableRead")||property.equals("Removed_Invocation_Added_FieldRead")||
						  property.equals("Removed_ConstructorCall_Added_VariableRead")||property.equals("Removed_Invocation_Added_Literal")
						  ||property.equals("Removed_ConstructorCall_Added_FieldRead")||property.equals("Removed_ConstructorCall_Added_Literal")
						  ||property.equals("Removed_NewClass_Added_FieldRead")||property.equals("Removed_ConstructorCall_Added_TypeAccess")||
						  property.equals("Removed_Invocation_Added_TypeAccess")||property.equals("Removed_ConstructorCall_Added_VariableWrite")
						  ) {
					  synthesizer = new MethodRwVarSynthesizer(faultyelement, TargetedRepairTran.Meth_RW_Var);
				  }  
			  }	
			  
			  if(synthesizer!=null) {
				  synthesizer.clearNewCode();
				  synthesizer.synthesize();
				  newcode=synthesizer.getNewCode();
				  detailedrepairinfo.add(Pair.of(faultyelement, newcode));
			  }	  
		  }

		  int size= detailedrepairinfo.size();
		  List<Pair<CtElement, String>> detailedinfoforrepair = new ArrayList<Pair<CtElement, String>>();
		  if(size==1) {

			  Pair<CtElement, ArrayList<String>> repairinfo = detailedrepairinfo.get(0);
			  CtElement elementstudy = repairinfo.getLeft();
			  ArrayList<String> newcodestring= repairinfo.getRight();  
			  for(int sizeindex=0; sizeindex<newcodestring.size(); sizeindex++) {
				  detailedinfoforrepair.clear();
				  detailedinfoforrepair.add(Pair.of(elementstudy, newcodestring.get(sizeindex)));

				  if(repair(changedclass,  project,  bug_id, detailedinfoforrepair))
				  {
					  System.out.println("A plausiable patch has been found for "+project.toString()+"_"
				        +Integer.toString(bug_id)+":"+" Replace "+elementstudy.toString()+" in Line "+elementstudy.getPosition().getLine()+
				        " of File "+changedclass+" with "+newcodestring.get(sizeindex));
					  break;
				  }
				  
				  if(sizeindex==(newcodestring.size()-1))
					  System.out.println("No plausiable patch has been found for "+project.toString()+"_"
						        +Integer.toString(bug_id));
			  }
		  } else if(size==2) {
			  Pair<CtElement, ArrayList<String>> repairinfo0 = detailedrepairinfo.get(0);
			  CtElement elementstudy0 = repairinfo0.getLeft();
			  ArrayList<String> newcodestring0= repairinfo0.getRight();  

			  Pair<CtElement, ArrayList<String>> repairinfo1 = detailedrepairinfo.get(1);
			  CtElement elementstudy1 = repairinfo1.getLeft();
			  ArrayList<String> newcodestring1= repairinfo1.getRight();  
			  boolean whetherfoundpatch = false;

			  for(int outter=0; outter<newcodestring0.size(); outter++) {
				  Pair<CtElement, String> detailedinfoforrepair0=Pair.of(elementstudy0, newcodestring0.get(outter));
				  for(int inner=0; inner<newcodestring1.size();inner++) {
					  Pair<CtElement, String> detailedinfoforrepair1=Pair.of(elementstudy1, newcodestring1.get(inner));
					  detailedinfoforrepair.clear();
					  detailedinfoforrepair.add(detailedinfoforrepair0);
					  detailedinfoforrepair.add(detailedinfoforrepair1);
					  
					  if(repair(changedclass,  project,  bug_id, detailedinfoforrepair))
					  {
						  System.out.println("A plausiable patch has been found for "+project.toString()+"_"
					        +Integer.toString(bug_id)+":"+" Replace "+elementstudy0.toString()+" in Line "+elementstudy0.getPosition().getLine()+
					        " of File "+changedclass+" with "+newcodestring0.get(outter)+" and "+" Replace "+elementstudy1.toString()+" in Line "+elementstudy1.getPosition().getLine()+
					        " of File "+changedclass+" with "+newcodestring1.get(inner));
						  whetherfoundpatch = true;
						  break;
					  }
				  }
				  
				  if(whetherfoundpatch)
					  break;
				  
				  if(outter==(newcodestring0.size()-1))
					  System.out.println("No plausiable patch has been found for "+project.toString()+"_"
						        +Integer.toString(bug_id));
			  }
		  } 
	}
	
	public static boolean repair(String changedclass, String project, int bug_id, List<Pair<CtElement, String>> detailedinfoforrepair) {
		String[] class_name_before_whole = changedclass.split("\\.");
		int length = class_name_before_whole.length; 

	    String repairdirectorybase = Config.projectsRoot+"/"+project.toLowerCase()+"/"+project.toLowerCase()
	                              +"_"+Integer.toString(bug_id);	      
	    String class_name = changedclass.replace(".", "/") + ".java";
		String sourcedir = getProjectSource(project, bug_id);
		String originalpathbefore = repairdirectorybase + "/" + sourcedir + "/" + class_name;
	//	String newpath=Config.defects4jrepairtemp+"/"+class_name_before_whole[length-1]+"_s.java";
		String newpath=Config.defects4jrepairtemp+"/"+class_name;
		copyfileToDir (originalpathbefore, Config.defects4jtempstore);

		Launcher spoonLauncher = new Launcher();
	    spoonLauncher.addInputResource(originalpathbefore);
	    spoonLauncher.getEnvironment().setNoClasspath(true);
	    spoonLauncher.setSourceOutputDirectory(Config.defects4jrepairtemp);
	    spoonLauncher.addProcessor(new SynthesisProcessor(detailedinfoforrepair));
	    spoonLauncher.getEnvironment().setCommentEnabled(false);
	    spoonLauncher.getEnvironment().setAutoImports(false);
	    spoonLauncher.run();

		replaceFile(originalpathbefore, newpath);
		
		if(compile(project, bug_id) && test(project, bug_id)==0) {
			replaceFile(originalpathbefore, Config.defects4jtempstore+"/"+class_name_before_whole[length-1]+".java");
			return true;
		}
		else {
			replaceFile(originalpathbefore, Config.defects4jtempstore+"/"+class_name_before_whole[length-1]+".java");
			return false;
		}
	}
	
	public static void copyfileToDir (String oldfile, String newfile) {
	    
	    try {
	        FileUtils.copyFileToDirectory(new File(oldfile), new File(newfile));
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public static boolean compile(String project, int bug_id) {
		boolean compilesucessful = true;
		String commandSequence="";
		String pathString="";
		pathString="export PATH="+Config.processPath(System.getenv("PATH"),8)+" && "+"export PATH="+Config.defects4jRoot
				+"/framework/bin:$PATH"+" && "+"cd ";
		commandSequence+=pathString;
		
		String workingdir=Config.projectsRoot+"/"+project.toLowerCase()+"/"+
		                    project.toLowerCase()+"_"+Integer.toString(bug_id);
		commandSequence+=workingdir;
		
		String compilecommand=" && "+"defects4j compile";
		commandSequence+=compilecommand;
		
		commandSequence+=" &> ";
		commandSequence+=System.getProperty("user.home");
		commandSequence+="/out.txt";
		
		String[] cmd = { "/bin/bash", "-c", commandSequence};
	    ProcessBuilder pb = new ProcessBuilder(cmd);
	    pb.redirectErrorStream(true);
        Process p = null;
        try {
            pb.inheritIO();
            p = pb.start();
            p.waitFor();

			FileInputStream fstream = new FileInputStream(System.getProperty("user.home")+"/out.txt");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
				String strLine;
				while ((strLine = br.readLine()) != null) {
					if(strLine.endsWith("FAIL")) {
						compilesucessful = false;
						return compilesucessful;
					}
				}
			}
			fstream.close();
        } catch (IOException e1) {
        	compilesucessful = false;
            e1.printStackTrace();
        } catch (InterruptedException e) {
        	compilesucessful = false;
            e.printStackTrace();
        } 	      
        return compilesucessful;
	}
	
	public static int test(String project, int bug_id) {
		int failingnumber = 0;
		String commandSequence="";
		String pathString="";
		pathString="export PATH="+Config.processPath(System.getenv("PATH"),8)+" && "+"export PATH="+Config.defects4jRoot
				+"/framework/bin:$PATH"+" && "+"cd ";
		commandSequence+=pathString;
		
		String workingdir=Config.projectsRoot+"/"+project.toLowerCase()+"/"+
		                    project.toLowerCase()+"_"+Integer.toString(bug_id);
		commandSequence+=workingdir;
		
		String compilecommand=" && "+"defects4j test";
		commandSequence+=compilecommand;
		
		commandSequence+=" &> ";
		commandSequence+=System.getProperty("user.home");
		commandSequence+="/out.txt";
		
		String[] cmd = { "/bin/bash", "-c", commandSequence};
	    ProcessBuilder pb = new ProcessBuilder(cmd);
	    pb.redirectErrorStream(true);
        Process p = null;
        try {
            pb.inheritIO();
            p = pb.start();
            p.waitFor();

			FileInputStream fstream = new FileInputStream(System.getProperty("user.home")+"/out.txt");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
				String strLine;
				while ((strLine = br.readLine()) != null) {
					if(strLine.startsWith("Failing tests:")) {
                        String[] splitted = strLine.split(" ");	
                        failingnumber = Integer.parseInt(splitted[splitted.length-1]);
						return failingnumber;
					}
				}
			}
			fstream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 	      
        return failingnumber;
	}
	
	public static void replaceFile (String oldfile, String newfile) {
		
		newfile=newfile.replace("_s", "");
		Path sourceFile = Paths.get(newfile);
        Path targetFile = Paths.get(oldfile);

        //copy source to target using Files Class
        try {
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
	}
	
	public static ArrayList<String> getChangedClass(String project, int bug_id) {
		
		String commandSequence="";
		String pathString="";
		pathString="export PATH="+Config.processPath(System.getenv("PATH"),8)+" && "+"export PATH="+Config.defects4jRoot
				+"/framework/bin:$PATH"+" && "+"defects4j export -p classes.modified -w ";
		commandSequence+=pathString;
		
		String workingdir=Config.projectsRootfix+"/"+project.toLowerCase()+"/"+
		                    project.toLowerCase()+"_"+Integer.toString(bug_id);
		commandSequence+=workingdir;
		commandSequence+=" 1> ";
		commandSequence+=System.getProperty("user.home");
		commandSequence+="/out.txt";
		
		ArrayList<String> changedClasses = new ArrayList<String>();
		String[] cmd = { "/bin/bash", "-c", commandSequence};
	    ProcessBuilder pb = new ProcessBuilder(cmd);
	    pb.redirectErrorStream(true);
        Process p = null;
        try {
            pb.inheritIO();
            p = pb.start();
            p.waitFor();

			FileInputStream fstream = new FileInputStream(System.getProperty("user.home")+"/out.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				changedClasses.add(strLine);
			}
			fstream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 	       
        return changedClasses;
	}	
	
   public static String getProjectSource(String project, int bug_id) {
		
		String commandSequence="";
		String pathString="";
		pathString="export PATH="+Config.processPath(System.getenv("PATH"),8)+" && "+"export PATH="+Config.defects4jRoot
				+"/framework/bin:$PATH"+" && "+"defects4j export -p dir.src.classes -w ";
		commandSequence+=pathString;
		
		String workingdir=Config.projectsRootfix+"/"+project.toLowerCase()+"/"+
		                    project.toLowerCase()+"_"+Integer.toString(bug_id);
		commandSequence+=workingdir;
		commandSequence+=" 1> ";
		commandSequence+=System.getProperty("user.home");
		commandSequence+="/out.txt";
		
		String sourcedir = "";
		String[] cmd = { "/bin/bash", "-c", commandSequence};
	    ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = null;
        try {
            pb.inheritIO();
            p = pb.start();
            p.waitFor();
            
			FileInputStream fstream = new FileInputStream(System.getProperty("user.home")+"/out.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				sourcedir=strLine;
			}
			fstream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 	 
        return sourcedir;
	}	
	
	public static void copyFileBTweenDirectories(String changedclass, String projectname, int bug_id) {

		String class_name = changedclass.replace(".", "/") + ".java";
		String sourcedir = getProjectSource(projectname, bug_id);
		
		String[] class_name_before_whole = changedclass.split("\\.");
	    int length = class_name_before_whole.length; 
	    String class_name_before = class_name_before_whole[length-1] + "_s.java";
	    
	    String class_name_after = class_name_before_whole[length-1] + "_t.java";
		
		String originalpathbefore = Config.projectsRoot+"/"+ projectname.toLowerCase()+"/"+
				projectname.toLowerCase()+"_"+Integer.toString(bug_id)+ "/" + sourcedir + "/" + class_name;
		String destinationpathbefore = Config.defects4jdiffraw + "/"+ projectname.toLowerCase()+"_"+Integer.toString(bug_id)
		                         + "/"+class_name_before_whole[length-1]+"/"+class_name_before;
		
		String originalpathafter = Config.projectsRootfix+"/"+ projectname.toLowerCase()+"/"+
				projectname.toLowerCase()+"_"+Integer.toString(bug_id)+ "/" + sourcedir + "/" + class_name;
		String destinationpathafter = Config.defects4jdiffraw + "/"+ projectname.toLowerCase()+"_"+Integer.toString(bug_id)
		                         + "/"+class_name_before_whole[length-1]+"/"+class_name_after;
		
		File sourceDirectorybefore = new File(originalpathbefore);
		File destinationDirectorybefore = new File(destinationpathbefore);
		
		File sourceDirectoryafter = new File(originalpathafter);
		File destinationDirectoryafter = new File(destinationpathafter);
		try {
			FileUtils.copyFile(sourceDirectorybefore, destinationDirectorybefore);
			FileUtils.copyFile(sourceDirectoryafter, destinationDirectoryafter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
