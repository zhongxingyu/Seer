 package org.getcomposer.test;
 
 import org.getcomposer.ComposerConstants;
 import org.getcomposer.RepositoryPackage;
 import org.getcomposer.collection.Versions;
 import org.getcomposer.entities.Version;
 import org.getcomposer.packages.PackageDownloader;
 import org.getcomposer.packages.PackagistDownloader;
 import org.junit.Test;
 
 import junit.framework.TestCase;
 
 public class VersionTest extends TestCase {
 
 	@Test
 	public void testParser() {
 		Version v = new Version("1.2.3");
 		
 		assertEquals("1", v.getMajor());
 		assertEquals("2", v.getMinor());
 		assertEquals("3", v.getFix());
 		
 		v.setVersion(">1.2.3-rc1@beta");
 		
 		assertEquals(">", v.getConstraint());
 		assertEquals("1", v.getSuffix());
 		assertEquals(ComposerConstants.BETA, v.getStabilityModifier());
 		assertEquals(ComposerConstants.RC, v.getStability());
 		
 		v.setVersion("dev-master");
 		assertEquals("master", v.getMajor());
		assertEquals(Version.BEGIN, v.getDevPosition());
 		
 		v.setVersion("v2.0.17");
 		assertEquals("2", v.getMajor());
 		assertEquals("v", v.getPrefix());
 	}
 	
 	@Test
 	public void testBuilder() {
 		Version v = new Version();
 		
 		v.setMajor("1");
 		v.setMinor("2");
 		v.setFix("4");
 		v.setBuild("5648");
 		v.setConstraint(">");
 		v.setStabilityModifier("beta");
 		v.setStability(ComposerConstants.ALPHA);
 		v.setSuffix("2");
 		
 		String version = v.toString();
 		assertEquals(">1.2.4.5648-alpha2@beta", version);
 		
 		Version t = new Version(version);
 		
 		assertEquals(">", t.getConstraint());
 		assertEquals("1", t.getMajor());
 		assertEquals("2", t.getMinor());
 		assertEquals("4", t.getFix());
 		assertEquals("5648", t.getBuild());
 		assertEquals("2", t.getSuffix());
 		assertEquals(ComposerConstants.ALPHA, t.getStability());
 		assertEquals(ComposerConstants.BETA, t.getStabilityModifier());
 		
 		v = new Version();
 		v.setStability(ComposerConstants.DEV);
 		v.setMajor("master");
 		v.setDevPosition(Version.BEGIN);
 		assertEquals("dev-master", v.toString());
 		
 		v = new Version();
 		v.setMajor("2");
 		v.setPrefix("v");
 		
 		assertEquals("v2", v.toString());
 	}
 	
 	@Test
 	public void testSymfony() {
 		try {
 			PackageDownloader downloader = new PackagistDownloader();
 			RepositoryPackage pkg = downloader.loadPackage("symfony/symfony");
 			
 			Versions versions = pkg.getVersions();
 			
 			assertNotNull(versions.getMajors());
 			assertTrue(Integer.parseInt(versions.getRecentMajor()) >= 2);
 			assertTrue(Integer.parseInt(versions.getRecentMinor("2")) >= 2);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Test
 	public void testComparison() {
 		Version v1 = new Version("2.0.10");
 		Version v2 = new Version("2.0.11");
 		assertEquals(-1, v1.compareTo(v2));
 		
 		v2.setVersion("2.0.7");
 		assertEquals(1, v1.compareTo(v2));
 		
 		v2.setVersion("2.0.10-beta2");
 		assertEquals(1, v1.compareTo(v2));
 		
 		v1.setVersion("2.0.10-beta3");
 		assertEquals(1, v1.compareTo(v2));
 		
 		v2.setVersion("2.0.x-dev");
 		assertEquals(-1, v1.compareTo(v2));
 	}
 }
