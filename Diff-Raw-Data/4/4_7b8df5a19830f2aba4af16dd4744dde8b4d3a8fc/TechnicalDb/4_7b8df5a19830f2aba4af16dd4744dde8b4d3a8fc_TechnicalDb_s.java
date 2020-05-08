 package db;
 
 import java.io.InputStreamReader;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import models.Change;
 import models.Commit;
 import models.CommitDiff;
 import models.CommitFamily;
 import models.DiffEntry;
 import models.FileCache;
 import models.FileDiff;
 import models.User;
 import models.DiffEntry.diff_types;
 
 import db.util.ISetter;
 import db.util.PreparedStatementExecutionItem;
 import db.util.ISetter.IntSetter;
 import db.util.ISetter.StringSetter;
 
 public class TechnicalDb extends DbConnection
 {
 	protected String branchName = null;
 	protected String branchID = null;
 	
 	/**
 	 * Creates a new technical database from the createTechnicalDb.sql schema.
 	 * Drop database if it already exists.
 	 * @param dbName
 	 * @return true for success
 	 */
 	public boolean createDB(String dbName)
 	{
 		try {
 			// Drop the DB if it already exists
 			String query = "DROP DATABASE IF EXISTS " + dbName;
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, null);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			
 			// First create the DB.
 			if (TechnicalResources.SET_ENC)
 				query = "CREATE DATABASE " + dbName + " ENCODING 'UTF8' TEMPLATE template0 LC_COLLATE 'C' LC_CTYPE 'C';";
 			else
 				query = "CREATE DATABASE " + dbName;
 			ei = new PreparedStatementExecutionItem(query, null);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			
 			// Reconnect to our new database.
 			close();
 			connect(dbName.toLowerCase());
 			
 			// load our schema			
 			runScript(new InputStreamReader(this.getClass().getResourceAsStream("createTechnicalDb.sql")));
 			//--------------------------------------------------------------------------------------
 			// Stored procedure for checking before inserting in a batch.											
 			// http://stackoverflow.com/questions/1109061/insert-on-duplicate-update-postgresql			
 			//--------------------------------------------------------------------------------------
 			query = "CREATE OR REPLACE FUNCTION upsert_owner_rec(c_id varchar(255), s_c_id varchar(255), a_id varchar(255), f_id varchar(255), c_start INT, c_end INT, c_type varchar(12)) RETURNS VOID AS" +
 						"'" +
 						" DECLARE " + 
 							"dummy integer;" + 
 						" BEGIN " +
 							" LOOP " +
 								" select owners.char_start into dummy from owners where commit_id=c_id and source_commit_id=s_c_id and owner_id=a_id and file_id=f_id and char_start=c_start and char_end=c_end and change_type=c_type;" +
 								" IF found THEN " +
 									" RETURN ;" +
 								" END IF;" +
 								" BEGIN " +
 									" INSERT INTO owners VALUES (c_id, s_c_id, a_id, f_id, c_start, c_end, c_type);" +
 									" RETURN; " +
 								" EXCEPTION WHEN unique_violation THEN " +
 								" END; " +
 							" END LOOP;" +
 						" END; " +
 						"'" +
 					" LANGUAGE plpgsql;";
 			ei = new PreparedStatementExecutionItem(query, null);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/***
 	 * Get the branchID of the branch name provided by the user when run scm2pgsql cmd
 	 * @return String branch id
 	 */
 	public String getBranchID() {
 		return branchID;
 	}
 	
 	/***
 	 * Set the main branch ID that the user wants to look at.
 	 * This does not change the database entry
 	 * @param branchID
 	 */
 	public void setBranchID(String branchID) {
 		this.branchID = branchID;
 	}
 
 	/***
 	 * Get the main Branch name
 	 * @return String Branch Name
 	 */
 	public String getBranchName() {
 		return branchName;
 	}
 	
 	/**
 	 * blocking
 	 * 
 	 * Should be called AFTER @see {@link #connect(String)}, as it also does 
 	 * a lookup on the branchID and sets it behind the scenes.
 	 * Also does a lookup in the branches table for 
 	 * @param branchName
 	 */
 	public void setBranchName(String branchName) {
 		this.branchName = branchName;
 		try
 		{
 			String query = "SELECT branch_id from branches where branch_name ~ ? LIMIT 1";
 			ISetter[] params = {new StringSetter(1,branchName)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
 			this.addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ei.getResult().next();
 			setBranchID(ei.getResult().getString(1));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/***
 	 * Help function to find the list of diffs for a file. Given the two commit id and the file name. 
 	 * @param comFam contains the two commit id
 	 * @param comDiffs contains a list of all the commit diffs
 	 * @param fileName
 	 * @return the FileDiff contains list of diffs. Null if it can not find the two commit id
 	 */
 	public FileDiff getFileDiffForCommitFamily(CommitFamily comFam, List<CommitDiff> comDiffs, String fileName)
 	{
 		for(CommitDiff cd: comDiffs)
 		{
 			if(cd.getNew_commit_id().equals(comFam.getChildId()) && 
 			   cd.getOld_commit_id().equals(comFam.getParentId()))
 			{
 				for(FileDiff fd : cd.getFileDiffs())
 				{
 					if(fd.getFile_id().equals(fileName))
 						return fd;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/***
 	 * Help function to find the first insert diff in a list of inserts. The first insert must have their start char greater than a threshold
 	 * @param lastEndChar: threshold index to search after it 
 	 * @param insertList: a list of all the inserts
 	 * @return insert as DiffEntry or Null
 	 */
 	protected DiffEntry getEarliestInsert(int lastEndChar, List<DiffEntry> insertList)
 	{
 		DiffEntry entry = null;
 		for(DiffEntry cur : insertList)
 		{
 			if(cur.getChar_start() >= lastEndChar)
 			{
 				if(entry!=null)
 				{
 					if(entry.getChar_start() >= cur.getChar_start())
 						entry = cur;
 				}
 				else
 				{
 					entry = cur;
 				}
 			}
 		}
 		
 		return entry;
 	}
 	
 	/***
 	 * Help function to find the last delete diff in a list of deletes. The last delete must have their star char lesser than a threshold
 	 * @param lastStartChar: threshold index to search before it
 	 * @param deleteList: a list of all deletes
 	 * @return delete as DiffEntry or Null
 	 */
 	protected DiffEntry getLatestDelete(int lastStartChar, List<DiffEntry> deleteList)
 	{
 		DiffEntry entry = null;
 		for(DiffEntry cur : deleteList)
 		{
 			if(cur.getChar_end() <= lastStartChar)
 			{
 				if(entry!=null)
 				{
 					if(entry.getChar_start() <= cur.getChar_start())
 						entry = cur;
 				}
 				else
 				{
 					entry = cur;
 				}
 			}
 		}
 		
 		return entry;
 	}
 	
 	/**
 	 * Construct raw file for the given fileID, commitID and a commitPath
 	 * @param fileID
 	 * @param commitID
 	 * @param commitPath 
 	 * @return String rawfile or Null if there is exceptions
 	 * @throws Exception 
 	 */
 	public String getRawFileFromDiffTree(String fileID, String commitID, List<CommitFamily> commitPath)
 	{
 		try
 		{
 			String rawFile = "";
 			
 			List<CommitDiff> commitDiffs = getDiffTreeFromFirstCommit(fileID, commitID);
 			List<CommitFamily> shortestCommitPath = new ArrayList<CommitFamily>();
 			Map<String, FileCache> fileCaches = getFileCachesFromCommit(fileID, commitID);
 			
 			// Rebuild the commit path that has the latest Add entry in it.
 			for(CommitFamily cf: commitPath)
 			{
 				// this commit Family has the lastest Add for the file, start from here
 				FileDiff fd = getFileDiffForCommitFamily(cf, commitDiffs, fileID);
 				if(fd != null)
 				{
 					// If the commit has a raw file for this, just get the raw file
 					if(fileCaches.containsKey(cf.getChildId()))
 					{
 						rawFile = fileCaches.get(cf.getChildId()).getRaw_file();
 						break;
 					}
 					else
 					if(fd.isAddCommit())
 					{
 						shortestCommitPath.add(cf);
 						break;
 					}
 					else
 					{
 						shortestCommitPath.add(cf);
 					}
 				}
 			}
 	
 			// Create raw file from the beginning of shortest path
 			for(int i =shortestCommitPath.size() - 1; i >= 0; i--)
 			{
 				// get commitDiff
 				FileDiff file = getFileDiffForCommitFamily(shortestCommitPath.get(i), commitDiffs, fileID);
 				if(file == null)
 					continue;
 				
 				if(file.isAddCommit())
 				{
 					//Should have only single DiffEntry - DIFF_ADD
 					if(file.getDiffEntries().size()>0)
 						rawFile = file.getDiffEntries().get(0).getDiff_text();
 					
 					continue;
 				}
 				else if(file.isDeleteCommit())
 				{
 					rawFile = "";
 				}
 				else //create the next version of the file
 				{
 					List<DiffEntry> deleteList = new ArrayList<DiffEntry>();
 					List<DiffEntry> insertList = new ArrayList<DiffEntry>();
 				
 					// Store list of Delete Entry backward
 					for(DiffEntry entry: file.getDiffEntries())
 					{
 						if(entry.getDiff_type() == diff_types.DIFF_MODIFYDELETE)
 							deleteList.add(entry);
 						else if(entry.getDiff_type() == diff_types.DIFF_MODIFYINSERT)
 							insertList.add(entry);
 					}		
 					
 					//Get Original Equal in reverse order
 					int lastStart = rawFile.length();
 					for(int j =deleteList.size() - 1; j >= 0; j--)
 					{
 						DiffEntry entry = getLatestDelete(lastStart, deleteList);
 						if(entry == null)
 							continue;
 						
 						if(entry.getChar_end() <= lastStart)
 						{
 							lastStart = entry.getChar_start();
 							
 							//Remove the delete entry
 							int firstEnd    = entry.getChar_start();
 							int secondStart = entry.getChar_end();
 							if(firstEnd < 0)
 								firstEnd = 0;
 							if(secondStart > rawFile.length() - 1)
 								secondStart = rawFile.length() - 1;
 							if(secondStart <0)
 								secondStart =0;
 							
 							// Encounter exception, ignore for now
 							if(firstEnd > rawFile.length())
 							{
 								String error = "Delete Raw file error: " + file.getFile_id() + " for " + commitID + "\n";
 								error += entry.getNewCommit_id() +"-"+ entry.getOldCommit_id() + "\n";
 								error += "from:" + entry.getChar_start() +" to "+ entry.getChar_end() + "\n";
 								throw new Exception("Building Rawfile messed up, we have a problem guys:" + error); 
 							}
 							
 							// Merge back rawfile
 							String firstPart  = rawFile.substring(0, firstEnd);
 							String secondPart = rawFile.substring(secondStart);
 							rawFile = firstPart + secondPart;
 						}
 					}
 					
 					//Have to ensure the the order 
 					//Create new version of the file
 					int lastEnd = 0;
 					for(int k=0; k< insertList.size(); k++)
 					{
 						DiffEntry entry = getEarliestInsert(lastEnd, insertList);
 						if(entry == null)
 							continue;
 						
 						// store the last line, need to find the min
 						if(entry.getChar_start() >= lastEnd)
 						{
 							lastEnd = entry.getChar_end();
 		
 							// Split up the Rawfile for insert
 							int firstEnd    = entry.getChar_start();
 							int secondStart = entry.getChar_start();
 							if(firstEnd < 0)
 								firstEnd = 0;
 							if(secondStart > rawFile.length() - 1)
 								secondStart = rawFile.length() - 1;
 							if(secondStart < 0)
 								secondStart =0;
 							
 							// Encounter exception, ignore for now
 							if(firstEnd > rawFile.length())
 							{
 								String error = "Insert Raw file error: " + file.getFile_id() + " for " + commitID + "\n";
 								error += entry.getNewCommit_id() +"-"+ entry.getOldCommit_id() + "\n";
 								error += "from:" + entry.getChar_start() +" to "+ entry.getChar_end() + "\n";
 								throw new Exception("Building Rawfile messed up, we have a problem guys:" + error); 
 							}
 							// insert new change
 							String firstPart  = rawFile.substring(0, firstEnd);
 							String secondPart = rawFile.substring(secondStart);
 							rawFile = firstPart + entry.getDiff_text() + secondPart;
 						}
 					}
 				}
 			}
 			return rawFile;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.out.print(e.getMessage());
 			return null;
 		}
 	}
 	
 	/**
 	 * Return all the files exist in current commit.
 	 * @param commitID
 	 * @return a list of all the file id in the given commit. Null otherwise
 	 */
 	public List<String> getSourceTree(String commitID)
 	{
 		try
 		{
 			List<String> fileList = new ArrayList<String>();
 			List<CommitFamily> commitPath = getCommitPathToRoot(commitID);
 			List<CommitFamily> shortestCommitPath = new ArrayList<CommitFamily>();
 			Map<String, List<String>> commitCaches = getCommitCachesFromCommit(commitID);
 			
 			// Build the shortest commit path
 			for(CommitFamily cf: commitPath)
 			{
 				// Start from the commit, moving backward until the root or hit a cached commit
 				if(commitCaches.containsKey(cf.getChildId()))
 				{
 					fileList.addAll(commitCaches.get(cf.getChildId()));
 					break;
 				}
 				else
 				{
 					shortestCommitPath.add(cf);
 				}
 			}
 			
 			// Get all the commit file history for every commit in the shortest path
 			if(shortestCommitPath.size() == 0)
 				return fileList;
 			
 			String oldCommit = shortestCommitPath.get(shortestCommitPath.size() - 1).getChildId();
 			Map<String, Map<String, List<String>>> commitHistoryMap = getAddDeleteFileForCommitRange(oldCommit, commitID);
 	
 			// Create list of file from the beginning of shortest path
 			for(int i =shortestCommitPath.size() - 1; i >= 0; i--)
 			{
 				// get all the add or delete files and update file list
 				String currentCommit = shortestCommitPath.get(i).getChildId();
 				if(commitHistoryMap.containsKey(currentCommit))
 				{
 					List<String> addedFiles   = commitHistoryMap.get(currentCommit).get("DIFF_ADD");
 					List<String> deletedFiles = commitHistoryMap.get(currentCommit).get("DIFF_DELETE");
 					if(addedFiles!=null)
 						fileList.addAll(addedFiles);
 					if(deletedFiles!=null)
 						fileList.removeAll(deletedFiles);
 				}
 			}
 			
 			return fileList;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.out.print(e.getMessage());
 			return null;
 		}
 	}
 	
 
 	/**
 	 * Get Add and Delete File history for all commits started from the newest commit to the oldest commit
 	 * @param oldCommitID 
 	 * @param newCommitID 
 	 * @return Map<commitID, Map<DiffType, List<FileID>>> or Null ow
 	 */
 	public Map<String, Map<String, List<String>>> getAddDeleteFileForCommitRange(String oldCommitID, String newCommitID) {
 		try 
 		{
 			Map<String, Map<String, List<String>>> commitFileMap = new HashMap<String, Map<String, List<String>>>();
 			String sql = "SELECT file_id, diff_type, commit_id FROM file_diffs natural join commits WHERE " +
 						"(branch_id=? or branch_id is NULL) and " +
 						"commit_date <= (SELECT commit_date from commits where commit_id=? and (branch_id=? OR branch_id is NULL) limit 1) and " +
 						"commit_date >= (SELECT commit_date from commits where commit_id=? and (branch_id=? OR branch_id is NULL) limit 1) and " +
 						 "new_commit_id=commit_id and " +
 						 "(diff_type='DIFF_ADD' or diff_type='DIFF_DELETE');"; 
 			
 			ISetter[] params = {new StringSetter(1,this.branchID),
 								new StringSetter(2,newCommitID),
 								new StringSetter(3,this.branchID),
 								new StringSetter(4,oldCommitID),
 								new StringSetter(5,this.branchID)};
 			
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 			{
 				String fileId   = rs.getString("file_id");
 				String diffType = rs.getString("diff_type");
 				String commitId = rs.getString("commit_id");
 				
 				if(commitFileMap.containsKey(commitId))
 				{
 					// already has DIFF_ADD or DIFF_DELETE key
 					if(commitFileMap.get(commitId).containsKey(diffType))
 					{
 						commitFileMap.get(commitId).get(diffType).add(fileId);
 					}
 					else
 					{
 						List<String> files = new ArrayList<String>();
 						files.add(fileId);
 						commitFileMap.get(commitId).put(diffType, files);
 					}
 				}
 				else // doesnt have the commit yet, add new <commit, Map<difftype, List<File>>>
 				{
 					List<String> files = new ArrayList<String>();
 					files.add(fileId);
 					
 					Map<String, List<String>> fileMap = new HashMap<String, List<String>>();
 					fileMap.put(diffType, files);
 					commitFileMap.put(commitId, fileMap);
 				}
 			}
 			
 			return commitFileMap;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Return a random path from a commit to the root.
 	 * The root commit is the one that has no parent in the commit family.
 	 * @param commitID
 	 * @return List<CommitFamily>, NULL ow
 	 */
 	public List<CommitFamily> getCommitPathToRoot(String commitID)
 	{
 		try {
 			String sql = "SELECT parent, child from commit_family natural join commits where " +
 					"(branch_id=? or branch_id is NULL) and " +
 					"commit_date <= " +
 					"(SELECT commit_date from commits where commit_id=? and (branch_id=? OR branch_id is NULL) limit 1) AND commit_id=child;";
 
 			Map<String, List<String>> familyMap = new HashMap<String, List<String>>();
 			List<CommitFamily> familyList 	    = new ArrayList<CommitFamily>();
 
 			ISetter[] parms = {new StringSetter(1, this.branchID), new StringSetter(2, commitID), new StringSetter(3, this.branchID)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, parms);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			// Create family map for all commits
 			while(rs.next())
 			{
 				String parentId = rs.getString("parent");
 				String childId  = rs.getString("child");
 				if(familyMap.containsKey(childId))
 				{
 					familyMap.get(childId).add(parentId);
 				}
 				else
 				{
 					List<String> parentList = new ArrayList<String>();
 					parentList.add(parentId);
 					familyMap.put(childId, parentList);
 				}
 			}
 
 			// Get a random path from this commit to Root
 			String currentChild = commitID;
 			while(familyMap.containsKey(currentChild))
 			{
 				String parentId = familyMap.get(currentChild).get(0);
 				familyList.add(new CommitFamily(parentId, currentChild));
 				currentChild = parentId;
 			}
 
 			return familyList;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Return the first 3 file caches found.
 	 * The branch id is the one provided by the user
 	 * @param fileID
 	 * @param commitID
 	 * @return Map<commitId, FileCache>, Null ow
 	 */
 	public Map<String, FileCache> getFileCachesFromCommit(String fileID, String commitID)
 	{
 		try{
 			Map<String, FileCache> cacheList = new HashMap<String, FileCache>();
 			String sql = "SELECT file_id, commit_id, raw_file from commits natural join file_caches where " +
 					"file_id=? and " +
 					"(branch_id=? or branch_id is NULL) and commit_date<= " + 
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? OR branch_id is NULL) limit 1) ORDER BY commit_date DESC limit 3";
 
 			ISetter[] parms = { new StringSetter(1, fileID),
 								new StringSetter(2, this.branchID),
 								new StringSetter(3, commitID),
 								new StringSetter(4, this.branchID)};
 								
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, parms);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 			{
 				String commitId     = rs.getString("commit_id");
 				String fileId 	    = rs.getString("file_id");
 				String rawFile      = rs.getString("raw_file");
 				cacheList.put(commitId, new FileCache(fileId, commitId, rawFile));
 			}
 			
 			return cacheList;	
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/***
 	 * Return the first 3 commit caches and their files name. The search started backward from this commitID to the root.
 	 * @param commitID
 	 * @return Map<CommitID, List<FileID>>, NULL ow
 	 */
 	public Map<String, List<String>> getCommitCachesFromCommit(String commitID)
 	{
 		try{
 			Map<String, List<String>> cacheList = new HashMap<String, List<String>>();
 			
 			String sql = "SELECT file_id, commit_id from commits natural join file_caches where " +
 					"(branch_id=? or branch_id is NULL) and commit_date<= " + 
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? OR branch_id is NULL) limit 1) ORDER BY commit_date DESC limit 3";
 
 			ISetter[] parms = { new StringSetter(1, this.branchID),
 								new StringSetter(2, commitID),
 								new StringSetter(3, this.branchID)};
 					
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, parms);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 
 			// added to commit cache map
 			while(rs.next())
 			{
 				String commitId     = rs.getString("commit_id");
 				String fileId 	    = rs.getString("file_id");
 				if(cacheList.containsKey(commitId))
 				{
 					cacheList.get(commitId).add(fileId);
 				}
 				else
 				{
 					List<String> fileList = new ArrayList<String>();
 					fileList.add(fileId);
 					cacheList.put(commitId, fileList);
 				}
 			}
 			
 			return cacheList;	
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Return the Diff tree for a file start from root to commitID.
 	 * @param fileID
 	 * @param commitID
 	 * @return Lis<CommitDiff> list of all the diffs from Root to Current Commit. Null ow
 	 */
 	public List<CommitDiff> getDiffTreeFromFirstCommit(String fileID, String commitID)
 	{
 		try{
 			// For each CommitDiff, store a list of FileDiff. For each FileDiff, store a list of DiffEntry
 			List<CommitDiff> CommitList = new ArrayList<CommitDiff>();
 			String sql = "SELECT file_id, new_commit_id, old_commit_id, diff_text, char_start, char_end, diff_type from commits natural join file_diffs where " +
 					"file_id=? and " +
 					"(branch_id=? or branch_id is NULL) and commit_date<= " + 
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? OR branch_id is NULL) limit 1) AND new_commit_id= commit_id ORDER BY old_commit_id, new_commit_id";
 
 			ISetter[] parms = { new StringSetter(1, fileID),
 								new StringSetter(2, this.branchID),
 								new StringSetter(3, commitID),
 								new StringSetter(4, this.branchID)};
 					
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, parms);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 
 			// Get first CommitDiff
 			if (!rs.next())
 				return CommitList;
 			
 			String currentNewCommitId = rs.getString("new_commit_id");
 			String currentOldCommitId = rs.getString("old_commit_id");
 			String currentFileId 	  = rs.getString("file_id");
 			String currentDiffTxt     = rs.getString("diff_text");
 			String currentDiffType		  = rs.getString("diff_type");
 			int currentCharStart = rs.getInt("char_start");
 			int currentCharEnd = rs.getInt("char_end");
 			
 			// Group CommitDiff by old,new commit id
 			List<FileDiff> currentFileDiffList = new ArrayList<FileDiff>();
 			CommitDiff currentCommitDiff = new CommitDiff(currentNewCommitId, currentOldCommitId, currentFileDiffList);
 			
 			// Group FileDiff by file_id
 			DiffEntry de = new DiffEntry(currentFileId, currentNewCommitId, currentOldCommitId, currentDiffTxt, currentCharStart, currentCharEnd, currentDiffType);
 			FileDiff currentFileDiff = new FileDiff(currentFileId, new ArrayList<DiffEntry>());
 			currentFileDiff.addDiffEntry(de);
 			
 			while(rs.next())
 			{
 				// Group all CommitDiff by old and new commit id
 				String newCommitId  = rs.getString("new_commit_id");
 				String oldCommitId  = rs.getString("old_commit_id");
 				String fileId 	    = rs.getString("file_id");
 				String diffTxt      = rs.getString("diff_text");
 				String diffType		= rs.getString("diff_type");
 				int charStart 		= rs.getInt("char_start");
 				int charEnd 		= rs.getInt("char_end");
 				
 				// same CommitDiff
 				if (newCommitId.equals(currentNewCommitId) && oldCommitId.equals(currentOldCommitId))
 				{
 					// same FileDiff
 					if(fileId.equals(currentFileId))
 					{
 						currentFileDiff.addDiffEntry(new DiffEntry(fileId, newCommitId, oldCommitId, diffTxt, charStart, charEnd, diffType));
 					}
 					else
 					{
 						// add the current FileDiff to the CommitDiff and start new fileDiff
 						currentCommitDiff.addFileDiff(currentFileDiff);
 						currentFileDiff = new FileDiff(fileId, new ArrayList<DiffEntry>());
 						currentFileId = fileId;
 					}
 				}
 				else
 				{
 					// add current CommitDiff and start new CommitDiff
 					currentCommitDiff.addFileDiff(currentFileDiff);
 					CommitList.add(currentCommitDiff);
 					currentCommitDiff = new CommitDiff(newCommitId, oldCommitId, new ArrayList<FileDiff>()); 
 					currentNewCommitId = newCommitId;
 					currentOldCommitId = oldCommitId;
 					
 					// start new File
 					currentFileDiff = new FileDiff(fileId, new ArrayList<DiffEntry>());
 					currentFileId = fileId;
 					
 					// add new diff entry
 					currentFileDiff.addDiffEntry(new DiffEntry(fileId, newCommitId, oldCommitId, diffTxt, charStart, charEnd, diffType));
 				}
 			}
 			
 			// Added last commit diff
 			currentCommitDiff.addFileDiff(currentFileDiff);
 			CommitList.add(currentCommitDiff);
 			
 			return CommitList;	
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Get diff entries for 2 consecutive commits
 	 * @param fileID
 	 * @param oldCommitID
 	 * @param newCommitID
 	 * @return List<DiffEntry> list of the diffs, Null if there is none
 	 */
 	public List<DiffEntry> getDiffsFromTwoConsecutiveCommits(String fileID, String oldCommitID, String newCommitID)
 	{
 		try{
 			List<DiffEntry> diffList = new ArrayList<DiffEntry>();
 			String sql = "SELECT file_id, new_commit_id, old_commit_id, diff_text, char_start, char_end, diff_type from file_diffs where " +
 						"file_id=? and old_commit_id=? and new_commit_id=?";
 			ISetter[] params = {new StringSetter(1, fileID), new StringSetter(2, oldCommitID), new StringSetter(3, newCommitID)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			this.addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			
 			ResultSet rs = ei.getResult();
 			while(rs.next())
 			{
 				String newCommitId  = rs.getString("new_commit_id");
 				String oldCommitId  = rs.getString("old_commit_id");
 				String fileId 	    = rs.getString("file_id");
 				String diffTxt      = rs.getString("diff_text");
 				String diffType		= rs.getString("diff_type");
 				int charStart 		= rs.getInt("char_start");
 				int charEnd 		= rs.getInt("char_end");
 				
 				diffList.add(new DiffEntry(fileId, newCommitId, oldCommitId, diffTxt, charStart, charEnd, diffType));
 			}
 			
 			return diffList;	
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Get commit from commitId
 	 * @param CommitId
 	 * @return Commit. Throw exception ow
 	 */
 	public Commit getCommit(String CommitId)
 	{
 		String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id FROM Commits where commit_id=? and (branch_id=? or branch_id is NULL);";
 		ISetter[] params = {new StringSetter(1,CommitId), new StringSetter(2,this.branchID)};
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 		this.addExecutionItem(ei);
 		return new Commit(ei);
 	}
 	
 	/**
 	 * Get a list of commits by a limit and an offset (where to start)
 	 * The maximum number of commit return is {@code iLIMIT}
 	 * @param iLIMIT
 	 * @param iOFFSET
 	 * @return List of commits
 	 */
 	public List<Commit> getCommits(int iLIMIT, int iOFFSET) {
 		LinkedList<Commit> commits = new LinkedList<Commit>();
 		String sql = "SELECT * FROM commits " +
					 "ORDER BY commit_date " +
 					 "LIMIT ? OFFSET ?"; 
 		ISetter[] params = {new IntSetter(1,iLIMIT), new IntSetter(2, iOFFSET)};
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 		addExecutionItem(ei);
 		
 		for (int i = 0;i < iLIMIT;i++)
 		{
 			commits.add(new Commit(ei));
 		}
 		return commits;
 	}
 	
 	public int getCommitCount() {
 		String sql = "SELECT COUNT(*) from commits;";
 		ISetter[] parms = {};
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, parms);
 		addExecutionItem(ei);
 		ei.waitUntilExecuted();
 		try
 		{
 			if (ei.getResult().next())
 			{
 				return ei.getResult().getInt(1);
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 	/**
 	 * Get a list of commits that happen within a week around a timestamp
 	 * Any commits happened a week before or after this timestamp will be returned
 	 * @param date
 	 * @return List of commits, NULL ow
 	 */
 	public List<Commit> getCommitsAroundDate(Timestamp date) {
 		try {
 			List<Commit> commits = new ArrayList<Commit>();
 			
 			String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id FROM commits WHERE" +
 					" (branch_id is NULL OR branch_id=?) AND" +
 					" commit_date >= ?::timestamp and commit_date <= ?::timestamp"; 
 			Timestamp dateAfter = new Timestamp(date.getTime());
 			dateAfter.setTime(date.getTime() + 3600 * 1000 * 24 * 7);
 			date.setTime(date.getTime() - 3600 * 1000 * 24 * 7);
 			
 			ISetter[] params = {new StringSetter(1,branchID), new StringSetter(2, date.toString()), new StringSetter(3, dateAfter.toString())};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 			{
 				commits.add(new Commit(
 						rs.getString("commit_id"),
 						rs.getString("author"),
 						rs.getString("author_email"),
 						rs.getString("comments"),
 						rs.getTimestamp("commit_date"),
 						rs.getString("branch_id")
 				));
 			}
 			return commits;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get all the name of the changed files in a commit
 	 * @param CommitId
 	 * @return List of file name, NULL ow
 	 */
 	public Set<String> getChangesetForCommit(String CommitId)
 	{
 		try {
 			Set<String> files = new HashSet<String>();
 			String sql = "Select distinct file_id from file_diffs where new_commit_id=?";
 			ISetter[] parms = {new StringSetter(1, CommitId)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, parms);
 			this.addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			while (ei.getResult().next())
 			{
 				files.add(ei.getResult().getString("file_id"));
 			}
 			return files;
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Get all the commits in the current branch
 	 * @return List of all commits, NULL if there is exception
 	 */
 	public Set<Commit> getAllCommits()
 	{
 		Set<Commit> commits = new HashSet<Commit>();
 		
 		try {
 			String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id from commits where (branch_id is NULL OR branch_id=?)";
 			ISetter[] params = {new StringSetter(1,branchID)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			while(rs.next())
 			{
 				commits.add(new Commit(
 						rs.getString("commit_id"),
 						rs.getString("author"),
 						rs.getString("author_email"),
 						rs.getString("comments"),
 						rs.getTimestamp("commit_date"),
 						rs.getString("branch_id")
 				));
 			}
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 		return commits;
 	}
 	
 	/**
 	 * Get User that created the given commit
 	 * @param CommitId
 	 * @return User, Null if there is none or exception
 	 */
 	public User getUserFromCommit(String CommitId)
 	{
 		try {
 			String sql = "SELECT author, author_email from commits where commit_id = ?";
 			ISetter[] params = {new StringSetter(1,CommitId)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			if (!rs.next())
 				return null;
 			
 			return new User(rs.getString("author_email"), rs.getString("author"));
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get all commit family entries that happened before including this commit
 	 * @param commitID
 	 * @return List of CommitFamily, Null if there is exception
 	 */
 	public List<CommitFamily> getCommitFamilyFromCommit(String commitID)
 	{
 		try {
 			String sql = "SELECT parent, child FROM commit_family NATURAL JOIN commits WHERE " +
 					"(branch_id=? or branch_id is NULL) and " +
 					"commit_date >= " +
 					"(SELECT commit_date from commits where commit_id=? and (branch_id=? OR branch_id is NULL) limit 1)" +
 					" AND commit_id=child ORDER BY commit_date desc;";
 
 			List<CommitFamily> rawFamilyList = new ArrayList<CommitFamily>();
 			
 			ISetter[] params = {new StringSetter(1,this.branchID),
 								new StringSetter(2,commitID),
 								new StringSetter(3,this.branchID)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 			{
 				String parentId = rs.getString("parent");
 				String childId  = rs.getString("child");
 				rawFamilyList.add(new CommitFamily(parentId, childId));
 			}
 			
 			return rawFamilyList;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a list of parent commits of a given commit
 	 * @param CommitID
 	 * @return List of parent commit, Empty if none found, NUll if exception
 	 */
 	public List<Commit> getCommitParents(String CommitID) {
 		try 
 		{
 			LinkedList<Commit> commits = new LinkedList<Commit>();
 			String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id FROM commit_family " +
 					"JOIN Commits ON (commit_family.parent = Commits.commit_id) where child=?" +
 					"and (branch_id is NULL OR branch_id=?) order by commit_date, commit_id;"; 
 			
 			ISetter[] params = {new StringSetter(1,CommitID),
 								new StringSetter(2,branchID)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			while(rs.next())
 			{
 				commits.add(new Commit(rs.getString(1), rs.getString(2),
 						rs.getString(3), rs.getString(4), rs.getTimestamp(5), rs.getString(6)));
 			}
 			return commits;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get file changes from an old commit to new commit
 	 * @param oldCommit
 	 * @param newCommit
 	 * @return list of file name, empty if none found, null if there is exception
 	 */
 	public List<String> getFilesChanged(String oldCommit, String newCommit) {
 		try 
 		{
 			LinkedList<String> files = new LinkedList<String>();
 			String sql = "SELECT file_id FROM file_diffs " +
 					"WHERE old_commit_id=? AND new_commit_id=?"; 
 			
 			ISetter[] params = {new StringSetter(1,oldCommit), new StringSetter(2,newCommit)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 			{
 				if(!files.contains(rs.getString("file_id")))
 					files.add(rs.getString("file_id"));
 			}
 			return files;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a list of Diff object for a given commitId and file id
 	 * @param commitID
 	 * @param file
 	 * @return list of DiffEntry. Empty if none found. Null if there is exception
 	 */
 	public List<DiffEntry> getDiffsFromCommitAndFile(String commitID, String file) {
 		try {
 			List<DiffEntry> diffs = new ArrayList<DiffEntry>();
 			String sql = "SELECT * FROM file_diffs WHERE file_id=? AND new_commit_id=?";
 			
 			ISetter[] params = {new StringSetter(1,file), new StringSetter(2,commitID)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 			{
 				diffs.add(new DiffEntry(file, commitID, rs.getString("old_commit_id"),
 						rs.getString("diff_text"), rs.getInt("char_start"), rs.getInt("char_end"), 
 						rs.getString("diff_type")));
 			}
 			
 			return diffs;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a list of added files in a commit
 	 * @param commitID
 	 * @return List of added file, empty if none found, NULL if exception
 	 */
 	public List<String> getFilesAdded(String commitID) {
 		try 
 		{
 			LinkedList<String> files = new LinkedList<String>();
 			String sql = "SELECT file_id, diff_type FROM file_diffs " +
 					"WHERE new_commit_id=?"; 
 		
 			ISetter[] params = {new StringSetter(1,commitID)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 
 			while(rs.next())
 			{
 				if(rs.getString("diff_type").equals("DIFF_ADD") && !files.contains(rs.getString("file_id")))
 					files.add(rs.getString("file_id"));
 			}
 			return files;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Get a list of deleted files in a commit
 	 * @param commitID
 	 * @return List of deleted file, empty if none found, NULL if exception
 	 */
 	public List<String> getFilesDeleted(String commitID) {
 		try 
 		{
 			LinkedList<String> files = new LinkedList<String>();
 			String sql = "SELECT file_id, diff_type FROM file_diffs " +
 					"WHERE new_commit_id=?"; 
 			
 			ISetter[] params = {new StringSetter(1,commitID)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 			{
 				if(rs.getString("diff_type").equals("DIFF_DELETE") && !files.contains(rs.getString("file_id")))
 					files.add(rs.getString("file_id"));
 			}
 			return files;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Check if there is a parent-child relationship between the two commit
 	 * @param parent
 	 * @param child
 	 * @return true if it is, false ow
 	 */
 	public boolean parentHasChild(String parent, String child) {
 		try 
 		{
 			String sql = "SELECT parent, child FROM commit_family " +
 					"WHERE parent=? AND child=?"; 
 			
 			ISetter[] params = {new StringSetter(1,parent), new StringSetter(2,child)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 				return true;
 			
 			return false;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Get a Map of all the ownership for a file happened before a commit (including this commit)
 	 * Any commit that changed this file will be added to this map
 	 * @param FileId
 	 * @param CommitId
 	 * @return Map<CommitID, List<Change>> where List<Change> has the ownership breakdown for this file in this CommitID. Null if there is exception
 	 */
 	public Map<String, List<Change>> getAllFileOwnerChangesBefore(String FileId, String CommitId)
 	{
 		try 
 		{
 			String sql = "SELECT commit_id, file_id, owner_id, char_start, char_end, change_type FROM owners natural join commits where commit_date <= (select commit_date from commits where commit_id=?)" +
 					"and (branch_id is NULL OR branch_id=?) and file_id=? order by commit_id;"; 
 			ISetter[] params = {new StringSetter(1,CommitId), new StringSetter(2,branchID), new StringSetter(3,FileId)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			// Create a map for <Commit_id, List<Change>>
 			Map<String, List<Change>> commitMap = new HashMap<String, List<Change>>();
 			LinkedList<Change> currentChanges = new LinkedList<Change>();
 			String currentCommit = "";
 			
 			while(rs.next())
 			{
 				String commitId = rs.getString("commit_id");
 				if(currentCommit.isEmpty())
 				{
 					// first commit
 					currentCommit = rs.getString("commit_id");
 					currentChanges.add(new Change(rs.getString("owner_id"),
 												  rs.getString("commit_id"), 
 												  Resources.ChangeType.valueOf(rs.getString("change_type")),
 												  rs.getString("file_id"),
 												  rs.getInt("char_start"),
 												  rs.getInt("char_end")));
 					
 				}
 				else 
 				{
 					if(commitId.equals(currentCommit))
 					{
 						// same commit, push into current map
 						currentChanges.add(new Change(rs.getString("owner_id"),
 													  rs.getString("commit_id"), 
 													  Resources.ChangeType.valueOf(rs.getString("change_type")),
 													  rs.getString("file_id"),
 													  rs.getInt("char_start"),
 													  rs.getInt("char_end")));
 					}
 					else
 					{
 						// add new File map
 						commitMap.put(currentCommit, currentChanges);
 						currentCommit = commitId;
 						currentChanges = new LinkedList<Change>();
 						currentChanges.add(new Change(rs.getString("owner_id"),
 													  rs.getString("commit_id"), 
 													  Resources.ChangeType.valueOf(rs.getString("change_type")),
 													  rs.getString("file_id"),
 													  rs.getInt("char_start"),
 													  rs.getInt("char_end")));
 					}
 				}
 			}
 			
 			//Add last commit
 			if(!currentCommit.isEmpty())
 				commitMap.put(currentCommit, currentChanges);
 			
 			return commitMap;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get onwer breakdown for a file for a specific commit. If the current commit does not have the file, look for its parent commit until find one.
 	 * @param FileId
 	 * @param CommitId
 	 * @return List of changes which contains the ownership breakdown. Null if none found
 	 */
 	public List<Change> getAllOwnersForFileAtCommit(String FileId, String CommitId, List<CommitFamily> commitPath)
 	{
 		// get all the onwer entries for this file since this commit
 		Map<String, List<Change>> commitMap = getAllFileOwnerChangesBefore(FileId, CommitId);
 		
 		// traverse comithPath if grab the first found as it has the latest owner break down.
 		for(CommitFamily cf: commitPath)
 		{
 			String commitId = cf.getChildId();
 			if(commitMap.containsKey(commitId))
 				return commitMap.get(commitId);
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Get the commit status from fix_inducing_changes
 	 * @param CommitId
 	 * @return {@code true} if commit passed, {@code false} if failed.
 	 */
 	public boolean getCommitStatus(String CommitId)
 	{
 		try
 		{
 			String sql = "SELECT * from fix_inducing where bug=?";
 			ISetter[] parms = {new StringSetter(1, CommitId)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, parms);
 			this.addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			if(ei.getResult().next())
 				return false;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return true;
 	}
 }
