 package org.mitre.hdata.test.tests;
 
 import edu.umd.cs.findbugs.annotations.NonNull;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.util.EntityUtils;
 import org.mitre.hdata.test.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Test for section document creation
  *
  * <pre>
  *  6.4 baseURL/sectionpath
  *
  *  6.4.2.2 POST Add new document
  *
  * When adding a new section document, the request Content Type MUST be multipart/form-data
  * if including metadata. In this case, the content part MUST contain the section document.
  * The content part MUST include a Content-Disposition header with a disposition of form-data
  * and a name of content. The metadata part MUST contain the metadata for this section document.
  * The metadata part MUST include a Content-Disposition header with a disposition of form-data
  * and a name of metadata. It is to be treated as informational, since the service MUST compute
  * the valid new metadata based on the requirements found in the HRF specification. The content
  * media type MUST conform to the media type of either the section or the media type identified
  * by metadata of the section document. For XML media types, the document MUST also conform to
  * the XML schema identified by the extensionId for the section or the document metadata.
  *
  * If the content cannot be validated against the media type and the XML schema identified
  * by the content type of this section, the server MUST return a status code of 400.
  *
  * If the request is successful, the new section document MUST show up in the document
  * feed for the section. The server returns a 201 with a Location header containing
  * the URI of the new document.
  *
  * Status Code: 201, 400
  * </pre>
  *
  * @author Jason Mathews, MITRE Corp.
  * Date: 2/20/12 10:45 AM
  */
 public class DocumentCreate extends BaseXmlTest {
 
 	private static final Logger log = LoggerFactory.getLogger(DocumentCreate.class);
 
	protected String sectionPath;
 	private URI documentURL;
 
 	@NonNull
 	@Override
 	public String getId() {
 		return "6.4.2.2";
 	}
 
 	@Override
 	public boolean isRequired() {
 		return true;
 	}
 
 	@NonNull
 	public String getName() {
 		return "POST operation on baseURL/sectionpath adds a new Document";
 	}
 
 	@NonNull
 	public List<Class<? extends TestUnit>> getDependencyClasses() {
 		return Collections.<Class<? extends TestUnit>> singletonList(BaseUrlRootXml.class); // 6.3.1.1
 	}
 
 	public void execute() throws TestException {
 		// pre-conditions: for this test to be executed the prerequisite test BaseUrlRootXml must have passed
 		// with 200 HTTP response and valid root.xml content.
 		TestUnit baseTest = getDependency(BaseUrlRootXml.class);
 		if (baseTest == null) {
 			// assertion failed: this should never be null
 			log.error("Failed to retrieve prerequisite test: BaseUrlRootXml");
 			setStatus(StatusEnumType.SKIPPED, "Failed to retrieve prerequisite test: 6.3.1.1");
 			return;
 		}
 		Map<String, String> extensionPathMap = ((BaseUrlRootXml)baseTest).getExtensionPathMap();
 		if (extensionPathMap.isEmpty()) {
 			log.error("Failed to retrieve prerequisite test results: BaseUrlRootXml");
 			setStatus(StatusEnumType.SKIPPED, "Failed to retrieve prerequisite test results: 6.3.1.1");
 			return;
 		}
 		final Context context = Loader.getInstance().getContext();
 		String extension = context.getString("document.extension");
 		if (StringUtils.isBlank(extension)) {
 			// check pre-conditions and setup
 			log.error("Failed to specify valid section extension property in configuration");
 			setStatus(StatusEnumType.SKIPPED, "Failed to specify valid section extension property in configuration");
 			return;
 		}
 		/*
 		expecting:
 
 		 <?xml version="1.0" encoding="UTF-8"?>
 		 <root xmlns="http://projecthdata.org/hdata/schemas/2009/06/core" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 			<extensions>
 				<extension extensionId="1">http://projecthdata.org/extension/c32</extension>
 				<extension extensionId="2">http://projecthdata.org/hdata/schemas/2009/06/allergy</extension>
 				...
 			</extensions>
 			<sections>
     			<section path="c32" name="C32" extensionId="1"/>
     			<section path="allergies" name="Allergies" extensionId="2"/>
     			...
 			</sections>
 		 </root>
 		 */
 
 		sectionPath = extensionPathMap.get(extension);
 		if (sectionPath == null) {
 			log.error("Failed to find section " + extension + " in root.xml");
 			setStatus(StatusEnumType.SKIPPED, "Failed to find section in test results");
 			return;
 		}
 
 		System.out.println("section path: " + sectionPath);
 		sendRequest(context);
 	}
 
 	protected void sendRequest(Context context) throws TestException {
 		File fileToUpload = context.getPropertyAsFile("document.file");
 		if (fileToUpload == null) {
 			// check pre-conditions and setup
 			log.error("Failed to specify valid document file property in configuration");
 			setStatus(StatusEnumType.SKIPPED, "Failed to specify valid document file property in configuration");
 			return;
 		}
 		final HttpClient client = context.getHttpClient();
 		try {
 			final URI baseUrl = context.getBaseURL(sectionPath);
 			if (log.isDebugEnabled()) {
 				System.out.println("\nURL: " + baseUrl);
 			}
 			FileEntity reqEntity = new FileEntity(fileToUpload, MIME_APPLICATION_XML);
 			/*
 			MultipartEntity reqEntity = new MultipartEntity();
 			FileBody fileBody = new FileBody(fileToUpload, MIME_APPLICATION_XML);
 			reqEntity.addPart("content", fileBody);
 			// reqEntity.addPart("metadata", new StringBody(fileToUpload.getName())); // should be a separate XML profile file ??
 			*/
 			HttpPost post = new HttpPost(baseUrl);
 			post.setEntity(reqEntity);
 			System.out.println("executing request: " + post.getRequestLine());
 			HttpResponse response = context.executeRequest(client, post);
 			int code = response.getStatusLine().getStatusCode();
 			if (log.isDebugEnabled()) {
 				System.out.println("----------------------------------------");
 				System.out.println("POST Response status=" + code);
 				for (Header header : response.getAllHeaders()) {
 					System.out.println("\t" + header.getName() + ": " + header.getValue());
 				}
 				HttpEntity resEntity = response.getEntity();
 				if (resEntity != null) {
 					System.out.println("----------------------------------------");
 					System.out.println(EntityUtils.toString(resEntity));
 				}
 			}
 			/*
 			Expected response:
 
 			HTTP/1.1 201 Created
 			Location: http://rhex.mitre.org:3000/records/4f735367d7d76a43b2000001/vital_signs/4f7af4efd7d76a292600081a
 			Content-Type: text/html; charset=utf-8
 			X-UA-Compatible: IE=Edge
 			ETag: "56f5afbd5beb6b227c829c06b582dc0c"
 			Cache-Control: max-age=0, private, must-revalidate
 			Set-Cookie: _hdata-server_session=xxx...; path=/; HttpOnly
 			X-Request-Id: a0da7b61ce2c04a6b61fc17005746f60
 			X-Runtime: 0.032346
 			Content-Length: 24
 			Connection: keep-alive
 			Server: thin 1.3.1 codename Triple Espresso
 			 */
 			if (code != 201) {
 				setStatus(StatusEnumType.FAILED, "Expected 201 HTTP status code but was: " + code);
 				return;
 			}
 
 			/*
 			Validate document at location exists
 			Subsequent GET to retrieve document:
 
 			GET /records/4f735367d7d76a43b2000001/vital_signs/4f7af4efd7d76a292600081a HTTP/1.1
 			Accept: application/xml
 			Host: rhex.mitre.org:3000
 			Connection: Keep-Alive
 			User-Agent: Apache-HttpClient/4.1.3 (java 1.5)
 			Cookie: _hdata-server_session=xxx...
 			Cookie2: $Version=1
 			 */
 			validateResponse(context, response);
 
 			// NOTE: verify document is added to the section ATOM feed in another test
 			// retrieve & store section ATOM feed for follow-on tests
 			setDocument(getSectionAtomDocument(context, baseUrl));
 
 			setStatus(StatusEnumType.SUCCESS);
 			setResponse(response);
 		} catch (IOException e) {
 			throw new TestException(e);
 		} catch (URISyntaxException e) {
 			throw new TestException(e);
 		} finally {
 			client.getConnectionManager().shutdown();
 		}
 	}
 
 	private void validateResponse(Context context, HttpResponse response)
 			throws TestException, URISyntaxException, IOException
 	{
 		Header header = response.getFirstHeader("Location");
 		if (header == null) {
 			fail("Expected Location header in response");
 			return;
 		}
 		String location = header.getValue();
 		assertTrue(StringUtils.isNotBlank(location), "Expected non-empty value for Location");
 
 		//=========================================================
 		// debug start -- workaround for bug
 		int ind = location.lastIndexOf('/');
 		// Location: http://rhex.mitre.org:3000/records/4f735367d7d76a43b2000001/vital_signs/4f7c90a2d7d76a2926000b7f
 		// => http://rhex.mitre.org:3000/records/1/vital_signs/4f7c90a2d7d76a2926000b7f
 		// [java] <link href="http://rhex.mitre.org:3000/records/1/vital_signs/4f7c90a2d7d76a2926000b7f" type="application/xml"/>
 		if (ind > 0) {
 			location = context.getBaseURL(sectionPath + location.substring(ind)).toASCIIString();
 			// log.debug("XXX: Location {}", location);
 			response.setHeader(header.getName(), location);
 		}
 		// debug end
 		//=========================================================
 
 		final HttpClient client = context.getHttpClient();
 		try {
 			documentURL = new URI(location);
 			HttpGet req = new HttpGet(documentURL);
 			req.setHeader("Accept", MIME_APPLICATION_XML);
 			System.out.println("executing request: " + req.getRequestLine());
 			HttpResponse getResponse = context.executeRequest(client, req);
 			int code = getResponse.getStatusLine().getStatusCode();
 			if (log.isDebugEnabled()) {
 				System.out.println("----------------------------------------");
 				System.out.println("GET Response: " + getResponse.getStatusLine());
 				for (Header h : response.getAllHeaders()) {
 					System.out.println("\t" + h.getName() + ": " + h.getValue());
 				}
 			}
 			//assertEquals(200, code);
 			if (code != 200)
 				addWarning("Expected 200 HTTP status code but was: " + code);
 		} finally {
 			client.getConnectionManager().shutdown();
 		}
 	}
 
 	public String getSectionPath() {
 		return sectionPath;
 	}
 
 	public URI getDocumentURL() {
 		return documentURL;
 	}
 }
