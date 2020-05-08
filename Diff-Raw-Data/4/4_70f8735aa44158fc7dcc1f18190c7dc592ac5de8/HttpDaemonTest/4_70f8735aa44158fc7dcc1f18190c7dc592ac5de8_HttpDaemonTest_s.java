 /*
  * Copyright (C) 2010 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.novelang.daemon;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.common.collect.Lists;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.ProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectHandler;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import org.fest.assertions.Assertions;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.novelang.ResourceTools;
 import org.novelang.ResourcesForTests;
 import org.novelang.common.filefixture.Directory;
 import org.novelang.common.filefixture.JUnitAwareResourceInstaller;
 import org.novelang.common.filefixture.Resource;
 import org.novelang.configuration.ConfigurationTools;
 import org.novelang.configuration.parse.DaemonParameters;
 import org.novelang.configuration.parse.GenericParameters;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.novelang.outfit.DefaultCharset;
 import org.novelang.outfit.TextTools;
 import org.novelang.produce.DocumentRequest;
 import org.novelang.produce.GenericRequest;
 import org.novelang.rendering.RenditionMimeType;
 import org.novelang.rendering.multipage.MultipageFixture;
 import org.novelang.testing.junit.NameAwareTestClassRunner;
 import org.pdfbox.pdmodel.PDDocument;
 import org.pdfbox.util.PDFTextStripper;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * End-to-end tests with {@link HttpDaemon} and the download of some generated documents.
  *
  * @author Laurent Caillette
  */
 @SuppressWarnings( { "HardcodedFileSeparator" } )
 @RunWith( value = NameAwareTestClassRunner.class )
 public class HttpDaemonTest {
 
   @Test
   public void novellaOk() throws Exception {
 
     final Resource resource = ResourcesForTests.Served.GOOD_PART;
     final String novellaSource = alternateSetup( resource, ISO_8859_1 ) ;
     final String generated = readAsString( new URL(
         "http://localhost:" + HTTP_DAEMON_PORT + "/" +
         resource.getName()
     ) ) ;
     final String shaved = shaveComments( generated ) ;
     save( "generated.novella", generated ) ;
     final String normalizedNovellaSource = TextTools.unixifyLineBreaks( novellaSource ) ;
     final String normalizedShaved = TextTools.unixifyLineBreaks( shaved ) ;
     assertEquals( normalizedNovellaSource, normalizedShaved ) ;
 
   }
 
   @Test
   public void pdfOk() throws Exception {
     final Resource resource = ResourcesForTests.Served.GOOD_PART;
     alternateSetup( resource, ISO_8859_1 ) ;
     final byte[] generated = readAsBytes( new URL(
         "http://localhost:" + HTTP_DAEMON_PORT + "/" + resource.getBaseName() + PDF ) ) ;
     save( "generated.pdf", generated ) ;
     assertTrue( generated.length > 100 ) ;
   }
 
   @Test
   public void correctMimeTypeForPdf() throws Exception {
     final Resource resource = ResourcesForTests.Served.GOOD_PART;
     setup( resource ) ;
     final HttpGet httpGet = new HttpGet(
         "http://localhost:" + HTTP_DAEMON_PORT + "/" + resource.getBaseName() + PDF ) ;
     final HttpResponse httpResponse = new DefaultHttpClient().execute( httpGet ) ;
     final Header[] headers = httpResponse.getHeaders( "Content-type" ) ;
     assertTrue( "Got:" + Arrays.asList( headers ), headers.length > 0 ) ;
     assertEquals( "Got:" + Arrays.asList( headers ), "application/pdf", headers[ 0 ].getValue() ) ;
   }
 
   @Test
   public void greekCharactersOk() throws Exception {
     final Resource novellaGreek = ResourcesForTests.Parts.NOVELLA_GREEK ;
     setup( novellaGreek ) ;
     renderAndCheckStatusCode( novellaGreek, "greek.pdf" );
   }
 
   @Test
   public void polishCharactersOk() throws Exception {
     final Resource novellaPolish = ResourcesForTests.Parts.NOVELLA_POLISH ;
     setup( novellaPolish ) ;
     renderAndCheckStatusCode( novellaPolish, "polish.pdf" );
   }
 
   @Test
   public void emptyFontListingMakesNoSmoke() throws Exception {
     setup() ;
     final byte[] generated = readAsBytes(
         new URL( "http://localhost:" + HTTP_DAEMON_PORT + FontDiscoveryHandler.DOCUMENT_NAME ) ) ;
     save( "generated.pdf", generated ) ;
     final String pdfText = extractPdfText( generated ) ;
     assertThat( pdfText ).contains( "No font found." ) ;
   }
 
   @Test
   public void fontListingMakesNoSmoke() throws Exception {
     daemonSetupWithFonts( ResourcesForTests.FontStructure.Parent.Child.dir ) ;
     final byte[] generated = readAsBytes(
         new URL( "http://localhost:" + HTTP_DAEMON_PORT + FontDiscoveryHandler.DOCUMENT_NAME ) ) ;
     save( "generated.pdf", generated ) ;
     final String pdfText = extractPdfText( generated ) ;
     assertThat( pdfText )
         .contains( ResourcesForTests.FontStructure.Parent.Child.MONO_OBLIQUE.getBaseName() )
         .contains( "There are broken fonts!" )
         .contains( ResourcesForTests.FontStructure.Parent.Child.BAD.getBaseName() )
     ;
 
     LOGGER.debug( "Text extracted from PDF: ", pdfText ) ;
   }
 
   @Test
   public void htmlNoSmoke() throws Exception {
     final Resource resource = ResourcesForTests.Served.GOOD_PART;
     setup( resource ) ;
     final byte[] generated = readAsBytes( new URL(
         "http://localhost:" + HTTP_DAEMON_PORT + "/" + resource.getBaseName() + HTML ) ) ;
     assertTrue( generated.length > 100 ) ;
   }
 
   @Test
   public void htmlBrokenCausesRedirection() throws Exception {
     final Resource resource = ResourcesForTests.Served.BROKEN_NOVELLA;
     setup( resource ) ;
 
     final String brokentDocumentName = resource.getBaseName() + HTML ;
     final String brokenDocumentUrl =
         "http://localhost:" + HTTP_DAEMON_PORT + "/" + brokentDocumentName ;
     final ResponseSnapshot responseSnapshot = followRedirection(
         brokenDocumentUrl ) ;
 
     assertTrue( responseSnapshot.getContent().contains( "Requested:" ) ) ;
 
     assertTrue(
         "Expected link to requested page",
         responseSnapshot.getContent().contains( brokentDocumentName )
     ) ;
 
     assertEquals( 1L, ( long ) responseSnapshot.getLocationsRedirectedTo().size() ) ;
     assertEquals(
         brokenDocumentUrl + GenericRequest.ERRORPAGE_SUFFIX,
         responseSnapshot.getLocationsRedirectedTo().get( 0 ).getValue()
     ) ;
   }
 
   @Test
   public void errorPageForUnbrokenHtmlNotBrokenCausesRedirection() throws Exception {
     final Resource resource = ResourcesForTests.Served.GOOD_PART;
     setup( resource ) ;
 
     final ResponseSnapshot responseSnapshot = followRedirection(
         "http://localhost:" + HTTP_DAEMON_PORT + "/" + resource.getBaseName() + HTML +
         GenericRequest.ERRORPAGE_SUFFIX
     ) ;
 
     assertFalse( responseSnapshot.getContent().contains( "Requested:" ) ) ;
 
   }
 
   @Test
   public void listDirectoryContentNoTrailingSolidus() throws Exception {
     final Resource resource = ResourcesForTests.Served.GOOD_PART;
     resourceInstaller.copyWithPath( resource ) ;
     setup() ;
     final ResponseSnapshot responseSnapshot =
         followRedirection( "http://localhost:" + HTTP_DAEMON_PORT ) ;
     checkDirectoryListing( responseSnapshot, resource ) ;
   }
 
   @Test
   public void listDirectoryContentWithTrailingSolidus() throws Exception {
       final Resource resource = ResourcesForTests.Served.GOOD_PART;
       resourceInstaller.copyWithPath( resource ) ;
       setup() ;
     final String urlAsString = "http://localhost:" + HTTP_DAEMON_PORT + "/";
     final ResponseSnapshot responseSnapshot = followRedirection( urlAsString ) ;
       checkDirectoryListing( responseSnapshot, resource ) ;
   }
 
   @Test
   public void listDirectoryContentWithSafari() throws Exception {
     final Resource resource = ResourcesForTests.Served.GOOD_PART;
     resourceInstaller.copyWithPath( resource ) ;
     setup() ;
 
     final String urlAsString = "http://localhost:" + HTTP_DAEMON_PORT + "/";
     final ResponseSnapshot responseSnapshot = followRedirection(
         urlAsString,
         SAFARI_USER_AGENT
     ) ;
 
     assertEquals( 1L, ( long ) responseSnapshot.getLocationsRedirectedTo().size() ) ;
     assertEquals( 
         urlAsString + DirectoryScanHandler.MIME_HINT,
         responseSnapshot.getLocationsRedirectedTo().get( 0 ).getValue()
     ) ;
 
     checkDirectoryListing( responseSnapshot, resource ) ;
 
   }
 
   @Test
   public void testAlternateStylesheetInQueryParameter() throws Exception {
     final Resource resource = ResourcesForTests.Served.GOOD_BOOK ;
     resourceInstaller.copy( resource ) ;
     resourceInstaller.copy( ResourcesForTests.Served.GOOD_PART ) ;
     final File stylesheetFile = resourceInstaller.copyScoped(
         ResourcesForTests.Served.dir, ResourcesForTests.Served.Style.VOID_XSL ) ;
     setup( stylesheetFile.getParentFile(), DefaultCharset.RENDERING ) ;
 
     final byte[] generated = readAsBytes( new URL(
         "http://localhost:" + HTTP_DAEMON_PORT + "/" + resource.getBaseName() + HTML +
                 "?stylesheet=" + ResourcesForTests.Served.Style.VOID_XSL.getName()
     ) ) ;
 
     save( "generated.html", generated ) ;
     assertTrue( new String( generated ).contains( "this is the void stylesheet" ) ) ;
 
   }
 
   @Test
   public void indicateErrorLocationForBrokentStylesheet() throws Exception {
     final Resource resource = ResourcesForTests.Served.GOOD_PART ;
     resourceInstaller.copy( resource ) ;
     final Resource stylesheetResource = ResourcesForTests.Served.Style.ERRONEOUS_XSL ;
     final File stylesheetFile = resourceInstaller.copyScoped(
         ResourcesForTests.Served.dir, stylesheetResource ) ;
     setup( stylesheetFile.getParentFile(), DefaultCharset.RENDERING ) ;
 
 
     final ResponseSnapshot responseSnapshot = followRedirection(
         "http://localhost:" + HTTP_DAEMON_PORT + "/" + resource.getBaseName() + HTML +
             "?stylesheet=" + stylesheetResource.getName()
     ) ;
 
     save( "generated.html", responseSnapshot.getContent() ) ;
     assertThat( responseSnapshot.getContent() ).contains(
        "Line=25; column= 38 - "
        + "xsl:this-is-not-supposed-to-work is not allowed in this position in the stylesheet!"
     ) ;
 
   }
 
   @Test
   public void testAlternateStylesheetInBook() throws Exception {
       final Resource resource = ResourcesForTests.Served.BOOK_ALTERNATE_XSL ;
       resourceInstaller.copy( resource ) ;
       resourceInstaller.copy( ResourcesForTests.Served.GOOD_PART ) ;
       final File stylesheetFile = resourceInstaller.copyScoped(
           ResourcesForTests.Served.dir, ResourcesForTests.Served.Style.VOID_XSL ) ;
       setup( stylesheetFile.getParentFile(), DefaultCharset.RENDERING ) ;
 
       final byte[] generated = readAsBytes( new URL(
           "http://localhost:" + HTTP_DAEMON_PORT + "/" + resource.getBaseName() + HTML ) ) ;
 
       save( "generated.html", generated ) ;
       assertTrue( new String( generated ).contains( "this is the void stylesheet" ) ) ;
 
   }
 
 
 
   @Test
   public void multipage() throws Exception {
 
     final MultipageFixture multipageFixture = new MultipageFixture(
         resourceInstaller,
         ResourcesForTests.Multipage.MULTIPAGE_XSL,
         ResourcesForTests.MainResources.Style.DEFAULT_NOVELLA_XSL
     ) ;
 
     setup( multipageFixture.getStylesheetFile().getParentFile(), DefaultCharset.RENDERING ) ;
 
     save( multipageFixture.getAncillaryDocument0File(),
         buildUrl( multipageFixture.requestForAncillaryDocument0() ) ) ;
     save( multipageFixture.getMainDocumentFile(), buildUrl( multipageFixture.requestForMain() ) ) ;
     save( multipageFixture.getAncillaryDocument1File(),
         buildUrl( multipageFixture.requestForAncillaryDocument1() ) ) ;
 
     multipageFixture.verifyGeneratedFiles() ;
   }
 
 
 // =======
 // Fixture
 // =======
 
   private static final Logger LOGGER = LoggerFactory.getLogger( HttpDaemonTest.class );
 
   static {
     ResourcesForTests.initialize() ;
   }
 
   private static final Charset ISO_8859_1 = Charset.forName( "ISO_8859_1" );
 
 
   private static final int TIMEOUT = 5000 ;
 
 
 
   private static final Pattern STRIP_COMMENTS_PATTERN = Pattern.compile( "%.*\\n" ) ;
 
   private static String shaveComments( final String s ) {
     final Matcher matcher = STRIP_COMMENTS_PATTERN.matcher( s ) ;
     final StringBuffer buffer = new StringBuffer() ;
     while( matcher.find() ) {
       matcher.appendReplacement( buffer, "" ) ;
     }
     matcher.appendTail( buffer ) ;
     return buffer.toString() ;
   }
 
   private static URL buildUrl( final DocumentRequest documentRequest )
       throws MalformedURLException
   {
     return new URL( "http://localhost:" + HTTP_DAEMON_PORT + documentRequest.getOriginalTarget() ) ;
   }
 
   private static String readAsString( final URL url ) throws IOException {
     final StringWriter stringWriter = new StringWriter() ;
     IOUtils.copy( url.openStream(), stringWriter ) ;
     return stringWriter.toString() ;
   }
 
   private static byte[] readAsBytes( final URL url ) throws IOException {
     final ByteArrayOutputStream outputStream = new ByteArrayOutputStream() ;
     IOUtils.copy( url.openStream(), outputStream ) ;
     return outputStream.toByteArray() ;
   }
 
   private void save( final String name, final String document ) throws IOException {
     final File file = new File( resourceInstaller.getTargetDirectory(), name ) ;
     FileUtils.writeStringToFile( file, document ) ;
     LOGGER.info( "Wrote file '", file.getAbsolutePath(), "'" ) ;
   }
 
   private void save( final String name, final byte[] document ) throws IOException {
     save( new File( resourceInstaller.getTargetDirectory(), name ), document ) ;
   }
 
   private static void save( final File file, final byte[] document ) throws IOException {
     FileUtils.writeByteArrayToFile( file, document ) ;
     LOGGER.info( "Wrote file '", file.getAbsolutePath(), "'" ) ;
   }
 
   private static void save( final File file, final URL documentUrl ) throws IOException {
     FileUtils.writeByteArrayToFile( file, readAsBytes( documentUrl ) ) ;
     LOGGER.info( "Wrote file '", file.getAbsolutePath(), "'" ) ;
   }
 
   private static final int HTTP_DAEMON_PORT = 8081 ;
 
 
 
   private static final String PDF = "." + RenditionMimeType.PDF.getFileExtension() ;
   private static final String HTML = "." + RenditionMimeType.HTML.getFileExtension() ;
 
 
 
   @SuppressWarnings( { "InstanceVariableMayNotBeInitialized" } )
   private HttpDaemon httpDaemon ;
 
   private final JUnitAwareResourceInstaller resourceInstaller = new JUnitAwareResourceInstaller() ;
 
 
 
   private void setup() throws Exception {
     daemonSetup( ISO_8859_1 ) ;
   }
 
 
   private String setup( final Resource resource ) throws Exception {
     resourceInstaller.copy( resource ) ;
     daemonSetup( DefaultCharset.RENDERING ) ;
     final String novellaSource = resource.getAsString( DefaultCharset.SOURCE ) ;
     return novellaSource ;
   }
 
   private void setup(
       final File styleDirectory,
       final Charset renderingCharset
   ) throws Exception {
     daemonSetup( styleDirectory, renderingCharset ) ;
   }
 
   private String alternateSetup(
       final Resource resource,
       final Charset renderingCharset
   ) throws Exception {
     resourceInstaller.copy( resource ) ;
     daemonSetup( renderingCharset ) ;
     final String novellaSource = resource.getAsString( DefaultCharset.SOURCE ) ;
     return novellaSource ;
   }
 
 
   private void daemonSetup( final File styleDirectory, final Charset renderingCharset )
       throws Exception
   {
     httpDaemon = new HttpDaemon( ResourceTools.createDaemonConfiguration(
         HTTP_DAEMON_PORT,
         resourceInstaller.getTargetDirectory(),
         styleDirectory,
         renderingCharset
     ) ) ;
     httpDaemon.start() ;
   }
 
   private void daemonSetup( final Charset renderingCharset )
       throws Exception
   {
     httpDaemon = new HttpDaemon( ResourceTools.createDaemonConfiguration(
         HTTP_DAEMON_PORT,
         resourceInstaller.getTargetDirectory(),
         renderingCharset
     ) ) ;
     httpDaemon.start() ;
   }
 
   private void daemonSetupWithFonts( final Directory fontDirectory )
       throws Exception
   {
     final File directoryAsFile = resourceInstaller.copy( fontDirectory ) ;
 
 
     final DaemonParameters daemonParameters = new DaemonParameters(
         resourceInstaller.getTargetDirectory(),
         GenericParameters.OPTIONPREFIX + DaemonParameters.OPTIONNAME_HTTPDAEMON_PORT,
         "" + HTTP_DAEMON_PORT,
         GenericParameters.OPTIONPREFIX + GenericParameters.OPTIONNAME_FONT_DIRECTORIES,
         directoryAsFile.getAbsolutePath()
     ) ;
 
     httpDaemon = new HttpDaemon(
         ConfigurationTools.createDaemonConfiguration( daemonParameters ) ) ;
 
     httpDaemon.start() ;
   }
 
 
   @After
   public void tearDown() throws Exception {
     httpDaemon.stop() ;
   }
 
 
   private static final String CAMINO_USER_AGENT =
       "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en; rv:1.8.1.14) " +
       "Gecko/20080512 Camino/1.6.1 (like Firefox/2.0.0.14)"
   ;
   
   private static final String SAFARI_USER_AGENT =
       "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_4_11; en) " +
       "AppleWebKit/525.18 (KHTML, like Gecko) " +
       "Version/3.1.2 Safari/525.22"
   ;
 
   private static final String DEFAULT_USER_AGENT = CAMINO_USER_AGENT ;
 
   private ResponseSnapshot followRedirection( final String originalUrlAsString )
       throws IOException
   {
     return followRedirection( originalUrlAsString, DEFAULT_USER_AGENT ) ;
   }
 
   /**
    * Follows redirection using {@link HttpClient}'s default, and returns response body.
    */
   private ResponseSnapshot followRedirection(
       final String originalUrlAsString,
       final String userAgent
   ) throws IOException {
 
     final List< Header > locationsRedirectedTo = Lists.newArrayList() ;
 
     final AbstractHttpClient httpClient = new DefaultHttpClient() ;
 
     httpClient.setRedirectHandler( new RecordingRedirectHandler( locationsRedirectedTo ) ) ;
     final HttpParams parameters = new BasicHttpParams() ;
     parameters.setIntParameter( CoreConnectionPNames.SO_TIMEOUT, TIMEOUT ) ;
     final HttpGet httpGet = new HttpGet( originalUrlAsString ) ;
     httpGet.setHeader( "User-Agent", userAgent ); ;
     httpGet.setParams( parameters ) ;
     final HttpResponse httpResponse = httpClient.execute( httpGet ) ;
 
     final ResponseSnapshot responseSnapshot =
         new ResponseSnapshot( httpResponse, locationsRedirectedTo ) ;
     save( "generated.html", responseSnapshot.getContent() ) ;
 
     return responseSnapshot ;
   }
 
   private static void checkDirectoryListing(
       final ResponseSnapshot responseSnapshot ,
       final Resource resource
   ) throws IOException {
     final String fullPath = resource.getFullPath().substring( 1 ) ; // Remove leading solidus.
     final String filePath = fullPath + resource.getBaseName() + ".html" ;
 
     LOGGER.debug( "fullpath='", fullPath, "'" ) ;
     LOGGER.debug( "filepath='", filePath, "'" ) ;
     LOGGER.debug( "Checking response body: \n", responseSnapshot.getContent() ) ;
 
     final String expectedFullPath = "<a href=\"" + fullPath + "\">" + fullPath + "</a>" ;
     LOGGER.debug( "Expected fullPath='", expectedFullPath, "'" ) ;
 
     assertTrue( responseSnapshot.getContent().contains( expectedFullPath ) ) ;
     assertTrue( responseSnapshot.getContent()
         .contains( "<a href=\"" + filePath + "\">" + filePath + "</a>" ) ) ;
   }
 
 
   /**
    * We need to read several values from an {@link HttpResponse} so it would be convenient
    * to use it as return type for {@link HttpDaemonTest#followRedirection(String, String)}
    * but it's impossible to read the streamable content more than once.
    * We turn this by keeping a snapshot of everything needed.
    */
   private static class ResponseSnapshot {
 
     private final String content ;
     private final List< Header > locationsRedirectedTo ;
 
     public ResponseSnapshot(
         final HttpResponse httpResponse,
         final List< Header > locationsRedirectedTo
     ) throws IOException {
       final ByteArrayOutputStream outputStream = new ByteArrayOutputStream() ;
       httpResponse.getEntity().writeTo( outputStream ) ;
       content = new String( outputStream.toByteArray(), DefaultCharset.RENDERING.name() ) ;
       this.locationsRedirectedTo = locationsRedirectedTo ;
     }
 
     public String getContent() {
       return content ;
     }
 
     public List< Header > getLocationsRedirectedTo() {
       return locationsRedirectedTo ;
     }
   }
 
   private static class RecordingRedirectHandler extends DefaultRedirectHandler {
 
     private final List< Header > locations ;
 
     public RecordingRedirectHandler( final List< Header > locations ) {
       this.locations = locations ;
     }
 
     @Override
     public URI getLocationURI( final HttpResponse response, final HttpContext context )
         throws ProtocolException
     {
       locations.addAll( Arrays.asList( response.getHeaders( "Location" ) ) ) ;
       return super.getLocationURI( response, context );
     }
   }
 
 
   private void renderAndCheckStatusCode( final Resource resource, final String savedFileName )
       throws IOException
   {
     final HttpGet httpGet = new HttpGet(
         "http://localhost:" + HTTP_DAEMON_PORT + "/" + resource.getBaseName() + PDF ) ;
     final HttpResponse httpResponse = new DefaultHttpClient().execute( httpGet ) ;
 
     final ByteArrayOutputStream responseContent = new ByteArrayOutputStream() ;
     IOUtils.copy( httpResponse.getEntity().getContent(), responseContent ) ;
     save( savedFileName, responseContent.toByteArray() ) ;
     final int statusCode = httpResponse.getStatusLine().getStatusCode();
     assertEquals( ( long ) HttpStatus.SC_OK, ( long ) statusCode ) ;
   }
 
   private static String extractPdfText( final byte[] pdfBytes ) throws IOException {
     final PDDocument pdfDocument = PDDocument.load( new ByteArrayInputStream( pdfBytes ) ) ;
     try {
       return new PDFTextStripper().getText( pdfDocument ) ;
     } finally {
       pdfDocument.close() ;
     }
   }
 
 }
