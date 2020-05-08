 package callgraphanalyzer;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import models.CallGraph;
 import models.Commit;
 import models.Method;
 import models.User;
 import parser.Parser;
 import parser.Resolver;
 import db.CallGraphDb;
 import differ.filediffer;
 import differ.filediffer.diffObjectResult;
 
 public class Comparator
 {
 	public class OwnedMethod 
 	{
 		public Method ownedMethod;
 		public User owner;
 		public int ownedStart;
 		public int ownedEnd;
 		
 		public OwnedMethod(User owner, Method method)
 		{
 			this.ownedMethod = method;
 			this.owner = owner;
 		}
 	}
 	public class ModifiedMethod
 	{
 		public ModifiedMethod(Set<Method> oldM, Set<Method> newM)
 		{
 			this.oldMethods = oldM;
 			this.newMethods = newM;
 		}
 
 		public Set<Method>	oldMethods	= new HashSet<Method>();
 		public Set<Method>	newMethods	= new HashSet<Method>();
 	}
 
 	public class CompareResult
 	{
 		public CompareResult()
 		{
 		}
 
 		public void clear()
 		{
 			addedFiles.clear();
 			deletedFiles.clear();
 			modifiedFileMethodMap.clear();
 			modifiedBinaryFiles.clear();
 		}
 
 		public void print()
 		{
 			for (String file : addedFiles)
 				System.out.println("+\t" + file);
 
 			for (String file : deletedFiles)
 				System.out.println("-\t" + file);
 
 			for (String file : modifiedBinaryFiles.keySet())
 			{
 				String commitID = modifiedBinaryFiles.get(file);
 				System.out.println("+-[BIN]\t" + file + " in " + commitID);
 			}
 
 			for (String file : modifiedFileMethodMap.keySet())
 			{
 				ModifiedMethod methods = modifiedFileMethodMap.get(file);
 				System.out.println("+-\t" + file);
 				for (Method mo : methods.oldMethods)
 					System.out
 							.println("\tModified old method: " + mo.getName());
 				for (Method mn : methods.newMethods)
 					System.out
 							.println("\tModified new method: " + mn.getName());
 			}
 		}
 
 		public Set<String>					addedFiles				= new HashSet<String>();
 		public Set<String>					deletedFiles			= new HashSet<String>();
 		public Map<String, ModifiedMethod>	modifiedFileMethodMap	= new HashMap<String, ModifiedMethod>();
 		public Map<String, String>			modifiedBinaryFiles		= new HashMap<String, String>();
 	}
 
 	public CallGraphDb				db;
 	private filediffer				differ;
 	public CallGraph				newCallGraph;
 	public CallGraph				oldCallGraph;
 	public Map<String, String>		FileMap;
 	public Map<String, String>		newCommitFileTree;
 	public Map<String, String>		oldCommitFileTree;
 	public Map<String, String>		libraryFileTree;
 	public Map<String, Set<String>>	commitsInBetween;
 
 	private CompareResult			compareResult	= new CompareResult();
 
 	public Commit					newCommit;
 	public Commit					oldCommit;
 	public String					CurrentBranch;
 	public String					CurrentBranchID;
 
 	/**
 	 * Constructs a new Comparator class. This class connects the FileDiffer
 	 * {@link #differ} and CallGraph {@link #newCallGraph}
 	 * 
 	 * @param db
 	 *            Name of the Database.
 	 * @param CommitIDOne
 	 *            SHA-1 Hash of the commit in question.
 	 * @param CommitIDTwo
 	 *            SHA-1 Hash of the second commit.
 	 */
 	public Comparator(CallGraphDb db, String CommitIDOne, String CommitIDTwo)
 	{
 		this.db = db;
 
 		// Figure out which commit is newer
 		Commit first = db.getCommit(CommitIDOne);
 		Commit second = db.getCommit(CommitIDTwo);
 		if (first.getCommit_date().compareTo(second.getCommit_date()) > 0)
 		{
 			this.newCommit = first;
 			this.oldCommit = second;
 			this.newCommitFileTree = this.getFilesTreeForCommit(CommitIDOne);
 			this.oldCommitFileTree = this.getFilesTreeForCommit(CommitIDTwo);
 		}
 		else
 		{
 			this.newCommit = second;
 			this.oldCommit = first;
 			this.newCommitFileTree = this.getFilesTreeForCommit(CommitIDTwo);
 			this.oldCommitFileTree = this.getFilesTreeForCommit(CommitIDOne);
 		}
 
 		// check and create our owners.
 		this.newCallGraph = generateCallGraph(this.newCommitFileTree);
 		this.oldCallGraph = generateCallGraph(this.oldCommitFileTree);
 
 		// get all the commits exist between the two commits and the newer
 		// commit
 		this.commitsInBetween = db.getCommitsBeforeAndAfterChanges(
 				this.newCommit.getCommit_id(), this.oldCommit.getCommit_id(),
 				false, false);
 		this.commitsInBetween.put(first.getCommit_id(), db
 				.getChangedFilesFromCommit(first.getCommit_id()));
 	}
 
 	/**
 	 * Generate Callgraph from a commitFileTree
 	 * 
 	 * @param commitFileTree
 	 *            map of file name and path from a commit
 	 * @return CallGraph a resolved CallGraph
 	 */
 	public CallGraph generateCallGraph(Map<String, String> commitFileTree)
 	{	
 		CallGraph callGraph = new CallGraph();
 		Parser parser = new Parser(callGraph);
 
 		for (String key : commitFileTree.keySet())
 		{
 			if (!key.endsWith(".java")) // Currently don't care about non-java
 										// files in our callgraph
 				continue;
 			parser.parseFileFromString(key, db.getRawFile(key, commitFileTree
 					.get(key)));
 		}
 		
 		// Get Java util
 		CallGraphDb libraryDB = new CallGraphDb();
 		libraryDB.connect("JavaLibraries");
 		libraryDB.setBranchName("master");
 		this.libraryFileTree = this.getFilesTreeForCommit("e436a78a73f967d47aebd02ac58677255bbec125");
 		
 		for (String key : libraryFileTree.keySet())
 		{
 			if (!key.endsWith(".java")) // Currently don't care about non-java
 				continue;
			parser.parseFileFromString(key, libraryDB.getRawFile(key, libraryFileTree
 					.get(key)));
 		}
 
 		System.out.println();
 		System.out.println();
 		System.out.println("Resolving the fuck out of this CallGraph");
 
 		Resolver resolver = new Resolver(callGraph);
 		resolver.resolveAll();
 
 		callGraph.print();
 		return callGraph;
 	}
 
 	public boolean CompareCommits()
 	{
 		Set<String> binaryFiles = db.getBinaryFiles();
 		this.compareResult.clear();
 
 		// For every file in the new commit tree
 		for (String newKey : newCommitFileTree.keySet())
 		{
 			// If the file exists in the old commit
 			if (oldCommitFileTree.containsKey(newKey))
 			{
 				// File is still present, might be modified.
 				// Check if its a binary file, ignore
 				if (binaryFiles.contains(newKey))
 				{
 					// check if this binary file has changed
 					String commitID = getCommitHasChangedBinaryFile(newKey);
 					if (!commitID.isEmpty())
 						this.compareResult.modifiedBinaryFiles.put(newKey,
 								commitID);
 					continue;
 				}
 				else
 				{
 					// Non-binary files, use differ to compare them.
 					String oldRaw = db.getRawFile(newKey, oldCommitFileTree
 							.get(newKey));
 					String newRaw = db.getRawFile(newKey, newCommitFileTree
 							.get(newKey));
 
 					differ = new filediffer(oldRaw, newRaw);
 					differ.diffFilesLineMode();
 
 					// The file was modified (+-) since the old commit.
 					if (differ.isModified())
 					{
 						// return the change sets from the two files
 						List<diffObjectResult> deleteObjects = differ
 								.getDeleteObjects();
 						List<diffObjectResult> insertObjects = differ
 								.getInsertObjects();
 
 						// figure out which function has changed
 						getModifiedMethodsForFile(newKey, deleteObjects,
 								insertObjects);
 					}
 				}
 			}
 			else
 			{
 				// The file was added (+) since the old commit.
 				this.compareResult.addedFiles.add(newKey);
 			}
 		}
 		for (String oldKey : oldCommitFileTree.keySet())
 		{
 			if (!newCommitFileTree.containsKey(oldKey))
 			{
 				// The file was deleted from the old tree
 				this.compareResult.deletedFiles.add(oldKey);
 			}
 		}
 
 		print();
 		return true;
 	}
 
 	/**
 	 * get all methods in a file that might be changed from new commit to other
 	 * 
 	 * @param fileName
 	 *            file path
 	 * @param callgraph
 	 * @param diffs
 	 *            list of diff objects
 	 * @return
 	 */
 	public void getModifiedMethodsForFile(String fileName,
 			List<diffObjectResult> deleteDiffs,
 			List<diffObjectResult> insertDiffs)
 	{
 		// TODO @braden write a wrapper around method here and include the diffs
 		Set<Method> newMethods = new HashSet<Method>();
 		Set<Method> oldMethods = new HashSet<Method>();
 
 		// methods from old file version+
 		for (diffObjectResult diff : deleteDiffs)
 		{
 			List<Method> changedMethod = this.oldCallGraph
 					.getMethodsUsingCharacters(fileName, diff.start, diff.end);
 			for (Method m : changedMethod)
 				if (!oldMethods.contains(m))
 					oldMethods.add(m);
 		}
 
 		// methods from new file version
 		for (diffObjectResult diff : insertDiffs)
 		{
 			List<Method> changedMethod = this.newCallGraph
 					.getMethodsUsingCharacters(fileName, diff.start, diff.end);
 			for (Method m : changedMethod)
 				if (!newMethods.contains(m))
 					newMethods.add(m);
 		}
 
 		// Insert to modifiedMethod map
 		if (!this.compareResult.modifiedFileMethodMap.containsKey(fileName))
 		{
 			ModifiedMethod mm = new ModifiedMethod(oldMethods, newMethods);
 			this.compareResult.modifiedFileMethodMap.put(fileName, mm);
 		}
 	}
 
 	/**
 	 * Recursively get the files for a commit, going down from the given commit.
 	 * adding them to @see {@link #newCommitFileTree}
 	 * 
 	 * @param commitID
 	 * @return true when successful
 	 */
 	public Map<String, String> getFilesTreeForCommit(String commitID)
 	{
 		Map<String, String> CommitFileTree = new HashMap<String, String>();
 		Map<String, Set<String>> prevChanges = db.getCommitsBeforeChanges(
 				commitID, false, false);
 		Set<String> requiredFiles = db.getFileStructureFromCommit(commitID); // First
 																				// commit;
 		Iterator<String> i = db.getCommitChangedFiles(commitID).iterator();
 		addFilesFromCommit(i, requiredFiles, CommitFileTree, commitID);
 
 		for (String commit : prevChanges.keySet())
 		{
 			Iterator<String> iter = prevChanges.get(commit).iterator();
 			addFilesFromCommit(iter, requiredFiles, CommitFileTree, commit);
 		}
 		return CommitFileTree;
 	}
 
 	public void addFilesFromCommit(Iterator<String> i,
 			Set<String> requiredFiles, Map<String, String> CommitFileTree,
 			String commit)
 	{
 		String currentChangedFile;
 		while (i.hasNext())
 		{
 			currentChangedFile = i.next();
 			System.out.println("Commit :" + commit + " changed file "
 					+ currentChangedFile);
 			if (requiredFiles.contains(currentChangedFile)
 					&& !CommitFileTree.containsKey(currentChangedFile))
 			{
 				CommitFileTree.put(currentChangedFile, commit);
 			}
 		}
 	}
 
 	public String getCommitHasChangedBinaryFile(String file)
 	{
 		// If the file was committed sometime between newCommit and oldCommit,
 		// there is a change
 		// Search from the newest commit down
 		for (String commit : commitsInBetween.keySet())
 		{
 			Set<String> fileChanged = commitsInBetween.get(commit);
 			if (fileChanged.contains(file))
 				return commit;
 		}
 		return "";
 	}
 
 	public void print()
 	{
 		this.compareResult.print();
 	}
 
 	public CompareResult getCompareResult()
 	{
 		return compareResult;
 	}
 
 }
