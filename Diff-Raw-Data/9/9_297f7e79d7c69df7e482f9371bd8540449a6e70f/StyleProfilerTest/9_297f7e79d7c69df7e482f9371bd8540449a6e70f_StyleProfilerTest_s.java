 package com.wedlum.styleprofile.domain.survey;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.wedlum.styleprofile.domain.photo.PhotoSourceMock;
 import com.wedlum.styleprofile.util.web.JsonUtils;
 
 
 public class StyleProfilerTest {
 
     @Test
    @Ignore
     public void testResolveMiniPalettes() throws Exception {
         Profile profile = new Profile();
         profile.addSession("singleColorSession1", Arrays.asList("1a.png","2a.png"));
         profile.addSession("singleColorSession2", Arrays.asList("1b.png"));
 
         StyleProfiler subject = new StyleProfiler(profile, null);
         Map<String, String> resolved  = subject.resolveAll();
 
         Assert.assertEquals(
         	"{\"miniPalette1\":\"1a_A.png\",\"miniPalette2\":\"2a_A.png\",\"miniPalette3\":\"1b_A.png\"," +
         	 "\"miniPalette4\":\"1a_C.png\",\"miniPalette5\":\"2a_C.png\",\"miniPalette6\":\"1b_C.png\"}",
         	JsonUtils.toJson(resolved)
         );
     }
 
     @Test
     @Ignore
     public void testResolvePalettes() {
     	Profile profile = new Profile();
         profile.addSession("session1", Arrays.asList("1a.png","2b.png"));
         profile.addSession("session2", Arrays.asList("1a.png"));
         profile.photos = Arrays.asList("1a.png", "1a.png", "1a.png", "2b.png", "2b.png");
 
         StyleProfiler subject = new StyleProfiler(profile, null);
         Map<String, String> resolved  = subject.resolveAll();
 
         PhotoSourceMock photoSourceMock = new PhotoSourceMock();
         photoSourceMock.setMetadata(
         		"1a.png",
         		"Photo:\n" + 
         		"   Description:\n" + 
         		"   Photographer:\n" + 
         		"       Drue Carr\n" + 
         		"   Tags:\n" + 
         		"       Colors:\n" + 
         		"           - 000S");
 
         photoSourceMock.setMetadata(
         		"2b.png",
         		"Photo:\n" + 
         		"   Description:\n" + 
         		"   Photographer:\n" + 
         		"       Drue Carr\n" + 
         		"   Tags:\n" + 
         		"       Colors:\n" + 
         		"           - 220S");
 
         photoSourceMock.setMetadata(
         		"palette_red",
         		"Photo:\n" + 
         		"   Description:\n" + 
         		"   Photographer:\n" + 
         		"       Drue Carr\n" + 
         		"   Tags:\n" + 
         		"      PaletteID:\n" + 
         		"         1001   \n" + 
         		"      PaletteType:\n" + 
         		"         Mono\n" + 
         		"      Texture:\n" + 
         		"         Soft\n" + 
         		"      Colors:\n" + 
         		"         - 000D\n" + 
         		"         - 000M\n" + 
         		"         - 000L\n" + 
         		"         - 035N\n" + 
         		"      FeaturedColor:\n" + 
         		"         - 000S\n" + 
         		"      Contrast:\n" + 
         		"         - Mid\n" + 
         		"      Boldness:\n" + 
         		"         - Low\n" + 
         		"      Foundation:\n" + 
         		"         - Mid");
 
         photoSourceMock.setMetadata(
         		"palette_red",
         		"Photo:\n" + 
         		"   Description:\n" + 
         		"   Photographer:\n" + 
         		"       Drue Carr\n" + 
         		"   Tags:\n" + 
         		"      PaletteID:\n" + 
         		"         1001   \n" + 
         		"      PaletteType:\n" + 
         		"         Mono\n" + 
         		"      Texture:\n" + 
         		"         Soft\n" + 
         		"      Colors:\n" + 
         		"         - 000D\n" + 
         		"         - 000M\n" + 
         		"         - 000L\n" + 
         		"         - 035N\n" + 
         		"      FeaturedColor:\n" + 
         		"         - 220S\n" + 
         		"      Contrast:\n" + 
         		"         - Mid\n" + 
         		"      Boldness:\n" + 
         		"         - Low\n" + 
         		"      Foundation:\n" + 
         		"         - Mid");
 
         Assert.assertEquals(
             	"{\"palette1\":\"palette_red.png\",\"palette2\":\"palette_blue.png\"}" ,
             	JsonUtils.toJson(resolved)
         );
     }
 
 }
