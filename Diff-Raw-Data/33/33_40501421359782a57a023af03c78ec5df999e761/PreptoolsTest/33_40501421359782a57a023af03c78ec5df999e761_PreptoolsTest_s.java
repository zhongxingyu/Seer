 package ch.sbs.plugin.preptools;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import ch.sbs.utils.preptools.vform.VFormUtil;
 
 public class PreptoolsTest {
 
 	@Test
 	public void testReplace() {
 		assertEquals("<brl:v-form>Sie</brl:v-form>", VFormUtil.replace("Sie"));
 		assertEquals("<brl:v-form>Ihre</brl:v-form>", VFormUtil.replace("Ihre"));
 		assertEquals("<brl:v-form>Ihr</brl:v-form>", VFormUtil.replace("Ihr"));
 		assertEquals("<brl:v-form>Ihren</brl:v-form>",
 				VFormUtil.replace("Ihren"));
 		assertEquals("<brl:v-form>Ihrem</brl:v-form>",
 				VFormUtil.replace("Ihrem"));
 		assertEquals("<brl:v-form>Ihres</brl:v-form>",
 				VFormUtil.replace("Ihres"));
 		assertEquals("<brl:v-form>Deine</brl:v-form>",
 				VFormUtil.replace("Deine"));
 		assertEquals("<brl:v-form>Dein</brl:v-form>", VFormUtil.replace("Dein"));
		assertEquals("nix", VFormUtil.replace("nix"));
		assertEquals("Deintegration", VFormUtil.replace("Deintegration"));
 	}
 
 }
