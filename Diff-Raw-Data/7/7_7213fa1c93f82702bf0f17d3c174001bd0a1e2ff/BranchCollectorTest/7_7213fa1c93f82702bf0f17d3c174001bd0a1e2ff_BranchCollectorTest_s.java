 package edu.illinois.gitsvn.infra.collectors;
 
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.Repository;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.illinois.gitsvn.infra.collectors.BranchCollector;
 
 public class BranchCollectorTest extends DataCollectorTestCase {
 
 	private static final String MASTER = "master";
 	private static final String BR1 = "br1";
 	private Repository repository;
 	
 	BranchCollector branchCollector;
 	
 	@Before
 	public void before() throws Exception {
 		repository = Git.open(testRepo).getRepository();
 		this.branchCollector = new BranchCollector(repository);
 		initTest(this.branchCollector);
 		try{
			this.branchCollector.branchesCheckout(repository);
 			add("f0", "file0 contents");
 		} catch (Exception e) {
 			System.out.println (e.getMessage());
 		}
 	}
 	
 	private int getNumberOfBranchCommits(String collectorData) {
 		String[] data = collectorData.split("; ");
 		int branchCommitsCount = 0;
 		for(String item: data) {
 			if(item.equals(Constants.R_HEADS + BR1)) {
 				branchCommitsCount++;
 			}
 		}
 		return branchCommitsCount;
 	}
 	private int getNumberOfMasterCommits(String collectorData) {
 		String[] data = collectorData.split("; ");
 		int masterCommitsCount = 0;
 		for(String item: data) {
 			if (item.equals(MASTER)) {
 				masterCommitsCount++;
 			}
 		}
 		return masterCommitsCount;
 	}
 	
 	@Test
 	public void testTwoCommits() throws Exception {
 		add("f1", "file1 contents");
 		
 		branch(BR1);
 		add("f2", "file2 contents");
 		
		this.branchCollector.branchesCheckout(repository);
 		finder.findInBranches();
 		
 		String[] data = collector.data.split("; ");
 		
 		assertEquals(1, getNumberOfBranchCommits(collector.data));
 		assertEquals(2, getNumberOfMasterCommits(collector.data));
 	}
 	
 	@Test
 	public void testThreeCommits() throws Exception {
 		
 		add("f1", "file1 contents");
 		
 		branch(BR1);
 		add("f2", "file2 contents");
 		
 		checkout(MASTER);
 		add("f3", "file3 contents");
 		
		this.branchCollector.branchesCheckout(repository);
 		finder.findInBranches();
 		
 		assertEquals(1, getNumberOfBranchCommits(collector.data));
 		assertEquals(3, getNumberOfMasterCommits(collector.data));
 	}
 	
 	@Test
 	public void testMergedCommits() throws Exception {
 		
 		add("f1", "file1 contents");
 		
 		branch(BR1);
 		add("f2", "file2 contents");
 		
 		checkout(MASTER);
 		add("f3", "file3 contents");
 		
 		checkout(BR1);
 		merge(MASTER);
 		
		this.branchCollector.branchesCheckout(repository);
 		finder.findInBranches();
 		
 		assertEquals(2, getNumberOfBranchCommits(collector.data));
 		assertEquals(4, getNumberOfMasterCommits(collector.data));
 	}
 
 }
