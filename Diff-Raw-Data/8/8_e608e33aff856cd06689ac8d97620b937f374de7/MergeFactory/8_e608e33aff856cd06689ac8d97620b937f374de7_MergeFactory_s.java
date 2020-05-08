 package controller.merge.xmi.xclass;
 
 import java.util.ArrayList;
 
 public class MergeFactory {
 /***
  * MergeFactory - Returns an instance of an Merge object depending on
  * the merging type. 
  * 
  */
 	
 	private static MergeFactory mergeFac = null;
 	private ArrayList<String> mergeLookUpTable = new ArrayList<String>();
 	
 	private MergeFactory(){		
 		mergeLookUpTable.add("xmiClassDiagram");
 		mergeLookUpTable.add("ecoreClassDiagram");
 		mergeLookUpTable.add("xmiSequenceDiagram");
 	}
 	
 	public static MergeFactory getInstance(){
 		if(mergeFac == null){
 			synchronized(MergeFactory.class){
 				if(mergeFac == null){
 					mergeFac = new MergeFactory();
 				}
 			}
 		}
 		return mergeFac;
 	}
 	
 	public MergerIntf createMerge(String diagramType)
 	{
 		MergerIntf merger = null;
 		if (!mergeLookUpTable.contains(diagramType))
 		{
 			return merger;
 		}
 		else
 		{
 			if(diagramType.equals("xmiClassDiagram"))
 			{
				merger = new XmiMergedClass();
 			}
 			else if(diagramType.equals("ecoreClassDiagram"))
 			{
				merger = new EcoreMergedClass();
 			}
 			else if(diagramType.equals("xmiSequenceDiagram"))
 			{
				merger = new SequenceMergedClass();
 			}
 		}
 		return merger;
 	}
 	
 }
