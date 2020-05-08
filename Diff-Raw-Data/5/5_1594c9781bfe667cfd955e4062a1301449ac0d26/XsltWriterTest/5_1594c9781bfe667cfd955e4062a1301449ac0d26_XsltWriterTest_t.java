 package de.unigoettingen.sub.convert.output;
 
 import static de.unigoettingen.sub.convert.model.builders.ImageBuilder.image;
 import static de.unigoettingen.sub.convert.model.builders.LanguageBuilder.language;
 import static de.unigoettingen.sub.convert.model.builders.LineBuilder.line;
 import static de.unigoettingen.sub.convert.model.builders.MetadataBuilder.metadata;
 import static de.unigoettingen.sub.convert.model.builders.NonWordBuilder.nonWord;
 import static de.unigoettingen.sub.convert.model.builders.PageBuilder.page;
 import static de.unigoettingen.sub.convert.model.builders.ParagraphBuilder.paragraph;
 import static de.unigoettingen.sub.convert.model.builders.TableBuilder.table;
 import static de.unigoettingen.sub.convert.model.builders.WordBuilder.word;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.unigoettingen.sub.convert.api.ConvertWriter;
 import de.unigoettingen.sub.convert.model.Metadata;
 import de.unigoettingen.sub.convert.model.Page;
 import de.unigoettingen.sub.convert.output.XsltWriter;
 
 public class XsltWriterTest {
 
 	private ConvertWriter writer;
 	private OutputStream baos;
 
 	@Before
 	public void setUp() throws Exception {
 		writer = new XsltWriter();
 		writer.addImplementationSpecificOption("xslt", "src/test/resources/xslt/toTei.xsl");
 		baos = new ByteArrayOutputStream();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		baos.close();
 	}
 
 	private String process(Page page) {
 		writer.setTarget(baos);
 		writer.writePage(page);
 		return baos.toString();
 	}
 
 	private String process(Metadata meta) {
 		writer.setTarget(baos);
 		writer.writeMetadata(meta);
 		return baos.toString();
 	}
 
 	@Test
 	public void shouldNotWorkWithoutOutput() {
 		try {
 			writer.writeStart();
 			fail("did not throw exception");
 		} catch (IllegalStateException e) {
 			assertEquals("The output target is not set", e.getMessage());
 		}
 	}
 
 	@Test
 	public void shouldWriteXmlHeaderAndTeiStartElement() {
 		writer.setTarget(baos);
 		writer.writeStart();
 		
 		String output = baos.toString();
 		
 		assertThat(output, containsString("<TEI"));
 	}
 
 	@Test
 	public void emptyMetadataShouldLeadToEmptyTeiHeader() {
 		Metadata meta = metadata().build();
 		String output = process(meta);
 
 		assertThat(output, containsString("<teiHeader"));
 		assertThat(output, containsString("</teiHeader>"));
 	}
 
 	@Test
 	public void outputShouldContainLanguageAndItsValidId() {
 		Metadata meta = metadata().with(language("GermanStandard").withLangId("de")).build();
 		String output = process(meta);
 		
 		assertThat(output, containsString("<language ident=\"de\">GermanStandard</language>"));
 	}
 	
 	@Test
 	public void outputShouldContainInvalidLanguageWithoutId() {
 		Metadata meta = metadata().with(language("SomeUnknownLanguage")).build();
 		String output = process(meta);
 		
 		assertThat(output, containsString("<language>SomeUnknownLanguage</language>"));
 	}
 
 	@Test
 	public void outputShouldContainCreatorInfos() {
 		Metadata meta = metadata().withSoftwareName("Finereader").withSoftwareVersion("8.0").build();
 		String output = process(meta);
 		
 		assertThat(output, containsString("<creation>Finereader 8.0</creation>"));
 	}
 
 	@Test
 	public void outputShouldContainTwoLanguages() {
 		Metadata meta = metadata().with(language("lang1")).with(language("lang2")).build();
 		String output = process(meta);
 
 		writer.writeEnd();
 		assertThat(output, containsString("<language>lang1</language>"));
 		assertThat(output, containsString("<language>lang2</language>"));
 	}
 
 	@Test
 	public void emptyPageShouldResultInAPageBreak() {
 		Page page = new Page();
 		String output = process(page);
 
 		assertThat(output, containsString("<milestone"));
		assertThat(output, containsString("n=\"\" type=\"page\""));
 		assertThat(output, containsString("<pb"));
 	}
 
 	@Test
 	public void pageWithPhysicalNumber() {
 		Page page = new Page();
 		page.setPhysicalNumber(1);
 		String output = process(page);
 
 		assertThat(output, containsString("<milestone "));
		assertThat(output, containsString("n=\"1\" type=\"page\""));
 		assertThat(output, containsString("<pb"));
 	}
 
 	@Test
 	public void secondPageShouldCreateSecondMilestone() {
 		writer.setTarget(baos);
 		Page page = new Page();
 		page.setPhysicalNumber(1);
 		writer.writePage(page);
 		page.setPhysicalNumber(2);
 		writer.writePage(page);
 		
 		String output = baos.toString();
 		assertThat(output, containsString("<milestone "));
 		assertThat(output, containsString("n=\"2\""));
 	}
 
 	@Test
 	public void documentWithOnePage() {
 		writer.setTarget(baos);
 		writer.writeStart();
 		writer.writePage(new Page());
 		writer.writeEnd();
 		String output = baos.toString();
 
 		assertThat(output, containsString("<TEI"));
 		output = output.replaceAll("\\n", " ");
 		assertTrue("text and body are in the wrong place", output.matches(".*<text>\\s*<body>\\s*<milestone.*"));
 		assertTrue("text and body must close just before tei", output.matches(".*</body>\\s*</text>\\s*</TEI>.*"));
 	}
 	
 	@Test
 	public void completeDocumentWithMetaAndPage() {
 		writer.setTarget(baos);
 		writer.writeStart();
 		writer.writeMetadata(new Metadata());
 		writer.writePage(new Page());
 		writer.writeEnd();
 		String output = baos.toString();
 //		System.out.println(output);
 
 		assertThat(output, containsString("<TEI"));
 		output = output.replaceAll("\\n", " ");
 		assertTrue("header should be before text and body", output.matches(".*</teiHeader>\\s*<text>\\s*<body>.*"));
 		assertTrue("text and body must close just before tei", output.matches(".*</body>\\s*</text>\\s*</TEI>.*"));
 	}
 	
 	@Test
 	public void paragraphShouldGetAnID() {
 		Page page = page().with(paragraph()).build();
 		String output = process(page);
 
 		assertThat(output, containsString("<p "));
 		assertThat(output, containsString("id=\"ID1_1\""));
 	}
 
 	@Test
 	public void paragraphIDShouldBeIncremented() {
 		Page page = page().with(paragraph()).with(paragraph()).build();
 		String output = process(page);
 
 		assertThat(output, containsString("<p "));
 		assertThat(output, containsString("id=\"ID1_2\""));
 	}
 
 	@Test
 	public void addLineBreakAfterALine() {
 		Page page = page().with(line()).build();
 		String output = process(page);
 
 		assertThat(output, containsString("<lb/>"));
 	}
 
 	@Test
 	public void shouldWrapWordInTags() {
 		Page page = page().with(word("test")).build();
 		String output = process(page);
 
 		assertThat(output, containsString("<w>test</w>"));
 	}
 
 	@Test
 	public void shouldNotWrapNonWordInTags() {
 		Page page = page().with(nonWord("...")).build();
 		String output = process(page);
 		
 		assertThat(output, not(containsString("<w>...</w>")));
 		assertThat(output, containsString("<pc>...</pc>"));
 	}
 
 	@Test
 	public void shouldAddWordCoordinates() {
 		Page page = page().with(word("test").withCoordinatesLTRB(1, 2, 3, 4)).build();
 		String output = process(page);
 
 		assertThat(output, containsString("<w function=\"1,2,3,4\">test</w>"));
 	}
 
 	@Test
 	public void shouldWriteFigure() {
 		Page page = page().with(image().withCoordinatesLTRB(1,2,3,4)).build();
 		String output = process(page);
 
 		assertThat(output, containsString("<figure"));
 		assertThat(output, containsString("id=\"ID1_1\""));
 		assertThat(output, containsString("function=\"1,2,3,4\""));
 	}
 
 	@Test
 	public void shouldCreateTableWithCoordinates() {
 		Page page = page().with(table().withCoordinatesLTRB(1, 2, 3, 4).with(word("a"))).build();
 		String output = process(page);
 		
 		assertThat(output, containsString("<table"));
 		assertThat(output, containsString("function=\"1,2,3,4\""));
 		assertThat(output, containsString("rows=\"1\""));
 		assertThat(output, containsString("cols=\"1\""));
 		assertThat(output, containsString("<row>"));
 		assertThat(output, containsString("<cell>"));
 		assertThat(output, containsString("<w>a</w>"));
 	}
 	
 	@Test
 	public void stylsheetWithoutMetadataAndWithRegex() {
 		writer.addImplementationSpecificOption("xslt", "src/test/resources/xslt/toExampleXml.xsl");
 		writer.setTarget(baos);
 		writer.writeStart();
 		writer.writePage(new Page());
 		writer.writeEnd();
 		String output = baos.toString();
 		
 		assertThat(output, containsString("<root>"));
 		assertThat(output, containsString("<child>"));
 		assertThat(output, containsString("regex characters"));
 		assertThat(output, containsString("</child>"));
 		assertThat(output, containsString("</root>"));
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void noStylesheet() {
 		writer.addImplementationSpecificOption("xslt", null);
 		writer.setTarget(baos);
 		writer.writeStart();
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void wrongStylesheetPath() {
 		writer.addImplementationSpecificOption("xslt", "/doesnotexist");
 		writer.setTarget(baos);
 		writer.writeStart();
 	}
 	
 	@Test
 	public void xslt2shouldAlsoWork() {
 		Page page = page().with(word("test")).build();
 		writer.addImplementationSpecificOption("xslt", "src/test/resources/xslt/toExampleXmlXslt20.xsl");
 		String output = process(page);
 
 		assertThat(output, containsString("SOME TEXT"));
 	}
 
 }
