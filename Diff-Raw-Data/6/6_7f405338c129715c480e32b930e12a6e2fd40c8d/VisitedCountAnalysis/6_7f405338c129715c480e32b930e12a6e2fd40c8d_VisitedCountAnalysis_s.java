 /**
  * 
  */
 package chord.analyses.stats;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import joeq.Class.jq_Class;
 import joeq.Class.jq_Method;
 import chord.doms.DomM;
 import chord.instr.InstrScheme;
 import chord.project.Chord;
 import chord.project.OutDirUtils;
 import chord.project.analyses.DynamicAnalysis;
 
 /**
  * @author omert
  *
  */
 @Chord(name = "dynamic-visitcnt-java")
 public class VisitedCountAnalysis extends DynamicAnalysis {
 	
 	private final static String[] LIB_PREFIXES = new String[] {
 		"sun.",
 		"com.sun.",
 		"com.ibm.jvm.",
 		"com.ibm.oti.",
 		"com.ibm.misc.",
 		"org.apache.harmony.",
 		"joeq.",
 		"jwutil.",
 		"java.",
 		"javax."
 	};
 	
 	private InstrScheme instrScheme;
 	private DomM domM;
 	private final Set<jq_Method> visitedMethods = new HashSet<jq_Method>(16);
 	private final Set<jq_Method> visitedAppMethods = new HashSet<jq_Method>(16);
 	private final Set<jq_Class> visitedClasses = new HashSet<jq_Class>(16);
 	private final Set<jq_Class> visitedAppClasses = new HashSet<jq_Class>(16);
 	private long totalBytecode = 0;
 	private long totalAppBytecode = 0;
 
 	public InstrScheme getInstrScheme() {
 		if (instrScheme != null) return instrScheme;
 		instrScheme = new InstrScheme();
 		instrScheme.setEnterAndLeaveMethodEvent();
 		return instrScheme;
 	}
 	
 	public void initAllPasses() {
 		domM = instrumentor.getDomM();
 	}
 	
     public void processEnterMethod(int m, int t) {
 		if (m >= 0) {
 			jq_Method mthd = domM.get(m);
 			visitedMethods.add(mthd);
 			jq_Class klass = mthd.getDeclaringClass();
 			visitedClasses.add(klass);
 			byte[] bytecode = mthd.getBytecode();
 			totalBytecode += bytecode.length;
 			if (isAppMethod(mthd)) {
 				visitedAppMethods.add(mthd);
 				visitedAppClasses.add(klass);
 				totalAppBytecode += bytecode.length;
 			}
 		}
     }
     
     public void processLeaveMethod(int m, int t) {
     	/* Do nothing. */
     }
     
     private boolean isAppMethod(jq_Method m) {
    	String name = m.getName().toString();
     	for (String pref : LIB_PREFIXES) {
    		if (name.startsWith(pref)) {
     			return false;
     		}
     	}
     	return true;
     }
     
     public void doneAllPasses() {
     	OutDirUtils.logOut("=== # of visited classes: %d ===", visitedClasses.size());
 		OutDirUtils.logOut("=== # of visited app. classes: %d ===", visitedAppClasses.size());
 		OutDirUtils.logOut("=== # of visited methods: %d ===", visitedMethods.size());
 		OutDirUtils.logOut("=== # of visited app. methods: %d ===", visitedAppMethods.size());
 		OutDirUtils.logOut("=== Total bytecode: %d ===", totalBytecode);
 		OutDirUtils.logOut("=== Total app. bytecode: %d ===", totalAppBytecode);
 	}
 }
