 package org.ocha.hdx.service;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.MultipartEntityBuilder;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.apache.http.impl.client.HttpClients;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class CkanClient {
 
 	private static final Logger log = LoggerFactory.getLogger(CkanClient.class);
 
 	protected final String technicalAPIKey;
 
 	public CkanClient(final String technicalAPIKey) {
 		super();
 		this.technicalAPIKey = technicalAPIKey;
 	}
 
 	protected String performHttpPOST(final String url, final String apiKey, final String query) throws IOException {
 		log.debug(String.format("About to post on : %s", url));
 
 		final HttpPost httpPost = new HttpPost(url);
 		try (CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build()) {
 
			final StringEntity se = new StringEntity(query);
 			httpPost.setEntity(se);
 
			// se.setContentType("text/xml");
 			httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
 			httpPost.addHeader("accept", "application/json");
 
 			if (apiKey != null) {
 				httpPost.addHeader("X-CKAN-API-Key", apiKey);
 			}
 
 			// log.debug("about to send query: " + query);
 
 			final ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			return closeableHttpClient.execute(httpPost, responseHandler);
 		}
 	}
 
 	protected String performHttpPOSTMultipart(final String url, final String apiKey, final String packageId, final File file) {
 		String responseBody = null;
 		final CloseableHttpClient httpclient = HttpClients.createDefault();
 
 		final HttpPost httpPost = new HttpPost(url);
 		try {
 
 			// se.setContentType("text/xml");
 
 			// This does not work yet. CKAN complains if boundary is not set
 			// but the content-Type should be exactly multipart/form-data !!
 			httpPost.addHeader("Content-Type", "multipart/form-data");
 			// httpPost.addHeader("Content-Type", "multipart/form-data; boundary=nwxUuePw4tNxnJqfcLQem2PLZJFBQS");
 			httpPost.addHeader("accept", "application/json");
 
 			if (apiKey != null) {
 				httpPost.addHeader("X-CKAN-API-Key", apiKey);
 			}
 
 			final FileBody bin = new FileBody(file);
 			final StringBody package_id = new StringBody(packageId, ContentType.TEXT_PLAIN);
 
 			final HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("upload", bin).addPart("package_id", package_id).build();
 			// final HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("package_id", package_id).build();
 
 			httpPost.setEntity(reqEntity);
 
 			System.out.println("executing request " + httpPost.getRequestLine());
 			final CloseableHttpResponse response = httpclient.execute(httpPost);
 			final HttpEntity resEntity = response.getEntity();
 			responseBody = EntityUtils.toString(resEntity);
 		} catch (final Exception e) {
 			e.printStackTrace();
 			log.debug(e.toString(), e);
 		}
 		return responseBody;
 	}
 
 }
