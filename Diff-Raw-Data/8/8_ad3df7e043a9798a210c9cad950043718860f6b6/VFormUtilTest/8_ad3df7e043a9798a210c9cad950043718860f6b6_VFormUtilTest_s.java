 package ch.sbs.utils.preptools.vform;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import ch.sbs.plugin.preptools.VFormActionHelper;
 import ch.sbs.utils.preptools.MarkupUtil;
 import ch.sbs.utils.preptools.Match;
 import ch.sbs.utils.preptools.RegionSkipper;
 
 public class VFormUtilTest {
 
 	@Test
 	public void testMatchesNull() {
 		assertFalse(VFormUtil.matches(null));
 	}
 
 	@Test
 	public void testBug1383() {
 		assertTrue(VFormUtil.matches("Eurer"));
 		assertTrue(VFormUtil.matches("Euerm"));
 		assertTrue(VFormUtil.matches("Euern"));
 	}
 
 	@Test
 	public void testReplaceMatches() {
 		assertTrue(VFormUtil.matches("Deine"));
 		assertFalse(VFormUtil.matches("Sieb"));
 	}
 
 	@Test
 	public void testNoMatchesMarkup() {
 		assertTrue(VFormUtil.matches("Deine"));
 		assertFalse(VFormUtil.matches(MarkupUtil.wrap("Deine", "brl:v-form")));
 	}
 
 	@Test
 	public void testReplaceKeep() {
 		MarkupUtil mu = new MarkupUtil(RegionSkipper.getCommentSkipper());
 		assertEquals(Match.NULL_MATCH,
 				mu.find("Sieb", 0, VFormUtil.getAllPattern()));
 		final String text = "Sieben können Sie haben.";
 		final Match match = mu.find(text, 0, VFormUtil.getAllPattern());
 		final String vform = "Sie";
 		assertEquals(text.lastIndexOf(vform), match.startOffset);
 		assertEquals(text.lastIndexOf(vform) + vform.length(), match.endOffset);
 	}
 
 	@Test
 	public void testMatch() {
 
 		final String text = "Das können Sie zu Ihren Akten legen.";
 		final MarkupUtil mu = new MarkupUtil(RegionSkipper.getCommentSkipper());
 
 		Match match = mu.find(text, 0, VFormUtil.getAllPattern());
 
 		final String vform = "Sie";
 		assertEquals(text.indexOf(vform), match.startOffset);
 		assertEquals(text.indexOf(vform) + vform.length(), match.endOffset);
 
 		match = mu.find(text, match.endOffset, VFormUtil.getAllPattern());
 
 		final String vform2 = "Ihren";
 		assertEquals(text.indexOf(vform2), match.startOffset);
 		assertEquals(text.indexOf(vform2) + vform2.length(), match.endOffset);
 
 		match = mu.find(text, match.endOffset, VFormUtil.getAllPattern());
 
 		assertEquals(Match.NULL_MATCH, match);
 
 	}
 
 	@Test
 	public void testMatchBoundary() {
 
 		final String text = "Dann können Sie's Ihrem Kollegen geben.";
 		final MarkupUtil mu = new MarkupUtil(RegionSkipper.getCommentSkipper());
 
 		Match match = mu.find(text, 0, VFormUtil.getAllPattern());
 
 		final String vform = "Sie";
 		assertEquals(text.indexOf(vform), match.startOffset);
 		assertEquals(text.indexOf(vform) + vform.length(), match.endOffset);
 
 		match = mu.find(text, match.endOffset, VFormUtil.getAllPattern());
 
 		final String vform2 = "Ihrem";
 		assertEquals(text.indexOf(vform2), match.startOffset);
 		assertEquals(text.indexOf(vform2) + vform2.length(), match.endOffset);
 
 		match = mu.find(text, match.endOffset, VFormUtil.getAllPattern());
 
 		assertEquals(Match.NULL_MATCH, match);
 
 	}
 
 	@Test
 	public void testNoMatch() {
 
 		final MarkupUtil mu = new MarkupUtil(RegionSkipper.getCommentSkipper());
 		final String text = "Dann kann Anna es ihrem Kollegen geben.";
 
 		final Match match = mu.find(text, 0, VFormUtil.getAllPattern());
 
 		assertEquals(Match.NULL_MATCH, match);
 
 	}
 
 	@Test
 	public void testMatch1() {
 
 		final MarkupUtil mu = new MarkupUtil(RegionSkipper.getCommentSkipper());
 		final String text = "Dann kann Anna es Ihrem Kollegen geben.";
 
 		final Match match = mu.find(text, 0, VFormUtil.getAllPattern());
 
 		final String vform = "Ihrem";
 		assertEquals(text.indexOf(vform), match.startOffset);
 		assertEquals(text.indexOf(vform) + vform.length(), match.endOffset);
 
 	}
 
 	@Test
 	public void testNoMatch1() {
 
 		final String tag = "brl:v-form";
 		final MarkupUtil mu = new MarkupUtil(
 				RegionSkipper.makeMarkupRegionSkipper(tag));
 
 		assertEquals(
 				Match.NULL_MATCH,
 				mu.find("Dann kann Anna es " + MarkupUtil.wrap("Ihrem", tag)
 						+ " Kollegen geben.", 0, VFormUtil.getAllPattern()));
 
 	}
 
 	@Test
 	public void testSettingPatternAll() {
 
 		final String text = "Dann kann Anna es Deinem Kollegen geben.";
 		final MarkupUtil mu = new MarkupUtil(
 				RegionSkipper
 						.makeMarkupRegionSkipper(VFormActionHelper.VFORM_TAG));
 		final Match match = mu.find(text, 0, VFormUtil.getAllPattern());
 
 		final String vform = "Deinem";
 		assertEquals(text.indexOf(vform), match.startOffset);
 		assertEquals(text.indexOf(vform) + vform.length(), match.endOffset);
 
 	}
 
 	@Test
 	public void testSettingPattern3rdPP() {
 
 		final MarkupUtil mu = new MarkupUtil(
 				RegionSkipper
 						.makeMarkupRegionSkipper(VFormActionHelper.VFORM_TAG));
 		assertEquals(mu.find("Dann kann Anna es Deinem Kollegen geben.", 0,
 				VFormUtil.get3rdPPPattern()), Match.NULL_MATCH);
 
 	}
 }
