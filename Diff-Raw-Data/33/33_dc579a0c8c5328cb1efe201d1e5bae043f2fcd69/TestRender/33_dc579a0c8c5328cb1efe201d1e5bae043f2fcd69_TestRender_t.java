 /*
  * Created on 8 Jan 2008
  */
 package uk.org.ponder.rsf.test.render;
 
 import uk.org.ponder.rsf.bare.RenderResponse;
 import uk.org.ponder.rsf.bare.junit.MultipleRSFTests;
 
 /** Test for ID NPE RSF-71, Verbatim RSF-85, UIMessage RSF-73, 
  * Relation target RSF-77, UISelect grouping RSF-108, cluster ID computation RSF-65
  */
 
 public class TestRender extends MultipleRSFTests {
   
   public TestRender() {
     contributeRequestConfigLocation("classpath:uk/org/ponder/rsf/test/render/render-request-context.xml");
     contributeConfigLocation("classpath:uk/org/ponder/rsf/test/render/render-application-context.xml");
   }
    
   public void testRender() {
     RenderResponse response = getRequestLauncher().renderView();
     assertRenderError(response, false);
     
     assertContains(response, "Test Message ID View");
     assertContains(response, "</div>");
     assertContains(response, "id=\"titleclosed\"");
     assertContains(response, "<i style=\"color");
     assertContains(response, "Referenced value");
     assertContains(response, "Remanent text");
     // For RSF-77
     assertContains(response, "for=\"row::target\"");
     assertContains(response, "for=\"row:1:target\"");
     // For RSF-108
     assertContains(response, "<optgroup label=\"A\">");
     assertContains(response, "<optgroup label=\"B\">");
     // For RSF-111
     assertContains(response, "for=\"target2\"");
     // For RSF-65
     assertContains(response, "select-2-selection");
    // For RSF-115
    assertContains(response, "<span > <a  href=\"http://place\">Link Text</a> </span>");
   }
 }
