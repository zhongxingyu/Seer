 package au.com.sensis.mobile.crf.showcase.selenium.fixture;
 
 import static junit.framework.Assert.assertTrue;
 
 import com.thoughtworks.selenium.Selenium;
 
 /**
  * Assertions and actions for the BDP Page.
  *
  * @author Adrian.Koh2@sensis.com.au (based on Heather's work in Whereis Mobile)
  */
 public abstract class IphoneBdpPage extends BdpPage {
 
     /**
      * Default constructor.
      *
      * @param selenium Selenium instance to use.
      */
     public IphoneBdpPage(final Selenium selenium) {
         super(selenium);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doAssertPageStructure() {
         assertIphoneCss();
         assertIphoneScript();
         assertIphoneJsp();
         assertImg();
         assertMapText();
     }
 
     private void assertMapText() {
         assertTrue("Map not found", getBrowser().isTextPresent("Map goes here"));
     }
 
     private void assertImg() {
         assertNumImgElements(getExpectedNumImgElements());
         assertImg("tile_bg img not found",
                 "mapZoomIn", "Map Zoom In", "Map Zoom In",
                 "mapComponent-iphone-ipod/component/map/in.png");
         doAssertImg();
     }
 
     /**
      * @return number of expected img elements.
      */
     protected int getExpectedNumImgElements() {
         return 1;
     }
 
     /**
      * Subclasses to override to assert images.
      */
     protected abstract void doAssertImg();
 
     private void assertIphoneCss() {
         final int expectedNumIphoneCssLinks = 10;
         assertNumCssLinks(expectedNumIphoneCssLinks);
         assertCssLink("default/common/main.css link not found",
                 "default/common/main.css");
         assertCssLink("webkit/common/main.css link not found",
                 "webkit/common/main.css");
         assertCssLink("applewebkit/common/main.css link not found",
                 "applewebkit/common/main.css");
         assertCssLink("iphone-ipod/common/main.css link not found",
                 "iphone-ipod/common/main.css");
 
         assertCssLink("iphone-ipod/common/columns.css link not found",
             "iphone-ipod/common/columns.css");
 
         assertCssLink("default/results/results.css link not found",
                 "default/results/results.css");
 
         assertCssLink("webkit/common/jazz.css link not found",
                 "webkit/common/jazz.css");
 
         assertCssLink("webkit/common/decorations.css link not found",
                 "webkit/common/decorations.css");
         assertCssLink("applewebkit/common/decorations.css link not found",
                 "applewebkit/common/decorations.css");
 
         assertCssLink("mapComponent-iphone-ipod/component/map/map.css link not found",
             "mapComponent-iphone-ipod/component/map/map.css");
 
     }
 
     private void assertIphoneScript() {
         final int expectedNumIphoneScripts = 29;
         assertNumScripts(expectedNumIphoneScripts);
 
         assertIphoneScriptByNameNoBundling();
         assertIphoneScriptByAllNoBundling();
     }
 
     private void assertIphoneScriptByNameNoBundling() {
         assertScript("default/common/main.js script not found",
                 "default/common/main.js");
         assertScript("webkit/common/main.js script not found",
                 "webkit/common/main.js");
         assertScript("applewebkit/common/main.js script not found",
                 "applewebkit/common/main.js");
         assertScript("iphone-ipod/common/main.js script not found",
                 "iphone-ipod/common/main.js");
 
         assertScript("iphone-ipod/common/columns.js script not found",
                 "iphone-ipod/common/columns.js");
 
         assertScript("default/results/results.js script not found",
                 "default/results/results.js");
 
         assertScript("webkit/common/jazz.js script not found",
                 "webkit/common/jazz.js");
 
         assertScript("webkit/common/decorations.js script not found",
                 "webkit/common/decorations.js");
         assertScript("applewebkit/common/decorations.js script not found",
                 "applewebkit/common/decorations.js");
     }
 
     private void assertIphoneScriptByAllNoBundling() {
         assertIphoneFielddecoratorScripts();
         assertIphonMapScripts();
         assertIphoneGridScripts();
         assertIphoneAnimationScripts();
         assertIphoneLayersScripts();
     }
 
     private void assertIphonMapScripts() {
         assertScript("default/component/map/map1.js script not found",
                 "default/component/map/map1.js");
         assertScript("default/component/map/map2.js script not found",
                 "default/component/map/map2.js");
         assertScript("webkit/component/map/map1.js script not found",
                 "webkit/component/map/map1.js");
         assertScript("webkit/component/map/map2.js script not found",
                 "webkit/component/map/map2.js");
         assertScript("applewebkit/component/map/map1.js script not found",
                 "applewebkit/component/map/map1.js");
         assertScript("applewebkit/component/map/map2.js script not found",
                 "applewebkit/component/map/map2.js");
         assertScript(
                 "mapComponent-iphone-ipod/component/map/map2.js script not found",
                 "mapComponent-iphone-ipod/component/map/map2.js");
         assertScript(
                 "mapComponent-iphone-ipod/component/map/map1.js script not found",
                 "mapComponent-iphone-ipod/component/map/map1.js");
     }
 
     private void assertIphoneFielddecoratorScripts() {
         assertScript("default/fielddecorators/decorator2.js script not found",
                 "default/fielddecorators/decorator2.js");
         assertScript("default/fielddecorators/decorator1.js script not found",
                 "default/fielddecorators/decorator1.js");
        assertScript("default/fielddecorators/decorator3.js script not found",
                "default/fielddecorators/decorator3.js");
     }
 
     private void assertIphoneGridScripts() {
         assertScript("iphone-ipod/grid/grid2.js script not found",
                 "iphone-ipod/grid/grid2.js");
         assertScript("iphone-ipod/grid/grid1.js script not found",
                 "iphone-ipod/grid/grid1.js");
        assertScript("iphone-ipod/grid/grid3.js script not found",
                "iphone-ipod/grid/grid3.js");
     }
 
     private void assertIphoneAnimationScripts() {
         assertScript("webkit/animation/animation1.js script not found",
                 "webkit/animation/animation1.js");
         assertScript("webkit/animation/animation2.js script not found",
                 "webkit/animation/animation2.js");
     }
 
     private void assertIphoneLayersScripts() {
         assertScript("webkit/layers/layers1.js script not found",
                 "webkit/layers/layers1.js");
         assertScript("webkit/layers/layers2.js script not found",
                 "webkit/layers/layers2.js");
         assertScript("applewebkit/layers/layers1.js script not found",
                 "applewebkit/layers/layers1.js");
         assertScript("applewebkit/layers/layers2.js script not found",
                 "applewebkit/layers/layers2.js");
     }
 
     private void assertIphoneJsp() {
         assertTrue(getBrowser().isTextPresent("[iphone-ipod] bdp.jsp"));
     }
 }
