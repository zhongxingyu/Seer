 package callgraphanalyzer;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import models.CallGraph;
 import models.CallGraph.MethodPercentage;
 import models.Change;
 import models.Method;
 import models.Relation;
 import models.User;
 import callgraphanalyzer.Comparator.CompareResult;
 import db.CallGraphDb;
 
 public class CallGraphAnalyzer
 {
 
 	private CallGraphDb		db;
 	private Comparator		comparator;
 
 	private Set<Relation>	Relations;
 
 	public CallGraphAnalyzer()
 	{
 		this.Relations = new HashSet<Relation>();
 	}
 
 	public void init(Comparator comp)
 	{
 		this.comparator = comp;
 		this.db = comp.db;
 	}
 
 	/** 
 	 * This ads ownership to the callGraph.
 	 */
 	public void generateLogicalOwnership()
 	{
 		// Go through all of the changes and in parallel update each callgraph.
 		List<Change> changes = db.getAllOwnerChangesBefore(this.comparator.newCommit.getCommit_id());
 		boolean updatingOld = true;
 		for (Change c : changes)
 		{
 			// todo update callgraphs
 			this.comparator.newCallGraph.updateOwnership(c);
 			if (updatingOld)
				this.comparator.oldCallGraph.updateOwnership(new Change(c));
 			if (c.getCommitId().equals(this.comparator.oldCommit.getCommit_id()))
 				updatingOld = false;
 		}
 		this.comparator.newCallGraph.print();
 	}
 
 	/**
 	 * This function will return all the methods that call the given method.
 	 * 
 	 * @param method
 	 * @param callGraph
 	 * @return
 	 */
 	private List<Method> retrieveMethodCalls(String method, CallGraph callGraph)
 	{
 		Method m = callGraph.containsMethod(method);
 		return m.getCalledBy();
 	}
 
 	/**
 	 * Generates the network.
 	 * @return The Set<Relations> that make up the network. 
 	 */
 	public void generateRelationships()
 	{
 		// For each modifiedFile, for each generate an owner for the method and
 		// the % they own.
 		CompareResult compareResult = comparator.getCompareResult();
 		for (String modifiedFile : compareResult.modifiedFileMethodMap.keySet())
 		{
 			// For each method in the new methods.
 			for (MethodPercentage newMethod : compareResult.modifiedFileMethodMap.get(modifiedFile).newMethods)
 			{
 				// get all methods this one is called by
 				Change newMethodChange = db.getLatestOwnerChange(modifiedFile, newMethod.getMethod().getstartChar(), newMethod.getMethod()
 						.getendChar(), comparator.newCommit.getCommit_date());
 				recurseMethods(new User(newMethodChange.getOwnerId()), newMethod.getMethod(), newMethod.getPercentage(), 0);
 			}
 			for (MethodPercentage oldMethod : compareResult.modifiedFileMethodMap.get(modifiedFile).oldMethods)
 			{
 				if (compareResult.modifiedFileMethodMap.get(modifiedFile).newMethods.contains(oldMethod))
 					continue;
 				// get all methods this one is called by
 				Change newMethodChange = db.getLatestOwnerChange(modifiedFile, oldMethod.getMethod().getstartChar(), oldMethod
 						.getMethod().getendChar(), comparator.oldCommit.getCommit_date());
 				recurseMethods(new User(newMethodChange.getOwnerId()), oldMethod.getMethod(), oldMethod.getPercentage(), 0);
 			}
 		}
 		compareResult.print();
 		for (Relation r : this.Relations)
 			r.print();
 	}
 
 	public void recurseMethods(User changingUser, Method currentMethod, float percentage, int currentDepth)
 	{
 		for (Method calledMethod : currentMethod.getCalledBy())
 		{
 			Change calledMethodChange = db.getLatestOwnerChange(calledMethod.getClazz().getFile().getFileName(),
 					calledMethod.getstartChar(), calledMethod.getendChar(), comparator.newCommit.getCommit_date());
 			boolean isSelf = false;
 			if (changingUser.getUserEmail().equals(calledMethodChange.getOwnerId()))
 				isSelf = true;
 			Relation r = new Relation(new User(changingUser.getUserEmail()), 
 					new User(calledMethodChange.getOwnerId()),
 					isSelf,
 					calledMethod.getClazz().getFile().getFileName(),
 					currentMethod.getName(),
 					calledMethod.getName(),
 					percentage*(calledMethod.getClazz().getFile().getMethodWeight(calledMethodChange.getOwnerId(), calledMethod) /(currentDepth+1)));
 			this.Relations.add(r);
 			recurseMethods(changingUser, calledMethod, percentage, currentDepth + 1);
 		}
 	}
 }
