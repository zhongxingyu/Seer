 package cc.explain.server.api;
 
 import com.google.common.collect.Lists;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.inject.Inject;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * User: kzimnick
  * Date: 27.04.13
  * Time: 18:26
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
         "classpath:spring-context.xml",
         "classpath:spring-dao.xml",
         "classpath:test-spring-dataSource.xml",
         "classpath:spring-security.xml",
         "classpath:spring-tx.xml"
 })
 public class TranslateServiceTest {
 
    @Autowired
    TranslateService translateService;
 
    @Test
    public void shouldReturnTranslatedWordsUsingGoogleTranslate(){
         String word1 = "get up";
         String word2 = "get down";
 
        String[] translated = translateService.translate(new String[]{word1, word2});
 
         assertEquals("wstać", translated[0]);
         assertEquals("schodzić", translated[1]);
    }
 
     @Test
     public void shouldReturnOneTranslatedWord() throws Exception {
         String word = "doghouse";
 
         Map<String, String> translatedWords = translateService.getTranslatedWord(Lists.newArrayList(word));
 
         assertEquals("psia buda", translatedWords);
     }
 
     @Test
     public void shouldReturnTranslatedWordWithCorrectEncoding() throws Exception {
         String word = "thickness";
 
         Map<String, String> databaseTranslated = translateService.getTranslatedWord(Lists.newArrayList(word));
        String[] googleTranslated = translateService.translate(new String[]{word});
 
         assertEquals(databaseTranslated.get(word), googleTranslated[0]);
     }
 
 
     @Test
     public void shouldReturnTranslatedListForEnglishList(){
         ArrayList<String> englishWords = Lists.newArrayList("car", "dog","untranlatablewordxyz");
 
         Map<String,String> translatedWords = translateService.getTranslatedWord(englishWords);
 
         assertEquals(2, translatedWords.size());
         assertEquals("samochód",translatedWords.get("car"));
         assertEquals("pies",translatedWords.get("dog"));
     }
 }
