 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.core.doc.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
import java.util.TimeZone;
 import java.util.Map.Entry;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 
 import junit.framework.TestCase;
 import junitx.framework.ArrayAssert;
 import junitx.framework.FileAssert;
 import junitx.framework.ListAssert;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.input.TeeInputStream;
 import org.apache.commons.io.output.TeeOutputStream;
 import org.paxle.core.doc.Field;
 import org.paxle.core.doc.ICommand;
 import org.paxle.core.doc.ICommandProfile;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.core.doc.IDocumentFactory;
 import org.paxle.core.doc.IIndexerDocument;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.LinkInfo;
 import org.paxle.core.io.temp.ITempDir;
 import org.paxle.core.io.temp.ITempFileManager;
 import org.paxle.core.io.temp.impl.TempFileManager;
 
 public class BasicDocumentFactoryTest extends TestCase {
 	private static final File CRAWLER_FILE = new File("src/test/resources/paxle.html");
 	private static final File PARSER_FILE = new File("src/test/resources/paxle.txt");
 
 	private ITempFileManager tmpFileManager;
 	private IDocumentFactory docFactory;
 	GregorianCalendar cal;
 		
 	@SuppressWarnings("unchecked") 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
		this.cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Vienna"));
 		this.cal.set(GregorianCalendar.YEAR, 2009);
 		this.cal.set(GregorianCalendar.MONTH, 11);
 		this.cal.set(GregorianCalendar.DAY_OF_MONTH, 16);
 		this.cal.set(GregorianCalendar.HOUR_OF_DAY,19);
 		this.cal.set(GregorianCalendar.MINUTE,01);
 		this.cal.set(GregorianCalendar.SECOND,14);
 		this.cal.set(GregorianCalendar.MILLISECOND,728);				
 		
 		this.tmpFileManager = new TempFileManager();
 		this.docFactory = new BasicDocumentFactory() {{
 			this.tempFileManager = tmpFileManager;			
 			this.activate(Collections.EMPTY_MAP);
 		}};
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		
 		// cleanup temp files
 		final Map<File,ITempDir> tempFiles = ((TempFileManager)this.tmpFileManager).getFileMap();
 		if (tempFiles != null) {
 			for (File file : tempFiles.keySet()) {
 				assertTrue(file.delete());
 			}
 		}
 	}
 	
 	protected ICrawlerDocument createTestCDoc(Class<?> crawlerDocClass, URI location) throws IOException, ParseException {
 		// creating a dummy crawler-document
 		final ICrawlerDocument cDoc = (ICrawlerDocument) this.docFactory.createDocument(crawlerDocClass);
 		cDoc.setOID(1441654849);
 		cDoc.setStatus(ICrawlerDocument.Status.OK,"CrawlerDocument is OK");
 		cDoc.setLocation(location);
 		cDoc.setCharset("ISO-8859-1");
 		cDoc.setMimeType("text/html");
 		cDoc.setCrawlerDate(cal.getTime());
 		cDoc.setLastModDate(cal.getTime());
 		cDoc.setLanguages(new String[]{"en"});		
 		cDoc.setContent(CRAWLER_FILE);
 		return cDoc;
 	}
 	
 	@SuppressWarnings("serial")
 	protected IParserDocument createTestPDoc (Class<?> parserDocClass) throws IOException {
 		final IParserDocument pDoc = (IParserDocument) this.docFactory.createDocument(parserDocClass);
 		pDoc.setOID(266560296);
 		pDoc.setStatus(IParserDocument.Status.OK,"ParserDocument is OK");
 		pDoc.setTextFile(PARSER_FILE);
 		pDoc.setLastChanged(cal.getTime());
 		pDoc.setTitle("Paxle    - PAXLE Search Framework");
 		pDoc.setAuthor("Paxle");
 		pDoc.setCharset(Charset.forName("UTF-8"));
 		pDoc.setMimeType("text/html");
 		pDoc.setKeywords(Arrays.asList("en","start"));
 		pDoc.setLanguages(new HashSet<String>(){{
 			add("en");
 		}});
 		pDoc.setHeadlines(Arrays.asList(new String[]{
 				"Paxle",
 				"What is Paxle?",
 				"What can you do with Paxle?",
 				"Is it difficult to use Paxle?"
 		}));
 		pDoc.setLinks(new HashMap<URI, LinkInfo>(){{
 			put(URI.create("http://www.osgi.org/"), new LinkInfo("OSGi",LinkInfo.Status.FILTERED,"Blocked by Robotx.txt","http://www.paxle.net"));
 			put(URI.create("http://lucene.apache.org/"), new LinkInfo("Lucene"));
 		}});
 		return pDoc;
 	}
 	
 	protected IIndexerDocument createTestIDoc (Class<?> indexerDocClass) throws IOException {
 		final IIndexerDocument iDoc = (IIndexerDocument) this.docFactory.createDocument(indexerDocClass);
 		iDoc.setOID(0);
 		iDoc.setStatus(IIndexerDocument.Status.OK, "IndexerDocument is OK");
 		iDoc.set(IIndexerDocument.AUTHOR, "Paxle");
 		iDoc.set(IIndexerDocument.KEYWORDS,new String[]{"en","start"});
 		iDoc.set(IIndexerDocument.LANGUAGES,new String[]{"en"});
 		iDoc.set(IIndexerDocument.LAST_CRAWLED,new Date(CRAWLER_FILE.lastModified()));
 		iDoc.set(IIndexerDocument.LAST_MODIFIED,new Date(PARSER_FILE.lastModified()));
 		iDoc.set(IIndexerDocument.LOCATION, "http://www.paxle.net");
 		iDoc.set(IIndexerDocument.MIME_TYPE, "text/html");
 		iDoc.set(IIndexerDocument.PROTOCOL, "http");
 		iDoc.set(IIndexerDocument.SIZE, Long.valueOf(new File("src/test/resources/paxle.html").length()));
 		iDoc.set(IIndexerDocument.TITLE,"Paxle");
 		iDoc.set(IIndexerDocument.TEXT,PARSER_FILE);
 		return iDoc;
 	}
 		
 	protected ICommand createTestCommand() throws IOException, ParseException {
 		final URI location = URI.create("http://www.paxle.net");		
 		
 		// creating a dummy crawler-document
 		final ICrawlerDocument cDoc = this.createTestCDoc(BasicCrawlerDocument.class, location);
 		
 		// creating a dummy parser-document
 		final IParserDocument pDoc = this.createTestPDoc(BasicParserDocument.class);
 		
 		// creating a dummy indexer-document
 		final IIndexerDocument iDoc = this.createTestIDoc(BasicIndexerDocument.class);
 		
 		// creating a dummy command
 		final BasicCommand cmd = this.docFactory.createDocument(BasicCommand.class);
 		cmd.setOID(412550205);
 		cmd.setProfileOID(372627797);
 		cmd.setLocation(location);
 		cmd.setCrawlerDocument(cDoc);
 		cmd.setParserDocument(pDoc);
 		cmd.setIndexerDocuments(new IIndexerDocument[]{iDoc});
 		
 		return cmd;
 	}
 	
 	public static void assertEquals(ICommand expected, ICommand actual) throws IOException {
 		if (expected == null && actual == null) return;
 		else if (expected != null && actual == null) fail();
 		else if (expected == null && actual != null) fail();
 		
 		assertEquals(expected.getOID(), actual.getOID());
 		assertEquals(expected.getProfileOID(), actual.getProfileOID());
 		assertEquals(expected.getLocation(), actual.getLocation());
 		
 		final ICrawlerDocument cdoc1 = expected.getCrawlerDocument();
 		final ICrawlerDocument cdoc2 = actual.getCrawlerDocument();
 		assertEquals(cdoc1, cdoc2);
 		
 		final IParserDocument pdoc1 = expected.getParserDocument();
 		final IParserDocument pdoc2 = actual.getParserDocument();
 		assertEquals(pdoc1, pdoc2);
 	}
 	
 	public static void assertEquals(ICrawlerDocument expected, ICrawlerDocument actual) {
 		if (expected == null && actual == null) return;
 		else if (expected != null && actual == null) fail();
 		else if (expected == null && actual != null) fail();
 		
 		assertEquals(expected.getOID(), actual.getOID());
 		assertEquals(expected.getLocation(), actual.getLocation());
 		assertEquals(expected.getCharset(), actual.getCharset());
 		assertEquals(expected.getMimeType(), actual.getMimeType());
 		assertEquals(expected.getCrawlerDate(), actual.getCrawlerDate());
 		assertEquals(expected.getLastModDate(), actual.getLastModDate());
 		ArrayAssert.assertEquals(expected.getLanguages(), actual.getLanguages());
 
 		final File crawlerFile1 = expected.getContent();
 		final File crawlerFile2 = actual.getContent();
 		FileAssert.assertBinaryEquals(crawlerFile1, crawlerFile2);				
 	}
 	
 	public static void assertEquals(IParserDocument expected, IParserDocument actual) throws IOException {
 		if (expected == null && actual == null) return;
 		else if (expected != null && actual == null) fail();
 		else if (expected == null && actual != null) fail();		
 		
 		assertEquals(expected.getOID(), actual.getOID());
 		assertEquals(expected.getStatus(), actual.getStatus());
 		assertEquals(expected.getStatusText(), actual.getStatusText());
 		assertEquals(expected.getLastChanged(), actual.getLastChanged());
 		assertEquals(expected.getTitle(), actual.getTitle());
 		assertEquals(expected.getAuthor(), actual.getAuthor());
 		assertEquals(expected.getCharset(), actual.getCharset());
 		assertEquals(expected.getMimeType(), actual.getMimeType());
 		ListAssert.assertEquals(new ArrayList<String>(expected.getKeywords()), new ArrayList<String>(actual.getKeywords()));
 		ListAssert.assertEquals(new ArrayList<String>(expected.getLanguages()), new ArrayList<String>(actual.getLanguages()));
 		ListAssert.assertEquals(new ArrayList<String>(expected.getHeadlines()), new ArrayList<String>(actual.getHeadlines()));
 
 		final File parserFile1 = expected.getTextFile();
 		final File parserFile2 = actual.getTextFile();
 		FileAssert.assertBinaryEquals(parserFile1, parserFile2);
 		
 		final Map<URI, LinkInfo> links1 = expected.getLinks();
 		final Map<URI, LinkInfo> links2 = actual.getLinks();
 		assertEquals(links1.size(), links2.size());
 		for(Entry<URI,LinkInfo> entries : links1.entrySet()) {
 			final URI key1 = entries.getKey();
 			final LinkInfo value1 = entries.getValue();
 			final LinkInfo value2 = links2.get(key1);
 			assertNotNull(value2);
 			assertEquals(value1, value2);
 		}		
 	}
 	
 	public static void assertEquals(LinkInfo expected, LinkInfo actual) {
 		assertEquals(expected.getStatus(), actual.getStatus());
 		assertEquals(expected.getStatusText(), actual.getStatusText());
 		assertEquals(expected.getStatusCode(), actual.getStatusCode());
 		assertEquals(expected.getTitle(), actual.getTitle());
 		assertEquals(expected.getLinkOrigin(), actual.getLinkOrigin());	
 	}
 	
 	public static void assertEquals(IIndexerDocument expected, IIndexerDocument actual) {
 		if (expected == null && actual == null) return;
 		else if (expected != null && actual == null) fail();
 		else if (expected == null && actual != null) fail();		
 		
 		assertEquals(expected.getStatus(), actual.getStatus());
 		assertEquals(expected.getStatusText(), actual.getStatusText());
 		
 		for (Entry<Field<?>, ?> entries : expected.getFields().entrySet()) {
 			final Field<?> key1 = entries.getKey();
 			final Class<?> type = key1.getType();
 			
 			final Serializable value1 = (Serializable) entries.getValue();			
 			final Serializable value2 = actual.get(key1);
 			assertNotNull(value2);
 			
 			if (type.isAssignableFrom(File.class)) {
 				assertNotSame(value1, value2);
 				FileAssert.assertBinaryEquals((File)value1, (File)value2);
 			} else if (type.isArray()) {
 				if (type.getComponentType().isAssignableFrom(String.class)) {
 					ArrayAssert.assertEquals((String[])value1, (String[])value2);
 				}			
 			} else {
 				assertEquals((Object)value1,(Object)value2);
 			}
 		}		
 	}
 	
 	public void testCreateCommand() throws IOException {
 		ICommand cmd = null;
 		
 		cmd = this.docFactory.createDocument(ICommand.class);
 		assertNotNull(cmd);
 		assertEquals(0, cmd.getOID());
 		assertEquals(-1, cmd.getProfileOID());
 		
 		cmd = this.docFactory.createDocument(BasicCommand.class);
 		assertNotNull(cmd);
 	}
 	
 	public void testCreateCommandProfile() throws IOException {
 		ICommandProfile profile = null;
 		
 		profile = this.docFactory.createDocument(ICommandProfile.class);
 		assertNotNull(profile);
 		
 		profile = this.docFactory.createDocument(BasicCommandProfile.class);
 		assertNotNull(profile);
 	}
 	
 	public void testCreateCrawlerDocument() throws IOException {
 		ICrawlerDocument cdoc = null;
 		
 		cdoc = this.docFactory.createDocument(ICrawlerDocument.class);
 		assertNotNull(cdoc);
 		
 		cdoc = this.docFactory.createDocument(BasicCrawlerDocument.class);
 		assertNotNull(cdoc);
 	}
 	
 	public void testCreateParserDocument() throws IOException {
 		IParserDocument pdoc = null;
 		
 		pdoc = this.docFactory.createDocument(IParserDocument.class);
 		assertNotNull(pdoc);
 		
 		pdoc = this.docFactory.createDocument(BasicParserDocument.class);
 		assertNotNull(pdoc);		
 		
 		pdoc = this.docFactory.createDocument(CachedParserDocument.class);
 		assertNotNull(pdoc);		
 	}
 	
 	public void testCreateIndexerDocument() throws IOException {
 		IIndexerDocument idoc = null;
 		
 		idoc = this.docFactory.createDocument(IIndexerDocument.class);
 		assertNotNull(idoc);
 		
 		idoc = this.docFactory.createDocument(BasicIndexerDocument.class);
 		assertNotNull(idoc);		
 	}
 	
 	public void testMarshalCommand() throws IOException, ParseException {
 		final ICommand cmd = this.createTestCommand();
 		this.docFactory.marshal(cmd, System.out);
 	}
 	
 	public void testUnmarshalBasicCommand() throws IOException {
 		InputStream input = null;
 		try {
 			input = new FileInputStream(new File("src/test/resources/command.xml"));
 			final ICommand cmd = this.docFactory.unmarshal(input, null);
 			assertNotNull(cmd);
 		} finally {
 			if (input != null) input.close();
 		}
 	}
 	
 	public void testMarshalUnmarshalBasicCommand() throws IOException, ParseException {
 		// creating a test command
 		final ICommand cmd1 = this.createTestCommand();
 		
 		// marshal command
 		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
 		final TeeOutputStream out = new TeeOutputStream(System.out, bout);
 		final Map<String, DataHandler> attachments = this.docFactory.marshal(cmd1, out);
 		out.close();
 		
 		// unmarshal command
 		final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());		
 		final ICommand cmd2 = this.docFactory.unmarshal(bin, attachments);		
 		
 		// check if the commands are equal
 		assertNotSame(cmd1, cmd2);
 		assertEquals(cmd1, cmd2);
 	}
 	
 	public void testMarshalUnmarshalBasicCrawlerDocument() throws IOException, ParseException {
 		final ICrawlerDocument expected = this.createTestCDoc(BasicCrawlerDocument.class, URI.create("http://www.paxle.net"));
 		assertNotNull(expected);
 		
 		// marshal crawler-document
 		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
 		final TeeOutputStream out = new TeeOutputStream(System.out, bout);
 		final Map<String, DataHandler> attachments = this.docFactory.marshal(expected, out);
 		out.close();
 		
 		// unmarshal crawler-document
 		final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
 		final ICrawlerDocument actual = this.docFactory.unmarshal(bin, attachments);
 		
 		assertEquals(expected, actual);
 	}	
 	
 	public void testMarshalUnmarshalBasicParserDocument() throws IOException {
 		final IParserDocument expected = this.createTestPDoc(BasicParserDocument.class);
 		assertNotNull(expected);
 		
 		// marshal parser-document
 		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
 		final TeeOutputStream out = new TeeOutputStream(System.out, bout);
 		final Map<String, DataHandler> attachments = this.docFactory.marshal(expected, out);
 		out.close();
 		
 		// unmarshal crawler-document
 		final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
 		final IParserDocument actual = this.docFactory.unmarshal(bin, attachments);
 		
 		assertEquals(expected, actual);
 	}	
 	
 	public void testMarshalUnmarshalBasicIndexerDocument() throws IOException {
 		final IIndexerDocument expected = this.createTestIDoc(BasicIndexerDocument.class);
 		assertNotNull(expected);
 		
 		// marshal parser-document
 		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
 		final TeeOutputStream out = new TeeOutputStream(System.out, bout);
 		final Map<String, DataHandler> attachments = this.docFactory.marshal(expected, out);
 		out.close();
 		
 		// unmarshal crawler-document
 		final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
 		final IIndexerDocument actual = this.docFactory.unmarshal(bin, attachments);
 		
 		assertEquals(expected, actual);
 	}	
 	
 	public void testStoreMarshalledCommand() throws IOException, ParseException {
         // Create the ZIP file
         final File outFile = File.createTempFile("command", ".zip");
         outFile.deleteOnExit();
         ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outFile));
 		
 		// creating a test command
 		final ICommand cmd = this.createTestCommand();
 		
 		// marshal command
 		final ZipEntry commandEntry = new ZipEntry("command.xml");
 		commandEntry.setComment("command.xml");
 		zipOut.putNextEntry(commandEntry);
 		
 		final TeeOutputStream out = new TeeOutputStream(System.out, zipOut);
 		final Map<String, DataHandler> attachments = this.docFactory.marshal(cmd, out);
 		zipOut.closeEntry();
 		
 		// write attachments
 		if (attachments != null) {
 			for (Entry<String, DataHandler> attachment : attachments.entrySet()) {
 				final String cid = attachment.getKey();
 				final DataHandler data = attachment.getValue();
 				
 				final ZipEntry zipEntry = new ZipEntry(cid);
 				zipEntry.setComment(data.getName());
 				zipOut.putNextEntry(zipEntry);
 				
 				IOUtils.copy(data.getInputStream(), zipOut);
 				zipOut.closeEntry();
 			}
 		}
 		zipOut.close();
 		System.out.println("Command written into file: " + outFile.toString());
 		
 		// print content
 		final ZipFile zf = new ZipFile(outFile);
         for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
         	ZipEntry entry = entries.nextElement();
         	System.out.println(entry.getName() + ": " + entry.getComment());
         }
         zf.close();
 	}
 	
 	public void testLoadUnmarshalledCommand() throws IOException, ParseException {
 		final ZipFile zf = new ZipFile(new File("src/test/resources/command.zip"));
 		
 		// process attachments
 		final Map<String, DataHandler> attachments = new HashMap<String, DataHandler>();
         for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
         	final ZipEntry entry = entries.nextElement();
         	final String name = entry.getName();
         	if (name.equals("command.xml")) continue;
         	
         	// create a data-source to load the attachment
         	final DataSource source = new DataSource() {
         		private ZipFile zip = zf;
         		private ZipEntry zipEntry = entry;
         		
 				public String getContentType() {
 					return "application/x-java-serialized-object";
 				}
 
 				public InputStream getInputStream() throws IOException {
 					return this.zip.getInputStream(this.zipEntry);
 				}
 
 				public String getName() {
 					return this.zipEntry.getName();
 				}
 
 				public OutputStream getOutputStream() throws IOException {
 					throw new UnsupportedOperationException();
 				}
         	};
         	final DataHandler handler = new DataHandler(source);
         	attachments.put(name, handler);
         }
         
         // process command
         final ZipEntry commandEntry = zf.getEntry("command.xml");
         final InputStream commandInput = zf.getInputStream(commandEntry);
         
         // marshal command
         TeeInputStream input = new TeeInputStream(commandInput, System.out);
         final ICommand cmd1 = this.docFactory.unmarshal(input, attachments);
         assertNotNull(cmd1);
         zf.close();
         
         final ICommand cmd2 = this.createTestCommand();
         assertEquals(cmd2, cmd1);
 	}
 }
