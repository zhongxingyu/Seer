 package de.unigoettingen.sub.convert.impl;
 
 import static de.unigoettingen.sub.convert.model.builders.ImageBuilder.image;
 import static de.unigoettingen.sub.convert.model.builders.LanguageBuilder.language;
 import static de.unigoettingen.sub.convert.model.builders.LineBuilder.line;
 import static de.unigoettingen.sub.convert.model.builders.MetadataBuilder.metadata;
 import static de.unigoettingen.sub.convert.model.builders.NonWordBuilder.nonWord;
 import static de.unigoettingen.sub.convert.model.builders.PageBuilder.page;
 import static de.unigoettingen.sub.convert.model.builders.WordBuilder.word;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import nl.siegmann.epublib.domain.Book;
 import nl.siegmann.epublib.domain.Resource;
 import nl.siegmann.epublib.epub.EpubReader;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.unigoettingen.sub.convert.api.ConvertWriter;
 import de.unigoettingen.sub.convert.model.Metadata;
 import de.unigoettingen.sub.convert.model.Page;
 
 public class EPUBWriterTest {
 
 	private ConvertWriter writer;
 	private ByteArrayOutputStream epubBaos;
 
 	@Before
 	public void setUp() throws Exception {
 		writer = new EPUBWriter();
 		epubBaos = new ByteArrayOutputStream();
 		writer.setTarget(epubBaos);
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		epubBaos.close();
 	}
 
 	@Test
 	public void writesEmptyEpub() throws IOException {
 		writer.writeStart();
 		writer.writeEnd();
 		Book book = writtenBook();
 		
 		assertEquals("# of reachable book resources", 0, book.getContents().size());
 	}
 	
 	@Test
 	public void writesLanguageId() throws IOException {
 		Metadata meta = metadata().with(language("German").withLangId("de")).build();
 		
 		writeToEpubBook(meta);
 		Book book = writtenBook();
 		
 		assertEquals("language", "de", book.getMetadata().getLanguage());
 	}
 	
 	@Test
 	public void languageDefaultsToEnglish() throws IOException {
 		Metadata emptyMeta = metadata().build();
 		
 		writeToEpubBook(emptyMeta);
 		Book book = writtenBook();
 		
 		assertEquals("language", "en", book.getMetadata().getLanguage());
 	}
 	
 	private void writeToEpubBook(Metadata meta) {
 		writer.writeStart();
 		writer.writeMetadata(meta);
 		writer.writeEnd();
 	}
 	
 	@Test
 	public void writesOneEmptyPage() throws IOException {
 		Page page = page().build();
 		writeToEpubBook(page);
 		
 		Book book = writtenBook();
 		
 		assertEquals("# of reachable book resources", 1, book.getContents().size());
 
 		Resource firstPage = book.getContents().get(0);
 		String rawHtml = toHtml(firstPage);
 		
 		assertEquals("media type", "application/xhtml+xml", firstPage.getMediaType().toString());
 		assertEquals("encoding", "UTF-8", firstPage.getInputEncoding());
 		assertThat(rawHtml, containsString("<div id=\"page1\""));
 	}
 	
 	@Test
 	public void writesTwoEmptyPages() throws IOException {
 		Page page = page().build();
 		writeToEpubBook(page, page);
 		
 		Book book = writtenBook();
 		assertEquals("# of reachable book resources", 2, book.getContents().size());
 
 		Resource firstPage = book.getContents().get(0);
 		String rawHtml = toHtml(firstPage);
 		assertThat(rawHtml, containsString("<div id=\"page1\""));
 		
 		Resource secondPage = book.getContents().get(1);
 		String rawHtml2 = toHtml(secondPage);
 		assertThat(rawHtml2, containsString("<div id=\"page2\""));
 	}
 	
 	@Test
 	public void writesPageWithText() throws IOException {
 		Page page = page().with(word("test")).build();
 		writeToEpubBook(page);
 		
 		String rawHtml = firstHtmlPage();
 		assertThat(rawHtml, containsString("test"));
 	}
 	
 	@Test
 	public void writesPageWithPuncuations() throws IOException {
 		Page page = page().with(nonWord("!!?")).build();
 		writeToEpubBook(page);
 		
 		String rawHtml = firstHtmlPage();
 		assertThat(rawHtml, containsString("!!?"));
 	}
 	
 	@Test
 	public void writesPageWithUmlauts() throws IOException {
 		Page page = page().with(word("üß")).build();
 		writeToEpubBook(page);
 		
 		String rawHtml = firstHtmlPage();
 		assertThat(rawHtml, containsString("üß"));
 	}
 	
 	@Test
 	public void writesPageWithEmptyLine() throws IOException {
 		Page page = page().with(line()).build();
 		writeToEpubBook(page);
 		
 		String rawHtml = firstHtmlPage();
 		assertThat(rawHtml, containsString("<br/>"));
 	}
 	
 	@Test
 	public void writesPageWithScan() throws IOException {
 		Page page = page().withHeight(3655).build();
 		writer.addImplementationSpecificOption("scans", "src/test/resources/withOneImage");
 		writeToEpubBook(page);
 		
 		String rawHtml = firstHtmlPage();
 		assertThat(rawHtml, containsString("<img src=\"scan1.png\""));
 	}
 	
 	@Test
 	public void writesPageWithSubimageAndScan() throws IOException {
 		Page page = page().withHeight(3655).
 				with(image().withCoordinatesLTRB(956, 2112, 1464, 2744)).build();
 		writer.addImplementationSpecificOption("scans", "src/test/resources/withOneImage");
 		writeToEpubBook(page);
 		
 		String rawHtml = firstHtmlPage();
 		assertThat(rawHtml, containsString("<img src=\"scan1.png\""));
 		assertThat(rawHtml, containsString("<img src=\"subimage1-1.png\""));
 	}
 	
 	@Test
 	public void writesPageWithSubimageButWithoutScan() throws IOException {
 //		writer.setTarget(new FileOutputStream("/tmp/out.epub"));
 		Page page = page().withHeight(3655).
 				with(image().withCoordinatesLTRB(956, 2112, 1464, 2744)).build();
 		writer.addImplementationSpecificOption("scans", "src/test/resources/withOneImage");
 		writer.addImplementationSpecificOption("includescans", "false");
 		writeToEpubBook(page);
 		
 		String rawHtml = firstHtmlPage();
 		assertThat(rawHtml, not(containsString("<img src=\"scan1.png\"")));
 		assertThat(rawHtml, containsString("<img src=\"subimage1-1.png\""));
 	}
 	
 	
 	
 	private String firstHtmlPage() throws IOException {
 		Book book = writtenBook();
 		Resource firstPage = book.getContents().get(0);
 		return toHtml(firstPage);
 	}
 
 	private void writeToEpubBook(Page... pages) {
 		writer.writeStart();
 		for(Page page : pages) {
 			writer.writePage(page);
 		}
 		writer.writeEnd();
 	}
 	
 	private Book writtenBook() throws IOException {
 		EpubReader reader = new EpubReader();
 		
 		byte[] bytes = epubBaos.toByteArray();
 		ByteArrayInputStream epubIn = new ByteArrayInputStream(bytes);
 		
 		return reader.readEpub(epubIn);
 	}	
 
 	private String toHtml(Resource htmlPage) throws IOException {
		return new String(htmlPage.getData(), "UTF-8");
 	}
 	
 }
