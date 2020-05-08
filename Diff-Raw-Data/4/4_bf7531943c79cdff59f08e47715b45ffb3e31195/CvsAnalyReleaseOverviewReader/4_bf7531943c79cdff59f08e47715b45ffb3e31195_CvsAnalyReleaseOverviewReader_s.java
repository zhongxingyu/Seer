 package cvsanaly;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Session;
 
 import seminer.ReleaseOverview;
 import seminer.ReleaseOverviewReader;
 
 public class CvsAnalyReleaseOverviewReader implements ReleaseOverviewReader {
 
 	Session s;
 	
 	public CvsAnalyReleaseOverviewReader(Session s) {
 		this.s = s;
 	}
 	
 	@Override
 	public List<ReleaseOverview> read(String project) {
 		List<ReleaseOverview> releases = new ArrayList<ReleaseOverview>();
 		
 		List<Object[]> results = s.createSQLQuery("SELECT {tags.*}, {scmlog.*} FROM tags LEFT OUTER JOIN tag_revisions ON tags.id = tag_revisions.tag_id LEFT OUTER JOIN scmlog ON scmlog.id = tag_revisions.commit_id LEFT OUTER JOIN repositories ON repositories.id = scmlog.repository_id WHERE repositories.name = '" + project + "'").addEntity("tags", Tags.class).addEntity("scmlog", Scmlog.class).list();
 		for (Object[] result : results) {
 			Tags tag = (Tags)result[0];
 			Scmlog scmlog = (Scmlog)result[1];
 			
 			String branchName = null;
 			List<Branches> branches = s.createSQLQuery("SELECT DISTINCT {branches.*} FROM actions LEFT OUTER JOIN branches ON actions.branch_id = branches.id WHERE actions.commit_id = " + scmlog.getId()).addEntity("branches", Branches.class).list();
			if(branches.size() != 1) {
 				System.err.println(project + " tag " + tag.getName() + " created from multiple branches");
 			} else {
 				branchName = branches.get(0).getName();
 			}
 			
 			ReleaseOverview release = new ReleaseOverview();
 			release.setProject_name(project);
 			release.setRelease_number(tag.getName());
 			release.setRelease_timestamp(scmlog.getDate());
 			release.setBranch_name(branchName);
 			//release.setLoc(loc);
 			//release.setNum_of_changerequest(num_of_changerequest);
 			//release.setNum_of_committers(num_of_committers);
 			//release.setNum_of_developers(num_of_developers);
 			//release.setNum_of_files(num_of_files)
 			//release.setRelease_location(release_location);
 			releases.add(release);
 		}
 		
 		return releases;
 	}
 }
