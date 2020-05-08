 /**
  * Copyright (c) 2012 Mark S. Kolich
  * http://mark.koli.ch
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.kolich.havalo.client.service;
 
 import static com.kolich.common.entities.KolichCommonEntity.getDefaultGsonBuilder;
 import static com.kolich.common.util.URLEncodingUtils.urlEncode;
 import static com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;
 import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.http.HttpStatus.SC_CREATED;
 import static org.apache.http.HttpStatus.SC_NO_CONTENT;
 import static org.apache.http.HttpStatus.SC_OK;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.protocol.HttpContext;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.kolich.common.either.Either;
 import com.kolich.havalo.client.entities.FileObject;
 import com.kolich.havalo.client.entities.KeyPair;
 import com.kolich.havalo.client.entities.ObjectList;
 import com.kolich.havalo.client.signing.HavaloAbstractSigner;
 import com.kolich.http.blocking.HttpClient4Closure;
 import com.kolich.http.blocking.helpers.EntityConverterClosures.CustomEntityConverterClosure;
 import com.kolich.http.blocking.helpers.GsonClosures.GsonOrHttpFailureClosure;
 import com.kolich.http.blocking.helpers.StatusCodeAndHeaderClosures.StatusCodeOrHttpFailureClosure;
 import com.kolich.http.blocking.helpers.definitions.CustomEntityConverter;
 import com.kolich.http.blocking.helpers.definitions.CustomFailureEntityConverter;
 import com.kolich.http.blocking.helpers.definitions.CustomSuccessEntityConverter;
 import com.kolich.http.common.response.HttpFailure;
 import com.kolich.http.common.response.HttpSuccess;
 
 public final class HavaloClient extends HavaloAbstractService {
 		
 	private static final String API_ACTION_AUTHENTICATE = "authenticate";
 	private static final String API_ACTION_REPOSITORY = "repository";
 	private static final String API_ACTION_OBJECT = "object";
 	
 	private static final String API_PARAM_STARTSWITH = "startsWith";
 	
 	private final HttpClient client_;
 	private final GsonBuilder gson_;
 	
 	public HavaloClient(final HttpClient client,
 		final HavaloAbstractSigner signer, final GsonBuilder gson,
 		final String apiEndpoint) {
 		super(signer, apiEndpoint);
 		client_ = client;
 		gson_ = gson;
 	}
 	
 	public HavaloClient(final HttpClient client,
 		final HavaloAbstractSigner signer, final String apiEndpoint) {
 		this(client, signer, getDefaultGsonBuilder(), apiEndpoint);
 	}
 	
 	public HavaloClient(final HavaloAbstractSigner signer,
 		final String apiEndpoint) {
 		this(getNewInstanceWithProxySelector(), signer, apiEndpoint);
 	}
 	
 	public HavaloClient(final HttpClient client,
 		final HavaloClientCredentials credentials, final String apiEndpoint) {
 		this(client, new HavaloClientSigner(credentials), apiEndpoint);
 	}
 	
 	public HavaloClient(final HavaloClientCredentials credentials,
 		final String apiEndpoint) {
 		this(new HavaloClientSigner(credentials), apiEndpoint);
 	}
 	
 	public HavaloClient(final HttpClient client, final UUID key,
 		final String secret, final String apiEndpoint) {
 		this(client, new HavaloClientCredentials(key, secret), apiEndpoint);
 	}
 	
 	public HavaloClient(final UUID key, final String secret,
 		final String apiEndpoint) {
 		this(new HavaloClientCredentials(key, secret), apiEndpoint);
 	}
 	
 	public HavaloClient(final HttpClient client, final String key,
 		final String secret, final String apiEndpoint) {
 		this(client, UUID.fromString(key), secret, apiEndpoint);
 	}
 	
 	public HavaloClient(final String key, final String secret,
 		final String apiEndpoint) {
 		this(UUID.fromString(key), secret, apiEndpoint);
 	}
 	
 	private abstract class HavaloBaseClosure<T>
 		extends HttpClient4Closure<HttpFailure,T> {
 		private final int expectStatus_;
 		public HavaloBaseClosure(final HttpClient client,
 			final int expectStatus) {
 			super(client);
 			expectStatus_ = expectStatus;
 		}
 		@Override
 		public void before(final HttpRequestBase request) throws Exception {
 			signRequest(request);
 		}
 		@Override
 		public boolean check(final HttpResponse response,
 			final HttpContext context) {
 			return expectStatus_ == response.getStatusLine().getStatusCode();
 		}
 		public final Either<HttpFailure,T> head(final String action,
 			final String... path) {
 			return super.head(buildPath(action, path));
 		}
 	}
 		
 	private abstract class HavaloGsonClosure<T>
 		extends GsonOrHttpFailureClosure<T> {
 		private final int expectStatus_;
 		public HavaloGsonClosure(final HttpClient client, final Gson gson,
 			final Class<T> clazz, final int expectStatus) {
 			super(client, gson, clazz);
 			expectStatus_ = expectStatus;
 		}
 		@Override
 		public void before(final HttpRequestBase request) throws Exception {
 			signRequest(request);
 		}
 		@Override
 		public boolean check(final HttpResponse response,
 			final HttpContext context) {
 			return expectStatus_ == response.getStatusLine().getStatusCode();
 		}
 		@Override
 		public final Either<HttpFailure,T> get(final String action) {
 			return super.get(buildPath(action));
 		}
 		@Override
 		public final Either<HttpFailure,T> post(final String action) {
 			return super.post(buildPath(action));
 		}
 		public final Either<HttpFailure,T> put(final String action,
 			final String... path) {
 			return super.put(buildPath(action, path));
 		}
 	}
 	
 	private abstract class HavaloStatusCodeClosure
 		extends StatusCodeOrHttpFailureClosure {
 		private final int expectStatus_;
 		public HavaloStatusCodeClosure(final HttpClient client,
 			final int expectStatus) {
 			super(client);
 			expectStatus_ = expectStatus;
 		}
 		@Override
 		public void before(final HttpRequestBase request) throws Exception {
 			signRequest(request);
 		}
 		@Override
 		public boolean check(final HttpResponse response,
 			final HttpContext context) {
 			return expectStatus_ == response.getStatusLine().getStatusCode();
 		}
 		public final Either<HttpFailure,Integer> delete(
 			final String action, final String... path) {
 			return super.delete(buildPath(action, path));
 		}
 	}
 	
 	private abstract class HavaloEntityConverterClosure<F,S>
 		extends CustomEntityConverterClosure<F,S> {
 		private final int expectStatus_;
 		public HavaloEntityConverterClosure(final HttpClient client,
 			final CustomEntityConverter<F,S> converter,
 			final int expectStatus) {
 			super(client, converter);
 			expectStatus_ = expectStatus;
 		}
 		@Override
 		public void before(final HttpRequestBase request) throws Exception {
 			signRequest(request);
 		}
 		@Override
 		public boolean check(final HttpResponse response,
 			final HttpContext context) {
 			return expectStatus_ == response.getStatusLine().getStatusCode();
 		}
 		public final Either<F,S> get(final String action,
 			final String... path) {
 			return super.get(buildPath(action, path));
 		}
 	}
 	
 	public Either<HttpFailure,KeyPair> authenticate() {
 		// The POST of auth credentials is only successful when the
 		// resulting status code is a 200 OK.  Any other status
 		// code on the response is failure.
 		return new HavaloGsonClosure<KeyPair>(client_, gson_.create(),
 			KeyPair.class, SC_OK){}.post(API_ACTION_AUTHENTICATE);
 	}
 	
 	public Either<HttpFailure,KeyPair> createRepository() {
 		// The POST of a repository is only successful when the
 		// resulting status code is a 201 Created.  Any other status
 		// code on the response is failure.
 		return new HavaloGsonClosure<KeyPair>(client_, gson_.create(),
			KeyPair.class, SC_CREATED){}.post(API_ACTION_REPOSITORY);
 	}
 	
 	public Either<HttpFailure,Integer> deleteRepository(
 		final UUID repoId) {
 		// The DELETE of a repository is only successful when the
 		// resulting status code is a 204 No Content.  Any other
 		// status code on the response is failure.
 		return new HavaloStatusCodeClosure(client_, SC_NO_CONTENT){}
 			.delete(API_ACTION_REPOSITORY, repoId.toString());
 	}
 	
 	public Either<HttpFailure,ObjectList> listObjects(
 		final String... path) {
 		// The listing of objects is only successful when the
 		// resulting status code is a 200 OK.  Any other status
 		// code on the response is failure.
 		return new HavaloGsonClosure<ObjectList>(client_, gson_.create(), 
 			ObjectList.class, SC_OK) {
 			@Override
 			public void before(final HttpRequestBase request) throws Exception {
 				final URIBuilder builder = new URIBuilder(request.getURI());
 				if(path != null && path.length > 0) {
 					builder.addParameter(API_PARAM_STARTSWITH,
 						varargsToPrefixString(path));
 				}
 				request.setURI(builder.build());
 				super.before(request);
 			}
 		}.get(API_ACTION_REPOSITORY);
 	}
 	
 	public Either<HttpFailure,ObjectList> listObjects() {
 		return listObjects((String[])null);
 	}
 	
 	public Either<HttpFailure,List<Header>> getObject(
 		final OutputStream destination, final String... path) {
 		return getObject(new CustomEntityConverter<HttpFailure,List<Header>>() {
 			@Override
 			public List<Header> success(final HttpSuccess success) throws Exception {
 				// Copy the object.
 				copyLarge(success.getContent(), destination);
 				// Get and return the headers on the HTTP response.
 				// This is where stuff like "Content-Type" and
 				// "Content-Length" live.
 				return Arrays.asList(success.getResponse().getAllHeaders());
 			}
 			@Override
 			public HttpFailure failure(final HttpFailure failure) {
 				return failure;
 			}
 		}, path);
 	}
 	
 	public <F,S> Either<F,S> getObject(
 		final CustomSuccessEntityConverter<S> success,
 		final CustomFailureEntityConverter<F> failure,
 		final String... path) {
 		// Create a new custom entity converter using the provided
 		// success and failure handlers.  This acts as a convenience
 		// "interface" between the entity converters and units of work
 		// that represent separate success and failure handlers.
 		return getObject(new CustomEntityConverter<F,S>() {
 			@Override
 			public S success(final HttpSuccess hSuccess) throws Exception {
 				return success.success(hSuccess);
 			}
 			@Override
 			public F failure(final HttpFailure hFailure) {
 				return failure.failure(hFailure);
 			}
 		}, path);
 	}
 	
 	public <F,S> Either<F,S> getObject(final CustomEntityConverter<F,S> converter,
 		final String... path) {
 		// The GET of an object is only successful when the
 		// resulting status code is a 200 OK.  Any other status
 		// code on the response is failure.
 		return new HavaloEntityConverterClosure<F,S>(client_, converter,
 			SC_OK){}.get(API_ACTION_OBJECT, path);
 	}
 
 	public Either<HttpFailure,List<Header>> getObjectMetaData(
 		final String... path) {
 		// The HEAD of an object is only successful when the
 		// resulting status code is a 200 OK.  Any other status
 		// code on the response is failure.
 		return new HavaloBaseClosure<List<Header>>(client_, SC_OK) {
 			@Override
 			public List<Header> success(final HttpSuccess success) {
 				return Arrays.asList(success.getResponse().getAllHeaders());
 			}
 		}.head(API_ACTION_OBJECT, path);
 	}
 	
 	public Either<HttpFailure,FileObject> putObject(final InputStream input,
 		final long contentLength, final Header[] headers, final String... path) {
 		// The upload of an object is only successful when the
 		// resulting status code is a 200 OK.  Any other status
 		// code on the response is failure.
 		return new HavaloGsonClosure<FileObject>(client_, gson_.create(),
 			FileObject.class, SC_OK) {
 			@Override
 			public void before(final HttpRequestBase request) throws Exception {
 				if(headers != null) {
 					request.setHeaders(headers);
 				}
 				((HttpPut)request).setEntity(new InputStreamEntity(input,
 					contentLength));
 				super.before(request);
 			}
 		}.put(API_ACTION_OBJECT, path);
 	}
 			
 	public Either<HttpFailure,FileObject> putObject(final byte[] input,
 		final Header[] headers, final String... path) {
 		final InputStream is = new ByteArrayInputStream(input);
 		return putObject(is, (long)input.length, headers, path);
 	}
 	
 	public Either<HttpFailure,FileObject> putObject(final byte[] input,
 		final String... path) {
 		return putObject(input, null, path);
 	}
 		
 	public Either<HttpFailure,Integer> deleteObject(final Header[] headers,
 		final String... path) {
 		// The deletion of an object is only successful when the
 		// resulting status code is a 204 No Content.  Any other status
 		// code on the response is failure.
 		return new HavaloStatusCodeClosure(client_, SC_NO_CONTENT) {
 			@Override
 			public void before(final HttpRequestBase request) throws Exception {
 				if(headers != null) {
 					request.setHeaders(headers);
 				}
 				super.before(request);
 			}
 		}.delete(API_ACTION_OBJECT, path);
 	}
 	
 	public Either<HttpFailure,Integer> deleteObject(final String... path) {
 		return deleteObject(null, path);
 	}
 	
 	private static final String buildPath(final String action,
 		final String... path) {
 		final StringBuilder sb = new StringBuilder(SLASH_STRING);
 		sb.append(action);
 		if(path != null) {
 			sb.append(SLASH_STRING).append(urlEncode(
 				varargsToPrefixString(path)));
 		}
 		return sb.toString();
 	}
 	
 	private static final String buildPath(final String action) {
 		return buildPath(action, (String[])null);
 	}
 	
 }
