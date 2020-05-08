 package edu.illinois.gitsvn.infra.collectors;
 
 import java.util.Collection;
 
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.treewalk.TreeWalk;
 import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
 import org.eclipse.jgit.treewalk.filter.TreeFilter;
 import org.gitective.core.filter.commit.DiffCountFilter;
 
 import edu.illinois.gitsvn.infra.DataCollector;
 import edu.illinois.gitsvn.infra.filters.blacklister.NonJavaFileExtensionBlacklister;
 
 public class JavaLineNumberFilter extends DiffCountFilter implements
 		DataCollector {
 
 	private int count;
	
	public JavaLineNumberFilter() {
		super(true);
	}
 
 	@Override
 	protected TreeWalk createTreeWalk(RevWalk walker, RevCommit commit) {
 		TreeWalk walk = super.createTreeWalk(walker, commit);
 
 		TreeFilter previousFilter = walk.getFilter();
 		TreeFilter newFilter = AndTreeFilter.create(
 				new NonJavaFileExtensionBlacklister(), previousFilter);
 		walk.setFilter(newFilter);
 
 		return walk;
 	}
 	
 	@Override
 	protected boolean include(RevCommit commit, Collection<DiffEntry> diffs, int diffCount) {
 		count = diffCount;
 
 		return true;
 	}
 
 	@Override
 	public String name() {
 		return "SLOC";
 	}
 
 	@Override
 	public String getDataForCommit() {
 		return "" + count;
 	}
 
 }
