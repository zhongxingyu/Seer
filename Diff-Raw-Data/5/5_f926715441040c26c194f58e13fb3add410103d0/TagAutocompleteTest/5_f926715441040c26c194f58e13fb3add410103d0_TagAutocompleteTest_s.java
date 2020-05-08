 package com.wedlum.styleprofile.business.model;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class TagAutocompleteTest {
 
     @Test
     public void testAutocomplete(){
         PhotoSourceMock photoSourceMock = new PhotoSourceMock();
         TagAutocomplete subject = TagAutocomplete.on(photoSourceMock);
 
         photoSourceMock.setMetadata("42.png",
                 "Tag:\n" +
                 "   Sub-Tag: \n" +
                 "      - Tag Value 1\n" +
                 "      - Tag Value 2");
 
         Map<String, Set<String>> suggestMap = subject.getSuggestions();
         Assert.assertEquals(
                "Root [Tag]\n" +
                 "Root/Tag [Sub-Tag]\n" +
                "Root/Tag/Sub-Tag [Tag Value 1, Tag Value 2]",
                 toString(suggestMap)
         );
     }
 
     @Test
     public void testEmptyInput(){
         PhotoSourceMock photoSourceMock = new PhotoSourceMock();
         TagAutocomplete subject = TagAutocomplete.on(photoSourceMock);
 
         photoSourceMock.setMetadata("42.png","");
 
         Map<String, Set<String>> suggestMap = subject.getSuggestions();
         Assert.assertEquals("", toString(suggestMap)
         );
     }
 
     @Test
     public void testDuplicateOutuputEntries() {
     	 PhotoSourceMock photoSourceMock = new PhotoSourceMock();
          TagAutocomplete subject = TagAutocomplete.on(photoSourceMock);
 
          photoSourceMock.setMetadata("42.png", "Tag:");
          photoSourceMock.setMetadata("43.png", "Tag:");
 
          Map<String, Set<String>> suggestMap = subject.getSuggestions();
          Assert.assertEquals("Root [Tag]", toString(suggestMap));
 	}
 
     private String toString(Map<String, Set<String>> suggestMap) {
         String result = "";
         for(Map.Entry<String, Set<String>> entry : suggestMap.entrySet())
             result += entry.getKey() + " " + Arrays.toString(entry.getValue().toArray()) + "\n" ;
         return result.trim();
     }
 }
