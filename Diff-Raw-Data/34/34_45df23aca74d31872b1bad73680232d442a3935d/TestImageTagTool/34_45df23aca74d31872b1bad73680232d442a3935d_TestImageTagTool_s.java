 package de.thischwa.pmcms.view.context.object.tagtool;
 
 import static org.junit.Assert.*;
 
import org.junit.After;
 import org.junit.AfterClass;
import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.thischwa.pmcms.Constants;
 import de.thischwa.pmcms.configuration.BasicConfigurator;
 import de.thischwa.pmcms.configuration.InitializationManager;
 import de.thischwa.pmcms.livecycle.PojoHelper;
 import de.thischwa.pmcms.livecycle.SiteHolder;
 import de.thischwa.pmcms.model.domain.pojo.Site;
 import de.thischwa.pmcms.model.tool.SitePersister;
 
 public class TestImageTagTool {
 
 	private static Site site;
 	private static SiteHolder siteHolder;
 	private static PojoHelper pojoHelper;
 
 	@BeforeClass
 	public static void setUp() throws Exception {
 		InitializationManager.start(new BasicConfigurator(Constants.APPLICATION_DIR), false);
 		site = SitePersister.read("pmcms.demo.site");
 		siteHolder = InitializationManager.getBean(SiteHolder.class);
 		siteHolder.setSite(site);
 		pojoHelper = new PojoHelper();
 		pojoHelper.putpo(siteHolder.get(11));
 	}
 
 	@AfterClass
 	public static void tearDown() throws Exception {
 		InitializationManager.getBean(SiteHolder.class).deleteSite();
 		InitializationManager.end();
 	}
 
 	@Test
 	public void testLayout() {
 		ImageTagTool itt = InitializationManager.getBean(ImageTagTool.class);
 		itt.setPojoHelper(pojoHelper);
		itt.setSrc("/site/layout/test.png");
 		itt.setWidth(150);
 		itt.setHeight(100);
 		String actual = itt.contructTag();
 		assertEquals("<img height=\"100\" width=\"150\" src=\"/site/layout/test.png\" />", actual);
 	}
 
 	@Test
 	public void testUsedFromEditor() {
 		ImageTagTool itt = InitializationManager.getBean(ImageTagTool.class);
 		itt.setPojoHelper(pojoHelper);
		//itt.isUsedFromEditor();
 		itt.setSrc("/site/image/test.png");
 		itt.setWidth(150);
 		itt.setHeight(100);
 		String actual = itt.contructTag();
 		assertEquals("<img height=\"100\" width=\"150\" src=\"/site/image/test.png\" />", actual);
 	}
 }
