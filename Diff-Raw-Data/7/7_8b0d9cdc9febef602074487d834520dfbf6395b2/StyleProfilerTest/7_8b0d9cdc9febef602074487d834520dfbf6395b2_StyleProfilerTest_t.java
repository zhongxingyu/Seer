 package com.wedlum.styleprofile.domain.profile;
 
 import com.wedlum.styleprofile.domain.survey.StyleProfiler;
 import com.wedlum.styleprofile.util.web.JsonUtils;
 import junit.framework.Assert;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class StyleProfilerTest {
 
     @Test
     public void testResolve() throws Exception {
         Map<String, List<String>> profile = new LinkedHashMap<String, List<String>>();
        profile.put("singleColorSession1", Arrays.asList("1a.png","2a.png"));
        profile.put("singleColorSession2", Arrays.asList("1b.png"));
 
         StyleProfiler subject = new StyleProfiler(profile);
         Map<String, String> resolved  = subject.resolveAll();
 
        Assert.assertEquals("{\"miniPalette1\":\"1a_bold.png\",\"miniPalette2\":\"2a_bold.png\",\"miniPalette3\":\"1b_bold.png\"}", JsonUtils.toJson(resolved));
     }
 }
