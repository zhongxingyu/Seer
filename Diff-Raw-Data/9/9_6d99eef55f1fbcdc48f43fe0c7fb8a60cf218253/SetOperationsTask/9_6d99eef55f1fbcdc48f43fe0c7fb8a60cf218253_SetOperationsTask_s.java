 package edu.ucsf.rbvi.setsApp.internal.tasks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cytoscape.work.AbstractTask;
 import org.cytoscape.work.TaskMonitor;
 import org.cytoscape.work.Tunable;
 import org.cytoscape.work.util.ListSingleSelection;
 
 import edu.ucsf.rbvi.setsApp.internal.CyIdType;
 import edu.ucsf.rbvi.setsApp.internal.SetOperations;
 
 public class SetOperationsTask extends AbstractTask {
 	@Tunable(description="Enter a name for the new set:")
 	public String setName;
 	@Tunable(description="Select name of second set:")
 	public ListSingleSelection<String> seT2;
 	@Tunable(description="Select name of first set:")
 	public ListSingleSelection<String> seT1;
 	private SetsManager sm;
 	private SetOperations operation;
 	
 	public SetOperationsTask(SetsManager setsManager, CyIdType type, SetOperations s) {
 		sm = setsManager;
 		List<String> attr = getSelectedSets(type); 
 		seT1 = new ListSingleSelection<String>(attr);
 		seT2 = new ListSingleSelection<String>(attr);
 		operation = s;
 	}
 	
 	private List<String> getSelectedSets(CyIdType type) {
 		ArrayList<String> selectSet = new ArrayList<String>();
 		List<String> mySetNames = sm.getSetNames();
 		if (type == CyIdType.NODE) {
 			for (String s: mySetNames)
 				if (sm.getType(s) == CyIdType.NODE)
 					selectSet.add(s);
 		}
 		if (type == CyIdType.EDGE) {
 			for (String s: mySetNames)
 				if (sm.getType(s) == CyIdType.EDGE)
 					selectSet.add(s);
 		}
 		return selectSet;
 	}
 	
 	@Override
 	public void run(TaskMonitor arg0) throws Exception {
 		String set1 = seT1.getSelectedValue();
 		String set2 = seT2.getSelectedValue();
 		switch (operation) {
 		case INTERSECT:
 			sm.intersection(setName, set1, set2);
 			break;
 		case DIFFERENCE:
			sm.intersection(setName, set1, set2);
 			break;
 		case UNION:
 			sm.union(setName, set1, set2);
 			break;
 		default:
 			break;
 		}
 	}
 
 }
