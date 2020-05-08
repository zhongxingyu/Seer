 package pcc.test.vercon;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import pcc.analysis.ChangeCounterUtils;
 import pcc.vercon.*;
 
 public class Driver {
 	public static void main(String[] args) throws IOException{
 		ArrayList<String>	filenames	=	new ArrayList<String> ();
 		HashMap<String, String[]> filesAndLines	=	new HashMap<String, String[]> ();
 		
 		filenames.add("a.txt");
 		filenames.add("b.txt");
 		
 		String [] alines	=	new String[]{"a-begin", "blah", "blah2", "a-end"};
 		String [] blines	=	new String[]{"b-begin", "blah", "blah2", "b-end"};
 	
 		filesAndLines.put(filenames.get(0), alines);
 		filesAndLines.put(filenames.get(1), blines);
 
 
		ProjectVersion version1 = new ProjectVersion ("testing", "1","me", "testing", filenames);
 		//version1.setLinesForFiles(filesAndLines);
 
 		// different lines for version 2
 		alines[1]			=	"boogie";
 		String[] blinesAdded	=	new String[]{"b-begin", "blah", "blah2", "blah3", "b-end"};
 		
 		filesAndLines.put(filenames.get(0), alines);
 		filesAndLines.put(filenames.get(1), blinesAdded);
 
 		
		ProjectVersion version2 = new ProjectVersion ("testing", "2","me", "testing 2", filenames);
 		//version2.setLinesForFiles(filesAndLines);
 		
 		System.out.println("### Export Change Lables ###");
 		ChangeCounterUtils.exportChangeLabels("testAnalysis", version1, version2);
 		
 		System.out.println("### Get Line Changes Report ###");
 		String report = ChangeCounterUtils.getLineChangesReport(version1, version2);
 		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		System.out.println(report);
 		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		
 		System.out.println("### Get LLOC ###");
 		int lloc = ChangeCounterUtils.getLLOC(version1);
 		System.out.println("LLOC: " + lloc);
 		
 		System.out.println("### Get LOC Changes ###");
 		report = ChangeCounterUtils.getLLOCChanges(version1, version2);
 		System.out.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		System.out.println(report);
 		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 	}
 }
