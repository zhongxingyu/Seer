 package fixinducingchanges;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import models.Change;
 import models.Commit;
 import models.CommitFamily;
 import models.DiffEntry;
 import models.Issue;
 import models.Link;
 import models.Pair;
 import db.FixInducingDB;
 import db.SocialDb;
 
 public class BugFinder
 {
 	private SocialDb sDB;
 	private FixInducingDB tDB;
 	
 	public BugFinder(SocialDb sDB, FixInducingDB tDB)
 	{
 		super();
 		
 		this.sDB = sDB;
 		this.tDB = tDB;
 		
 		createTable();
 	}
 	
 	public void createTable() {
 		tDB.createTable();
 	}
 	
 	public void findBugs() {
 		int offset = 0;
 		
 		List<Issue> issues = sDB.getIssues(FixResources.DB_LIMIT, offset);
 		
 		for(;;) {
 			for(Issue issue: issues) {
 				List<Link> links = sDB.getLinksFromIssue(issue);
 				for(Link link: links) {
 					Set<String> files = getFilesFromCommit(link.getCommitID());
 					
 					Set<String> fixInducing = new HashSet<String>();
 
 					for(String file: files) {
 						List<DiffEntry> diffs = tDB.getDiffsFromCommitAndFile(link.getCommitID(), file);
 
 						if(!diffs.isEmpty()) {
 							List<CommitFamily> oldCommitPath = tDB.getCommitPathToRoot(diffs.get(0).getOldCommit_id());
 
 							List<Change> oldOwners = tDB.getAllOwnersForFileAtCommit(file, diffs.get(0).getOldCommit_id(), 
 									oldCommitPath);
 							
							if(oldOwners != null && !oldOwners.isEmpty()) {
 								for(Change change: oldOwners) {
 									for(DiffEntry diff: diffs) {
 										if(rangesIntersect(diff.getChar_start(), diff.getChar_end(), 
 												change.getCharStart(), change.getCharEnd())) {
 											updateCandidates(fixInducing, change.getCommitId(), issue.getCreationTS());
 										}
 									}
 								}
 							}
 						}
 					}
 					
 					if(!fixInducing.isEmpty()) {
 						tDB.exportBugs(fixInducing, link.getCommitID());
 					}
 				}
 			}
 			
 			if(issues.size() < FixResources.DB_LIMIT)
 				break;
 			else {
 				offset += FixResources.DB_LIMIT;
 				issues = sDB.getIssues(FixResources.DB_LIMIT, offset);
 			}
 		}
 		
 	}
 	
 	private boolean rangesIntersect(int start1, int end1, int start2, int end2) {
 		return !(start1 >= end2 || start2 >= end1);
 	}
 	
 	private Set<String> getFilesFromCommit(String commit) {
 		return tDB.getChangesetForCommit(commit);
 	}
 	
 	private void updateCandidates(Set<String> candidates, String candidate, Timestamp limit) {
 		if(candidates.contains(candidate))
 			return;
 		
 		Commit commit = tDB.getCommit(candidate);
		if(!commit.getCommit_date().after(limit)) {
			System.out.println("Found a bug candidate: " + commit.getCommit_id());
 			candidates.add(candidate);
		}
 	}
 }
