 package cc.explain.server.subtitle;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
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
 
 }
