 package org.chaoticbits.collabcloud.vc.svn;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.Set;
 
 import org.chaoticbits.collabcloud.CloudWeights;
 import org.chaoticbits.collabcloud.ISummarizable;
 import org.chaoticbits.collabcloud.codeprocessor.java.JavaClassSummarizable;
 import org.chaoticbits.collabcloud.vc.Developer;
 import org.chaoticbits.collabcloud.vc.DiffToken;
 import org.junit.Test;
 
 public class SVNLoaderTest {
 
 	public static final String TESTSVN_REPO = "testsvn/repo";
 
 	private DiffToken timedNegaScout = new DiffToken(new JavaClassSummarizable(new File("mancala/player/TimedNegaScoutPlayer.java")),
 			"TimedNegaScoutPlayer", "");
 	private DiffToken greedyPlayer = new DiffToken(new JavaClassSummarizable(new File("mancala/player/GreedyPlayer.java")), "GreedyPlayer", "");
 	private DiffToken getPlay = new DiffToken(new JavaClassSummarizable(new File("mancala/player/TimedNegaScoutPlayer.java")), "getPlay", "");
 	private DiffToken setLog = new DiffToken(null, "setLog", "");
 	private DiffToken play = new DiffToken(new JavaClassSummarizable(new File("mancala/player/GreedyPlayer.java")), "play", "");
 
 	@Test
 	public void allThreeDevs() throws Exception {
 		SVNLoader gitLoader = new SVNLoader(new File(TESTSVN_REPO), "/trunk/", 3L, 6L);
 		Set<Developer> developers = gitLoader.getDevelopers();
 		assertEquals("Only 3 developers", 3, developers.size());
 		assertTrue("Contains Andy Meneely", developers.contains(new Developer("Andy Meneely", "andy.meneely@gmail.com")));
 		assertTrue("Contains Andy Programmer", developers.contains(new Developer("Andy Programmer", "apmeneel@ncsu.edu")));
 		assertTrue("Contains Kelly Doctor", developers.contains(new Developer("Kelly Doctor", "andy@se.rit.edu")));
 	}
 
 	@Test
 	public void bunchOfFiles() throws Exception {
 		SVNLoader svnLoader = new SVNLoader(new File(TESTSVN_REPO), "/trunk/", 4L, 6L);
 		Set<ISummarizable> artifacts = svnLoader.getFilesChanged();
 		assertEquals("Only 2 files changed", 2, artifacts.size());
 		assertTrue(artifacts.contains(new JavaClassSummarizable(new File("mancala/player/TimedNegaScoutPlayer.java"))));
 		assertTrue(artifacts.contains(new JavaClassSummarizable(new File("mancala/player/GreedyPlayer.java"))));
 	}
 
 	@Test
 	public void pullsCloudWeights() throws Exception {
 		SVNLoader svnLoader = new SVNLoader(new File(TESTSVN_REPO), "/trunk/", 4L, 6L);
 
 		CloudWeights weights = svnLoader.getCloudWeights();
		assertEquals(2.0, weights.get(timedNegaScout), 0.001);
		assertEquals(4.0, weights.get(greedyPlayer), 0.001);
 		assertEquals(2.0, weights.get(getPlay), 0.001);
 		assertEquals(0.0, weights.get(setLog), 0.001);
 		assertEquals(18.0, weights.get(play), 0.001);
 	}
 
 	@Test
 	public void getFileContribution() throws Exception {
 		SVNLoader svnLoader = new SVNLoader(new File(TESTSVN_REPO), "/trunk/", 4L, 6L);
 
 		Developer andy = new Developer("Andy Meneely", "andy.meneely@gmail.com");
 		JavaClassSummarizable greedy = new JavaClassSummarizable(new File("mancala/player/GreedyPlayer.java"));
 
 		assertTrue("has Andy Meneely", svnLoader.getDevelopers().contains(andy));
 		assertTrue("has GreedyPlayer.java", svnLoader.getFilesChanged().contains(greedy));
 		Set<ISummarizable> map = svnLoader.getFilesContributed(andy);
 		assertTrue("Andy Meneely worked on GreedyPlayer.java", map.contains(greedy));
 	}
 
 	@Test
 	public void getOnlyFileContribution() throws Exception {
 		SVNLoader svnLoader = new SVNLoader(new File(TESTSVN_REPO), "/trunk/", 4L, 6L);
 		Developer kelly = new Developer("Kelly Doctor", "andy@se.rit.edu");
 		JavaClassSummarizable negaScout = new JavaClassSummarizable(new File("mancala/player/TimedNegaScoutPlayer.java"));
 
 		assertTrue("has Kelly", svnLoader.getDevelopers().contains(kelly));
 		assertTrue("has TimedNegaScoutPlayer.java", svnLoader.getFilesContributed(kelly).contains(negaScout));
 		assertEquals("Kelly contributed to only one file", 1, svnLoader.getFilesContributed(kelly).size());
 	}
 }
