 package constantpropagation;
 
 import java.util.*;
 
 import soot.Body;
 import soot.SootMethod;
 import soot.Unit;
 import soot.jimple.JimpleBody;
import soot.jimple.internal.JAssignStmt;
 import soot.toolkits.graph.BlockGraph;
 
 /**
  * Implements the worklist algorithm to execute a constant propagation analysis.
  * 
  * @author Christian Adriano
  *
  */
 
 
 public class Analysis {
 
 	static final String MUST = "MUST";
 	static final String MAY = "MAY";
 	private String type; 
 	
 	HashMap<String,Value> fvList= new HashMap<String,Value>();
 	WorkList workList = new WorkList();
 	BlockGraph graph; //this is initialized by the constructor
 	private Body body;
 	 
 	
 	public Analysis(BlockGraph graph, Body body, String analysisType) {
 		this.graph=graph;
 		this.body=body;
 		this.type = analysisType;	
 	}
 
 	//How to navigate the callGraph? Maybe I can use the UnitIterator itself. Check what the UnitIterator does 
 	//in the presence of loops and ifs.
 	
 	public void obtainPredecessors(){
 		//check this code below, because it uses graph.Block, which has methods to retrieve predecessors of a unit
 		//http://stackoverflow.com/questions/6792305/identify-loops-in-java-byte-code
 	}
 	
 	public void initializeWorkList(){}
 	
 	
 	/** Method iterates through the body of a program to find the free variables, 
 	 * which are the variables at the left hand side of the statements.
 	 * @return array of free variable names as primitive string type.
 	 */
	public HashMap<String, Value> obtainFreeVariables(){
 		
 		fvList= new HashMap<String,Value>();
 		SootMethod m = (SootMethod)body.getMethod();
 		if(m.isConcrete()){
 			JimpleBody jbody = (JimpleBody) m.retrieveActiveBody();
 			Iterator<Unit> unitIt = jbody.getUnits().iterator();
 			while(unitIt.hasNext()){
 				Unit unit = (Unit) unitIt.next();
 				System.out.println("Unit:"+unit.toString());
 				
 				if(unit instanceof soot.jimple.internal.JAssignStmt){
					fvList.put(((JAssignStmt)unit).leftBox.getValue().toString(),new Value(true,this.type));
 				}
 				
 				//else
 				//	System.out.println("Just Unit = "+unit.toString());
 			}
		}
 		
 		return fvList;
 	}
 	
 	public ArrayList meet(){
 		return null;
 	}
 	
 }
