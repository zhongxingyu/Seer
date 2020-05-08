 /**
  * ImgServer Java REST Client
  * Copyright (C) 2008 Sami Dalouche
  *
  * This file is part of ImgServer Java REST Client.
  *
  * ImgServer Java REST Client is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ImgServer Java REST Client is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with ImgServer.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.sirika.imgserver.client;
 
 import static com.sirika.imgserver.client.ImageFormat.JPEG;
 import static com.sirika.imgserver.client.ImageReference.originalImage;
 import static com.sirika.imgserver.client.ImageScale.width;
 import static com.sirika.imgserver.client.objectmothers.ImageIdObjectMother.cornicheKabyleId;
 import static com.sirika.imgserver.client.objectmothers.ImageIdObjectMother.yemmaGourayaId;
 import static com.sirika.imgserver.client.objectmothers.ImageReferenceObjectMother.cornicheKabyle;
 import static com.sirika.imgserver.client.objectmothers.ImageReferenceObjectMother.yemmaGouraya;
 import static com.sirika.imgserver.client.objectmothers.PictureStreamAssertionUtils.is100x100CornicheKabylePicture;
 import static com.sirika.imgserver.client.objectmothers.PictureStreamAssertionUtils.isCornicheKabylePicture;
 import static com.sirika.imgserver.client.objectmothers.PictureStreamAssertionUtils.isYemmaGourayaPicture;
 import static com.sirika.imgserver.client.objectmothers.PictureStreamSourceObjectMother.textfileresource;
 import static com.sirika.imgserver.client.objectmothers.PictureStreamSourceObjectMother.yemmaGourayaOriginalPictureStream;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.InputStreamSource;
 
 import com.sirika.imgserver.client.impl.RESTfulUrlGenerator;
 import com.sirika.imgserver.client.impl.RESTfulUrlGeneratorTest;
 import com.sirika.imgserver.client.impl.UploadImageCommand;
 import com.sirika.imgserver.client.objectmothers.ImageIdObjectMother;
 import com.sirika.imgserver.client.objectmothers.PictureStreamSourceObjectMother;
 import com.sirika.imgserver.httpclienthelpers.InputStreamSourceBody;
 import com.sirika.imgserver.httpclienthelpers.RepeatableMultipartEntity;
 
 /** 
  * The idea of this class is to specify the behavior (executable specification) of Image Server for borderline cases.
  * 
  * Some of the tests do not directly hit the ImageServer client API as 
  * <ul>
  * <li>the goal is to explicitly reproduce errors that are not possible when using the API (e.g. the client API always generates a correct URLs).</li>
  * <li>we want a clearly-readable specification of image server's behavior</li>
  * </ul>
  * @author Sami Dalouche (sami.dalouche@gmail.com)
  *
  */
 public class ImageServerErrorHandlingIntegrationTest extends AbstractImageServerIntegrationTestCase {
 
     @Autowired private HttpClient httpClient;
     @Autowired @Qualifier("baseUrl") private String baseUrl;
     
     @Before @After public void clean() throws IOException {
 	cleanup();
     }
 
     private void cleanup() throws IOException {
 	for(ImageReference imageReference : Arrays.asList(yemmaGouraya(), cornicheKabyle())) {
 	    try {
 		InputStreamSource source = imageServer.downloadImage(imageReference);
 		source.getInputStream();
 		imageServer.deleteImage(imageReference.getId());
 	    } catch(ResourceNotExistingException e) {
 		// do nothing, it's fine
 	    }
 	}
     }
     
     @Test public void shouldRaise404WhenDownloadingNotExistingOriginalResource() throws ClientProtocolException, IOException {
 	HttpGet httpGet = new HttpGet(baseUrl + "/original/someOriginalResourceThatDoesNotExist");
 	HttpResponse response = httpClient.execute(httpGet);
 	assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
 	httpGet.abort();
     }
     
     @Test public void shouldRaise404WhenDeletingNonExistingOriginalResource() throws ClientProtocolException, IOException {
 	HttpDelete httpDelete = new HttpDelete(baseUrl + "/original/someOriginalResourceThatDoesNotExist");
 	HttpResponse response = httpClient.execute(httpDelete);
 	assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
 	httpDelete.abort();
     }
     
     @Test public void shouldRaise404WhenDownloadingNotExistingDerivedResource() throws ClientProtocolException, IOException {
 	HttpGet httpGet = new HttpGet(baseUrl + "/derived/someDerivedResourceThatDoesNotExist-100x100.jpg");
 	HttpResponse response = httpClient.execute(httpGet);
 	assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
 	httpGet.abort();
     }
     
     @Test public void shouldRaise404WhenDerivedResourceUrlFormatIsInvalid() throws ClientProtocolException, IOException {
 	HttpGet httpGet = new HttpGet(baseUrl + "/derived/derivedResourceThatHasNoSizeNorFormat-100x100.jpg");
 	HttpResponse response = httpClient.execute(httpGet);
 	assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
 	httpGet.abort();
     }
     
     @Test public void shouldRaise400WhenRequestingNotSupportedImageFormat() throws ClientProtocolException, IOException {
 	uploadYemmaGouraya();
 	HttpGet httpGet = new HttpGet(baseUrl + "/derived/" + yemmaGourayaId() + "-100x100.pixar");
 	HttpResponse response = httpClient.execute(httpGet);
 	assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
 	httpGet.abort();
     }
    
    @Test public void shouldRaise405WhenHttpMethodNotSupported() throws ClientProtocolException, IOException {
	uploadYemmaGouraya();
	HttpDelete httpDelete = new HttpDelete(baseUrl + "/derived/" + yemmaGourayaId() + "-100x100.jpg");
	HttpResponse response = httpClient.execute(httpDelete);
	assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getStatusLine().getStatusCode());
	httpDelete.abort();
    }
 
     @Test public void shouldRaise400WhenImageStreamIsNotRecognized() throws ClientProtocolException, IOException {
 	
 	HttpPost httpPost = new HttpPost(baseUrl + "/original/myimage");
 	MultipartEntity entity = new RepeatableMultipartEntity();
 	entity.addPart(UploadImageCommand.UPLOAD_PARAMETER_NAME, new InputStreamSourceBody(textfileresource(), "text/plain", UploadImageCommand.UPLOAD_PARAMETER_NAME));
 	
 	httpPost.setEntity(entity);
 	
 	HttpResponse response = httpClient.execute(httpPost);
 	assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
 	httpPost.abort();
     }
     
     @Test public void shouldRaise400WhenMultipartEntityDoesNotContainRequiredField() throws ClientProtocolException, IOException {
 	HttpPost httpPost = new HttpPost(baseUrl + "/original/myimage");
 	MultipartEntity entity = new RepeatableMultipartEntity();
 	entity.addPart("fuckedupParameterName", new InputStreamSourceBody(yemmaGourayaOriginalPictureStream(), JPEG.mimeType(), "fuckedupParameterName"));
 	
 	httpPost.setEntity(entity);
 	
 	HttpResponse response = httpClient.execute(httpPost);
 	assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
 	httpPost.abort();
     }
     
     @Test public void shouldRaise409WhenImageIdAlreadyExists() throws ClientProtocolException, IOException {
 	uploadYemmaGouraya();
 	HttpResponse response = uploadYemmaGouraya();
 	assertEquals(HttpStatus.SC_CONFLICT, response.getStatusLine().getStatusCode());
     }
     
     @Test public void shouldRaise403WhenSpecifyingUnauthorizedResizeCharacteristics() throws ClientProtocolException, IOException {
 	uploadYemmaGouraya();
 	HttpGet httpGet = new HttpGet(baseUrl + "/derived/" + yemmaGourayaId() + "-25000x25000.jpg");
 	HttpResponse response = httpClient.execute(httpGet);
 	assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
 	httpGet.abort();
     }
     
     private HttpResponse uploadYemmaGouraya() throws IOException, ClientProtocolException {
 	HttpPost httpPost = new HttpPost(baseUrl + "/original/" + yemmaGourayaId());
 	MultipartEntity entity = new RepeatableMultipartEntity();
 	entity.addPart(UploadImageCommand.UPLOAD_PARAMETER_NAME, new InputStreamSourceBody(yemmaGourayaOriginalPictureStream(), JPEG.mimeType(), UploadImageCommand.UPLOAD_PARAMETER_NAME));
 	httpPost.setEntity(entity);
 	HttpResponse response = httpClient.execute(httpPost);
 	httpPost.abort();
 	return response;
     }
 
     /*
     @Test
     public void shouldThrowResourceNotExistingExceptionWhenResourceNotFound() throws IOException {
 	ImageReference imageReference = originalImage("anyImageThatNobodyHasEverUploadedOnThisPlanet");
 	try {
 	    InputStreamSource source = imageServer.downloadImage(imageReference);
 	    source.getInputStream();
 	    fail();
 	} catch(ResourceNotExistingException e) {
 	    assertEquals(imageReference, e.getImageReference());
 	} 
     }
     
     @Test
     public void shouldUploadAndDownloadOriginalYemmaGourayaPicture() throws IOException {
 	ImageReference yemmaGouraya = imageServer.uploadImage(yemmaGourayaId(), JPEG, yemmaGourayaOriginalPictureStream());
 	InputStreamSource source = imageServer.downloadImage(yemmaGouraya);
 	assertNotNull(source);
 	assertTrue(isYemmaGourayaPicture(source));
 	assertFalse(isCornicheKabylePicture(source));
 	imageServer.deleteImage(yemmaGourayaId());
     }
     
     @Test(expected=ResourceNotExistingException.class)
     public void shouldNotDownloadDeletedYemmaGourayaPicture() throws IOException {
 	imageServer.uploadImage(yemmaGourayaId(), JPEG, yemmaGourayaOriginalPictureStream());
 	imageServer.deleteImage(yemmaGourayaId());
 	ImageReference imageReference = yemmaGouraya();
 	InputStreamSource source = imageServer.downloadImage(imageReference);
 	source.getInputStream();
     }
     
     @Test
     public void shouldUploadSeveralPicturesAndDownloadCornicheKabylePicture() throws IOException {
 	imageServer.uploadImage(yemmaGourayaId(), JPEG, yemmaGourayaOriginalPictureStream());
 	ImageReference cornicheKabyle = imageServer.uploadImage(cornicheKabyleId(), JPEG, PictureStreamSourceObjectMother.cornicheKabyleOriginalPictureStream());
 	InputStreamSource source = imageServer.downloadImage(cornicheKabyle);
 	assertNotNull(source);
 	assertFalse(isYemmaGourayaPicture(source));
 	assertTrue(isCornicheKabylePicture(source));
 	imageServer.deleteImage(yemmaGourayaId());
 	imageServer.deleteImage(cornicheKabyleId());
     }
     @Test
     public void shouldUploadYemmaGourayaAndDownloadDerivedPicture() throws IOException {
 	ImageReference yemmaGouraya = imageServer.uploadImage(yemmaGourayaId(), JPEG, yemmaGourayaOriginalPictureStream());
 	InputStreamSource source = imageServer.downloadImage(yemmaGouraya.rescaledTo(width(100).by(100)).convertedTo(JPEG));
 	assertNotNull(source);
 	assertTrue(is100x100CornicheKabylePicture(source));
 	assertFalse(isCornicheKabylePicture(source));
 	imageServer.deleteImage(yemmaGourayaId());
     }*/
 }
