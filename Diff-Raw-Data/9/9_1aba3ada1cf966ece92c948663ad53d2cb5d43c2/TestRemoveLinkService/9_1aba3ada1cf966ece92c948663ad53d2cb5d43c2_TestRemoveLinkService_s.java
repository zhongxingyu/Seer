 package br.com.haxor.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.codec.binary.Base64;
 import org.junit.Test;
 
 /**
  * Example urls:
    http://www.megaupload.com/?d=G6ZFTBJW
    http://clubedodownload.info/link/?url=http://www.megaupload.com/?d=G6ZFTBJW
    http://fire.tiozao.net/?url=Sjh56Jm/elif/moc.evreselif.www//:ptth
    http://linkprotegido.info/link/?url=http://yess.me/ir/id/aHR0cDovL3d3dy4yc2hhcmVkLmNvbS92aWRlby9NeFFiYWtXRi9PQVBERkVJLmh0bWw=/  = "http://www.2shared.com/video/MxQbakWF/OAPDFEI.html"
    
    Casos que ainda não atendem:
    http://meggacelular.com/baixando/?link=rar.esooL_-_odatruF_ylleN/99448921/selif/moc.erahsdipar//:ptth
    http://www.protetordelinks.com/links/?go!aHR0cDovL2xpeC5pbi8tNTgzNmQ3
    
    ahiosihoasdhioahisodhttp://ahiosehioase
    jdownloader
  */
 public class TestRemoveLinkService {
 	
 	private RemoveLinkService service = new RemoveLinkServiceImpl();
 
 	@Test
 	public void decodeBase64(){
 		// Vou testar soh o decode aqui.
 		String code = "aHR0cDovL3d3dy4yc2hhcmVkLmNvbS92aWRlby9NeFFiYWtXRi9PQVBERkVJLmh0bWw=/";
 		
 		String decoded = new String(Base64.decodeBase64(code.getBytes()));
 		assertNotNull(decoded);
 		assertEquals("http://www.2shared.com/video/MxQbakWF/OAPDFEI.html", decoded);
 	}
 	
 	@Test
 	public void breakSimpleURL() throws Exception{
 		String wrongUrl = "http://www.megaupload.com/?d=G6ZFTBJW";
 		String url = service.breakUrl(wrongUrl);
 		
 		assertNotNull(url);
 		assertEquals(wrongUrl, url);
 	}
 	
 	@Test 
 	public void breakCommomProtectedUrl() throws Exception{
 		String wrongUrl = "http://clubedodownload.info/link/?url=http://www.megaupload.com/?d=G6ZFTBJW";
 		String url = service.breakUrl(wrongUrl);
 		
 		assertNotNull(url);
 		assertEquals("http://www.megaupload.com/?d=G6ZFTBJW", url);
 	}
 
 	@Test
 	public void breakBase64Url() throws Exception{
 		String wrongUrl = "http://linkprotegido.info/link/?url=http://yess.me/ir/id/aHR0cDovL3d3dy4yc2hhcmVkLmNvbS92aWRlby9NeFFiYWtXRi9PQVBERkVJLmh0bWw=/";
 		String url = service.breakUrl(wrongUrl);
 		
 		assertNotNull(url);
 		assertEquals("http://www.2shared.com/video/MxQbakWF/OAPDFEI.html", url);
 	}
 	
 	@Test
 	public void breakUrlWithBlankSpaces() throws Exception{
 		String wrongUrl = "  http://linkprotegido.info/link/?url=http://yess.me/ir/id/aHR0cDovL3d3dy4yc2hhcmVkLmNvbS92aWRlby9NeFFiYWtXRi9PQVBERkVJLmh0bWw=/  ";
 		String url = service.breakUrl(wrongUrl);
 		
 		assertNotNull(url);
 		assertEquals("http://www.2shared.com/video/MxQbakWF/OAPDFEI.html", url);
 	}
 
 	@Test
 	public void breakInvalidUrl(){
 		try {
 			String wrongUrl = "fanfarrao.com";
 			service.breakUrl(wrongUrl);
 			
 			fail("url should throw exception");
 		} catch (Exception e) {
 		}
 	}
 	
 	@Test
 	public void breakEmptyUrl(){
 		try {
 			String wrongUrl = "";
 			service.breakUrl(wrongUrl);
 			
 			fail("url should throw exception");
 		} catch (Exception e) {
 		}
 	}
 	
 	@Test
 	public void breakInvertedUrl() throws Exception{
 		String wrongUrl = "http://fire.tiozao.net/?url=Sjh56Jm/elif/moc.evreselif.www//:ptth";
 		String url = service.breakUrl(wrongUrl);
 		
 		assertNotNull(url);
 		assertEquals("http://www.fileserve.com/file/mJ65hjS", url);
 	}
 
 	@Test
 	public void breakComplexUrl() throws Exception{
 		try {
 			String wrongUrl = "http://linkprotegido.info/link/?url=http://yess.me/ir/id/aHR0cDovL3d3dy4yc2hhcmVkLmNvbS92aWRlby9NeFFiYWtXRi9PQVBERkVJLmh0bWw=/  = \"http://www.2shared.com/video/MxQbakWF/OAPDFEI.html\"";
 			service.breakUrl(wrongUrl);
 			
 			fail("url should throw exception");
 		} catch (Exception e) {
 		}
 	}
 	
 	@Test
 	public void testandoMatcher(){  
 		Pattern padrao = Pattern.compile("[a-zA-Z_0-9]") ;
 		Matcher matcher = padrao.matcher("/a/bbc");
 		while (matcher.find()){
 			System.out.println(matcher.group());
 		}
 	}
 
 }
