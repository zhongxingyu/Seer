 package burrito.util;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import siena.PersistenceManager;
 import siena.PersistenceManagerFactory;
 import siena.core.PersistenceManagerLifeCycleWrapper;
 import siena.gae.GaePersistenceManager;
 import burrito.test.TestBase;
 import burrito.test.crud.LinkableEntity;
 
 public class BBCodeCreatorTest extends TestBase {
 
 	@Before
 	public void enableLifeTimeSupport() {
 		PersistenceManager pm = new GaePersistenceManager();
 		pm = new PersistenceManagerLifeCycleWrapper(pm);
 		pm.init(null);
 
 		PersistenceManagerFactory.install(pm, LinkableEntity.class);
 	}
 
 	@Test
 	public void italic() {
 		String bbcode = "[i]text[/i]";
 		String html = BBCodeCreator.generateHTML(bbcode);
 
 		assertEquals("<span style=\"font-style:italic;\">text</span>", html);
 	}
 
 	@Test
 	public void italicBreakLine() {
 		String bbcode = "[i]text \n\r qwe[/i]";
 		String html = BBCodeCreator.generateHTML(bbcode);
 
 		assertEquals("<span style=\"font-style:italic;\">text <br/> qwe</span>", html);
 	}
 
 	@Test
 	public void testUrlLink() {
 		String bbcode = "[url=http://test/url?x=1&y=2]1 < 2[/url]";
 		String html = BBCodeCreator.generateHTML(bbcode);
 
		assertEquals("<a href=\"http://test/url?x=1&amp;y=2\">1 &lt; 2</a>", html);
 
 		bbcode = "[url]http://test/url?x=1&y=2[/url]";
 		html = BBCodeCreator.generateHTML(bbcode);
 
		assertEquals("<a href=\"http://test/url?x=1&amp;y=2\">http://test/url?x=1&amp;y=2</a>", html);
 	}
 
 	@Test
 	public void testLinkableLink() {
 		LinkableEntity linkable = new LinkableEntity();
 		linkable.setUrl("http://test/url?x=1&y=2");
 		linkable.insert();
 
 		String bbcode = "[linkable=burrito.test.crud.LinkableEntity:" + linkable.getId() + "]\"A clean desk is a sign of a sick mind.\"[/linkable]";
 		String html = BBCodeCreator.generateHTML(bbcode);
 
 		assertEquals("<a href=\"http://test/url?x=1&amp;y=2\">&quot;A clean desk is a sign of a sick mind.&quot;</a>", html);
 	}
 }
