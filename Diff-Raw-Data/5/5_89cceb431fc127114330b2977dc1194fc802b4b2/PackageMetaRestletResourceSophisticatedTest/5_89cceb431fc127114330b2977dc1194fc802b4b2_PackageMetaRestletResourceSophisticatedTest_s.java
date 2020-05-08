 package at.ikt.ckan.rest;
 
 import static junit.framework.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import at.ikt.ckan.entities.PackageMeta;
 
 public class PackageMetaRestletResourceSophisticatedTest {
 
 	private RestletResourceFactory resourceFactory;
 
 	@Before
 	public void setUp() throws Exception {
 		resourceFactory = new RestletResourceFactory("clap://class/",
 				".ckan.json");
 	}
 
 	@Test
 	public void testWithFileSophisticatedFile() {
 		PackageMetaRestletResource packageMetaResource = resourceFactory
 				.createPackageMetaResource("hauptwohsitzbevolkerung-geschlecht-und-familienstand");
 		PackageMeta packageMeta = packageMetaResource.get();
 
 		System.out.println(packageMeta);
 
 		assertEquals("hauptwohsitzbevolkerung-geschlecht-und-familienstand",
 				packageMeta.getName());
 		assertEquals("7e9bd962-afea-4e54-9640-73c2c749845b",
 				packageMeta.getId());
 		assertEquals("1.0", packageMeta.getVersion());
 		assertEquals("active", packageMeta.getState());
 
 		assertEquals(
				"Hauptwohnsitzbevlkerung - Geschlecht und Familienstand ",
 				packageMeta.getTitle());
 
 		assertEquals("Open Commons Region Linz", packageMeta.getMaintainer());
 		assertEquals("Magistrat der Landeshauptstadt Linz, Stadtforschung",
 				packageMeta.getAuthor());
 
 		assertNotNull(packageMeta.getNotes());
 		assertNotNull(packageMeta.getNotesRendered());
 
 		assertEquals(
 				"<p>Tabelle mit Altersgruppen.\n"
 						+ "</p>\n"
						+ "<p>1) zum Familienstand \"ledig\" wurden auch Personen mit unbekanntem Familienstand gezhlt\n"
 						+ "   2) derzeit keine \"hinterbliebene eingetragene PartnerInnen\".\n"
 						+ "</p>", packageMeta.getNotesRendered());
 
 		assertEquals(
 				"http://ckan.data.linz.gv.at/package/hauptwohsitzbevolkerung-geschlecht-und-familienstand",
 				packageMeta.getCkanUrl());
 		assertEquals(
 				"http://data.linz.gv.at/resource/population/hauptwohnsitzbevoelkerung_alter_familienstand/2JSCH.PDF",
 				packageMeta.getDownloadUrl());
 		assertEquals(
 				"http://www.linz.at/zahlen/040_Bevoelkerung/040_Hauptwohnsitzbevoelkerung/010_Bevoelkerungspyramiden/",
 				packageMeta.getUrl());
 
 		assertEquals("OKD Compliant::Creative Commons Attribution",
 				packageMeta.getLicense());
 		assertEquals("cc-by", packageMeta.getLicenseId());
 	}
 }
