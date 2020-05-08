 package cc.explain.server.subtitle;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import antlr.RecognitionException;
 import org.junit.Test;
 
 public class OnlyTranslationsSubtitleStrategyTest extends AbstractTest{
 
 	@Test
 	public void shouldAddTranslationToText() throws IOException, RecognitionException {
 		Map<String, String> translations = new HashMap<String, String>();
 		translations.put("night", "noc");
 		translations.put("started", "wystartowała");
 		translations.put("house", "dom");
 		Subtitle subtitle = createSubtitleFromFileWithTranslations("/subtitle.srt", translations);
 		
 		Subtitle processedSubtitle = new OnlyTranslationsSubtitleStrategy().process(subtitle, SubtitleUtilsTest.PATTERN, null);
 		
 		assertEquals("night = noc\nstarted = wystartowała\n", processedSubtitle.getSubtitleElements().get(0).getText());
 		assertEquals("", processedSubtitle.getSubtitleElements().get(1).getText());
 		assertEquals("house = dom\n", processedSubtitle.getSubtitleElements().get(2).getText());
 	}
 
    	@Test
	public void shouldProcessSubtitleForTestPhrasalVerbs() throws IOException, RecognitionException {
		Map<String, String> phraslaVerbs = new HashMap<String, String>();
		phraslaVerbs.put("get away", "odejdz");

		Subtitle subtitle = createSubtitleFromFile("/subtitleWithPhrasalVerbs.srt", Collections.<String, String>emptyMap(), phraslaVerbs);
		Subtitle processedSubtitle = new OnlyTranslationsSubtitleStrategy().process(subtitle, SubtitleUtilsTest.PATTERN, SubtitleUtilsTest.PATTERN);

		assertEquals("(get away = odejdz)", processedSubtitle.getSubtitleElements().get(1).getText());
	}

 }
